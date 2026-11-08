FROM node:22-alpine

WORKDIR /usr/src/app

# Prisma's postinstall hook runs `prisma generate`, so make its schema and
# config available before installing dependencies.
COPY package*.json ./
COPY prisma/schema.prisma ./prisma/schema.prisma
COPY prisma.config.ts ./

# Prisma config reads DATABASE_URL while generating the client. This build-time
# default is only for generation; set DATABASE_URL in the deployment environment
# to the actual database URL.
ARG DATABASE_URL=file:./prisma/dev.db
RUN npm ci

COPY . .

# Run explicitly as well so builds remain correct if the install hook changes.
RUN npx prisma generate

ENV PORT=5000
EXPOSE 5000

CMD ["sh", "-c", "npx prisma db push && node src/server.js"]
