FROM node:22-alpine

WORKDIR /usr/src/app

# Prisma's install hook runs `prisma generate`, so copy the schema and config
# before installing dependencies.
COPY package*.json ./
COPY prisma/schema.prisma ./prisma/schema.prisma
COPY prisma.config.ts ./

# Build-time placeholder for Prisma client generation only. Set the real
# DATABASE_URL in the deployment platform's environment variables.
ARG DATABASE_URL=postgresql://build:build@localhost:5432/build?schema=public
RUN npm ci

COPY . .
RUN npx prisma generate

ENV PORT=5000
EXPOSE 5000

CMD ["sh", "-c", "npx prisma db push && node src/server.js"]
