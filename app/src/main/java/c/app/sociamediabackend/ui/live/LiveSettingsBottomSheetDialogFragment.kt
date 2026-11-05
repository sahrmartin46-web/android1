package c.app.sociamediabackend.ui.live

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import c.app.sociamediabackend.R
import c.app.sociamediabackend.databinding.DialogLiveSettingsBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class LiveSettingsBottomSheetDialogFragment(
    private val onLayoutSelected: ((String) -> Unit)? = null
) : BottomSheetDialogFragment() {

    private var _binding: DialogLiveSettingsBinding? = null
    private val binding get() = _binding!!

    private var selectedLayoutMode = "Side by Side"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DialogLiveSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.chipGroupNavLayouts.setOnCheckedChangeListener { _, checkedId ->
            selectedLayoutMode = when (checkedId) {
                R.id.chipSideBySide -> "Side by Side"
                R.id.chipSpotlight -> "Spotlight"
                R.id.chipRoundtable1 -> "Roundtable I"
                R.id.chipRoundtable2 -> "Roundtable II"
                else -> "Side by Side"
            }
        }

        binding.btnSaveSettings.setOnClickListener {
            onLayoutSelected?.invoke(selectedLayoutMode)
            Toast.makeText(requireContext(), "Live Grid Layout set to $selectedLayoutMode", Toast.LENGTH_SHORT).show()
            dismiss()
        }

        binding.btnCloseSettings.setOnClickListener {
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "LiveSettingsBottomSheet"
    }
}
