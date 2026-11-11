const { PrismaClient } = require("@prisma/client");
const bcrypt = require("bcryptjs");

const prisma = new PrismaClient();

async function main() {
    console.log("Seeding initial user...");

    const passwordHash = await bcrypt.hash("Password123!", 12);

    const user = await prisma.user.upsert({
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

    console.log("Created initial user:", user);
}

main()
    .catch((e) => {
        console.error(e);
        process.exit(1);
    })
    .finally(async () => {
        await prisma.$disconnect();
    });
