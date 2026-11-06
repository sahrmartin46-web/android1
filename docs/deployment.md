# FUGEN Deployment Guide

## Sevalla Cloud Deployment

- **Sevalla App ID**: `4fed556d-25ba-4b06-8b91-6a9e2917d7fe`
- **Sevalla Public URL**: `https://proud-gray-gecko-0tqwb.ext.sevalla.app`
- **Storage / Backup Bucket**: `proud-gray-gecko-backups-vkgy5/` (Cloudflare R2)

## Production Deployment Checklist

1. **Infrastructure (Docker)**:
   - Deploy PostgreSQL container with persistent volume.
   - Deploy Redis for real-time pub/sub caching.
   - Deploy LiveKit SFU media server with SSL/WSS enabled (`wss://live.fugen.app`).

2. **Backend API (`apps/server`)**:
   - Build TypeScript server: `npm run build`
   - Run Prisma migrations: `npx prisma migrate deploy`
   - Start process manager (PM2 / Docker): `pm2 start dist/server.js`

3. **Mobile Client (`apps/mobile`)**:
   - Configure production API base URL (`https://api.fugen.app/api/`).
   - Configure LiveKit WebSocket URL (`wss://live.fugen.app`).
   - Compile release builds for Android (`flutter build apk --release`) and iOS (`flutter build ipa --release`).
