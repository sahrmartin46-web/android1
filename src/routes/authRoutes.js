const express = require("express");

const {
    register,
    login,
    firebaseAuth,
    me
} = require("../controllers/authController");

const authMiddleware = require("../middleware/authMiddleware");

const router = express.Router();

router.post("/register", register);
router.post("/login", login);
router.post("/firebase", firebaseAuth);
router.post("/refresh", authMiddleware, me);
router.post("/logout", authMiddleware, (req, res) => {
    res.json({ success: true, message: "Logged out successfully" });
});
router.get("/me", authMiddleware, me);

module.exports = router;
