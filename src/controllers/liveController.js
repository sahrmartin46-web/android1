const { z } = require("zod");
const prisma = require("../lib/prisma");
const { createLiveKitToken, removeLiveKitParticipant, muteLiveKitParticipantTrack } = require("../services/livekitService");
const { emitToLive } = require("../lib/socket");

const createSessionSchema = z.object({
    title: z.string().min(1).max(200),
    description: z.string().optional(),
    category: z.string().optional().default("Gaming"),
    maxGuests: z.number().optional().default(8),
    autoAcceptGuests: z.boolean().optional().default(false),
    commentsEnabled: z.boolean().optional().default(true),
    giftsEnabled: z.boolean().optional().default(true),
    likesEnabled: z.boolean().optional().default(true),
    guestRequestsEnabled: z.boolean().optional().default(true),
    layout: z.string().optional().default("SIDE_BY_SIDE")
});

async function createSession(req, res, next) {
    try {
        const data = createSessionSchema.parse(req.body);

        const uniqueRoomName = `room_${Date.now()}_${Math.random().toString(36).substring(2, 9)}`;

        const liveSession = await prisma.liveSession.create({
            data: {
                hostId: req.user.userId,
                title: data.title,
                description: data.description || null,
                category: data.category || "Gaming",
                status: "SCHEDULED",
                maxGuests: 8,
                autoAcceptGuests: data.autoAcceptGuests,
                commentsEnabled: data.commentsEnabled,
                giftsEnabled: data.giftsEnabled,
                likesEnabled: data.likesEnabled,
                guestRequestsEnabled: data.guestRequestsEnabled,
                layout: data.layout,
                roomName: uniqueRoomName
            },
            include: {
                host: {
                    select: { id: true, username: true, displayName: true, avatarUrl: true, isVerified: true }
                }
            }
        });

        await prisma.liveParticipant.create({
            data: {
                liveSessionId: liveSession.id,
                userId: req.user.userId,
                role: "HOST",
                status: "CONNECTED",
                canPublish: true,
                canSubscribe: true
            }
        });

        const liveKitToken = createLiveKitToken(
            uniqueRoomName,
            req.user.userId,
            req.user.username || "Host",
            true
        );

        res.status(201).json({
            success: true,
            message: "Live session created with unique LiveKit room",
            liveId: liveSession.id,
            liveSession,
            liveKitToken,
            wsUrl: process.env.LIVEKIT_WS_URL || "ws://localhost:7880"
        });
    } catch (error) {
        next(error);
    }
}

async function startSession(req, res, next) {
    try {
        const liveId = req.params.liveId || req.params.id;

        const session = await prisma.liveSession.findUnique({
            where: { id: liveId },
            include: { host: true }
        });

        if (!session) {
            return res.status(404).json({
                success: false,
                error: { code: "LIVE_NOT_FOUND", message: "Live session not found" }
            });
        }

        if (session.hostId !== req.user.userId && req.user.role !== "MODERATOR" && req.user.role !== "ADMIN") {
            return res.status(403).json({
                success: false,
                error: { code: "NOT_HOST", message: "Unauthorized: Only the host can start the live stream" }
            });
        }

        if (session.status === "LIVE") {
            return res.status(400).json({
                success: false,
                error: { code: "LIVE_NOT_ACTIVE", message: "Live stream is already active" }
            });
        }

        const roomName = session.roomName || `room_${Date.now()}_${Math.random().toString(36).substring(2, 9)}`;

        const updated = await prisma.liveSession.update({
            where: { id: liveId },
            data: {
                status: "LIVE",
                startedAt: new Date(),
                peakViewers: 1,
                totalViewers: 1,
                roomName
            },
            include: {
                host: {
                    select: { id: true, username: true, displayName: true, avatarUrl: true, isVerified: true }
                }
            }
        });

        const hostToken = createLiveKitToken(
            roomName,
            req.user.userId,
            updated.host.username || "Host",
            true
        );

        emitToLive(liveId, "live:started", { liveId, liveSession: updated });

        res.json({
            success: true,
            liveId: updated.id,
            message: "Live session is now LIVE",
            liveSession: updated,
            hostToken,
            wsUrl: process.env.LIVEKIT_WS_URL || "ws://localhost:7880"
        });
    } catch (error) {
        next(error);
    }
}

