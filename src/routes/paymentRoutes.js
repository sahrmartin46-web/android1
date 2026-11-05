const express = require("express");
const router = express.Router();
const { buyCoins, checkoutPayment, chargeCreditCard } = require("../controllers/walletController");
const authMiddleware = require("../middleware/authMiddleware");

router.post("/intent", authMiddleware, (req, res) => {
    res.json({ success: true, clientSecret: "pi_test_secret_12345", paymentIntentId: "pi_123" });
});

router.post("/checkout", authMiddleware, checkoutPayment);
router.post("/confirm", authMiddleware, buyCoins);
router.post("/charge-card", authMiddleware, chargeCreditCard);

module.exports = router;
