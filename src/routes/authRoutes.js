const express = require("express");

const {
    register,
    login,
    me
} = require("../controllers/authController");

const authMiddleware = require("../middleware/authMiddleware");

const router = express.Router();

router.post("/register", register);
router.post("/login", login);
router.post("/refresh", authMiddleware, me);
router.post("/logout", authMiddleware, (req, res) => {
    res.json({ success: true, message: "Logged out successfully" });
});
router.get("/me", authMiddleware, me);

module.exports = router;
