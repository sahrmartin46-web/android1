const { z } = require("zod");
const prisma = require("../lib/prisma");

async function getBankAccounts(req, res, next) {
    try {
        const accounts = await prisma.bankAccount.findMany({
            where: { userId: req.user.userId },
            orderBy: { createdAt: "desc" }
        });

        res.json({
            success: true,
            bankAccounts: accounts
        });
    } catch (error) {
        next(error);
    }
}

const addBankAccountSchema = z.object({
    region: z.string().min(2), // US, EU, UK, CA, GLOBAL
    accountHolder: z.string().min(2),
    bankName: z.string().min(2),
    accountNumber: z.string().min(4),
    routingNumber: z.string().optional(),
    iban: z.string().optional(),
    swiftBic: z.string().optional(),
    isPrimary: z.boolean().optional().default(false)
});

async function addBankAccount(req, res, next) {
    try {
        const data = addBankAccountSchema.parse(req.body);

        if (data.isPrimary) {
            await prisma.bankAccount.updateMany({
                where: { userId: req.user.userId },
                data: { isPrimary: false }
            });
        }

        const account = await prisma.bankAccount.create({
            data: {
                userId: req.user.userId,
                region: data.region,
                accountHolder: data.accountHolder,
                bankName: data.bankName,
                accountNumber: data.accountNumber,
                routingNumber: data.routingNumber || null,
                iban: data.iban || null,
                swiftBic: data.swiftBic || null,
                isPrimary: data.isPrimary
            }
        });

        res.json({
            success: true,
            message: "Bank account linked successfully",
            bankAccount: account
        });
    } catch (error) {
        next(error);
    }
}

async function deleteBankAccount(req, res, next) {
    try {
        const { id } = req.params;

        await prisma.bankAccount.deleteMany({
            where: { id, userId: req.user.userId }
        });

        res.json({
            success: true,
            message: "Bank account removed successfully"
        });
    } catch (error) {
        next(error);
    }
}

module.exports = {
    getBankAccounts,
    addBankAccount,
    deleteBankAccount
};
