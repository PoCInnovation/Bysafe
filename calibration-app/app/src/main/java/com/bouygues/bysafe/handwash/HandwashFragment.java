package com.bouygues.bysafe.handwash;

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
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.StringRes;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;

import com.bouygues.bysafe.MainActivity;
import com.bouygues.bysafe.R;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import org.dpppt.android.sdk.internal.AppConfigManager;
import org.dpppt.android.sdk.internal.logger.Logger;

import java.util.concurrent.TimeUnit;

public class HandwashFragment extends Fragment {
    private long washingTime = 7200;
    private boolean isRunning = false;
    private long pausedTime = 0;
    private long lastWashingTime = 0;
    private boolean isPaused = false;
    private Handler handler = new Handler();
    AppConfigManager appConfigManager;

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
        appConfigManager = AppConfigManager.getInstance(getContext());
        Bundle _savedInstanceState = appConfigManager.getHandwashBundle();
        Logger.d("BUNDLE", String.valueOf(_savedInstanceState));
        isPaused = _savedInstanceState.getBoolean("is_paused");
        isRunning = _savedInstanceState.getBoolean("is_running");
        lastWashingTime = _savedInstanceState.getLong("last_washing_time");
        pausedTime = _savedInstanceState.getLong("paused_time");
        return inflater.inflate(R.layout.fragment_handwash, container, false);
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final ImageButton infoBarrierGesture = view.findViewById(R.id.button_info_barrier_gesture);
        final Button handWashButton = view.findViewById(R.id.washed_hands);
        final Button pauseHandWashButton = view.findViewById(R.id.pause_resume_hand_wash);

        handWashButton.setOnClickListener(v -> {
            lastWashingTime = System.currentTimeMillis();
            handler.removeCallbacks(timerTask);
            handler.post(timerTask);
            isRunning = true;
            isPaused = false;
            pauseHandWashButton.setText(R.string.button_pause_hand_wash);
        });

        pauseHandWashButton.setOnClickListener(v -> {
            if (!isPaused && isRunning) {
                pausedTime = System.currentTimeMillis() - lastWashingTime;
                handler.removeCallbacks(timerTask);
                pauseHandWashButton.setText(R.string.button_resume_hand_wash);
                isRunning = false;
                isPaused = true;
            } else if (!isRunning && isPaused) {
                lastWashingTime = System.currentTimeMillis() - pausedTime;
                pausedTime = 0;
                handler.post(timerTask);
                pauseHandWashButton.setText(getString(R.string.button_pause_hand_wash));
                isRunning = true;
                isPaused = false;
            }
        });

        infoBarrierGesture.setOnClickListener(v -> {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_fragment_container, BarrierGestureInfoFragment.newInstance())
                    .commit();
        });
    }

    public static HandwashFragment newInstance() {
        return new HandwashFragment();
    }

    @Override
    public void onPause() {
        super.onPause();
        appConfigManager.setHandwashBundle(getBundle());
    }

    private Bundle getBundle() {
        Bundle b = new Bundle();
        b.putLong("last_washing_time", lastWashingTime);
        b.putLong("paused_time", pausedTime);
        b.putBoolean("is_running", isRunning);
        b.putBoolean("is_paused", isPaused);
        return b;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (lastWashingTime > 0 && !isPaused) {
            handler.removeCallbacks(timerTask);
            handler.post(timerTask);
        }
        long millis;

        Logger.d("isPaused", String.valueOf(isPaused));
        if (isPaused) {
            millis = TimeUnit.SECONDS.toMillis(washingTime) - pausedTime;
        } else {
            millis = TimeUnit.SECONDS.toMillis(washingTime) - (System.currentTimeMillis() - lastWashingTime);
        }
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);
        CircularProgressBar pb = getView().findViewById(R.id.circularProgressBar);
        pb.setProgress(millis != 0 ? ((float) seconds / (float) washingTime) * 100 : 0);

        Button pauseHandWashButton = getView().findViewById(R.id.pause_resume_hand_wash);
        TextView text = getView().findViewById(R.id.washed_hands_timer);

        text.setText(getTimerString());
        pauseHandWashButton.setText(isPaused ? R.string.button_resume_hand_wash : R.string.button_pause_hand_wash);
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
        long millis;
        if (isPaused) {
            millis = TimeUnit.SECONDS.toMillis(washingTime) - pausedTime;
        } else {
            millis = TimeUnit.SECONDS.toMillis(washingTime) - (System.currentTimeMillis() - lastWashingTime);
        }
        long days = TimeUnit.MILLISECONDS.toDays(millis);
        millis -= TimeUnit.DAYS.toMillis(days);
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        millis -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        millis -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);

        if (millis > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        }
        return MainActivity.getContext().getString(R.string.button_wash);
    }

    private Runnable timerTask = new Runnable() {
        public void run() {
            long millis = TimeUnit.SECONDS.toMillis(washingTime) - (System.currentTimeMillis() - lastWashingTime);
            long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);
            final View view = getView();
            final TextView text = view != null ? view.findViewById(R.id.washed_hands_timer) : null;
            final CircularProgressBar pb = view != null ? view.findViewById(R.id.circularProgressBar) : null;

            if (text != null) {
                text.setText(getTimerString());
            }
            if (pb != null) {
                pb.setProgress(((float) seconds / (float) washingTime) * 100);
            }

            if (millis > 0) {
                handler.postDelayed(this, 1000);
            } else {
                if (text != null) {
                    text.setText(getTimerString());
                }
                isPaused = false;
                isRunning = false;
                showNotification(MainActivity.getContext(), R.string.app_name, R.string.wash_alert, R.drawable.ic_bysafe);
            }
        }
    };
}
