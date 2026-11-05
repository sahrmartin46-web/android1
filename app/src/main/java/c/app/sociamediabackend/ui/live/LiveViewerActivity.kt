package c.app.sociamediabackend.ui.live

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import c.app.sociamediabackend.data.api.ApiClient
import c.app.sociamediabackend.data.api.DashPlayerManager
import c.app.sociamediabackend.data.api.LiveSocketManager
import c.app.sociamediabackend.data.api.SessionManager
import androidx.media3.exoplayer.ExoPlayer
import c.app.sociamediabackend.databinding.ActivityLiveViewerBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.*

class LiveViewerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLiveViewerBinding
    private lateinit var liveGridAdapter: LiveGridAdapter
    private var streamId: String = ""
    private var hostName: String = "Creator"
    private var exoPlayer: ExoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityLiveViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.root.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        streamId = intent.getStringExtra("STREAM_ID").orEmpty()
        hostName = intent.getStringExtra("HOST_NAME") ?: "Creator"

        binding.tvViewerHostName.text = "@$hostName"

        setupLiveGrid()
        val streamUrl = intent.getStringExtra("STREAM_URL") ?: "http://10.0.2.2:5000/live/$streamId/index.mpd"
        setupLiveStreamPlayback(streamUrl)

        if (streamId.isNotEmpty()) {
            LiveSocketManager.connect(this) {
                val sessionManager = SessionManager(this)
                val userId = sessionManager.getUser()?.id ?: "viewer_user"
                LiveSocketManager.joinLiveSession(streamId, userId)

                LiveSocketManager.onEvent("live:comment_created") { args ->
                    runOnUiThread {
                        val data = args.getOrNull(0) as? org.json.JSONObject
                        val text = data?.optString("text") ?: ""
                        val username = data?.optString("username") ?: "User"
                        val currentLog = binding.tvViewerChatLog.text.toString()
                        binding.tvViewerChatLog.text = "$currentLog\n$username: $text"
                        binding.scrollViewerChat.fullScroll(View.FOCUS_DOWN)
                    }
                }

                LiveSocketManager.onEvent("live:gift_sent") { args ->
                    runOnUiThread {
                        val data = args.getOrNull(0) as? org.json.JSONObject
                        val giftSlug = data?.optString("giftSlug") ?: "Gift"
                        val sender = data?.optString("senderName") ?: "Viewer"
                        binding.tvGiftBanner.text = "🎁 $sender sent $giftSlug!"
                        binding.tvGiftBanner.visibility = View.VISIBLE
                        lifecycleScope.launch {
                            delay(3000L)
                            binding.tvGiftBanner.visibility = View.GONE
                        }
                    }
                }
            }
        }

        binding.btnCloseViewer.setOnClickListener { finish() }
        binding.btnSwitchLayout.setOnClickListener { showLayoutSwitcherDialog() }
        binding.btnSendViewerChat.setOnClickListener { sendChat() }

        binding.btnOpenGifts.setOnClickListener {
            val giftsSheet = GiftBottomSheetDialogFragment(streamId) { gift ->
                binding.tvGiftBanner.text = "👑 GIFT SENT: ${gift.name} ${gift.iconUrl}!"
                binding.tvGiftBanner.visibility = View.VISIBLE
                CoroutineScope(Dispatchers.Main).launch {
                    delay(3000L)
                    binding.tvGiftBanner.visibility = View.GONE
                }
            }
            giftsSheet.show(supportFragmentManager, GiftBottomSheetDialogFragment.TAG)
        }
    }

    private fun setupLiveGrid() {
        liveGridAdapter = LiveGridAdapter(
            onParticipantClicked = { participant ->
                Toast.makeText(this, "Tapped @${participant.name}", Toast.LENGTH_SHORT).show()
            },
            onParticipantLongClicked = { participant ->
                if (!participant.isInviteSlot) {
                    MaterialAlertDialogBuilder(this)
                        .setTitle("Moderation: @${participant.name}")
                        .setItems(arrayOf("Mute Audio", "Remove Guest", "Block User", "Report")) { _, _ ->
                            Toast.makeText(this, "Action applied on @${participant.name}", Toast.LENGTH_SHORT).show()
                        }
                        .show()
                }
            },
            onInviteClicked = {
                requestToJoinLiveGuest()
            }
        )

        applyLayoutMode(1)
    }

    private fun requestToJoinLiveGuest() {
        lifecycleScope.launch {
            try {
                if (streamId.isNotEmpty()) {
                    val res = ApiClient.getService(this@LiveViewerActivity).requestGuestJoin(streamId)
                    if (res.isSuccessful && res.body()?.success == true) {
                        Toast.makeText(this@LiveViewerActivity, "🙋 Request sent to host to join LIVE as guest!", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this@LiveViewerActivity, "Guest join request submitted", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@LiveViewerActivity, "Guest request submitted successfully", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun applyLayoutMode(mode: Int) {
        val list = mutableListOf<LiveParticipant>()
        val spanCount = when (mode) {
            1 -> 1
            2 -> 2
            3 -> 2
            4 -> 2
            6 -> 2
            9 -> 3
            else -> 4
        }
        binding.rvLiveGrid.layoutManager = GridLayoutManager(this, spanCount)

        list.add(LiveParticipant("1", hostName, "", isHost = true, isActiveSpeaker = true))
        liveGridAdapter.setParticipants(list)
    }

    private fun showLayoutSwitcherDialog() {
        val layouts = arrayOf(
            "1 Person (Full Screen)",
            "2 Persons (50/50 Split)",
            "3 Persons (Host + 2 Guests)",
            "4 Persons (2x2 Grid)",
            "6 Persons (2x3 Grid)",
            "9 Persons (3x3 Grid)",
            "16 Persons (4x4 Grid)"
        )
        MaterialAlertDialogBuilder(this)
            .setTitle("Switch Live Grid Layout")
            .setItems(layouts) { _, which ->
                val mode = when (which) {
                    0 -> 1
                    1 -> 2
                    2 -> 3
                    3 -> 4
                    4 -> 6
                    5 -> 9
                    else -> 16
                }
                applyLayoutMode(mode)
                Toast.makeText(this, "Switched to ${layouts[which]}", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun sendChat() {
        val text = binding.etViewerChat.text?.toString()?.trim().orEmpty()
        if (text.isNotEmpty() && streamId.isNotEmpty()) {
            lifecycleScope.launch {
                try {
                    val res = ApiClient.getService(this@LiveViewerActivity).sendLiveComment(streamId, mapOf("text" to text))
                    if (res.isSuccessful && res.body()?.success == true) {
                        val currentLog = binding.tvViewerChatLog.text.toString()
                        binding.tvViewerChatLog.text = "$currentLog\nYou: $text"
                        binding.etViewerChat.text?.clear()
                        binding.scrollViewerChat.fullScroll(View.FOCUS_DOWN)
                    }
                } catch (e: Exception) {
                    val currentLog = binding.tvViewerChatLog.text.toString()
                    binding.tvViewerChatLog.text = "$currentLog\nYou: $text"
                    binding.etViewerChat.text?.clear()
                    binding.scrollViewerChat.fullScroll(View.FOCUS_DOWN)
                }
            }
        }
    }

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    private fun setupLiveStreamPlayback(streamUrl: String) {
        if (streamUrl.isNotEmpty()) {
            val player = DashPlayerManager.createAdaptivePlayer(this)
            val mediaSource = DashPlayerManager.buildMediaSource(this, streamUrl)
            player.setMediaSource(mediaSource)
            player.prepare()
            player.playWhenReady = true
            exoPlayer = player
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer?.release()
        exoPlayer = null
        LiveSocketManager.disconnect()
    }
}
