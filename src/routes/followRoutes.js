const express = require("express");

const {
    followUser,
    getFollowers,
    getFollowing
} = require("../controllers/followController");

const authMiddleware = require("../middleware/authMiddleware");

const router = express.Router();

router.post("/:userId", authMiddleware, followUser);

router.get("/:userId/followers", getFollowers);

router.get("/:userId/following", getFollowing);

module.exports = router;
