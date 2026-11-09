FROM node:22-alpine

WORKDIR /usr/src/app

# Prisma's install hook needs the schema and config before dependencies install.
COPY package*.json ./
COPY prisma/schema.prisma ./prisma/schema.prisma
COPY prisma.config.ts ./

# Prisma validates the datasource during generation; this placeholder is build-only.
ARG DB_URL=postgresql://build:build@localhost:5432/build?schema=public
RUN npm ci

COPY . .
RUN npx prisma generate

ENV PORT=5000
EXPOSE 5000

# The hosting platform must inject the real DB_URL at runtime.
CMD ["sh", "-c", "npx prisma db push && node src/server.js"]
