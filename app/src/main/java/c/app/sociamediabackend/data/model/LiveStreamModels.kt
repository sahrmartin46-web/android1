package c.app.sociamediabackend.data.model

data class LiveSession(
    val id: String,
    val title: String,
    val description: String?,
    val category: String?,
    val status: String?, // PREPARING, LIVE, ENDED
    val hostId: String,
    val totalViewers: Int?,
    val peakViewers: Int?,
    val totalDiamonds: Int?,
    val createdAt: String?,
    val host: User?
)

data class LiveSessionResponse(
    val success: Boolean,
    val message: String?,
    val liveSession: LiveSession?
)

data class LiveSessionsResponse(
    val success: Boolean,
    val liveSessions: List<LiveSession>?
)

data class LiveTokenResponse(
    val success: Boolean,
    val token: String?,
    val roomName: String?
)

data class LiveCommentItem(
    val id: String,
    val liveId: String,
    val userId: String,
    val text: String,
    val user: User?
)

data class LiveCommentsResponse(
    val success: Boolean,
    val comments: List<LiveCommentItem>?
)
