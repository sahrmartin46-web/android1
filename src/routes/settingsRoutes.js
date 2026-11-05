const express = require("express");
const authMiddleware = require("../middleware/authMiddleware");
const {
    getSettings,
    updateSettings,
    changePassword,
    switchAccountType,
    getSessions,
    revokeSession,
    getBlockedUsers,
    blockUser,
    unblockUser,
    getKeywordFilters,
    addKeywordFilter,
    deleteKeywordFilter,
    exportUserData,
    deactivateAccount,
    deleteAccount
} = require("../controllers/settingsController");

const router = express.Router();

router.get("/", authMiddleware, getSettings);
router.patch("/", authMiddleware, updateSettings);
router.put("/", authMiddleware, updateSettings);

router.post("/change-password", authMiddleware, changePassword);
router.post("/switch-account-type", authMiddleware, switchAccountType);

router.get("/sessions", authMiddleware, getSessions);
router.delete("/sessions/:sessionId", authMiddleware, revokeSession);

router.get("/blocks", authMiddleware, getBlockedUsers);
router.post("/blocks", authMiddleware, blockUser);
router.delete("/blocks/:targetUserId", authMiddleware, unblockUser);

router.get("/keywords", authMiddleware, getKeywordFilters);
router.post("/keywords", authMiddleware, addKeywordFilter);
router.delete("/keywords/:keyword", authMiddleware, deleteKeywordFilter);

router.get("/export", authMiddleware, exportUserData);
router.post("/deactivate", authMiddleware, deactivateAccount);
router.delete("/account", authMiddleware, deleteAccount);

module.exports = router;
