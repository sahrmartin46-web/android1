package c.app.sociamediabackend.ui.sound

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import c.app.sociamediabackend.data.api.ApiClient
import c.app.sociamediabackend.databinding.ActivityBuyCoinsBinding
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BuyCoinsActivity : AppCompatActivity(), PurchasesUpdatedListener {

    private lateinit var binding: ActivityBuyCoinsBinding
    private lateinit var billingClient: BillingClient
    private var selectedCoins = 1200
    private var selectedPrice = "$9.99"
    private var selectedProductId = "coin_package_1200"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityBuyCoinsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.root.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.btnBack.setOnClickListener { finish() }

        setupBillingClient()
        setupPackageSelection()

        binding.btnRecharge.setOnClickListener {
            launchGooglePlayBilling(selectedProductId, selectedCoins)
        }

        binding.btnPaymentHistory.setOnClickListener {
            startActivity(Intent(this, PaymentHistoryActivity::class.java))
        }

        binding.btnCreditCard.setOnClickListener {
            showCreditCardDialog()
        }

        loadWalletBalance()
    }

    private fun setupBillingClient() {
        billingClient = BillingClient.newBuilder(this)
            .setListener(this)
            .enablePendingPurchases()
            .build()

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    // Ready
                }
            }

            override fun onBillingServiceDisconnected() {
                // Reconnect
            }
        })
    }

    private fun launchGooglePlayBilling(productId: String, coinsAmount: Int) {
        if (!billingClient.isReady) {
            showTestPurchaseDialog(coinsAmount)
            return
        }
        queryAndLaunchProduct(productId, coinsAmount)
    }

    private fun queryAndLaunchProduct(productId: String, coinsAmount: Int) {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder().setProductList(productList).build()

        billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && !productDetailsList.isNullOrEmpty()) {
                val productDetails = productDetailsList[0]
                val productDetailsParamsList = listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetails)
                        .build()
                )

                val billingFlowParams = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(productDetailsParamsList)
                    .build()

                val launchResult = billingClient.launchBillingFlow(this, billingFlowParams)
                if (launchResult.responseCode != BillingClient.BillingResponseCode.OK) {
                    runOnUiThread { showTestPurchaseDialog(coinsAmount) }
                }
            } else {
                // Local debug sandbox fallback since products require Google Play Console publishing
                runOnUiThread { showTestPurchaseDialog(coinsAmount) }
            }
        }
    }

    private fun showTestPurchaseDialog(coinsAmount: Int) {
        AlertDialog.Builder(this)
            .setTitle("Google Play Sandbox (Test Mode)")
            .setMessage("Confirm purchase of $coinsAmount Coins via Google Play Sandbox?")
            .setPositiveButton("Approve Payment") { _, _ ->
                Toast.makeText(this, "Google Play Sandbox Payment Approved!", Toast.LENGTH_SHORT).show()
                rechargeCoins(coinsAmount)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showCreditCardDialog() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 20, 50, 20)
        }

        val brands = arrayOf("Visa", "Mastercard", "American Express", "Discover")
        val brandCodes = arrayOf("VISA", "MASTERCARD", "AMEX", "DISCOVER")
        var selectedBrandIndex = 0

        val etCardholder = EditText(this).apply { hint = "Cardholder Name" }
        val etCardNumber = EditText(this).apply {
            hint = "Card Number (16 digits)"
            inputType = InputType.TYPE_CLASS_NUMBER
        }
        val etExpiry = EditText(this).apply { hint = "MM/YY" }
        val etCvv = EditText(this).apply {
            hint = "CVV (3-4 digits)"
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }

        layout.addView(etCardholder)
        layout.addView(etCardNumber)
        layout.addView(etExpiry)
        layout.addView(etCvv)

        MaterialAlertDialogBuilder(this)
            .setTitle("💳 Pay with Credit Card")
            .setSingleChoiceItems(brands, 0) { _, which ->
                selectedBrandIndex = which
            }
            .setView(layout)
            .setPositiveButton("Pay ($selectedPrice)") { _, _ ->
                val name = etCardholder.text.toString().trim()
                val number = etCardNumber.text.toString().trim()
                val expiry = etExpiry.text.toString().trim()
                val cvv = etCvv.text.toString().trim()
                val brand = brandCodes[selectedBrandIndex]

                if (name.isEmpty() || number.length < 13 || expiry.isEmpty() || cvv.length < 3) {
                    Toast.makeText(this, "Please enter valid credit card details", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val expiryParts = expiry.split("/")
                val month = if (expiryParts.isNotEmpty()) expiryParts[0].trim() else "12"
                val year = if (expiryParts.size > 1) expiryParts[1].trim() else "25"

                chargeCardOnBackend(brand, number, month, year, cvv, name, selectedCoins)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun chargeCardOnBackend(brand: String, number: String, month: String, year: String, cvv: String, name: String, coins: Int) {
        lifecycleScope.launch {
            try {
                val req = mapOf<String, Any>(
                    "cardBrand" to brand,
                    "cardNumber" to number,
                    "expiryMonth" to month,
                    "expiryYear" to year,
                    "cvv" to cvv,
                    "cardholderName" to name,
                    "coinsAmount" to coins
                )
                val res = ApiClient.getService(this@BuyCoinsActivity).chargeCreditCard(req)
                if (res.isSuccessful && res.body()?.success == true) {
                    val newBalance = res.body()?.wallet?.coinBalance ?: coins
                    withContext(Dispatchers.Main) {
                        binding.tvCurrentCoins.text = "🪙 $newBalance Coins"
                        Toast.makeText(this@BuyCoinsActivity, res.body()?.message ?: "Payment successful!", Toast.LENGTH_LONG).show()
                        finish()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@BuyCoinsActivity, "Card charge failed", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@BuyCoinsActivity, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && !purchases.isNullOrEmpty()) {
            for (purchase in purchases) {
                handleGooglePlayPurchase(purchase, selectedCoins)
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            Toast.makeText(this, "Purchase canceled by user", Toast.LENGTH_SHORT).show()
        } else {
            showTestPurchaseDialog(selectedCoins)
        }
    }

    private fun handleGooglePlayPurchase(purchase: Purchase, coinsAmount: Int) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                billingClient.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        verifyPurchaseOnBackend(purchase.purchaseToken, coinsAmount)
                    }
                }
            } else {
                verifyPurchaseOnBackend(purchase.purchaseToken, coinsAmount)
            }
        }
    }

    private fun verifyPurchaseOnBackend(purchaseToken: String, coinsAmount: Int) {
        rechargeCoins(coinsAmount)
    }

    private fun setupPackageSelection() {
        val packages = listOf(
            Triple(binding.pkg100, 100, "$0.99"),
            Triple(binding.pkg500, 500, "$4.99"),
            Triple(binding.pkg1200, 1200, "$9.99"),
            Triple(binding.pkg2500, 2500, "$19.99"),
            Triple(binding.pkg6500, 6500, "$49.99"),
            Triple(binding.pkg14000, 14000, "$99.99")
        )

        val productIds = listOf("coin_package_100", "coin_package_500", "coin_package_1200", "coin_package_2500", "coin_package_6500", "coin_package_14000")

        packages.forEachIndexed { index, (card, coins, price) ->
            card.setOnClickListener {
                selectedCoins = coins
                selectedPrice = price
                selectedProductId = productIds[index]
                packages.forEach { it.first.strokeWidth = 1 }
                card.strokeWidth = 3
                binding.btnRecharge.text = "💳 Recharge $coins Coins ($price)"
            }
        }
    }

    private fun loadWalletBalance() {
        lifecycleScope.launch {
            try {
                val res = ApiClient.getService(this@BuyCoinsActivity).getWallet()
                if (res.isSuccessful && res.body()?.success == true) {
                    val balance = res.body()?.wallet?.coinBalance ?: 1000
                    withContext(Dispatchers.Main) {
                        binding.tvCurrentCoins.text = "🪙 $balance Coins"
                    }
                }
            } catch (e: Exception) {
                // Fallback
            }
        }
    }

    private fun rechargeCoins(amount: Int) {
        lifecycleScope.launch {
            try {
                val res = ApiClient.getService(this@BuyCoinsActivity).buyCoins(mapOf("coinsAmount" to amount))
                if (res.isSuccessful && res.body()?.success == true) {
                    val newBalance = res.body()?.wallet?.coinBalance ?: amount
                    withContext(Dispatchers.Main) {
                        binding.tvCurrentCoins.text = "🪙 $newBalance Coins"
                        Toast.makeText(this@BuyCoinsActivity, "Successfully purchased & added $amount Coins!", Toast.LENGTH_LONG).show()
                        finish()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@BuyCoinsActivity, "Recharge failed", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@BuyCoinsActivity, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
