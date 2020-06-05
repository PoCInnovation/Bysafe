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
import java.util.ArrayList;
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
            SimpleDateFormat formater = new SimpleDateFormat("hh:mm");
            long total = journeyContact.size();
            float totalTime = (float)total / (float)12;
            if (total == 0)
                total = 1;
            long contacts = 0;

            StringBuilder stringBuilder = new StringBuilder();
            Logger.d("TOTAL INTERVAL TODAY", Long.toString(total));
            for (Pair<Long, Integer> interval: journeyContact) {
                if (interval.second > 0) {
                    contacts += 1;
                }
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
}