async function endSession(req, res, next) {
    try {
        const liveId = req.params.liveId || req.params.id;

        const session = await prisma.liveSession.findUnique({
            where: { id: liveId }
        });

        if (!session) {
            return res.status(404).json({
                success: false,
                error: { code: "LIVE_NOT_FOUND", message: "Live session not found" }
            });
        }

        if (session.hostId !== req.user.userId && req.user.role !== "MODERATOR" && req.user.role !== "ADMIN") {
            return res.status(403).json({
                success: false,
                error: { code: "NOT_HOST", message: "Unauthorized: Only the host or moderator can end the live stream" }
            });
        }

        await prisma.liveSession.update({
            where: { id: liveId },
            data: { status: "ENDING" }
        });

        const endedSession = await prisma.liveSession.update({
            where: { id: liveId },
            data: {
                status: "ENDED",
                endedAt: new Date()
            },
            include: {
                host: {
                    select: { id: true, username: true, displayName: true, avatarUrl: true, isVerified: true }
                }
            }
        });

        await prisma.liveParticipant.updateMany({
            where: { liveSessionId: liveId, status: "CONNECTED" },
            data: { status: "LEFT", leftAt: new Date(), canPublish: false }
        });

        emitToLive(liveId, "live:ended", { liveId, liveSession: endedSession });
        emitToLive(liveId, "live:moderation_action", { liveId, action: "END_LIVE" });

        res.json({
            success: true,
            liveId: endedSession.id,
            message: "Live session ended successfully",
            liveSession: endedSession
        });
    } catch (error) {
        next(error);
    }
}

async function getLiveSessions(req, res, next) {
    try {
        const category = req.query.category;
        const whereClause = { status: "LIVE" };

        if (category && category !== "All") {
            whereClause.category = category;
        }

        let liveSessions = await prisma.liveSession.findMany({
            where: whereClause,
            orderBy: { createdAt: "desc" },
            include: {
                host: {
                    select: { id: true, username: true, displayName: true, avatarUrl: true, isVerified: true }
                }
            }
        });

        if (liveSessions.length === 0) {
            const firstUser = await prisma.user.findFirst();
            if (firstUser) {
                const defaultSession = await prisma.liveSession.create({
                    data: {
                        hostId: firstUser.id,
                        title: "Fugen Live Studio 🔥",
                        category: "Gaming",
                        status: "LIVE",
                        startedAt: new Date(),
                        totalViewers: 1420,
                        roomName: `room_default_${Date.now()}`
                    },
                    include: {
                        host: {
                            select: { id: true, username: true, displayName: true, avatarUrl: true, isVerified: true }
                        }
                    }
                });
                liveSessions = [defaultSession];
            }
        }

        res.json({
            success: true,
            liveSessions
        });
    } catch (error) {
        next(error);
    }
}

async function getLiveSessionById(req, res, next) {
    try {
        const liveId = req.params.liveId || req.params.id;

        const liveSession = await prisma.liveSession.findUnique({
            where: { id: liveId },
            include: {
                host: {
                    select: { id: true, username: true, displayName: true, avatarUrl: true, isVerified: true }
                },
                participants: {
                    include: {
                        user: { select: { id: true, username: true, displayName: true, avatarUrl: true, isVerified: true } }
                    }
                }
            }
        });

        if (!liveSession) {
            return res.status(404).json({
                success: false,
                error: { code: "LIVE_NOT_FOUND", message: "Live session not found" }
            });
        }

        res.json({
            success: true,
            liveId: liveSession.id,
            liveSession
        });
    } catch (error) {
        next(error);
    }
}

async function getLiveParticipants(req, res, next) {
    try {
        const liveId = req.params.liveId || req.params.id;

        const session = await prisma.liveSession.findUnique({
            where: { id: liveId }
        });

        if (!session) {
            return res.status(404).json({
                success: false,
                error: { code: "LIVE_NOT_FOUND", message: "Live session not found" }
            });
        }

        const participants = await prisma.liveParticipant.findMany({
            where: { liveSessionId: liveId },
            include: {
                user: {
                    select: { id: true, username: true, displayName: true, avatarUrl: true, isVerified: true }
                }
            }
        });

        res.json({
            success: true,
            liveId,
            participants
        });
    } catch (error) {
        next(error);
    }
}

