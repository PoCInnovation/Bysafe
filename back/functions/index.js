const admin = require('firebase-admin');
admin.initializeApp({ credential: admin.credential.cert(require('./keys/admin.json')) });

const onRequest = require('firebase-functions').https.onRequest;
const functions = ['IdExists', 'AddReport', 'GetReportsFromManager', 'AddUsers', 'DeleteUser', 'GetAllUsers'];

functions.forEach((func) => {
    exports[func] = onRequest(require('./src/' + func)[func](admin));
});
