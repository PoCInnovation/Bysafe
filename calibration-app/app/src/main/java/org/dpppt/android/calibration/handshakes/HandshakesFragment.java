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

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

import org.dpppt.android.calibration.MainActivity;
import org.dpppt.android.calibration.MainApplication;
import org.dpppt.android.calibration.R;
import org.dpppt.android.sdk.backend.models.ApplicationInfo;
import org.dpppt.android.sdk.internal.AppConfigManager;
import org.dpppt.android.sdk.internal.database.Database;
import org.dpppt.android.sdk.internal.database.models.Handshake;
import org.dpppt.android.sdk.internal.logger.Logger;

public class HandshakesFragment extends Fragment {

    private static final int MAX_NUMBER_OF_MISSING_HANDSHAKES = 3;

    private TextView handshakeList;
    private Thread thread;
    private boolean continueWork = true;

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

        handshakeList = view.findViewById(R.id.handshake_list);

        handshakeList = view.findViewById(R.id.handshake_list);
        loadHandshakes();
    }

    private void loadHandshakes() {
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
                            Logger.d("HandShake", Integer.toString(counter));
                            handshakeList.setText(stringBuilder.toString());
                        });
                        Thread.sleep(1000);
                    }
                } catch (InterruptedException e) {
                }
            }
        };
        thread.start();
    }

    @Override
    public void onDestroyView () {
        super.onDestroyView();
        continueWork = true;

        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
