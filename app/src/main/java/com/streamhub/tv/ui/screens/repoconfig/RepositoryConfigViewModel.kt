package com.streamhub.tv.ui.screens.repoconfig

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streamhub.tv.data.repository.ChannelRepository
import com.streamhub.tv.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RepositoryConfigViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val channelRepository: ChannelRepository
) : ViewModel() {

    val repoUrl: StateFlow<String> = settingsRepository.repoUrl.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), ""
    )

    fun saveUrl(url: String) {
        viewModelScope.launch {
            settingsRepository.setRepoUrl(url.trim())
            channelRepository.fetchChannels(forceRefresh = true)
        }
    }
}
