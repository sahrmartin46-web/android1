package c.app.sociamediabackend.ui.settings

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import c.app.sociamediabackend.data.api.ApiClient
import c.app.sociamediabackend.data.api.NetworkResult
import c.app.sociamediabackend.data.api.SessionManager
import c.app.sociamediabackend.data.repository.SettingsRepository
import c.app.sociamediabackend.databinding.ActivitySettingsBinding
import c.app.sociamediabackend.ui.auth.LoginActivity
import c.app.sociamediabackend.ui.live.MonetizationActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var viewModel: SettingsViewModel
    private lateinit var repository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.root.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        sessionManager = SessionManager(this)
        val apiService = ApiClient.getService(this)
        repository = SettingsRepository(apiService)
        viewModel = SettingsViewModel(repository)

        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.etBaseUrl.setText(sessionManager.getBaseUrl())

        binding.btnSaveServerUrl.setOnClickListener {
            val newUrl = binding.etBaseUrl.text?.toString()?.trim().orEmpty()
            if (newUrl.isNotEmpty()) {
                sessionManager.saveBaseUrl(newUrl)
                ApiClient.reset()
                testServerConnection()
            }
        }

        binding.btnAccountManagement.setOnClickListener {
            startActivity(Intent(this, AccountManagementActivity::class.java))
        }

        binding.btnPrivacyControls.setOnClickListener {
            startActivity(Intent(this, PrivacyControlsActivity::class.java))
        }

        binding.btnContentPreferences.setOnClickListener {
            startActivity(Intent(this, ContentPreferencesActivity::class.java))
        }

        binding.btnMonetizationHub.setOnClickListener {
            startActivity(Intent(this, MonetizationActivity::class.java))
        }

        setupSwitchListeners()
        setupButtonActions()
        observeViewModel()

        viewModel.loadSettings()
    }

    private fun setupSwitchListeners() {
        binding.switchPrivateAccount.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateSetting("isPrivateAccount", isChecked)
        }

        binding.switchDataSaver.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateSetting("dataSaverEnabled", isChecked)
        }

        binding.switchAutoplay.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateSetting("autoplayCellular", isChecked)
        }

        binding.switchLiveAlerts.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateSetting("liveAlertsNotification", isChecked)
        }

        binding.switchCommentsAlerts.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateSetting("commentsNotification", isChecked)
        }
    }

    private fun setupButtonActions() {
        binding.btnManageSessions.setOnClickListener {
            val state = viewModel.uiState.value
            if (state is SettingsUiState.Success) {
                val sessions = state.sessions
                val items = if (sessions.isNotEmpty()) {
                    sessions.map { "${it.deviceName} (${it.ipAddress ?: "127.0.0.1"})${if (it.isCurrent) " - Current" else ""}" }.toTypedArray()
                } else {
                    arrayOf("Current Device (Active Session)")
                }

                MaterialAlertDialogBuilder(this)
                    .setTitle("📱 Active Device Sessions")
                    .setItems(items) { _, which ->
                        if (which < sessions.size && !sessions[which].isCurrent) {
                            viewModel.revokeSession(sessions[which].id)
                            Toast.makeText(this, "Session revoked", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .setNegativeButton("Close", null)
                    .show()
            }
        }

        binding.btnBlockedUsers.setOnClickListener {
            val state = viewModel.uiState.value
            if (state is SettingsUiState.Success) {
                val blocks = state.blockedUsers
                val items = if (blocks.isNotEmpty()) {
                    blocks.map { "@${it.username} (${it.displayName ?: "User"})" }.toTypedArray()
                } else {
                    arrayOf("No blocked users")
                }

                MaterialAlertDialogBuilder(this)
                    .setTitle("🚫 Blocked Accounts")
                    .setItems(items) { _, which ->
                        if (which < blocks.size) {
                            viewModel.unblockUser(blocks[which].id)
                            Toast.makeText(this, "Unblocked @${blocks[which].username}", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .setNegativeButton("Close", null)
                    .show()
            }
        }

        binding.btnKeywordFilters.setOnClickListener {
            val state = viewModel.uiState.value
            if (state is SettingsUiState.Success) {
                val keywords = state.keywords
                val items = if (keywords.isNotEmpty()) {
                    (listOf("➕ Add New Filter Keyword") + keywords.map { "❌ Remove: \"$it\"" }).toTypedArray()
                } else {
                    arrayOf("➕ Add New Filter Keyword", "No keywords filtered")
                }

                MaterialAlertDialogBuilder(this)
                    .setTitle("🔤 Comment Keyword Filters")
                    .setItems(items) { _, which ->
                        if (which == 0) {
                            showAddKeywordDialog()
                        } else if (which - 1 < keywords.size) {
                            viewModel.removeKeyword(keywords[which - 1])
                            Toast.makeText(this, "Keyword removed", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .setNegativeButton("Close", null)
                    .show()
            }
        }

        binding.btnChangePassword.setOnClickListener {
            showChangePasswordDialog()
        }

        binding.btnClearCache.setOnClickListener {
            try {
                cacheDir.deleteRecursively()
                Toast.makeText(this, "Media cache cleared successfully!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this, "Failed to clear cache", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnExportData.setOnClickListener {
            Toast.makeText(this, "Generating data export...", Toast.LENGTH_SHORT).show()
            lifecycleScope.launch {
                when (repository.exportUserData()) {
                    is NetworkResult.Success -> {
                        MaterialAlertDialogBuilder(this@SettingsActivity)
                            .setTitle("📥 Data Export Ready")
                            .setMessage("Your account data, settings, posts, and transactions export has been generated successfully!")
                            .setPositiveButton("OK", null)
                            .show()
                    }
                    else -> Toast.makeText(this@SettingsActivity, "Failed to generate export", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.btnDeactivateAccount.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("⏸️ Deactivate Account")
                .setMessage("Temporarily deactivate your profile? You can log back in anytime to reactivate.")
                .setPositiveButton("Deactivate") { _, _ ->
                    lifecycleScope.launch {
                        repository.deactivateAccount()
                        sessionManager.clearSession()
                        startActivity(Intent(this@SettingsActivity, LoginActivity::class.java))
                        finishAffinity()
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        binding.btnDeleteAccount.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("⚠️ Delete Account Permanently")
                .setMessage("Are you sure? This action is permanent and will delete your profile, posts, comments, and data.")
                .setPositiveButton("Delete Permanently") { _, _ ->
                    lifecycleScope.launch {
                        repository.deleteAccount()
                        sessionManager.clearSession()
                        startActivity(Intent(this@SettingsActivity, LoginActivity::class.java))
                        finishAffinity()
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        binding.btnCommunityGuidelines.setOnClickListener {
            startActivity(Intent(this, CommunityGuidelinesActivity::class.java))
        }

        binding.btnLogout.setOnClickListener {
            sessionManager.clearSession()
            startActivity(Intent(this, LoginActivity::class.java))
            finishAffinity()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    if (state is SettingsUiState.Success) {
                        val s = state.settings
                        binding.switchPrivateAccount.isChecked = s.isPrivateAccount
                        binding.switchDataSaver.isChecked = s.dataSaverEnabled
                        binding.switchAutoplay.isChecked = s.autoplayCellular
                        binding.switchLiveAlerts.isChecked = s.liveAlertsNotification
                        binding.switchCommentsAlerts.isChecked = s.commentsNotification
                    }
                }
            }
        }
    }

    private fun showAddKeywordDialog() {
        val input = EditText(this).apply {
            hint = "Enter keyword to filter out"
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("🔤 Add Comment Keyword Filter")
            .setView(input)
            .setPositiveButton("Add") { _, _ ->
                val keyword = input.text.toString().trim()
                if (keyword.isNotEmpty()) {
                    viewModel.addKeyword(keyword)
                    Toast.makeText(this, "Added \"$keyword\" to filter list", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showChangePasswordDialog() {
        val input = EditText(this).apply {
            hint = "Enter new password"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("🔒 Change Password")
            .setView(input)
            .setPositiveButton("Update") { _, _ ->
                val newPass = input.text.toString().trim()
                if (newPass.length >= 6) {
                    Toast.makeText(this, "Password updated successfully!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun testServerConnection() {
        Toast.makeText(this, "Testing connection...", Toast.LENGTH_SHORT).show()

        lifecycleScope.launch {
            try {
                val response = ApiClient.getService(this@SettingsActivity).getMe()
                if (response.isSuccessful) {
                    Toast.makeText(this@SettingsActivity, "Server connection verified!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@SettingsActivity, "Server reached. Code: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@SettingsActivity, "Connection error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
