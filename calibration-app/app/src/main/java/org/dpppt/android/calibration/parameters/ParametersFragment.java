/*
 * Created by Ubique Innovation AG
 * https://www.ubique.ch
 * Copyright (c) 2020. All rights reserved.
 */
package org.dpppt.android.calibration.parameters;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.dpppt.android.calibration.MainApplication;
import org.dpppt.android.calibration.R;
import org.dpppt.android.calibration.util.DialogUtil;
import org.dpppt.android.calibration.util.RequirementsUtil;
import org.dpppt.android.sdk.DP3T;
import org.dpppt.android.sdk.TracingStatus;
import org.dpppt.android.sdk.internal.AppConfigManager;

public class ParametersFragment extends Fragment {

    ///CONTROL

    private static final String TAG = ParametersFragment.class.getCanonicalName();
    private static final int REQUEST_CODE_PERMISSION_LOCATION = 1;
    private static final int REQUEST_CODE_SAVE_DB = 2;
    private static final int REQUEST_CODE_REPORT_EXPOSED = 3;

    private static final DateFormat DATE_FORMAT_SYNC = SimpleDateFormat.getDateTimeInstance();

    private static final String REGEX_VALIDITY_AUTH_CODE = "\\w+";
    private static final int EXPOSED_MIN_DATE_DIFF = -21;

