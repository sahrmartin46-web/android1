function logEvent(eventType, details = {}) {
    const sanitized = { ...details };
    delete sanitized.password;
    delete sanitized.passwordHash;
    delete sanitized.token;
    delete sanitized.jwtSecret;
    delete sanitized.apiSecret;
    delete sanitized.paymentSecret;

    console.log(`[OBSERVABILITY][${new Date().toISOString()}] ${eventType}`, JSON.stringify(sanitized));
}

module.exports = { logEvent };
