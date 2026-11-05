package c.app.sociamediabackend.ui.auth

import c.app.sociamediabackend.data.api.ApiService
import c.app.sociamediabackend.data.api.NetworkResult
import c.app.sociamediabackend.data.model.AuthResponse
import c.app.sociamediabackend.data.model.BankAccount
import c.app.sociamediabackend.data.model.BankAccountsResponse
import c.app.sociamediabackend.data.model.BlockedUsersResponse
import c.app.sociamediabackend.data.model.CommentsResponse
import c.app.sociamediabackend.data.model.CreatePostRequest
import c.app.sociamediabackend.data.model.DataExportResponse
import c.app.sociamediabackend.data.model.GenericResponse
import c.app.sociamediabackend.data.model.GiftCatalogResponse
import c.app.sociamediabackend.data.model.KeywordFiltersResponse
import c.app.sociamediabackend.data.model.LiveCommentsResponse
import c.app.sociamediabackend.data.model.LiveSessionResponse
import c.app.sociamediabackend.data.model.LiveSessionsResponse
import c.app.sociamediabackend.data.model.LiveTokenResponse
import c.app.sociamediabackend.data.model.LoginRequest
import c.app.sociamediabackend.data.model.MayaProfileResponse
import c.app.sociamediabackend.data.model.MonetizationProgramsResponse
import c.app.sociamediabackend.data.model.PostsResponse
import c.app.sociamediabackend.data.model.ProfileResponse
import c.app.sociamediabackend.data.model.RegisterRequest
import c.app.sociamediabackend.data.model.SoundDetailsResponse
import c.app.sociamediabackend.data.model.UploadResponse
import c.app.sociamediabackend.data.model.User
import c.app.sociamediabackend.data.model.UserSessionsResponse
import c.app.sociamediabackend.data.model.UserSettings
import c.app.sociamediabackend.data.model.UserSettingsResponse
import c.app.sociamediabackend.data.model.WalletResponse
import c.app.sociamediabackend.data.repository.AuthRepository
import kotlinx.coroutines.runBlocking
import okhttp3.MultipartBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

class FakeApiService : ApiService {
    var shouldSucceed = true
    var responseToken = "fake-jwt-token"

    override suspend fun register(request: RegisterRequest): Response<AuthResponse> {
        return if (shouldSucceed) {
            Response.success(AuthResponse(success = true, token = responseToken, user = User(id = "1", username = "testuser")))
        } else {
            Response.success(AuthResponse(success = false, message = "User already exists"))
        }
    }

    override suspend fun login(request: LoginRequest): Response<AuthResponse> {
        return if (shouldSucceed) {
            Response.success(AuthResponse(success = true, token = responseToken, user = User(id = "1", username = "testuser")))
        } else {
            Response.success(AuthResponse(success = false, message = "Invalid credentials"))
        }
    }

    override suspend fun getMe(): Response<AuthResponse> {
        return Response.success(AuthResponse(success = true, user = User(id = "1", username = "testuser")))
    }

    override suspend fun getPosts(feedType: String, currentUserId: String?, cursor: String?, page: Int, limit: Int): Response<PostsResponse> {
        return Response.success(PostsResponse(success = true))
    }

    override suspend fun createPost(request: CreatePostRequest): Response<GenericResponse> {
        return Response.success(GenericResponse(success = true))
    }

    override suspend fun deletePost(postId: String): Response<GenericResponse> {
        return Response.success(GenericResponse(success = true))
    }

    override suspend fun likePost(postId: String): Response<GenericResponse> {
        return Response.success(GenericResponse(success = true))
    }

    override suspend fun incrementView(postId: String): Response<GenericResponse> {
        return Response.success(GenericResponse(success = true))
    }

    override suspend fun getComments(postId: String): Response<CommentsResponse> {
        return Response.success(CommentsResponse(success = true))
    }

    override suspend fun createComment(postId: String, request: Map<String, String>): Response<GenericResponse> {
        return Response.success(GenericResponse(success = true))
    }

