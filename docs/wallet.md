# FUGEN Server-Authoritative Wallet & Gifting Ledger

Financial integrity is enforced via server-authoritative ledger transactions with row-level database locking.

## Double-Spending Prevention
When sending gifts, the server executes an atomic SQL update verifying sufficient balance:

```sql
UPDATE "Wallet"
SET "coinBalance" = "coinBalance" - $1
WHERE "userId" = $2 AND "coinBalance" >= $1;
```

If `affectedRows === 0`, the transaction is aborted.

## Conversion Rates
- **1 Coin** = **2 Creator Diamonds**
- **200 Diamonds** = **$1.00 USD** (Withdrawable via Bank, PayPal, or Mobile Money)
