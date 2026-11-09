FROM node:22-alpine

WORKDIR /usr/src/app

# 1) Install dependencies only after Prisma files are available
COPY package*.json ./
COPY prisma ./prisma
COPY prisma.config.ts ./

# Optional build-time placeholder only for generation
ARG DB_URL=postgresql://postgres:postgres@localhost:5432/app?schema=public
ENV DB_URL=$DB_URL

RUN npm install --include=dev

# 2) Copy the rest of the app
COPY . .

# 3) Generate Prisma client explicitly after all files exist
RUN npx prisma generate

ENV PORT=5000
EXPOSE 5000

CMD ["sh", "-c", "npx prisma db push && node src/server.js"]
