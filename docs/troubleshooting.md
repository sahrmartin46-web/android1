# Sevalla Deployment Troubleshooting Guide

## PostgreSQL connection

The Prisma schema reads the PostgreSQL connection from `DB_URL`. In the hosting platform, attach the managed PostgreSQL database to the application using its **internal** connection. The platform should inject `DB_URL` into the app environment.

The configured database is PostgreSQL 17 in Ashburn (`us-ashburn-1`). Its internal host is `proud-gray-gecko-0tqwb-postgresql.proud-gray-gecko-0tqwb.svc.cluster.local`, port `5432`, database `proud-gray-gecko`, and user `nimble-black-seal`. Use the platform-provided internal connection string; the password is secret and must not be committed to this repository.

## Required application environment variables

- `DB_URL`: injected by the platform's internal PostgreSQL connection.
- `JWT_SECRET`: set a strong random secret in the hosting platform.
- `PORT`: use the port assigned by the hosting platform, or `5000` when configured there.

## Container startup

The Docker image generates the Prisma client during build. At runtime it runs `npx prisma db push` and then starts the Node.js server. Ensure the app has the database attached and `DB_URL` available before deployment.
