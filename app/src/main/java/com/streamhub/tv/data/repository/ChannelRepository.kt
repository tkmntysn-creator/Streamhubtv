package com.streamhub.tv.data.repository

import com.streamhub.tv.data.local.ChannelCacheDao
import com.streamhub.tv.data.local.ChannelCacheEntity
import com.streamhub.tv.data.local.PreferencesManager
import com.streamhub.tv.data.model.Channel
import com.streamhub.tv.data.model.ChannelsResponse
import com.streamhub.tv.data.remote.ChannelApiService
import com.streamhub.tv.util.Resource
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Single source of truth for the live TV channel catalogue.
 *
 * Flow:
 *  1. Try to download the JSON from the user-configured GitHub raw URL.
 *  2. On success -> persist it to Room (offline cache) and return it.
 *  3. On failure (no network, 404, malformed JSON, timeout, etc.) -> fall back to the
 *     last successfully cached payload, if any, so the app still shows channels offline.
 *  4. If there is no cache either, surface an error to the UI.
 *
 * NOTE: channel content itself is never hardcoded - it always originates from JSON,
 * either freshly downloaded or previously cached.
 */
@Singleton
class ChannelRepository @Inject constructor(
    private val api: ChannelApiService,
    private val cacheDao: ChannelCacheDao,
    private val preferencesManager: PreferencesManager
) {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    suspend fun fetchChannels(forceRefresh: Boolean = false): Resource<List<Channel>> {
        val url = preferencesManager.repoUrl.first()

        return try {
            // Always revalidate on app/screen open. The old cache-first shortcut made
            // edits to the remote channels.json invisible forever on installed builds.
            // Keep Room as an offline fallback, while bypassing GitHub's raw edge cache.
            val requestUrl = if (url.contains("raw.githubusercontent.com")) {
                url + (if (url.contains("?")) "&" else "?") + "refresh=${System.currentTimeMillis()}"
            } else {
                url
            }
            val response = api.getChannels(requestUrl)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                persistCache(body, url)
                preferencesManager.setLastSyncAt(Instant.now().toString())
                Resource.Success(body.channels.filter { it.enabled })
            } else {
                fallbackToCache("Server returned ${response.code()}")
            }
        } catch (e: Exception) {
            fallbackToCache(e.message ?: "Network error")
        }
    }

    /** Returns cached channels only, without attempting a network call. Used for instant offline UI. */
    suspend fun getCachedChannels(): Resource<List<Channel>> = fallbackToCache(null)

    suspend fun clearCache() = cacheDao.clearCache()

    suspend fun getLastCachedAt(): Long? = cacheDao.getCache()?.cachedAt

    private suspend fun persistCache(response: ChannelsResponse, url: String) {
        cacheDao.saveCache(
            ChannelCacheEntity(
                json = json.encodeToString(ChannelsResponse.serializer(), response),
                sourceUrl = url
            )
        )
    }

    private suspend fun fallbackToCache(errorMessage: String?): Resource<List<Channel>> {
        val cached = cacheDao.getCache()
        return if (cached != null) {
            decodeCached(cached.json, errorMessage)
        } else {
            Resource.Error(errorMessage ?: "No channels available and no cache found")
        }
    }

    private fun decodeCached(
        payload: String,
        errorMessage: String? = null
    ): Resource<List<Channel>> = try {
        val parsed = json.decodeFromString(ChannelsResponse.serializer(), payload)
        Resource.Success(parsed.channels.filter { it.enabled })
    } catch (e: Exception) {
        Resource.Error(errorMessage ?: "Failed to read cached channel data")
    }
}
