const prisma = require("../src/lib/prisma");

async function seed() {
    console.log("Seeding Enterprise Gifts and Coin Packages...");

    const gifts = [
        { name: "Rose", slug: "gift_rose", iconUrl: "🌹", coinCost: 1, category: "Love" },
        { name: "Heart", slug: "gift_heart", iconUrl: "❤️", coinCost: 5, category: "Love" },
        { name: "Diamond", slug: "gift_diamond", iconUrl: "💎", coinCost: 100, category: "Premium" },
        { name: "Crown", slug: "gift_crown", iconUrl: "👑", coinCost: 1000, category: "Premium" },
        { name: "Rocket", slug: "gift_rocket", iconUrl: "🚀", coinCost: 5000, category: "Luxury" },
        { name: "Universe", slug: "gift_universe", iconUrl: "🌌", coinCost: 20000, category: "Luxury" }
    ];

    for (const g of gifts) {
        const existing = await prisma.gift.findUnique({ where: { slug: g.slug } });
        if (!existing) {
            await prisma.gift.create({ data: g });
        }
    }

    const packages = [
        { name: "100 Coins", coins: 100, priceMinor: 99 },
        { name: "500 Coins", coins: 500, priceMinor: 499 },
        { name: "1,000 Coins", coins: 1000, priceMinor: 999 },
        { name: "5,000 Coins", coins: 5000, priceMinor: 4999 },
        { name: "10,000 Coins", coins: 10000, priceMinor: 9999 }
    ];

    for (const p of packages) {
        const existing = await prisma.coinPackage.findFirst({ where: { coins: p.coins } });
        if (!existing) {
            await prisma.coinPackage.create({ data: p });
        }
    }

    console.log("Enterprise Gifts and Coin Packages seeded successfully!");
    process.exit(0);
}

seed().catch(err => {
    console.error(err);
    process.exit(1);
});