async function getLiveToken(req, res, next) {
    try {
        const sessionId = req.params.liveId || req.params.id;
        const session = await prisma.liveSession.findUnique({
            where: { id: sessionId },
            include: { host: true }
        });

        if (!session) {
            return res.status(404).json({
                success: false,
                error: { code: "LIVE_NOT_FOUND", message: "Live session not found" }
            });
        }

        let role = "VIEWER";
        let canPublish = false;
        let canSubscribe = true;

        if (session.hostId === req.user.userId) {
            role = "HOST";
            canPublish = true;
            canSubscribe = true;
        } else {
            const participant = await prisma.liveParticipant.findUnique({
                where: {
                    liveSessionId_userId: {
                        liveSessionId: sessionId,
                        userId: req.user.userId
                    }
                }
            });

            if (participant) {
                if (participant.status === "REJECTED" || participant.status === "REMOVED") {
                    canPublish = false;
                    role = "VIEWER";
                } else if (participant.role === "GUEST" && participant.status === "CONNECTED") {
                    const currentPublishers = await prisma.liveParticipant.count({
                        where: { liveSessionId: sessionId, status: "CONNECTED", canPublish: true }
                    });

                    if (currentPublishers < 9) {
                        role = "GUEST";
                        canPublish = true;
                    } else {
                        role = "VIEWER";
                        canPublish = false;
                    }
                }
            }
        }

        await prisma.liveParticipant.upsert({
            where: {
                liveSessionId_userId: {
                    liveSessionId: sessionId,
                    userId: req.user.userId
                }
            },
            create: {
                liveSessionId: sessionId,
                userId: req.user.userId,
                role,
                status: "CONNECTED",
                canPublish,
                canSubscribe
            },
            update: {
                role,
                canPublish,
                canSubscribe,
                status: "CONNECTED"
            }
        });

        const token = createLiveKitToken(
            session.roomName || sessionId,
            req.user.userId,
            req.user.username || "User",
            canPublish
        );

        res.json({
            success: true,
            role,
            canPublish,
            canSubscribe,
            token,
            wsUrl: process.env.LIVEKIT_WS_URL || "ws://localhost:7880"
        });
    } catch (error) {
        next(error);
    }
}

async function createGuestRequest(req, res, next) {
    try {
        const sessionId = req.params.liveId || req.params.id;
        const session = await prisma.liveSession.findUnique({
            where: { id: sessionId }
        });

        if (!session || session.status !== "LIVE") {
            return res.status(400).json({
                success: false,
                error: { code: "LIVE_NOT_ACTIVE", message: "Live stream is not active" }
            });
        }

        if (!session.guestRequestsEnabled) {
            return res.status(400).json({
                success: false,
                error: { code: "GUEST_REQUEST_DISABLED", message: "Guest requests are disabled for this live stream" }
            });
        }

        const activePublishers = await prisma.liveParticipant.count({
            where: { liveSessionId: sessionId, status: "CONNECTED", canPublish: true }
        });

        if (activePublishers >= 9) {
            return res.status(400).json({
                success: false,
                error: { code: "LIVE_STAGE_FULL", message: "The LIVE stage is full." }
            });
        }

        const existingParticipant = await prisma.liveParticipant.findUnique({
            where: {
                liveSessionId_userId: {
                    liveSessionId: sessionId,
                    userId: req.user.userId
                }
            }
        });

        if (existingParticipant && existingParticipant.status === "CONNECTED" && existingParticipant.canPublish) {
            return res.status(400).json({
                success: false,
                error: { code: "ALREADY_PARTICIPANT", message: "User is already publishing on stage" }
            });
        }

        const existingPendingRequest = await prisma.guestRequest.findFirst({
            where: {
                liveSessionId: sessionId,
                requesterId: req.user.userId,
                status: "PENDING"
            }
        });

        if (existingPendingRequest) {
            return res.status(400).json({
                success: false,
                error: { code: "GUEST_REQUEST_PENDING", message: "Guest request already pending" }
            });
        }

        let requestStatus = "PENDING";
        let participantRole = "VIEWER";
        let canPublish = false;

        if (session.autoAcceptGuests) {
            if (activePublishers >= 9) {
                return res.status(400).json({
                    success: false,
                    error: { code: "LIVE_STAGE_FULL", message: "The LIVE stage is full." }
                });
            }
            requestStatus = "ACCEPTED";
            participantRole = "GUEST";
            canPublish = true;

            await prisma.liveParticipant.upsert({
                where: {
                    liveSessionId_userId: {
                        liveSessionId: sessionId,
                        userId: req.user.userId
                    }
                },
                create: {
                    liveSessionId: sessionId,
                    userId: req.user.userId,
                    role: participantRole,
                    status: "CONNECTED",
                    canPublish,
                    canSubscribe: true,
                    joinedAt: new Date()
                },
                update: {
                    role: participantRole,
                    status: "CONNECTED",
                    canPublish,
                    joinedAt: new Date()
                }
            });
        }

        const guestRequest = await prisma.guestRequest.create({
            data: {
                liveSessionId: sessionId,
                requesterId: req.user.userId,
                status: requestStatus,
                respondedBy: session.autoAcceptGuests ? "SYSTEM_AUTO_ACCEPT" : null,
                respondedAt: session.autoAcceptGuests ? new Date() : null
            },
            include: {
                requester: { select: { id: true, username: true, displayName: true, avatarUrl: true } }
            }
        });

        let guestToken = null;
        if (session.autoAcceptGuests) {
            guestToken = createLiveKitToken(
                session.roomName || sessionId,
                req.user.userId,
                guestRequest.requester.username || "Guest",
                true
            );
            emitToLive(sessionId, "live:guest_request_accepted", { liveId: sessionId, guestRequest, participant: { userId: req.user.userId, role: "GUEST", status: "CONNECTED", canPublish: true } });
        } else {
            emitToLive(sessionId, "live:guest_request_created", { liveId: sessionId, guestRequest });
        }

        res.status(201).json({
            success: true,
            message: session.autoAcceptGuests ? "Guest request automatically accepted" : "Guest join request submitted",
            guestRequest,
            guestToken,
            wsUrl: process.env.LIVEKIT_WS_URL || "ws://localhost:7880"
        });
    } catch (error) {
        next(error);
    }
}

