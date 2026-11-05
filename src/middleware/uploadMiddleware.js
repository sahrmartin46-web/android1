const multer = require("multer");
const path = require("path");
const fs = require("fs");

const uploadDir = path.join(__dirname, "../../uploads");

if (!fs.existsSync(uploadDir)) {
    fs.mkdirSync(uploadDir, { recursive: true });
}

const storage = multer.diskStorage({
    destination: (req, file, cb) => {
        cb(null, uploadDir);
    },
    filename: (req, file, cb) => {
        const uniqueSuffix = Date.now() + "-" + Math.round(Math.random() * 1e9);
        const ext = path.extname(file.originalname) || ".bin";
        cb(null, `${file.fieldname}-${uniqueSuffix}${ext}`);
    }
});

const fileFilter = (req, file, cb) => {
    const mimetype = (file.mimetype || "").toLowerCase();
    const ext = path.extname(file.originalname || "").toLowerCase();

    const isImage = mimetype.startsWith("image") || [".jpg", ".jpeg", ".png", ".webp", ".gif", ".heic"].includes(ext);
    const isVideo = mimetype.startsWith("video") || [".mp4", ".mov", ".mkv", ".webm", ".avi", ".3gp"].includes(ext);

    if (isImage || isVideo || mimetype === "application/octet-stream") {
        cb(null, true);
    } else {
        const err = new Error("Unsupported file type. Only images and videos are allowed.");
        err.statusCode = 400;
        cb(err);
    }
};

const upload = multer({
    storage,
    fileFilter,
    limits: {
        fileSize: 100 * 1024 * 1024 // 100MB
    }
});

module.exports = upload;
