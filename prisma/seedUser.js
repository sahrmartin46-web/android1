require("dotenv").config();
const { PrismaClient } = require("@prisma/client");
const bcrypt = require("bcryptjs");

const prisma = new PrismaClient();

async function main() {
    console.log("Seeding standard user...");

    const userEmail = process.env.USER_EMAIL || "user@fugen.app";
    const userUsername = process.env.USER_USERNAME || "fugen_user";
    const userRawPassword = process.env.USER_PASSWORD || "UserPass_9872!";
    const userPasswordHash = await bcrypt.hash(userRawPassword, 12);

    // Standard User
    const regularUser = await prisma.user.upsert({
        where: { email: userEmail },
        update: {},
        create: {
            username: userUsername,
            email: userEmail,
            displayName: "Fugen Member",
            passwordHash: userPasswordHash,
            role: "USER",
            isVerified: false
        }
    });

    console.log("Seeded Standard User:", regularUser.username, `(${regularUser.email})`);
}

main()
    .catch((e) => {
        console.error(e);
        process.exit(1);
    })
    .finally(async () => {
        await prisma.$disconnect();
    });
