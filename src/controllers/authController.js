const bcrypt = require("bcryptjs");
const jwt = require("jsonwebtoken");
const { z } = require("zod");

const prisma = require("../lib/prisma");

const registerSchema = z.object({
    username: z
        .string()
        .min(3)
        .max(30)
        .regex(/^[a-zA-Z0-9_]+$/, "Username can only contain letters, numbers, and underscores"),

    email: z.string().email(),

    password: z.string().min(8).max(100),

    displayName: z
        .string()
        .min(1)
        .max(100)
        .optional()
});

function createToken(user) {
    const secret = process.env.JWT_SECRET || "default_jwt_secret";
    return jwt.sign(
        {
            userId: user.id,
            email: user.email,
            username: user.username
        },
        secret,
        {
            expiresIn: "7d"
        }
    );
}

async function register(req, res, next) {
    try {
        const data = registerSchema.parse(req.body);

        const existingUser = await prisma.user.findFirst({
            where: {
                OR: [
                    { email: data.email.toLowerCase() },
                    { username: data.username }
                ]
            }
        });

        if (existingUser) {
            return res.status(409).json({
                success: false,
                message: "Username or email already exists"
            });
        }

        const passwordHash = await bcrypt.hash(
            data.password,
            12
        );

        const user = await prisma.user.create({
            data: {
                username: data.username,
                email: data.email.toLowerCase(),
                passwordHash,
                displayName: data.displayName || data.username
            },
            select: {
                id: true,
                username: true,
                email: true,
                displayName: true,
                bio: true,
                avatarUrl: true,
                coverUrl: true,
                createdAt: true
            }
        });

        const token = createToken(user);

        res.status(201).json({
            success: true,
            message: "Registration successful",
            token,
            user
        });

    } catch (error) {
        next(error);
    }
}

async function login(req, res, next) {
    try {
        const identifier = (req.body.email || req.body.username || req.body.identifier || "").trim();
        const password = req.body.password;

        if (!identifier || !password) {
            return res.status(400).json({
                success: false,
                message: "Email/Username and password are required"
            });
        }

        const user = await prisma.user.findFirst({
            where: {
                OR: [
                    { email: identifier.toLowerCase() },
                    { username: identifier }
                ]
            }
        });

        if (!user) {
            return res.status(401).json({
                success: false,
                message: "Invalid email/username or password"
            });
        }

        const validPassword = await bcrypt.compare(
            password,
            user.passwordHash
        );

        if (!validPassword) {
            return res.status(401).json({
                success: false,
                message: "Invalid email/username or password"
            });
        }

        const token = createToken(user);

        const safeUser = {
            id: user.id,
            username: user.username,
            email: user.email,
            displayName: user.displayName,
            bio: user.bio,
            avatarUrl: user.avatarUrl,
            coverUrl: user.coverUrl,
            createdAt: user.createdAt
        };

        res.json({
            success: true,
            message: "Login successful",
            token,
            user: safeUser
        });

    } catch (error) {
        next(error);
    }
}

async function me(req, res, next) {
    try {
        const user = await prisma.user.findUnique({
            where: {
                id: req.user.userId
            },
            select: {
                id: true,
                username: true,
                email: true,
                displayName: true,
                bio: true,
                avatarUrl: true,
                coverUrl: true,
                createdAt: true
            }
        });

        if (!user) {
            return res.status(404).json({
                success: false,
                message: "User not found"
            });
        }

        res.json({
            success: true,
            user
        });

    } catch (error) {
        next(error);
    }
}

module.exports = {
    register,
    login,
    me
};
