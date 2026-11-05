# FUGEN Real-Time Events (Socket.IO)

Fugen uses Socket.IO for real-time application events during LIVE streaming sessions.

## Event Contracts

### 1. Client → Server Events
- `live.join`: Join a live session room.
  - Payload: `{ liveSessionId, userId }`
- `live.comment`: Send a real-time chat comment.
  - Payload: `{ liveSessionId, userId, text }`
- `live.like`: Send a heart reaction / like.
  - Payload: `{ liveSessionId, userId }`
- `live.gift`: Send a virtual gift.
  - Payload: `{ liveSessionId, giftSlug, quantity }`
- `live.guest.request`: Request to join stage as guest.
  - Payload: `{ liveSessionId, userId }`

### 2. Server → Client Events
- `live.comment.new`: Broadcast new comment to room participants.
- `live.like.new`: Broadcast like counter increment.
- `live.gift.new`: Broadcast virtual gift animation banner.
- `live.guest.requested`: Emit join request notification to host.
- `live.session.ended`: Notify room participants that the live session has ended.
