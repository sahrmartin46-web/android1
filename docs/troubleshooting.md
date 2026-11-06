# Sevalla Deployment Troubleshooting Guide

If your deployment on Sevalla failed, check the following common causes and solutions:

## 1. Missing Prisma Client Generation (`prisma generate`)
- **Issue**: Cloud platforms run `npm install` during build, but Prisma Client is not generated unless a `postinstall` script runs.
- **Solution**: We added `"postinstall": "prisma generate"` to `package.json` so Prisma generates the client automatically upon dependency installation.

## 2. Missing Environment Variables in Sevalla Dashboard
Ensure the following environment variables are added in your Sevalla app dashboard under **Environment Variables**:
- `DATABASE_URL` = `file:./dev.db` (or your production PostgreSQL URL)
- `JWT_SECRET` = `<your-secure-jwt-secret>`
- `PORT` = `5000` (or leave default assigned by Sevalla)
- `STORAGE_BASE_URL` = `https://f6d1d15e6f0b37b4b8fcad3c41a7922d.r2.cloudflarestorage.com`
- `BACKUP_BUCKET` = `proud-gray-gecko-backups-vkgy5/`

## 3. Correct Build & Start Commands in Sevalla
- **Build Command**: Leave empty or `npm install`
- **Start Command**: `npm start`
