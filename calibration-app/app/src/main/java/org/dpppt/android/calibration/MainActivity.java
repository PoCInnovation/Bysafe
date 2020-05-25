/*
 * Copyright (c) 2020 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */

package org.dpppt.android.calibration;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.dpppt.android.calibration.handshakes.HandshakesFragment;
import org.dpppt.android.calibration.parameters.ParameterActivity;
import org.dpppt.android.calibration.report.ActivitiesFragment;
import org.dpppt.android.calibration.handwash.HandwashFragment;

import org.dpppt.android.sdk.internal.AppConfigManager;
import org.dpppt.android.sdk.internal.database.Database;
import org.dpppt.android.sdk.internal.database.models.Handshake;
import org.dpppt.android.sdk.internal.logger.Logger;
import org.dpppt.android.sdk.DP3T;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.http.HEAD;


public class MainActivity extends AppCompatActivity {

    private static Context context;
    private HandwashFragment handwashFragment = HandwashFragment.newInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = this;

        handwashFragment.setArguments(handwashFragment.getBundle());

        setContentView(R.layout.activity_main);

        setupNavigationView();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.main_fragment_container, HandwashFragment.newInstance())
                    .commit();
        }
        Calendar calendarThen = Calendar.getInstance();
        Calendar calendarNow = Calendar.getInstance();
        calendarThen.setTimeInMillis(AppConfigManager.getInstance(MainApplication.getContext()).getJourneyStart());
        if (calendarThen.get(Calendar.DAY_OF_MONTH) != calendarNow.get(Calendar.DAY_OF_MONTH)) {
            AppConfigManager.getInstance(MainApplication.getContext()).setJourneyStart(System.currentTimeMillis());
        }
        uploadContact();
    }

    public static Context getContext() {
        return context;
    }

    private void uploadContact() {
        Thread thread = new Thread() {

            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(1000);
                        runOnUiThread(() -> {
                            new Database(MainApplication.getContext()).getHandshakes(response -> {
                                AppConfigManager.getInstance(MainApplication.getContext()).setContactNumber(0); // modifier si besoin de debug
                                HashMap<String, List<Handshake>> groupedHandshakes = new HashMap<>();
                                Collections.sort(response, (h1, h2) -> Long.compare(h2.getTimestamp(), h1.getTimestamp()));
                                long scanInterval = AppConfigManager.getInstance(MainApplication.getContext()).getScanInterval();
                                long scanDuration = AppConfigManager.getInstance(MainApplication.getContext()).getScanDuration();
                                for (Handshake handShake : response) {
                                    if (handShake.getTimestamp() > System.currentTimeMillis() - (scanInterval + scanDuration) * 2) { // Durée durant laquelle un handshake est pris en compte
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
                                }
                                for (Map.Entry<String, List<Handshake>> stringListEntry : groupedHandshakes.entrySet()) {
                                    if (stringListEntry.getValue().size() >= 2) // Nombre de handshake necessaire pour valider un contact
                                        AppConfigManager.getInstance(MainApplication.getContext()).setContactNumber(
                                                AppConfigManager.getInstance(MainApplication.getContext()).getContactNumber() + 1
                                        );
                                }
                            });
                        });
                        if (AppConfigManager.getInstance(MainApplication.getContext()).getVibrationTimer() == 0) {
                            if (AppConfigManager.getInstance(MainApplication.getContext()).getContactNumber() != 0) {
                                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                                // Durée de vibration = 500
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    assert v != null;
                                    v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                                } else {
                                    assert v != null;
                                    v.vibrate(500);
                                }
                                AppConfigManager.getInstance(MainApplication.getContext()).setVibrationTimer(20); // Modifier le nombre de tour de thread à attendre entre deux vibrations
                            }
                        } else {
                            AppConfigManager.getInstance(MainApplication.getContext()).setVibrationTimer(
                                    AppConfigManager.getInstance(MainApplication.getContext()).getVibrationTimer() - 1
                            );
                        }
                    }
                } catch(InterruptedException ignored) {}
        }};
        thread.start();
    }

    private void setupNavigationView() {
        BottomNavigationView navigationView = findViewById(R.id.main_navigation_view);
        navigationView.inflateMenu(R.menu.menu_navigation_main);

        navigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.action_parameters:
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.main_fragment_container, handwashFragment)
                            .commit();
                    break;
                case R.id.action_handshakes:
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.main_fragment_container, HandshakesFragment.newInstance())
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

}
