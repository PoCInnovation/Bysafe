exports.IdExists = (admin) => (request, response) => {
    const id = request.params['0'].slice(1);

    if (id.length == 0) {
        response.status(404).json({ error: 'Un indentifiant est requis' });
    } else if (isNaN(id)) {
        response.status(404).json({ error: 'Indentifiant invalide' });
    } else {
        admin
            .firestore()
            .collection('users')
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
};