async function acceptGuestRequest(req, res, next) {
    try {
        const requestId = req.params.requestId || req.params.id;

        const guestRequest = await prisma.guestRequest.findUnique({
            where: { id: requestId },
            include: { liveSession: true, requester: true }
        });

        if (!guestRequest) {
            return res.status(404).json({
                success: false,
                error: { code: "GUEST_REQUEST_NOT_FOUND", message: "Guest request not found" }
            });
        }

        const session = guestRequest.liveSession;
        if (session.hostId !== req.user.userId && req.user.role !== "MODERATOR" && req.user.role !== "ADMIN") {
            return res.status(403).json({
                success: false,
                error: { code: "NOT_HOST", message: "Unauthorized: Only the host can accept guest requests" }
            });
        }

        const activePublishers = await prisma.liveParticipant.count({
            where: { liveSessionId: guestRequest.liveSessionId, status: "CONNECTED", canPublish: true }
        });

        if (activePublishers >= 9) {
            return res.status(400).json({
                success: false,
                error: { code: "LIVE_STAGE_FULL", message: "The LIVE stage is full." }
            });
        }

        const updatedRequest = await prisma.guestRequest.update({
            where: { id: requestId },
            data: {
                status: "ACCEPTED",
                respondedBy: req.user.userId,
                respondedAt: new Date()
            }
        });

        const participant = await prisma.liveParticipant.upsert({
            where: {
                liveSessionId_userId: {
                    liveSessionId: guestRequest.liveSessionId,
                    userId: guestRequest.requesterId
                }
            },
            create: {
                liveSessionId: guestRequest.liveSessionId,
                userId: guestRequest.requesterId,
                role: "GUEST",
                status: "CONNECTED",
                canPublish: true,
                canSubscribe: true,
                joinedAt: new Date()
            },
            update: {
                role: "GUEST",
                status: "CONNECTED",
                canPublish: true,
                joinedAt: new Date()
            },
            include: {
                user: { select: { id: true, username: true, displayName: true, avatarUrl: true } }
            }
        });

        const guestToken = createLiveKitToken(
            session.roomName || guestRequest.liveSessionId,
            guestRequest.requesterId,
            participant.user.username || "Guest",
            true
        );

        emitToLive(guestRequest.liveSessionId, "live:guest_request_accepted", {
            liveId: guestRequest.liveSessionId,
            guestRequest: updatedRequest,
            participant,
            guestToken
        });

        res.json({
            success: true,
            message: "Guest request accepted successfully",
            guestRequest: updatedRequest,
            participant,
            guestToken,
            wsUrl: process.env.LIVEKIT_WS_URL || "ws://localhost:7880"
        });
    } catch (error) {
        next(error);
    }
}

async function rejectGuestRequest(req, res, next) {
    try {
        const requestId = req.params.requestId || req.params.id;

        const guestRequest = await prisma.guestRequest.findUnique({
            where: { id: requestId },
            include: { liveSession: true }
        });

        if (!guestRequest) {
            return res.status(404).json({
                success: false,
                error: { code: "GUEST_REQUEST_NOT_FOUND", message: "Guest request not found" }
            });
        }

        const session = guestRequest.liveSession;
        if (session.hostId !== req.user.userId && req.user.role !== "MODERATOR" && req.user.role !== "ADMIN") {
            return res.status(403).json({
                success: false,
                error: { code: "NOT_HOST", message: "Unauthorized: Only the host can reject guest requests" }
            });
        }

        const updatedRequest = await prisma.guestRequest.update({
            where: { id: requestId },
            data: {
                status: "REJECTED",
                respondedBy: req.user.userId,
                respondedAt: new Date()
            }
        });

        await prisma.liveParticipant.updateMany({
            where: { liveSessionId: guestRequest.liveSessionId, userId: guestRequest.requesterId },
            data: { status: "REJECTED", canPublish: false }
        });

        emitToLive(guestRequest.liveSessionId, "live:guest_request_rejected", {
            liveId: guestRequest.liveSessionId,
            guestRequest: updatedRequest
        });

        res.json({
            success: true,
            message: "Guest request rejected",
            guestRequest: updatedRequest
        });
    } catch (error) {
        next(error);
    }
}

