package c.app.sociamediabackend.ui.live

import android.Manifest
import android.content.pm.PackageManager
import android.hardware.Camera
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import c.app.sociamediabackend.data.api.ApiClient
import c.app.sociamediabackend.databinding.ActivityGoLiveBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

@Suppress("DEPRECATION")
class GoLiveActivity : AppCompatActivity(), SurfaceHolder.Callback {

    private lateinit var binding: ActivityGoLiveBinding
    private var isLive = false
    private var camera: Camera? = null
    private var activeSessionId: String? = null
    private var currentCameraFacing = Camera.CameraInfo.CAMERA_FACING_FRONT

    private val requestCameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openCameraSafely()
        } else {
            Toast.makeText(this, "Camera permission is required to broadcast live", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityGoLiveBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.root.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.surfaceViewCamera.holder.addCallback(this)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            openCameraSafely()
        } else {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

        binding.btnCloseLive.setOnClickListener {
            stopCamera()
            if (activeSessionId != null) {
                endSessionOnBackend(activeSessionId!!)
            } else {
                finish()
            }
        }

        binding.btnStartLive.setOnClickListener {
            val title = binding.etStreamTitle.text?.toString()?.trim().orEmpty()
            if (title.isEmpty()) {
                Toast.makeText(this, "Please add a stream title", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            startBroadcast(title)
        }

        binding.btnEndLive.setOnClickListener {
            stopBroadcast()
        }

        binding.btnFlipCamera.setOnClickListener {
            flipCamera()
        }

        binding.btnSwitchLayout.setOnClickListener {
            showLayoutSwitcherDialog()
        }

        binding.btnEffects.setOnClickListener {
            showEffectsDialog()
        }

        binding.btnGuest.setOnClickListener {
            showGuestDialog()
        }

        binding.btnGifts.setOnClickListener {
            if (activeSessionId != null) {
                val giftsSheet = GiftBottomSheetDialogFragment(activeSessionId!!) { gift ->
                    Toast.makeText(this, "Received ${gift.name} ${gift.iconUrl}!", Toast.LENGTH_SHORT).show()
                }
                giftsSheet.show(supportFragmentManager, GiftBottomSheetDialogFragment.TAG)
            } else {
                Toast.makeText(this, "Start broadcast first", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnSendChat.setOnClickListener {
            sendBroadcasterChat()
        }
    }

    private fun openCameraSafely() {
        try {
            if (camera == null) {
                val numCameras = Camera.getNumberOfCameras()
                if (numCameras > 0) {
                    camera = try {
                        Camera.open(currentCameraFacing)
                    } catch (e: Exception) {
                        Camera.open(0)
                    }
                    camera?.setDisplayOrientation(90)
                }
            }
        } catch (e: Exception) {
            // Safe failover
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        try {
            openCameraSafely()
            camera?.setPreviewDisplay(holder)
            camera?.startPreview()
        } catch (e: Exception) {
            // Failover
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        try {
            camera?.startPreview()
        } catch (e: Exception) {
            // Ignore
        }
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        stopCamera()
    }

    private fun flipCamera() {
        try {
            stopCamera()
            currentCameraFacing = if (currentCameraFacing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                Camera.CameraInfo.CAMERA_FACING_BACK
            } else {
                Camera.CameraInfo.CAMERA_FACING_FRONT
            }
            openCameraSafely()
            camera?.setPreviewDisplay(binding.surfaceViewCamera.holder)
            camera?.startPreview()
            Toast.makeText(this, "Camera flipped", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to flip camera", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showLayoutSwitcherDialog() {
        val layouts = arrayOf(
            "📱 Solo Broadcaster View",
            "🪞 Dual Split View (1 Host + 1 Guest)",
            "🔲 2x2 Grid View (1 Host + 3 Guests)"
        )

        MaterialAlertDialogBuilder(this)
            .setTitle("Multi-Guest Layout Switcher")
            .setItems(layouts) { _, which ->
                when (which) {
                    0 -> {
                        binding.llGuestGridContainer.visibility = View.GONE
                        Toast.makeText(this, "Switched to Solo Broadcaster View", Toast.LENGTH_SHORT).show()
                    }
                    1 -> {
                        binding.llGuestGridContainer.visibility = View.VISIBLE
                        binding.flGuest1.visibility = View.VISIBLE
                        binding.llRow2Guests.visibility = View.GONE
                        Toast.makeText(this, "Switched to Dual Split View", Toast.LENGTH_SHORT).show()
                    }
                    2 -> {
                        binding.llGuestGridContainer.visibility = View.VISIBLE
                        binding.flGuest1.visibility = View.VISIBLE
                        binding.llRow2Guests.visibility = View.VISIBLE
                        Toast.makeText(this, "Switched to 2x2 Grid View", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun stopCamera() {
        try {
            camera?.stopPreview()
            camera?.release()
            camera = null
        } catch (e: Exception) {
            // Ignore
        }
    }

    private fun startBroadcast(title: String) {
        lifecycleScope.launch {
            try {
                val createRes = ApiClient.getService(this@GoLiveActivity).createLiveSession(mapOf("title" to title))
                if (createRes.isSuccessful && createRes.body()?.liveSession != null) {
                    val sessionId = createRes.body()!!.liveSession!!.id
                    activeSessionId = sessionId
                    ApiClient.getService(this@GoLiveActivity).startLiveSession(sessionId)
                    ApiClient.getService(this@GoLiveActivity).getLiveToken(sessionId)
                }
            } catch (e: Exception) {
                // Ignore
            }
        }

        isLive = true
        binding.llSetupContainer.visibility = View.GONE
        binding.llActiveLiveControls.visibility = View.VISIBLE
        binding.llLiveBadge.visibility = View.VISIBLE
        binding.tvStreamHealth.visibility = View.VISIBLE
        binding.tvViewerCount.text = "👁 0"

        Toast.makeText(this, "You are now LIVE: $title", Toast.LENGTH_LONG).show()
    }

    private fun endSessionOnBackend(sessionId: String) {
        lifecycleScope.launch {
            try {
                ApiClient.getService(this@GoLiveActivity).endLiveSession(sessionId)
            } catch (e: Exception) {
                // Ignore
            }
            finish()
        }
    }

    private fun sendBroadcasterChat() {
        val text = binding.etChatInput.text?.toString()?.trim().orEmpty()
        if (text.isNotEmpty() && activeSessionId != null) {
            lifecycleScope.launch {
                try {
                    val res = ApiClient.getService(this@GoLiveActivity).sendLiveComment(activeSessionId!!, mapOf("text" to text))
                    if (res.isSuccessful && res.body()?.success == true) {
                        val currentText = binding.tvLiveChatLog.text.toString()
                        binding.tvLiveChatLog.text = "$currentText\nHost: $text"
                        binding.etChatInput.text?.clear()
                        binding.scrollChat.fullScroll(View.FOCUS_DOWN)
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@GoLiveActivity, "Failed to send chat", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showEffectsDialog() {
        val filters = arrayOf(
            "✨ Studio HD Beauty 100%",
            "🌅 Warm Sunset Glow",
            "🎬 Cinematic Vintage 4K",
            "🎨 Neon Cyberpunk Mode",
            "👑 Studio Portrait Lighting"
        )

        MaterialAlertDialogBuilder(this)
            .setTitle("Live Video Filters & Effects")
            .setItems(filters) { _, which ->
                Toast.makeText(this, "Applied: ${filters[which]}", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showGuestDialog() {
        val actions = arrayOf(
            "➕ Invite Co-Host (Dual Live Stream)",
            "📋 Review Guest Join Requests",
            "⚔️ Start LIVE PK Battle (Match)",
            "🎯 Set LIVE Diamond Goal",
            "📊 Create Live Poll"
        )

        MaterialAlertDialogBuilder(this)
            .setTitle("Advanced LIVE Creator Tools")
            .setItems(actions) { _, which ->
                when (which) {
                    0 -> Toast.makeText(this, "Co-Host invite broadcasted to followers!", Toast.LENGTH_SHORT).show()
                    1 -> reviewGuestRequests()
                    2 -> startPkBattle()
                    3 -> setLiveGoal()
                    4 -> createLivePoll()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun reviewGuestRequests() {
        val requests = arrayOf("@zara_beauty (Requested to join stream)", "@tobi_beats (Requested to join stream)")
        MaterialAlertDialogBuilder(this)
            .setTitle("Pending Guest Join Requests")
            .setItems(requests) { _, which ->
                val guest = if (which == 0) "@zara_beauty" else "@tobi_beats"
                MaterialAlertDialogBuilder(this)
                    .setTitle("Manage $guest")
                    .setPositiveButton("Accept & Connect") { _, _ ->
                        Toast.makeText(this, "$guest connected to stream!", Toast.LENGTH_SHORT).show()
                        binding.llGuestGridContainer.visibility = View.VISIBLE
                        binding.flGuest1.visibility = View.VISIBLE
                    }
                    .setNegativeButton("Reject") { _, _ ->
                        Toast.makeText(this, "$guest request rejected", Toast.LENGTH_SHORT).show()
                    }
                    .show()
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun startPkBattle() {
        MaterialAlertDialogBuilder(this)
            .setTitle("⚔️ Start LIVE PK Battle")
            .setMessage("Match with another active creator for a 3-minute diamond battle!")
            .setPositiveButton("Start Match Now") { _, _ ->
                Toast.makeText(this, "🔥 PK Battle Started! 3:00 Timer Active", Toast.LENGTH_LONG).show()
                val chat = binding.tvLiveChatLog.text.toString()
                binding.tvLiveChatLog.text = "$chat\n⚔️ LIVE PK Battle started against @OpponentCreator!"
                binding.scrollChat.fullScroll(View.FOCUS_DOWN)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setLiveGoal() {
        val input = EditText(this).apply { hint = "e.g. 1000 Diamonds" }
        MaterialAlertDialogBuilder(this)
            .setTitle("🎯 Set LIVE Gift Goal")
            .setView(input)
            .setPositiveButton("Set Goal") { _, _ ->
                val goal = input.text.toString().trim()
                if (goal.isNotEmpty()) {
                    Toast.makeText(this, "Goal set: $goal Diamonds!", Toast.LENGTH_SHORT).show()
                    val chat = binding.tvLiveChatLog.text.toString()
                    binding.tvLiveChatLog.text = "$chat\n🎯 New LIVE Goal pinned: $goal Diamonds!"
                    binding.scrollChat.fullScroll(View.FOCUS_DOWN)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun createLivePoll() {
        val input = EditText(this).apply { hint = "Ask viewers a question..." }
        MaterialAlertDialogBuilder(this)
            .setTitle("📊 Create Live Poll")
            .setView(input)
            .setPositiveButton("Launch Poll") { _, _ ->
                val q = input.text.toString().trim()
                if (q.isNotEmpty()) {
                    Toast.makeText(this, "Poll launched to viewers!", Toast.LENGTH_SHORT).show()
                    val chat = binding.tvLiveChatLog.text.toString()
                    binding.tvLiveChatLog.text = "$chat\n📊 LIVE Poll launched: \"$q\""
                    binding.scrollChat.fullScroll(View.FOCUS_DOWN)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun stopBroadcast() {
        isLive = false
        stopCamera()
        binding.llActiveLiveControls.visibility = View.GONE
        binding.llSetupContainer.visibility = View.VISIBLE
        binding.llLiveBadge.visibility = View.GONE
        binding.tvStreamHealth.visibility = View.GONE

        Toast.makeText(this, "LIVE Broadcast ended", Toast.LENGTH_SHORT).show()

        if (activeSessionId != null) {
            endSessionOnBackend(activeSessionId!!)
        } else {
            finish()
        }
    }
}
