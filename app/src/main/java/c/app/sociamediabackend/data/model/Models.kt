package c.app.sociamediabackend.data.model

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("id") val id: String = "",
    @SerializedName("username") val username: String = "",
    @SerializedName("email") val email: String? = null,
    @SerializedName("displayName") val displayName: String? = null,
    @SerializedName("bio") val bio: String? = null,
    @SerializedName("avatarUrl") val avatarUrl: String? = null,
    @SerializedName("coverUrl") val coverUrl: String? = null,
    @SerializedName("role") val role: String = "USER",
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("_count") val count: UserCount? = null
)

data class UserCount(
    @SerializedName("posts") val posts: Int = 0,
    @SerializedName("followers") val followers: Int = 0,
    @SerializedName("following") val following: Int = 0
)

data class PostAuthor(
    @SerializedName("id") val id: String = "",
    @SerializedName("username") val username: String = "",
    @SerializedName("displayName") val displayName: String? = null,
    @SerializedName("avatarUrl") val avatarUrl: String? = null
)

data class PostCount(
    @SerializedName("likes") var likes: Int = 0,
    @SerializedName("comments") var comments: Int = 0
)

data class Post(
    @SerializedName("id") val id: String = "",
    @SerializedName("content") val content: String = "",
    @SerializedName("imageUrl") val imageUrl: String? = null,
    @SerializedName("videoUrl") val videoUrl: String? = null,
    @SerializedName("views") val views: Int = 0,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("authorId") val authorId: String? = null,
    @SerializedName("author") val author: PostAuthor? = null,
    @SerializedName("_count") var count: PostCount? = null
)

data class Comment(
    @SerializedName("id") val id: String = "",
    @SerializedName("content") val content: String = "",
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("user") val user: PostAuthor? = null
)

data class CommentsResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("comments") val comments: List<Comment> = emptyList()
)

data class AuthResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String = "",
    @SerializedName("token") val token: String? = null,
    @SerializedName("user") val user: User? = null
)

data class UploadResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String = "",
    @SerializedName("url") val url: String? = null
)

data class GenericResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String = "",
    @SerializedName("liked") val liked: Boolean? = null
)

data class PostsResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("nextCursor") val nextCursor: String? = null,
    @SerializedName("posts") val posts: List<Post> = emptyList(),
    @SerializedName("total") val total: Int = 0
)

data class ProfileResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("user") val user: User? = null
)

data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class RegisterRequest(
    @SerializedName("username") val username: String,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("displayName") val displayName: String? = null
)

data class CreatePostRequest(
    @SerializedName("content") val content: String,
    @SerializedName("imageUrl") val imageUrl: String? = null,
    @SerializedName("videoUrl") val videoUrl: String? = null
)

data class BankAccount(
    @SerializedName("id") val id: String = "",
    @SerializedName("region") val region: String = "US",
    @SerializedName("accountHolder") val accountHolder: String = "",
    @SerializedName("bankName") val bankName: String = "",
    @SerializedName("accountNumber") val accountNumber: String = "",
    @SerializedName("routingNumber") val routingNumber: String? = null,
    @SerializedName("iban") val iban: String? = null,
    @SerializedName("swiftBic") val swiftBic: String? = null,
    @SerializedName("isPrimary") val isPrimary: Boolean = false
)

data class BankAccountsResponse(
    @SerializedName("success") val success: Boolean = true,
    @SerializedName("bankAccounts") val bankAccounts: List<BankAccount> = emptyList(),
    @SerializedName("bankAccount") val bankAccount: BankAccount? = null,
    @SerializedName("message") val message: String? = null
)

