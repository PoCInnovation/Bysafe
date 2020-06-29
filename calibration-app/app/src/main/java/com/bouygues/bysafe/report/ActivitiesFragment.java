package com.bouygues.bysafe.report;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bouygues.bysafe.MainApplication;
import com.bouygues.bysafe.R;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import org.dpppt.android.sdk.internal.AppConfigManager;
import org.dpppt.android.sdk.internal.logger.Logger;
import org.dpppt.android.sdk.internal.util.Triplet;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ActivitiesFragment extends Fragment {

    private CircularProgressBar pb;
    private TextView text_date_header;
    private ImageView unhappyMasked;
    private ImageView happyMasked;
    private TextView totalTimeTv;
    private TextView exposedTimeTv;
    private ImageView unhappyIc;
    private ImageView happyIc;
    private long interval = 60000;

    //    private ArrayList<String> list = new ArrayList<>();
//    private ArrayList<android.widget.TextView> listId = new ArrayList<>();
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

        pb = view.findViewById(R.id.day_report_circular_progress_bar);
        text_date_header = view.findViewById(R.id.text_date_header);

        happyMasked = view.findViewById(R.id.activities_happy_masked);
        unhappyMasked = view.findViewById(R.id.activities_unhappy_masked);

        totalTimeTv = view.findViewById(R.id.activities_total_time);
        exposedTimeTv = view.findViewById(R.id.activities_time_exposed);

        happyIc = view.findViewById(R.id.activities_status_ok_ic);
        unhappyIc = view.findViewById(R.id.activities_status_alert_ic);

        TextView reportTv = view.findViewById(R.id.go_to_report_list);
        ImageButton manager = view.findViewById(R.id.button_manager_view);

        if (AppConfigManager.getInstance(getContext()).getPrefManager()) {
            manager.setVisibility(View.VISIBLE);
            manager.setOnClickListener(v -> getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_fragment_container, TeamActivitiesFragment.newInstance()).commit());
        }

        reportTv.setOnClickListener(v -> getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_fragment_container, ActivitiesReportFragment.newInstance())
                .commit());

        getJourneyPercentage();
    }

    @SuppressLint("DefaultLocale")
    private void getJourneyPercentage() {
        try {
            ArrayList<Triplet<Long, Integer, String>> journeyContact = AppConfigManager.getInstance(MainApplication.getContext()).getJourneyContact();
            long total = 0;

            long contacts = 0;

            for (Triplet<Long, Integer, String> interval : journeyContact) {
                if (interval.third.equals(AppConfigManager.getInstance(MainApplication.getContext()).getPrefBadgeNumber())) {
                    total += 1;
                    if (interval.second > 0)
                        contacts += 1;
                }
            }
            float totalTime = (float) total / (float) 12;
            if (total == 0)
                total = 1;

            int hours = (int) totalTime;
            int minutes = (int) ((totalTime - Math.floor(totalTime)) * 60);
            totalTimeTv.setText(String.format("%02d:%02d", hours, minutes));

            float exposedTime = (float) contacts / (float) 12;
            hours = (int) exposedTime;
            minutes = (int) ((exposedTime - Math.floor(exposedTime)) * 60);
            exposedTimeTv.setText(String.format("%02d:%02d", hours, minutes));

            String date = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(new Date());
            text_date_header.setText(date);

            float percent = ((((float) (contacts)) / ((float) total)) * 100);
            if (percent >= 10) {
                pb.setProgressBarColor(Color.parseColor("#FA990C"));
                exposedTimeTv.setTextColor(Color.parseColor("#FA990C"));
                happyMasked.setVisibility(View.GONE);
                unhappyMasked.setVisibility(View.VISIBLE);
                happyIc.setVisibility(View.GONE);
                unhappyIc.setVisibility(View.VISIBLE);
            } else {
                pb.setProgressBarColor(Color.parseColor("#87EA4A"));
                exposedTimeTv.setTextColor(Color.parseColor("#87EA4A"));
                unhappyMasked.setVisibility(View.GONE);
                happyMasked.setVisibility(View.VISIBLE);
                unhappyIc.setVisibility(View.GONE);
                happyIc.setVisibility(View.VISIBLE);
            }
            Logger.d("MANAGER", String.valueOf(percent));
            pb.setProgress(percent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
