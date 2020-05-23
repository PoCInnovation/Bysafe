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

import android.content.Intent;
import android.os.Bundle;
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

public class HandshakesFragment extends Fragment {

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

        final ImageButton button = getView().findViewById(R.id.button_settings);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ParameterActivity.class);
                startActivity(intent);
            }
        });

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
