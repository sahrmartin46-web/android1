const { AccessToken, RoomServiceClient } = require("livekit-server-sdk");

const API_KEY = process.env.LIVEKIT_API_KEY || "devkey";
const API_SECRET = process.env.LIVEKIT_API_SECRET || "secret";
const LIVEKIT_HTTP_URL = process.env.LIVEKIT_HTTP_URL || process.env.LIVEKIT_URL || "http://localhost:7880";

const httpUrl = LIVEKIT_HTTP_URL.replace(/^ws/, "http");

const roomService = new RoomServiceClient(httpUrl, API_KEY, API_SECRET);

function createLiveKitToken(roomName, participantIdentity, participantName, isPublisher = false) {
    const at = new AccessToken(API_KEY, API_SECRET, {
        identity: participantIdentity,
        name: participantName,
        ttl: "2h",
    });
    at.addGrant({
        roomJoin: true,
        room: roomName,
        canPublish: isPublisher,
        canSubscribe: true,
        canPublishData: true,
    });
    return at.toJwt();
}

async function removeLiveKitParticipant(roomName, participantIdentity) {
    try {
        await roomService.removeParticipant(roomName, participantIdentity);
    } catch (error) {
        console.warn("LiveKit removeParticipant warning:", error.message);
    }
}

async function muteLiveKitParticipantTrack(roomName, participantIdentity, trackSid, muted = true) {
    try {
        await roomService.mutePublishedTrack(roomName, participantIdentity, trackSid, muted);
    } catch (error) {
        console.warn("LiveKit mutePublishedTrack warning:", error.message);
    }
}

module.exports = {
    createLiveKitToken,
    removeLiveKitParticipant,
    muteLiveKitParticipantTrack
};
