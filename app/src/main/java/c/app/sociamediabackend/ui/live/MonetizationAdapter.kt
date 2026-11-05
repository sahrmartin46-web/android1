package c.app.sociamediabackend.ui.live

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import c.app.sociamediabackend.data.model.MonetizationProgram
import c.app.sociamediabackend.databinding.ItemMonetizationProgramBinding

class MonetizationAdapter(
    private val onProgramClicked: (MonetizationProgram) -> Unit
) : RecyclerView.Adapter<MonetizationAdapter.MonetizationViewHolder>() {

    private val programs = mutableListOf<MonetizationProgram>()

    fun setPrograms(newList: List<MonetizationProgram>) {
        programs.clear()
        programs.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MonetizationViewHolder {
        val binding = ItemMonetizationProgramBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MonetizationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MonetizationViewHolder, position: Int) {
        holder.bind(programs[position])
    }

    override fun getItemCount(): Int = programs.size

    inner class MonetizationViewHolder(private val binding: ItemMonetizationProgramBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(program: MonetizationProgram) {
            binding.tvProgramIcon.text = program.icon
            binding.tvProgramTitle.text = program.title
            binding.tvPayoutRate.text = program.payoutRate
            binding.tvStatusBadge.text = program.status
            binding.tvProgramDesc.text = program.description
            binding.tvRequirement.text = "Requirement: ${program.requirement}"

            binding.btnManageProgram.setOnClickListener {
                onProgramClicked(program)
            }
        }
    }
}
