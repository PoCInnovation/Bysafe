/*
 * Copyright (c) 2020 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package org.dpppt.android.calibration.handshakes;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.dpppt.android.calibration.R;
import org.dpppt.android.calibration.parameters.ParameterActivity;
import org.dpppt.android.sdk.internal.AppConfigManager;
import org.dpppt.android.sdk.internal.logger.Logger;

import java.util.Objects;

public class HandshakesFragment extends Fragment {

    private TextView handshakeList;
    private Thread thread;
    private boolean continueWork = true;
    private static final int REQUEST_CODE_PERMISSION_LOCATION = 1;

    public static HandshakesFragment newInstance() {
        return new HandshakesFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_handshakes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final ImageButton button = getView().findViewById(R.id.button_settings);
        button.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ParameterActivity.class);
            startActivity(intent);
        });

        handshakeList = getView().findViewById(R.id.handshake_list);

        loadContacts();

        // PERMISSIONS
        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_CODE_PERMISSION_LOCATION);

        startActivity(new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                Uri.parse("package:" + requireContext().getPackageName())));
        // PERMISSIONS
    }

    private void loadContacts() {
        handshakeList.setText("Loading...");

        thread = new Thread() {

            @Override
            public void run() {
                try {
                    while (!isInterrupted() && getActivity() != null && continueWork) {

                        getActivity().runOnUiThread(() -> {
                            StringBuilder stringBuilder = new StringBuilder();

                            int counter = AppConfigManager.getInstance(getContext()).getContactNumber();
                            stringBuilder.append(counter);
                            Logger.d("Contacts", Integer.toString(counter));
                            handshakeList.setText(stringBuilder.toString());
                        });
                        for (int i = 0; i < 10; i++) {
                            if (continueWork) {
                                Thread.sleep(100);
                            } else {
                                return;
                            }
                        }
                    }
                } catch (InterruptedException e) {
                }
            }
        };
        thread.start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        continueWork = false;

        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
