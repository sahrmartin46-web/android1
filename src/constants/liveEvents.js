const LIVE_SOCKET_EVENTS = {
    CREATED: "live:created",
    STARTED: "live:started",
    ENDED: "live:ended",

    PARTICIPANT_JOINED: "live:participant_joined",
    PARTICIPANT_LEFT: "live:participant_left",
    PARTICIPANT_UPDATED: "live:participant_updated",

    GUEST_REQUEST_CREATED: "live:guest_request_created",
    GUEST_REQUEST_ACCEPTED: "live:guest_request_accepted",
    GUEST_REQUEST_REJECTED: "live:guest_request_rejected",

    GUEST_REMOVED: "live:guest_removed",

    PARTICIPANT_MUTED: "live:participant_muted",
    PARTICIPANT_CAMERA_CHANGED: "live:participant_camera_changed",
    PARTICIPANT_MICROPHONE_CHANGED: "live:participant_microphone_changed",

    LAYOUT_CHANGED: "live:layout_changed",
    SPOTLIGHT_CHANGED: "live:spotlight_changed",

    COMMENT_CREATED: "live:comment_created",
    COMMENT_DELETED: "live:comment_deleted",

    LIKE_CREATED: "live:like_created",

    GIFT_SENT: "live:gift_sent",
    WALLET_UPDATED: "live:wallet_updated",

    MODERATION_ACTION: "live:moderation_action",

    ERROR: "live:error"
};

module.exports = {
    LIVE_SOCKET_EVENTS
};
