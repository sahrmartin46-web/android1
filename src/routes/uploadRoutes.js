const express = require("express");

const { uploadFile } = require("../controllers/uploadController");

const upload = require("../middleware/uploadMiddleware");

const authMiddleware = require("../middleware/authMiddleware");

const router = express.Router();

router.post(
    "/video",
    authMiddleware,
    upload.single("video"),
    uploadFile
);

router.post(
    "/image",
    authMiddleware,
    upload.single("image"),
    uploadFile
);

router.post(
    "/file",
    authMiddleware,
    upload.single("file"),
    uploadFile
);

module.exports = router;
