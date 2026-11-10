const { PrismaClient } = require("@prisma/client");

const prisma = new PrismaClient({
  datasources: {
    db: {
      url: process.env.DB_URL || process.env.DATABASE_URL,
    },
  },
});

module.exports = prisma;
