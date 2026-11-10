const MAX_KEYS = 5000;
const CLEANUP_INTERVAL = 60 * 1000;

const rateLimits = new Map();
let lastCleanup = Date.now();

function pruneExpiredEntries(now) {
    if (now - lastCleanup < CLEANUP_INTERVAL) return;

    for (const [key, record] of rateLimits.entries()) {
        if (now - record.startTime > record.windowMs) {
            rateLimits.delete(key);
        }
    }

    if (rateLimits.size > MAX_KEYS) {
        const oldestEntries = [...rateLimits.entries()]
            .sort((a, b) => a[1].startTime - b[1].startTime)
            .slice(0, rateLimits.size - MAX_KEYS);

        for (const [key] of oldestEntries) {
            rateLimits.delete(key);
        }
    }

    lastCleanup = now;
}

function rateLimiter(options = {}) {
    const windowMs = options.windowMs || 60 * 1000;
    const max = options.max || 100;

    return (req, res, next) => {
        const ip = req.ip || req.headers["x-forwarded-for"] || "127.0.0.1";
        const key = `${req.path}:${ip}`;
        const now = Date.now();

        pruneExpiredEntries(now);

        let record = rateLimits.get(key);
        if (!record || now - record.startTime > windowMs) {
            record = { count: 1, startTime: now, windowMs };
            rateLimits.set(key, record);
        } else {
            record.count += 1;
            record.startTime = record.startTime || now;
        }

        if (record.count > max) {
            return res.status(429).json({
                success: false,
                error: {
                    code: "RATE_LIMITED",
                    message: "Too many requests. Please try again later."
                }
            });
        }

        next();
    };
}

module.exports = rateLimiter;
