const express = require("express");

const {
    createPost,
    getPosts,
    getPost,
    deletePost,
    likePost,
    incrementView
} = require("../controllers/postController");

const authMiddleware = require("../middleware/authMiddleware");

const router = express.Router();

router.get("/", getPosts);

router.get("/:id", getPost);

router.post("/", authMiddleware, createPost);

router.delete("/:id", authMiddleware, deletePost);

router.post("/:id/like", authMiddleware, likePost);

router.post("/:id/view", authMiddleware, incrementView);

module.exports = router;
