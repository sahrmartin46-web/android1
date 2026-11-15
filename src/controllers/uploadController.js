function uploadFile(req, res, next) {
    try {
        if (!req.file) {
            return res.status(400).json({
                success: false,
                message: "No file uploaded"
            });
        }

        let fileUrl;
        if (process.env.STORAGE_BASE_URL) {
            const cleanBaseUrl = process.env.STORAGE_BASE_URL.endsWith('/')
                ? process.env.STORAGE_BASE_URL.slice(0, -1)
                : process.env.STORAGE_BASE_URL;
            fileUrl = `${cleanBaseUrl}/${req.file.filename}`;
        } else {
            const host = req.get("host");
            const protocol = req.protocol === "https" || req.get("x-forwarded-proto") === "https" ? "https" : "http";
            fileUrl = `${protocol}://${host}/uploads/${req.file.filename}`;
        }

        res.status(201).json({
            success: true,
            message: "File uploaded successfully",
            url: fileUrl,
            file: {
                filename: req.file.filename,
                originalname: req.file.originalname,
                mimetype: req.file.mimetype,
                size: req.file.size
            }
        });

    } catch (error) {
        next(error);
    }
}

module.exports = {
    uploadFile
};
