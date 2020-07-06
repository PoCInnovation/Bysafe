const cors = require('cors')({
    origin: true,
});

exports.GetAllUsers = (admin) => (request, response) => {
    return cors(request, response, () => {
        admin
            .auth()
            .verifyIdToken(request.query.token || '')
            .then(({ uid }) => {
                if (uid !== 'PciCitpbScemcOEnmUsbeYJCeSj1') {
                    response.status(403).send("Vous n'êtes pas l'administrateur");
                    return;
                }

                admin
                    .firestore()
                    .collection('users')
                    .get()
                    .then(({ docs }) => {
                        let users = [];

                        docs.forEach((doc) => {
                            let _data = doc.data();

                            _data.reports = undefined;
                            _data.id = doc.id;
                            users.push(_data);
                        });

                        response.json(users);
                    });
            })
            .catch((e) => {
                console.error('cannot verify IdToken', e);
                response.status(401).send('Vous devez être connecté');
            });
    });
};
