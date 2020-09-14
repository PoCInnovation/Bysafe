# Firebase Cloud Function - Backend

## 1. Create a Firebase Project
- https://firebase.google.com/

### Auth
- Add a user with email `admin@bysafe.app`
- Disallow the creation of email&password accounts (they will be created from the admin SDK)

### Database
- Create a `users` collection

## 2. Initialize the local project

Install dependencies
```bash
cd back/functions && yarn
```

Download the project config file:
`Firebase console -> your bysafe project -> settings -> Service Account -> Generate new private key`

Then, copy the file to `back/functions/keys/admin.json`. Keep it hidden

In [back/functions/src/](./functions/src/) files, change the admin ID (currently `PciCitpbScemcOEnmUsbeYJCeSj1`) to your admin id.

## 3. Deploy
```bash
yarn firebase deploy
```

Follow the CLI instructions  
You may need to auth your CLI with firebase (https://firebase.google.com/docs/cli)
