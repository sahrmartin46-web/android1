const express = require("express");

const {
    getProfile,
    getProfileById,
    getMyProfile,
    updateProfile
} = require("../controllers/userController");

const authMiddleware = require("../middleware/authMiddleware");

const router = express.Router();

router.get("/me", authMiddleware, getMyProfile);
router.patch("/me", authMiddleware, updateProfile);
router.put("/me", authMiddleware, updateProfile);
router.get("/:userId", authMiddleware, getProfileById);
router.get("/username/:username", getProfile);

module.exports = router;
