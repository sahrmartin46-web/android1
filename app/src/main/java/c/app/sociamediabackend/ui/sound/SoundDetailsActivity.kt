package c.app.sociamediabackend.ui.sound

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import c.app.sociamediabackend.data.api.ApiClient
import c.app.sociamediabackend.data.api.SessionManager
import c.app.sociamediabackend.databinding.ActivitySoundDetailsBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SoundDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySoundDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivitySoundDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.root.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val soundTitle = intent.getStringExtra("SOUND_TITLE") ?: "Original Sound"
        binding.tvTitle.text = soundTitle

        binding.btnBack.setOnClickListener { finish() }

        binding.btnUseSound.setOnClickListener {
            Toast.makeText(this, "Opening Camera with $soundTitle", Toast.LENGTH_SHORT).show()
        }

        binding.rvSoundVideos.layoutManager = GridLayoutManager(this, 3)

        loadRealSoundPosts()
    }

    private fun loadRealSoundPosts() {
        lifecycleScope.launch {
            try {
                val response = ApiClient.getService(this@SoundDetailsActivity).getPosts(feedType = "foryou", limit = 15)
                if (response.isSuccessful && response.body()?.success == true) {
                    val posts = response.body()?.posts.orEmpty()
                    val sessionManager = SessionManager(this@SoundDetailsActivity)
                    val serverHost = sessionManager.getBaseUrl().substringBefore("api/")

                    val items = posts.map { post ->
                        val rawUrl = post.imageUrl ?: post.videoUrl
                        val resolvedUrl = when {
                            rawUrl.isNullOrEmpty() -> ""
                            rawUrl.startsWith("http") -> rawUrl
                            else -> "${serverHost}${rawUrl.removePrefix("/")}"
                        }

                        SoundVideoItem(
                            id = post.id,
                            views = "▶ ${post.views}",
                            handle = "@${post.author?.username ?: "creator"}",
                            imageUrl = resolvedUrl
                        )
                    }
                    withContext(Dispatchers.Main) {
                        binding.rvSoundVideos.adapter = SoundVideosAdapter(items) { video ->
                            Toast.makeText(this@SoundDetailsActivity, "Playing video by ${video.handle}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (e: Exception) {
                // Ignore
            }
        }
    }
}
