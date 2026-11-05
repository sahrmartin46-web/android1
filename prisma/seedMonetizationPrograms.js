const prisma = require("../src/lib/prisma");

async function seed() {
    console.log("Seeding 22 Monetization Programs & Artist Tracks...");

    const programs = [
        { title: "🎵 Work with Artists Program", icon: "🎵", category: "Music Partnerships", payoutRate: "$50 - $500 per Sponsored Song", description: "Get paid by top record labels and artists to feature official music tracks in your videos and live streams.", requirement: "None" },
        { title: "⚡ LIVE Creator Incentive Program", icon: "⚡", category: "Live Payouts", payoutRate: "$50/hr Base + Viewer Bonuses", description: "Official platform hourly compensation and viewer goal bonuses paid directly to live streaming creators.", requirement: "1,000 Followers" },
        { title: "🎁 LIVE Virtual Gifts & Diamonds", icon: "🎁", category: "Live Gifting", payoutRate: "100% Gift Value to Diamonds", description: "Receive virtual gifts from fans during live streams and convert them directly to withdrawable cash.", requirement: "1,000 Followers" },
        { title: "💎 Creator Rewards Program", icon: "💎", category: "Fund", payoutRate: "$1.50 per 1,000 Views", description: "Performance-based payouts for high-quality, long-form video posts with high watch time.", requirement: "10,000 Views / mo" },
        { title: "⚔️ LIVE Match & Battle Bounties", icon: "⚔️", category: "Competitions", payoutRate: "Up to $500 per Match", description: "Earn instant cash bonuses by winning 3-minute PK Live Battles with your followers.", requirement: "5 LIVE Streams" },
        { title: "🛍️ Brand Sponsorships Marketplace", icon: "🛍️", category: "Partnerships", payoutRate: "$200 - $5,000 per Post", description: "Connect directly with top global brands for sponsored posts, product reviews, and shoutouts.", requirement: "5,000 Followers" },
        { title: "🛒 Affiliate Product Store", icon: "🛒", category: "E-Commerce", payoutRate: "Up to 20% Commission", description: "Tag verified products in your video posts and live streams to earn commission on every sale.", requirement: "None" },
        { title: "🔒 Exclusive Subscriber Fan Club", icon: "🔒", category: "Subscriptions", payoutRate: "$4.99 / mo per Fan", description: "Offer paid fan subscriptions for exclusive badges, subscriber-only chat, and private streams.", requirement: "500 Followers" },
        { title: "❓ Paid Q&A & Video Advice", icon: "❓", category: "Services", payoutRate: "$10.00 per Question", description: "Answer fan questions or offer 1-on-1 advice for paid tips and custom video answers.", requirement: "None" },
        { title: "🎟️ Ticketed Virtual Live Events", icon: "🎟️", category: "Events", payoutRate: "$5.00 - $50.00 / Ticket", description: "Host private paid ticketed live performances, workshops, masterclasses, or concerts.", requirement: "Level 2 Creator" },
        { title: "🏆 Monthly Virality Fund Pool", icon: "🏆", category: "Bonus", payoutRate: "$100,000 Monthly Pool", description: "Monthly $100K Pool rewarded automatically to the top 100 most viral creators.", requirement: "Top 100 Ranking" },
        { title: "📢 In-Feed Video Ad Revenue Sharing", icon: "📢", category: "Ad Revenue", payoutRate: "55% Ad Split", description: "Earn 55% of net revenue generated from in-feed ads placed between your video posts.", requirement: "10,000 Views" },
        { title: "💬 Super Chat & Paid Comment Pins", icon: "💬", category: "Engagement", payoutRate: "$2.00 - $100.00 / Pin", description: "Viewers pay to highlight and pin their comments at the top of your live streams.", requirement: "None" },
        { title: "🎨 Profile Custom Merch Store", icon: "🎨", category: "Merchandise", payoutRate: "Direct Sales", description: "Design and sell custom apparel, stickers, and digital downloads directly on your profile.", requirement: "Verified Account" },
        { title: "🥇 Tiered Loyalty Fan Badges", icon: "🥇", category: "Subscriptions", payoutRate: "Recurring Perks", description: "Recurring revenue from Gold, Platinum, and Diamond fan loyalty tiers.", requirement: "None" },
        { title: "🤝 Co-Host Collaboration Bounties", icon: "🤝", category: "Networking", payoutRate: "$50.00 / Appearance", description: "Get paid by other creators for guest appearances and co-hosting joint streams.", requirement: "1,000 Followers" },
        { title: "🎧 Original Sound & Music Royalties", icon: "🎧", category: "Audio", payoutRate: "$0.01 per Video Use", description: "Earn sound royalties whenever other creators use your original audio in their videos.", requirement: "Original Audio" },
        { title: "🎯 Creator Milestone Goals", icon: "🎯", category: "Milestones", payoutRate: "Cash Rewards", description: "Unlock platform milestone bonuses by reaching follower, view, and stream goals.", requirement: "Ongoing Goals" },
        { title: "📜 Masterclasses & Course Sales", icon: "📜", category: "Education", payoutRate: "$19.99 - $199.99", description: "Sell structured video course packages and masterclass series directly to fans.", requirement: "Apply Online" },
        { title: "❤️ Fan Digital Tip Jar", icon: "❤️", category: "Tipping", payoutRate: "100% Tips Kept", description: "Receive direct cash tips from appreciative fans on your profile and video posts.", requirement: "None" },
        { title: "⚡ Early Access Video Subscriptions", icon: "⚡", category: "Perks", payoutRate: "$2.99 / mo", description: "Charge fans for 24-hour early access to your new video releases before public feed.", requirement: "Level 1 Creator" },
        { title: "🔥 Regional Influencer Leaderboard Payouts", icon: "🔥", category: "Competitions", payoutRate: "$10,000 Weekly Pool", description: "Top 10 creators on the weekly regional leaderboard split $10,000 in cash bonuses.", requirement: "Weekly Leaderboard" }
    ];

    for (const p of programs) {
        const existing = await prisma.monetizationProgram.findUnique({ where: { title: p.title } });
        if (!existing) {
            await prisma.monetizationProgram.create({ data: p });
        }
    }

    const artistTracks = [
        { trackTitle: "Summer Vibes Remix 🔥", artistName: "DJ Pulse", payoutPerUse: 50.00 },
        { trackTitle: "Afrobeat Midnight Party 💃", artistName: "StarBoy Beats", payoutPerUse: 100.00 },
        { trackTitle: "Cyberpunk Synthwave ⚡", artistName: "Neo Sound", payoutPerUse: 75.00 }
    ];

    for (const t of artistTracks) {
        const existing = await prisma.artistTrack.findFirst({ where: { trackTitle: t.trackTitle } });
        if (!existing) {
            await prisma.artistTrack.create({ data: t });
        }
    }

    console.log("Seeded 22 Programs and Artist Tracks successfully!");
    process.exit(0);
}

seed().catch(err => {
    console.error(err);
    process.exit(1);
});
