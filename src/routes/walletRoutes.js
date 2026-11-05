const express = require("express");
const router = express.Router();
const { getWallet, getWalletTransactions, buyCoins, checkoutPayment, sendGift, requestWithdrawal } = require("../controllers/walletController");
const authMiddleware = require("../middleware/authMiddleware");

router.get("/", authMiddleware, getWallet);
router.get("/transactions", authMiddleware, getWalletTransactions);
router.post("/buy-coins", authMiddleware, buyCoins);
router.post("/checkout", authMiddleware, checkoutPayment);
router.post("/send-gift", authMiddleware, sendGift);
router.post("/withdraw", authMiddleware, requestWithdrawal);

module.exports = router;
