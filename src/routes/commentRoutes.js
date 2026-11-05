const express = require("express");

const {
    createComment,
    getComments,
    deleteComment
} = require("../controllers/commentController");

const authMiddleware = require("../middleware/authMiddleware");

const router = express.Router();

router.get("/post/:postId", getComments);

router.post("/post/:postId", authMiddleware, createComment);

router.delete("/:commentId", authMiddleware, deleteComment);

module.exports = router;
