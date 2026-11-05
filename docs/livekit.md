# FUGEN LiveKit WebRTC Integration

Fugen uses LiveKit WebRTC SFU for interactive real-time video and audio streaming supporting up to 16 participants simultaneously on stage.

## Token Generation
Clients request a LiveKit access token from the backend (`POST /api/v1/live/sessions/:id/token`). The backend uses `livekit-server-sdk` to create a cryptographically signed JWT grant:

```javascript
const { AccessToken } = require("livekit-server-sdk");

function createLiveKitToken(roomName, participantIdentity, participantName, isPublisher) {
    const at = new AccessToken(API_KEY, API_SECRET, {
        identity: participantIdentity,
        name: participantName,
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
```

## Participants & Layouts
Supports automatic reflow across 7 grid layouts (Side by Side, Spotlight, Roundtable I, Roundtable II, 2x2, 3x3, 4x4).
