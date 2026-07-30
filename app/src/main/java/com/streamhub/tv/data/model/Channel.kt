package com.streamhub.tv.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * An alternate stream source ("server") for a channel. Some public/free IPTV feeds go
 * down often, so a channel can optionally list several mirrors under "sources" in
 * channels.json; the player lets the user switch between them from the Settings (⚙)
 * button without leaving the screen.
 */
@Serializable
data class StreamSource(
    @SerialName("label") val label: String,
    @SerialName("url") val url: String
)

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
    @SerialName("enabled") val enabled: Boolean = true,
    // Optional list of alternate mirrors/servers for this same channel. When absent,
    // the player falls back to a single source built from `streamUrl` labeled "Server 1".
    @SerialName("sources") val sources: List<StreamSource> = emptyList()
) {
    /** All playable sources for this channel: the declared `sources` list if present,
     *  otherwise a single-item list built from the legacy `streamUrl` field. */
    val allSources: List<StreamSource>
        get() = sources.ifEmpty { listOf(StreamSource(label = "Server 1", url = streamUrl)) }

    /** Best-effort guess of a URL's stream type, used to configure ExoPlayer's MediaSource. */
    fun streamTypeOf(url: String): StreamType = when {
        url.contains(".m3u8", ignoreCase = true) -> StreamType.HLS
        url.contains(".mpd", ignoreCase = true) -> StreamType.DASH
        url.contains(".mp4", ignoreCase = true) -> StreamType.MP4
        else -> StreamType.OTHER
    }

    /** Best-effort guess of the primary stream's type (kept for backward compatibility). */
    val streamType: StreamType get() = streamTypeOf(streamUrl)
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
 *       "enabled": true,
 *       "sources": [
 *         { "label": "Server 1", "url": "https://example.com/live/sports1/index.m3u8" },
 *         { "label": "Server 2 (backup)", "url": "https://mirror.example.com/sports1/index.m3u8" }
 *       ]
 *     }
 *   ]
 * }
 * ```
 * The "sources" array is optional - if you omit it, the app just uses "streamUrl" as
 * the single "Server 1" source.
 */
@Serializable
data class ChannelsResponse(
    @SerialName("updatedAt") val updatedAt: String = "",
    @SerialName("channels") val channels: List<Channel> = emptyList()
)
