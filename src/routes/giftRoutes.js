const express = require("express");
const router = express.Router();
const { getGiftCatalog } = require("../controllers/coinController");
const authMiddleware = require("../middleware/authMiddleware");

router.get("/", authMiddleware, getGiftCatalog);

module.exports = router;
