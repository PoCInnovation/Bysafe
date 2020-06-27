/*
 * Copyright (c) 2020 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */

package com.bouygues.bysafe;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.Pair;
import android.widget.ImageButton;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.bouygues.bysafe.auth.AuthActivity;
import com.bouygues.bysafe.protection.ProtectionFragment;
import com.bouygues.bysafe.report.ActivitiesFragment;
import com.bouygues.bysafe.handwash.HandwashFragment;
import com.google.firebase.auth.FirebaseAuth;

import org.dpppt.android.sdk.DP3T;
import org.dpppt.android.sdk.internal.AppConfigManager;
import org.dpppt.android.sdk.internal.database.Database;
import org.dpppt.android.sdk.internal.database.models.Handshake;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dpppt.android.sdk.internal.logger.Logger;
import org.dpppt.android.sdk.internal.util.Triplet;

import io.grpc.internal.IoUtils;

import static java.lang.Math.floor;

public class MainActivity extends AppCompatActivity {

    private static Context context;
    private static Thread thread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppConfigManager appConfigManager = AppConfigManager.getInstance(getContext());
        context = this;

        boolean isLogged = appConfigManager.getIsLogged();
        if (!isLogged) {
            Intent intent = new Intent(this, AuthActivity.class);
            startActivity(intent);
            finish();
        } else {
            if (appConfigManager.getPrefIsFirstOpening()) {
                appConfigManager.setPrefIsFirstOpening(false);
                Intent intent = new Intent(this, OpeningScreen.class);
                startActivity(intent);
                finish();
                return;
            }

            setContentView(R.layout.activity_main);
            setupNavigationView();

            if (savedInstanceState == null) {
                String chosenMenu = appConfigManager.getPrefChosenOpeningMenu();
                BottomNavigationView navigationView = findViewById(R.id.main_navigation_view);
                switch (chosenMenu) {
                    case "protection":
                        navigationView.setSelectedItemId(R.id.action_handshakes);
                        break;
                    case "activities":
                        navigationView.setSelectedItemId(R.id.action_activities);
                        break;
                    default:
                        navigationView.setSelectedItemId(R.id.action_barrier_gestures);
                        break;
                }
                appConfigManager.setPrefChosenOpeningMenu(null);
            }

            final ImageButton logoutButton = findViewById(R.id.logout_button);
            logoutButton.setOnClickListener(v -> {
                if (FirebaseAuth.getInstance().getCurrentUser() != null)
                    FirebaseAuth.getInstance().signOut();
                appConfigManager.setPrefManager(false);
                appConfigManager.setIsLogged(false);
                Intent intent = new Intent(this, AuthActivity.class);
                startActivity(intent);
                finish();
            });

            Calendar calendarThen = Calendar.getInstance();
            Calendar calendarNow = Calendar.getInstance();
            calendarThen.setTimeInMillis(AppConfigManager.getInstance(MainApplication.getContext()).getJourneyStart());
            if (calendarThen.get(Calendar.DAY_OF_MONTH) != calendarNow.get(Calendar.DAY_OF_MONTH)) {
                appConfigManager.setJourneyStart(System.currentTimeMillis());
                try {
                    appConfigManager.setJourneyContact(new ArrayList<>());

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (!appConfigManager.getIsThread()) {
                new SendAllToBack().execute("");
                appConfigManager.setIsThread(true);
                threadContact();
                DP3T.start(getContext());
            }

            // PERMISSIONS
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
            startActivity(new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                    Uri.parse("package:" + MainActivity.getContext().getPackageName())));
            // PERMISSIONS
        }

    }

    public static Context getContext() {
        return context;
    }

