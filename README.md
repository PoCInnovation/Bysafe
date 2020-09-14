# Bysafe App

## Description

Bysafe is a social distancing app made by PoC students in partnership with Spot Bouygues.  
The app is based on DP3T calibration-app implementation and written in Java for Android.  

## App Compilation

This application requires a firebase instance. You just have to paste the `google-services.json` in calibration-app/app.
Open `bysafe/calibration-app/app/` in Android studio, and press the `Build` button. ???

## Installation steps

Open `bysafe/calibration-app/app/` in Android studio, plug your device to your computer, activate USB debbuging on your device, and press `Run` ???

## Backend

The backend is in node and uses firebase and google cloud functions. It is used for user connection, contact storage and manager reports.  
To create your own Firebase project, read [these instructions](back/README.MD).  

## Frontend - Admin Panel

The admin panel is in React and uses firebase and google cloud functions to manage data ???  
To deploy your own Admin Console, read [these instructions](admin_panel/README.MD).

## Files and lines where the backend is called : 
- [Authentification](https://github.com/PoCFrance/Bysafe/blame/master/calibration-app/app/src/main/java/com/bouygues/bysafe/auth/AuthActivity.java#L193)
- [Team activites report](https://github.com/PoCFrance/Bysafe/blame/master/calibration-app/app/src/main/java/com/bouygues/bysafe/report/TeamActivitiesReportFragment.java#L154)
- [Team activites](https://github.com/PoCFrance/Bysafe/blame/master/calibration-app/app/src/main/java/com/bouygues/bysafe/report/TeamActivitiesFragment.java#L111) 
- [Report posting](https://github.com/PoCFrance/Bysafe/blame/master/calibration-app/app/src/main/java/com/bouygues/bysafe/MainActivity.java#L302)

## Settings and backend variables

- [Modification of the RSSI level detected](https://github.com/PoCFrance/Bysafe/blame/master/dp3t-sdk/sdk/src/main/java/org/dpppt/android/sdk/internal/AppConfigManager.java#L61)
- [Modification of scan duration and interval](https://github.com/PoCFrance/Bysafe/blame/master/dp3t-sdk/sdk/src/main/java/org/dpppt/android/sdk/internal/AppConfigManager.java#L59)
- [Handwash timer duration](https://github.com/PoCFrance/Bysafe/blame/master/calibration-app/app/src/main/java/com/bouygues/bysafe/handwash/HandwashFragment.java#L41)
- [Anonyme mode](https://github.com/PoCFrance/Bysafe/blame/master/calibration-app/app/src/main/java/com/bouygues/bysafe/auth/AuthActivity.java#L57)

## Authors
- [Killian Clette](https://github.com/Skerilyo)
- [Lorenzo Rosmarino](https://github.com/Drysque)
- [Robin Christol](https://github.com/ltsrc)
