package c.app.sociamediabackend.ui.live

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import c.app.sociamediabackend.databinding.ActivityLiveSetupBinding

class LiveSetupActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLiveSetupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityLiveSetupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.root.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.btnBack.setOnClickListener { finish() }

        binding.btnGoLiveNow.setOnClickListener {
            val title = binding.etStreamTitle.text?.toString()?.trim().orEmpty()
            if (title.isEmpty()) {
                Toast.makeText(this, "Please enter a stream title or topic", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(this, GoLiveStudioActivity::class.java).apply {
                putExtra("STREAM_TITLE", title)
            }
            startActivity(intent)
            finish()
        }
    }
}
