const { z } = require("zod");

const prisma = require("../lib/prisma");

const commentSchema = z.object({
    content: z.string().min(1).max(2000)
});

async function createComment(req, res, next) {
    try {
        const data = commentSchema.parse(req.body);

        const post = await prisma.post.findUnique({
            where: {
                id: req.params.postId
            }
        });

        if (!post) {
            return res.status(404).json({
                success: false,
                message: "Post not found"
            });
        }

        const comment = await prisma.comment.create({
            data: {
                content: data.content,
                postId: req.params.postId,
                userId: req.user.userId
            },
            include: {
                user: {
                    select: {
                        id: true,
                        username: true,
                        displayName: true,
                        avatarUrl: true
                    }
                }
            }
        });

        res.status(201).json({
            success: true,
            message: "Comment created",
            comment
        });

    } catch (error) {
        next(error);
    }
}

async function getComments(req, res, next) {
    try {
        const comments = await prisma.comment.findMany({
            where: {
                postId: req.params.postId
            },
            orderBy: {
                createdAt: "desc"
            },
            include: {
                user: {
                    select: {
                        id: true,
                        username: true,
                        displayName: true,
                        avatarUrl: true
                    }
                }
            }
        });

        res.json({
            success: true,
            comments
        });

    } catch (error) {
        next(error);
    }
}

async function deleteComment(req, res, next) {
    try {
        const comment = await prisma.comment.findUnique({
            where: {
                id: req.params.commentId
            }
        });

        if (!comment) {
            return res.status(404).json({
                success: false,
                message: "Comment not found"
            });
        }

        if (comment.userId !== req.user.userId) {
            return res.status(403).json({
                success: false,
                message: "You can only delete your own comments"
            });
        }

        await prisma.comment.delete({
            where: {
                id: req.params.commentId
            }
        });

        res.json({
            success: true,
            message: "Comment deleted"
        });

    } catch (error) {
        next(error);
    }
}

module.exports = {
    createComment,
    getComments,
    deleteComment
};