    private BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                checkPermissionRequirements();
                updateSdkStatus();
            }
        }
    };

    private BroadcastReceiver sdkReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateSdkStatus();
        }
    };

    //END_CONTROL

    private static final int MIN_INTERVAL_SCANNING_SECONDS = 30;
    private static final int MAX_INTERVAL_SCANNING_SECONDS = 900;
    private static final int MIN_DURATION_SCANNING_SECONDS = 10;
    private static final int MIN_RSSI_DETECTED_LEVEL = -127;
    private static final int MAX_RSSI_DETECTED_LEVEL = 126;
    private static final int DEFAULT_RSSI_DETECTED_LEVEL = -85;
    private SeekBar seekBarScanInterval;
    private SeekBar seekBarScanDuration;
    private SeekBar seekBarRSSIDetectedLevel;
    private EditText inputScanInterval;
    private EditText inputScanDuration;
    private EditText inputRSSIDetectedLevel;

    public static ParametersFragment newInstance() {
        return new ParametersFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_parameters, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        seekBarScanInterval = view.findViewById(R.id.parameter_seekbar_scan_interval);
        inputScanInterval = view.findViewById(R.id.parameter_input_scan_interval);
        seekBarScanDuration = view.findViewById(R.id.parameter_seekbar_scan_duration);
        inputScanDuration = view.findViewById(R.id.parameter_input_scan_duration);
        seekBarRSSIDetectedLevel = view.findViewById(R.id.parameter_seekbar_rssi_detected_level);
        inputRSSIDetectedLevel = view.findViewById(R.id.parameter_input_rssi_detected_level);

        seekBarRSSIDetectedLevel.setMax(-MIN_RSSI_DETECTED_LEVEL + MAX_RSSI_DETECTED_LEVEL);
        seekBarRSSIDetectedLevel.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                inputRSSIDetectedLevel.setText(String.valueOf(progress + MIN_RSSI_DETECTED_LEVEL));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                setRssiDetectedLevel(seekBar.getProgress() + MIN_RSSI_DETECTED_LEVEL);
            }
        });
        inputRSSIDetectedLevel.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                String input = inputRSSIDetectedLevel.getText().toString();
                if (input.length() == 0) {
                    return true;
                }
                try {
                    int inputRSSILevel = Integer.parseInt(input);
                    inputRSSILevel = Math.min(MAX_RSSI_DETECTED_LEVEL, Math.max(MIN_RSSI_DETECTED_LEVEL, inputRSSILevel));
                    inputRSSIDetectedLevel.setText(String.valueOf(inputRSSILevel));
                    seekBarRSSIDetectedLevel.setProgress(inputRSSILevel - MIN_RSSI_DETECTED_LEVEL);
                    hideKeyboard(v);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
                return true;
            }
            return false;
        });
        inputRSSIDetectedLevel.setText(String.valueOf(DEFAULT_RSSI_DETECTED_LEVEL));
        seekBarRSSIDetectedLevel.setProgress(DEFAULT_RSSI_DETECTED_LEVEL - MIN_RSSI_DETECTED_LEVEL);

        seekBarScanInterval.setMax(MAX_INTERVAL_SCANNING_SECONDS - MIN_INTERVAL_SCANNING_SECONDS);
        seekBarScanInterval.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int intervalDuration = progress + MIN_INTERVAL_SCANNING_SECONDS;
                inputScanInterval.setText(String.valueOf(intervalDuration));
                int newMaxProgress = intervalDuration - 1 - MIN_DURATION_SCANNING_SECONDS;
                adjustNewDurationMaximum(newMaxProgress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                setScanInterval(seekBar.getProgress() + MIN_INTERVAL_SCANNING_SECONDS);
            }
        });
        inputScanInterval.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                String input = inputScanInterval.getText().toString();
                if (input.length() == 0) return true;
                try {
                    int inputIntervalSeconds = Integer.parseInt(input);
                    inputIntervalSeconds =
                            Math.min(MAX_INTERVAL_SCANNING_SECONDS, Math.max(MIN_INTERVAL_SCANNING_SECONDS, inputIntervalSeconds));
                    inputScanInterval.setText(String.valueOf(inputIntervalSeconds));
                    seekBarScanInterval.setProgress(inputIntervalSeconds - MIN_INTERVAL_SCANNING_SECONDS);
                    setScanInterval(inputIntervalSeconds);
                    hideKeyboard(v);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
                return true;
            }
            return false;
        });

        seekBarScanDuration.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int scanDuration = progress + MIN_DURATION_SCANNING_SECONDS;
                inputScanDuration.setText(String.valueOf(scanDuration));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                setScanDuration(seekBar.getProgress() + MIN_DURATION_SCANNING_SECONDS);
            }
        });
        inputScanDuration.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                String input = inputScanDuration.getText().toString();
                if (input.length() == 0) return true;
                try {
                    int inputDurationSeconds = Integer.parseInt(input);
                    inputDurationSeconds =
                            Math.min(getScanInterval() - 1, Math.max(MIN_DURATION_SCANNING_SECONDS, inputDurationSeconds));
                    inputScanDuration.setText(String.valueOf(inputDurationSeconds));
                    seekBarScanDuration.setProgress(inputDurationSeconds - MIN_DURATION_SCANNING_SECONDS);
                    setScanDuration(inputDurationSeconds);
                    hideKeyboard(v);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
                return true;
            }
            return false;
        });

        //control
        setupUi(view);

        //end_control
    }

    private void adjustNewDurationMaximum(int durationProgressMaximum) {
        int currentDurationProgress = seekBarScanDuration.getProgress();
        seekBarScanDuration.setMax(durationProgressMaximum);
        if (currentDurationProgress > durationProgressMaximum) {
            setScanDuration(durationProgressMaximum + MIN_DURATION_SCANNING_SECONDS);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        AppConfigManager appConfigManager = AppConfigManager.getInstance(getContext());
        int interval = (int) (appConfigManager.getScanInterval() / 1000);
        seekBarScanInterval.setProgress(interval - MIN_INTERVAL_SCANNING_SECONDS);
        int duration = (int) (appConfigManager.getScanDuration() / 1000);
        seekBarScanDuration.setProgress(duration - MIN_DURATION_SCANNING_SECONDS);
        int RSSILevel = (int) (appConfigManager.getRSSIDetectedLevel());
        seekBarRSSIDetectedLevel.setProgress(RSSILevel - MIN_RSSI_DETECTED_LEVEL);

        //Control
        getContext().registerReceiver(bluetoothReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        getContext().registerReceiver(sdkReceiver, DP3T.getUpdateIntentFilter());
        checkPermissionRequirements();
        updateSdkStatus();

        //end Control
    }

    @Override
    public void onPause() {
        super.onPause();
        getContext().unregisterReceiver(bluetoothReceiver);
        getContext().unregisterReceiver(sdkReceiver);
    }

    private int getScanInterval() {
        return seekBarScanInterval.getProgress() + MIN_INTERVAL_SCANNING_SECONDS;
    }

    private void setScanInterval(int interval) {
        AppConfigManager.getInstance(getContext()).setScanInterval(interval * 1000);
    }

    private void setScanDuration(int duration) {
        AppConfigManager.getInstance(getContext()).setScanDuration(duration * 1000);
    }

    private void setRssiDetectedLevel(int rssiDetectedLevel) {
        AppConfigManager.getInstance(getContext()).setRSSIDetectedLevel(rssiDetectedLevel);
    }

    private void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    // Control
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    }

    private void setupUi(View view) {
        Button locationButton = view.findViewById(R.id.home_button_location);
        locationButton.setOnClickListener(
                v -> requestPermissions(new String[] { Manifest.permission.ACCESS_FINE_LOCATION },
                        REQUEST_CODE_PERMISSION_LOCATION));

        Button batteryButton = view.findViewById(R.id.home_button_battery_optimization);
        batteryButton.setOnClickListener(
                v -> startActivity(new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                        Uri.parse("package:" + getContext().getPackageName()))));

        Button bluetoothButton = view.findViewById(R.id.home_button_bluetooth);
        bluetoothButton.setOnClickListener(v -> {
            if (BluetoothAdapter.getDefaultAdapter() != null) {
                BluetoothAdapter.getDefaultAdapter().enable();
            } else {
                Toast.makeText(getContext(), "No BluetoothAdapter found!", Toast.LENGTH_LONG).show();
            }
        });

        Button buttonClearData = view.findViewById(R.id.home_button_clear_data);
        buttonClearData.setOnClickListener(v -> {
            DialogUtil.showConfirmDialog(v.getContext(), R.string.dialog_clear_data_title,
                    (dialog, which) -> {
                        DP3T.clearData(v.getContext(), () ->
                                new Handler(getContext().getMainLooper()).post(this::updateSdkStatus));
                        MainApplication.initDP3T(v.getContext());
                    });
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSION_LOCATION) {
            checkPermissionRequirements();
            updateSdkStatus();
        }
    }

    private void checkPermissionRequirements() {
        View view = getView();
        Context context = getContext();
        if (view == null || context == null) return;

        boolean locationGranted = RequirementsUtil.isLocationPermissionGranted(context);
        Button locationButton = view.findViewById(R.id.home_button_location);
        locationButton.setEnabled(!locationGranted);
        locationButton.setText(locationGranted ? R.string.req_location_permission_granted
                : R.string.req_location_permission_ungranted);

        boolean batteryOptDeactivated = RequirementsUtil.isBatteryOptimizationDeactivated(context);
        Button batteryButton = view.findViewById(R.id.home_button_battery_optimization);
        batteryButton.setEnabled(!batteryOptDeactivated);
        batteryButton.setText(batteryOptDeactivated ? R.string.req_battery_deactivated
                : R.string.req_battery_deactivated);

        boolean bluetoothActivated = RequirementsUtil.isBluetoothEnabled();
        Button bluetoothButton = view.findViewById(R.id.home_button_bluetooth);
        bluetoothButton.setEnabled(!bluetoothActivated);
        bluetoothButton.setText(bluetoothActivated ? R.string.req_bluetooth_active
                : R.string.req_bluetooth_inactive);
    }

    private void updateSdkStatus() {
        View view = getView();
        Context context = getContext();
        if (context == null || view == null) return;

        TracingStatus status = DP3T.getStatus(context);

        Button buttonStartStopTracking = view.findViewById(R.id.home_button_start_stop_tracking);
        boolean isRunning = status.isAdvertising() || status.isReceiving();
        buttonStartStopTracking.setSelected(isRunning);
        buttonStartStopTracking.setText(getString(isRunning ? R.string.button_tracking_stop
                : R.string.button_tracking_start));
        buttonStartStopTracking.setOnClickListener(v -> {
            if (isRunning) {
                DP3T.stop(v.getContext());
            } else {
                DP3T.start(v.getContext());
            }
            updateSdkStatus();
        });

        Button buttonClearData = view.findViewById(R.id.home_button_clear_data);
        buttonClearData.setEnabled(!isRunning);
    }

    // end control

}