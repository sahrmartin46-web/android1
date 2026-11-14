const express = require("express");
const router = express.Router();
const os = require("os");
const authMiddleware = require("../middleware/authMiddleware");

router.get("/status", authMiddleware, (req, res) => {
    res.json({
        success: true,
        ssh: {
            status: "SECURE_CONNECTED",
            protocol: "SSH-2.0-OpenSSH_9.2p1",
            host: "proud-gray-gecko.sevalla.internal",
            port: 22,
            uptimeSeconds: process.uptime(),
            loadAverage: os.loadavg(),
            freeMemory: os.freemem(),
            totalMemory: os.totalmem(),
            platform: os.platform(),
            nodeVersion: process.version
        }
    });
});

module.exports = router;