    override suspend fun deleteComment(commentId: String): Response<GenericResponse> {
        return Response.success(GenericResponse(success = true))
    }

    override suspend fun getProfile(username: String): Response<ProfileResponse> {
        return Response.success(ProfileResponse(success = true))
    }

    override suspend fun updateProfile(request: Map<String, String>): Response<ProfileResponse> {
        return Response.success(ProfileResponse(success = true))
    }

    override suspend fun followUser(userId: String): Response<GenericResponse> {
        return Response.success(GenericResponse(success = true))
    }

    override suspend fun createLiveSession(request: Map<String, Any>): Response<LiveSessionResponse> {
        return Response.success(LiveSessionResponse(success = true, message = null, liveSession = null))
    }

    override suspend fun startLiveSession(sessionId: String): Response<LiveSessionResponse> {
        return Response.success(LiveSessionResponse(success = true, message = null, liveSession = null))
    }

    override suspend fun endLiveSession(sessionId: String): Response<LiveSessionResponse> {
        return Response.success(LiveSessionResponse(success = true, message = null, liveSession = null))
    }

    override suspend fun getLiveSessions(category: String?): Response<LiveSessionsResponse> {
        return Response.success(LiveSessionsResponse(success = true, liveSessions = emptyList()))
    }

    override suspend fun getLiveToken(sessionId: String): Response<LiveTokenResponse> {
        return Response.success(LiveTokenResponse(success = true, token = "token", roomName = "room"))
    }

    override suspend fun getLiveComments(sessionId: String): Response<LiveCommentsResponse> {
        return Response.success(LiveCommentsResponse(success = true, comments = emptyList()))
    }

    override suspend fun sendLiveComment(sessionId: String, request: Map<String, String>): Response<GenericResponse> {
        return Response.success(GenericResponse(success = true))
    }

    override suspend fun likeLiveStream(sessionId: String): Response<GenericResponse> {
        return Response.success(GenericResponse(success = true))
    }

    override suspend fun requestGuestJoin(liveId: String): Response<GenericResponse> {
        return Response.success(GenericResponse(success = true))
    }

    override suspend fun acceptGuestRequest(requestId: String): Response<GenericResponse> {
        return Response.success(GenericResponse(success = true))
    }

    override suspend fun rejectGuestRequest(requestId: String): Response<GenericResponse> {
        return Response.success(GenericResponse(success = true))
    }

    override suspend fun getWallet(): Response<WalletResponse> {
        return Response.success(WalletResponse(success = true, message = null, wallet = null))
    }

    override suspend fun buyCoins(request: Map<String, Int>): Response<WalletResponse> {
        return Response.success(WalletResponse(success = true, message = null, wallet = null))
    }

    override suspend fun chargeCreditCard(request: Map<String, Any>): Response<WalletResponse> {
        return Response.success(WalletResponse(success = true, message = "Charged successfully", wallet = null))
    }

    override suspend fun sendGift(request: Map<String, Any>): Response<WalletResponse> {
        return Response.success(WalletResponse(success = true, message = null, wallet = null))
    }

    override suspend fun requestWithdrawal(request: Map<String, Any>): Response<GenericResponse> {
        return Response.success(GenericResponse(success = true))
    }

    override suspend fun getGiftCatalog(): Response<GiftCatalogResponse> {
        return Response.success(GiftCatalogResponse(success = true, gifts = emptyList()))
    }

    override suspend fun getMonetizationPrograms(): Response<MonetizationProgramsResponse> {
        return Response.success(MonetizationProgramsResponse(success = true, programs = emptyList()))
    }

    override suspend fun enrollInProgram(request: Map<String, String>): Response<GenericResponse> {
        return Response.success(GenericResponse(success = true))
    }

    override suspend fun uploadVideo(video: MultipartBody.Part): Response<UploadResponse> {
        return Response.success(UploadResponse(success = true))
    }

    override suspend fun getMayaProfile(): Response<MayaProfileResponse> {
        return Response.success(MayaProfileResponse(success = true))
    }

    override suspend fun getSoundDetails(): Response<SoundDetailsResponse> {
        return Response.success(SoundDetailsResponse(success = true))
    }

