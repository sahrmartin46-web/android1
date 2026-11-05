const { Server } = require("socket.io");
const jwt = require("jsonwebtoken");
const { LIVE_SOCKET_EVENTS } = require("../constants/liveEvents");

let io = null;

function initSocket(server) {
    io = new Server(server, {
        cors: {
            origin: "*",
            methods: ["GET", "POST"]
        }
    });

    // Socket.io JWT Authentication Middleware (Section 30)
    io.use((socket, next) => {
        const token = socket.handshake.auth?.token || socket.handshake.headers?.authorization?.split(" ")[1];

        if (!token) {
            return next(new Error("Authentication error: Token missing"));
        }

        try {
            const secret = process.env.JWT_SECRET || "supersecretkey";
            const decoded = jwt.verify(token, secret);
            socket.user = decoded;
            next();
        } catch (err) {
            next(new Error("Authentication error: Invalid token"));
        }
    });

    io.on("connection", (socket) => {
        console.log(`Authenticated socket connected: ${socket.id} (User: ${socket.user.userId})`);

        // Canonical user room: user:{userId}
        socket.join(`user:${socket.user.userId}`);

        // Canonical rooms enforcement (Section 29) - do not accept arbitrary client-defined room names
        socket.on("live.join", (data) => {
            const { liveId, liveSessionId } = data || {};
            const targetLiveId = liveId || liveSessionId;
            if (targetLiveId) {
                const liveRoom = `live:${targetLiveId}`;
                socket.join(liveRoom);
                console.log(`Socket ${socket.id} joined canonical room ${liveRoom}`);
            }
        });

        socket.on("disconnect", () => {
            console.log(`Socket disconnected: ${socket.id}`);
        });
    });

    return io;
}

function getIo() {
    return io;
}

function emitToLive(liveId, eventName, payload) {
    if (io && liveId) {
        io.to(`live:${liveId}`).emit(eventName, payload);
    }
}

function emitToUser(userId, eventName, payload) {
    if (io && userId) {
        io.to(`user:${userId}`).emit(eventName, payload);
    }
}

module.exports = {
    initSocket,
    getIo,
    emitToLive,
    emitToUser
};
