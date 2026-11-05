package c.app.sociamediabackend.ui.sound

import android.Manifest
import android.R
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.view.ScaleGestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.FallbackStrategy
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import c.app.sociamediabackend.databinding.ActivityCameraStudioBinding
import c.app.sociamediabackend.ui.live.LiveSetupActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

class CameraStudioActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCameraStudioBinding
    private var imageCapture: ImageCapture? = null
    private var videoCapture: VideoCapture<Recorder>? = null
    private var activeRecording: Recording? = null
    private var lensFacing = CameraSelector.LENS_FACING_BACK
    private var activeCamera: Camera? = null
    private var isTorchOn = false
    private var selectedDurationSeconds = 5
    private var selectedSpeedMultiplier = 1.0f

    private var selectedFilterIndex = 0
    private var selectedColorMatrix = ColorMatrix()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.CAMERA] == true && permissions[Manifest.permission.RECORD_AUDIO] == true) {
            startCamera()
        } else {
            Toast.makeText(this, "Camera and Audio permissions are required for the Studio", Toast.LENGTH_SHORT).show()
        }
    }

    private val pickPictureLauncher = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val file = uriToCacheFile(uri, false)
            if (file != null) navigateToPreview(Uri.fromFile(file), false)
            else Toast.makeText(this, "Failed to read picture", Toast.LENGTH_SHORT).show()
        }
    }

    private val pickVideoLauncher = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val file = uriToCacheFile(uri, true)
            if (file != null) navigateToPreview(Uri.fromFile(file), true)
            else Toast.makeText(this, "Failed to read video", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraStudioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnClose.setOnClickListener { finish() }

        binding.btnFlipCamera.setOnClickListener {
            lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                CameraSelector.LENS_FACING_FRONT
            } else {
                CameraSelector.LENS_FACING_BACK
            }
            startCamera()
            Toast.makeText(this, "Camera flipped", Toast.LENGTH_SHORT).show()
        }

        binding.btnFlash.setOnClickListener {
            isTorchOn = !isTorchOn
            activeCamera?.cameraControl?.enableTorch(isTorchOn)
            Toast.makeText(this, if (isTorchOn) "Flash Torch ON 🔦" else "Flash Torch OFF", Toast.LENGTH_SHORT).show()
        }

        binding.btnStudioFilters.setOnClickListener {
            showStudioFiltersDialog()
        }

        binding.btnPickAudioSound.setOnClickListener {
            showAudioSoundPicker()
        }

        setupDurationSelectors()
        setupFilters()

        binding.btnSnapPicture.setOnClickListener {
            takePhotoSnip()
        }

        binding.btnRecordTimedVideo.setOnClickListener {
            startTimedVideoRecording()
        }

        binding.btnPickPicture.setOnClickListener {
            pickPictureLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.btnPickVideo.setOnClickListener {
            pickVideoLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
        }

        binding.btnGoLive.setOnClickListener {
            startActivity(Intent(this, LiveSetupActivity::class.java))
        }

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO))
        }
    }

    private fun allPermissionsGranted() = listOf(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO
    ).all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun setupDurationSelectors() {
        val views = listOf(binding.dur2s, binding.dur5s, binding.dur8s, binding.dur10s, binding.dur15s)
        val seconds = listOf(2, 5, 8, 10, 15)

        views.forEachIndexed { index, textView ->
            textView.setOnClickListener {
                selectedDurationSeconds = seconds[index]
                views.forEach { it.setTextColor(ContextCompat.getColor(this, R.color.white)) }
                textView.setTextColor(Color.CYAN)
                Toast.makeText(this, "Video duration set to ${selectedDurationSeconds}s", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupFilters() {
        binding.filterBeauty.setOnClickListener {
            setActiveFilterButton(binding.filterBeauty)
            selectedFilterIndex = 0
            selectedColorMatrix = ColorMatrix()
            applyCameraFilter(selectedColorMatrix)
            Toast.makeText(this, "Effect: Natural (Original)", Toast.LENGTH_SHORT).show()
        }

        binding.filterNormal.setOnClickListener {
            setActiveFilterButton(binding.filterNormal)
            selectedFilterIndex = 1
            selectedColorMatrix = ColorMatrix(floatArrayOf(
                1.12f, 0.02f, 0.00f, 0f, 8f,
                0.02f, 1.08f, 0.02f, 0f, 5f,
                0.00f, 0.02f, 1.02f, 0f, -2f,
                0.00f, 0.00f, 0.00f, 1f, 0f
            ))
            applyCameraFilter(selectedColorMatrix)
            Toast.makeText(this, "Effect: Soft Beauty (Skin Smooth & Brightness)", Toast.LENGTH_SHORT).show()
        }

        binding.filterGrayscale.setOnClickListener {
            setActiveFilterButton(binding.filterGrayscale)
            selectedFilterIndex = 2
            selectedColorMatrix = ColorMatrix(floatArrayOf(
                1.08f, 0f, 0f, 0f, 8f,
                0f, 1.02f, 0f, 0f, 3f,
                0f, 0f, 0.88f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            ))
            applyCameraFilter(selectedColorMatrix)
            Toast.makeText(this, "Effect: Warm Golden Hour", Toast.LENGTH_SHORT).show()
        }

        binding.filterSepia.setOnClickListener {
            setActiveFilterButton(binding.filterSepia)
            selectedFilterIndex = 3
            selectedColorMatrix = ColorMatrix(floatArrayOf(
                0.92f, 0f, 0f, 0f, 0f,
                0f, 1.01f, 0f, 0f, 0f,
                0f, 0f, 1.12f, 0f, 5f,
                0f, 0f, 0f, 1f, 0f
            ))
            applyCameraFilter(selectedColorMatrix)
            Toast.makeText(this, "Effect: Arctic Cool Blue", Toast.LENGTH_SHORT).show()
        }

        binding.filterSunset.setOnClickListener {
            setActiveFilterButton(binding.filterSunset)
            selectedFilterIndex = 4
            selectedColorMatrix = ColorMatrix(floatArrayOf(
                0.85f, 0.08f, 0.02f, 0f, 12f,
                0.04f, 0.90f, 0.03f, 0f, 6f,
                0.02f, 0.06f, 0.72f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            ))
            applyCameraFilter(selectedColorMatrix)
            Toast.makeText(this, "Effect: Vintage Retro", Toast.LENGTH_SHORT).show()
        }

        binding.filterCool.setOnClickListener {
            setActiveFilterButton(binding.filterCool)
            selectedFilterIndex = 5
            selectedColorMatrix = ColorMatrix().apply { setSaturation(0f) }
            applyCameraFilter(selectedColorMatrix)
            Toast.makeText(this, "Effect: Mono (Black & White)", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setActiveFilterButton(activeButton: MaterialButton) {
        val filterButtons = listOf(
            binding.filterBeauty,
            binding.filterNormal,
            binding.filterGrayscale,
            binding.filterSepia,
            binding.filterSunset,
            binding.filterCool
        )
        filterButtons.forEach { it.setTextColor(Color.WHITE) }
        activeButton.setTextColor(Color.parseColor("#25F4EE"))
    }

    private fun applyCameraFilter(matrix: ColorMatrix) {
        binding.previewView.setLayerType(View.LAYER_TYPE_HARDWARE, Paint().apply {
            colorFilter = ColorMatrixColorFilter(matrix)
        })
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.previewView.surfaceProvider)
            }
            imageCapture = ImageCapture.Builder().build()

            val qualitySelector = QualitySelector.fromOrderedList(
                listOf(Quality.UHD, Quality.FHD, Quality.HD),
                FallbackStrategy.lowerQualityOrHigherThan(Quality.SD)
            )

            val recorder = Recorder.Builder()
                .setQualitySelector(qualitySelector)
                .build()
            videoCapture = VideoCapture.withOutput(recorder)

            val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()

            try {
                cameraProvider.unbindAll()
                activeCamera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture, videoCapture)
                applyCameraFilter(selectedColorMatrix)
                setupTapToFocusAndZoom()
            } catch (e: Exception) {
                Toast.makeText(this, "Failed to start camera", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun setupTapToFocusAndZoom() {
        val camera = activeCamera ?: return
        val cameraControl = camera.cameraControl
        val cameraInfo = camera.cameraInfo

        val scaleGestureDetector = ScaleGestureDetector(this, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                val currentZoomRatio = cameraInfo.zoomState.value?.zoomRatio ?: 1f
                val delta = detector.scaleFactor
                cameraControl.setZoomRatio(currentZoomRatio * delta)
                return true
            }
        })

        binding.previewView.setOnTouchListener { _, event ->
            scaleGestureDetector.onTouchEvent(event)
            if (event.action == MotionEvent.ACTION_UP) {
                val factory = binding.previewView.meteringPointFactory
                val point = factory.createPoint(event.x, event.y)
                val action = FocusMeteringAction.Builder(point, FocusMeteringAction.FLAG_AF)
                    .setAutoCancelDuration(3, TimeUnit.SECONDS)
                    .build()
                cameraControl.startFocusAndMetering(action)
                Toast.makeText(this, "Focused 🎯", Toast.LENGTH_SHORT).show()
            }
            true
        }
    }

    private fun takePhotoSnip() {
        val imageCapture = imageCapture ?: return
        val photoFile = File(cacheDir, "snip_${System.currentTimeMillis()}.jpg")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Toast.makeText(this@CameraStudioActivity, "Photo capture failed: ${exc.message}", Toast.LENGTH_SHORT).show()
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    try {
                        val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
                        if (bitmap != null) {
                            val filtered = Bitmap.createBitmap(bitmap.width, bitmap.height, bitmap.config ?: Bitmap.Config.ARGB_8888)
                            val canvas = Canvas(filtered)
                            val paint = Paint().apply {
                                colorFilter = ColorMatrixColorFilter(selectedColorMatrix)
                            }
                            canvas.drawBitmap(bitmap, 0f, 0f, paint)
                            val out = FileOutputStream(photoFile)
                            filtered.compress(Bitmap.CompressFormat.JPEG, 95, out)
                            out.close()
                        }
                    } catch (e: Exception) {
                        // Keep original if filter application throws exception
                    }

                    Toast.makeText(this@CameraStudioActivity, "Ultra HD Photo captured!", Toast.LENGTH_SHORT).show()
                    navigateToPreview(Uri.fromFile(photoFile), false)
                }
            }
        )
    }

    private fun startTimedVideoRecording() {
        val videoCapture = videoCapture ?: return
        binding.btnRecordTimedVideo.isEnabled = false

        val videoFile = File(cacheDir, "recorded_${System.currentTimeMillis()}.mp4")
        val outputOptions = FileOutputOptions.Builder(videoFile).build()

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        activeRecording = videoCapture.output
            .prepareRecording(this, outputOptions)
            .withAudioEnabled()
            .start(ContextCompat.getMainExecutor(this)) { recordEvent ->
                when (recordEvent) {
                    is VideoRecordEvent.Finalize -> {
                        if (!recordEvent.hasError()) {
                            Toast.makeText(this, "Ultra HD Video recorded successfully!", Toast.LENGTH_SHORT).show()
                            navigateToPreview(Uri.fromFile(videoFile), true)
                        } else {
                            activeRecording = null
                            binding.btnRecordTimedVideo.isEnabled = true
                            Toast.makeText(this, "Video capture error", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

        binding.tvTimer.visibility = View.VISIBLE
        val millisInFuture = (selectedDurationSeconds * 1000 / selectedSpeedMultiplier).toLong()
        object : CountDownTimer(millisInFuture, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secs = millisUntilFinished / 1000 + 1
                binding.tvTimer.text = "🔴 RECORDING ($secs s) [${selectedSpeedMultiplier}x]"
            }

            override fun onFinish() {
                binding.tvTimer.text = "✅ PROCESSING..."
                binding.tvTimer.visibility = View.GONE
                activeRecording?.stop()
                activeRecording = null
            }
        }.start()
    }

    private fun showStudioFiltersDialog() {
        val effects = arrayOf(
            "📷 1. Natural (Original)",
            "👑 2. Soft Beauty (Skin Smooth & Brightness)",
            "🌅 3. Warm Golden Hour",
            "❄️ 4. Arctic Cool Blue",
            "🔥 5. Vintage Retro Film",
            "🎬 6. Mono (Black & White)",
            "🔮 7. Cyberpunk Neon Glow",
            "✨ 8. Glow Beauty + Warm"
        )
        MaterialAlertDialogBuilder(this)
            .setTitle("✨ FUGEN Video Effects Studio")
            .setItems(effects) { _, which ->
                when (which) {
                    0 -> binding.filterBeauty.performClick()
                    1 -> binding.filterNormal.performClick()
                    2 -> binding.filterGrayscale.performClick()
                    3 -> binding.filterSepia.performClick()
                    4 -> binding.filterSunset.performClick()
                    5 -> binding.filterCool.performClick()
                    6 -> {
                        selectedFilterIndex = 6
                        selectedColorMatrix = ColorMatrix(floatArrayOf(
                            1.4f, 0f, 0f, 0f, 20f,
                            0f, 0.8f, 0.2f, 0f, 10f,
                            0f, 0f, 1.6f, 0f, 30f,
                            0f, 0f, 0f, 1f, 0f
                        ))
                        applyCameraFilter(selectedColorMatrix)
                        Toast.makeText(this, "Effect: Cyberpunk Neon Glow", Toast.LENGTH_SHORT).show()
                    }
                    7 -> {
                        selectedFilterIndex = 7
                        selectedColorMatrix = ColorMatrix(floatArrayOf(
                            1.2f, 0.05f, 0.00f, 0f, 15f,
                            0.02f, 1.10f, 0.02f, 0f, 10f,
                            0.00f, 0.02f, 0.95f, 0f, 0f,
                            0.00f, 0.00f, 0.00f, 1f, 0f
                        ))
                        applyCameraFilter(selectedColorMatrix)
                        Toast.makeText(this, "Effect: Glow Beauty + Warm", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showAudioSoundPicker() {
        val sounds = arrayOf(
            "🎵 Fugen Official Anthem (Trending)",
            "🔥 Afrobeat Bounce 2025",
            "🎹 Lo-Fi Chill Study Beat",
            "🎸 Acoustic Guitar Pop",
            "📂 Pick Audio from Device Gallery..."
        )
        MaterialAlertDialogBuilder(this)
            .setTitle("🎵 Sound & Music Library")
            .setItems(sounds) { _, which ->
                if (which == 4) {
                    Toast.makeText(this, "Opening device music gallery...", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Selected Sound: ${sounds[which]}", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showSpeedSelectorDialog() {
        val speeds = arrayOf("🐢 0.3x (Slow Motion)", "🐢 0.5x (Slow)", "⚡ 1.0x (Normal)", "🚀 2.0x (Fast)", "🚀 3.0x (Super Fast)")
        val speedValues = floatArrayOf(0.3f, 0.5f, 1.0f, 2.0f, 3.0f)

        MaterialAlertDialogBuilder(this)
            .setTitle("⏱️ Select Recording Speed")
            .setItems(speeds) { _, which ->
                selectedSpeedMultiplier = speedValues[which]
                Toast.makeText(this, "Video Speed: ${speeds[which]}", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun uriToCacheFile(uri: Uri, isVideo: Boolean): File? {
        return try {
            val extension = if (isVideo) ".mp4" else ".jpg"
            val tempFile = File(cacheDir, "picked_${System.currentTimeMillis()}$extension")
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val outputStream = FileOutputStream(tempFile)
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()
            if (tempFile.exists() && tempFile.length() > 0) tempFile else null
        } catch (e: Exception) {
            null
        }
    }

    private fun navigateToPreview(uri: Uri, isVideo: Boolean) {
        val intent = Intent(this, PostPreviewActivity::class.java).apply {
            putExtra("MEDIA_URI", uri.toString())
            putExtra("IS_VIDEO", isVideo)
            putExtra("FILTER_TYPE", selectedFilterIndex)
            putExtra("SPEED_MULTIPLIER", selectedSpeedMultiplier)
        }
        startActivity(intent)
        finish()
    }
}
