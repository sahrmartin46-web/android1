package c.app.sociamediabackend.ui.live

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import c.app.sociamediabackend.data.api.ApiClient
import c.app.sociamediabackend.data.model.Gift
import c.app.sociamediabackend.databinding.DialogGiftsBinding
import c.app.sociamediabackend.ui.sound.BuyCoinsActivity
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch

class GiftBottomSheetDialogFragment(
    private val liveId: String,
    private val onGiftSent: (Gift) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: DialogGiftsBinding? = null
    private val binding get() = _binding!!

    private lateinit var giftsAdapter: GiftsAdapter
    private var currentCoins = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DialogGiftsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        giftsAdapter = GiftsAdapter { gift ->
            sendGift(gift)
        }

        binding.rvGifts.layoutManager = GridLayoutManager(requireContext(), 4)
        binding.rvGifts.adapter = giftsAdapter

        binding.btnBuyCoins.setOnClickListener {
            val intent = Intent(requireContext(), BuyCoinsActivity::class.java)
            startActivity(intent)
        }

        loadWallet()
        loadGifts()
    }

    private fun loadWallet() {
        lifecycleScope.launch {
            try {
                val res = ApiClient.getService(requireContext()).getWallet()
                if (res.isSuccessful && res.body()?.success == true) {
                    currentCoins = res.body()?.wallet?.coinBalance ?: 0
                    binding.tvCoinsBalance.text = "🪙 $currentCoins"
                } else {
                    binding.tvCoinsBalance.text = "🪙 0"
                }
            } catch (e: Exception) {
                binding.tvCoinsBalance.text = "🪙 0"
            }
        }
    }

    private fun loadGifts() {
        lifecycleScope.launch {
            try {
                val res = ApiClient.getService(requireContext()).getGiftCatalog()
                if (res.isSuccessful && res.body()?.success == true) {
                    val giftsList = res.body()?.gifts.orEmpty()
                    giftsAdapter.setGifts(giftsList)
                } else {
                    giftsAdapter.setGifts(emptyList())
                    Toast.makeText(requireContext(), "Failed to load gifts", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                giftsAdapter.setGifts(emptyList())
                Toast.makeText(requireContext(), "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendGift(gift: Gift) {
        if (currentCoins < gift.coinCost) {
            Toast.makeText(requireContext(), "Insufficient coins. Tap + Buy to get more!", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val req = mapOf<String, Any>(
                    "liveId" to liveId,
                    "giftSlug" to gift.slug,
                    "quantity" to 1
                )
                val res = ApiClient.getService(requireContext()).sendGift(req)
                if (res.isSuccessful && res.body()?.success == true) {
                    currentCoins = res.body()?.wallet?.coinBalance ?: (currentCoins - gift.coinCost)
                    binding.tvCoinsBalance.text = "🪙 $currentCoins"
                    Toast.makeText(requireContext(), "Sent ${gift.name} ${gift.iconUrl}!", Toast.LENGTH_SHORT).show()
                    onGiftSent(gift)
                    dismiss()
                } else {
                    Toast.makeText(requireContext(), res.body()?.message ?: "Failed to send gift", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error sending gift: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "GiftBottomSheet"
    }
}
