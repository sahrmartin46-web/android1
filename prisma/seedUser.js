const { PrismaClient } = require("@prisma/client");
const bcrypt = require("bcryptjs");

const prisma = new PrismaClient();

async function main() {
    console.log("Seeding default users and creators...");

    const passwordHash = await bcrypt.hash("Password123!", 12);

    // 1. Regular User
    const regularUser = await prisma.user.upsert({
        where: { email: "user@fugen.app" },
        update: {},
        create: {
            username: "fugen_user",
            email: "user@fugen.app",
            displayName: "Fugen Member",
            passwordHash: passwordHash,
            role: "USER",
            isVerified: false
        }
    });

    // 2. Creator User
    const creatorUser = await prisma.user.upsert({
        where: { email: "creator@fugen.app" },
        update: {},
        create: {
            username: "fugen_creator",
            email: "creator@fugen.app",
            displayName: "Fugen Star Creator",
            bio: "Official Fugen Content Creator | Daily Live Streams 🎥",
            passwordHash: passwordHash,
            role: "CREATOR",
            isVerified: true
        }
    });

    // 3. Admin User
    const adminUser = await prisma.user.upsert({
        where: { email: "admin@fugen.app" },
        update: {},
        create: {
            username: "fugen_admin",
            email: "admin@fugen.app",
            displayName: "Fugen Admin",
            passwordHash: passwordHash,
            role: "ADMIN",
            isVerified: true
        }
    });

    console.log("Seeded Users:");
    console.log(" - Regular User:", regularUser.username);
    console.log(" - Creator User:", creatorUser.username);
    console.log(" - Admin User:", adminUser.username);
}

main()
    .catch((e) => {
        console.error(e);
        process.exit(1);
    })
    .finally(async () => {
        await prisma.$disconnect();
    });
