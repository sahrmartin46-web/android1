package c.app.sociamediabackend.ui.main

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import c.app.sociamediabackend.data.api.ApiClient
import c.app.sociamediabackend.data.api.SessionManager
import c.app.sociamediabackend.data.repository.PostRepository
import c.app.sociamediabackend.databinding.FragmentFeedBinding
import kotlinx.coroutines.launch

class FeedFragment : Fragment() {

    private var _binding: FragmentFeedBinding? = null
    private val binding get() = _binding!!

    private lateinit var postAdapter: PostAdapter
    private lateinit var sessionManager: SessionManager
    private lateinit var viewModel: FeedViewModel

    private var currentFeedType = "foryou"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentFeedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())
        val apiService = ApiClient.getService(requireContext())
        val repository = PostRepository(apiService)
        viewModel = FeedViewModel(repository)

        postAdapter = PostAdapter(
            onLikeClicked = { post -> viewModel.likePost(post.id) },
            onCommentClicked = { post ->
                val commentsSheet = CommentsBottomSheetDialogFragment(post.id)
                commentsSheet.show(childFragmentManager, CommentsBottomSheetDialogFragment.TAG)
            },
            onDeleteClicked = { post -> viewModel.deletePost(post.id) }
        )

        val layoutManager = LinearLayoutManager(requireContext())
        binding.rvPosts.layoutManager = layoutManager
        binding.rvPosts.adapter = postAdapter
        binding.rvPosts.setHasFixedSize(true)
        binding.rvPosts.setItemViewCacheSize(20)
        binding.rvPosts.itemAnimator = null

        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(binding.rvPosts)

        binding.rvPosts.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val firstVisible = layoutManager.findFirstVisibleItemPosition()
                    val lastVisible = layoutManager.findLastVisibleItemPosition()
                    val centerPosition = (firstVisible + lastVisible) / 2
                    postAdapter.playVideoAt(centerPosition)

                    val totalItemCount = layoutManager.itemCount
                    if (centerPosition >= totalItemCount - 3) {
                        viewModel.loadMorePosts()
                    }
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val totalItemCount = layoutManager.itemCount
                val lastVisible = layoutManager.findLastVisibleItemPosition()
                if (lastVisible >= totalItemCount - 3) {
                    viewModel.loadMorePosts()
                }
            }
        })

        binding.swipeRefresh.setOnRefreshListener {
            val userId = sessionManager.getUser()?.id
            viewModel.loadPosts(feedType = currentFeedType, currentUserId = userId, isRefresh = true)
        }

        fun updateTabs(selectedType: String) {
            currentFeedType = selectedType
            val activeColor = Color.WHITE
            val inactiveColor = Color.parseColor("#999999")

            binding.tvTabForYou.setTextColor(if (selectedType == "foryou") activeColor else inactiveColor)
            binding.tvTabForYou.textSize = if (selectedType == "foryou") 18f else 16f

            binding.tvTabFollowing.setTextColor(if (selectedType == "following") activeColor else inactiveColor)
            binding.tvTabFollowing.textSize = if (selectedType == "following") 18f else 16f

            binding.tvTabFollowers.setTextColor(if (selectedType == "followers") activeColor else inactiveColor)
            binding.tvTabFollowers.textSize = if (selectedType == "followers") 18f else 16f

            binding.tvTabTrending.setTextColor(if (selectedType == "trending") activeColor else inactiveColor)
            binding.tvTabTrending.textSize = if (selectedType == "trending") 18f else 16f

            val userId = sessionManager.getUser()?.id
            viewModel.loadPosts(feedType = selectedType, currentUserId = userId)
        }

        binding.tvTabForYou.setOnClickListener { updateTabs("foryou") }
        binding.tvTabFollowing.setOnClickListener { updateTabs("following") }
        binding.tvTabFollowers.setOnClickListener { updateTabs("followers") }
        binding.tvTabTrending.setOnClickListener { updateTabs("trending") }

        var isDarkMode = true
        binding.btnThemeToggle.setOnClickListener {
            isDarkMode = !isDarkMode
            if (isDarkMode) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                Toast.makeText(requireContext(), "Dark Mode Enabled 🌙", Toast.LENGTH_SHORT).show()
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                Toast.makeText(requireContext(), "Light Mode Enabled ☀️", Toast.LENGTH_SHORT).show()
            }
        }

        observeViewModel()
        updateTabs("foryou")
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is FeedUiState.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                            binding.swipeRefresh.isRefreshing = false
                        }
                        is FeedUiState.Success -> {
                            binding.progressBar.visibility = View.GONE
                            binding.swipeRefresh.isRefreshing = state.isRefreshing

                            postAdapter.setPosts(state.posts)

                            if (state.posts.isNotEmpty()) {
                                postAdapter.playVideoAt(0)
                                binding.llEmpty.visibility = View.GONE
                            } else {
                                binding.llEmpty.visibility = View.VISIBLE
                            }
                        }
                        is FeedUiState.Error -> {
                            binding.progressBar.visibility = View.GONE
                            binding.swipeRefresh.isRefreshing = false
                            Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        postAdapter.releasePlayers()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        postAdapter.releasePlayers()
        _binding = null
    }
}
