const express = require("express");
const router = express.Router();
const { getBankAccounts, addBankAccount, deleteBankAccount } = require("../controllers/bankController");
const authMiddleware = require("../middleware/authMiddleware");

router.get("/", authMiddleware, getBankAccounts);
router.post("/", authMiddleware, addBankAccount);
router.delete("/:id", authMiddleware, deleteBankAccount);

router.get("/bank-accounts", authMiddleware, getBankAccounts);
router.post("/bank-accounts", authMiddleware, addBankAccount);
router.delete("/bank-accounts/:id", authMiddleware, deleteBankAccount);

module.exports = router;
