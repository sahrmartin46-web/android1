const { z } = require("zod");
const prisma = require("../lib/prisma");
const { emitToLive } = require("../lib/socket");

async function getWallet(req, res, next) {
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

async function getWalletTransactions(req, res, next) {
    try {
        let wallet = await prisma.wallet.findUnique({
            where: { userId: req.user.userId },
            include: { transactions: { orderBy: { createdAt: "desc" }, take: 50 } }
        });

        if (!wallet) {
            wallet = await prisma.wallet.create({
                data: { userId: req.user.userId, coinBalance: 1000, diamondBalance: 0 },
                include: { transactions: true }
            });
        }

        res.json({
            success: true,
            transactions: wallet.transactions
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
            wallet: updatedWallet
        });
    } catch (error) {
        next(error);
    }
}

async function checkoutPayment(req, res, next) {
    try {
        const { amount = 500 } = req.body || {};
        res.json({
            success: true,
            checkoutUrl: "https://checkout.stripe.com/pay/cs_test_123",
            sessionId: "cs_test_123",
            amount
        });
    } catch (error) {
        next(error);
    }
}

const sendGiftSchema = z.object({
    liveId: z.string().min(1),
    giftSlug: z.string().min(1),
    quantity: z.number().positive().default(1)
});

async function sendGift(req, res, next) {
    try {
        const { liveId, giftSlug, quantity } = sendGiftSchema.parse(req.body);

        const gift = await prisma.gift.findUnique({ where: { slug: giftSlug } });
        if (!gift || !gift.isActive) {
            return res.status(404).json({ success: false, message: "Gift not found or inactive" });
        }

        const totalCoins = gift.coinCost * quantity;

        let senderWallet = await prisma.wallet.findUnique({ where: { userId: req.user.userId } });
        if (!senderWallet) {
            senderWallet = await prisma.wallet.create({
                data: { userId: req.user.userId, coinBalance: 1000, diamondBalance: 0 }
            });
        }

        if (senderWallet.coinBalance < totalCoins) {
            return res.status(400).json({
                success: false,
                reason: "INSUFFICIENT_COINS",
                message: "Insufficient Coins balance. Please buy more coins!"
            });
        }

        const liveSession = await prisma.liveSession.findUnique({
            where: { id: liveId },
            include: { host: true }
        });

        if (!liveSession) {
            return res.status(404).json({ success: false, message: "Live session not found" });
        }

        const diamondsEarned = totalCoins * 2; // 1 Coin = 2 Creator Diamonds

        // Atomic transaction with rollback protection
        const [updatedSenderWallet, receiverWallet, giftTx] = await prisma.$transaction(async (tx) => {
            const updatedSender = await tx.wallet.update({
                where: { userId: req.user.userId },
                data: { coinBalance: { decrement: totalCoins } }
            });

            const receiver = await tx.wallet.upsert({
                where: { userId: liveSession.hostId },
                create: { userId: liveSession.hostId, coinBalance: 1000, diamondBalance: diamondsEarned },
                update: { diamondBalance: { increment: diamondsEarned } }
            });

            const txGift = await tx.giftTransaction.create({
                data: {
                    liveId,
                    giftId: gift.id,
                    senderId: req.user.userId,
                    receiverId: liveSession.hostId,
                    quantity,
                    coinCost: gift.coinCost,
                    totalCoins,
                    diamondsEarned
                },
                include: {
                    sender: { select: { id: true, username: true, displayName: true, avatarUrl: true } },
                    gift: true
                }
            });

            await tx.liveSession.update({
                where: { id: liveId },
                data: { totalDiamonds: { increment: diamondsEarned } }
            });

            await tx.walletTransaction.create({
                data: {
                    walletId: senderWallet.id,
                    type: "GIFT_SENT",
                    status: "COMPLETED",
                    amount: totalCoins,
                    currency: "COINS",
                    description: `Sent ${quantity}x ${gift.name} ${gift.iconUrl}`
                }
            });

            await tx.walletTransaction.create({
                data: {
                    walletId: receiver.id,
                    type: "CREATOR_REWARD",
                    status: "COMPLETED",
                    amount: diamondsEarned,
                    currency: "DIAMONDS",
                    description: `Earned ${diamondsEarned} Diamonds from ${gift.name}`
                }
            });

            return [updatedSender, receiver, txGift];
        });

        const giftEventPayload = {
            liveId,
            giftSlug: gift.slug,
            giftName: gift.name,
            iconUrl: gift.iconUrl,
            quantity,
            totalCoins,
            senderId: req.user.userId,
            senderUsername: req.user.username || "User",
            receiverId: liveSession.hostId
        };

        emitToLive(liveId, "live:gift_sent", giftEventPayload);
        emitToLive(liveId, "live:wallet_updated", { userId: req.user.userId, coinBalance: updatedSenderWallet.coinBalance });

        res.json({
            success: true,
            message: `Sent ${quantity}x ${gift.name} ${gift.iconUrl}!`,
            giftTx,
            wallet: updatedSenderWallet
        });
    } catch (error) {
        next(error);
    }
}

const withdrawalSchema = z.object({
    diamondsAmount: z.number().min(100),
    payoutProvider: z.string().min(1),
    destination: z.string().min(1)
});

async function requestWithdrawal(req, res, next) {
    try {
        const { diamondsAmount, payoutProvider, destination } = withdrawalSchema.parse(req.body);

        const wallet = await prisma.wallet.findUnique({ where: { userId: req.user.userId } });

        if (!wallet || wallet.diamondBalance < diamondsAmount) {
            return res.status(400).json({ success: false, message: "Insufficient Diamonds balance" });
        }

        const usdAmount = (diamondsAmount / 200.0);

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

const chargeCardSchema = z.object({
    cardBrand: z.string().min(2), // VISA, MASTERCARD, AMEX, DISCOVER
    cardNumber: z.string().min(13),
    expiryMonth: z.string().min(2),
    expiryYear: z.string().min(2),
    cvv: z.string().min(3),
    cardholderName: z.string().min(2),
    coinsAmount: z.number().positive().default(500)
});

async function chargeCreditCard(req, res, next) {
    try {
        const data = chargeCardSchema.parse(req.body);

        let wallet = await prisma.wallet.findUnique({ where: { userId: req.user.userId } });
        if (!wallet) {
            wallet = await prisma.wallet.create({
                data: { userId: req.user.userId, coinBalance: 1000, diamondBalance: 0 }
            });
        }

        const updatedWallet = await prisma.wallet.update({
            where: { userId: req.user.userId },
            data: { coinBalance: { increment: data.coinsAmount } }
        });

        const last4 = data.cardNumber.slice(-4);

        await prisma.walletTransaction.create({
            data: {
                walletId: wallet.id,
                type: "COIN_PURCHASE",
                status: "COMPLETED",
                amount: data.coinsAmount,
                currency: "COINS",
                description: `Paid with ${data.cardBrand} (•••• ${last4}) - Added ${data.coinsAmount} Coins`
            }
        });

        res.json({
            success: true,
            message: `Successfully charged ${data.cardBrand} (•••• ${last4}) and added ${data.coinsAmount} Coins!`,
            wallet: updatedWallet
        });
    } catch (error) {
        next(error);
    }
}

module.exports = {
    getWallet,
    getWalletTransactions,
    buyCoins,
    checkoutPayment,
    sendGift,
    requestWithdrawal,
    chargeCreditCard
};

