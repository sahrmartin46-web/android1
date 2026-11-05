const { z } = require("zod");
const bcrypt = require("bcryptjs");
const prisma = require("../lib/prisma");

const updateSettingsSchema = z.object({
    isPrivateAccount: z.boolean().optional(),
    postAudience: z.enum(["EVERYONE", "FRIENDS", "ONLY_ME"]).optional(),
    commentPermission: z.enum(["EVERYONE", "FOLLOWERS", "FRIENDS", "NO_ONE"]).optional(),
    directMessagePermission: z.enum(["EVERYONE", "FRIENDS", "NO_ONE"]).optional(),
    duetPermission: z.enum(["EVERYONE", "FRIENDS", "NO_ONE"]).optional(),
    stitchPermission: z.enum(["EVERYONE", "FRIENDS", "NO_ONE"]).optional(),
    downloadPermission: z.enum(["EVERYONE", "NO_ONE"]).optional(),
    mentionPermission: z.enum(["EVERYONE", "FOLLOWERS", "NO_ONE"]).optional(),
    pushNotificationsEnabled: z.boolean().optional(),
    likesNotification: z.boolean().optional(),
    commentsNotification: z.boolean().optional(),
    newFollowersNotification: z.boolean().optional(),
    directMessagesNotification: z.boolean().optional(),
    liveAlertsNotification: z.boolean().optional(),
    quietHoursEnabled: z.boolean().optional(),
    quietHoursStart: z.string().optional(),
    quietHoursEnd: z.string().optional(),
    dataSaverEnabled: z.boolean().optional(),
    autoplayCellular: z.boolean().optional(),
    appLanguage: z.string().optional(),
    screenTimeLimitMinutes: z.number().int().nonnegative().optional(),
    restrictedMode: z.boolean().optional(),
    liveGuestPermission: z.enum(["EVERYONE", "FOLLOWERS", "FRIENDS", "NO_ONE"]).optional()
});

const changePasswordSchema = z.object({
    oldPassword: z.string().min(1),
    newPassword: z.string().min(8)
});

async function getSettings(req, res, next) {
    try {
        let settings = await prisma.userSettings.findUnique({
            where: { userId: req.user.userId }
        });

        if (!settings) {
            settings = await prisma.userSettings.create({
                data: { userId: req.user.userId }
            });
        }

        res.json({ success: true, settings });
    } catch (error) {
        next(error);
    }
}

async function updateSettings(req, res, next) {
    try {
        const validated = updateSettingsSchema.parse(req.body);

        const settings = await prisma.userSettings.upsert({
            where: { userId: req.user.userId },
            create: { userId: req.user.userId, ...validated },
            update: validated
        });

        res.json({ success: true, message: "Settings updated", settings });
    } catch (error) {
        next(error);
    }
}

async function changePassword(req, res, next) {
    try {
        const { oldPassword, newPassword } = changePasswordSchema.parse(req.body);
        const user = await prisma.user.findUnique({ where: { id: req.user.userId } });

        if (!user) {
            return res.status(404).json({ success: false, message: "User not found" });
        }

        const valid = await bcrypt.compare(oldPassword, user.passwordHash);
        if (!valid) {
            return res.status(401).json({ success: false, message: "Current password is incorrect" });
        }

        const newHash = await bcrypt.hash(newPassword, 12);
        await prisma.user.update({
            where: { id: req.user.userId },
            data: { passwordHash: newHash }
        });

        await prisma.auditLog.create({
            data: { userId: req.user.userId, action: "PASSWORD_CHANGED", details: "Password changed successfully" }
        });

        res.json({ success: true, message: "Password updated successfully" });
    } catch (error) {
        next(error);
    }
}

async function switchAccountType(req, res, next) {
    try {
        const { targetRole } = req.body;
        const newRole = targetRole === "CREATOR" ? "CREATOR" : "USER";

        const updatedUser = await prisma.user.update({
            where: { id: req.user.userId },
            data: { role: newRole },
            select: { id: true, username: true, email: true, role: true, displayName: true, avatarUrl: true }
        });

        await prisma.auditLog.create({
            data: { userId: req.user.userId, action: "ACCOUNT_TYPE_SWITCHED", details: `Role changed to ${newRole}` }
        });

        res.json({ success: true, message: `Account type switched to ${newRole}`, user: updatedUser });
    } catch (error) {
        next(error);
    }
}

async function getSessions(req, res, next) {
    try {
        let sessions = await prisma.userSession.findMany({
            where: { userId: req.user.userId },
            orderBy: { lastActive: "desc" }
        });

        if (sessions.length === 0) {
            const currentSession = await prisma.userSession.create({
                data: {
                    userId: req.user.userId,
                    deviceName: "Android Device (Current)",
                    deviceType: "ANDROID",
                    isCurrent: true
                }
            });
            sessions = [currentSession];
        }

        res.json({ success: true, sessions });
    } catch (error) {
        next(error);
    }
}