async function removeParticipant(req, res, next) {
    try {
        const liveId = req.params.liveId || req.params.id;
        const participantId = req.params.participantId;

        const session = await prisma.liveSession.findUnique({
            where: { id: liveId }
        });

        if (!session) {
            return res.status(404).json({
                success: false,
                error: { code: "LIVE_NOT_FOUND", message: "Live session not found" }
            });
        }

        if (session.hostId !== req.user.userId && req.user.role !== "MODERATOR" && req.user.role !== "ADMIN") {
            return res.status(403).json({
                success: false,
                error: { code: "NOT_HOST", message: "Unauthorized: Only the host or moderator can remove participants" }
            });
        }

        const participant = await prisma.liveParticipant.findFirst({
            where: {
                OR: [
                    { id: participantId },
                    { userId: participantId }
                ],
                liveSessionId: liveId
            },
            include: { user: true }
        });

        if (!participant) {
            return res.status(404).json({
                success: false,
                error: { code: "PARTICIPANT_NOT_FOUND", message: "Participant not found in this live session" }
            });
        }

        const updatedParticipant = await prisma.liveParticipant.update({
            where: { id: participant.id },
            data: {
                status: "REMOVED",
                canPublish: false,
                removedAt: new Date()
            }
        });

        if (session.roomName) {
            await removeLiveKitParticipant(session.roomName, participant.userId);
        }

        emitToLive(liveId, "live:guest_removed", {
            liveId,
            participantId: participant.id,
            userId: participant.userId
        });
        emitToLive(liveId, "live:moderation_action", {
            liveId,
            action: "REMOVE_PARTICIPANT",
            participantId: participant.id,
            userId: participant.userId
        });

        res.json({
            success: true,
            message: "Participant removed successfully",
            participant: updatedParticipant
        });
    } catch (error) {
        next(error);
    }
}

async function muteParticipant(req, res, next) {
    try {
        const liveId = req.params.liveId || req.params.id;
        const participantId = req.params.participantId;
        const { trackSid, muted = true } = req.body || {};

        const session = await prisma.liveSession.findUnique({
            where: { id: liveId }
        });

        if (!session) {
            return res.status(404).json({
                success: false,
                error: { code: "LIVE_NOT_FOUND", message: "Live session not found" }
            });
        }

        if (session.hostId !== req.user.userId && req.user.role !== "MODERATOR" && req.user.role !== "ADMIN") {
            return res.status(403).json({
                success: false,
                error: { code: "NOT_HOST", message: "Unauthorized: Only the host or moderator can mute participants" }
            });
        }

        const participant = await prisma.liveParticipant.findFirst({
            where: {
                OR: [
                    { id: participantId },
                    { userId: participantId }
                ],
                liveSessionId: liveId
            }
        });

        if (!participant) {
            return res.status(404).json({
                success: false,
                error: { code: "PARTICIPANT_NOT_FOUND", message: "Participant not found" }
            });
        }

        const updatedParticipant = await prisma.liveParticipant.update({
            where: { id: participant.id },
            data: {
                microphoneEnabled: !muted,
                canPublish: muted ? false : participant.canPublish
            }
        });

        if (session.roomName && trackSid) {
            await muteLiveKitParticipantTrack(session.roomName, participant.userId, trackSid, muted);
        }

        emitToLive(liveId, "live:participant_muted", {
            liveId,
            participantId: participant.id,
            userId: participant.userId,
            muted
        });
        emitToLive(liveId, "live:moderation_action", {
            liveId,
            action: "MUTE_PARTICIPANT",
            participantId: participant.id,
            userId: participant.userId,
            muted
        });

        res.json({
            success: true,
            message: muted ? "Participant muted" : "Participant unmuted",
            participant: updatedParticipant
        });
    } catch (error) {
        next(error);
    }
}

async function getLiveSettings(req, res, next) {
    try {
        const liveId = req.params.liveId || req.params.id;

        const session = await prisma.liveSession.findUnique({
            where: { id: liveId },
            select: {
                id: true,
                title: true,
                description: true,
                commentsEnabled: true,
                giftsEnabled: true,
                likesEnabled: true,
                guestRequestsEnabled: true,
                autoAcceptGuests: true,
                maxGuests: true,
                layout: true,
                spotlightParticipantId: true
            }
        });

        if (!session) {
            return res.status(404).json({
                success: false,
                error: { code: "LIVE_NOT_FOUND", message: "Live session not found" }
            });
        }

        res.json({
            success: true,
            liveId: session.id,
            settings: session
        });
    } catch (error) {
        next(error);
    }
}

const updateSettingsSchema = z.object({
    title: z.string().min(1).max(200).optional(),
    description: z.string().optional().nullable(),
    commentsEnabled: z.boolean().optional(),
    giftsEnabled: z.boolean().optional(),
    likesEnabled: z.boolean().optional(),
    guestRequestsEnabled: z.boolean().optional(),
    autoAcceptGuests: z.boolean().optional(),
    maxGuests: z.number().int().min(1).max(8).optional(),
    layout: z.string().optional(),
    spotlightParticipantId: z.string().optional().nullable()
});

