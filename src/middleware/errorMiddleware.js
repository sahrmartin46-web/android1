const { ZodError } = require("zod");
const multer = require("multer");

function errorMiddleware(err, req, res, next) {
    console.error("Express Error:", err);

    if (err instanceof ZodError) {
        const msg = err.errors.map(e => `${e.path.join(".")}: ${e.message}`).join(", ");
        return res.status(400).json({
            success: false,
            message: msg,
            error: {
                code: "VALIDATION_ERROR",
                message: msg
            }
        });
    }

    if (err instanceof multer.MulterError) {
        return res.status(400).json({
            success: false,
            message: `File upload error: ${err.message}`,
            error: {
                code: "FILE_UPLOAD_ERROR",
                message: `File upload error: ${err.message}`
            }
        });
    }

    const statusCode = err.statusCode || err.status || 500;
    const errorCode = err.code || (statusCode === 401 ? "AUTH_REQUIRED" : statusCode === 403 ? "NOT_HOST" : statusCode === 404 ? "LIVE_NOT_FOUND" : "INTERNAL_ERROR");
    const message = err.message || "Internal server error";

    res.status(statusCode).json({
        success: false,
        message: message,
        error: {
            code: errorCode,
            message: message
        }
    });
}

module.exports = errorMiddleware;