async function revokeSession(req, res, next) {
    try {
        const { sessionId } = req.params;
        await prisma.userSession.deleteMany({
            where: { id: sessionId, userId: req.user.userId }
        });
        res.json({ success: true, message: "Session revoked" });
    } catch (error) {
        next(error);
    }
}

async function getBlockedUsers(req, res, next) {
    try {
        const blocks = await prisma.blockedUser.findMany({
            where: { userId: req.user.userId },
            include: {
                blocked: {
                    select: { id: true, username: true, displayName: true, avatarUrl: true }
                }
            }
        });
        res.json({ success: true, blockedUsers: blocks.map(b => b.blocked) });
    } catch (error) {
        next(error);
    }
}

async function blockUser(req, res, next) {
    try {
        const { targetUserId } = req.body;
        if (!targetUserId) {
            return res.status(400).json({ success: false, message: "targetUserId is required" });
        }
        await prisma.blockedUser.upsert({
            where: { userId_blockedId: { userId: req.user.userId, blockedId: targetUserId } },
            create: { userId: req.user.userId, blockedId: targetUserId },
            update: {}
        });
        res.json({ success: true, message: "User blocked" });
    } catch (error) {
        next(error);
    }
}

async function unblockUser(req, res, next) {
    try {
        const { targetUserId } = req.params;
        await prisma.blockedUser.deleteMany({
            where: { userId: req.user.userId, blockedId: targetUserId }
        });
        res.json({ success: true, message: "User unblocked" });
    } catch (error) {
        next(error);
    }
}

async function getKeywordFilters(req, res, next) {
    try {
        const filters = await prisma.keywordFilter.findMany({
            where: { userId: req.user.userId },
            orderBy: { createdAt: "desc" }
        });
        res.json({ success: true, keywords: filters.map(f => f.keyword) });
    } catch (error) {
        next(error);
    }
}

async function addKeywordFilter(req, res, next) {
    try {
        const { keyword } = req.body;
        if (!keyword || typeof keyword !== "string") {
            return res.status(400).json({ success: false, message: "Valid keyword required" });
        }
        const cleaned = keyword.trim().toLowerCase();
        await prisma.keywordFilter.upsert({
            where: { userId_keyword: { userId: req.user.userId, keyword: cleaned } },
            create: { userId: req.user.userId, keyword: cleaned },
            update: {}
        });
        res.json({ success: true, message: "Keyword filter added" });
    } catch (error) {
        next(error);
    }
}

async function deleteKeywordFilter(req, res, next) {
    try {
        const { keyword } = req.params;
        await prisma.keywordFilter.deleteMany({
            where: { userId: req.user.userId, keyword: keyword.toLowerCase() }
        });
        res.json({ success: true, message: "Keyword filter removed" });
    } catch (error) {
        next(error);
    }
}

async function exportUserData(req, res, next) {
    try {
        const user = await prisma.user.findUnique({
            where: { id: req.user.userId },
            include: {
                settings: true,
                posts: true,
                comments: true,
                wallet: true
            }
        });
        res.json({
            success: true,
            exportData: {
                user: { id: user.id, username: user.username, email: user.email, displayName: user.displayName, createdAt: user.createdAt },
                settings: user.settings,
                postsCount: user.posts.length,
                commentsCount: user.comments.length,
                wallet: user.wallet,
                exportedAt: new Date().toISOString()
            }
        });
    } catch (error) {
        next(error);
    }
}

async function deactivateAccount(req, res, next) {
    try {
        await prisma.auditLog.create({
            data: { userId: req.user.userId, action: "ACCOUNT_DEACTIVATED", details: "User deactivated account" }
        });
        res.json({ success: true, message: "Account deactivated successfully" });
    } catch (error) {
        next(error);
    }
}

async function deleteAccount(req, res, next) {
    try {
        await prisma.auditLog.create({
            data: { userId: req.user.userId, action: "ACCOUNT_DELETED", details: "Permanent account deletion requested" }
        });
        await prisma.user.delete({ where: { id: req.user.userId } });
        res.json({ success: true, message: "Account permanently deleted" });
    } catch (error) {
        next(error);
    }
}

module.exports = {
    getSettings,
    updateSettings,
    changePassword,
    switchAccountType,
    getSessions,
    revokeSession,
    getBlockedUsers,
    blockUser,
    unblockUser,
    getKeywordFilters,
    addKeywordFilter,
    deleteKeywordFilter,
    exportUserData,
    deactivateAccount,
    deleteAccount
};
