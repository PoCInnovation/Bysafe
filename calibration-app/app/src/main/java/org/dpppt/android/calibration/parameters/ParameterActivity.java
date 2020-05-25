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
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.dpppt.android.calibration.MainApplication;
import org.dpppt.android.calibration.R;
import org.dpppt.android.calibration.util.DialogUtil;
import org.dpppt.android.calibration.util.RequirementsUtil;
import org.dpppt.android.sdk.DP3T;
import org.dpppt.android.sdk.TracingStatus;
import org.dpppt.android.sdk.internal.AppConfigManager;

public class ParameterActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_PERMISSION_LOCATION = 1;
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

    private static final int MIN_INTERVAL_SCANNING_SECONDS = 30;
    private static final int MAX_INTERVAL_SCANNING_SECONDS = 900;
    private static final int MIN_DURATION_SCANNING_SECONDS = 10;
    private static final int MIN_RSSI_DETECTED_LEVEL = -127;
    private static final int MAX_RSSI_DETECTED_LEVEL = 126;
    private static final int DEFAULT_RSSI_DETECTED_LEVEL = -100;
    private SeekBar seekBarScanInterval;
    private SeekBar seekBarScanDuration;
    private SeekBar seekBarRSSIDetectedLevel;
    private EditText inputScanInterval;
    private EditText inputScanDuration;
    private EditText inputRSSIDetectedLevel;

    private static Context mContext;

    public static Context getContext() {
        return mContext;
    }

    public void setContext(Context mContext) {
        ParameterActivity.mContext = mContext;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.fragment_parameters);
        this.setContext(this);

        final Button button = findViewById(R.id.settings_go_back);
        button.setOnClickListener(v -> finish());

        seekBarScanInterval = findViewById(R.id.parameter_seekbar_scan_interval);
        inputScanInterval = findViewById(R.id.parameter_input_scan_interval);
        seekBarScanDuration = findViewById(R.id.parameter_seekbar_scan_duration);
        inputScanDuration = findViewById(R.id.parameter_input_scan_duration);
        seekBarRSSIDetectedLevel = findViewById(R.id.parameter_seekbar_rssi_detected_level);
        inputRSSIDetectedLevel = findViewById(R.id.parameter_input_rssi_detected_level);

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

        setupUi();
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

    private void setupUi() {
        Button locationButton = findViewById(R.id.home_button_location);
        locationButton.setOnClickListener(
                v -> requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_CODE_PERMISSION_LOCATION));

        Button batteryButton = findViewById(R.id.home_button_battery_optimization);
        batteryButton.setOnClickListener(
                v -> startActivity(new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                        Uri.parse("package:" + getContext().getPackageName()))));

        Button bluetoothButton = findViewById(R.id.home_button_bluetooth);
        bluetoothButton.setOnClickListener(v -> {
            if (BluetoothAdapter.getDefaultAdapter() != null) {
                BluetoothAdapter.getDefaultAdapter().enable();
            } else {
                Toast.makeText(getContext(), "No BluetoothAdapter found!", Toast.LENGTH_LONG).show();
            }
        });

        Button buttonClearData = findViewById(R.id.home_button_clear_data);
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
        Context context = getContext();

        boolean locationGranted = RequirementsUtil.isLocationPermissionGranted(context);
        Button locationButton = findViewById(R.id.home_button_location);
        locationButton.setEnabled(!locationGranted);
        locationButton.setText(locationGranted ? R.string.req_location_permission_granted
                : R.string.req_location_permission_ungranted);

        boolean batteryOptDeactivated = RequirementsUtil.isBatteryOptimizationDeactivated(context);
        Button batteryButton = findViewById(R.id.home_button_battery_optimization);
        batteryButton.setEnabled(!batteryOptDeactivated);
        batteryButton.setText(batteryOptDeactivated ? R.string.req_battery_deactivated
                : R.string.req_battery_deactivated);

        boolean bluetoothActivated = RequirementsUtil.isBluetoothEnabled();
        Button bluetoothButton = findViewById(R.id.home_button_bluetooth);
        bluetoothButton.setEnabled(!bluetoothActivated);
        bluetoothButton.setText(bluetoothActivated ? R.string.req_bluetooth_active
                : R.string.req_bluetooth_inactive);
    }

    private void updateSdkStatus() {
        Context context = getContext();

        TracingStatus status = DP3T.getStatus(context);

        Button buttonStartStopTracking = findViewById(R.id.home_button_start_stop_tracking);
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

        Button buttonClearData = findViewById(R.id.home_button_clear_data);
        buttonClearData.setEnabled(!isRunning);
    }
}


