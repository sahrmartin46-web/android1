const jwt = require("jsonwebtoken");
const { verifyFirebaseToken } = require("../lib/firebase");
const prisma = require("../lib/prisma");

async function authMiddleware(req, res, next) {
    try {
        const authHeader = req.headers.authorization;

        if (!authHeader) {
            return res.status(401).json({
                success: false,
                message: "Authorization token required"
            });
        }

        const parts = authHeader.split(" ");

        if (parts.length !== 2 || parts[0] !== "Bearer") {
            return res.status(401).json({
                success: false,
                message: "Invalid authorization format"
            });
        }

        const token = parts[1];

        // 1. Try Firebase ID Token verification
        try {
            const decodedFirebaseToken = await verifyFirebaseToken(token);
            if (decodedFirebaseToken && decodedFirebaseToken.uid) {
                const firebaseUid = decodedFirebaseToken.uid;
                const email = (decodedFirebaseToken.email || `${firebaseUid}@fugen.app`).toLowerCase();

                let user = await prisma.user.findFirst({
                    where: {
                        OR: [
                            { firebaseUid: firebaseUid },
                            { email: email }
                        ]
                    }
                });

                if (!user) {
                    // Auto-create user profile linked to Firebase UID
                    const baseUsername = email.split("@")[0].replace(/[^a-zA-Z0-9_]/g, "") || `user_${Date.now()}`;
                    const uniqueUsername = `${baseUsername}_${Math.floor(1000 + Math.random() * 9000)}`;

                    user = await prisma.user.create({
                        data: {
                            firebaseUid: firebaseUid,
                            email: email,
                            username: uniqueUsername,
                            displayName: decodedFirebaseToken.name || uniqueUsername,
                            avatarUrl: decodedFirebaseToken.picture || null,
                            isVerified: decodedFirebaseToken.email_verified || false
                        }
                    });
                } else if (!user.firebaseUid) {
                    // Link existing email user to Firebase UID
                    user = await prisma.user.update({
                        where: { id: user.id },
                        data: { firebaseUid: firebaseUid }
                    });
                }

                req.user = {
                    userId: user.id,
                    firebaseUid: user.firebaseUid,
                    email: user.email,
                    username: user.username,
                    role: user.role
                };

                return next();
            }
        } catch (firebaseErr) {
            // Token is not a valid Firebase ID Token; proceed to legacy JWT check
        }

        // 2. Fallback to Legacy JWT verification
        const secret = process.env.JWT_SECRET || "default_jwt_secret";
        const decoded = jwt.verify(token, secret);

        req.user = decoded;
        next();

    } catch (error) {
        return res.status(401).json({
            success: false,
            message: "Invalid or expired token"
        });
    }
}

module.exports = authMiddleware;
