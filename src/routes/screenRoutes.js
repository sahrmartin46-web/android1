const express = require("express");
const router = express.Router();
const prisma = require("../lib/prisma");

// Get Maya Lin Creator Profile Screen Feed & Stats
router.get("/maya-profile", async (req, res) => {
    try {
        let user = await prisma.user.findUnique({
            where: { username: "maya.creates" },
            include: { posts: true, followers: true, following: true }
        });

        if (!user) {
            user = await prisma.user.create({
                data: {
                    username: "maya.creates",
                    email: "maya@fugen.app",
                    passwordHash: "seeded",
                    displayName: "Maya Lin (she/her)",
                    bio: "Visual artist & electronic music producer 🎧 | Tokyo ↔ LA | New single out now 👇",
                    isVerified: true,
                    role: "CREATOR"
                },
                include: { posts: true, followers: true, following: true }
            });
        }

        const posts = await prisma.post.findMany({
            orderBy: { createdAt: "desc" },
            take: 12,
            include: { author: true, likes: true, comments: true }
        });

        res.json({
            success: true,
            profile: {
                username: user.username,
                displayName: user.displayName,
                bio: user.bio,
                isVerified: user.isVerified,
                followingCount: 248,
                followersCount: "3.2M",
                likesCount: "48.9M",
                avatarUrl: user.avatarUrl || "https://images.unsplash.com/photo-1534528741775-53994a69daeb",
                highlights: [
                    { title: "Tokyo Tour", type: "video" },
                    { title: "Studio Gear", type: "video" },
                    { title: "Tutorials", type: "video" },
                    { title: "Drops", type: "video" }
                ],
                videos: posts.map((p, idx) => ({
                    id: p.id,
                    views: `▶ ${(idx + 1) * 450}K`,
                    handle: `@${user.username}`,
                    imageUrl: p.imageUrl || p.videoUrl || "https://images.unsplash.com/photo-1514525253161-7a46d19cd819"
                }))
            }
        });
    } catch (error) {
        res.status(500).json({ success: false, error: error.message });
    }
});

// Get Tokyo Drift Lofi Sound Details Feed & Track Info
router.get("/sound-details", async (req, res) => {
    try {
        const posts = await prisma.post.findMany({
            orderBy: { createdAt: "desc" },
            take: 6,
            include: { author: true }
        });

        res.json({
            success: true,
            sound: {
                title: "Tokyo Drift Lofi",
                mix: "Original Mix",
                duration: "0:45",
                videosCount: "482.6K videos",
                creator: "maya.creates",
                isVerified: true,
                waveform: [12, 18, 24, 20, 14, 22, 16, 10, 16, 22, 18, 12, 20, 14],
                videos: posts.map((p, idx) => ({
                    id: p.id,
                    views: `▶ ${(idx + 2) * 270}K`,
                    handle: p.author ? `@${p.author.username}` : "@ryu.moves",
                    imageUrl: p.imageUrl || "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad"
                }))
            }
        });
    } catch (error) {
        res.status(500).json({ success: false, error: error.message });
    }
});

module.exports = router;
