package c.app.sociamediabackend.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import c.app.sociamediabackend.data.api.ApiClient
import c.app.sociamediabackend.data.api.SessionManager
import c.app.sociamediabackend.data.model.Comment
import c.app.sociamediabackend.databinding.DialogCommentsBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch

class CommentsBottomSheetDialogFragment(
    private val postId: String
) : BottomSheetDialogFragment() {

    private var _binding: DialogCommentsBinding? = null
    private val binding get() = _binding!!

    private lateinit var commentsAdapter: CommentsAdapter
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DialogCommentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())
        val currentUser = sessionManager.getUser()

        commentsAdapter = CommentsAdapter(currentUser?.id) { comment: Comment ->
            deleteComment(comment.id)
        }

        binding.rvComments.layoutManager = LinearLayoutManager(requireContext())
        binding.rvComments.adapter = commentsAdapter

        binding.btnSend.setOnClickListener {
            val content = binding.etComment.text?.toString()?.trim().orEmpty()
            if (content.isNotEmpty()) {
                sendComment(content)
            }
        }

        loadComments()
    }

    private fun loadComments() {
        lifecycleScope.launch {
            try {
                val response = ApiClient.getService(requireContext()).getComments(postId)
                if (response.isSuccessful && response.body()?.success == true) {
                    val comments = response.body()?.comments.orEmpty()
                    commentsAdapter.setComments(comments)
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Failed to load comments", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendComment(content: String) {
        lifecycleScope.launch {
            try {
                val req = mapOf("content" to content)
                val response = ApiClient.getService(requireContext()).createComment(postId, req)
                if (response.isSuccessful && response.body()?.success == true) {
                    binding.etComment.text?.clear()
                    loadComments()
                } else {
                    Toast.makeText(requireContext(), "Failed to add comment", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deleteComment(commentId: String) {
        lifecycleScope.launch {
            try {
                val response = ApiClient.getService(requireContext()).deleteComment(commentId)
                if (response.isSuccessful && response.body()?.success == true) {
                    loadComments()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Failed to delete comment", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "CommentsBottomSheet"
    }
}
