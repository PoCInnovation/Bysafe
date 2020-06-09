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
import com.mikhaellopez.circularprogressbar.CircularProgressBar;

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

    private CircularProgressBar pb;
    private TextView percentage_header;
    private ImageView unhappyMasked;
    private ImageView happyMasked;
    private long interval = 60000;
    private ArrayList<String> list = new ArrayList<>();
    private ArrayList<android.widget.TextView> listId = new ArrayList<>();
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

        listId.add(view.findViewById(R.id.Hour8));
        listId.add(view.findViewById(R.id.Hour9));
        listId.add(view.findViewById(R.id.Hour10));
        listId.add(view.findViewById(R.id.Hour11));
        listId.add(view.findViewById(R.id.Hour12));
        listId.add(view.findViewById(R.id.Hour13));
        listId.add(view.findViewById(R.id.Hour14));
        listId.add(view.findViewById(R.id.Hour15));
        listId.add(view.findViewById(R.id.Hour16));
        listId.add(view.findViewById(R.id.Hour17));
        listId.add(view.findViewById(R.id.Hour18));

        pb = view.findViewById(R.id.day_report_circular_progress_bar);
        percentage_header = view.findViewById(R.id.text_percentage_header);
        happyMasked = view.findViewById(R.id.activities_happy_masked);
        unhappyMasked = view.findViewById(R.id.activities_unhappy_masked);
        getJourneyPercentage();


        //ListView ll = view.findViewById(R.id.log_list);
        //ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(view.getContext(), R.layout.row, R.id.row_text, list);
        //ll.setAdapter(arrayAdapter);
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

            SpannableStringBuilder stringBuilder = new SpannableStringBuilder();
            for (long i = atStartOfDay(new java.util.Date()); i <= atEndOfDay(new java.util.Date()); i += 3600000) {
                stringBuilder.append(formater.format(i)).append("   ");
                for (long j = i; j < i + 3600000; j += 300000) {
                    boolean set = false;
                    for (Pair<Long, Integer> interval: journeyContact) {
                        if (interval.first >= j && interval.first < j + 300000) {
                            if (interval.second > 0) {
                                stringBuilder.append("V");
                                stringBuilder.setSpan(new BackgroundColorSpan(Color.RED),stringBuilder.length() - 1, stringBuilder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                stringBuilder.setSpan(new ForegroundColorSpan(Color.RED),stringBuilder.length() - 1, stringBuilder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            } else {
                                stringBuilder.append("V");
                                stringBuilder.setSpan(new BackgroundColorSpan(Color.GREEN),stringBuilder.length() - 1, stringBuilder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                stringBuilder.setSpan(new ForegroundColorSpan(Color.GREEN),stringBuilder.length() - 1, stringBuilder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            }
                            set = true;
                        }
                    }
                    if (!set) {
                        stringBuilder.append("V");
                        stringBuilder.setSpan(new BackgroundColorSpan(Color.LTGRAY), stringBuilder.length() - 1, stringBuilder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        stringBuilder.setSpan(new ForegroundColorSpan(Color.LTGRAY),stringBuilder.length() - 1, stringBuilder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                }
                long x = (i - atStartOfDay(new java.util.Date())) / 3600000;
                listId.get((int)x).setText(stringBuilder);
                list.add(stringBuilder.toString());
                stringBuilder.clear();
                stringBuilder.clearSpans();
            }
            for (Pair<Long, Integer> interval: journeyContact) {
                if (interval.second > 0) {
                    contacts += 1;
                }
            }
            String toDisplay = String.format("Pourcentage d'exposition de la journée (sur %.1f heures): \n", totalTime);
            percentage_header.setText(toDisplay);
            float percent = ((((float)(contacts)) / ((float)total)) * 100);
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
