package c.app.sociamediabackend.ui.analytics

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import c.app.sociamediabackend.data.api.SessionManager
import c.app.sociamediabackend.databinding.ActivityAnalyticsBinding

class AnalyticsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAnalyticsBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityAnalyticsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.root.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        sessionManager = SessionManager(this)
        val user = sessionManager.getUser()

        binding.btnBack.setOnClickListener { finish() }

        binding.tvFollowersMetric.text = (user?.count?.followers ?: 1240).toString()
        binding.tvViewsMetric.text = "48.2K"
        binding.tvLikesMetric.text = "12.8K"
        binding.tvTotalRevenue.text = "💎 2,450 ($12.25 USD)"

        binding.btnWithdraw.setOnClickListener {
            Toast.makeText(this, "Payout withdrawal request submitted successfully!", Toast.LENGTH_LONG).show()
        }
    }
}
