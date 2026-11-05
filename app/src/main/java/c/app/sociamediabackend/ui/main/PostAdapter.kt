package c.app.sociamediabackend.ui.main

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.recyclerview.widget.RecyclerView
import c.app.sociamediabackend.R
import c.app.sociamediabackend.data.api.ApiClient
import c.app.sociamediabackend.data.api.SessionManager
import kotlinx.coroutines.launch
import c.app.sociamediabackend.data.model.Post
import c.app.sociamediabackend.data.model.PostCount
import c.app.sociamediabackend.databinding.ItemPostBinding
import c.app.sociamediabackend.ui.sound.SoundDetailsActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.SimpleDateFormat
import java.util.*

class PostAdapter(
    private val onLikeClicked: (Post) -> Unit,
    private val onCommentClicked: (Post) -> Unit,
    private val onDeleteClicked: (Post) -> Unit
) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    private val posts = mutableListOf<Post>()
    private val playersMap = mutableMapOf<Int, ExoPlayer>()

    fun setPosts(newPosts: List<Post>) {
        releasePlayers()
        posts.clear()
        posts.addAll(newPosts)
        notifyDataSetChanged()
    }

    fun appendPosts(newPosts: List<Post>) {
        val startPosition = posts.size
        posts.addAll(newPosts)
        notifyItemRangeInserted(startPosition, newPosts.size)
    }

    fun updatePostLike(postId: String, isLiked: Boolean) {
        val index = posts.indexOfFirst { it.id == postId }
        if (index != -1) {
            val post = posts[index]
            if (post.count == null) {
                post.count = PostCount(0, 0)
            }
            val currentLikes = post.count?.likes ?: 0
            val newLikes = if (isLiked) currentLikes + 1 else (currentLikes - 1).coerceAtLeast(0)
            post.count?.likes = newLikes
            notifyItemChanged(index)
        }
    }

    fun updatePostCommentCount(postId: String, increment: Int) {
        val index = posts.indexOfFirst { it.id == postId }
        if (index != -1) {
            val post = posts[index]
            if (post.count == null) {
                post.count = PostCount(0, 0)
            }
            val currentComments = post.count?.comments ?: 0
            post.count?.comments = (currentComments + increment).coerceAtLeast(0)
            notifyItemChanged(index)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        binding.root.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(posts[position], position)
    }

    override fun getItemCount(): Int = posts.size

    fun playVideoAt(position: Int) {
        playersMap.forEach { (pos, player) ->
            player.playWhenReady = (pos == position)
        }
    }

    inner class PostViewHolder(private val binding: ItemPostBinding) : RecyclerView.ViewHolder(binding.root) {

        @OptIn(UnstableApi::class)
        fun bind(post: Post, position: Int) {
            val author = post.author
            binding.tvDisplayName.text = author?.displayName ?: author?.username ?: "Anonymous"
            
            val timeAgo = formatRelativeTime(post.createdAt)
            binding.tvUsernameDate.text = "@${author?.username ?: "user"} • $timeAgo"
            
            binding.tvContent.text = post.content

            binding.tvLikesCount.text = (post.count?.likes ?: 0).toString()
            binding.tvCommentsCount.text = (post.count?.comments ?: 0).toString()
            binding.tvLikesCountImg.text = (post.count?.likes ?: 0).toString()
            binding.tvCommentsCountImg.text = (post.count?.comments ?: 0).toString()

            val sessionManager = SessionManager(binding.root.context)
            val currentUser = sessionManager.getUser()

            // Show delete button only if it's the current user's post
            if ((author?.id == currentUser?.id || post.authorId == currentUser?.id) && !currentUser?.id.isNullOrEmpty()) {
                binding.btnDeletePost.visibility = View.VISIBLE
                binding.btnDeletePost.setOnClickListener {
                    MaterialAlertDialogBuilder(binding.root.context)
                        .setTitle("Delete Post")
                        .setMessage("Are you sure you want to delete this post?")
                        .setPositiveButton("Delete") { _, _ ->
                            onDeleteClicked(post)
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                }
            } else {
                binding.btnDeletePost.visibility = View.GONE
            }

            // Resolve avatar URL & Sound Disc Picture
            val resolvedAvatarUrl = resolveUrl(author?.avatarUrl, sessionManager)
            val creatorName = author?.displayName ?: author?.username ?: "Creator"
            binding.tvSoundTitle.text = "Original Sound - $creatorName"

            if (!resolvedAvatarUrl.isNullOrEmpty()) {
                Glide.with(binding.root.context)
                    .load(resolvedAvatarUrl)
                    .placeholder(R.drawable.ic_default_avatar)
                    .error(R.drawable.ic_default_avatar)
                    .circleCrop()
                    .into(binding.ivAvatar)

                Glide.with(binding.root.context)
                    .load(resolvedAvatarUrl)
                    .placeholder(R.drawable.ic_default_avatar)
                    .error(R.drawable.ic_default_avatar)
                    .circleCrop()
                    .into(binding.ivSoundAvatar)

                Glide.with(binding.root.context)
                    .load(resolvedAvatarUrl)
                    .placeholder(R.drawable.ic_default_avatar)
                    .error(R.drawable.ic_default_avatar)
                    .circleCrop()
                    .into(binding.ivSoundDiscAvatar)
            } else {
                binding.ivAvatar.setImageResource(R.drawable.ic_default_avatar)
                binding.ivSoundAvatar.setImageResource(R.drawable.ic_default_avatar)
                binding.ivSoundDiscAvatar.setImageResource(R.drawable.ic_default_avatar)
            }

            val soundClickListener = View.OnClickListener {
                val context = binding.root.context
                val intent = Intent(context, SoundDetailsActivity::class.java).apply {
                    putExtra("SOUND_TITLE", "Original Sound - $creatorName")
                    putExtra("CREATOR_NAME", creatorName)
                }
                context.startActivity(intent)
            }

            binding.llSoundContainer.setOnClickListener(soundClickListener)
            binding.cvSoundDisc.setOnClickListener(soundClickListener)

            // Resolve Image URL
            val resolvedImageUrl = resolveUrl(post.imageUrl, sessionManager)
            if (!resolvedImageUrl.isNullOrEmpty()) {
                binding.ivPostImage.visibility = View.VISIBLE
                binding.playerView.visibility = View.GONE
                val context = binding.root.context

                Glide.with(context)
                    .load(resolvedImageUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop()
                    .into(binding.ivPostImage)
            } else {
                binding.ivPostImage.visibility = View.GONE
            }

            // Resolve Video URL
            val resolvedVideoUrl = resolveUrl(post.videoUrl, sessionManager)
            if (!resolvedVideoUrl.isNullOrEmpty()) {
                binding.playerView.visibility = View.VISIBLE
                binding.ivPostImage.visibility = View.GONE

                // Release existing player at this position if any to prevent OOM
                playersMap[position]?.stop()
                playersMap[position]?.release()

                val context = binding.root.context
                val exoPlayer = ExoPlayer.Builder(context).build().apply {
                    val mediaItem = MediaItem.fromUri(Uri.parse(resolvedVideoUrl))
                    setMediaItem(mediaItem)
                    repeatMode = Player.REPEAT_MODE_ALL
                    prepare()
                }

                playersMap[position] = exoPlayer
                binding.playerView.player = exoPlayer
            } else {
                binding.playerView.visibility = View.GONE
                binding.playerView.player = null
            }

            // Follow Badge Click Action
            if (author?.id == currentUser?.id || currentUser?.id.isNullOrEmpty()) {
                binding.cvFollowBadge.visibility = View.GONE
            } else {
                binding.cvFollowBadge.visibility = View.VISIBLE
            }

            binding.cvFollowBadge.setOnClickListener { v ->
                val targetAuthorId = author?.id ?: return@setOnClickListener
                v.animate().scaleX(1.3f).scaleY(1.3f).setDuration(120).withEndAction {
                    v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(120).start()
                    v.visibility = View.GONE
                }.start()

                (binding.root.context as? AppCompatActivity)?.lifecycleScope?.launch {
                    try {
                        val res = ApiClient.getService(binding.root.context).followUser(targetAuthorId)
                        if (res.isSuccessful) {
                            Toast.makeText(binding.root.context, "Following @${author.username}", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(binding.root.context, "Following @${author.username}", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            // Like Action with scale bounce animation
            val likeClickListener = View.OnClickListener { v ->
                v.animate().scaleX(1.3f).scaleY(1.3f).setDuration(100).withEndAction {
                    v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start()
                }.start()
                onLikeClicked(post)
            }
            binding.btnLikeLayout.setOnClickListener(likeClickListener)
            binding.btnLikeLayoutImg.setOnClickListener(likeClickListener)

            // Comment Action
            binding.btnCommentLayout.setOnClickListener {
                onCommentClicked(post)
            }
            binding.btnCommentLayoutImg.setOnClickListener {
                onCommentClicked(post)
            }

            // Save / Bookmark Action
            var isSaved = false
            binding.btnSaveLayoutImg.setOnClickListener { v ->
                isSaved = !isSaved
                v.animate().scaleX(1.3f).scaleY(1.3f).setDuration(120).withEndAction {
                    v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(120).start()
                }.start()

                if (isSaved) {
                    binding.ivSaveImg.setColorFilter(Color.YELLOW)
                    binding.tvSaveCountImg.text = "Saved"
                    Toast.makeText(binding.root.context, "Saved video to Bookmarks! 🔖", Toast.LENGTH_SHORT).show()
                } else {
                    binding.ivSaveImg.setColorFilter(Color.WHITE)
                    binding.tvSaveCountImg.text = "Save"
                    Toast.makeText(binding.root.context, "Removed from Bookmarks", Toast.LENGTH_SHORT).show()
                }
            }

            // Share & Options Action (Includes Share + Delete if author)
            val shareClickListener = View.OnClickListener {
                val context = binding.root.context
                val isMyPost = author?.id == currentUser?.id && !currentUser?.id.isNullOrEmpty()

                val options = if (isMyPost) {
                    arrayOf("Share Post", "Delete Post")
                } else {
                    arrayOf("Share Post")
                }

                MaterialAlertDialogBuilder(context)
                    .setTitle("Post Options")
                    .setItems(options) { _, which ->
                        if (which == 0) {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "Check out this post")
                                val mediaText = when {
                                    !resolvedVideoUrl.isNullOrEmpty() -> "\nWatch Video: $resolvedVideoUrl"
                                    !resolvedImageUrl.isNullOrEmpty() -> "\nImage: $resolvedImageUrl"
                                    else -> ""
                                }
                                putExtra(Intent.EXTRA_TEXT, "${post.content}$mediaText\n\nShared via Fugen")
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share post via"))
                        } else if (which == 1) {
                            MaterialAlertDialogBuilder(context)
                                .setTitle("Delete Post")
                                .setMessage("Are you sure you want to delete this video/picture post?")
                                .setPositiveButton("Delete") { _, _ ->
                                    onDeleteClicked(post)
                                }
                                .setNegativeButton("Cancel", null)
                                .show()
                        }
                    }
                    .show()
            }

            binding.btnShareLayout.setOnClickListener(shareClickListener)
            binding.btnShareLayoutImg.setOnClickListener(shareClickListener)
        }
    }

    private fun resolveUrl(rawUrl: String?, sessionManager: SessionManager): String? {
        if (rawUrl.isNullOrEmpty()) return null
        val baseUrl = sessionManager.getBaseUrl()
        val serverHost = baseUrl.substringBefore("api/")

        return if (rawUrl.startsWith("http://") || rawUrl.startsWith("https://")) {
            rawUrl
        } else {
            "${serverHost}${rawUrl.removePrefix("/")}"
        }
    }

    private fun formatRelativeTime(dateStr: String?): String {
        if (dateStr.isNullOrEmpty()) return "Just now"
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            val date = sdf.parse(dateStr) ?: return "Just now"
            val diffMs = System.currentTimeMillis() - date.time

            val seconds = diffMs / 1000
            val minutes = seconds / 60
            val hours = minutes / 60
            val days = hours / 24

            when {
                seconds < 60 -> "Just now"
                minutes < 60 -> "${minutes}m ago"
                hours < 24 -> "${hours}h ago"
                else -> "${days}d ago"
            }
        } catch (e: Exception) {
            "Just now"
        }
    }

    fun releasePlayers() {
        playersMap.values.forEach { player ->
            player.stop()
            player.release()
        }
        playersMap.clear()
    }
}
