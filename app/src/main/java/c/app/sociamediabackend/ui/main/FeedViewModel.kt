package c.app.sociamediabackend.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import c.app.sociamediabackend.data.api.NetworkResult
import c.app.sociamediabackend.data.model.Post
import c.app.sociamediabackend.data.repository.PostRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface FeedUiState {
    object Loading : FeedUiState
    data class Success(
        val posts: List<Post>,
        val nextCursor: String?,
        val isLoadingMore: Boolean = false,
        val isRefreshing: Boolean = false
    ) : FeedUiState
    data class Error(val message: String) : FeedUiState
}

class FeedViewModel(private val postRepository: PostRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<FeedUiState>(FeedUiState.Loading)
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    private var currentFeedType: String = "foryou"
    private var currentUserId: String? = null

    fun loadPosts(feedType: String = currentFeedType, currentUserId: String? = this.currentUserId, isRefresh: Boolean = false) {
        this.currentFeedType = feedType
        this.currentUserId = currentUserId

        val currentState = _uiState.value
        if (isRefresh && currentState is FeedUiState.Success) {
            _uiState.value = currentState.copy(isRefreshing = true)
        } else if (!isRefresh) {
            _uiState.value = FeedUiState.Loading
        }

        viewModelScope.launch {
            when (val result = postRepository.getPosts(feedType = feedType, currentUserId = currentUserId)) {
                is NetworkResult.Success -> {
                    _uiState.value = FeedUiState.Success(
                        posts = result.data.posts,
                        nextCursor = result.data.nextCursor
                    )
                }
                is NetworkResult.Error -> {
                    _uiState.value = FeedUiState.Error(result.message)
                }
                is NetworkResult.Exception -> {
                    _uiState.value = FeedUiState.Error(result.throwable.localizedMessage ?: "Failed to load posts")
                }
            }
        }
    }

    fun loadMorePosts() {
        val currentState = _uiState.value
        if (currentState !is FeedUiState.Success || currentState.isLoadingMore || currentState.nextCursor == null) {
            return
        }

        _uiState.value = currentState.copy(isLoadingMore = true)

        viewModelScope.launch {
            when (val result = postRepository.getPosts(
                feedType = currentFeedType,
                currentUserId = currentUserId,
                cursor = currentState.nextCursor
            )) {
                is NetworkResult.Success -> {
                    val updatedPosts = currentState.posts + result.data.posts
                    _uiState.value = FeedUiState.Success(
                        posts = updatedPosts,
                        nextCursor = result.data.nextCursor
                    )
                }
                is NetworkResult.Error -> {
                    _uiState.value = currentState.copy(isLoadingMore = false)
                }
                is NetworkResult.Exception -> {
                    _uiState.value = currentState.copy(isLoadingMore = false)
                }
            }
        }
    }

    fun likePost(postId: String) {
        viewModelScope.launch {
            postRepository.likePost(postId)
        }
    }

    fun deletePost(postId: String) {
        val currentState = _uiState.value
        if (currentState is FeedUiState.Success) {
            val updatedPosts = currentState.posts.filterNot { it.id == postId }
            _uiState.value = currentState.copy(posts = updatedPosts)
        }
        viewModelScope.launch {
            postRepository.deletePost(postId)
        }
    }
}
