package c.app.sociamediabackend.data.model

import com.google.gson.annotations.SerializedName

data class UserSettings(
    @SerializedName("id") val id: String = "",
    @SerializedName("userId") val userId: String = "",
    @SerializedName("isPrivateAccount") val isPrivateAccount: Boolean = false,
    @SerializedName("postAudience") val postAudience: String = "EVERYONE",
    @SerializedName("commentPermission") val commentPermission: String = "EVERYONE",
    @SerializedName("directMessagePermission") val directMessagePermission: String = "EVERYONE",
    @SerializedName("duetPermission") val duetPermission: String = "EVERYONE",
    @SerializedName("stitchPermission") val stitchPermission: String = "EVERYONE",
    @SerializedName("downloadPermission") val downloadPermission: String = "EVERYONE",
    @SerializedName("mentionPermission") val mentionPermission: String = "EVERYONE",
    @SerializedName("pushNotificationsEnabled") val pushNotificationsEnabled: Boolean = true,
    @SerializedName("likesNotification") val likesNotification: Boolean = true,
    @SerializedName("commentsNotification") val commentsNotification: Boolean = true,
    @SerializedName("newFollowersNotification") val newFollowersNotification: Boolean = true,
    @SerializedName("directMessagesNotification") val directMessagesNotification: Boolean = true,
    @SerializedName("liveAlertsNotification") val liveAlertsNotification: Boolean = true,
    @SerializedName("quietHoursEnabled") val quietHoursEnabled: Boolean = false,
    @SerializedName("quietHoursStart") val quietHoursStart: String? = "22:00",
    @SerializedName("quietHoursEnd") val quietHoursEnd: String? = "07:00",
    @SerializedName("dataSaverEnabled") val dataSaverEnabled: Boolean = false,
    @SerializedName("autoplayCellular") val autoplayCellular: Boolean = true,
    @SerializedName("appLanguage") val appLanguage: String = "en",
    @SerializedName("screenTimeLimitMinutes") val screenTimeLimitMinutes: Int = 0,
    @SerializedName("restrictedMode") val restrictedMode: Boolean = false,
    @SerializedName("liveGuestPermission") val liveGuestPermission: String = "EVERYONE"
)

data class UserSettingsResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("settings") val settings: UserSettings? = null
)

data class UserSession(
    @SerializedName("id") val id: String = "",
    @SerializedName("userId") val userId: String = "",
    @SerializedName("deviceName") val deviceName: String = "Android Device",
    @SerializedName("deviceType") val deviceType: String = "ANDROID",
    @SerializedName("ipAddress") val ipAddress: String? = null,
    @SerializedName("lastActive") val lastActive: String? = null,
    @SerializedName("isCurrent") val isCurrent: Boolean = false
)

data class UserSessionsResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("sessions") val sessions: List<UserSession> = emptyList()
)

data class BlockedUsersResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("blockedUsers") val blockedUsers: List<User> = emptyList()
)

data class KeywordFiltersResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("keywords") val keywords: List<String> = emptyList()
)

data class DataExportResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("exportData") val exportData: Any? = null
)
