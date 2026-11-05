# FUGEN LIVE — Production Real-Time Streaming & Financial Architecture

## 1. Production Environment Variables (`.env`)

To run Fugen LIVE in a production environment with zero mock implementations, configure the following environment variables in your Node.js backend:

```env
# Server Configuration
PORT=5000
NODE_ENV=production

# Database (PostgreSQL + Prisma)
DATABASE_URL="postgresql://fugen_user:secure_password@postgres.production.internal:5432/fugen_db?schema=public"

# JWT Authentication
JWT_SECRET="super_secret_jwt_key_change_in_production"
JWT_EXPIRES_IN="7d"

# LiveKit WebRTC SFU Server Configuration
LIVEKIT_API_KEY="your_livekit_api_key"
LIVEKIT_API_SECRET="your_livekit_api_secret"
LIVEKIT_WS_URL="wss://live.fugen.app"

# Redis Cache & Pub/Sub (Active Viewers & Real-Time Events)
REDIS_URL="redis://default:redis_password@redis.production.internal:6379"

# Payment Gateway Webhooks (Stripe / Regional Processors)
STRIPE_SECRET_KEY="sk_live_..."
STRIPE_WEBHOOK_SECRET="whsec_..."
```

---

## 2. Server-Authoritative Financial Ledger

Fugen utilizes an immutable financial ledger for all virtual currency operations. Client applications are strictly prohibited from modifying coin or diamond balances.

### Gift Sending & Double-Spending Prevention Flow
1. **Client Request**: Viewer invokes `POST /api/v1/wallet/send-gift` with `{ liveId, giftSlug, quantity }`.
2. **Server Validation**:
   - Authenticates JWT token in `authMiddleware`.
   - Fetches active `LiveSession` and `Gift` pricing from PostgreSQL.
   - Calculates `totalCoins = gift.coinCost * quantity`.
3. **Atomic Ledger Transaction**:
   - Executes atomic row-locked balance decrement on sender wallet:
     ```sql
     UPDATE "Wallet"
     SET "coinBalance" = "coinBalance" - $1
     WHERE "userId" = $2 AND "coinBalance" >= $1;
     ```
   - Verifies `affectedRows === 1`. If insufficient balance, returns `400 Bad Request`.
   - Credits creator's diamond balance (`diamondBalance += totalCoins * 2`).
   - Records immutable `GiftTransaction`, sender `WalletTransaction`, and receiver `WalletTransaction`.

---

## 3. WebRTC SFU Media Plane (LiveKit Integration)

Fugen separates the metadata/financial plane (PostgreSQL + Node.js) from the real-time media plane (LiveKit WebRTC SFU).

- **Token Generation**: When a host starts a live session or a guest is accepted, the Node.js server generates a cryptographically signed JWT LiveKit access token via `livekit-server-sdk`.
- **Client Connection**: The Flutter/Android client connects to `LIVEKIT_WS_URL` using the authorized token, ensuring zero exposure of LiveKit API secrets on mobile devices.
