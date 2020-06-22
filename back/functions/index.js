const functions = require('firebase-functions');
const admin = require('firebase-admin');

admin.initializeApp({ credential: admin.credential.cert(require('./keys/admin.json')) });

const db = admin.firestore();

exports.IdExists = functions.https.onRequest((request, response) => {
    const id = request.params['0'].slice(1);

    if (id.length == 0) {
        response.status(404).send({ error: `A user ID is required` });
    } else if (isNaN(id)) {
        response.status(404).send({ error: `Invalid ID` });
    } else {
        db.collection('users')
            .doc(id)
            .get()
            .then((doc) => {
                if (doc.exists) {
                    response.send(); // 200
                } else {
                    response.status(404).send({ error: `User ${id} does not exist` });
                }
            })
            .catch(console.log);
    }
});

// // ref: https://stackoverflow.com/a/1293163
// const CSVToArray = (strData, strDelimiter) => {
//     strDelimiter = strDelimiter || ',';
//     var objPattern = new RegExp(
//         '(\\' +
//             strDelimiter +
//             '|\\r?\\n|\\r|^)' +
//             '(?:"([^"]*(?:""[^"]*)*)"|' +
//             '([^"\\' +
//             strDelimiter +
//             '\\r\\n]*))',
//         'gi'
//     );

//     var arrData = [[]];
//     var arrMatches = null;

//     while ((arrMatches = objPattern.exec(strData))) {
//         var strMatchedDelimiter = arrMatches[1];
//         if (strMatchedDelimiter.length && strMatchedDelimiter !== strDelimiter) arrData.push([]);

//         var strMatchedValue;
//         if (arrMatches[2]) strMatchedValue = arrMatches[2].replace(new RegExp('""', 'g'), '"');
//         else strMatchedValue = arrMatches[3];
//         arrData[arrData.length - 1].push(strMatchedValue);
//     }
//     return arrData;
// };

// const handleErr = (err) => {
//     console.error(err);
//     response.send(err);
// };

// exports.helloWorld = functions.https.onRequest((request, response) => {
//     // const toto = String(request.body)
//     //     .split('\r\n')[4]
//     //     .split('\n')
//     //     .filter((el) => el != '');

//     // ? here req.query
//     // admin
//     // .auth()
//     // .verifyIdToken(idToken)
//     // .then((decodedToken) => decodedToken.uid)
//     // .catch(handleErr)

//     // ? client
//     // firebase
//     //     .auth()
//     //     .currentUser.getIdToken(true)
//     //     .then((idToken) =>

//     //     )
//     //     .catch(handleErr);

//     // console.log(String(request.body));

//     // db.collection('users').doc('12');
//     response.send('done');
// });
