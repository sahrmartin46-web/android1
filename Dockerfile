FROM node:22-alpine

WORKDIR /usr/src/app

# Install dependencies and make the Prisma schema available before generation.
COPY package*.json ./
COPY prisma ./prisma
RUN npm install --include=dev

# Copy the application source.
COPY . .

# Prisma needs a syntactically valid datasource URL during client generation,
# but generation does not connect to the database. Keep this placeholder scoped
# to this build step so it cannot become the runtime DB_URL.
RUN DB_URL="postgresql://build:build@127.0.0.1:5432/build?schema=public" npm run prisma:generate

ENV PORT=5000
EXPOSE 5000

# DB_URL must be supplied by the hosting platform at runtime.
CMD ["sh", "-c", "npx prisma db push && node src/server.js"]
