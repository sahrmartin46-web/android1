package c.app.sociamediabackend.ui.main

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import c.app.sociamediabackend.R
import c.app.sociamediabackend.data.api.ApiClient
import c.app.sociamediabackend.data.api.SessionManager
import c.app.sociamediabackend.databinding.FragmentDiscoverBinding
import c.app.sociamediabackend.ui.sound.SoundDetailsActivity
import kotlinx.coroutines.launch

class DiscoverFragment : Fragment() {

    private var _binding: FragmentDiscoverBinding? = null
    private val binding get() = _binding!!

    private var currentCategory = "Users"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDiscoverBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvDiscoverResults.layoutManager = LinearLayoutManager(requireContext())

        binding.chipGroupDiscover.setOnCheckedChangeListener { _, checkedId ->
            currentCategory = when (checkedId) {
                R.id.chipUsers -> "Users"
                R.id.chipVideos -> "Videos"
                R.id.chipHashtags -> "Hashtags"
                R.id.chipSounds -> "Sounds"
                R.id.chipTopics -> "Topics"
                else -> "Users"
            }
            binding.tvCategoryHeader.text = "DISCOVER ${currentCategory.uppercase()}"
            performSearch(binding.etSearch.text?.toString().orEmpty())
        }

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                performSearch(s?.toString().orEmpty())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        performSearch("")
    }

    private fun performSearch(query: String) {
        lifecycleScope.launch {
            try {
                val response = ApiClient.getService(requireContext()).getPosts(feedType = "foryou", limit = 15)
                if (response.isSuccessful && response.body()?.success == true) {
                    val posts = response.body()?.posts.orEmpty()
                    val adapter = PostAdapter(
                        onLikeClicked = {},
                        onCommentClicked = { p ->
                            val commentsSheet = CommentsBottomSheetDialogFragment(p.id)
                            commentsSheet.show(childFragmentManager, CommentsBottomSheetDialogFragment.TAG)
                        },
                        onDeleteClicked = {}
                    )
                    binding.rvDiscoverResults.adapter = adapter

                    val filtered = if (query.isEmpty()) posts else posts.filter {
                        it.content.contains(query, ignoreCase = true) ||
                        (it.author?.username?.contains(query, ignoreCase = true) == true)
                    }
                    adapter.setPosts(filtered)
                }
            } catch (e: Exception) {
                // Search handling
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
