package com.bouygues.bysafe.report;

import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bouygues.bysafe.MainApplication;
import com.bouygues.bysafe.R;

import org.dpppt.android.sdk.internal.AppConfigManager;
import org.dpppt.android.sdk.internal.util.Triplet;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

public class ActivitiesReportFragment extends Fragment {

    private ArrayList<ReportRow> list = new ArrayList<ReportRow>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main_fragment_container, ActivitiesFragment.newInstance())
                        .commit();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_activities_report, container, false);
        return view;
    }

    public static ActivitiesReportFragment newInstance() {
        return new ActivitiesReportFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final ImageButton goToActivities = view.findViewById(R.id.go_to_activities);
        goToActivities.setOnClickListener(v -> getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_fragment_container, ActivitiesFragment.newInstance())
                .commit());
        getJourneyPercentage();
    }

    private void getJourneyPercentage() {
        try {
            ArrayList<Triplet<Long, Integer, String>> journeyContact = AppConfigManager.getInstance(MainApplication.getContext()).getJourneyContact();
            SimpleDateFormat formater = new SimpleDateFormat("HH:mm");
            int exposedMinutes;

            SpannableStringBuilder stringBuilder = new SpannableStringBuilder();
            for (long i = atStartOfDay(new java.util.Date()); i <= atEndOfDay(new java.util.Date()); i += 3600000) {
                exposedMinutes = 0;
                for (long j = i; j < i + 3600000; j += 300000) {
                    boolean set = false;
                    for (Triplet<Long, Integer, String> interval : journeyContact) {
                        if (interval.first >= j && interval.first < j + 300000 && interval.third.equals(AppConfigManager.getInstance(MainApplication.getContext()).getPrefBadgeNumber())) {
                            if (interval.second > 0) {
                                // contact on this interval
                                exposedMinutes += 5;
                                stringBuilder.append("X");
                            } else {
                                // no contact on this interval
                                stringBuilder.append("O");
                            }
                            set = true;
                        }
                    }
                    if (!set) {
                        // no info on this interval
                        stringBuilder.append("_");
                    }
                }
                ReportRow row = new ReportRow(formater.format(i), stringBuilder.toString(), exposedMinutes);
                list.add(row);
                stringBuilder.clear();
                stringBuilder.clearSpans();
            }
            ListView lv = requireView().findViewById(R.id.report_list);
            ReportListAdapter itemsAdapter = new ReportListAdapter(requireContext(), list);
            lv.setAdapter(itemsAdapter);
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
