package com.streamhub.tv.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import android.content.Context
import com.streamhub.tv.BuildConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "streamhub_prefs")

/** Typed keys for every user-configurable app setting. */
object PrefKeys {
    val REPO_URL = stringPreferencesKey("repo_url")
    val THEME_MODE = stringPreferencesKey("theme_mode") // "dark" | "light" | "system"
    val AUTO_UPDATE = booleanPreferencesKey("auto_update")
    val APP_LANGUAGE = stringPreferencesKey("app_language") // "en" | "ar" | "fr"
    val LAST_SYNC_AT = stringPreferencesKey("last_sync_at")
}

enum class ThemeMode { DARK, LIGHT, SYSTEM }

/** Thin wrapper exposing typed Flows over DataStore, injected via Hilt into SettingsRepository. */
class PreferencesManager(private val context: Context) {

    val repoUrl: Flow<String> = context.dataStore.data.map {
        it[PrefKeys.REPO_URL] ?: BuildConfig.DEFAULT_CHANNELS_URL
    }

    val themeMode: Flow<ThemeMode> = context.dataStore.data.map {
        when (it[PrefKeys.THEME_MODE]) {
            "light" -> ThemeMode.LIGHT
            "system" -> ThemeMode.SYSTEM
            else -> ThemeMode.DARK
        }
    }

    val autoUpdate: Flow<Boolean> = context.dataStore.data.map { it[PrefKeys.AUTO_UPDATE] ?: true }

    val language: Flow<String> = context.dataStore.data.map { it[PrefKeys.APP_LANGUAGE] ?: "en" }

    val lastSyncAt: Flow<String> = context.dataStore.data.map { it[PrefKeys.LAST_SYNC_AT] ?: "" }

    suspend fun setRepoUrl(url: String) {
        context.dataStore.edit { it[PrefKeys.REPO_URL] = url }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[PrefKeys.THEME_MODE] = mode.name.lowercase() }
    }

    suspend fun setAutoUpdate(enabled: Boolean) {
        context.dataStore.edit { it[PrefKeys.AUTO_UPDATE] = enabled }
    }

    suspend fun setLanguage(lang: String) {
        context.dataStore.edit { it[PrefKeys.APP_LANGUAGE] = lang }
    }

    suspend fun setLastSyncAt(iso: String) {
        context.dataStore.edit { it[PrefKeys.LAST_SYNC_AT] = iso }
    }
}
