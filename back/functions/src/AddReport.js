exports.AddReport = (admin) => (request, response) => {
    const data = request.body;

    for (const id of Object.keys(data))
        admin.firestore().collection('users').doc(id).set({ reports: data[id] }, { merge: true });

    response.json();
};
