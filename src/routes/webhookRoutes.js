const express = require("express");
const router = express.Router();

router.post("/stripe", express.raw({ type: "application/json" }), (req, res) => {
    // Handle verified Stripe webhook events
    res.json({ received: true });
});

module.exports = router;
