package org.dpppt.android.calibration.report;

import android.icu.util.ULocale;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import org.dpppt.android.calibration.MainApplication;
import org.dpppt.android.calibration.R;
import org.dpppt.android.sdk.internal.AppConfigManager;
import org.dpppt.android.sdk.internal.database.Database;
import org.dpppt.android.sdk.internal.database.models.Handshake;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ActivitiesFragment extends Fragment {

    private TextView percentage;
    private long interval = 300000;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_activities, container, false);
    }

    public static ActivitiesFragment newInstance() {
        return new ActivitiesFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        percentage = view.findViewById(R.id.text_percentage);
        getJourneyPercentage();
    }

    private void getJourneyPercentage() {
        long journeyStart = AppConfigManager.getInstance(MainApplication.getContext()).getJourneyStart();
        long now = System.currentTimeMillis();

        new Database(MainApplication.getContext()).getHandshakes(response -> {
            long temp = journeyStart;
            long loop = (now - journeyStart) / interval;
            if (loop == 0)
                loop = 1;
            long contacts = 0;
            HashMap<String, List<Handshake>> groupedHandshakes = new HashMap<>();
            Collections.sort(response, (h1, h2) -> Long.compare(h2.getTimestamp(), h1.getTimestamp()));
            while (temp <= now) {
                for (Handshake handShake : response) {
                    if (handShake.getTimestamp() > temp && handShake.getTimestamp() < temp + interval) {
                        byte[] head = new byte[4];
                        for (int i = 0; i < 4; i++) {
                            head[i] = handShake.getEphId().getData()[i];
                        }
                        String identifier = new String(head);
                        if (!groupedHandshakes.containsKey(identifier)) {
                            groupedHandshakes.put(identifier, new ArrayList<>());
                        }
                        groupedHandshakes.get(identifier).add(handShake);
                    }
                }
                for (Map.Entry<String, List<Handshake>> stringListEntry : groupedHandshakes.entrySet()) {
                    if (stringListEntry.getValue().size() >= 2) { // Nombre de handshake necessaire pour valider un contact
                        contacts += 1;
                        break;
                    }
                }
                temp += interval;
            }
            // (float)(contacts / loop) * 100 = pourcentage de temps passer en contact avec des gens
            percentage.setText(String.format("Pourcentage d'exposition\n depuis le début de la journée\n%.1f", ((float)(contacts / loop) * 100)));
        });
    }
}
