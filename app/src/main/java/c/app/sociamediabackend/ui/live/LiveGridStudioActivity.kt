package c.app.sociamediabackend.ui.live

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import c.app.sociamediabackend.databinding.ActivityLiveGridStudioBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class LiveGridStudioActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLiveGridStudioBinding
    private lateinit var adapter: LiveGridAdapter
    private var currentMode = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityLiveGridStudioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.root.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.btnClose.setOnClickListener { finish() }

        adapter = LiveGridAdapter(
            onParticipantClicked = { p -> Toast.makeText(this, "Tapped @${p.name}", Toast.LENGTH_SHORT).show() },
            onParticipantLongClicked = { p -> Toast.makeText(this, "Manage @${p.name}", Toast.LENGTH_SHORT).show() },
            onInviteClicked = { Toast.makeText(this, "Invite slot tapped", Toast.LENGTH_SHORT).show() }
        )

        binding.rvLiveGridStudio.layoutManager = GridLayoutManager(this, 1)
        binding.rvLiveGridStudio.adapter = adapter

        applyLayout(1)

        binding.btnSwitchLayout.setOnClickListener { showLayoutDialog() }
        binding.btnLayoutModal.setOnClickListener { showLayoutDialog() }
    }

    private fun showLayoutDialog() {
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
            .setTitle("Fugen Live Grid Layout Guide")
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
                applyLayout(mode)
                Toast.makeText(this, "Applied ${layouts[which]}", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun applyLayout(mode: Int) {
        currentMode = mode
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
        binding.rvLiveGridStudio.layoutManager = GridLayoutManager(this, spanCount)

        list.add(LiveParticipant("1", "Host", "", isHost = true, isActiveSpeaker = true))
        adapter.setParticipants(list)
    }
}
