package com.bouygues.bysafe.report;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.dpppt.android.sdk.internal.util.Pair;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bouygues.bysafe.MainActivity;
import com.bouygues.bysafe.MainApplication;
import com.bouygues.bysafe.R;
import com.bouygues.bysafe.handwash.BarrierGestureInfoFragment;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import org.dpppt.android.sdk.internal.AppConfigManager;
import org.dpppt.android.sdk.internal.database.Database;
import org.dpppt.android.sdk.internal.database.models.Handshake;
import org.dpppt.android.sdk.internal.logger.Logger;
import org.dpppt.android.sdk.internal.util.Triplet;

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

    private CircularProgressBar pb;
    private TextView percentage_header;
    private ImageView unhappyMasked;
    private ImageView happyMasked;
    private Button reportButton;
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
        percentage_header = view.findViewById(R.id.text_percentage_header);
        happyMasked = view.findViewById(R.id.activities_happy_masked);
        unhappyMasked = view.findViewById(R.id.activities_unhappy_masked);
        reportButton = view.findViewById(R.id.go_to_report_list);
        ImageButton manager = view.findViewById(R.id.button_manager_view);

        if (AppConfigManager.getInstance(getContext()).getPrefManager()) {
            manager.setVisibility(View.VISIBLE);
            manager.setOnClickListener(v -> getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_fragment_container, TeamActivitiesFragment.newInstance()).commit());
        }

        reportButton.setOnClickListener(v -> getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_fragment_container, ActivitiesReportFragment.newInstance())
                .commit());

        getJourneyPercentage();
    }

    private void getJourneyPercentage() {
        try {
            ArrayList<Triplet<Long, Integer, String>> journeyContact = AppConfigManager.getInstance(MainApplication.getContext()).getJourneyContact();
            long total = journeyContact.size();
            float totalTime = (float) total / (float) 12;
            if (total == 0)
                total = 1;
            long contacts = 0;

            for (Triplet<Long, Integer, String> interval : journeyContact) {
                if (interval.second > 0 && interval.third.equals(AppConfigManager.getInstance(MainApplication.getContext()).getPrefBadgeNumber())) {
                    contacts += 1;
                }
            }
            String toDisplay = String.format("Pourcentage d'exposition de la journée (sur %.1f heures): \n", totalTime);
            percentage_header.setText(toDisplay);
            float percent = ((((float) (contacts)) / ((float) total)) * 100);
            if (percent >= 10) {
                pb.setProgressBarColor(Color.parseColor("#FA990C"));
                happyMasked.setVisibility(View.GONE);
                unhappyMasked.setVisibility(View.VISIBLE);
            } else {
                pb.setProgressBarColor(Color.parseColor("#87EA4A"));
                unhappyMasked.setVisibility(View.GONE);
                happyMasked.setVisibility(View.VISIBLE);
            }
            pb.setProgress(percent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
