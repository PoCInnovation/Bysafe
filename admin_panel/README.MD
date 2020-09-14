# Firebase Hosting - Admin Panel

## 1. Create a Firebase Project

Follow instuctions in [back/README.MD](../back/README.MD)

## 2. Initialize the local project

Install dependencies:
```bash
cd admin_panel/functions && yarn
```

Download the project config file:
`Firebase console -> your bysafe project -> settings -> General settings -> Download latest config file`

The, copy the file to: `admin_panel/public/firebase-config.json`

In [admin_panel/public/firebase-app.js](./public/firebase-app.js), change the `backEndpoint` variable to your Cloud Function backend endpoint.

## 3. Deploy
Follow the CLI instructions

```bash
yarn firebase deploy
```
