const admin = require("firebase-admin");

let firebaseApp = null;

function initFirebase() {
    if (firebaseApp) return firebaseApp;

    try {
        if (process.env.FIREBASE_SERVICE_ACCOUNT) {
            let serviceAccount = process.env.FIREBASE_SERVICE_ACCOUNT;
            if (typeof serviceAccount === "string") {
                try {
                    serviceAccount = JSON.parse(serviceAccount);
                } catch (e) {
                    // Check if base64 encoded
                    const decoded = Buffer.from(serviceAccount, "base64").toString("utf8");
                    serviceAccount = JSON.parse(decoded);
                }
            }

            firebaseApp = admin.initializeApp({
                credential: admin.credential.cert(serviceAccount)
            });
            console.log("Firebase Admin SDK initialized with FIREBASE_SERVICE_ACCOUNT JSON");
            return firebaseApp;
        }

        if (process.env.FIREBASE_PROJECT_ID && process.env.FIREBASE_CLIENT_EMAIL && process.env.FIREBASE_PRIVATE_KEY) {
            const privateKey = process.env.FIREBASE_PRIVATE_KEY.replace(/\\n/g, "\n");
            firebaseApp = admin.initializeApp({
                credential: admin.credential.cert({
                    projectId: process.env.FIREBASE_PROJECT_ID,
                    clientEmail: process.env.FIREBASE_CLIENT_EMAIL,
                    privateKey: privateKey
                })
            });
            console.log("Firebase Admin SDK initialized with individual environment variables");
            return firebaseApp;
        }

        // Default application default credentials if available
        if (admin.apps.length > 0) {
            firebaseApp = admin.apps[0];
            return firebaseApp;
        }

        console.warn("WARNING: Firebase Admin SDK credentials not configured in environment. Set FIREBASE_SERVICE_ACCOUNT or FIREBASE_PROJECT_ID/CLIENT_EMAIL/PRIVATE_KEY on Sevalla.");
        return null;

    } catch (error) {
        console.error("Failed to initialize Firebase Admin SDK:", error.message);
        return null;
    }
}

async function verifyFirebaseToken(idToken) {
    const app = initFirebase();
    if (!app) {
        throw new Error("Firebase Admin SDK is not initialized. Check Sevalla environment variables.");
    }
    return await admin.auth().verifyIdToken(idToken);
}

module.exports = {
    initFirebase,
    verifyFirebaseToken,
    admin
};
