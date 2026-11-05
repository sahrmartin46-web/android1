package c.app.sociamediabackend.ui.main

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import c.app.sociamediabackend.data.api.ApiClient
import c.app.sociamediabackend.data.model.CreatePostRequest
import c.app.sociamediabackend.databinding.FragmentCreatePostBinding
import c.app.sociamediabackend.ui.live.LiveSetupActivity
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

class CreatePostFragment : Fragment() {

    private var _binding: FragmentCreatePostBinding? = null
    private val binding get() = _binding!!

    private var selectedImageUri: Uri? = null
    private var selectedVideoUri: Uri? = null
    private var exoPlayerPreview: ExoPlayer? = null

    private var cameraTempUri: Uri? = null

    // Gallery launchers
    private val selectImageLauncher = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            selectedVideoUri = null
            binding.ivPreview.visibility = View.VISIBLE
            binding.playerViewPreview.visibility = View.GONE
            Glide.with(this).load(it).into(binding.ivPreview)
        }
    }

    private val selectVideoLauncher = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        uri?.let {
            selectedVideoUri = it
            selectedImageUri = null
            binding.ivPreview.visibility = View.GONE
            binding.playerViewPreview.visibility = View.VISIBLE

            setupVideoPreview(it)
        }
    }

    // Camera photo capture launcher
    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
        if (success && cameraTempUri != null) {
            selectedImageUri = cameraTempUri
            selectedVideoUri = null
            binding.ivPreview.visibility = View.VISIBLE
            binding.playerViewPreview.visibility = View.GONE
            Glide.with(this).load(cameraTempUri).into(binding.ivPreview)
            Toast.makeText(requireContext(), "Photo captured!", Toast.LENGTH_SHORT).show()
        }
    }

    // Camera video capture launcher
    private val captureVideoLauncher = registerForActivityResult(ActivityResultContracts.CaptureVideo()) { success: Boolean ->
        if (success && cameraTempUri != null) {
            selectedVideoUri = cameraTempUri
            selectedImageUri = null
            binding.ivPreview.visibility = View.GONE
            binding.playerViewPreview.visibility = View.VISIBLE
            setupVideoPreview(cameraTempUri!!)
            Toast.makeText(requireContext(), "Video recorded!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCreatePostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnPickImage.setOnClickListener {
            selectImageLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.btnPickVideo.setOnClickListener {
            selectVideoLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
        }

        binding.btnOpenCamera.setOnClickListener {
            showCameraOptionsDialog()
        }

        binding.btnSubmitPost.setOnClickListener {
            submitPost()
        }

        // Open camera / media options automatically the instant the user taps the + sign tab
        if (selectedImageUri == null && selectedVideoUri == null) {
            showCameraOptionsDialog()
        }
    }

    private fun showCameraOptionsDialog() {
        val options = arrayOf(
            "📸 Take Photo",
            "📱 Record Vertical Video (9:16 Portrait / Reels)",
            "📺 Record Landscape Video (16:9 Horizontal)",
            "🖼️ Pick from Gallery",
            "🔴 Go Live Stream"
        )

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Create Post or Go Live")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> launchCameraPhoto()
                    1 -> launchCameraVideo(isLandscape = false)
                    2 -> launchCameraVideo(isLandscape = true)
                    3 -> selectImageLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    4 -> startActivity(Intent(requireContext(), LiveSetupActivity::class.java))
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun launchCameraPhoto() {
        try {
            val file = File(requireContext().cacheDir, "camera_photo_${System.currentTimeMillis()}.jpg")
            cameraTempUri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                file
            )
            takePictureLauncher.launch(cameraTempUri)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Failed to launch camera: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun launchCameraVideo(isLandscape: Boolean) {
        try {
            val file = File(requireContext().cacheDir, "camera_video_${System.currentTimeMillis()}.mp4")
            cameraTempUri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                file
            )

            if (isLandscape) {
                Toast.makeText(requireContext(), "Hold phone horizontally for Landscape video (16:9)", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(requireContext(), "Hold phone vertically for Vertical video (9:16 / Reels)", Toast.LENGTH_LONG).show()
            }

            captureVideoLauncher.launch(cameraTempUri)

        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Failed to launch video camera: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupVideoPreview(uri: Uri) {
        exoPlayerPreview?.release()
        exoPlayerPreview = ExoPlayer.Builder(requireContext()).build().apply {
            setMediaItem(MediaItem.fromUri(uri))
            prepare()
        }
        binding.playerViewPreview.player = exoPlayerPreview
    }

    private fun submitPost() {
        val content = binding.etContent.text?.toString()?.trim().orEmpty()

        if (content.isEmpty() && selectedImageUri == null && selectedVideoUri == null) {
            Toast.makeText(requireContext(), "Please write content or attach image/video", Toast.LENGTH_SHORT).show()
            return
        }

        setLoading(true)

        lifecycleScope.launch {
            try {
                var uploadedImageUrl: String? = null
                var uploadedVideoUrl: String? = null

                if (selectedImageUri != null) {
                    val mimeType = requireContext().contentResolver.getType(selectedImageUri!!) ?: "image/jpeg"
                    val file = withContext(Dispatchers.IO) { uriToFile(requireContext(), selectedImageUri!!) }
                    val reqFile = file.asRequestBody(mimeType.toMediaTypeOrNull())
                    val body = MultipartBody.Part.createFormData("image", file.name, reqFile)
                    val uploadRes = ApiClient.getService(requireContext()).uploadImage(body)
                    if (uploadRes.isSuccessful && uploadRes.body()?.success == true) {
                        uploadedImageUrl = uploadRes.body()!!.url
                    } else {
                        setLoading(false)
                        Toast.makeText(requireContext(), "Image upload failed: ${uploadRes.message()}", Toast.LENGTH_SHORT).show()
                        return@launch
                    }
                } else if (selectedVideoUri != null) {
                    val mimeType = requireContext().contentResolver.getType(selectedVideoUri!!) ?: "video/mp4"
                    val file = withContext(Dispatchers.IO) { uriToFile(requireContext(), selectedVideoUri!!) }
                    val reqFile = file.asRequestBody(mimeType.toMediaTypeOrNull())
                    val body = MultipartBody.Part.createFormData("video", file.name, reqFile)
                    val uploadRes = ApiClient.getService(requireContext()).uploadVideo(body)
                    if (uploadRes.isSuccessful && uploadRes.body()?.success == true) {
                        uploadedVideoUrl = uploadRes.body()!!.url
                    } else {
                        setLoading(false)
                        Toast.makeText(requireContext(), "Video upload failed: ${uploadRes.message()}", Toast.LENGTH_SHORT).show()
                        return@launch
                    }
                }

                val createPostReq = CreatePostRequest(
                    content = if (content.isEmpty()) "Check out my new post! 🔥" else content,
                    imageUrl = uploadedImageUrl,
                    videoUrl = uploadedVideoUrl
                )

                val response = ApiClient.getService(requireContext()).createPost(createPostReq)
                setLoading(false)

                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Post published successfully!", Toast.LENGTH_SHORT).show()
                    binding.etContent.text?.clear()
                    selectedImageUri = null
                    selectedVideoUri = null
                    binding.ivPreview.visibility = View.GONE
                    binding.playerViewPreview.visibility = View.GONE
                } else {
                    Toast.makeText(requireContext(), "Failed to publish post: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                setLoading(false)
                Toast.makeText(requireContext(), "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun uriToFile(context: Context, uri: Uri): File {
        val contentResolver = context.contentResolver
        val type = contentResolver.getType(uri) ?: ""
        val extension = when {
            type.contains("png") -> ".png"
            type.contains("gif") -> ".gif"
            type.contains("mp4") -> ".mp4"
            type.contains("webm") -> ".webm"
            type.contains("mov") -> ".mov"
            type.contains("mkv") -> ".mkv"
            else -> if (type.startsWith("video")) ".mp4" else ".jpg"
        }
        val tempFile = File(context.cacheDir, "temp_upload_${System.currentTimeMillis()}$extension")
        contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(tempFile).use { output ->
                input.copyTo(output)
            }
        }
        return tempFile
    }

    private fun setLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnSubmitPost.isEnabled = !loading
    }

    override fun onDestroyView() {
        super.onDestroyView()
        exoPlayerPreview?.release()
        exoPlayerPreview = null
        _binding = null
    }
}
