package c.app.sociamediabackend.ui.live

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import c.app.sociamediabackend.databinding.DialogLiveSummaryBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class LiveSummaryDialogFragment(
    private val onDone: () -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: DialogLiveSummaryBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DialogLiveSummaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnDoneSummary.setOnClickListener {
            onDone()
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "LiveSummaryDialog"
    }
}
