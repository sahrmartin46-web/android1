package c.app.sociamediabackend.ui.sound

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import c.app.sociamediabackend.MainActivity
import c.app.sociamediabackend.data.api.ApiClient
import c.app.sociamediabackend.data.model.CreatePostRequest
import c.app.sociamediabackend.databinding.ActivityDuetStudioBinding
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DuetStudioActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDuetStudioBinding
    private var exoPlayer: ExoPlayer? = null
    private var videoUrl: String? = null

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) startCamera()
        else Toast.makeText(this, "Camera permission required for Duet", Toast.LENGTH_SHORT).show()
    }

    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDuetStudioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        videoUrl = intent.getStringExtra("VIDEO_URL")

        binding.btnBack.setOnClickListener { finish() }

        if (!videoUrl.isNullOrEmpty()) {
            exoPlayer = ExoPlayer.Builder(this).build().apply {
                val mediaItem = MediaItem.fromUri(Uri.parse(videoUrl))
                setMediaItem(mediaItem)
                repeatMode = Player.REPEAT_MODE_ALL
                prepare()
                playWhenReady = true
            }
            binding.playerOriginal.player = exoPlayer
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

        binding.btnRecordDuet.setOnClickListener {
            binding.btnRecordDuet.isEnabled = false
            Toast.makeText(this, "🔴 Recording Duet (5s)...", Toast.LENGTH_SHORT).show()

            object : CountDownTimer(5000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    val sec = millisUntilFinished / 1000 + 1
                    binding.btnRecordDuet.text = "Recording... ($sec s)"
                }

                override fun onFinish() {
                    Toast.makeText(this@DuetStudioActivity, "Duet recorded successfully!", Toast.LENGTH_LONG).show()
                    publishDuetPost()
                }
            }.start()
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.previewCamera.surfaceProvider)
            }
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, CameraSelector.DEFAULT_FRONT_CAMERA, preview)
            } catch (e: Exception) {
                // Ignore
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun publishDuetPost() {
        lifecycleScope.launch {
            try {
                val req = CreatePostRequest(
                    content = "🔥 Check out my Duet reaction! #duet #fugen",
                    videoUrl = videoUrl
                )
                val res = ApiClient.getService(this@DuetStudioActivity).createPost(req)
                withContext(Dispatchers.Main) {
                    if (res.isSuccessful) {
                        Toast.makeText(this@DuetStudioActivity, "Duet posted successfully!", Toast.LENGTH_LONG).show()
                        val intent = Intent(this@DuetStudioActivity, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@DuetStudioActivity, "Failed to publish duet", Toast.LENGTH_SHORT).show()
                        binding.btnRecordDuet.isEnabled = true
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@DuetStudioActivity, "Error publishing duet", Toast.LENGTH_SHORT).show()
                    binding.btnRecordDuet.isEnabled = true
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer?.release()
    }
}
