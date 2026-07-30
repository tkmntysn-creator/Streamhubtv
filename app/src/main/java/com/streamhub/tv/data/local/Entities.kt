package com.streamhub.tv.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/** A user's favorited channel, keyed by the channel's remote id. */
@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val channelId: String,
    val name: String,
    val logo: String,
    val category: String,
    val streamUrl: String,
    val addedAt: Long = System.currentTimeMillis()
)

/** Tracks channels the user recently watched, for the "Recently Watched" / "Continue Watching" rows. */
@Entity(tableName = "watch_history")
data class WatchHistoryEntity(
    @PrimaryKey val channelId: String,
    val name: String,
    val logo: String,
    val category: String,
    val streamUrl: String,
    val lastWatchedAt: Long = System.currentTimeMillis(),
    val watchCount: Int = 1
)

/**
 * A single-row local cache of the last successfully downloaded channels.json payload,
 * used so the app can display channels while offline or when the remote host is
 * temporarily unreachable.
 */
@Entity(tableName = "channel_cache")
data class ChannelCacheEntity(
    @PrimaryKey val id: Int = 1,
    val json: String,
    val cachedAt: Long = System.currentTimeMillis(),
    val sourceUrl: String
)
