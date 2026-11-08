FROM node:22-alpine

WORKDIR /usr/src/app

# Prisma's install hook needs the schema available before npm ci runs.
COPY package*.json ./
COPY prisma/schema.prisma ./prisma/schema.prisma
COPY prisma.config.ts ./
RUN npm ci

COPY . .
RUN npx prisma generate

ENV PORT=5000
EXPOSE 5000

# The hosting platform must inject DB_URL at runtime. Do not bake credentials
# into this image or commit them to the repository.
CMD ["sh", "-c", "npx prisma db push && node src/server.js"]
