require("dotenv").config();

const express = require("express");
const http = require("http");
const { initSocket } = require("./lib/socket");
const cors = require("cors");
const compression = require("compression");
const path = require("path");
const NodeMediaServer = require("node-media-server");

const prisma = require("./lib/prisma");

const authRoutes = require("./routes/authRoutes");
const userRoutes = require("./routes/userRoutes");
const postRoutes = require("./routes/postRoutes");
const commentRoutes = require("./routes/commentRoutes");
const followRoutes = require("./routes/followRoutes");
const uploadRoutes = require("./routes/uploadRoutes");
const liveRoutes = require("./routes/liveRoutes");
const walletRoutes = require("./routes/walletRoutes");
const giftRoutes = require("./routes/giftRoutes");
const paymentRoutes = require("./routes/paymentRoutes");
const webhookRoutes = require("./routes/webhookRoutes");
const monetizationRoutes = require("./routes/monetizationRoutes");
const screenRoutes = require("./routes/screenRoutes");
const coinRoutes = require("./routes/coinRoutes");
const settingsRoutes = require("./routes/settingsRoutes");
const bankRoutes = require("./routes/bankRoutes");

const errorMiddleware = require("./middleware/errorMiddleware");

const app = express();

app.use(cors());
app.use(compression());

app.use(express.json({
    limit: "100mb"
}));

app.use(express.urlencoded({
    extended: true,
    limit: "100mb"
}));

app.use("/uploads", express.static(path.join(__dirname, "../uploads"), {
    maxAge: "7d",
    etag: true
}));

app.get("/", (req, res) => {
    res.json({
        success: true,
        message: "Social Media API v1 is running",
        version: "1.0.0"
    });
});

app.get("/api/health", async (req, res) => {
    try {
        await prisma.$queryRaw`SELECT 1`;

        res.json({
            success: true,
            database: "connected",
            server: "running"
        });

    } catch (error) {
        res.status(500).json({
            success: false,
            database: "disconnected",
            error: error.message
        });
    }
});

app.use("/api/auth", authRoutes);
app.use("/api/users", userRoutes);
app.use("/api/posts", postRoutes);
app.use("/api/comments", commentRoutes);
app.use("/api/follows", followRoutes);
app.use("/api/upload", uploadRoutes);

app.use("/api/live", liveRoutes);
app.use("/api/v1/live", liveRoutes);
app.use("/api/gifts", giftRoutes);
app.use("/api/wallet", walletRoutes);
app.use("/api/v1/wallet", walletRoutes);
app.use("/api/payments", paymentRoutes);
app.use("/api/webhooks", webhookRoutes);
app.use("/api/v1/monetization", monetizationRoutes);
app.use("/api/v1/screens", screenRoutes);
app.use("/api/coins", coinRoutes);
app.use("/api/v1/coins", coinRoutes);
app.use("/api/settings", settingsRoutes);
app.use("/api/v1/settings", settingsRoutes);
app.use("/api/settings/banks", bankRoutes);
app.use("/api/v1/settings/banks", bankRoutes);

app.use((req, res) => {
    res.status(404).json({
        success: false,
        message: "API route not found"
    });
});

app.use(errorMiddleware);

const PORT = process.env.PORT || 5000;

const nmsConfig = {
    rtmp: {
        port: 1935,
        chunk_size: 60000,
        gop_cache: true,
        ping: 30,
        ping_timeout: 60
    },
    http: {
        port: 8000,
        mediaroot: path.join(__dirname, "../media"),
        allow_origin: "*"
    }
};

const nms = new NodeMediaServer(nmsConfig);

async function startServer() {
    try {
        await prisma.$connect();
        console.log("Database connected");

        nms.run();
        console.log("RTMP Live Media Server running on rtmp://localhost:1935 & http://localhost:8000");

        const server = http.createServer(app);
        initSocket(server);

        server.listen(PORT, () => {
            console.log(
                `Server running at http://localhost:${PORT} with compression enabled (Socket.IO active)`
            );
        });

    } catch (error) {
        console.error(
            "Failed to connect to database:",
            error
        );

        process.exit(1);
    }
}

startServer();
