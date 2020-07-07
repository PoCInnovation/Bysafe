const backEndpoint = 'https://us-central1-bysafe-4ee9a.cloudfunctions.net/';
const addUsersEndpoint = backEndpoint + 'AddUsers/';
const deleteUserEndpoint = backEndpoint + 'DeleteUser/';
const getUsersEndpoint = backEndpoint + 'GetAllUsers/';

var global_idToken;

const getElem = (id) => document.getElementById(id);

firebase.auth().onAuthStateChanged((user) => {
    if (user) {
        getElem('auth_section').hidden = true;
        getElem('admin_section').hidden = false;
        user.getIdToken(true).then((idToken) => {
            global_idToken = idToken;
        });
    } else {
        getElem('auth_section').hidden = false;
        getElem('admin_section').hidden = true;
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
    const password = getElem('login_password').value;

    firebase.auth().signInWithEmailAndPassword(email, password).catch(console.error);
};

const queryString = () => '?token=' + global_idToken;

const validateForm = () => {
    getElem('file_form').action = addUsersEndpoint + queryString();
};

const deleteUser = () => {
    const id = getElem('delete_id').value;
    if (id === '' || id === 'admin') return;

    getElem('delete_button').disabled = true;
    const url = deleteUserEndpoint + id + queryString();

    fetch(url, { method: 'POST', mode: 'no-cors' })
        .catch(console.error)
        .finally(() => {
            getElem('delete_id').value = '';
            getElem('delete_button').disabled = false;
        });
};

const refreshUsers = () => {
    getElem('db_refresh').disabled = true;
    getElem('db_refresh').textContent = 'Rafraîchissement en cours';

    getElem('db_content').innerHTML = '';

    fetch(getUsersEndpoint + queryString())
        .then((r) =>
            r.json().then((ret) => {
                ret.sort((l, r) => l.id - r.id).forEach(({ id, firstname, lastname, manager }) => {
                    let value = `<div class="db_elem">👤 ${id} - ${firstname} ${lastname}<br>👥 ${manager}</div>`;
                    getElem('db_content').innerHTML += value;
                });

                getElem('db_refresh').textContent = 'Rafraîchir';
                getElem('db_refresh').disabled = false;
                getElem('db_info').textContent =
                    'Dernier rafraîchissement le ' +
                    new Date().toISOString().substring(0, 19).replace('T', ' à ');
            })
        )
        .catch((e) => {
            console.error(e);
            getElem('db_refresh').textContent = 'Rafraîchir';
            getElem('db_refresh').disabled = false;
            getElem('db_info').textContent = 'Erreur de rafraîchissement';
        });
};

window.addEventListener('DOMContentLoaded', () => {
    getElem('file_input').addEventListener('change', (e) => {
        var inputText = '';
        const filesNb = e.target.files.length;

        if (filesNb == 0) {
            inputText = `Aucun fichier selectionné`;
            getElem('file_submit').disabled = true;
        } else {
            if (filesNb == 1) inputText = e.target.files[0].name;
            else inputText = `${filesNb} fichiers selectionnés`;
            getElem('file_submit').disabled = false;
        }

        getElem('file_select').innerHTML = inputText;
    });
});
