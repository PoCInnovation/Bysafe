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
                    let _data = doc.data();
                    if (_data.manager == id) {
                        res[`${_data.firstname} ${_data.lastname}`] = _data.reports;
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
        .verifyIdToken(request.query.token || '')
        .then(({ uid }) => {
            if (uid !== 'PciCitpbScemcOEnmUsbeYJCeSj1') {
                response.status(403).send("Vous n'êtes pas l'administrateur");
                return;
            }

            const multipart = require('parse-multipart');

            const boundary = multipart.getBoundary(request.headers['content-type']);
            const parts = multipart.Parse(request.body, boundary);

            var allManagers = [];
            let allUsers = {};

            for (const { data, filename } of parts) {
                const lines = CSVToArray(String(data)).filter((line) => line[0].length !== 0);

                for (const line of lines) {
                    if (line.length < 4) {
                        response
                            .status(406)
                            .send(
                                `Erreur dans le fichier ${filename}: Chaque ligne doit contenir au moins 4 champs.`
                            );
                        return;
                    }

                    if (line[4] && line[4].length != 0) {
                        if (line[4].length < 6) {
                            response
                                .status(406)
                                .send(
                                    `Erreur dans le fichier ${filename}: Le mot de passe des managers doit contenir 6 characters minimum.`
                                );
                            return;
                        }

                        allManagers.push({
                            email: line[0] + '@bysafe.app',
                            password: line[4],
                        });
                    }
                    allUsers[line[0]] = {
                        manager: line[3],
                        firstname: line[1],
                        lastname: line[2],
                    };
                }
            }

            allManagers.forEach((manager) =>
                admin
                    .auth()
                    .createUser(manager)
                    .catch((err) => {
                        if (err.code !== 'auth/email-already-exists')
                            return console.error('Cannot create manager:', err);

                        admin
                            .auth()
                            .getUserByEmail(manager.email)
                            .then((user) => {
                                admin
                                    .auth()
                                    .updateUser(user.uid, {
                                        password: manager.password,
                                    })
                                    .catch((err) =>
                                        console.error('Could not update manager pass:', err)
                                    );
                            })
                            .catch((err) =>
                                console.error('Impossible: Could not get manager from email:', err)
                            );
                    })
            );

            for (const id of Object.keys(allUsers))
                db.collection('users').doc(id).set(allUsers[id]);

            response.send(`Opération réalisée avec succès`);
        })
        .catch((e) => {
            console.error('cannot verify IdToken', e);
            response.status(401).send('Vous devez être connecté');
        });
});
