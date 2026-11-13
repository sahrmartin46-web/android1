const { PrismaClient } = require("@prisma/client");

const connectionString = process.env.DB_URL || process.env.DATABASE_URL;

if (!connectionString) {
    console.warn("WARNING: DB_URL or DATABASE_URL is not set in environment variables!");
}

const prisma = new PrismaClient({
  datasources: {
    db: {
      url: connectionString || "postgresql://postgres:postgres@localhost:5432/fallback?schema=public",
    },
  },
});

module.exports = prisma;
