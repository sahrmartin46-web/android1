package c.app.sociamediabackend.ui.live

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import c.app.sociamediabackend.R
import c.app.sociamediabackend.databinding.ItemLiveParticipantBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

class LiveGridAdapter(
    private val onParticipantClicked: (LiveParticipant) -> Unit,
    private val onParticipantLongClicked: (LiveParticipant) -> Unit,
    private val onInviteClicked: () -> Unit
) : RecyclerView.Adapter<LiveGridAdapter.ViewHolder>() {

    private val participants = mutableListOf<LiveParticipant>()

    fun setParticipants(newParticipants: List<LiveParticipant>) {
        participants.clear()
        participants.addAll(newParticipants)
        notifyDataSetChanged()
    }

    class ViewHolder(val binding: ItemLiveParticipantBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLiveParticipantBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val p = participants[position]
        val context = holder.binding.root.context

        if (p.isInviteSlot) {
            holder.binding.tvName.text = "Invite Guest"
            holder.binding.tvBadge.visibility = View.GONE
            holder.binding.ivAvatar.setImageResource(R.drawable.ic_default_avatar)
            holder.binding.root.setOnClickListener { onInviteClicked() }
            holder.binding.root.setOnLongClickListener(null)
            return
        }

        holder.binding.tvName.text = p.name
        holder.binding.tvBadge.visibility = if (p.isHost) View.VISIBLE else View.GONE
        if (p.isHost) holder.binding.tvBadge.text = "👑 HOST"

        if (p.avatarUrl.isNotEmpty()) {
            Glide.with(context)
                .load(p.avatarUrl)
                .placeholder(R.drawable.ic_default_avatar)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .into(holder.binding.ivAvatar)
        } else {
            holder.binding.ivAvatar.setImageResource(R.drawable.ic_default_avatar)
        }

        if (p.isActiveSpeaker) {
            holder.binding.cardContainer.strokeColor = Color.parseColor("#25F4EE")
            holder.binding.cardContainer.strokeWidth = 3
        } else {
            holder.binding.cardContainer.strokeColor = Color.parseColor("#33FFFFFF")
            holder.binding.cardContainer.strokeWidth = 1
        }

        holder.binding.root.setOnClickListener { onParticipantClicked(p) }
        holder.binding.root.setOnLongClickListener {
            onParticipantLongClicked(p)
            true
        }
    }

    override fun getItemCount(): Int = participants.size
}