    public void threadContact() {
        thread = new Thread() {

            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(1000);
                        updateContact();
                        vibrateContact();
                        long now = System.currentTimeMillis();
                        Logger.d("TIMESTAMP", Long.toString(((long) floor(now / 1000.0)) % 300));
                        if (((long) floor(now / 1000.0)) % 300 == 0) {
                            addContactToReport((((long) (floor(now / 1000.0)) - 300) * 1000));
                        }
                    }
                } catch (InterruptedException ignored) {
                }
            }
        };
        thread.start();
    }

    private void addContactToReport(long now) {
        new Database(MainApplication.getContext()).getHandshakesAfter(now - 300000, response -> {
            int contacts = 0;
            HashMap<String, List<Handshake>> groupedHandshakes = new HashMap<>();
            Collections.sort(response, (h1, h2) -> Long.compare(h2.getTimestamp(), h1.getTimestamp()));
            for (Handshake handShake : response) {
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
            for (Map.Entry<String, List<Handshake>> stringListEntry : groupedHandshakes.entrySet()) {
                if (stringListEntry.getValue().size() >= 6) // Nombre de handshake necessaire sur 5 minutes pour envoyé un rapport
                    contacts += 1;
            }
            try {
                String badge = AppConfigManager.getInstance(MainApplication.getContext()).getPrefBadgeNumber();
                AppConfigManager.getInstance(MainApplication.getContext()).addJourneyContact(new Triplet<>(now, contacts, badge));
                if (AppConfigManager.getInstance(MainApplication.getContext()).getPrefOnline()) {
                    AppConfigManager.getInstance(MainApplication.getContext()).addContactToSend(new Triplet<>(now, contacts, badge));
                    new SendAllToBack().execute("");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void vibrateContact() {
        if (AppConfigManager.getInstance(MainApplication.getContext()).getVibrationTimer() == 0) {
            if (AppConfigManager.getInstance(MainApplication.getContext()).getContactNumber() != 0) {
                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    assert v != null;
                    v.vibrate(VibrationEffect.createWaveform(new long[]{0, 400, 200, 200, 200, 400}, -1));
                } else {
                    assert v != null;
                    v.vibrate(new long[]{0, 400, 200, 200, 200, 400}, -1);
                }
                AppConfigManager.getInstance(MainApplication.getContext()).setVibrationTimer(300); // Modifier le nombre de tour de thread à attendre entre deux vibrations
            }
        } else {
            AppConfigManager.getInstance(MainApplication.getContext()).setVibrationTimer(
                    AppConfigManager.getInstance(MainApplication.getContext()).getVibrationTimer() - 1
            );
        }
    }

    private void updateContact() {
        long scanInterval = AppConfigManager.getInstance(MainApplication.getContext()).getScanInterval();
        long scanDuration = AppConfigManager.getInstance(MainApplication.getContext()).getScanDuration();
        new Database(MainApplication.getContext()).getHandshakesAfter(System.currentTimeMillis() - (scanInterval + scanDuration), response -> {
            AppConfigManager.getInstance(MainApplication.getContext()).setContactNumber(0); // modifier si besoin de debug
            HashMap<String, List<Handshake>> groupedHandshakes = new HashMap<>();
            Collections.sort(response, (h1, h2) -> Long.compare(h2.getTimestamp(), h1.getTimestamp()));

            for (Handshake handShake : response) {
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
            for (Map.Entry<String, List<Handshake>> stringListEntry : groupedHandshakes.entrySet()) {
                if (stringListEntry.getValue().size() >= 2) {// Nombre de handshake necessaire pour valider un contact
                    Logger.d("EPHID", stringListEntry.getKey());
                    AppConfigManager.getInstance(MainApplication.getContext()).setContactNumber(
                            AppConfigManager.getInstance(MainApplication.getContext()).getContactNumber() + 1
                    );
                }
            }
        });
    }

    private void setupNavigationView() {
        BottomNavigationView navigationView = findViewById(R.id.main_navigation_view);
        navigationView.inflateMenu(R.menu.menu_navigation_main);

        navigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.action_barrier_gestures:
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.main_fragment_container, HandwashFragment.newInstance())
                            .commit();
                    break;
                case R.id.action_handshakes:
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.main_fragment_container, ProtectionFragment.newInstance())
                            .commit();
                    break;
                case R.id.action_activities:
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.main_fragment_container, ActivitiesFragment.newInstance())
                            .commit();
                    break;
            }
            return true;
        });
    }



    static class SendAllToBack extends AsyncTask<String, Void, Integer> {


        Integer sendAllToBack(String toSend) {
            URL url;
            HttpURLConnection urlConnection = null;

            try {
                Logger.d("SENDALLTOBACK", "START");
                url = new URL(" https://us-central1-bysafe-4ee9a.cloudfunctions.net/AddReport");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setRequestProperty("Accept", "application/json");
                conn.setFixedLengthStreamingMode(toSend.getBytes(StandardCharsets.UTF_8).length);
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.connect();

                try(OutputStream os = conn.getOutputStream()) {
                    Logger.d("SENDALLTOBACK", toSend);
                    os.write(toSend.getBytes(StandardCharsets.UTF_8));
                }
                conn.connect();
                Logger.d("SENDALLTOBACK", "STOP");
                return conn.getResponseCode();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            return 503;
        }

        @Override
        protected Integer doInBackground(String... strings) {
            try {
                ArrayList<Triplet<Long, Integer, String>> array = AppConfigManager.getInstance(getContext()).getContactToSend();
                if (array.isEmpty())
                    return 0;
                String id = array.get(0).third;
                StringBuilder result = new StringBuilder("{ ");
                result.append("\"").append(id).append("\"").append(": {");
                boolean first = true;
                long to_destroy = 0;
                long final_destroy = 0;
                int val = 0;
                for (Triplet<Long, Integer, String> interval : array) {
                    if (!interval.third.equals(id)) {
                        result.append("}}");
                        val = sendAllToBack(result.toString());
                        Logger.d("SENDALLTOBACK", String.valueOf(val));
                        id = interval.third;
                        if (val != 200) {
                            result = new StringBuilder("{");
                            break;
                        }
                        final_destroy = to_destroy;
                        result = new StringBuilder("{");
                        result.append("\"").append(id).append("\"").append(": {");
                        first = true;
                    }
                    if (!first)
                        result.append(",\n");
                    first = false;
                    result.append("\"").append(interval.first).append("\"").append(": ").append(interval.second);
                    to_destroy += 1;
                }
                result.append("}}");
                if (result.length() > 3) {
                    val = sendAllToBack(result.toString());
                    if (val == 200) {
                        final_destroy = to_destroy;
                    }
                }
                for (int i = 0; i < final_destroy; i++)
                    array.remove(0);
                AppConfigManager.getInstance(getContext()).setContactToSend(array);
                return val;
            } catch (Exception e) {
                e.printStackTrace();
                return 84;
            }
        }

        protected void onPostExecute(Integer responseCode) {
            Logger.d("SENDALLTOBACK", responseCode.toString());
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        new SendAllToBack().execute("");

        if (!AppConfigManager.getInstance(MainApplication.getContext()).getIsLogged()) {
            if (thread != null) {
                AppConfigManager.getInstance(MainApplication.getContext()).setIsThread(false);
                DP3T.stop(getContext());
                thread.interrupt();
                if (AppConfigManager.getInstance(MainApplication.getContext()).getPrefOnline()) {
                    AppConfigManager.getInstance(MainApplication.getContext()).setPrefOnline(false);
                }
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
