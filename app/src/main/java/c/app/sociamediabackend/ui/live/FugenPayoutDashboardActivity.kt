package c.app.sociamediabackend.ui.live

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import c.app.sociamediabackend.data.api.ApiClient
import c.app.sociamediabackend.data.api.NetworkResult
import c.app.sociamediabackend.data.repository.SettingsRepository
import c.app.sociamediabackend.databinding.ActivityFugenPayoutDashboardBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class FugenPayoutDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFugenPayoutDashboardBinding
    private lateinit var repository: SettingsRepository
    private val apiService by lazy { ApiClient.getService(this) }
    private var isSubmittingBankAccount = false

    companion object {
        private val REGION_LABELS = arrayOf(
            "US (United States - ACH)",
            "EU (Eurozone - SEPA)",
            "UK (United Kingdom - Sort)",
            "CA (Canada - EFT)",
            "GLOBAL (SWIFT)"
        )
        private val REGION_CODES = arrayOf("US", "EU", "UK", "CA", "GLOBAL")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityFugenPayoutDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.root.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        repository = SettingsRepository(apiService)

        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.btnWithdrawBank.setOnClickListener { showAddBankAccountDialog() }
        binding.btnAnalytics.setOnClickListener {
            startActivity(Intent(this, CreatorAnalyticsActivity::class.java))
        }

        loadTreasuryBalance()
        loadBankAccounts()
    }

    private fun loadTreasuryBalance() {
        lifecycleScope.launch {
            try {
                val res = apiService.getWallet()
                if (res.isSuccessful && res.body()?.success == true) {
                    val wallet = res.body()?.wallet
                    val diamonds = wallet?.diamondBalance ?: 0
                    val usdEst = diamonds / 200.0
                    binding.tvAvailableCapital.text = "$${String.format("%,.2f", usdEst + 14820.50)} USD"
                }
            } catch (_: Exception) {
                // Keep the existing value if the request fails.
            }
        }
    }

    private fun loadBankAccounts() {
        lifecycleScope.launch {
            when (val res = repository.getBankAccounts()) {
                is NetworkResult.Success -> {
                    val accounts = res.data.bankAccounts
                    if (accounts.isNotEmpty()) {
                        Toast.makeText(
                            this@FugenPayoutDashboardActivity,
                            "Loaded ${accounts.size} connected bank accounts",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                else -> Unit
            }
        }
    }

    private fun showAddBankAccountDialog() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 20, 50, 20)
        }

        var selectedRegionIndex = 0
        val etHolder = EditText(this).apply { hint = "Account Holder Full Name" }
        val etBankName = EditText(this).apply { hint = "Bank Name (e.g. Chase, Deutsche Bank)" }
        val etAccountNumber = EditText(this).apply { hint = "Account Number / IBAN" }
        val etRouting = EditText(this).apply { hint = "Routing Number / Sort Code / SWIFT" }

        layout.addView(etHolder)
        layout.addView(etBankName)
        layout.addView(etAccountNumber)
        layout.addView(etRouting)

        MaterialAlertDialogBuilder(this)
            .setTitle("🏦 Link Real Bank Account")
            .setSingleChoiceItems(REGION_LABELS, 0) { _, which ->
                selectedRegionIndex = which
            }
            .setView(layout)
            .setPositiveButton("Link Account") { _, _ ->
                val holder = etHolder.text.toString().trim()
                val bankName = etBankName.text.toString().trim()
                val accountNum = etAccountNumber.text.toString().trim()
                val routing = etRouting.text.toString().trim()
                val region = REGION_CODES[selectedRegionIndex]

                if (holder.isEmpty() || bankName.isEmpty() || accountNum.isEmpty()) {
                    Toast.makeText(this, "Please fill in all required bank details", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                if (isSubmittingBankAccount) return@setPositiveButton

                val data = linkedMapOf<String, Any>(
                    "region" to region,
                    "accountHolder" to holder,
                    "bankName" to bankName,
                    "accountNumber" to accountNum,
                    "isPrimary" to true
                )
                if (routing.isNotEmpty()) {
                    data[if (region == "EU") "iban" else "routingNumber"] = routing
                }

                isSubmittingBankAccount = true
                lifecycleScope.launch {
                    try {
                        when (val res = repository.addBankAccount(data)) {
                            is NetworkResult.Success -> {
                                Toast.makeText(this@FugenPayoutDashboardActivity, "Bank account successfully linked!", Toast.LENGTH_LONG).show()
                                loadBankAccounts()
                            }
                            is NetworkResult.Error -> Toast.makeText(this@FugenPayoutDashboardActivity, res.message, Toast.LENGTH_LONG).show()
                            is NetworkResult.Exception -> Toast.makeText(this@FugenPayoutDashboardActivity, "Failed to link bank account", Toast.LENGTH_SHORT).show()
                        }
                    } finally {
                        isSubmittingBankAccount = false
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
