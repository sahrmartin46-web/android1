const express = require("express");
const router = express.Router();
const { getMonetizationPrograms, enrollInProgram, getArtistTracks } = require("../controllers/monetizationController");
const authMiddleware = require("../middleware/authMiddleware");

router.get("/programs", authMiddleware, getMonetizationPrograms);
router.post("/enroll", authMiddleware, enrollInProgram);
router.get("/artist-tracks", authMiddleware, getArtistTracks);

module.exports = router;
