# Firebase Hosting - Admin Panel

## Create a Firebase Project
- Follow instuctions in back/README.MD

## Initialize the local project
- Install dependencies<br>
    ```cd admin_panel/functions && yarn```
- Add project config file to local project
    `Firebase console -> your bysafe project -> settings -> General settings -> Download latest config file`; Copy the file to `admin_panel/public/firebase-config.json`
- In `admin_panel/public/firebase-app.js`, change the `backEndpoint` variable to your Cloud Function backend endpoint.

## Deploy
Follow the CLI instructions
```yarn firebase deploy```
