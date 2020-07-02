const addUsersEndpoint = 'https://us-central1-bysafe-4ee9a.cloudfunctions.net/addUsers';

let global_idToken;

const getElem = (id) => document.getElementById(id);

const signOut = () => {
    firebase
        .auth()
        .signOut()
        .then(() => {})
        .catch((_) => {});
};

const login = () => {
    signOut();

    email = getElem('site_id').value + '@bysafe.app';
    password = getElem('password').value;

    console.log(email, password);

    firebase
        .auth()
        .signInWithEmailAndPassword(email, password)
        .then(console.log)
        .catch(console.error);
};

firebase.auth().onAuthStateChanged((user) => {
    if (user) {
        getElem('login').hidden = true;
        getElem('upload_form').hidden = false;
        user.getIdToken(true).then((idToken) => {
            global_idToken = idToken;
        });
    } else {
        getElem('login').hidden = false;
        getElem('upload_form').hidden = true;
        global_idToken = null;
    }
});

const addIdToken = () => {
    const queryString = '?token=' + global_idToken;

    getElem('upload_form').action = addUsersEndpoint + queryString;
};