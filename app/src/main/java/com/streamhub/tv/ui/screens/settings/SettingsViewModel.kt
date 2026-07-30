package com.streamhub.tv.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streamhub.tv.data.local.ThemeMode
import com.streamhub.tv.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.DARK,
    val autoUpdate: Boolean = true,
    val language: String = "en",
    val repoUrl: String = "",
    val lastSyncAt: String = "",
    val cacheCleared: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState

    init {
        viewModelScope.launch {
            settingsRepository.themeMode.collect { _uiState.value = _uiState.value.copy(themeMode = it) }
        }
        viewModelScope.launch {
            settingsRepository.autoUpdate.collect { _uiState.value = _uiState.value.copy(autoUpdate = it) }
        }
        viewModelScope.launch {
            settingsRepository.language.collect { _uiState.value = _uiState.value.copy(language = it) }
        }
        viewModelScope.launch {
            settingsRepository.repoUrl.collect { _uiState.value = _uiState.value.copy(repoUrl = it) }
        }
        viewModelScope.launch {
            settingsRepository.lastSyncAt.collect { _uiState.value = _uiState.value.copy(lastSyncAt = it) }
        }
    }

    fun setThemeMode(mode: ThemeMode) = viewModelScope.launch { settingsRepository.setThemeMode(mode) }
    fun setAutoUpdate(enabled: Boolean) = viewModelScope.launch { settingsRepository.setAutoUpdate(enabled) }
    fun setLanguage(lang: String) = viewModelScope.launch { settingsRepository.setLanguage(lang) }

    fun clearCache() {
        viewModelScope.launch {
            settingsRepository.clearCache()
            _uiState.value = _uiState.value.copy(cacheCleared = true)
        }
    }

    fun cacheClearedHandled() {
        _uiState.value = _uiState.value.copy(cacheCleared = false)
    }
}
