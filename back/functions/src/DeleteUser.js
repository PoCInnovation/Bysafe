exports.DeleteUser = (admin) => (request, response) => {
    const id = request.params['0'].slice(1);

    if (id.length == 0) {
        response.status(404).json({ error: 'Un indentifiant est requis' });
    } else if (isNaN(id)) {
        response.status(404).json({ error: 'Indentifiant invalide' });
    } else {
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
                    .doc(id)
                    .delete()
                    .catch((error) => console.error('Error removing document: ', error));
                admin
                    .auth()
                    .getUserByEmail(id + '@bysafe.app')
                    .then(({ uid }) =>
                        admin
                            .auth()
                            .deleteUser(uid)
                            .catch((error) => console.error('Error deleting user: ', error))
                    )
                    .catch(() => {});
                response.send();
            })
            .catch((e) => {
                console.error('cannot verify IdToken', e);
                response.status(401).send('Vous devez être connecté');
            });
    }
};
