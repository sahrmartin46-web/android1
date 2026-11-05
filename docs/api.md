# FUGEN API Documentation

## Authentication
- `POST /api/auth/register` — Register a new user account.
- `POST /api/auth/login` — Authenticate and receive JWT access token.
- `GET /api/auth/me` — Retrieve current authenticated user profile.

## Feed & Videos
- `GET /api/posts` — Retrieve Feed posts ranked by the Fugen FeedScore algorithm.
- `POST /api/posts` — Create a new video or photo post.
- `DELETE /api/posts/:id` — Delete post (author-only).
- `POST /api/posts/:id/like` — Toggle like on post.

## LIVE Streaming & WebRTC
- `GET /api/v1/live/sessions` — List active live streaming sessions.
- `POST /api/v1/live/sessions` — Prepare a new live session and generate initial room token.
- `POST /api/v1/live/sessions/:id/start` — Transition session status to LIVE.
- `POST /api/v1/live/sessions/:id/end` — End live streaming session.
- `POST /api/v1/live/sessions/:id/token` — Generate scoped LiveKit WebRTC SFU room token.

## Wallet & Gifting Ledger
- `GET /api/v1/wallet` — Retrieve user wallet balances (Coins & Diamonds).
- `POST /api/v1/wallet/buy-coins` — Purchase coin packages.
- `POST /api/v1/wallet/send-gift` — Send virtual gift during live stream with atomic double-spending protection.
- `POST /api/v1/wallet/withdraw` — Request cash withdrawal from diamond balance.

## Monetization Programs
- `GET /api/v1/monetization/programs` — Retrieve all 22 creator monetization programs.
- `POST /api/v1/monetization/enroll` — Enroll in a creator monetization program.
- `GET /api/v1/monetization/artist-tracks` — Retrieve Work with Artists sponsored audio tracks.
