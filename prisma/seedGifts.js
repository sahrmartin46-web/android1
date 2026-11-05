const prisma = require("../src/lib/prisma");

async function seed() {
    console.log("Seeding Gift Catalog...");

    const gifts = [
        { name: "Rose", slug: "rose", category: "Love", iconUrl: "🌹", coinCost: 5 },
        { name: "Heart", slug: "heart", category: "Love", iconUrl: "❤️", coinCost: 10 },
        { name: "Star", slug: "star", category: "Love", iconUrl: "⭐", coinCost: 20 },
        { name: "Fire", slug: "fire", category: "Love", iconUrl: "🔥", coinCost: 50 },
        { name: "Crown", slug: "crown", category: "Premium", iconUrl: "👑", coinCost: 500 },
        { name: "Diamond", slug: "diamond", category: "Luxury", iconUrl: "💎", coinCost: 1000 }
    ];

    for (const g of gifts) {
        await prisma.gift.upsert({
            where: { slug: g.slug },
            update: { name: g.name, coinCost: g.coinCost, iconUrl: g.iconUrl, category: g.category },
            create: g
        });
    }

    console.log("Gift Catalog seeded successfully!");
}

if (require.main === module) {
    seed().then(() => process.exit(0)).catch(err => {
        console.error(err);
        process.exit(1);
    });
}

module.exports = seed;
