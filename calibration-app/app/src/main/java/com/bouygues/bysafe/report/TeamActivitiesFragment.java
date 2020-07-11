package com.bouygues.bysafe.report;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bouygues.bysafe.MainActivity;
import com.bouygues.bysafe.MainApplication;
import com.bouygues.bysafe.R;
import com.bouygues.bysafe.auth.AuthActivity;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import org.dpppt.android.sdk.internal.AppConfigManager;
import org.dpppt.android.sdk.internal.logger.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

import static com.bouygues.bysafe.MainApplication.getContext;

public class TeamActivitiesFragment extends Fragment {
    private TextView handshakeList;
    private CircularProgressBar pb;
    private TextView statusText;
    private ImageView statusImageGood;
    private ImageView statusImageBad;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_team_activities, container, false);
    }

    public static TeamActivitiesFragment newInstance() {
        return new TeamActivitiesFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        pb = view.findViewById(R.id.day_report_circular_progress_bar_team);
        TextView percentage_header = view.findViewById(R.id.text_percentage_header_team);
        String date = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(new Date());
        percentage_header.setText(date);
        TextView reportTv = view.findViewById(R.id.go_to_report_list_team);
        handshakeList = getView().findViewById(R.id.handshake_list2);
        ImageButton goToReportActivities = getView().findViewById(R.id.go_to_report_activities);
        statusText = getView().findViewById(R.id.activities_exposed_gravity);
        statusImageGood = getView().findViewById(R.id.activities_team_status_ok_ic);
        statusImageBad = getView().findViewById(R.id.activities_team_status_alert_ic);

        goToReportActivities.setOnClickListener(v -> {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_fragment_container, ActivitiesFragment.newInstance())
                    .commit();
        });

        reportTv.setOnClickListener(v -> {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_fragment_container, TeamActivitiesReportFragment.newInstance())
                    .commit();
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
                //make calculus and make progress bar
                Logger.d("MANAGER", response);
                int number_of_users = 0; // Nombre de membre de la team qui ont envoyé des rapports dans la journée
                try {
                    final JSONObject obj = new JSONObject(response); // json de la réponse
                    JSONObject temp;
                    float percentage = 0;
                    Iterator<String> keys = obj.keys(); // "nom_prenom"
                    boolean is_here;

                    while (keys.hasNext()) {
                        is_here = false;

                        String key = keys.next();
                        if (obj.get(key) instanceof JSONObject) {
                            // key = nom_prenom
                            int total = 0;
                            int contacts = 0;
                            temp = (JSONObject) obj.get(key);
                            Iterator<String> temp_keys = temp.keys();
                            while (temp_keys.hasNext()) {
                                //str_timestamp = timestamp
                                //temp.getInt = number of contact in 5 minutes
                                String str_timestamp = temp_keys.next();
                                if (Long.parseLong(str_timestamp) > atStartOfDay(new java.util.Date())) {
                                    is_here = true;
                                    total += 1;
                                    if (temp.getInt(str_timestamp) > 0) {
                                        contacts += 1;
                                    }
                                }
                            }
                            if (is_here)
                                number_of_users += 1;
                            if (total == 0)
                                total = 1;
                            percentage += ((((float) (contacts)) / ((float) total)) * 100);
                            Logger.d("MANAGER", String.valueOf((((float) (contacts)) / ((float) total)) * 100));
                        }
                    }
                    if (number_of_users > 0) {
                        percentage /= number_of_users;
                    } else {
                        percentage = 0;
                    }
                    Logger.d("MANAGER", String.valueOf(percentage));
                    handshakeList.setVisibility(View.VISIBLE);
                    handshakeList.setText(String.valueOf(number_of_users));
                    if (number_of_users == 0)
                        number_of_users = 1;
                    percentage /= number_of_users;
                    if (percentage >= 10) {
                        pb.setProgressBarColor(Color.parseColor("#FA990C"));
                        statusImageBad.setVisibility(View.VISIBLE);
                        statusImageGood.setVisibility(View.GONE);
                    } else {
                        pb.setProgressBarColor(Color.parseColor("#0dcf06"));
                        statusImageBad.setVisibility(View.GONE);
                        statusImageGood.setVisibility(View.VISIBLE);
                    }
                    if (percentage <= 10) {
                        statusText.setText("Faible");
                    } else if (percentage > 10 && percentage <= 30) {
                        statusText.setText("Moyen");
                    } else {
                        statusText.setText("Élevé");
                    }
                    pb.setProgress(percentage);
                } catch (JSONException | NullPointerException e) {
                    e.printStackTrace();
                }
            }
        }

        public long atEndOfDay(Date date) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.SECOND, 59);
            calendar.set(Calendar.MILLISECOND, 0);
            return calendar.getTimeInMillis();
        }

        private long atStartOfDay(Date date) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            return calendar.getTimeInMillis();
        }
    }
}
