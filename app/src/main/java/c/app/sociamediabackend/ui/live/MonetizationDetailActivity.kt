package c.app.sociamediabackend.ui.live

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import c.app.sociamediabackend.data.api.ApiClient
import c.app.sociamediabackend.databinding.ActivityMonetizationDetailBinding
import kotlinx.coroutines.launch

class MonetizationDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMonetizationDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMonetizationDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.root.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        val programId = intent.getStringExtra("PROGRAM_ID").orEmpty()
        val title = intent.getStringExtra("TITLE") ?: "Monetization Program"
        val icon = intent.getStringExtra("ICON") ?: "💰"
        val category = intent.getStringExtra("CATEGORY") ?: "Creator Program"
        val payout = intent.getStringExtra("PAYOUT") ?: "Custom Payout"
        val desc = intent.getStringExtra("DESC") ?: "Monetization program details."
        val req = intent.getStringExtra("REQ") ?: "Standard requirements apply."

        binding.tvDetailIcon.text = icon
        binding.tvDetailTitle.text = title
        binding.tvDetailCategory.text = category
        binding.tvDetailPayout.text = payout
        binding.tvDetailRequirement.text = "Requirement: $req"
        binding.tvDetailDesc.text = desc

        binding.btnApplyOrManage.setOnClickListener {
            if (programId.isNotEmpty()) {
                enrollInProgram(programId, title)
            } else {
                Toast.makeText(this, "Successfully enrolled in $title!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun enrollInProgram(programId: String, title: String) {
        binding.btnApplyOrManage.isEnabled = false
        lifecycleScope.launch {
            try {
                val res = ApiClient.getService(this@MonetizationDetailActivity).enrollInProgram(mapOf("programId" to programId))
                binding.btnApplyOrManage.isEnabled = true
                if (res.isSuccessful && res.body()?.success == true) {
                    Toast.makeText(this@MonetizationDetailActivity, res.body()?.message ?: "Successfully enrolled in $title!", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    Toast.makeText(this@MonetizationDetailActivity, res.body()?.message ?: "Failed to enroll in program", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                binding.btnApplyOrManage.isEnabled = true
                Toast.makeText(this@MonetizationDetailActivity, "Enrollment error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
