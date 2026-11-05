const { ZodError } = require("zod");
const multer = require("multer");

function errorMiddleware(err, req, res, next) {
    console.error("Express Error:", err);

    if (err instanceof ZodError) {
        return res.status(400).json({
            success: false,
            error: {
                code: "VALIDATION_ERROR",
                message: err.errors.map(e => `${e.path.join(".")}: ${e.message}`).join(", ")
            }
        });
    }

    if (err instanceof multer.MulterError) {
        return res.status(400).json({
            success: false,
            error: {
                code: "FILE_UPLOAD_ERROR",
                message: `File upload error: ${err.message}`
            }
        });
    }

    const statusCode = err.statusCode || err.status || 500;
    const errorCode = err.code || (statusCode === 401 ? "AUTH_REQUIRED" : statusCode === 403 ? "NOT_HOST" : statusCode === 404 ? "LIVE_NOT_FOUND" : "INTERNAL_ERROR");

    res.status(statusCode).json({
        success: false,
        error: {
            code: errorCode,
            message: err.message || "Internal server error"
        }
    });
}

module.exports = errorMiddleware;
