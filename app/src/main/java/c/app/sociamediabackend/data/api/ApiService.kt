package c.app.sociamediabackend.data.api

import c.app.sociamediabackend.data.model.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @GET("auth/me")
    suspend fun getMe(): Response<AuthResponse>

    @GET("posts")
    suspend fun getPosts(
        @Query("feedType") feedType: String = "foryou",
        @Query("currentUserId") currentUserId: String? = null,
        @Query("cursor") cursor: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<PostsResponse>

    @POST("posts")
    suspend fun createPost(@Body request: CreatePostRequest): Response<GenericResponse>

    @DELETE("posts/{id}")
    suspend fun deletePost(@Path("id") postId: String): Response<GenericResponse>

    @POST("posts/{id}/like")
    suspend fun likePost(@Path("id") postId: String): Response<GenericResponse>

    @POST("posts/{id}/view")
    suspend fun incrementView(@Path("id") postId: String): Response<GenericResponse>

    @GET("comments/post/{postId}")
    suspend fun getComments(@Path("postId") postId: String): Response<CommentsResponse>

    @POST("comments/post/{postId}")
    suspend fun createComment(@Path("postId") postId: String, @Body request: Map<String, String>): Response<GenericResponse>

    @DELETE("comments/{commentId}")
    suspend fun deleteComment(@Path("commentId") commentId: String): Response<GenericResponse>

    @GET("users/{username}")
    suspend fun getProfile(@Path("username") username: String): Response<ProfileResponse>

    @PUT("users/me")
    suspend fun updateProfile(@Body request: Map<String, String>): Response<ProfileResponse>

    @POST("follows/{userId}")
    suspend fun followUser(@Path("userId") userId: String): Response<GenericResponse>

    @POST("v1/live/sessions")
    suspend fun createLiveSession(@Body request: Map<String, Any>): Response<LiveSessionResponse>

    @POST("v1/live/sessions/{id}/start")
    suspend fun startLiveSession(@Path("id") sessionId: String): Response<LiveSessionResponse>

    @POST("v1/live/sessions/{id}/end")
    suspend fun endLiveSession(@Path("id") sessionId: String): Response<LiveSessionResponse>

    @GET("v1/live/sessions")
    suspend fun getLiveSessions(@Query("category") category: String? = null): Response<LiveSessionsResponse>

    @POST("v1/live/sessions/{id}/token")
    suspend fun getLiveToken(@Path("id") sessionId: String): Response<LiveTokenResponse>

    @GET("v1/live/sessions/{id}/comments")
    suspend fun getLiveComments(@Path("id") sessionId: String): Response<LiveCommentsResponse>

    @POST("v1/live/sessions/{id}/comments")
    suspend fun sendLiveComment(@Path("id") sessionId: String, @Body request: Map<String, String>): Response<GenericResponse>

    @POST("v1/live/sessions/{id}/likes")
    suspend fun likeLiveStream(@Path("id") sessionId: String): Response<GenericResponse>

    @POST("v1/live/sessions/{id}/guest-requests")
    suspend fun requestGuestJoin(@Path("id") liveId: String): Response<GenericResponse>

    @POST("v1/live/guest-requests/{id}/accept")
    suspend fun acceptGuestRequest(@Path("id") requestId: String): Response<GenericResponse>

    @POST("v1/live/guest-requests/{id}/reject")
    suspend fun rejectGuestRequest(@Path("id") requestId: String): Response<GenericResponse>

    @GET("v1/wallet")
    suspend fun getWallet(): Response<WalletResponse>

    @POST("v1/wallet/buy-coins")
    suspend fun buyCoins(@Body request: Map<String, Int>): Response<WalletResponse>

    @POST("v1/wallet/send-gift")
    suspend fun sendGift(@Body request: Map<String, Any>): Response<WalletResponse>

    @POST("v1/wallet/withdraw")
    suspend fun requestWithdrawal(@Body request: Map<String, Any>): Response<GenericResponse>

    @POST("payments/charge-card")
    suspend fun chargeCreditCard(@Body request: Map<String, @JvmSuppressWildcards Any>): Response<WalletResponse>

    @GET("coins/gifts")
    suspend fun getGiftCatalog(): Response<GiftCatalogResponse>

    @GET("v1/monetization/programs")
    suspend fun getMonetizationPrograms(): Response<MonetizationProgramsResponse>

    @POST("v1/monetization/enroll")
    suspend fun enrollInProgram(@Body request: Map<String, String>): Response<GenericResponse>

    @Multipart
    @POST("upload/video")
    suspend fun uploadVideo(@Part video: MultipartBody.Part): Response<UploadResponse>

    @GET("v1/screens/maya-profile")
    suspend fun getMayaProfile(): Response<MayaProfileResponse>

    @GET("v1/screens/sound-details")
    suspend fun getSoundDetails(): Response<SoundDetailsResponse>

    @Multipart
    @POST("upload/image")
    suspend fun uploadImage(@Part image: MultipartBody.Part): Response<UploadResponse>

    @GET("settings")
    suspend fun getSettings(): Response<UserSettingsResponse>

    @PATCH("settings")
    suspend fun updateSettings(@Body request: Map<String, @JvmSuppressWildcards Any>): Response<UserSettingsResponse>

    @POST("settings/change-password")
    suspend fun changePassword(@Body request: Map<String, String>): Response<GenericResponse>

    @POST("settings/switch-account-type")
    suspend fun switchAccountType(@Body request: Map<String, String>): Response<AuthResponse>

    @GET("settings/sessions")
    suspend fun getSessions(): Response<UserSessionsResponse>

    @DELETE("settings/sessions/{sessionId}")
    suspend fun revokeSession(@Path("sessionId") sessionId: String): Response<GenericResponse>

    @GET("settings/blocks")
    suspend fun getBlockedUsers(): Response<BlockedUsersResponse>

    @POST("settings/blocks")
    suspend fun blockUser(@Body request: Map<String, String>): Response<GenericResponse>

    @DELETE("settings/blocks/{targetUserId}")
    suspend fun unblockUser(@Path("targetUserId") targetUserId: String): Response<GenericResponse>

    @GET("settings/keywords")
    suspend fun getKeywordFilters(): Response<KeywordFiltersResponse>

    @POST("settings/keywords")
    suspend fun addKeywordFilter(@Body request: Map<String, String>): Response<GenericResponse>

    @DELETE("settings/keywords/{keyword}")
    suspend fun deleteKeywordFilter(@Path("keyword") keyword: String): Response<GenericResponse>

    @GET("settings/banks")
    suspend fun getBankAccounts(): Response<BankAccountsResponse>

    @POST("settings/banks")
    suspend fun addBankAccount(@Body request: Map<String, @JvmSuppressWildcards Any>): Response<BankAccountsResponse>

    @DELETE("settings/banks/{id}")
    suspend fun deleteBankAccount(@Path("id") id: String): Response<GenericResponse>

    @GET("settings/export")
    suspend fun exportUserData(): Response<DataExportResponse>

    @POST("settings/deactivate")
    suspend fun deactivateAccount(): Response<GenericResponse>

    @DELETE("settings/account")
    suspend fun deleteAccount(): Response<GenericResponse>
}
