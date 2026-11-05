const express = require("express");
const router = express.Router();
const {
    createSession,
    startSession,
    endSession,
    getLiveSessions,
    getLiveSessionById,
    getLiveParticipants,
    getLiveToken,
    createGuestRequest,
    acceptGuestRequest,
    rejectGuestRequest,
    removeParticipant,
    muteParticipant,
    getLiveSettings,
    updateLiveSettings,
    getLiveComments,
    createLiveComment,
    deleteLiveComment,
    likeLiveStream,
    sendLiveGift,
    createLivePoll,
    voteLivePoll,
    getLivePolls,
    createLiveQuestion,
    getLiveQuestions,
    answerLiveQuestion,
    startLiveMatch,
    getLiveMatch,
    setLiveGoal,
    getLiveGoal
} = require("../controllers/liveController");
const authMiddleware = require("../middleware/authMiddleware");

// Canonical Live Routes
router.get("/", authMiddleware, getLiveSessions);
router.post("/", authMiddleware, createSession);
router.get("/:liveId", authMiddleware, getLiveSessionById);
router.get("/:liveId/participants", authMiddleware, getLiveParticipants);
router.get("/:liveId/settings", authMiddleware, getLiveSettings);
router.patch("/:liveId/settings", authMiddleware, updateLiveSettings);

router.post("/:liveId/start", authMiddleware, startSession);
router.post("/:liveId/end", authMiddleware, endSession);
router.post("/:liveId/token", authMiddleware, getLiveToken);
router.post("/:liveId/guest-requests", authMiddleware, createGuestRequest);

router.get("/:liveId/comments", authMiddleware, getLiveComments);
router.post("/:liveId/comments", authMiddleware, createLiveComment);
router.delete("/:liveId/comments/:commentId", authMiddleware, deleteLiveComment);

router.post("/:liveId/likes", authMiddleware, likeLiveStream);
router.post("/:liveId/gifts", authMiddleware, sendLiveGift);

router.get("/:liveId/polls", authMiddleware, getLivePolls);
router.post("/:liveId/polls", authMiddleware, createLivePoll);
router.post("/:liveId/polls/:pollId/vote", authMiddleware, voteLivePoll);

router.get("/:liveId/questions", authMiddleware, getLiveQuestions);
router.post("/:liveId/questions", authMiddleware, createLiveQuestion);
router.patch("/:liveId/questions/:questionId/answer", authMiddleware, answerLiveQuestion);

router.get("/:liveId/matches", authMiddleware, getLiveMatch);
router.post("/:liveId/matches/start", authMiddleware, startLiveMatch);

router.get("/:liveId/goals", authMiddleware, getLiveGoal);
router.post("/:liveId/goals", authMiddleware, setLiveGoal);

router.post("/:liveId/participants/:participantId/remove", authMiddleware, removeParticipant);
router.post("/:liveId/participants/:participantId/mute", authMiddleware, muteParticipant);

// Backwards-compatible aliases under /sessions
router.get("/sessions", authMiddleware, getLiveSessions);
router.post("/sessions", authMiddleware, createSession);
router.get("/sessions/:liveId", authMiddleware, getLiveSessionById);
router.get("/sessions/:liveId/participants", authMiddleware, getLiveParticipants);
router.get("/sessions/:liveId/settings", authMiddleware, getLiveSettings);
router.patch("/sessions/:liveId/settings", authMiddleware, updateLiveSettings);

router.post("/sessions/:liveId/start", authMiddleware, startSession);
router.post("/sessions/:liveId/end", authMiddleware, endSession);
router.post("/sessions/:liveId/token", authMiddleware, getLiveToken);
router.post("/sessions/:liveId/guest-requests", authMiddleware, createGuestRequest);

router.get("/sessions/:liveId/comments", authMiddleware, getLiveComments);
router.post("/sessions/:liveId/comments", authMiddleware, createLiveComment);
router.delete("/sessions/:liveId/comments/:commentId", authMiddleware, deleteLiveComment);

router.post("/sessions/:liveId/likes", authMiddleware, likeLiveStream);
router.post("/sessions/:liveId/gifts", authMiddleware, sendLiveGift);

router.post("/sessions/:liveId/participants/:participantId/remove", authMiddleware, removeParticipant);
router.post("/sessions/:liveId/participants/:participantId/mute", authMiddleware, muteParticipant);

router.post("/guest-requests/:requestId/accept", authMiddleware, acceptGuestRequest);
router.post("/guest-requests/:requestId/reject", authMiddleware, rejectGuestRequest);
router.post("/sessions/guest-requests/:requestId/accept", authMiddleware, acceptGuestRequest);
router.post("/sessions/guest-requests/:requestId/reject", authMiddleware, rejectGuestRequest);

module.exports = router;
