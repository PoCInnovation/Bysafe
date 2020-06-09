/*
 * Copyright (c) 2020 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package com.bouygues.bysafe.protection;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.media.Image;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bouygues.bysafe.R;

import org.dpppt.android.sdk.internal.AppConfigManager;
import org.dpppt.android.sdk.internal.logger.Logger;

public class ProtectionFragment extends Fragment {

    private TextView handshakeList;
    private TextView statusMessage;
    private ImageView centerHappy;
    private ImageView centerUnhappy;
    private ImageView discHappy;
    private ImageView discUnhappy;
    private GradientDrawable mainBackground;
    private Thread thread;
    private boolean continueWork = true;
    private static final int REQUEST_CODE_PERMISSION_LOCATION = 1;

    public static ProtectionFragment newInstance() {
        return new ProtectionFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_protection, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

//        final ImageButton button = getView().findViewById(R.id.button_settings);
//        button.setOnClickListener(v -> {
//            Intent intent = new Intent(getActivity(), ParameterActivity.class);
//            startActivity(intent);
//        });

        handshakeList = getView().findViewById(R.id.handshake_list);
        statusMessage = getView().findViewById(R.id.protection_status_message);

        centerHappy = getView().findViewById(R.id.image_view_happy_masked);
        centerUnhappy = getView().findViewById(R.id.image_view_unhappy_masked);

        discHappy = getView().findViewById(R.id.image_view_disc_happy);
        discUnhappy = getView().findViewById(R.id.image_view_disc_unhappy);

        mainBackground = (GradientDrawable) getView().findViewById(R.id.main_relative_layout_protection).getBackground();

        loadContacts();
    }

    private void loadContacts() {
        thread = new Thread() {

            @Override
            public void run() {
                try {
                    while (!isInterrupted() && getActivity() != null && continueWork) {

                        getActivity().runOnUiThread(() -> {

                            int counter = AppConfigManager.getInstance(getContext()).getContactNumber();
                            Logger.d("Contacts", Integer.toString(counter));
                            if (counter == 0) {
                                handshakeList.setText("");
                                statusMessage.setText(getString(R.string.be_vigilant));
                                handshakeList.setVisibility(View.GONE);
                                centerUnhappy.setVisibility(View.GONE);
                                centerHappy.setVisibility(View.VISIBLE);
                                discUnhappy.setVisibility(View.GONE);
                                discHappy.setVisibility(View.VISIBLE);
                                mainBackground.setColor(Color.argb(255, 135, 234, 74));
                            } else {
                                statusMessage.setText(getString(R.string.protect_yourself));
                                handshakeList.setText(String.valueOf(counter));
                                handshakeList.setVisibility(View.VISIBLE);
                                centerUnhappy.setVisibility(View.VISIBLE);
                                centerHappy.setVisibility(View.GONE);
                                discUnhappy.setVisibility(View.VISIBLE);
                                discHappy.setVisibility(View.GONE);
                                mainBackground.setColor(Color.argb(255, 255, 180, 72));
                            }
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
