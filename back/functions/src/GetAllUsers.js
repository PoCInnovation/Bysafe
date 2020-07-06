const cors = require('cors');

exports.GetAllUsers = (admin) => (request, response) => {
    admin
        .firestore()
        .collection('users')
        .get()
        .then(({ docs }) => {
            let users = [];

            docs.forEach((doc) => {
                let _data = doc.data();

                users.push(_data);
            });

            return cors(request, response, () => response.json(users));
        });
};
