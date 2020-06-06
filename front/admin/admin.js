let id;

const getElem = (id) => document.getElementById(id);

const signOut = () => {
    firebase
        .auth()
        .signOut()
        .then(() => {})
        .catch((_) => {});
};

const updateDisplay = (user) => {
    const u = user != null;

    getElem('btn-create').disabled = u || !id;
    getElem('btn-delete').disabled = !u;
};

const buildCredentials = (id) => ({
    email: id + '@bysafe.app',
    password: 'no-pass',
});

const maybeConnect = () => {
    getElem('btn-create').disabled = true;
    getElem('btn-delete').disabled = true;

    signOut();

    id = getElem('site_id').value;
    if (id) getElem('display_id').innerHTML = id;
    else {
        getElem('display_id').innerHTML = 'none';
        return;
    }

    let { email, password } = buildCredentials(id);

    firebase
        .auth()
        .signInWithEmailAndPassword(email, password)
        .catch((_) => updateDisplay(null));
};

const createUser = () => {
    let { email, password } = buildCredentials(id);

    firebase.auth().createUserWithEmailAndPassword(email, password).catch(console.log);
};

const deleteUser = () => {
    firebase
        .auth()
        .currentUser.delete()
        .then(() => {})
        .catch(console.log);
};

firebase.auth().onAuthStateChanged(updateDisplay);
