const { z } = require("zod");

const prisma = require("../lib/prisma");

const optionalUrl = z.string().optional().or(z.literal("")).nullable();

const updateProfileSchema = z.object({
    displayName: z.string().min(1).max(100).optional(),
    bio: z.string().max(500).optional(),
    avatarUrl: optionalUrl,
    coverUrl: optionalUrl
});

async function getProfile(req, res, next) {
    try {
        const { username } = req.params;

        const user = await prisma.user.findUnique({
            where: {
                username
            },
            select: {
                id: true,
                username: true,
                displayName: true,
                bio: true,
                avatarUrl: true,
                coverUrl: true,
                createdAt: true,
                _count: {
                    select: {
                        posts: true,
                        followers: true,
                        following: true
                    }
                }
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

async function getProfileById(req, res, next) {
    try {
        const { userId } = req.params;

        const user = await prisma.user.findUnique({
            where: {
                id: userId
            },
            select: {
                id: true,
                username: true,
                displayName: true,
                bio: true,
                avatarUrl: true,
                coverUrl: true,
                createdAt: true,
                _count: {
                    select: {
                        posts: true,
                        followers: true,
                        following: true
                    }
                }
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

async function getMyProfile(req, res, next) {
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
                createdAt: true,
                _count: {
                    select: {
                        posts: true,
                        followers: true,
                        following: true
                    }
                }
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

async function updateProfile(req, res, next) {
    try {
        const data = updateProfileSchema.parse(req.body);

        const updateData = {};
        if (data.displayName !== undefined) updateData.displayName = data.displayName;
        if (data.bio !== undefined) updateData.bio = data.bio;
        if (data.avatarUrl !== undefined) updateData.avatarUrl = data.avatarUrl || null;
        if (data.coverUrl !== undefined) updateData.coverUrl = data.coverUrl || null;

        const user = await prisma.user.update({
            where: {
                id: req.user.userId
            },
            data: updateData,
            select: {
                id: true,
                username: true,
                email: true,
                displayName: true,
                bio: true,
                avatarUrl: true,
                coverUrl: true
            }
        });

        res.json({
            success: true,
            message: "Profile updated",
            user
        });

    } catch (error) {
        next(error);
    }
}

module.exports = {
    getProfile,
    getProfileById,
    getMyProfile,
    updateProfile
};
