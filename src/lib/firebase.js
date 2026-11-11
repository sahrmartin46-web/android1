const admin = require("firebase-admin");

if (!admin.apps.length) {
    try {
        if (process.env.FIREBASE_SERVICE_ACCOUNT) {
            let serviceAccount;
            try {
                serviceAccount = JSON.parse(process.env.FIREBASE_SERVICE_ACCOUNT);
            } catch (e) {
                serviceAccount = JSON.parse(Buffer.from(process.env.FIREBASE_SERVICE_ACCOUNT, 'base64').toString('utf8'));
            }
            admin.initializeApp({
                credential: admin.credential.cert(serviceAccount)
            });
        } else if (process.env.FIREBASE_PROJECT_ID && process.env.FIREBASE_CLIENT_EMAIL && process.env.FIREBASE_PRIVATE_KEY) {
            admin.initializeApp({
                credential: admin.credential.cert({
                    projectId: process.env.FIREBASE_PROJECT_ID,
                    clientEmail: process.env.FIREBASE_CLIENT_EMAIL,
                    privateKey: process.env.FIREBASE_PRIVATE_KEY.replace(/\\n/g, '\n')
                })
            });
        } else {
            admin.initializeApp();
        }
    } catch (error) {
        console.error("Firebase Admin initialization error:", error.message);
    }
}

async function verifyFirebaseToken(idToken) {
    try {
        if (!admin.apps.length) {
            throw new Error("Firebase Admin SDK is not initialized");
        }
        const decodedToken = await admin.auth().verifyIdToken(idToken);
        return decodedToken;
    } catch (error) {
        throw new Error(`Firebase token verification failed: ${error.message}`);
    }
}

module.exports = {
    admin,
    verifyFirebaseToken
};
