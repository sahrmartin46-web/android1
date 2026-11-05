const { z } = require("zod");
const prisma = require("../lib/prisma");
const { calculateAndRankFeed } = require("./feedEngine");

const optionalUrl = z.string().url().optional().or(z.literal("")).nullable();

const createPostSchema = z.object({
    content: z.string().min(1).max(5000),
    imageUrl: optionalUrl,
    videoUrl: optionalUrl
});

async function createPost(req, res, next) {
    try {
        const data = createPostSchema.parse(req.body);

        const post = await prisma.post.create({
            data: {
                content: data.content,
                imageUrl: data.imageUrl || null,
                videoUrl: data.videoUrl || null,
                authorId: req.user.userId
            },
            include: {
                author: {
                    select: {
                        id: true,
                        username: true,
                        displayName: true,
                        avatarUrl: true,
                        isVerified: true
                    }
                },
                _count: {
                    select: {
                        likes: true,
                        comments: true
                    }
                }
            }
        });

        res.status(201).json({
            success: true,
            message: "Post created",
            post
        });

    } catch (error) {
        next(error);
    }
}

async function getPosts(req, res, next) {
    try {
        const limit = Math.min(Math.max(Number(req.query.limit) || 20, 1), 50);
        const feedType = req.query.feedType || "foryou";
        const currentUserId = req.query.currentUserId;
        const cursor = req.query.cursor || null;

        const { posts, nextCursor } = await calculateAndRankFeed(currentUserId, feedType, cursor, limit);

        res.json({
            success: true,
            limit,
            nextCursor,
            posts
        });

    } catch (error) {
        next(error);
    }
}

async function getPost(req, res, next) {
    try {
        const post = await prisma.post.findUnique({
            where: {
                id: req.params.id
            },
            include: {
                author: {
                    select: {
                        id: true,
                        username: true,
                        displayName: true,
                        avatarUrl: true,
                        isVerified: true
                    }
                },
                likes: {
                    select: {
                        userId: true
                    }
                },
                comments: {
                    orderBy: {
                        createdAt: "desc"
                    },
                    include: {
                        user: {
                            select: {
                                id: true,
                                username: true,
                                displayName: true,
                                avatarUrl: true,
                                isVerified: true
                            }
                        }
                    }
                }
            }
        });

        if (!post) {
            return res.status(404).json({
                success: false,
                message: "Post not found"
            });
        }

        res.json({
            success: true,
            post
        });

    } catch (error) {
        next(error);
    }
}

async function deletePost(req, res, next) {
    try {
        const post = await prisma.post.findUnique({
            where: {
                id: req.params.id
            }
        });

        if (!post) {
            return res.status(404).json({
                success: false,
                message: "Post not found"
            });
        }

        if (post.authorId !== req.user.userId) {
            return res.status(403).json({
                success: false,
                message: "You can only delete your own posts"
            });
        }

        await prisma.post.delete({
            where: {
                id: req.params.id
            }
        });

        res.json({
            success: true,
            message: "Post deleted"
        });

    } catch (error) {
        next(error);
    }
}

async function likePost(req, res, next) {
    try {
        const post = await prisma.post.findUnique({
            where: {
                id: req.params.id
            }
        });

        if (!post) {
            return res.status(404).json({
                success: false,
                message: "Post not found"
            });
        }

        const existingLike = await prisma.like.findUnique({
            where: {
                userId_postId: {
                    userId: req.user.userId,
                    postId: req.params.id
                }
            }
        });

        if (existingLike) {
            await prisma.like.delete({
                where: {
                    id: existingLike.id
                }
            });

            return res.json({
                success: true,
                liked: false,
                message: "Post unliked"
            });
        }

        await prisma.like.create({
            data: {
                userId: req.user.userId,
                postId: req.params.id
            }
        });

        res.json({
            success: true,
            liked: true,
            message: "Post liked"
        });

    } catch (error) {
        next(error);
    }
}

async function incrementView(req, res, next) {
    try {
        const postId = req.params.id;
        const post = await prisma.post.update({
            where: { id: postId },
            data: {
                views: { increment: 1 }
            }
        });
        res.json({
            success: true,
            views: post.views
        });
    } catch (error) {
        next(error);
    }
}

module.exports = {
    createPost,
    getPosts,
    getPost,
    deletePost,
    likePost,
    incrementView
};
