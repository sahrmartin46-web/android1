const { Server } = require("socket.io");
const jwt = require("jsonwebtoken");

let io = null;

function initSocket(server, options = {}) {
    const { adapter, corsOrigin = "*", ...socketOptions } = options;
    io = new Server(server, {
        ...socketOptions,
        cors: {
            origin: corsOrigin,
            methods: ["GET", "POST"]
        }
    });

    // Optional adapter hook (for example, a Redis adapter) allows room broadcasts
    // to span multiple server processes without coupling this module to a provider.
    if (adapter) io.adapter(adapter);

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
        socket.join(`user:${socket.user.userId}`);

        socket.on("live.join", (data) => {
            const { liveId, liveSessionId } = data || {};
            const targetLiveId = liveId || liveSessionId;
            if (targetLiveId) {
                socket.join(`live:${targetLiveId}`);
            }
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
