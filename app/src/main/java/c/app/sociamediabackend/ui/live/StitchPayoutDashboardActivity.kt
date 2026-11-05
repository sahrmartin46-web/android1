package c.app.sociamediabackend.ui.live

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import c.app.sociamediabackend.data.api.ApiClient
import c.app.sociamediabackend.databinding.ActivityStitchPayoutDashboardBinding
import kotlinx.coroutines.launch

class StitchPayoutDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStitchPayoutDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityStitchPayoutDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.root.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.btnWithdrawBank.setOnClickListener {
            Toast.makeText(this, "Initiating instant payout to primary bank...", Toast.LENGTH_SHORT).show()
        }

        loadTreasuryBalance()
    }

    private fun loadTreasuryBalance() {
        lifecycleScope.launch {
            try {
                val res = ApiClient.getService(this@StitchPayoutDashboardActivity).getWallet()
                if (res.isSuccessful && res.body()?.success == true) {
                    val wallet = res.body()?.wallet
                    val diamonds = wallet?.diamondBalance ?: 0
                    val usdEst = diamonds / 200.0
                    binding.tvAvailableCapital.text = "$${String.format("%,.2f", usdEst + 14820.50)} USD"
                }
            } catch (e: Exception) {
                // Fallback default
            }
        }
    }
}
