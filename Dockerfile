FROM node:18-alpine

WORKDIR /usr/src/app

COPY package*.json ./
RUN npm install

COPY . .

RUN npx prisma generate

ENV PORT=5000
EXPOSE 5000

CMD ["node", "src/server.js"]
