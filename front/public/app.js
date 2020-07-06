const backEndpoint = 'https://us-central1-bysafe-4ee9a.cloudfunctions.net/';
const addUsersEndpoint = backEndpoint + 'AddUsers/';
const deleteUserEndpoint = backEndpoint + 'DeleteUser/';
const getUsersEndpoint = backEndpoint + 'GetAllUsers/';

var global_idToken;

const getElem = (id) => document.getElementById(id);

firebase.auth().onAuthStateChanged((user) => {
    if (user) {
        getElem('login').hidden = true;
        getElem('logged_in').hidden = false;
        user.getIdToken(true).then((idToken) => {
            global_idToken = idToken;
        });
    } else {
        getElem('login').hidden = false;
        getElem('logged_in').hidden = true;
        global_idToken = null;
    }
});

const signOut = () => {
    firebase
        .auth()
        .signOut()
        .catch(() => {});
};

const login = () => {
    signOut();

    const email = 'admin@bysafe.app';
    const password = getElem('password').value;

    console.log(email, password);

    firebase
        .auth()
        .signInWithEmailAndPassword(email, password)
        .then(console.log)
        .catch(console.error);
};

const queryString = () => '?token=' + global_idToken;

const validateForm = () => {
    getElem('upload_form').action = addUsersEndpoint + queryString();
};

const deleteUser = () => {
    const url = deleteUserEndpoint + getElem('delete_id').value + queryString();

    fetch(url, { method: 'POST', mode: 'no-cors' }).catch(console.error);
};

const refreshUsers = () => {
    getElem('refresh_db').textContent = 'Rafraîchissement en cours';

    getElem('all_db').innerHTML = '';

    fetch(getUsersEndpoint + queryString())
        .then((r) =>
            r.json().then((ret) => {
                ret.sort((l, r) => l.id - r.id).forEach(({ id, firstname, lastname, manager }) => {
                    let value = `<div class="db_elem">👤 ${id} - ${firstname} ${lastname}<br>👥 ${manager}</div>`;
                    getElem('all_db').innerHTML += value;
                });

                getElem('refresh_db').textContent = 'Rafraîchir';
                getElem('info_db').textContent =
                    'Dernier rafraîchissement le ' +
                    new Date().toISOString().substring(0, 19).replace('T', ' à ');
            })
        )
        .catch((e) => {
            console.error(e);
            getElem('refresh_db').textContent = 'Rafraîchir';
            getElem('info_db').textContent = 'Erreur de rafraîchissement';
        });
};