async function updateLiveSettings(req, res, next) {
    try {
        const liveId = req.params.liveId || req.params.id;
        const data = updateSettingsSchema.parse(req.body);

        const session = await prisma.liveSession.findUnique({
            where: { id: liveId }
        });

        if (!session) {
            return res.status(404).json({
                success: false,
                error: { code: "LIVE_NOT_FOUND", message: "Live session not found" }
            });
        }

        if (session.hostId !== req.user.userId && req.user.role !== "MODERATOR" && req.user.role !== "ADMIN") {
            return res.status(403).json({
                success: false,
                error: { code: "NOT_HOST", message: "Unauthorized: Only the host or moderator can update live settings" }
            });
        }

        const updatedSession = await prisma.liveSession.update({
            where: { id: liveId },
            data,
            select: {
                id: true,
                title: true,
                description: true,
                commentsEnabled: true,
                giftsEnabled: true,
                likesEnabled: true,
                guestRequestsEnabled: true,
                autoAcceptGuests: true,
                maxGuests: true,
                layout: true,
                spotlightParticipantId: true
            }
        });

        if (data.layout !== undefined) {
            emitToLive(liveId, "live:layout_changed", { liveId, layout: data.layout });
        }

        if (data.spotlightParticipantId !== undefined) {
            emitToLive(liveId, "live:spotlight_changed", { liveId, spotlightParticipantId: data.spotlightParticipantId });
        }

        res.json({
            success: true,
            message: "Live settings updated successfully",
            liveId: updatedSession.id,
            settings: updatedSession
        });
    } catch (error) {
        next(error);
    }
}

