/*
 * Copyright (c) 2020 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package org.dpppt.android.sdk.internal;

import android.content.Context;
import android.content.SharedPreferences;
import android.service.autofill.Dataset;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.dpppt.android.sdk.backend.ResponseCallback;
import org.dpppt.android.sdk.backend.models.ApplicationInfo;
import org.dpppt.android.sdk.internal.backend.BackendReportRepository;
import org.dpppt.android.sdk.internal.backend.DiscoveryRepository;
import org.dpppt.android.sdk.internal.backend.models.ApplicationsList;
import org.dpppt.android.sdk.internal.util.Json;
import org.dpppt.android.sdk.internal.util.Pair;
import org.json.JSONObject;

public class AppConfigManager {

	private static AppConfigManager instance;

	public static synchronized AppConfigManager getInstance(Context context) {
		if (instance == null) {
			instance = new AppConfigManager(context);
		}
		return instance;
	}

	public static final int CALIBRATION_TEST_DEVICE_NAME_LENGTH = 4;

	static final long DEFAULT_SCAN_INTERVAL = 30 * 1000L;
	static final long DEFAULT_SCAN_DURATION = 29 * 1000L;
	private static final long DEFAULT_RSSI_DETECTED_LEVEL = -85;
	private static final BluetoothScanMode DEFAULT_BLUETOOTH_SCAN_MODE = BluetoothScanMode.SCAN_MODE_LOW_POWER;
	private static final BluetoothTxPowerLevel DEFAULT_BLUETOOTH_POWER_LEVEL = BluetoothTxPowerLevel.ADVERTISE_TX_POWER_ULTRA_LOW;
	private static final BluetoothAdvertiseMode DEFAULT_BLUETOOTH_ADVERTISE_MODE = BluetoothAdvertiseMode.ADVERTISE_MODE_BALANCED;
	private static final boolean DEFAULT_BLUETOOTH_USE_SCAN_RESPONSE_ENABLED = true;

	private static final int DEFAULT_NUMBER_OF_WINDOWS_FOR_EXPOSURE = 3;
	private static final int DEFAULT_CONTACT_NUMBER = 0;
	private static final long DEFAULT_VIBRATION_TIMER = 0;
	private static final long DEFAULT_JOURNEY_START = 0;
	private static final String DEFAULT_JOURNEY_CONTACT =  new Gson().toJson(new ArrayList<Pair<Long, Integer>>());
	private static final float DEFAULT_CONTACT_ATTENUATION_THRESHOLD = 73.0f;

	private static final String PREFS_NAME = "dp3t_sdk_preferences";
	private static final String PREF_APPLICATION_LIST = "applicationList";
	private static final String PREF_ADVERTISING_ENABLED = "advertisingEnabled";
	private static final String PREF_RECEIVING_ENABLED = "receivingEnabled";
	private static final String PREF_LAST_LOADED_BATCH_RELEASE_TIME = "lastLoadedBatchReleaseTime";
	private static final String PREF_LAST_SYNC_DATE = "lastSyncDate";
	private static final String PREF_LAST_SYNC_NET_SUCCESS = "lastSyncNetSuccess";
	private static final String PREF_I_AM_INFECTED = "IAmInfected";
	private static final String PREF_CALIBRATION_TEST_DEVICE_NAME = "calibrationTestDeviceName";
	private static final String PREF_SCAN_INTERVAL = "scanInterval";
	private static final String PREF_SCAN_DURATION = "scanDuration";
	private static final String PREF_RSSI_DETECTED_LEVEL = "rssiDetectedLevel";
	private static final String PREF_BLUETOOTH_SCAN_MODE = "scanMode";
	private static final String PREF_ADVERTISEMENT_POWER_LEVEL = "advertisementPowerLevel";
	private static final String PREF_ADVERTISEMENT_MODE = "advertisementMode";
	private static final String PREF_BLUETOOTH_USE_SCAN_RESPONSE = "scanResponseEnabled";
	private static final String PREF_CONTACT_ATTENUATION_THRESHOLD = "contact_attenuation_threshold";
	private static final String PREF_NUMBER_OF_WINDOWS_FOR_EXPOSURE = "number_of_windows_for_exposure";
	private static final String PREF_CONTACT_NUMBER = "number_of_contact";
	private static final String PREF_VIBRATION_TIMER = "timer_for_vibration";
	private static final String PREF_JOURNEY_START = "journey_start";
	private static final String PREF_JOURNEY_CONTACT = "journey_contact";
	private static final String PREF_IS_LOGGED = "is_logged";

	private String appId;
	private boolean useDiscovery = false;
	private boolean isDevDiscoveryMode = false;
	private SharedPreferences sharedPrefs;
	private DiscoveryRepository discoveryRepository;

	private AppConfigManager(Context context) {
		discoveryRepository = new DiscoveryRepository(context);
		sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public void triggerLoad() {
		useDiscovery = true;
		discoveryRepository.getDiscovery(new ResponseCallback<ApplicationsList>() {
			@Override
			public void onSuccess(ApplicationsList response) {
				sharedPrefs.edit().clear().putString(PREF_APPLICATION_LIST, Json.toJson(response)).commit();
			}

			@Override
			public void onError(Throwable throwable) {
				throwable.printStackTrace();
			}
		}, isDevDiscoveryMode);
	}

	public void setManualApplicationInfo(ApplicationInfo applicationInfo) {
		useDiscovery = false;
		setAppId(applicationInfo.getAppId());
		ApplicationsList applicationsList = new ApplicationsList();
		applicationsList.getApplications().add(applicationInfo);
		sharedPrefs.edit().clear().putString(PREF_APPLICATION_LIST, Json.toJson(applicationsList)).commit();
	}

	public void updateFromDiscoverySynchronous() throws IOException {
		if (useDiscovery) {
			ApplicationsList response = discoveryRepository.getDiscoverySync(isDevDiscoveryMode);
			sharedPrefs.edit().clear().putString(PREF_APPLICATION_LIST, Json.toJson(response)).commit();
		}
	}

	public ApplicationsList getLoadedApplicationsList() {
		return Json.safeFromJson(sharedPrefs.getString(PREF_APPLICATION_LIST, "{}"), ApplicationsList.class,
				ApplicationsList::new);
	}

	public ApplicationInfo getAppConfig() throws IllegalStateException {
		for (ApplicationInfo application : getLoadedApplicationsList().getApplications()) {
			if (application.getAppId().equals(appId)) {
				return application;
			}
		}
		throw new IllegalStateException("The provided appId is not found by the discovery service!");
	}

	public void setAdvertisingEnabled(boolean enabled) {
		sharedPrefs.edit().clear().putBoolean(PREF_ADVERTISING_ENABLED, enabled).commit();
	}

	public boolean isAdvertisingEnabled() {
		return sharedPrefs.getBoolean(PREF_ADVERTISING_ENABLED, false);
	}

	public void setReceivingEnabled(boolean enabled) {
		sharedPrefs.edit().clear().putBoolean(PREF_RECEIVING_ENABLED, enabled).commit();
	}

	public boolean isReceivingEnabled() {
		return sharedPrefs.getBoolean(PREF_RECEIVING_ENABLED, false);
	}

	public void setLastLoadedBatchReleaseTime(long lastLoadedBatchReleaseTime) {
		sharedPrefs.edit().clear().putLong(PREF_LAST_LOADED_BATCH_RELEASE_TIME, lastLoadedBatchReleaseTime).commit();
	}

	public long getLastLoadedBatchReleaseTime() {
		return sharedPrefs.getLong(PREF_LAST_LOADED_BATCH_RELEASE_TIME, -1);
	}

	public void setLastSyncDate(long lastSyncDate) {
		sharedPrefs.edit().clear().putLong(PREF_LAST_SYNC_DATE, lastSyncDate).commit();
	}

	public long getLastSyncDate() {
		return sharedPrefs.getLong(PREF_LAST_SYNC_DATE, 0);
	}

	public void setLastSyncNetworkSuccess(boolean success) {
		sharedPrefs.edit().clear().putBoolean(PREF_LAST_SYNC_NET_SUCCESS, success).commit();
	}

	public boolean getLastSyncNetworkSuccess() {
		return sharedPrefs.getBoolean(PREF_LAST_SYNC_NET_SUCCESS, true);
	}

	public boolean getIAmInfected() {
		return sharedPrefs.getBoolean(PREF_I_AM_INFECTED, false);
	}

	public void setIAmInfected(boolean exposed) {
		sharedPrefs.edit().clear().putBoolean(PREF_I_AM_INFECTED, exposed).commit();
	}

	public BackendReportRepository getBackendReportRepository(Context context) throws IllegalStateException {
		ApplicationInfo appConfig = getAppConfig();
		return new BackendReportRepository(context, appConfig.getReportBaseUrl());
	}

	public void setDevDiscoveryModeEnabled(boolean enable) {
		isDevDiscoveryMode = enable;
	}

	public void setCalibrationTestDeviceName(String name) {
		if (name != null && name.length() != CALIBRATION_TEST_DEVICE_NAME_LENGTH) {
			throw new IllegalArgumentException(
					"CalibrationTestDevice Name must have length " + CALIBRATION_TEST_DEVICE_NAME_LENGTH + ", provided string '" +
							name + "' with length " + name.length());
		}
		sharedPrefs.edit().clear().putString(PREF_CALIBRATION_TEST_DEVICE_NAME, name).commit();
	}

	public String getCalibrationTestDeviceName() {
		return sharedPrefs.getString(PREF_CALIBRATION_TEST_DEVICE_NAME, null);
	}

	public void setScanDuration(long scanDuration) {
		sharedPrefs.edit().clear().putLong(PREF_SCAN_DURATION, scanDuration).commit();
	}

	public long getScanDuration() {
		return sharedPrefs.getLong(PREF_SCAN_DURATION, DEFAULT_SCAN_DURATION);
	}

	public void setRSSIDetectedLevel(long rssiDetectedLevel) {
		sharedPrefs.edit().clear().putLong(PREF_RSSI_DETECTED_LEVEL, rssiDetectedLevel).commit();
	}

	public long getRSSIDetectedLevel() {
		return sharedPrefs.getLong(PREF_RSSI_DETECTED_LEVEL, DEFAULT_RSSI_DETECTED_LEVEL);
	}

	public void setScanInterval(long scanInterval) {
		sharedPrefs.edit().clear().putLong(PREF_SCAN_INTERVAL, scanInterval).commit();
	}

	public long getScanInterval() {
		return sharedPrefs.getLong(PREF_SCAN_INTERVAL, DEFAULT_SCAN_INTERVAL);
	}

	public void setBluetoothPowerLevel(BluetoothTxPowerLevel powerLevel) {
		sharedPrefs.edit().clear().putInt(PREF_ADVERTISEMENT_POWER_LEVEL, powerLevel.ordinal()).commit();
	}

	public BluetoothTxPowerLevel getBluetoothTxPowerLevel() {
		return BluetoothTxPowerLevel.values()[sharedPrefs
				.getInt(PREF_ADVERTISEMENT_POWER_LEVEL, DEFAULT_BLUETOOTH_POWER_LEVEL.ordinal())];
	}

	public BluetoothScanMode getBluetoothScanMode() {
		return BluetoothScanMode.values()[sharedPrefs.getInt(PREF_BLUETOOTH_SCAN_MODE, DEFAULT_BLUETOOTH_SCAN_MODE.ordinal())];
	}

	public void setBluetoothScanMode(BluetoothScanMode scanMode) {
		sharedPrefs.edit().clear().putInt(PREF_BLUETOOTH_SCAN_MODE, scanMode.ordinal()).commit();
	}

	public void setBluetoothAdvertiseMode(BluetoothAdvertiseMode advertiseMode) {
		sharedPrefs.edit().clear().putInt(PREF_ADVERTISEMENT_MODE, advertiseMode.ordinal()).commit();
	}

	public BluetoothAdvertiseMode getBluetoothAdvertiseMode() {
		return BluetoothAdvertiseMode.values()[sharedPrefs
				.getInt(PREF_ADVERTISEMENT_MODE, DEFAULT_BLUETOOTH_ADVERTISE_MODE.ordinal())];
	}

	public boolean isScanResponseEnabled() {
		return sharedPrefs.getBoolean(PREF_BLUETOOTH_USE_SCAN_RESPONSE, DEFAULT_BLUETOOTH_USE_SCAN_RESPONSE_ENABLED);
	}

	public void setUseScanResponse(boolean useScanResponse) {
		sharedPrefs.edit().clear().putBoolean(PREF_BLUETOOTH_USE_SCAN_RESPONSE, useScanResponse).commit();
	}

	public float getContactAttenuationThreshold() {
		return sharedPrefs.getFloat(PREF_CONTACT_ATTENUATION_THRESHOLD, DEFAULT_CONTACT_ATTENUATION_THRESHOLD);
	}

	public void setContactAttenuationThreshold(float threshold) {
		sharedPrefs.edit().clear().putFloat(PREF_CONTACT_ATTENUATION_THRESHOLD, threshold).commit();
	}

	public int getNumberOfWindowsForExposure() {
		return sharedPrefs.getInt(PREF_NUMBER_OF_WINDOWS_FOR_EXPOSURE, DEFAULT_NUMBER_OF_WINDOWS_FOR_EXPOSURE);
	}

	public void setNumberOfWindowsForExposure(int threshold) {
		sharedPrefs.edit().clear().putInt(PREF_NUMBER_OF_WINDOWS_FOR_EXPOSURE, threshold).commit();
	}

	public int getContactNumber() {
		return sharedPrefs.getInt(PREF_CONTACT_NUMBER, DEFAULT_CONTACT_NUMBER);
	}

	public void setContactNumber(int threshold) {
		sharedPrefs.edit().clear().putInt(PREF_CONTACT_NUMBER, threshold).commit();
	}

	public void setVibrationTimer(long vibrationTimer) {
		sharedPrefs.edit().clear().putLong(PREF_VIBRATION_TIMER, vibrationTimer).commit();
	}

	public long getVibrationTimer() {
		return sharedPrefs.getLong(PREF_VIBRATION_TIMER, DEFAULT_VIBRATION_TIMER);
	}

	public void setJourneyStart(long journeyStart) {
		sharedPrefs.edit().clear().putLong(PREF_JOURNEY_START, journeyStart).commit();
	}

	public long getJourneyStart() {
		return sharedPrefs.getLong(PREF_JOURNEY_START, DEFAULT_JOURNEY_START);
	}

	public void addJourneyContact(Pair<Long, Integer> pair) throws IOException {
		Gson gson = new Gson();

		Type datasetListType = new TypeToken<ArrayList<Pair<Long, Integer>>>() {}.getType();
		ArrayList<Pair<Long, Integer>> list = gson.fromJson(sharedPrefs.getString(PREF_JOURNEY_CONTACT, DEFAULT_JOURNEY_CONTACT), datasetListType);
		list.add(pair);
		sharedPrefs.edit().clear().putString(PREF_JOURNEY_CONTACT, gson.toJson(list)).commit();
	}

	public void setJourneyContact(ArrayList<Pair<Long, Integer>> list) throws IOException {
		Gson gson = new Gson();
		sharedPrefs.edit().clear().putString(PREF_JOURNEY_CONTACT, gson.toJson(list)).commit();
	}

	public ArrayList<Pair<Long, Integer>> getJourneyContact() throws IOException {
		Gson gson = new Gson();
		Type datasetListType = new TypeToken<ArrayList<Pair<Long, Integer>>>() {}.getType();
		return gson.fromJson(sharedPrefs.getString(PREF_JOURNEY_CONTACT, DEFAULT_JOURNEY_CONTACT), datasetListType);
	}

	public void setIsLogged(boolean isLogged) {
		sharedPrefs.edit().putBoolean(PREF_IS_LOGGED, isLogged).apply();
	}

	public boolean getIsLogged() {
		return sharedPrefs.getBoolean(PREF_IS_LOGGED, false);
	}


	public void clearPreferences() {
		sharedPrefs.edit().clear().clear().commit();
	}
}