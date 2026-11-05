package c.app.sociamediabackend.ui.live

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import c.app.sociamediabackend.data.api.ApiClient
import c.app.sociamediabackend.data.api.LiveSocketManager
import c.app.sociamediabackend.data.api.SessionManager
import c.app.sociamediabackend.databinding.ActivityGoLiveStudioBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class GoLiveStudioActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGoLiveStudioBinding
    private var lensFacing = CameraSelector.LENS_FACING_FRONT
    private var activeSessionId: String? = null
    private var activeLayoutMode = "Side by Side"

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.CAMERA] == true && permissions[Manifest.permission.RECORD_AUDIO] == true) {
            startCamera()
        } else {
            Toast.makeText(this, "Camera & Audio permissions are required for live streaming", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityGoLiveStudioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.root.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val hasCamera = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        val hasAudio = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED

        if (hasCamera && hasAudio) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO))
        }

        binding.btnCloseLive.setOnClickListener {
            endLiveSession()
            finish()
        }

        binding.btnLiveSettings.setOnClickListener {
            val settingsSheet = LiveSettingsBottomSheetDialogFragment { layoutMode ->
                applyLayoutMode(layoutMode)
            }
            settingsSheet.show(supportFragmentManager, LiveSettingsBottomSheetDialogFragment.TAG)
        }

        binding.btnStudioGifts.setOnClickListener {
            val giftsSheet = GiftBottomSheetDialogFragment(activeSessionId ?: "default_live") { gift ->
                Toast.makeText(this, "Received Gift: ${gift.name} ${gift.iconUrl}", Toast.LENGTH_SHORT).show()
                val log = binding.tvJoinNotifications.text.toString()
                binding.tvJoinNotifications.text = "$log\n💎 Gift sent: ${gift.name} ${gift.iconUrl}"
                binding.scrollJoinNotifications.fullScroll(View.FOCUS_DOWN)
            }
            giftsSheet.show(supportFragmentManager, GiftBottomSheetDialogFragment.TAG)
        }

        setupInviteSlots()
        setupBottomToolbarActions()
        createLiveSessionOnBackend()
    }

    private fun applyLayoutMode(mode: String) {
        activeLayoutMode = mode
        binding.layoutSideBySide.visibility = View.GONE
        binding.layoutSpotlight.visibility = View.GONE
        binding.layoutRoundtable1.visibility = View.GONE
        binding.layoutRoundtable2.visibility = View.GONE

        when (mode) {
            "Side by Side" -> binding.layoutSideBySide.visibility = View.VISIBLE
            "Spotlight" -> binding.layoutSpotlight.visibility = View.VISIBLE
            "Roundtable I" -> binding.layoutRoundtable1.visibility = View.VISIBLE
            "Roundtable II" -> binding.layoutRoundtable2.visibility = View.VISIBLE
            else -> binding.layoutSideBySide.visibility = View.VISIBLE
        }

        startCamera()
        Toast.makeText(this, "Switched layout to $mode", Toast.LENGTH_SHORT).show()
    }

    private fun setupInviteSlots() {
        val inviteClickListener = View.OnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("➕ Invite Guest or Co-Host")
                .setMessage("Send a live invitation to your followers to join this stream as a guest?")
                .setPositiveButton("Send Invite") { _, _ ->
                    Toast.makeText(this, "Live invitation sent to followers!", Toast.LENGTH_SHORT).show()
                    val log = binding.tvJoinNotifications.text.toString()
                    binding.tvJoinNotifications.text = "$log\n⚡ Invitation sent to guest!"
                    binding.scrollJoinNotifications.fullScroll(View.FOCUS_DOWN)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        for (i in 1 until binding.layoutRoundtable2.childCount) {
            val child = binding.layoutRoundtable2.getChildAt(i)
            child.setOnClickListener(inviteClickListener)
        }
    }

    private fun setupBottomToolbarActions() {
        binding.llBottomToolbar.getChildAt(0).setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("🤝 Co-Host Match")
                .setMessage("Search and invite another active creator for a live PK Battle / Co-Host stream?")
                .setPositiveButton("Find Co-Host") { _, _ ->
                    Toast.makeText(this, "Searching for co-host creators...", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        binding.llBottomToolbar.getChildAt(1).setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("👥 Guest Management")
                .setMessage("Manage active connected guests, mute mics, or remove participants.")
                .setPositiveButton("View Requests") { _, _ ->
                    Toast.makeText(this, "Approved guest to join stream", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Close", null)
                .show()
        }

        binding.llBottomToolbar.getChildAt(2).setOnClickListener {
            Toast.makeText(this, "Comments and chat active", Toast.LENGTH_SHORT).show()
        }

        binding.llBottomToolbar.getChildAt(3).setOnClickListener {
            val tools = arrayOf("❓ Start LIVE Q&A", "📊 Create Live Poll", "🎯 Set Live Goal", "⚔️ Start LIVE Match (PK)")
            MaterialAlertDialogBuilder(this)
                .setTitle("🛠️ Creator Tools & Widgets")
                .setItems(tools) { _, which ->
                    Toast.makeText(this, "Activated: ${tools[which]}", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        binding.llBottomToolbar.getChildAt(4).setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "Join my Fugen LIVE stream!")
                putExtra(Intent.EXTRA_TEXT, "Watch my LIVE stream on Fugen right now! https://fugen.app/live")
            }
            startActivity(Intent.createChooser(shareIntent, "Share LIVE via"))
        }

        binding.llBottomToolbar.getChildAt(5).setOnClickListener {
            val effects = arrayOf("✨ Beauty Glow", "🌈 Vibrant Boost", "🎬 Cinematic Filter", "🖤 Noir Black & White", "⚡ Cyberpunk Neon")
            MaterialAlertDialogBuilder(this)
                .setTitle("✨ Live Filters & AR Effects")
                .setItems(effects) { _, which ->
                    Toast.makeText(this, "Applied: ${effects[which]}", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun createLiveSessionOnBackend() {
        lifecycleScope.launch {
            try {
                val req = mapOf("title" to "Fugen Creator Studio LIVE 🔥", "category" to "Gaming")
                val res = ApiClient.getService(this@GoLiveStudioActivity).createLiveSession(req)
                if (res.isSuccessful && res.body()?.liveSession != null) {
                    activeSessionId = res.body()!!.liveSession!!.id
                    ApiClient.getService(this@GoLiveStudioActivity).startLiveSession(activeSessionId!!)

                    LiveSocketManager.connect(this@GoLiveStudioActivity) {
                        val sessionManager = SessionManager(this@GoLiveStudioActivity)
                        val userId = sessionManager.getUser()?.id ?: "creator_host"
                        LiveSocketManager.joinLiveSession(activeSessionId!!, userId)

                        LiveSocketManager.onEvent("live:participant_joined") { args ->
                            runOnUiThread {
                                val data = args.getOrNull(0) as? org.json.JSONObject
                                val username = data?.optString("username") ?: "Viewer"
                                val log = binding.tvJoinNotifications.text.toString()
                                binding.tvJoinNotifications.text = "$log\n👋 $username joined the live stream!"
                                binding.scrollJoinNotifications.fullScroll(View.FOCUS_DOWN)
                            }
                        }

                        LiveSocketManager.onEvent("live:comment_created") { args ->
                            runOnUiThread {
                                val data = args.getOrNull(0) as? org.json.JSONObject
                                val text = data?.optString("text") ?: ""
                                val username = data?.optString("username") ?: "User"
                                val log = binding.tvJoinNotifications.text.toString()
                                binding.tvJoinNotifications.text = "$log\n💬 $username: $text"
                                binding.scrollJoinNotifications.fullScroll(View.FOCUS_DOWN)
                            }
                        }

                        LiveSocketManager.onEvent("live:gift_sent") { args ->
                            runOnUiThread {
                                val data = args.getOrNull(0) as? org.json.JSONObject
                                val giftSlug = data?.optString("giftSlug") ?: "Gift"
                                val sender = data?.optString("senderName") ?: "Viewer"
                                val log = binding.tvJoinNotifications.text.toString()
                                binding.tvJoinNotifications.text = "$log\n🎁 $sender sent a gift: $giftSlug!"
                                binding.scrollJoinNotifications.fullScroll(View.FOCUS_DOWN)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                // Offline fallback
            }
        }
    }

    private fun endLiveSession() {
        if (activeSessionId != null) {
            lifecycleScope.launch {
                try {
                    ApiClient.getService(this@GoLiveStudioActivity).endLiveSession(activeSessionId!!)
                } catch (e: Exception) {
                    // Ignore
                }
            }
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            
            val targetPreview = when (activeLayoutMode) {
                "Side by Side" -> binding.previewViewHostSide
                "Spotlight" -> binding.previewViewHostSpotlight
                "Roundtable I" -> binding.previewViewHostRT1
                "Roundtable II" -> binding.previewViewHostRT2
                else -> binding.previewViewHostSide
            }

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(targetPreview.surfaceProvider)
            }
            val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview)
            } catch (e: Exception) {
                Toast.makeText(this, "Failed to start camera preview", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    override fun onDestroy() {
        super.onDestroy()
        endLiveSession()
    }
}
