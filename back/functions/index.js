const functions = require('firebase-functions');
const admin = require('firebase-admin');

admin.initializeApp({ credential: admin.credential.cert(require('./keys/admin.json')) });

const db = admin.firestore();

exports.IdExists = functions.https.onRequest((request, response) => {
    const id = request.params['0'].slice(1);

    if (id.length == 0) {
        response.status(404).json({ error: 'Un indentifiant est requis' });
    } else if (isNaN(id)) {
        response.status(404).json({ error: 'Indentifiant invalide' });
    } else {
        db.collection('users')
            .doc(id)
            .get()
            .then((doc) => {
                if (doc.exists) {
                    response.json(); // 200
                } else {
                    response.status(404).json({ error: `L'utilisateur ${id} n'existe pas` });
                }
            });
    }
});

exports.AddReport = functions.https.onRequest((request, response) => {
    const data = request.body;

    for (const id of Object.keys(data))
        db.collection('users').doc(id).set({ reports: data[id] }, { merge: true });

    response.json();
});

exports.GetReport = functions.https.onRequest((request, response) => {
    const id = request.params['0'].slice(1);

    if (id.length == 0) {
        response.status(404).json({ error: 'Un indentifiant est requis' });
    } else if (isNaN(id)) {
        response.status(404).json({ error: 'Indentifiant invalide' });
    } else {
        db.collection('users')
            .doc(id)
            .get()
            .then((doc) => response.json(doc.data().reports));
    }
});

exports.GetReportsFromManager = functions.https.onRequest((request, response) => {
    const id = request.params['0'].slice(1);

    if (id.length == 0) {
        response.status(404).json({ error: 'Un indentifiant est requis' });
    } else if (isNaN(id)) {
        response.status(404).json({ error: 'Indentifiant invalide' });
    } else {
        db.collection('users')
            .get()
            .then(({ docs }) => {
                let res = {};

                docs.forEach((doc) => {
                    if (doc.data().manager == id) {
                        res[doc.id] = doc.data().reports;
                    }
                });

                response.json(res);
            });
    }
});

// ref: https://stackoverflow.com/a/1293163
const CSVToArray = (strData, strDelimiter) => {
    strDelimiter = strDelimiter || ',';
    var objPattern = new RegExp(
        '(\\' +
            strDelimiter +
            '|\\r?\\n|\\r|^)' +
            '(?:"([^"]*(?:""[^"]*)*)"|' +
            '([^"\\' +
            strDelimiter +
            '\\r\\n]*))',
        'gi'
    );

    var arrData = [[]];
    var arrMatches = null;

    while ((arrMatches = objPattern.exec(strData))) {
        var strMatchedDelimiter = arrMatches[1];
        if (strMatchedDelimiter.length && strMatchedDelimiter !== strDelimiter) arrData.push([]);

        var strMatchedValue;
        if (arrMatches[2]) strMatchedValue = arrMatches[2].replace(new RegExp('""', 'g'), '"');
        else strMatchedValue = arrMatches[3];
        arrData[arrData.length - 1].push(strMatchedValue);
    }
    return arrData;
};

exports.addUsers = functions.https.onRequest((request, response) => {
    admin
        .auth()
        .verifyIdToken(request.query.token)
        .then(({ uid }) => {
            if (uid !== '8CScTXV2ITg9HeLpqGDGYwxQkD93') {
                response.status(403).send("Vous n'êtes pas l'administrateur");
                return;
            }

            const multipart = require('parse-multipart');

            const boundary = multipart.getBoundary(request.headers['content-type']);
            const parts = multipart.Parse(request.body, boundary);

            var allManagers = [];
            let allUsers = {};

            for (let i = 0; i < parts.length; i++) {
                const { data, filename } = parts[i];
                const lines = CSVToArray(String(data)).filter((line) => line[0].length !== 0);
                const managerLine = lines[0];

                if (managerLine.length !== 2) {
                    response
                        .status(406)
                        .send(
                            `Erreur dans le fichier ${filename}: La première ligne doit contenir le numéro de chantier et le mot de passe du manager. Aucun changement n'a été effectué.`
                        );
                    return;
                }

                allManagers.push({
                    email: managerLine[0] + '@bysafe.app',
                    password: managerLine[1],
                });

                lines.slice(1).forEach((user) => {
                    allUsers[user[0]] = {
                        manager: managerLine[0],
                        firstname: user[1],
                        lastname: user[2],
                    };
                });
            }

            allManagers.forEach((manager) =>
                admin
                    .auth()
                    .createUser(manager)
                    .catch(() => {})
            );

            for (const id of Object.keys(allUsers))
                db.collection('users').doc(id).set(allUsers[id]);

            response.send(`Opération réalisée avec succès`);
        })
        .catch((e) => {
            console.error(e);
            response.status(401).send('Vous devez être connecté');
        });
});
