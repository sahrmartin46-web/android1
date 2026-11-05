package c.app.sociamediabackend.ui.live

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import c.app.sociamediabackend.data.model.Gift
import c.app.sociamediabackend.databinding.ItemGiftBinding

class GiftsAdapter(
    private val onGiftSelected: (Gift) -> Unit
) : RecyclerView.Adapter<GiftsAdapter.GiftViewHolder>() {

    private val gifts = mutableListOf<Gift>()

    fun setGifts(newList: List<Gift>) {
        gifts.clear()
        gifts.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GiftViewHolder {
        val binding = ItemGiftBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GiftViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GiftViewHolder, position: Int) {
        holder.bind(gifts[position])
    }

    override fun getItemCount(): Int = gifts.size

    inner class GiftViewHolder(private val binding: ItemGiftBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(gift: Gift) {
            binding.tvGiftIcon.text = gift.iconUrl
            binding.tvGiftName.text = gift.name
            binding.tvGiftCost.text = "🪙 ${gift.coinCost}"

            binding.root.setOnClickListener {
                onGiftSelected(gift)
            }
        }
    }
}
