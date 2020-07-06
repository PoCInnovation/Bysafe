exports.GetReportsFromManager = (admin) => (request, response) => {
    const id = request.params['0'].slice(1);

    if (id.length == 0) {
        response.status(404).json({ error: 'Un indentifiant est requis' });
    } else if (isNaN(id)) {
        response.status(404).json({ error: 'Indentifiant invalide' });
    } else {
        admin
            .firestore()
            .collection('users')
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
};