    override suspend fun uploadImage(image: MultipartBody.Part): Response<UploadResponse> {
        return Response.success(UploadResponse(success = true))
    }

    override suspend fun getSettings(): Response<UserSettingsResponse> {
        return Response.success(UserSettingsResponse(success = true, settings = UserSettings()))
    }

    override suspend fun updateSettings(request: Map<String, Any>): Response<UserSettingsResponse> {
        val isPrivate = request["isPrivateAccount"] as? Boolean ?: false
        return Response.success(UserSettingsResponse(success = true, settings = UserSettings(isPrivateAccount = isPrivate)))
    }

    override suspend fun changePassword(request: Map<String, String>): Response<GenericResponse> {
        return Response.success(GenericResponse(success = true, message = "Password updated successfully"))
    }

    override suspend fun switchAccountType(request: Map<String, String>): Response<AuthResponse> {
        val role = request["targetRole"] ?: "USER"
        return Response.success(AuthResponse(success = true, message = "Account type switched", user = User(id = "1", username = "testuser", role = role)))
    }

    override suspend fun getSessions(): Response<UserSessionsResponse> {
        return Response.success(UserSessionsResponse(success = true))
    }

    override suspend fun revokeSession(sessionId: String): Response<GenericResponse> {
        return Response.success(GenericResponse(success = true))
    }

    override suspend fun getBlockedUsers(): Response<BlockedUsersResponse> {
        return Response.success(BlockedUsersResponse(success = true))
    }

    override suspend fun blockUser(request: Map<String, String>): Response<GenericResponse> {
        return Response.success(GenericResponse(success = true))
    }

    override suspend fun unblockUser(targetUserId: String): Response<GenericResponse> {
        return Response.success(GenericResponse(success = true))
    }

    override suspend fun getKeywordFilters(): Response<KeywordFiltersResponse> {
        return Response.success(KeywordFiltersResponse(success = true))
    }

    override suspend fun addKeywordFilter(request: Map<String, String>): Response<GenericResponse> {
        return Response.success(GenericResponse(success = true))
    }

    override suspend fun deleteKeywordFilter(keyword: String): Response<GenericResponse> {
        return Response.success(GenericResponse(success = true))
    }

    override suspend fun exportUserData(): Response<DataExportResponse> {
        return Response.success(DataExportResponse(success = true))
    }

    override suspend fun deactivateAccount(): Response<GenericResponse> {
        return Response.success(GenericResponse(success = true))
    }

    override suspend fun deleteAccount(): Response<GenericResponse> {
        return Response.success(GenericResponse(success = true))
    }

    override suspend fun getBankAccounts(): Response<BankAccountsResponse> {
        return Response.success(BankAccountsResponse(success = true, bankAccounts = emptyList()))
    }

    override suspend fun addBankAccount(request: Map<String, Any>): Response<BankAccountsResponse> {
        return Response.success(BankAccountsResponse(success = true, bankAccount = BankAccount()))
    }

    override suspend fun deleteBankAccount(id: String): Response<GenericResponse> {
        return Response.success(GenericResponse(success = true))
    }
}

class AuthViewModelTest {

    @Test
    fun login_blankEmail_returnsErrorState() {
        val fakeApi = FakeApiService()
        val repository = AuthRepository(fakeApi)
        val viewModel = AuthViewModel(repository)

        viewModel.login("", "password123")

        val state = viewModel.loginState.value
        assertTrue(state is LoginUiState.Error)
        assertEquals("Email required", (state as LoginUiState.Error).message)
    }

    @Test
    fun login_successfulApiCall_returnsSuccessResultInRepository() = runBlocking {
        val fakeApi = FakeApiService().apply { shouldSucceed = true }
        val repository = AuthRepository(fakeApi)

        val result = repository.login(LoginRequest("test@example.com", "password123"))

        assertTrue(result is NetworkResult.Success)
        val data = (result as NetworkResult.Success).data
        assertTrue(data.success)
        assertEquals("fake-jwt-token", data.token)
    }
}
