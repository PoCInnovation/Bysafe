package com.bouygues.bysafe.report;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import org.dpppt.android.sdk.internal.util.Pair;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bouygues.bysafe.MainApplication;
import com.bouygues.bysafe.R;
import org.dpppt.android.sdk.internal.AppConfigManager;
import org.dpppt.android.sdk.internal.database.Database;
import org.dpppt.android.sdk.internal.database.models.Handshake;
import org.dpppt.android.sdk.internal.logger.Logger;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActivitiesFragment extends Fragment {

    private TextView percentage;
    private TextView percentage_header;
    private long interval = 60000;
    private ArrayList<String> list = new ArrayList<>();
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
        percentage_header = view.findViewById(R.id.text_percentage_header);
        getJourneyPercentage();


        ListView ll = view.findViewById(R.id.log_list);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(view.getContext(), R.layout.row, R.id.row_text, list);
        ll.setAdapter(arrayAdapter);
    }

    private void getJourneyPercentage() {
        try {
            ArrayList<Pair<Long, Integer>> journeyContact = AppConfigManager.getInstance(MainApplication.getContext()).getJourneyContact();
            SimpleDateFormat formater = new SimpleDateFormat("HH:mm");
            long total = journeyContact.size();
            float totalTime = (float)total / (float)12;
            if (total == 0)
                total = 1;
            long contacts = 0;

            StringBuilder stringBuilder = new StringBuilder();
            for (long i = atStartOfDay(new java.util.Date()); i < atEndOfDay(new java.util.Date()); i += 3600000) {
                stringBuilder.append(formater.format(i)).append("  ");
                for (long j = i; j < i + 3600000; j += 300000) {
                    if (journeyContact.isEmpty()) {
                        stringBuilder.append('V');
                    }
                    for (Pair<Long, Integer> interval: journeyContact) {
                        if (interval.first >= j && interval.first < j + 300000) {
                            if (interval.second > 0) {
                                stringBuilder.append('O');
                                break;
                            }
                        }
                        stringBuilder.append('V');
                    }
                }
                stringBuilder.append('\n');
            }
            Logger.d("futur display", stringBuilder.toString());

            Logger.d("TOTAL INTERVAL TODAY", Long.toString(total));
            for (Pair<Long, Integer> interval: journeyContact) {
                if (interval.second > 0) {
                    contacts += 1;
                }
                Logger.d("I_WANT_TO_SEE_THE_TIMESTAMP", Long.toString(interval.first));
                list.add(formater.format(interval.first) + String.format(" %d", interval.second));
            }
            String toDisplay = String.format("Pourcentage d'exposition de la journée (sur %.1f heures): \n", totalTime);
            percentage_header.setText(toDisplay);
            float percent = ((((float)(contacts)) / ((float)total)) * 100);
            percentage.setText(String.format("%.1f", percent) + "%");
            percentage.setTextColor(percent >= 20 ? Color.RED : Color.GREEN);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public long atEndOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 18);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    private long atStartOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 8);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }
}
