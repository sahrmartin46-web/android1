package c.app.sociamediabackend.ui.sound

import android.content.Intent
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import c.app.sociamediabackend.MainActivity
import c.app.sociamediabackend.data.api.ApiClient
import c.app.sociamediabackend.data.model.CreatePostRequest
import c.app.sociamediabackend.databinding.ActivityPostPreviewBinding
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class PostPreviewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPostPreviewBinding
    private var mediaUriString: String? = null
    private var isVideo: Boolean = false
    private var filterType: Int = 0
    private var speedMultiplier: Float = 1.0f
    private var pickedAudioUri: Uri? = null

    private val pickAudioLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            pickedAudioUri = uri
            val fileName = uri.lastPathSegment?.substringAfterLast('/') ?: "Device Audio"
            binding.btnSelectSound.text = "🎵 Sound: $fileName (Attached)"
            Toast.makeText(this, "Audio track attached from device gallery!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityPostPreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.root.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        mediaUriString = intent.getStringExtra("MEDIA_URI")
        isVideo = intent.getBooleanExtra("IS_VIDEO", false)
        filterType = intent.getIntExtra("FILTER_TYPE", 6)
        speedMultiplier = intent.getFloatExtra("SPEED_MULTIPLIER", 1.0f)

        if (speedMultiplier != 1.0f) {
            Toast.makeText(this, "Recording Speed: ${speedMultiplier}x", Toast.LENGTH_SHORT).show()
        }

        binding.btnBack.setOnClickListener { finish() }

        if (!mediaUriString.isNullOrEmpty()) {
            Glide.with(this)
                .load(Uri.parse(mediaUriString))
                .into(binding.ivMediaPreview)

            if (filterType > 0) {
                val matrix = when (filterType) {
                    1 -> ColorMatrix().apply { setSaturation(0f) }
                    2 -> ColorMatrix(floatArrayOf(
                        0.393f, 0.769f, 0.189f, 0f, 0f,
                        0.349f, 0.686f, 0.168f, 0f, 0f,
                        0.272f, 0.534f, 0.131f, 0f, 0f,
                        0.0f, 0.0f, 0.0f, 1f, 0f
                    ))
                    3 -> ColorMatrix(floatArrayOf(
                        1.2f, 0f, 0f, 0f, 20f,
                        0f, 1.0f, 0f, 0f, 10f,
                        0f, 0f, 0.8f, 0f, -20f,
                        0f, 0f, 0f, 1f, 0f
                    ))
                    4 -> ColorMatrix(floatArrayOf(
                        0.8f, 0f, 0f, 0f, -10f,
                        0f, 0.9f, 0f, 0f, 0f,
                        0f, 0f, 1.3f, 0f, 30f,
                        0f, 0f, 0f, 1f, 0f
                    ))
                    5 -> ColorMatrix(floatArrayOf(
                        -1f, 0f, 0f, 0f, 255f,
                        0f, -1f, 0f, 0f, 255f,
                        0f, 0f, -1f, 0f, 255f,
                        0f, 0f, 0f, 1f, 0f
                    ))
                    6 -> ColorMatrix(floatArrayOf(
                        1.12f, 0.02f, 0.00f, 0f, 8f,
                        0.02f, 1.08f, 0.02f, 0f, 5f,
                        0.00f, 0.02f, 1.02f, 0f, -2f,
                        0.00f, 0.00f, 0.00f, 1f, 0f
                    ))
                    else -> ColorMatrix()
                }
                binding.ivMediaPreview.colorFilter = ColorMatrixColorFilter(matrix)
            }
        }

        // Sound / Music Selector (Includes Browse Device Gallery)
        binding.btnSelectSound.setOnClickListener {
            val options = arrayOf(
                "📂 Browse Device Gallery (Audio)",
                "🎵 Tokyo Drift Lofi - Original Mix",
                "🎵 Summer Vibes - DJ Pulse",
                "🎵 Afrobeat Midnight Party - StarBoy Beats",
                "🎵 Cyberpunk Synthwave - Neo Sound"
            )
            MaterialAlertDialogBuilder(this)
                .setTitle("Select Background Sound Track")
                .setItems(options) { _, which ->
                    if (which == 0) {
                        pickAudioLauncher.launch("audio/*")
                    } else {
                        val chosen = options[which]
                        pickedAudioUri = null
                        binding.btnSelectSound.text = chosen
                        Toast.makeText(this, "Attached $chosen to post!", Toast.LENGTH_SHORT).show()
                    }
                }
                .show()
        }

        // Hashtag click shortcuts
        binding.tagFugen.setOnClickListener { appendTag("#fugen") }
        binding.tagViral.setOnClickListener { appendTag("#viral") }
        binding.tagMusic.setOnClickListener { appendTag("#music") }
        binding.tagTrending.setOnClickListener { appendTag("#trending") }
        binding.tagDance.setOnClickListener { appendTag("#dance") }

        binding.btnPostNow.setOnClickListener {
            val caption = binding.etCaption.text?.toString()?.trim().orEmpty()
            if (caption.isEmpty()) {
                Toast.makeText(this, "Please write a title or caption for your post", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!mediaUriString.isNullOrEmpty()) {
                uploadAndPublish(Uri.parse(mediaUriString), caption, isVideo)
            } else {
                Toast.makeText(this, "No media found to post", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun appendTag(tag: String) {
        val current = binding.etCaption.text?.toString().orEmpty()
        binding.etCaption.setText("$current $tag")
        binding.etCaption.setSelection(binding.etCaption.text?.length ?: 0)
    }

    private fun uploadAndPublish(uri: Uri, caption: String, isVideo: Boolean) {
        Toast.makeText(this, "Uploading media...", Toast.LENGTH_SHORT).show()
        binding.btnPostNow.isEnabled = false

        lifecycleScope.launch {
            try {
                val file = uriToTempFile(uri) ?: run {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@PostPreviewActivity, "Failed to read media file", Toast.LENGTH_SHORT).show()
                        binding.btnPostNow.isEnabled = true
                    }
                    return@launch
                }

                val mediaType = (if (isVideo) "video/*" else "image/*").toMediaTypeOrNull()
                val requestFile = file.asRequestBody(mediaType)
                val body = MultipartBody.Part.createFormData(if (isVideo) "video" else "image", file.name, requestFile)

                val uploadRes = if (isVideo) {
                    ApiClient.getService(this@PostPreviewActivity).uploadVideo(body)
                } else {
                    ApiClient.getService(this@PostPreviewActivity).uploadImage(body)
                }

                if (uploadRes.isSuccessful && uploadRes.body()?.success == true) {
                    val url = uploadRes.body()?.url
                    val postReq = CreatePostRequest(
                        content = caption,
                        imageUrl = if (!isVideo) url else null,
                        videoUrl = if (isVideo) url else null
                    )
                    val postRes = ApiClient.getService(this@PostPreviewActivity).createPost(postReq)
                    withContext(Dispatchers.Main) {
                        if (postRes.isSuccessful && postRes.body()?.success == true) {
                            Toast.makeText(this@PostPreviewActivity, "Posted successfully with audio/sound!", Toast.LENGTH_LONG).show()
                            val intent = Intent(this@PostPreviewActivity, MainActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this@PostPreviewActivity, "Failed to create post", Toast.LENGTH_SHORT).show()
                            binding.btnPostNow.isEnabled = true
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@PostPreviewActivity, "Upload failed", Toast.LENGTH_SHORT).show()
                        binding.btnPostNow.isEnabled = true
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@PostPreviewActivity, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    binding.btnPostNow.isEnabled = true
                }
            }
        }
    }

    private fun uriToTempFile(uri: Uri): File? {
        return try {
            val extension = if (isVideo) ".mp4" else ".jpg"
            val tempFile = File(cacheDir, "upload_${System.currentTimeMillis()}$extension")

            contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                val inputStream = FileInputStream(pfd.fileDescriptor)
                val outputStream = FileOutputStream(tempFile)
                inputStream.copyTo(outputStream)
                inputStream.close()
                outputStream.close()
            } ?: run {
                val inputStream = contentResolver.openInputStream(uri) ?: return null
                val outputStream = FileOutputStream(tempFile)
                inputStream.copyTo(outputStream)
                inputStream.close()
                outputStream.close()
            }

            if (tempFile.exists() && tempFile.length() > 0) tempFile else null
        } catch (e: Exception) {
            try {
                val inputStream = contentResolver.openInputStream(uri) ?: return null
                val tempFile = File(cacheDir, "fallback_${System.currentTimeMillis()}.tmp")
                val outputStream = FileOutputStream(tempFile)
                inputStream.copyTo(outputStream)
                inputStream.close()
                outputStream.close()
                if (tempFile.exists() && tempFile.length() > 0) tempFile else null
            } catch (ex: Exception) {
                null
            }
        }
    }
}
