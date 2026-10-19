package com.streamhub.tv.data.repository

import com.streamhub.tv.data.local.PreferencesManager
import com.streamhub.tv.data.local.ThemeMode
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val channelRepository: ChannelRepository
) {
    val repoUrl: Flow<String> = preferencesManager.repoUrl
    val themeMode: Flow<ThemeMode> = preferencesManager.themeMode
    val autoUpdate: Flow<Boolean> = preferencesManager.autoUpdate
    val language: Flow<String> = preferencesManager.language
    val lastSyncAt: Flow<String> = preferencesManager.lastSyncAt

    suspend fun setRepoUrl(url: String) = preferencesManager.setRepoUrl(url)
    suspend fun setThemeMode(mode: ThemeMode) = preferencesManager.setThemeMode(mode)
    suspend fun setAutoUpdate(enabled: Boolean) = preferencesManager.setAutoUpdate(enabled)
    suspend fun setLanguage(lang: String) = preferencesManager.setLanguage(lang)

    suspend fun clearCache() = channelRepository.clearCache()
}
