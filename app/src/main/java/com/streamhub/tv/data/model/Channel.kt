package com.streamhub.tv.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a single live TV channel.
 *
 * IMPORTANT: This model is NEVER hardcoded in the app. All channel instances are
 * deserialized at runtime from a remote `channels.json` file hosted on GitHub
 * (see [com.streamhub.tv.data.remote.ChannelApiService]). Adding, removing, renaming,
 * enabling/disabling, or reorganizing channels only requires editing that JSON file -
 * no app update is required.
 */
@Serializable
data class Channel(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("logo") val logo: String = "",
    @SerialName("category") val category: String,
    @SerialName("streamUrl") val streamUrl: String,
    @SerialName("description") val description: String = "",
    @SerialName("country") val country: String = "",
    @SerialName("language") val language: String = "",
    @SerialName("enabled") val enabled: Boolean = true
) {
    /** Best-effort guess of the stream type, used to configure ExoPlayer's MediaSource. */
    val streamType: StreamType
        get() = when {
            streamUrl.contains(".m3u8", ignoreCase = true) -> StreamType.HLS
            streamUrl.contains(".mpd", ignoreCase = true) -> StreamType.DASH
            streamUrl.contains(".mp4", ignoreCase = true) -> StreamType.MP4
            else -> StreamType.OTHER
        }
}

enum class StreamType { HLS, DASH, MP4, OTHER }

/**
 * Root object of the remote channels.json payload.
 *
 * Example JSON hosted on GitHub:
 * ```json
 * {
 *   "updatedAt": "2026-07-29T00:00:00Z",
 *   "channels": [
 *     {
 *       "id": "sports_1",
 *       "name": "Sports 1",
 *       "logo": "https://example.com/logos/sports1.png",
 *       "category": "Sports",
 *       "streamUrl": "https://example.com/live/sports1/index.m3u8",
 *       "description": "24/7 live sports coverage",
 *       "country": "Global",
 *       "language": "Multi",
 *       "enabled": true
 *     }
 *   ]
 * }
 * ```
 */
@Serializable
data class ChannelsResponse(
    @SerialName("updatedAt") val updatedAt: String = "",
    @SerialName("channels") val channels: List<Channel> = emptyList()
)
