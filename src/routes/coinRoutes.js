const express = require("express");
const router = express.Router();
const { getCoinBalance, buyCoins, getCreatorWallet, requestWithdrawal, getGiftCatalog } = require("../controllers/coinController");
const authMiddleware = require("../middleware/authMiddleware");

router.get("/balance", authMiddleware, getCoinBalance);
router.post("/buy", authMiddleware, buyCoins);
router.get("/wallet", authMiddleware, getCreatorWallet);
router.post("/withdraw", authMiddleware, requestWithdrawal);
router.get("/gifts", authMiddleware, getGiftCatalog);

module.exports = router;