async function getLiveComments(req, res, next) {
    try {
        const liveId = req.params.liveId || req.params.id;

        const comments = await prisma.liveComment.findMany({
            where: { liveId, isDeleted: false },
            orderBy: { createdAt: "asc" },
            take: 100,
            include: {
                user: {
                    select: { id: true, username: true, displayName: true, avatarUrl: true, isVerified: true }
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

const createCommentSchema = z.object({
    text: z.string().min(1).max(500)
});

async function createLiveComment(req, res, next) {
    try {
        const liveId = req.params.liveId || req.params.id;
        const { text } = createCommentSchema.parse(req.body);

        const session = await prisma.liveSession.findUnique({
            where: { id: liveId }
        });

        if (!session) {
            return res.status(404).json({
                success: false,
                error: { code: "LIVE_NOT_FOUND", message: "Live session not found" }
            });
        }

        if (!session.commentsEnabled) {
            return res.status(400).json({
                success: false,
                error: { code: "COMMENTS_DISABLED", message: "Comments are disabled for this live stream" }
            });
        }

        const comment = await prisma.liveComment.create({
            data: {
                liveId,
                userId: req.user.userId,
                text
            },
            include: {
                user: {
                    select: { id: true, username: true, displayName: true, avatarUrl: true, isVerified: true }
                }
            }
        });

        await prisma.liveSession.update({
            where: { id: liveId },
            data: { totalComments: { increment: 1 } }
        });

        emitToLive(liveId, "live:comment_created", { liveId, comment });

        res.status(201).json({
            success: true,
            message: "Comment posted",
            comment
        });
    } catch (error) {
        next(error);
    }
}

async function deleteLiveComment(req, res, next) {
    try {
        const liveId = req.params.liveId || req.params.id;
        const commentId = req.params.commentId;

        const comment = await prisma.liveComment.findUnique({
            where: { id: commentId },
            include: { live: true }
        });

        if (!comment || comment.liveId !== liveId) {
            return res.status(404).json({
                success: false,
                error: { code: "COMMENT_NOT_FOUND", message: "Comment not found" }
            });
        }

        if (comment.userId !== req.user.userId && comment.live.hostId !== req.user.userId && req.user.role !== "MODERATOR" && req.user.role !== "ADMIN") {
            return res.status(403).json({
                success: false,
                error: { code: "NOT_HOST", message: "Unauthorized: Only the comment author, host, or moderator can delete comments" }
            });
        }

        await prisma.liveComment.update({
            where: { id: commentId },
            data: { isDeleted: true }
        });

        emitToLive(liveId, "live:comment_deleted", { liveId, commentId });
        emitToLive(liveId, "live:moderation_action", { liveId, action: "DELETE_COMMENT", commentId });

        res.json({
            success: true,
            message: "Comment deleted successfully"
        });
    } catch (error) {
        next(error);
    }
}

async function likeLiveStream(req, res, next) {
    try {
        const liveId = req.params.liveId || req.params.id;

        const session = await prisma.liveSession.findUnique({
            where: { id: liveId }
        });

        if (!session) {
            return res.status(404).json({
                success: false,
                error: { code: "LIVE_NOT_FOUND", message: "Live session not found" }
            });
        }

        if (!session.likesEnabled) {
            return res.status(400).json({
                success: false,
                error: { code: "LIKES_DISABLED", message: "Likes are disabled for this live stream" }
            });
        }

        const updated = await prisma.liveSession.update({
            where: { id: liveId },
            data: { totalLikes: { increment: 1 } },
            select: { id: true, totalLikes: true }
        });

        emitToLive(liveId, "live:like_created", { liveId, totalLikes: updated.totalLikes, userId: req.user.userId });

        res.json({
            success: true,
            totalLikes: updated.totalLikes
        });
    } catch (error) {
        next(error);
    }
}

const sendGiftSchema = z.object({
    giftSlug: z.string().min(1),
    quantity: z.number().int().positive().default(1),
    receiverId: z.string().optional()
});

async function sendLiveGift(req, res, next) {
    try {
        const liveId = req.params.liveId || req.params.id;
        const { giftSlug, quantity, receiverId } = sendGiftSchema.parse(req.body);

        const session = await prisma.liveSession.findUnique({
            where: { id: liveId }
        });

        if (!session) {
            return res.status(404).json({
                success: false,
                error: { code: "LIVE_NOT_FOUND", message: "Live session not found" }
            });
        }

        if (!session.giftsEnabled) {
            return res.status(400).json({
                success: false,
                error: { code: "GIFT_DISABLED", message: "Gifts are disabled for this live stream" }
            });
        }

        const gift = await prisma.gift.findUnique({
            where: { slug: giftSlug }
        });

        if (!gift || !gift.isActive) {
            return res.status(404).json({
                success: false,
                error: { code: "GIFT_NOT_FOUND", message: "Gift not found or inactive" }
            });
        }

        const targetReceiverId = receiverId || session.hostId;
        const totalCoins = gift.coinCost * quantity;
        const diamondsEarned = Math.floor(totalCoins * 0.5);

        let senderWallet = await prisma.wallet.findUnique({
            where: { userId: req.user.userId }
        });

        if (!senderWallet || senderWallet.coinBalance < totalCoins) {
            return res.status(400).json({
                success: false,
                error: { code: "INSUFFICIENT_COINS", message: "Insufficient coin balance" }
            });
        }

        let receiverWallet = await prisma.wallet.findUnique({
            where: { userId: targetReceiverId }
        });
        if (!receiverWallet) {
            receiverWallet = await prisma.wallet.create({
                data: { userId: targetReceiverId, coinBalance: 1000, diamondBalance: 0 }
            });
        }

        await prisma.$transaction([
            prisma.wallet.update({
                where: { userId: req.user.userId },
                data: { coinBalance: { decrement: totalCoins } }
            }),
            prisma.wallet.update({
                where: { userId: targetReceiverId },
                data: { diamondBalance: { increment: diamondsEarned } }
            }),
            prisma.giftTransaction.create({
                data: {
                    liveId,
                    giftId: gift.id,
                    senderId: req.user.userId,
                    receiverId: targetReceiverId,
                    quantity,
                    coinCost: gift.coinCost,
                    totalCoins,
                    diamondsEarned
                }
            }),
            prisma.liveSession.update({
                where: { id: liveId },
                data: { totalDiamonds: { increment: diamondsEarned } }
            })
        ]);

        const giftEventPayload = {
            liveId,
            giftSlug: gift.slug,
            giftName: gift.name,
            iconUrl: gift.iconUrl,
            quantity,
            totalCoins,
            senderId: req.user.userId,
            senderUsername: req.user.username || "User",
            receiverId: targetReceiverId
        };

        emitToLive(liveId, "live:gift_sent", giftEventPayload);
        emitToLive(liveId, "live:wallet_updated", { userId: req.user.userId, coinBalance: senderWallet.coinBalance - totalCoins });

        res.json({
            success: true,
            message: `Successfully sent ${quantity}x ${gift.name}!`,
            totalCoins,
            diamondsEarned
        });
    } catch (error) {
        next(error);
    }
}

// Live Polls
async function createLivePoll(req, res, next) {
    try {
        const liveId = req.params.liveId || req.params.id;
        const { question, options } = req.body || {};

        if (!question || !Array.isArray(options) || options.length < 2) {
            return res.status(400).json({
                success: false,
                error: { code: "VALIDATION_ERROR", message: "Question and at least 2 options required" }
            });
        }

        const poll = await prisma.livePoll.create({
            data: {
                liveId,
                question,
                options: {
                    create: options.map(text => ({ text, votes: 0 }))
                }
            },
            include: { options: true }
        });

        emitToLive(liveId, "live:poll_created", { liveId, poll });

        res.status(201).json({
            success: true,
            poll
        });
    } catch (error) {
        next(error);
    }
}

async function voteLivePoll(req, res, next) {
    try {
        const liveId = req.params.liveId || req.params.id;
        const pollId = req.params.pollId;
        const { optionId } = req.body || {};

        await prisma.livePollOption.update({
            where: { id: optionId },
            data: { votes: { increment: 1 } }
        });

        const poll = await prisma.livePoll.findUnique({
            where: { id: pollId },
            include: { options: true }
        });

        emitToLive(liveId, "live:poll_voted", { liveId, poll });

        res.json({
            success: true,
            poll
        });
    } catch (error) {
        next(error);
    }
}

async function getLivePolls(req, res, next) {
    try {
        const liveId = req.params.liveId || req.params.id;
        const polls = await prisma.livePoll.findMany({
            where: { liveId, active: true },
            include: { options: true }
        });

        res.json({
            success: true,
            polls
        });
    } catch (error) {
        next(error);
    }
}

// Live Questions (Q&A)
async function createLiveQuestion(req, res, next) {
    try {
        const liveId = req.params.liveId || req.params.id;
        const { question } = req.body || {};

        if (!question || !question.trim()) {
            return res.status(400).json({
                success: false,
                error: { code: "VALIDATION_ERROR", message: "Question content required" }
            });
        }

        const q = await prisma.liveQuestion.create({
            data: {
                liveId,
                userId: req.user.userId,
                question: question.trim()
            }
        });

        emitToLive(liveId, "live:question_created", { liveId, question: q });

        res.status(201).json({
            success: true,
            question: q
        });
    } catch (error) {
        next(error);
    }
}

async function getLiveQuestions(req, res, next) {
    try {
        const liveId = req.params.liveId || req.params.id;
        const questions = await prisma.liveQuestion.findMany({
            where: { liveId },
            orderBy: { createdAt: "desc" }
        });

        res.json({
            success: true,
            questions
        });
    } catch (error) {
        next(error);
    }
}

async function answerLiveQuestion(req, res, next) {
    try {
        const liveId = req.params.liveId || req.params.id;
        const questionId = req.params.questionId;

        const q = await prisma.liveQuestion.update({
            where: { id: questionId },
            data: { answered: true, pinned: true }
        });

        emitToLive(liveId, "live:question_answered", { liveId, question: q });

        res.json({
            success: true,
            question: q
        });
    } catch (error) {
        next(error);
    }
}

// Live 1v1 PK Battles
async function startLiveMatch(req, res, next) {
    try {
        const liveId = req.params.liveId || req.params.id;
        const { oppStreamId, durationSec = 180 } = req.body || {};

        const match = await prisma.liveMatch.create({
            data: {
                hostStreamId: liveId,
                oppStreamId: oppStreamId || `opp_${Date.now()}`,
                durationSec
            }
        });

        emitToLive(liveId, "live:match_started", { liveId, match });

        res.status(201).json({
            success: true,
            match
        });
    } catch (error) {
        next(error);
    }
}

async function getLiveMatch(req, res, next) {
    try {
        const liveId = req.params.liveId || req.params.id;
        const match = await prisma.liveMatch.findFirst({
            where: { hostStreamId: liveId, status: "ACTIVE" },
            orderBy: { createdAt: "desc" }
        });

        res.json({
            success: true,
            match
        });
    } catch (error) {
        next(error);
    }
}

// Live Creator Goals
async function setLiveGoal(req, res, next) {
    try {
        const liveId = req.params.liveId || req.params.id;
        const { title, target } = req.body || {};

        const goal = await prisma.liveGoal.create({
            data: {
                liveId,
                title: title || "Stream Diamond Goal 💎",
                target: target || 500
            }
        });

        emitToLive(liveId, "live:goal_updated", { liveId, goal });

        res.status(201).json({
            success: true,
            goal
        });
    } catch (error) {
        next(error);
    }
}

async function getLiveGoal(req, res, next) {
    try {
        const liveId = req.params.liveId || req.params.id;
        const goal = await prisma.liveGoal.findFirst({
            where: { liveId },
            orderBy: { id: "desc" }
        });

        res.json({
            success: true,
            goal
        });
    } catch (error) {
        next(error);
    }
}

module.exports = {
    createSession,
    startSession,
    endSession,
    getLiveSessions,
    getLiveSessionById,
    getLiveParticipants,
    getLiveToken,
    createGuestRequest,
    acceptGuestRequest,
    rejectGuestRequest,
    removeParticipant,
    muteParticipant,
    getLiveSettings,
    updateLiveSettings,
    getLiveComments,
    createLiveComment,
    deleteLiveComment,
    likeLiveStream,
    sendLiveGift,
    createLivePoll,
    voteLivePoll,
    getLivePolls,
    createLiveQuestion,
    getLiveQuestions,
    answerLiveQuestion,
    startLiveMatch,
    getLiveMatch,
    setLiveGoal,
    getLiveGoal
};
