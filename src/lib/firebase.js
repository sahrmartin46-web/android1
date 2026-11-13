const admin = require("firebase-admin");

let initialized = false;

try {
    if (!admin.apps.length) {
        if (process.env.FIREBASE_SERVICE_ACCOUNT) {
            let serviceAccount = JSON.parse(process.env.FIREBASE_SERVICE_ACCOUNT);
            if (serviceAccount.private_key) {
                serviceAccount.private_key = serviceAccount.private_key.replace(/\\n/g, '\n').replace(/^"|"$/g, '');
            }
            admin.initializeApp({
                credential: admin.credential.cert(serviceAccount)
            });
            initialized = true;
            console.log("Firebase Admin SDK initialized successfully.");
        } else if (process.env.FIREBASE_PROJECT_ID && process.env.FIREBASE_CLIENT_EMAIL && process.env.FIREBASE_PRIVATE_KEY) {
            let privateKey = process.env.FIREBASE_PRIVATE_KEY;
            if (privateKey) {
                privateKey = privateKey.replace(/\\n/g, '\n').replace(/^"|"$/g, '');
            }
            admin.initializeApp({
                credential: admin.credential.cert({
                    projectId: process.env.FIREBASE_PROJECT_ID,
                    clientEmail: process.env.FIREBASE_CLIENT_EMAIL,
                    privateKey: privateKey
                })
            });
            initialized = true;
            console.log("Firebase Admin SDK initialized successfully via env vars.");
        }
    } else {
        initialized = true;
    }
} catch (error) {
    console.warn("Firebase Admin init warning (non-fatal):", error.message);
}

async function verifyFirebaseToken(idToken) {
    try {
        if (!initialized || !admin.apps.length) return null;
        if (!idToken) return null;
        return await admin.auth().verifyIdToken(idToken);
    } catch (error) {
        console.error("Firebase token verification error:", error.message);
        return null;
    }
}

module.exports = {
    admin: initialized ? admin : null,
    verifyFirebaseToken
};
