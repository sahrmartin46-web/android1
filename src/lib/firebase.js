const admin = require("firebase-admin");

if (!admin.apps.length) {
    try {
        if (process.env.FIREBASE_SERVICE_ACCOUNT) {
            const serviceAccount = JSON.parse(process.env.FIREBASE_SERVICE_ACCOUNT);
            admin.initializeApp({
                credential: admin.credential.cert(serviceAccount)
            });
            console.log("Firebase Admin initialized via FIREBASE_SERVICE_ACCOUNT JSON.");
        } else if (process.env.FIREBASE_PROJECT_ID && process.env.FIREBASE_CLIENT_EMAIL && process.env.FIREBASE_PRIVATE_KEY) {
            admin.initializeApp({
                credential: admin.credential.cert({
                    projectId: process.env.FIREBASE_PROJECT_ID,
                    clientEmail: process.env.FIREBASE_CLIENT_EMAIL,
                    privateKey: process.env.FIREBASE_PRIVATE_KEY.replace(/\\n/g, '\n')
                })
            });
            console.log("Firebase Admin initialized via individual environment variables.");
        } else {
            admin.initializeApp();
            console.log("Firebase Admin initialized via Application Default Credentials.");
        }
    } catch (error) {
        console.warn("Warning: Firebase Admin initialization failed:", error.message);
    }
}

async function verifyFirebaseToken(idToken) {
    try {
        if (!idToken) return null;
        const decodedToken = await admin.auth().verifyIdToken(idToken);
        return decodedToken;
    } catch (error) {
        console.error("Firebase token verification error:", error.message);
        return null;
    }
}

module.exports = {
    admin,
    verifyFirebaseToken
};
