const functions = require('firebase-functions');
const admin = require('firebase-admin');

admin.initializeApp({ credential: admin.credential.cert(require('./keys/admin.json')) });

const db = admin.firestore();

exports.IdExists = functions.https.onRequest((request, response) => {
    const id = request.params['0'].slice(1);

    if (id.length == 0) {
        response.status(404).json({ error: `A user ID is required` });
    } else if (isNaN(id)) {
        response.status(404).json({ error: `Invalid ID` });
    } else {
        db.collection('users')
            .doc(id)
            .get()
            .then((doc) => {
                if (doc.exists) {
                    response.json(); // 200
                } else {
                    response.status(404).json({ error: `User ${id} does not exist` });
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
        response.status(404).json({ error: `A user ID is required` });
    } else if (isNaN(id)) {
        response.status(404).json({ error: `Invalid ID` });
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
        response.status(404).json({ error: `A user ID is required` });
    } else if (isNaN(id)) {
        response.status(404).json({ error: `Invalid ID` });
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

const managerIntegrity = (manager) => {
    if (manager.length === 2) return true;
    response
        .status(406)
        .send(
            `Erreur dans le fichier ${filename}: La première ligne doit contenir le numéro de chantier et le mot de passe du manager`
        );
    return false;
};

exports.addUsers = functions.https.onRequest((request, response) => {
    admin
        .auth()
        .verifyIdToken(request.query.token)
        .then(({ uid }) => {
            if (uid !== '8CScTXV2ITg9HeLpqGDGYwxQkD93') {
                response.status(403).send('You must be the admin');
                return;
            }

            const multipart = require('parse-multipart');

            const boundary = multipart.getBoundary(request.headers['content-type']);
            const parts = multipart.Parse(request.body, boundary);

            let managerGroups = {};

            for (let i = 0; i < parts.length; i++) {
                const { data, filename } = parts[i];
                const lines = CSVToArray(String(data));

                const manager = lines[0];
                if (!managerIntegrity(manager)) return;

                admin
                    .auth()
                    .createUser({
                        email: manager[0] + '@bysafe.app',
                    })
                    .catch(() => {});

                const users = lines.slice(1);

                users.forEach((user) => {
                    managerGroups[manager[0]];
                });

                //    .forEach((e) => {
                //         if (e.length !== 3) return;
                //         console.log(e[0], e[1], e[2]);
                //     });
            }

            response.send(boundary);
        })
        .catch(() => {
            response.status(401).send('You must be logged in');
        });
});

// const handleErr = (err) => {
//     console.error(err);
//     response.send(err);
// };

// exports.helloWorld = functions.https.onRequest((request, response) => {
//     // const toto = String(request.body)
//     //     .split('\r\n')[4]
//     //     .split('\n')
//     //     .filter((el) => el != '');

//     // console.log(String(request.body));

//     // db.collection('users').doc('12');
//     response.send('done');
// });
