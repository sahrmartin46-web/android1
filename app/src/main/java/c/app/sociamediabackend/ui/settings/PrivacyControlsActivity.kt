package c.app.sociamediabackend.ui.settings

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import c.app.sociamediabackend.data.api.ApiClient
import c.app.sociamediabackend.data.repository.SettingsRepository
import c.app.sociamediabackend.databinding.ActivityPrivacyControlsBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class PrivacyControlsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPrivacyControlsBinding
    private lateinit var viewModel: SettingsViewModel
    private lateinit var repository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityPrivacyControlsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.root.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val apiService = ApiClient.getService(this)
        repository = SettingsRepository(apiService)
        viewModel = SettingsViewModel(repository)

        binding.toolbar.setNavigationOnClickListener { finish() }

        setupListeners()
        observeViewModel()

        viewModel.loadSettings()
    }

    private fun setupListeners() {
        binding.switchPrivateAccount.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateSetting("isPrivateAccount", isChecked)
        }

        binding.layoutPostAudience.setOnClickListener {
            val options = arrayOf("Everyone", "Friends", "Only Me")
            val keys = arrayOf("EVERYONE", "FRIENDS", "ONLY_ME")
            MaterialAlertDialogBuilder(this)
                .setTitle("Default Post Audience")
                .setItems(options) { _, which ->
                    viewModel.updateSetting("postAudience", keys[which])
                    Toast.makeText(this, "Post audience set to ${options[which]}", Toast.LENGTH_SHORT).show()
                }
                .show()
        }

        binding.layoutCommentPerm.setOnClickListener {
            val options = arrayOf("Everyone", "Followers", "Friends", "No One")
            val keys = arrayOf("EVERYONE", "FOLLOWERS", "FRIENDS", "NO_ONE")
            MaterialAlertDialogBuilder(this)
                .setTitle("Who Can Comment")
                .setItems(options) { _, which ->
                    viewModel.updateSetting("commentPermission", keys[which])
                    Toast.makeText(this, "Comment permission set to ${options[which]}", Toast.LENGTH_SHORT).show()
                }
                .show()
        }

        binding.layoutDMPerm.setOnClickListener {
            val options = arrayOf("Everyone", "Friends", "No One")
            val keys = arrayOf("EVERYONE", "FRIENDS", "NO_ONE")
            MaterialAlertDialogBuilder(this)
                .setTitle("Who Can Send Direct Messages")
                .setItems(options) { _, which ->
                    viewModel.updateSetting("directMessagePermission", keys[which])
                    Toast.makeText(this, "DM permission set to ${options[which]}", Toast.LENGTH_SHORT).show()
                }
                .show()
        }

        binding.layoutDuetPerm.setOnClickListener {
            val options = arrayOf("Everyone", "Friends", "No One")
            val keys = arrayOf("EVERYONE", "FRIENDS", "NO_ONE")
            MaterialAlertDialogBuilder(this)
                .setTitle("Who Can Duet & Stitch")
                .setItems(options) { _, which ->
                    viewModel.updateSetting("duetPermission", keys[which])
                    Toast.makeText(this, "Duet permission set to ${options[which]}", Toast.LENGTH_SHORT).show()
                }
                .show()
        }

        binding.layoutDownloadPerm.setOnClickListener {
            val options = arrayOf("Everyone", "No One")
            val keys = arrayOf("EVERYONE", "NO_ONE")
            MaterialAlertDialogBuilder(this)
                .setTitle("Allow Video Downloads")
                .setItems(options) { _, which ->
                    viewModel.updateSetting("downloadPermission", keys[which])
                    Toast.makeText(this, "Download permission set to ${options[which]}", Toast.LENGTH_SHORT).show()
                }
                .show()
        }

        binding.layoutMentionPerm.setOnClickListener {
            val options = arrayOf("Everyone", "Followers", "No One")
            val keys = arrayOf("EVERYONE", "FOLLOWERS", "NO_ONE")
            MaterialAlertDialogBuilder(this)
                .setTitle("Who Can Mention & Tag You")
                .setItems(options) { _, which ->
                    viewModel.updateSetting("mentionPermission", keys[which])
                    Toast.makeText(this, "Mention permission set to ${options[which]}", Toast.LENGTH_SHORT).show()
                }
                .show()
        }

        binding.layoutLiveGuestPerm.setOnClickListener {
            val options = arrayOf("Everyone", "Followers", "Friends", "No One")
            val keys = arrayOf("EVERYONE", "FOLLOWERS", "FRIENDS", "NO_ONE")
            MaterialAlertDialogBuilder(this)
                .setTitle("LIVE Guest Permissions")
                .setItems(options) { _, which ->
                    viewModel.updateSetting("liveGuestPermission", keys[which])
                    Toast.makeText(this, "LIVE guest permission set to ${options[which]}", Toast.LENGTH_SHORT).show()
                }
                .show()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    if (state is SettingsUiState.Success) {
                        val s = state.settings
                        binding.switchPrivateAccount.isChecked = s.isPrivateAccount
                        binding.tvPostAudienceValue.text = formatLabel(s.postAudience)
                        binding.tvCommentPermValue.text = formatLabel(s.commentPermission)
                        binding.tvDMPermValue.text = formatLabel(s.directMessagePermission)
                        binding.tvDuetPermValue.text = formatLabel(s.duetPermission)
                        binding.tvDownloadPermValue.text = formatLabel(s.downloadPermission)
                        binding.tvMentionPermValue.text = formatLabel(s.mentionPermission)
                        binding.tvLiveGuestPermValue.text = formatLabel(s.liveGuestPermission)
                    }
                }
            }
        }
    }

    private fun formatLabel(key: String): String {
        return when (key) {
            "EVERYONE" -> "Everyone"
            "FOLLOWERS" -> "Followers"
            "FRIENDS" -> "Friends"
            "ONLY_ME" -> "Only Me"
            "NO_ONE" -> "No One"
            else -> key
        }
    }
}
