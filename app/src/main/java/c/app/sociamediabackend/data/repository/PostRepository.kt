package c.app.sociamediabackend.data.repository

import c.app.sociamediabackend.data.api.ApiService
import c.app.sociamediabackend.data.api.NetworkResult
import c.app.sociamediabackend.data.api.safeApiCall
import c.app.sociamediabackend.data.model.CommentsResponse
import c.app.sociamediabackend.data.model.CreatePostRequest
import c.app.sociamediabackend.data.model.GenericResponse
import c.app.sociamediabackend.data.model.PostsResponse

class PostRepository(private val apiService: ApiService) {

    suspend fun getPosts(
        feedType: String = "foryou",
        currentUserId: String? = null,
        cursor: String? = null,
        page: Int = 1,
        limit: Int = 20
    ): NetworkResult<PostsResponse> {
        return safeApiCall {
            apiService.getPosts(
                feedType = feedType,
                currentUserId = currentUserId,
                cursor = cursor,
                page = page,
                limit = limit
            )
        }
    }

    suspend fun createPost(request: CreatePostRequest): NetworkResult<GenericResponse> {
        return safeApiCall { apiService.createPost(request) }
    }

    suspend fun deletePost(postId: String): NetworkResult<GenericResponse> {
        return safeApiCall { apiService.deletePost(postId) }
    }

    suspend fun likePost(postId: String): NetworkResult<GenericResponse> {
        return safeApiCall { apiService.likePost(postId) }
    }

    suspend fun getComments(postId: String): NetworkResult<CommentsResponse> {
        return safeApiCall { apiService.getComments(postId) }
    }

    suspend fun createComment(postId: String, content: String): NetworkResult<GenericResponse> {
        return safeApiCall { apiService.createComment(postId, mapOf("content" to content)) }
    }
}
