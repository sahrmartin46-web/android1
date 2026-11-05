const { z } = require("zod");
const prisma = require("../lib/prisma");

async function getCoinBalance(req, res, next) {
    try {
        let wallet = await prisma.wallet.findUnique({
            where: { userId: req.user.userId },
            select: { coinBalance: true }
        });

        if (!wallet) {
            wallet = await prisma.wallet.create({
                data: { userId: req.user.userId, coinBalance: 1000, diamondBalance: 0 },
                select: { coinBalance: true }
            });
        }

        res.json({
            success: true,
            coinsBalance: wallet.coinBalance
        });
    } catch (error) {
        next(error);
    }
}

const buyCoinsSchema = z.object({
    packageId: z.string().optional(),
    coinsAmount: z.number().positive().default(500)
});

async function buyCoins(req, res, next) {
    try {
        const { coinsAmount } = buyCoinsSchema.parse(req.body);

        let wallet = await prisma.wallet.findUnique({ where: { userId: req.user.userId } });
        if (!wallet) {
            wallet = await prisma.wallet.create({
                data: { userId: req.user.userId, coinBalance: 1000, diamondBalance: 0 }
            });
        }

        const updatedWallet = await prisma.wallet.update({
            where: { userId: req.user.userId },
            data: { coinBalance: { increment: coinsAmount } }
        });

        await prisma.walletTransaction.create({
            data: {
                walletId: wallet.id,
                type: "COIN_PURCHASE",
                status: "COMPLETED",
                amount: coinsAmount,
                currency: "COINS",
                description: `Purchased ${coinsAmount} Coins`
            }
        });

        res.json({
            success: true,
            message: `Added ${coinsAmount} Coins to your wallet!`,
            wallet: updatedWallet,
            coinsBalance: updatedWallet.coinBalance
        });
    } catch (error) {
        next(error);
    }
}

async function getCreatorWallet(req, res, next) {
    try {
        let wallet = await prisma.wallet.findUnique({
            where: { userId: req.user.userId },
            include: { transactions: { orderBy: { createdAt: "desc" }, take: 20 } }
        });

        if (!wallet) {
            wallet = await prisma.wallet.create({
                data: {
                    userId: req.user.userId,
                    coinBalance: 1000,
                    diamondBalance: 0
                },
                include: { transactions: true }
            });
        }

        res.json({
            success: true,
            wallet
        });
    } catch (error) {
        next(error);
    }
}

const withdrawSchema = z.object({
    diamondsAmount: z.number().min(100),
    payoutProvider: z.string().min(1),
    destination: z.string().min(1)
});

async function requestWithdrawal(req, res, next) {
    try {
        const { diamondsAmount, payoutProvider, destination } = withdrawSchema.parse(req.body);

        const wallet = await prisma.wallet.findUnique({ where: { userId: req.user.userId } });

        if (!wallet || wallet.diamondBalance < diamondsAmount) {
            return res.status(400).json({ success: false, message: "Insufficient Diamonds balance" });
        }

        const usdAmount = (diamondsAmount / 200.0); // 200 Diamonds = $1.00 USD

        const updatedWallet = await prisma.wallet.update({
            where: { userId: req.user.userId },
            data: { diamondBalance: { decrement: diamondsAmount } }
        });

        const withdrawal = await prisma.withdrawal.create({
            data: {
                userId: req.user.userId,
                amountUsd: usdAmount,
                diamondsDeducted: diamondsAmount,
                payoutProvider,
                destination,
                status: "COMPLETED"
            }
        });

        await prisma.walletTransaction.create({
            data: {
                walletId: wallet.id,
                type: "WITHDRAWAL",
                status: "COMPLETED",
                amount: diamondsAmount,
                currency: "DIAMONDS",
                description: `Withdrew $${usdAmount.toFixed(2)} USD (${diamondsAmount} Diamonds)`
            }
        });

        res.json({
            success: true,
            message: `Successfully cashed out $${usdAmount.toFixed(2)} USD!`,
            withdrawal,
            wallet: updatedWallet
        });
    } catch (error) {
        next(error);
    }
}

async function getGiftCatalog(req, res, next) {
    try {
        const gifts = await prisma.gift.findMany({
            where: { isActive: true },
            orderBy: { coinCost: "asc" }
        });

        res.json({
            success: true,
            gifts: gifts.map(g => ({
                id: g.id,
                name: g.name,
                slug: g.slug,
                iconUrl: g.iconUrl,
                coinCost: g.coinCost,
                category: g.category
            }))
        });
    } catch (error) {
        next(error);
    }
}

module.exports = {
    getCoinBalance,
    buyCoins,
    getCreatorWallet,
    requestWithdrawal,
    getGiftCatalog
};
