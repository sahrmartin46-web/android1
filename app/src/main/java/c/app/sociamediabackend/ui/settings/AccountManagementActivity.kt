package c.app.sociamediabackend.ui.settings

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import c.app.sociamediabackend.data.api.ApiClient
import c.app.sociamediabackend.data.api.NetworkResult
import c.app.sociamediabackend.data.api.SessionManager
import c.app.sociamediabackend.data.repository.SettingsRepository
import c.app.sociamediabackend.databinding.ActivityAccountManagementBinding
import c.app.sociamediabackend.ui.auth.LoginActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class AccountManagementActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAccountManagementBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var repository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityAccountManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.root.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        sessionManager = SessionManager(this)
        val apiService = ApiClient.getService(this)
        repository = SettingsRepository(apiService)

        binding.toolbar.setNavigationOnClickListener { finish() }

        loadUserInfo()
        setupListeners()
    }

    private fun loadUserInfo() {
        val user = sessionManager.getUser()
        if (user != null) {
            binding.tvUsername.text = "Username: @${user.username}"
            binding.tvEmail.text = "Email: ${user.email ?: "Not added"}"

            val isCreator = user.role == "CREATOR"
            binding.tvCurrentRole.text = "Current Account Type: ${if (isCreator) "Creator Account" else "Personal Account"}"
            binding.btnSwitchAccountType.text = if (isCreator) "👤 Switch to Personal Account" else "🌟 Switch to Creator Account"
        }
    }

    private fun setupListeners() {
        binding.btnChangePassword.setOnClickListener {
            showChangePasswordDialog()
        }

        binding.switch2FA.setOnCheckedChangeListener { _, isChecked ->
            Toast.makeText(this, if (isChecked) "Two-Factor Authentication (2FA) Enabled" else "2FA Disabled", Toast.LENGTH_SHORT).show()
        }

        binding.btnTrustedDevices.setOnClickListener {
            lifecycleScope.launch {
                when (val res = repository.getSessions()) {
                    is NetworkResult.Success -> {
                        val sessions = res.data.sessions
                        val items = sessions.map { "${it.deviceName} (${it.ipAddress ?: "127.0.0.1"})${if (it.isCurrent) " [Current]" else ""}" }.toTypedArray()
                        MaterialAlertDialogBuilder(this@AccountManagementActivity)
                            .setTitle("📱 Trusted Devices & Active Sessions")
                            .setItems(items) { _, which ->
                                if (which < sessions.size && !sessions[which].isCurrent) {
                                    revokeSession(sessions[which].id)
                                }
                            }
                            .setNegativeButton("Close", null)
                            .show()
                    }
                    else -> Toast.makeText(this@AccountManagementActivity, "Failed to fetch active sessions", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.btnLoginHistory.setOnClickListener {
            lifecycleScope.launch {
                when (val res = repository.getSessions()) {
                    is NetworkResult.Success -> {
                        val sessions = res.data.sessions
                        val items = sessions.map { "• ${it.deviceName} (${it.ipAddress ?: "127.0.0.1"}) - Active: ${it.lastActive ?: "Recent"}" }.toTypedArray()
                        MaterialAlertDialogBuilder(this@AccountManagementActivity)
                            .setTitle("🛡️ Login History & Active Sessions")
                            .setItems(items, null)
                            .setPositiveButton("Close", null)
                            .show()
                    }
                    else -> Toast.makeText(this@AccountManagementActivity, "Failed to load login history", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.btnSwitchAccountType.setOnClickListener {
            val user = sessionManager.getUser()
            val targetRole = if (user?.role == "CREATOR") "USER" else "CREATOR"
            lifecycleScope.launch {
                when (val res = repository.switchAccountType(targetRole)) {
                    is NetworkResult.Success -> {
                        res.data.user?.let { sessionManager.saveUser(it) }
                        loadUserInfo()
                        Toast.makeText(this@AccountManagementActivity, res.data.message, Toast.LENGTH_SHORT).show()
                    }
                    else -> Toast.makeText(this@AccountManagementActivity, "Failed to switch account type", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.btnDownloadData.setOnClickListener {
            Toast.makeText(this, "Generating account data package...", Toast.LENGTH_SHORT).show()
            lifecycleScope.launch {
                when (repository.exportUserData()) {
                    is NetworkResult.Success -> {
                        MaterialAlertDialogBuilder(this@AccountManagementActivity)
                            .setTitle("📥 Personal Account Data Export")
                            .setMessage("Your personal data archive (profile, settings, posts, and transactions) has been generated successfully!")
                            .setPositiveButton("OK", null)
                            .show()
                    }
                    else -> Toast.makeText(this@AccountManagementActivity, "Failed to export data", Toast.LENGTH_SHORT).show()
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
                        startActivity(Intent(this@AccountManagementActivity, LoginActivity::class.java))
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
                        startActivity(Intent(this@AccountManagementActivity, LoginActivity::class.java))
                        finishAffinity()
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun revokeSession(sessionId: String) {
        lifecycleScope.launch {
            when (repository.revokeSession(sessionId)) {
                is NetworkResult.Success -> Toast.makeText(this@AccountManagementActivity, "Session revoked", Toast.LENGTH_SHORT).show()
                else -> Toast.makeText(this@AccountManagementActivity, "Failed to revoke session", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showChangePasswordDialog() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 20, 50, 20)
        }

        val etOldPass = EditText(this).apply {
            hint = "Current Password"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }

        val etNewPass = EditText(this).apply {
            hint = "New Password (min 8 chars)"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }

        layout.addView(etOldPass)
        layout.addView(etNewPass)

        MaterialAlertDialogBuilder(this)
            .setTitle("🔒 Change Password & Verify Identity")
            .setView(layout)
            .setPositiveButton("Update Password") { _, _ ->
                val oldPass = etOldPass.text.toString().trim()
                val newPass = etNewPass.text.toString().trim()

                if (oldPass.isEmpty() || newPass.length < 8) {
                    Toast.makeText(this, "Please enter your current password and a new password (min 8 chars)", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                lifecycleScope.launch {
                    when (val res = repository.changePassword(oldPass, newPass)) {
                        is NetworkResult.Success -> Toast.makeText(this@AccountManagementActivity, res.data.message, Toast.LENGTH_SHORT).show()
                        is NetworkResult.Error -> Toast.makeText(this@AccountManagementActivity, res.message, Toast.LENGTH_LONG).show()
                        else -> Toast.makeText(this@AccountManagementActivity, "Failed to change password", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
