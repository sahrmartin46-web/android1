function uploadFile(req, res, next) {
    try {
        if (!req.file) {
            return res.status(400).json({
                success: false,
                message: "No file uploaded"
            });
        }

        const protocol = req.protocol;
        const host = req.get("host");
        const fileUrl = `${protocol}://${host}/uploads/${req.file.filename}`;

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
