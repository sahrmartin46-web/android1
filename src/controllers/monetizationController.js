const { z } = require("zod");
const prisma = require("../lib/prisma");

async function getMonetizationPrograms(req, res, next) {
    try {
        const programs = await prisma.monetizationProgram.findMany({
            where: { isActive: true },
            orderBy: { createdAt: "asc" },
            include: {
                enrollments: {
                    where: { userId: req.user.userId }
                }
            }
        });

        const formattedPrograms = programs.map(p => ({
            id: p.id,
            icon: p.icon,
            title: p.title,
            category: p.category,
            payoutRate: p.payoutRate,
            description: p.description,
            requirement: p.requirement,
            status: p.enrollments.length > 0 ? p.enrollments[0].status : "ACTIVE",
            isEnrolled: p.enrollments.length > 0
        }));

        res.json({
            success: true,
            programs: formattedPrograms
        });
    } catch (error) {
        next(error);
    }
}

const enrollSchema = z.object({
    programId: z.string().min(1)
});

async function enrollInProgram(req, res, next) {
    try {
        const { programId } = enrollSchema.parse(req.body);

        const program = await prisma.monetizationProgram.findUnique({
            where: { id: programId }
        });

        if (!program) {
            return res.status(404).json({ success: false, message: "Monetization program not found" });
        }

        const enrollment = await prisma.creatorEnrollment.upsert({
            where: {
                userId_programId: {
                    userId: req.user.userId,
                    programId
                }
            },
            create: {
                userId: req.user.userId,
                programId,
                status: "APPROVED"
            },
            update: {
                status: "APPROVED"
            }
        });

        res.json({
            success: true,
            message: `Successfully enrolled in ${program.title}!`,
            enrollment
        });
    } catch (error) {
        next(error);
    }
}

async function getArtistTracks(req, res, next) {
    try {
        const tracks = await prisma.artistTrack.findMany({
            where: { isActive: true },
            orderBy: { createdAt: "desc" }
        });

        res.json({
            success: true,
            tracks
        });
    } catch (error) {
        next(error);
    }
}

module.exports = {
    getMonetizationPrograms,
    enrollInProgram,
    getArtistTracks
};
