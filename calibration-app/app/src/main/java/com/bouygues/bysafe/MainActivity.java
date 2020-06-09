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
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.widget.ImageButton;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.bouygues.bysafe.auth.AuthActivity;
import com.bouygues.bysafe.protection.ProtectionFragment;
import com.bouygues.bysafe.report.ActivitiesFragment;
import com.bouygues.bysafe.handwash.HandwashFragment;

import org.dpppt.android.sdk.DP3T;
import org.dpppt.android.sdk.internal.AppConfigManager;
import org.dpppt.android.sdk.internal.database.Database;
import org.dpppt.android.sdk.internal.database.models.Handshake;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dpppt.android.sdk.internal.logger.Logger;
import org.dpppt.android.sdk.internal.util.Pair;

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
                if (stringListEntry.getValue().size() >= 3) // Nombre de handshake necessaire pour valider un contact
                    contacts += 1;
            }
            try {
                Logger.d("OK HERE WE ARE", "Interval +1");
                AppConfigManager.getInstance(MainApplication.getContext()).addJourneyContact(new Pair<Long, Integer>(now, contacts));
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
                if (stringListEntry.getValue().size() >= 3) {// Nombre de handshake necessaire pour valider un contact
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


    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (!AppConfigManager.getInstance(MainApplication.getContext()).getIsLogged()) {
            if (thread != null) {
                AppConfigManager.getInstance(MainApplication.getContext()).setIsThread(false);
                DP3T.stop(getContext());
                thread.interrupt();
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
