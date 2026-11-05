package c.app.sociamediabackend.ui.live

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import c.app.sociamediabackend.data.api.ApiClient
import c.app.sociamediabackend.databinding.ActivityCreatorWalletBinding
import kotlinx.coroutines.launch

class CreatorWalletActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreatorWalletBinding
    private var diamondBalance = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityCreatorWalletBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.root.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        binding.btnSubmitWithdrawal.setOnClickListener {
            submitWithdrawal()
        }

        binding.btnStitchDashboard.setOnClickListener {
            startActivity(Intent(this, FugenPayoutDashboardActivity::class.java))
        }

        loadWallet()
    }

    private fun loadWallet() {
        lifecycleScope.launch {
            try {
                val res = ApiClient.getService(this@CreatorWalletActivity).getWallet()
                if (res.isSuccessful && res.body()?.success == true) {
                    val wallet = res.body()?.wallet
                    diamondBalance = wallet?.diamondBalance ?: 0
                    binding.tvDiamondsBalance.text = "$diamondBalance 💎"
                    val usdEst = (diamondBalance / 200.0)
                    binding.tvEstimatedUsd.text = "Estimated Earnings: $${String.format("%.2f", usdEst)} USD"
                }
            } catch (e: Exception) {
                binding.tvDiamondsBalance.text = "0 💎"
            }
        }
    }

    private fun submitWithdrawal() {
        val diamondsStr = binding.etDiamondsInput.text?.toString()?.trim().orEmpty()
        val method = binding.etPayoutMethod.text?.toString()?.trim().orEmpty()
        val details = binding.etAccountDetails.text?.toString()?.trim().orEmpty()

        val diamonds = diamondsStr.toIntOrNull() ?: 0
        if (diamonds < 100) {
            Toast.makeText(this, "Minimum withdrawal is 100 Diamonds", Toast.LENGTH_SHORT).show()
            return
        }

        if (method.isEmpty() || details.isEmpty()) {
            Toast.makeText(this, "Please enter payout method and account details", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val req = mapOf<String, Any>(
                    "diamondsAmount" to diamonds,
                    "payoutProvider" to method,
                    "destination" to details
                )
                val res = ApiClient.getService(this@CreatorWalletActivity).requestWithdrawal(req)
                if (res.isSuccessful && res.body()?.success == true) {
                    Toast.makeText(this@CreatorWalletActivity, res.body()?.message ?: "Withdrawal processed!", Toast.LENGTH_LONG).show()
                    loadWallet()
                } else {
                    Toast.makeText(this@CreatorWalletActivity, "Withdrawal failed", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@CreatorWalletActivity, "Withdrawal error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
