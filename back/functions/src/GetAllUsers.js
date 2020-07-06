const cors = require('cors')({
    origin: true,
});

exports.GetAllUsers = (admin) => (request, response) => {
    return cors(request, response, () => {
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
    });
};
