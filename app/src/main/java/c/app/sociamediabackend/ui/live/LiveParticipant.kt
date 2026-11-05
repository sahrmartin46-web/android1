package c.app.sociamediabackend.ui.live

data class LiveParticipant(
    val id: String,
    val name: String,
    val avatarUrl: String,
    val isHost: Boolean = false,
    val isInviteSlot: Boolean = false,
    val viewerCount: String = "",
    val isActiveSpeaker: Boolean = false
)
