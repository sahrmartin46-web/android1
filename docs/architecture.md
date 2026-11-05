# FUGEN Architecture Specification

Fugen employs a strict two-plane architecture separating the **Metadata & Financial Plane** from the **Real-Time Media Plane**.

```
┌──────────────────────────────────────────────┐
│             Flutter Mobile App               │
└──────────────────────┬───────────────────────┘
                       │ HTTPS REST / WSS
                       ▼
┌──────────────────────────────────────────────┐
│           Fugen Node.js API Gateway           │
│  - JWT Auth      - Wallet Ledger   - Feed    │
│  - RBAC          - Moderation      - Live    │
└──────────────────────┬───────────────────────┘
                       │
             ┌─────────┴─────────┐
             ▼                   ▼
     PostgreSQL + Prisma       Redis Cache
             │
             ▼
      LiveKit WebRTC SFU (Host + 16 Guests)
```

1. **Client Apps**: Never contain API secrets, database credentials, or signing keys.
2. **API Gateway**: Handles authentication, rate limits, ledger transactions, and issues scoped LiveKit JWT tokens.
3. **Media Plane**: LiveKit WebRTC SFU handles video and audio ingestion, transcoding, and distribution for up to 16 participants per room.
