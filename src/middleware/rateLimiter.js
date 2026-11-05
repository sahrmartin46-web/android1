const rateLimits = new Map();

function rateLimiter(options = {}) {
    const windowMs = options.windowMs || 60 * 1000;
    const max = options.max || 100;

    return (req, res, next) => {
        const ip = req.ip || req.headers["x-forwarded-for"] || "127.0.0.1";
        const key = `${req.path}:${ip}`;
        const now = Date.now();

        let record = rateLimits.get(key);
        if (!record || now - record.startTime > windowMs) {
            record = { count: 1, startTime: now };
            rateLimits.set(key, record);
        } else {
            record.count += 1;
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
