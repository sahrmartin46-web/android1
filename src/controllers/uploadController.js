function uploadFile(req, res, next) {
    try {
        if (!req.file) {
            return res.status(400).json({
                success: false,
                message: "No file uploaded"
            });
        }

        const defaultStorageUrl = "https://f6d1d15e6f0b37b4b8fcad3c41a7922d.r2.cloudflarestorage.com";
        const storageBaseUrl = process.env.STORAGE_BASE_URL || defaultStorageUrl;
        const cleanBaseUrl = storageBaseUrl.endsWith('/') ? storageBaseUrl.slice(0, -1) : storageBaseUrl;
        const fileUrl = `${cleanBaseUrl}/${req.file.filename}`;

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
