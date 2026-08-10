package com.streamhub.tv.util

/**
 * Generic wrapper used across repositories/ViewModels to represent the state of an
 * asynchronous operation (network call, DB query, etc.) in a unified way for the UI.
 */
sealed class Resource<out T> {
    data class Success<T>(val data: T) : Resource<T>()
    data class Error(val message: String, val cachedData: Any? = null) : Resource<Nothing>()
    data object Loading : Resource<Nothing>()
}

object Constants {
    const val PREFS_NAME = "streamhub_settings"
    const val DEFAULT_REFRESH_INTERVAL_MINUTES = 60L
    const val CACHE_FILE_NAME = "channels_cache.json"
    const val NETWORK_TIMEOUT_SECONDS = 15L
}
