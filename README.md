# Bysafe App

## Description

Social distancing app in Java made by PoC students for a Spot Bouygues Project.
Based on DP3T calibration-app implementation.

## Compilation step

This application requires a firebase instance. You just have to past the google-services.json in calibration-app/app.
Open ```bysafe/calibration-app/app/``` in Android studio, and press Build button

## Installation steps

Open ```bysafe/calibration-app/app/``` in Android studio, plug your device to your computer, 
activate USB debbuging on your device, and press Run

## Frontend

To deploy your own Admin Console, read [these instructions](admin_panel/README.MD).

## Backend

To create your own Firebase project, read [these instructions](back/README.MD).

The app requires a backend hosted on firebase.
This backend is used for user connection, contact storage and manager reports.
Links to backend are used in : 
- [Authentification](calibration-app/app/src/main/java/com/bouygues/bysafe/auth/AuthActivity.java) -> line 193
- [Team activites report](calibration-app/app/src/main/java/com/bouygues/bysafe/report/TeamActivitiesReportFragment.java) -> line 154
- [Team activites](calibration-app/app/src/main/java/com/bouygues/bysafe/report/TeamActivitiesFragment.java) -> line 111
- [Report posting](calibration-app/app/src/main/java/com/bouygues/bysafe/MainActivity.java) -> line 302

## Settings and Back interactions

[Modification of the RSSI level detected](dp3t-sdk/sdk/src/main/java/org/dpppt/android/sdk/internal/AppConfigManager.java) -> line 61

[Modification of scan duration and interval](dp3t-sdk/sdk/src/main/java/org/dpppt/android/sdk/internal/AppConfigManager.java) -> line 59-60

[Handwash timer duration](calibration-app/app/src/main/java/com/bouygues/bysafe/handwash/HandwashFragment.java) -> line 41

[Anonyme mode](calibration-app/app/src/main/java/com/bouygues/bysafe/auth/AuthActivity.java) -> line 57

## License
This project is licensed under the terms of the MPL 2 license. See the LICENSE file.

## Maintainer
[Killian Clette](https://github.com/Skerilyo)

[Lorenzo Rosmarino](https://github.com/Drysque)

[Robin Christol](https://github.com/ltsrc)
