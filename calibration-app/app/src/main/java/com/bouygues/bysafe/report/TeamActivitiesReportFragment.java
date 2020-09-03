package com.bouygues.bysafe.report;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.text.TextUtilsCompat;
import androidx.fragment.app.Fragment;

import com.bouygues.bysafe.MainApplication;
import com.bouygues.bysafe.R;

import org.dpppt.android.sdk.internal.AppConfigManager;
import org.dpppt.android.sdk.internal.logger.Logger;
import org.dpppt.android.sdk.internal.util.Triplet;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

public class TeamActivitiesReportFragment extends Fragment {

    private TextView date;
    private ImageView leftChevron;
    private ImageView rightChevron;
    private ArrayList<TeamReportRow> reports = new ArrayList<TeamReportRow>();
    private int daysBefore = 0;
    private int msDay = 86400000;
    private ListView lv;
    private Boolean pressed = false;
    private ArrayList<String> list = new ArrayList<>();

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

        final ImageButton goToActivities = view.findViewById(R.id.go_to_team_activities);
        goToActivities.setOnClickListener(v -> {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_fragment_container, TeamActivitiesFragment.newInstance())
                    .commit();
        });

        date = view.findViewById(R.id.team_activities_date);
        leftChevron = view.findViewById(R.id.switch_date_left);
        rightChevron = view.findViewById(R.id.switch_date_right);
        lv = requireView().findViewById(R.id.team_report_list);
        String currentDate = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(System.currentTimeMillis() - msDay * daysBefore);

        date.setText(currentDate);
        if (daysBefore == 0)
            rightChevron.setVisibility(View.GONE);
        if (daysBefore == 14)
            leftChevron.setVisibility(View.GONE);

        leftChevron.setOnClickListener(v -> {
            if (pressed)
                return;
            pressed = true;
            list.clear();
            reports.clear();
            lv.setAdapter(null);
            daysBefore += 1;
            if (daysBefore == 0)
                rightChevron.setVisibility(View.GONE);
            else
                rightChevron.setVisibility(View.VISIBLE);
            if (daysBefore == 14)
                leftChevron.setVisibility(View.GONE);
            else
                leftChevron.setVisibility(View.VISIBLE);
            date.setText(new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(System.currentTimeMillis() - msDay * daysBefore));
            new getJourneyPercentage().execute();
        });

        rightChevron.setOnClickListener(v -> {
            if (pressed)
                return;
            pressed = true;
            list.clear();
            reports.clear();
            lv.setAdapter(null);
            daysBefore -= 1;
            if (daysBefore == 0)
                rightChevron.setVisibility(View.GONE);
            else
                rightChevron.setVisibility(View.VISIBLE);
            if (daysBefore == 14)
                leftChevron.setVisibility(View.GONE);
            else
                leftChevron.setVisibility(View.VISIBLE);
            date.setText(new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(System.currentTimeMillis() - msDay * daysBefore));
            new getJourneyPercentage().execute();
        });
        new getJourneyPercentage().execute();
    }

    class getJourneyPercentage extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            URL url;
            HttpURLConnection urlConnection = null;

            try {
                url = new URL("https://us-central1-bysafe-4ee9a.cloudfunctions.net/GetReportsFromManager/" + AppConfigManager.getInstance(getContext()).getPrefBadgeNumber());
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();
                BufferedReader br;
                if (200 <= connection.getResponseCode() && connection.getResponseCode() <= 299) {
                    br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                } else {
                    return String.valueOf(connection.getResponseCode());
                }
                StringBuilder sb = new StringBuilder();
                String output;
                while ((output = br.readLine()) != null) {
                    sb.append(output);
                }
                return sb.toString();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            return "503";
        }

        protected void onPostExecute(String response) {
            if (response.length() == 3) {
                Toast.makeText(getContext(), "Nous n'avons pas pu ouvrir la connection.", Toast.LENGTH_SHORT).show();
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main_fragment_container, ActivitiesFragment.newInstance())
                        .commit();
            } else {
                SpannableStringBuilder stringBuilder = new SpannableStringBuilder();
                Logger.d("MANAGER", response);
                try {
                    final JSONObject obj = new JSONObject(response); // json de la réponse
                    JSONObject temp;
                    float percentage;
                    Iterator<String> keys = obj.keys();
                    while (keys.hasNext()) {
                        String key = keys.next();
                        if (obj.get(key) instanceof JSONObject) {
                            int total = 0;
                            int contacts = 0;
                            temp = (JSONObject) obj.get(key);
                            Iterator<String> temp_keys = temp.keys();
                            while (temp_keys.hasNext()) {
                                String str_timestamp = temp_keys.next();
                                if (Long.parseLong(str_timestamp) > (atStartOfDayMidNight(new java.util.Date()) - msDay * daysBefore) && Long.parseLong(str_timestamp) < (atEndOfDayMidNight(new java.util.Date()) - msDay * daysBefore)) {
                                    total += 1;
                                    if (temp.getInt(str_timestamp) > 0) {
                                        contacts += 1;
                                    }
                                }
                            }
                            if (total == 0)
                                total = 1;
                            percentage = ((((float) (contacts)) / ((float) total)) * 100);

                            int totalExposedMinutes = 0;
                            ArrayList<ReportRow> subrows = new ArrayList<ReportRow>();
                            SimpleDateFormat formater = new SimpleDateFormat("HH:mm");
                            for (long i = atStartOfDay(new Date()) - msDay * daysBefore; i <= atEndOfDay(new Date()) - msDay * daysBefore; i += 3600000) {
                                int exposedMinutes = 0;
                                for (long j = i; j < i + 3600000; j += 300000) {
                                    boolean set = false;
                                    temp = (JSONObject) obj.get(key);
                                    Iterator<String> ntemp_keys = temp.keys();
                                    while (ntemp_keys.hasNext()) {
                                        String str_timestamp = ntemp_keys.next();
                                        if (Long.parseLong(str_timestamp) >= j && Long.parseLong(str_timestamp) < j + 300000) {
                                            if (temp.getInt(str_timestamp) > 0) {
                                                stringBuilder.append("X");
                                                exposedMinutes += 5;
                                            } else {
                                                stringBuilder.append("O");
                                            }
                                            set = true;
                                        }
                                    }
                                    if (!set) {
                                        stringBuilder.append("_");
                                    }
                                }
                                totalExposedMinutes += exposedMinutes;
                                ReportRow subrow = new ReportRow(formater.format(i), stringBuilder.toString(), exposedMinutes);
                                subrows.add(subrow);
                                stringBuilder.clear();
                                stringBuilder.clearSpans();
                            }
                            int hours = totalExposedMinutes / 60;
                            int minutes = totalExposedMinutes % 60;
                            TeamReportRow row = new TeamReportRow(key, (int) percentage, hours, minutes, subrows);
                            reports.add(row);
                        }
                    }
                    TeamReportListAdapter itemsAdapter = new TeamReportListAdapter(getContext(), reports);
                    lv.setAdapter(itemsAdapter);
                } catch (JSONException | NullPointerException e) {
                    e.printStackTrace();
                }
            }
            pressed = false;
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

    public long atEndOfDayMidNight(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 59);
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

    private long atStartOfDayMidNight(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

}
