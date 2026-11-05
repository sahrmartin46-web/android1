package c.app.sociamediabackend.ui.live

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import c.app.sociamediabackend.data.model.LiveSession
import c.app.sociamediabackend.databinding.ItemLiveStreamBinding

class LiveStreamsAdapter(
    private val onStreamClicked: (LiveSession) -> Unit
) : RecyclerView.Adapter<LiveStreamsAdapter.LiveStreamViewHolder>() {

    private val sessions = mutableListOf<LiveSession>()

    fun setSessions(newList: List<LiveSession>) {
        sessions.clear()
        sessions.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LiveStreamViewHolder {
        val binding = ItemLiveStreamBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LiveStreamViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LiveStreamViewHolder, position: Int) {
        holder.bind(sessions[position])
    }

    override fun getItemCount(): Int = sessions.size

    inner class LiveStreamViewHolder(private val binding: ItemLiveStreamBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(session: LiveSession) {
            binding.tvStreamTitle.text = session.title
            val hostName = session.host?.displayName ?: session.host?.username ?: "Creator"
            binding.tvHostName.text = "@$hostName"
            binding.tvStreamViewers.text = "👁 ${session.totalViewers ?: 1}"

            binding.root.setOnClickListener {
                onStreamClicked(session)
            }
        }
    }
}
