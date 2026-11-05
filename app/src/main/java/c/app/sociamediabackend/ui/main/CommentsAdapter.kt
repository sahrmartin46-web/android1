package c.app.sociamediabackend.ui.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import c.app.sociamediabackend.R
import c.app.sociamediabackend.data.model.Comment
import c.app.sociamediabackend.databinding.ItemCommentBinding
import com.bumptech.glide.Glide

class CommentsAdapter(
    private val currentUserId: String?,
    private val onDeleteClicked: (Comment) -> Unit
) : RecyclerView.Adapter<CommentsAdapter.CommentViewHolder>() {

    private val comments = mutableListOf<Comment>()

    fun setComments(newComments: List<Comment>) {
        comments.clear()
        comments.addAll(newComments)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val binding = ItemCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CommentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.bind(comments[position])
    }

    override fun getItemCount(): Int = comments.size

    inner class CommentViewHolder(private val binding: ItemCommentBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(comment: Comment) {
            val user = comment.user
            binding.tvDisplayName.text = user?.displayName ?: user?.username ?: "Anonymous"
            binding.tvContent.text = comment.content

            if (!user?.avatarUrl.isNullOrEmpty()) {
                Glide.with(binding.root.context)
                    .load(user?.avatarUrl)
                    .placeholder(R.drawable.ic_default_avatar)
                    .circleCrop()
                    .into(binding.ivAvatar)
            } else {
                binding.ivAvatar.setImageResource(R.drawable.ic_default_avatar)
            }

            if (user?.id == currentUserId && !currentUserId.isNullOrEmpty()) {
                binding.btnDelete.visibility = View.VISIBLE
                binding.btnDelete.setOnClickListener { onDeleteClicked(comment) }
            } else {
                binding.btnDelete.visibility = View.GONE
            }
        }
    }
}
