# FUGEN — Production Real-Time Video & LIVE Streaming Platform

Fugen is a ByteDance-inspired short-form video and real-time interactive LIVE streaming platform built with enterprise-grade server-authoritative architecture.

## Architecture

- **Mobile Client**: Flutter / Dart
- **API Gateway & Backend**: Node.js + TypeScript (Express / Socket.IO)
- **Database & ORM**: PostgreSQL + Prisma ORM
- **Real-Time Media Plane**: LiveKit WebRTC SFU
- **Financial Ledger**: Server-authoritative double-spending protected coin and diamond wallet ledger.

---

## Getting Started

1. Copy `.env.example` to `.env` and configure your credentials.
2. Spin up infrastructure via Docker:
   ```bash
   cd docker
   docker-compose up -d
   ```
3. Run database migrations:
   ```bash
   npx prisma db push
   ```
4. Start the backend server:
   ```bash
   npm run start
   ```
