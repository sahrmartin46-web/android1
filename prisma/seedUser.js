require("dotenv").config();
const { PrismaClient } = require("@prisma/client");
const bcrypt = require("bcryptjs");

const prisma = new PrismaClient();

async function main() {
    console.log("Seeding users with unique passwords from environment variables...");

    const userEmail = process.env.USER_EMAIL || "user@fugen.app";
    const userUsername = process.env.USER_USERNAME || "fugen_user";
    const userRawPassword = process.env.USER_PASSWORD || "UserPass_9872!";
    const userPasswordHash = await bcrypt.hash(userRawPassword, 12);

    const creatorEmail = process.env.CREATOR_EMAIL || "creator@fugen.app";
    const creatorUsername = process.env.CREATOR_USERNAME || "fugen_creator";
    const creatorRawPassword = process.env.CREATOR_PASSWORD || "CreatorPass_4561!";
    const creatorPasswordHash = await bcrypt.hash(creatorRawPassword, 12);

    const adminEmail = process.env.ADMIN_EMAIL || "admin@fugen.app";
    const adminUsername = process.env.ADMIN_USERNAME || "fugen_admin";
    const adminRawPassword = process.env.ADMIN_PASSWORD || "AdminPass_1234!";
    const adminPasswordHash = await bcrypt.hash(adminRawPassword, 12);

    // 1. Regular User
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

    // 2. Creator User
    const creatorUser = await prisma.user.upsert({
        where: { email: creatorEmail },
        update: {},
        create: {
            username: creatorUsername,
            email: creatorEmail,
            displayName: "Fugen Star Creator",
            bio: "Official Fugen Content Creator | Daily Live Streams 🎥",
            passwordHash: creatorPasswordHash,
            role: "CREATOR",
            isVerified: true
        }
    });

    // 3. Admin User
    const adminUser = await prisma.user.upsert({
        where: { email: adminEmail },
        update: {},
        create: {
            username: adminUsername,
            email: adminEmail,
            displayName: "Fugen Admin",
            passwordHash: adminPasswordHash,
            role: "ADMIN",
            isVerified: true
        }
    });

    console.log("Seeded Users with unique passwords:");
    console.log(" - Regular User:", regularUser.username, `(${regularUser.email})`);
    console.log(" - Creator User:", creatorUser.username, `(${creatorUser.email})`);
    console.log(" - Admin User:", adminUser.username, `(${adminUser.email})`);
}

main()
    .catch((e) => {
        console.error(e);
        process.exit(1);
    })
    .finally(async () => {
        await prisma.$disconnect();
    });
