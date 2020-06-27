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
import android.widget.ImageButton;

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

public class TeamActivitiesReportFragment extends Fragment {

    private ArrayList<String> list = new ArrayList<>();
    private ArrayList<android.widget.TextView> listId = new ArrayList<>();

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
        View view = inflater.inflate(R.layout.fragment_team_activities_report, container, false);
        return view;
    }

    public static TeamActivitiesReportFragment newInstance() {
        return new TeamActivitiesReportFragment();
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

        final ImageButton goToActivities = view.findViewById(R.id.go_to_team_activities);
        goToActivities.setOnClickListener(v -> {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_fragment_container, TeamActivitiesFragment.newInstance())
                    .commit();
        });

        getJourneyPercentage();
    }

    private void getJourneyPercentage() {
        //TODO
//        try {
//            ArrayList<Triplet<Long, Integer, String>> journeyContact = AppConfigManager.getInstance(MainApplication.getContext()).getJourneyContact();
//            SimpleDateFormat formater = new SimpleDateFormat("HH:mm");
//
//            SpannableStringBuilder stringBuilder = new SpannableStringBuilder();
//            for (long i = atStartOfDay(new Date()); i <= atEndOfDay(new Date()); i += 3600000) {
//                stringBuilder.append(formater.format(i)).append("   ");
//                for (long j = i; j < i + 3600000; j += 300000) {
//                    boolean set = false;
//                    for (Triplet<Long, Integer, String> interval : journeyContact) {
//                        if (interval.first >= j && interval.first < j + 300000 && interval.third.equals(AppConfigManager.getInstance(MainApplication.getContext()).getPrefBadgeNumber())) {
//                            if (interval.second > 0) {
//                                stringBuilder.append("V");
//                                stringBuilder.setSpan(new BackgroundColorSpan(Color.RED), stringBuilder.length() - 1, stringBuilder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//                                stringBuilder.setSpan(new ForegroundColorSpan(Color.RED), stringBuilder.length() - 1, stringBuilder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//                            } else {
//                                stringBuilder.append("V");
//                                stringBuilder.setSpan(new BackgroundColorSpan(Color.GREEN), stringBuilder.length() - 1, stringBuilder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//                                stringBuilder.setSpan(new ForegroundColorSpan(Color.GREEN), stringBuilder.length() - 1, stringBuilder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//                            }
//                            set = true;
//                        }
//                    }
//                    if (!set) {
//                        stringBuilder.append("V");
//                        stringBuilder.setSpan(new BackgroundColorSpan(Color.LTGRAY), stringBuilder.length() - 1, stringBuilder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//                        stringBuilder.setSpan(new ForegroundColorSpan(Color.LTGRAY), stringBuilder.length() - 1, stringBuilder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//                    }
//                }
//                long x = (i - atStartOfDay(new Date())) / 3600000;
//                listId.get((int) x).setText(stringBuilder);
//                list.add(stringBuilder.toString());
//                stringBuilder.clear();
//                stringBuilder.clearSpans();
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
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
