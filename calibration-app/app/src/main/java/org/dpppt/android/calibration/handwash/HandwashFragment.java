package org.dpppt.android.calibration.handwash;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.dpppt.android.calibration.R;
import org.dpppt.android.sdk.internal.logger.Logger;

import java.util.concurrent.TimeUnit;

public class HandwashFragment extends Fragment {
    private long lastWashedTime = System.currentTimeMillis();
    private Handler handler = new Handler();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
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

            Logger.d("D/WASH", "" + hours + ":" + minutes + ":" + seconds);

            if (seconds > 0){
                handler.postDelayed(this, 1000);
            } else {
            }
        }
    };
}
