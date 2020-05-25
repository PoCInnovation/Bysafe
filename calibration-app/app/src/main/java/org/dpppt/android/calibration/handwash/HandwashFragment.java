package org.dpppt.android.calibration.handwash;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.StringRes;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;

import org.dpppt.android.calibration.MainActivity;
import org.dpppt.android.calibration.R;
import org.dpppt.android.sdk.internal.logger.Logger;

import java.util.concurrent.TimeUnit;

public class HandwashFragment extends Fragment {
    private long washingTime = 5;
    private boolean isRunning;
    private long pausedTime;
    private long lastWashedTime;
    private Handler handler = new Handler();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Context context = getContext();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            String channelName = context.getString(R.string.app_name);
            NotificationChannel channel = new NotificationChannel(channelName, channelName, NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }
        View view = inflater.inflate(R.layout.fragment_handwash, container, false);
        TextView text = view.findViewById(R.id.washed_hands_timer);
        text.setText(getTimerString());
        return view;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (savedInstanceState != null) {
            if (lastWashedTime > 0) {
                handler.removeCallbacks(timerTask);
                handler.post(timerTask);
            }
        }

        final Button handWashButton = getView().findViewById(R.id.washed_hands);
        final Button pauseHandWashButton = getView().findViewById(R.id.pause_resume_hand_wash);

        pauseHandWashButton.setText(handler.hasCallbacks(timerTask) ? R.string.button_resume_hand_wash : R.string.button_pause_hand_wash);
        handWashButton.setOnClickListener(v -> {
            lastWashedTime = System.currentTimeMillis();
            handler.removeCallbacks(timerTask);
            handler.post(timerTask);
            isRunning = true;
            pauseHandWashButton.setText(R.string.button_pause_hand_wash);
        });
        Logger.d("debuuuuuuuuuug", "isRunning: " + isRunning);

        pauseHandWashButton.setOnClickListener(v -> {
            if (isRunning) {
                // pauses the counter
                pausedTime = System.currentTimeMillis() - lastWashedTime;
                handler.removeCallbacks(timerTask);
                pauseHandWashButton.setText(R.string.button_resume_hand_wash);
                isRunning = false;
            } else if (pausedTime != 0) {
                // resume it
                lastWashedTime = System.currentTimeMillis() - pausedTime;
                pausedTime = 0;
                handler.post(timerTask);
                pauseHandWashButton.setText(getString(R.string.button_pause_hand_wash));
                isRunning = true;
            }
        });
    }

    public static HandwashFragment newInstance() {
        return new HandwashFragment();
    }

    @Override
    public void onPause() {
        super.onPause();
        Bundle b = getArguments();
        if (b == null) {
            b = new Bundle();
        }
        b.putLong("lastWashedTime", lastWashedTime);
        b.putLong("pausedTime", pausedTime);
        b.putBoolean("isRunning", isRunning);
        setArguments(b);
    }

    @Override
    public void onResume() {
        super.onResume();
        Bundle savedInstanceState = getArguments();
        if (savedInstanceState != null) {
            lastWashedTime = savedInstanceState.getLong("lastWashedTime");
            pausedTime = savedInstanceState.getLong("pausedTime");
            isRunning = savedInstanceState.getBoolean("isRunning");
            Logger.d("tatata", "getting args : " + savedInstanceState);
        }
    }

    public static void showNotification(Context context, @StringRes int title, @StringRes int message, @DrawableRes int icon) {
        Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
        PendingIntent contentIntent = null;
        if (launchIntent != null) {
            contentIntent = PendingIntent.getActivity(context, 0, launchIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        Notification notification =
                new NotificationCompat.Builder(context, context.getString(R.string.dp3t_sdk_service_notification_channel))
                        .setContentTitle(context.getString(title))
                        .setContentText(context.getString(message))
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setSmallIcon(icon)
                        .setContentIntent(contentIntent)
                        .setAutoCancel(true)
                        .build();

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(2, notification);
    }

    private String getTimerString() {
        long millis = TimeUnit.SECONDS.toMillis(washingTime) - (System.currentTimeMillis() - lastWashedTime);
        long days = TimeUnit.MILLISECONDS.toDays(millis);
        millis -= TimeUnit.DAYS.toMillis(days);
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        millis -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        millis -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);

        if (seconds > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        }
        return MainActivity.getContext().getString(R.string.button_wash);
    }

    private Runnable timerTask = new Runnable() {
        public void run() {
            long millis = TimeUnit.SECONDS.toMillis(washingTime) - (System.currentTimeMillis() - lastWashedTime);
            long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);
            final View view = getView();
            final TextView text = view != null ? view.findViewById(R.id.washed_hands_timer) : null;

            if (text != null) {
                
                text.setText(getTimerString());
            }

            if (seconds > 0) {
                handler.postDelayed(this, 1000);
            } else {
                if (text != null) {
                    text.setText(getTimerString());
                }
//                isRunning = false;
                showNotification(MainActivity.getContext(), R.string.app_name, R.string.wash_alert, R.drawable.ic_bysafe);
            }
        }
    };
}
