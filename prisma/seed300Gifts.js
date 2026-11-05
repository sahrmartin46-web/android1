const prisma = require("../src/lib/prisma");

async function seed() {
    console.log("Seeding 300 Unique Gifts...");

    const emojis = ["🌹", "❤️", "💎", "👑", "🚀", "🌌", "🦁", "🚗", "🎂", "🍕", "🎸", "⚽", "🎮", "🔥", "⭐", "⚡", "🍸", "🍔", "🎸", "🏆", "🎁", "🧸", "🎉", "🍉", "☕", "🍩", "🏀", "🎤", "🎧", "💻"];
    const categories = ["Love", "Fun", "Celebration", "Premium", "Luxury", "Sports", "Music", "Food", "Tech", "Travel"];

    const giftsToCreate = [];

    for (let i = 1; i <= 300; i++) {
        const emoji = emojis[(i - 1) % emojis.length];
        const category = categories[(i - 1) % categories.length];
        const coinCost = (i % 10 === 0) ? i * 50 : (i % 5 === 0) ? i * 10 : i;

        giftsToCreate.push({
            name: `Gift #${i}`,
            slug: `gift_item_${i}`,
            iconUrl: emoji,
            coinCost: Math.max(1, coinCost),
            category,
            isActive: true
        });
    }

    for (const g of giftsToCreate) {
        await prisma.gift.upsert({
            where: { slug: g.slug },
            update: { coinCost: g.coinCost, name: g.name },
            create: g
        });
    }

    console.log("Successfully seeded 300 different gifts!");
    process.exit(0);
}

seed().catch(err => {
    console.error(err);
    process.exit(1);
});
