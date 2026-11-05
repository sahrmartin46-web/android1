package c.app.sociamediabackend.ui.live

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import c.app.sociamediabackend.data.api.ApiClient
import c.app.sociamediabackend.databinding.ActivityMonetizationBinding
import kotlinx.coroutines.launch

class MonetizationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMonetizationBinding
    private lateinit var monetizationAdapter: MonetizationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMonetizationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.root.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        binding.btnOpenWallet.setOnClickListener {
            startActivity(Intent(this, CreatorWalletActivity::class.java))
        }

        monetizationAdapter = MonetizationAdapter { program ->
            val intent = Intent(this, MonetizationDetailActivity::class.java).apply {
                putExtra("PROGRAM_ID", program.id)
                putExtra("TITLE", program.title)
                putExtra("ICON", program.icon)
                putExtra("CATEGORY", program.category)
                putExtra("PAYOUT", program.payoutRate)
                putExtra("DESC", program.description)
                putExtra("REQ", program.requirement)
            }
            startActivity(intent)
        }

        binding.rvMonetizationPrograms.layoutManager = LinearLayoutManager(this)
        binding.rvMonetizationPrograms.adapter = monetizationAdapter

        loadWalletSummary()
        loadMonetizationPrograms()
    }

    private fun loadWalletSummary() {
        lifecycleScope.launch {
            try {
                val res = ApiClient.getService(this@MonetizationActivity).getWallet()
                if (res.isSuccessful && res.body()?.success == true) {
                    val wallet = res.body()?.wallet
                    val diamonds = wallet?.diamondBalance ?: 0
                    binding.tvDiamondsCount.text = "Withdrawable Diamonds: $diamonds 💎"
                    val usdEst = (diamonds / 200.0)
                    binding.tvTotalEarningsUsd.text = "$${String.format("%.2f", usdEst)} USD"
                } else {
                    binding.tvDiamondsCount.text = "Withdrawable Diamonds: 0 💎"
                    binding.tvTotalEarningsUsd.text = "$0.00 USD"
                }
            } catch (e: Exception) {
                binding.tvDiamondsCount.text = "Withdrawable Diamonds: 0 💎"
                binding.tvTotalEarningsUsd.text = "$0.00 USD"
            }
        }
    }

    private fun loadMonetizationPrograms() {
        lifecycleScope.launch {
            try {
                val res = ApiClient.getService(this@MonetizationActivity).getMonetizationPrograms()
                if (res.isSuccessful && res.body()?.success == true) {
                    val programs = res.body()?.programs.orEmpty()
                    monetizationAdapter.setPrograms(programs)
                    if (programs.isEmpty()) {
                        Toast.makeText(this@MonetizationActivity, "No active monetization programs available", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    monetizationAdapter.setPrograms(emptyList())
                    Toast.makeText(this@MonetizationActivity, "Failed to load monetization programs", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                monetizationAdapter.setPrograms(emptyList())
                Toast.makeText(this@MonetizationActivity, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
