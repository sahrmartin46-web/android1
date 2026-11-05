package c.app.sociamediabackend.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import c.app.sociamediabackend.R
import c.app.sociamediabackend.data.api.SessionManager
import c.app.sociamediabackend.data.model.Post
import c.app.sociamediabackend.databinding.ItemProfileGridPostBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

class ProfileGridAdapter(
    private val onDeleteClicked: (Post) -> Unit
) : RecyclerView.Adapter<ProfileGridAdapter.ViewHolder>() {

    private val posts = mutableListOf<Post>()

    fun setPosts(newPosts: List<Post>) {
        posts.clear()
        posts.addAll(newPosts)
        notifyDataSetChanged()
    }

    class ViewHolder(val binding: ItemProfileGridPostBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemProfileGridPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post = posts[position]
        val context = holder.binding.root.context
        val sessionManager = SessionManager(context)
        val baseUrl = sessionManager.getBaseUrl().substringBefore("api/")

        val rawUrl = post.imageUrl ?: post.videoUrl
        val resolvedUrl = when {
            rawUrl.isNullOrEmpty() -> ""
            rawUrl.startsWith("http") -> rawUrl
            else -> "$baseUrl${rawUrl.removePrefix("/")}"
        }

        if (resolvedUrl.isNotEmpty()) {
            Glide.with(context)
                .load(resolvedUrl)
                .placeholder(R.drawable.ic_default_avatar)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .into(holder.binding.ivThumbnail)
        } else {
            holder.binding.ivThumbnail.setImageResource(R.drawable.ic_default_avatar)
        }

        holder.binding.tvViews.text = "❤️ ${post.count?.likes ?: 0}"
        holder.binding.root.setOnLongClickListener {
            onDeleteClicked(post)
            true
        }
    }

    override fun getItemCount(): Int = posts.size
}
