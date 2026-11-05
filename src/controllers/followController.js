const prisma = require("../lib/prisma");

async function followUser(req, res, next) {
    try {
        const targetUserId = req.params.userId;

        if (targetUserId === req.user.userId) {
            return res.status(400).json({
                success: false,
                message: "You cannot follow yourself"
            });
        }

        const targetUser = await prisma.user.findUnique({
            where: {
                id: targetUserId
            }
        });

        if (!targetUser) {
            return res.status(404).json({
                success: false,
                message: "User not found"
            });
        }

        const existingFollow = await prisma.follow.findUnique({
            where: {
                followerId_followingId: {
                    followerId: req.user.userId,
                    followingId: targetUserId
                }
            }
        });

        if (existingFollow) {
            await prisma.follow.delete({
                where: {
                    id: existingFollow.id
                }
            });

            return res.json({
                success: true,
                following: false,
                message: "User unfollowed"
            });
        }

        await prisma.follow.create({
            data: {
                followerId: req.user.userId,
                followingId: targetUserId
            }
        });

        res.json({
            success: true,
            following: true,
            message: "User followed"
        });

    } catch (error) {
        next(error);
    }
}

async function getFollowers(req, res, next) {
    try {
        const followers = await prisma.follow.findMany({
            where: {
                followingId: req.params.userId
            },
            include: {
                follower: {
                    select: {
                        id: true,
                        username: true,
                        displayName: true,
                        avatarUrl: true
                    }
                }
            },
            orderBy: {
                createdAt: "desc"
            }
        });

        res.json({
            success: true,
            followers: followers.map(item => item.follower)
        });

    } catch (error) {
        next(error);
    }
}

async function getFollowing(req, res, next) {
    try {
        const following = await prisma.follow.findMany({
            where: {
                followerId: req.params.userId
            },
            include: {
                following: {
                    select: {
                        id: true,
                        username: true,
                        displayName: true,
                        avatarUrl: true
                    }
                }
            },
            orderBy: {
                createdAt: "desc"
            }
        });

        res.json({
            success: true,
            following: following.map(item => item.following)
        });

    } catch (error) {
        next(error);
    }
}

module.exports = {
    followUser,
    getFollowers,
    getFollowing
};
