package c.app.sociamediabackend.data.model

import com.google.gson.annotations.SerializedName

data class ScreenVideoItem(
    @SerializedName("id") val id: String = "",
    @SerializedName("views") val views: String = "",
    @SerializedName("handle") val handle: String = "",
    @SerializedName("imageUrl") val imageUrl: String? = null
)

data class HighlightItem(
    @SerializedName("title") val title: String = "",
    @SerializedName("type") val type: String = ""
)

data class MayaProfileData(
    @SerializedName("username") val username: String = "",
    @SerializedName("displayName") val displayName: String = "",
    @SerializedName("bio") val bio: String = "",
    @SerializedName("isVerified") val isVerified: Boolean = true,
    @SerializedName("followingCount") val followingCount: Int = 248,
    @SerializedName("followersCount") val followersCount: String = "3.2M",
    @SerializedName("likesCount") val likesCount: String = "48.9M",
    @SerializedName("avatarUrl") val avatarUrl: String? = null,
    @SerializedName("highlights") val highlights: List<HighlightItem> = emptyList(),
    @SerializedName("videos") val videos: List<ScreenVideoItem> = emptyList()
)

data class MayaProfileResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("profile") val profile: MayaProfileData? = null
)

data class SoundDetailsData(
    @SerializedName("title") val title: String = "",
    @SerializedName("mix") val mix: String = "",
    @SerializedName("duration") val duration: String = "",
    @SerializedName("videosCount") val videosCount: String = "",
    @SerializedName("creator") val creator: String = "",
    @SerializedName("isVerified") val isVerified: Boolean = true,
    @SerializedName("waveform") val waveform: List<Int> = emptyList(),
    @SerializedName("videos") val videos: List<ScreenVideoItem> = emptyList()
)

data class SoundDetailsResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("sound") val sound: SoundDetailsData? = null
)
