const admin = require("firebase-admin");

if (!admin.apps.length) {
    try {
        if (process.env.FIREBASE_SERVICE_ACCOUNT) {
            let serviceAccount = JSON.parse(process.env.FIREBASE_SERVICE_ACCOUNT);
            if (serviceAccount.private_key) {
                serviceAccount.private_key = serviceAccount.private_key.replace(/\\\\n/g, '\n').replace(/\\n/g, '\n');
            }
            admin.initializeApp({
                credential: admin.credential.cert(serviceAccount)
            });
            console.log("Firebase Admin SDK successfully initialized via FIREBASE_SERVICE_ACCOUNT JSON.");
        } else if (process.env.FIREBASE_PROJECT_ID && process.env.FIREBASE_CLIENT_EMAIL && process.env.FIREBASE_PRIVATE_KEY) {
            let privateKey = process.env.FIREBASE_PRIVATE_KEY;
            if (privateKey) {
                privateKey = privateKey.replace(/\\\\n/g, '\n').replace(/\\n/g, '\n');
            }
            admin.initializeApp({
                credential: admin.credential.cert({
                    projectId: process.env.FIREBASE_PROJECT_ID,
                    clientEmail: process.env.FIREBASE_CLIENT_EMAIL,
                    privateKey: privateKey
                })
            });
            console.log("Firebase Admin SDK successfully initialized via individual environment variables.");
        } else {
            console.log("Firebase Admin SDK skipped (no credentials provided).");
        }
    } catch (error) {
        console.warn("Non-fatal Warning: Firebase Admin initialization failed:", error.message);
    }
}

async function verifyFirebaseToken(idToken) {
    try {
        if (!admin.apps.length) return null;
        if (!idToken) return null;
        const decodedToken = await admin.auth().verifyIdToken(idToken);
        return decodedToken;
    } catch (error) {
        console.error("Firebase token verification error:", error.message);
        return null;
    }
}

module.exports = {
    admin: admin.apps.length ? admin : null,
    verifyFirebaseToken
};
