package c.app.sociamediabackend.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import c.app.sociamediabackend.data.api.NetworkResult
import c.app.sociamediabackend.data.model.*
import c.app.sociamediabackend.data.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface SettingsUiState {
    object Loading : SettingsUiState
    data class Success(
        val settings: UserSettings,
        val sessions: List<UserSession> = emptyList(),
        val blockedUsers: List<User> = emptyList(),
        val keywords: List<String> = emptyList()
    ) : SettingsUiState
    data class Error(val message: String) : SettingsUiState
}

class SettingsViewModel(private val repository: SettingsRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<SettingsUiState>(SettingsUiState.Loading)
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun loadSettings() {
        _uiState.value = SettingsUiState.Loading
        viewModelScope.launch {
            when (val result = repository.getSettings()) {
                is NetworkResult.Success -> {
                    val settings = result.data.settings ?: UserSettings()
                    _uiState.value = SettingsUiState.Success(settings = settings)
                    loadSessions()
                    loadBlockedUsers()
                    loadKeywordFilters()
                }
                is NetworkResult.Error -> {
                    _uiState.value = SettingsUiState.Error(result.message)
                }
                is NetworkResult.Exception -> {
                    _uiState.value = SettingsUiState.Error(result.throwable.localizedMessage ?: "Failed to load settings")
                }
            }
        }
    }

    fun updateSetting(key: String, value: Any) {
        val currentState = _uiState.value
        if (currentState is SettingsUiState.Success) {
            viewModelScope.launch {
                val updateMap = mapOf(key to value)
                when (val result = repository.updateSettings(updateMap)) {
                    is NetworkResult.Success -> {
                        val updated = result.data.settings ?: currentState.settings
                        _uiState.value = currentState.copy(settings = updated)
                    }
                    else -> {}
                }
            }
        }
    }

    private fun loadSessions() {
        viewModelScope.launch {
            when (val res = repository.getSessions()) {
                is NetworkResult.Success -> {
                    val currentState = _uiState.value
                    if (currentState is SettingsUiState.Success) {
                        _uiState.value = currentState.copy(sessions = res.data.sessions)
                    }
                }
                else -> {}
            }
        }
    }

    fun revokeSession(sessionId: String) {
        viewModelScope.launch {
            if (repository.revokeSession(sessionId) is NetworkResult.Success) {
                loadSessions()
            }
        }
    }

    private fun loadBlockedUsers() {
        viewModelScope.launch {
            when (val res = repository.getBlockedUsers()) {
                is NetworkResult.Success -> {
                    val currentState = _uiState.value
                    if (currentState is SettingsUiState.Success) {
                        _uiState.value = currentState.copy(blockedUsers = res.data.blockedUsers)
                    }
                }
                else -> {}
            }
        }
    }

    fun unblockUser(userId: String) {
        viewModelScope.launch {
            if (repository.unblockUser(userId) is NetworkResult.Success) {
                loadBlockedUsers()
            }
        }
    }

    private fun loadKeywordFilters() {
        viewModelScope.launch {
            when (val res = repository.getKeywordFilters()) {
                is NetworkResult.Success -> {
                    val currentState = _uiState.value
                    if (currentState is SettingsUiState.Success) {
                        _uiState.value = currentState.copy(keywords = res.data.keywords)
                    }
                }
                else -> {}
            }
        }
    }

    fun addKeyword(keyword: String) {
        viewModelScope.launch {
            if (repository.addKeywordFilter(keyword) is NetworkResult.Success) {
                loadKeywordFilters()
            }
        }
    }

    fun removeKeyword(keyword: String) {
        viewModelScope.launch {
            if (repository.deleteKeywordFilter(keyword) is NetworkResult.Success) {
                loadKeywordFilters()
            }
        }
    }
}
