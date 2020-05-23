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

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;

import org.dpppt.android.calibration.R;

import java.util.concurrent.TimeUnit;

public class HandwashFragment extends Fragment {
    private long lastWashedTime = System.currentTimeMillis();
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
        return inflater.inflate(R.layout.fragment_handwash, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final Button button = getView().findViewById(R.id.washed_hands);
        button.setOnClickListener(v -> {
            lastWashedTime = System.currentTimeMillis();
            handler.removeCallbacks(timerTask);
            handler.post(timerTask);
        });
    }

    public static HandwashFragment newInstance() {
        return new HandwashFragment();
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

    private Runnable timerTask = new Runnable() {
        public void run() {
            long millis = TimeUnit.SECONDS.toMillis(5) - (System.currentTimeMillis() - lastWashedTime);
            long days = TimeUnit.MILLISECONDS.toDays(millis);
            millis -= TimeUnit.DAYS.toMillis(days);
            long hours = TimeUnit.MILLISECONDS.toHours(millis);
            millis -= TimeUnit.HOURS.toMillis(hours);
            long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
            millis -= TimeUnit.MINUTES.toMillis(minutes);
            long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);

            final Button button = getView().findViewById(R.id.washed_hands);
            button.setText("" + hours + ":" + minutes + ":" + seconds);

            if (seconds > 0){
                handler.postDelayed(this, 1000);
            } else {
                button.setText(getContext().getString(R.string.button_wash));
                showNotification(getContext(), R.string.app_name,  R.string.wash_alert, R.drawable.ic_bysafe);
            }
        }
    };
}
