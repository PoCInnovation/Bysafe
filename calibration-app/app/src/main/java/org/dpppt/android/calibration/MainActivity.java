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

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.dpppt.android.calibration.handshakes.HandshakesFragment;
import org.dpppt.android.calibration.parameters.ParametersFragment;
import org.dpppt.android.sdk.internal.AppConfigManager;
import org.dpppt.android.sdk.internal.database.Database;
import org.dpppt.android.sdk.internal.database.models.Handshake;
import org.dpppt.android.sdk.internal.logger.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupNavigationView();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.main_fragment_container, ParametersFragment.newInstance())
                    .commit();
        }
        loadHandshakes();
    }

    private void loadHandshakes() {
        Thread thread = new Thread() {

            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(1000);
                        runOnUiThread(() -> {
                            new Database(MainApplication.getContext()).getHandshakes(response -> {
                                AppConfigManager.getInstance(MainApplication.getContext()).setContactNumber(0);
                                HashMap<String, List<Handshake>> groupedHandshakes = new HashMap<>();
                                Collections.sort(response, (h1, h2) -> Long.compare(h2.getTimestamp(), h1.getTimestamp()));
                                long scanInterval = AppConfigManager.getInstance(MainApplication.getContext()).getScanInterval();
                                long scanDuration = AppConfigManager.getInstance(MainApplication.getContext()).getScanDuration();
                                for (Handshake handShake : response) {
                                    if (handShake.getTimestamp() < System.currentTimeMillis() - (scanInterval - scanDuration) * 2) {
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
                                    if (stringListEntry.getValue().size() >= 1 && !stringListEntry.getKey().equals("null"))
                                        AppConfigManager.getInstance(MainApplication.getContext()).setContactNumber(
                                                AppConfigManager.getInstance(MainApplication.getContext()).getContactNumber() + 1
                                        );
                                }
                            });
                        });
                    }
                } catch (InterruptedException e) {
                }
            }
        };

        thread.start();
    }

    private void setupNavigationView() {
        BottomNavigationView navigationView = findViewById(R.id.main_navigation_view);
        navigationView.inflateMenu(R.menu.menu_navigation_main);

        navigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.action_parameters:
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.main_fragment_container, ParametersFragment.newInstance())
                            .commit();
                    break;
                case R.id.action_handshakes:
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.main_fragment_container, HandshakesFragment.newInstance())
                            .commit();
                    break;
                case R.id.action_activities:
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.main_fragment_container, ParametersFragment.newInstance())
                            .commit();
                    break;
            }
            return true;
        });
    }

}
