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
import c.app.sociamediabackend.databinding.ActivityContentPreferencesBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class ContentPreferencesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityContentPreferencesBinding
    private lateinit var viewModel: SettingsViewModel
    private lateinit var repository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityContentPreferencesBinding.inflate(layoutInflater)
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
        binding.layoutScreenTimeLimit.setOnClickListener {
            val options = arrayOf("Disabled (Unlimited)", "30 Minutes / day", "60 Minutes / day", "90 Minutes / day", "120 Minutes / day")
            val minutes = intArrayOf(0, 30, 60, 90, 120)
            MaterialAlertDialogBuilder(this)
                .setTitle("Daily Screen Time Limit")
                .setItems(options) { _, which ->
                    viewModel.updateSetting("screenTimeLimitMinutes", minutes[which])
                    Toast.makeText(this, "Screen time limit set to ${options[which]}", Toast.LENGTH_SHORT).show()
                }
                .show()
        }

        binding.switchRestrictedMode.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateSetting("restrictedMode", isChecked)
        }

        binding.switchQuietHours.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateSetting("quietHoursEnabled", isChecked)
        }

        binding.layoutQuietHoursSchedule.setOnClickListener {
            val options = arrayOf("22:00 - 07:00 (Night)", "23:00 - 06:00", "00:00 - 08:00")
            MaterialAlertDialogBuilder(this)
                .setTitle("Quiet Hours Schedule")
                .setItems(options) { _, which ->
                    val selected = options[which].substringBefore(" ")
                    val parts = selected.split("-")
                    if (parts.size == 2) {
                        viewModel.updateSetting("quietHoursStart", parts[0].trim())
                        viewModel.updateSetting("quietHoursEnd", parts[1].trim())
                        Toast.makeText(this, "Quiet hours updated to $selected", Toast.LENGTH_SHORT).show()
                    }
                }
                .show()
        }

        binding.layoutAppLanguage.setOnClickListener {
            val languages = arrayOf("English (US)", "Español", "Français", "العربية", "日本語")
            val codes = arrayOf("en", "es", "fr", "ar", "ja")
            MaterialAlertDialogBuilder(this)
                .setTitle("App Language")
                .setItems(languages) { _, which ->
                    viewModel.updateSetting("appLanguage", codes[which])
                    Toast.makeText(this, "Language set to ${languages[which]}", Toast.LENGTH_SHORT).show()
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
                        binding.tvScreenTimeValue.text = if (s.screenTimeLimitMinutes > 0) "${s.screenTimeLimitMinutes} Minutes / day" else "Disabled (Unlimited)"
                        binding.switchRestrictedMode.isChecked = s.restrictedMode
                        binding.switchQuietHours.isChecked = s.quietHoursEnabled
                        binding.tvQuietHoursValue.text = "${s.quietHoursStart ?: "22:00"} - ${s.quietHoursEnd ?: "07:00"}"
                        binding.tvAppLanguageValue.text = when (s.appLanguage) {
                            "es" -> "Español"
                            "fr" -> "Français"
                            "ar" -> "العربية"
                            "ja" -> "日本語"
                            else -> "English (US)"
                        }
                    }
                }
            }
        }
    }
}
