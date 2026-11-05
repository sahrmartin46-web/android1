package c.app.sociamediabackend.ui.sound

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import c.app.sociamediabackend.R
import c.app.sociamediabackend.databinding.ItemSoundVideoBinding
import com.bumptech.glide.Glide

data class SoundVideoItem(
    val id: String,
    val views: String,
    val handle: String,
    val imageUrl: String? = null
)

class SoundVideosAdapter(
    private val videos: List<SoundVideoItem>,
    private val onItemClick: (SoundVideoItem) -> Unit
) : RecyclerView.Adapter<SoundVideosAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemSoundVideoBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSoundVideoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val video = videos[position]
        holder.binding.tvViews.text = video.views
        holder.binding.tvHandle.text = video.handle

        val url = video.imageUrl
        if (!url.isNullOrEmpty()) {
            Glide.with(holder.binding.ivThumbnail.context)
                .load(url)
                .placeholder(R.drawable.ic_default_avatar)
                .centerCrop()
                .into(holder.binding.ivThumbnail)
        } else {
            holder.binding.ivThumbnail.setImageResource(R.drawable.ic_default_avatar)
        }

        holder.binding.root.setOnClickListener { onItemClick(video) }
    }

    override fun getItemCount(): Int = videos.size
}
