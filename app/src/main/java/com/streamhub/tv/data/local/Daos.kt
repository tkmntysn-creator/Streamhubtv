package com.streamhub.tv.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites ORDER BY addedAt DESC")
    fun observeFavorites(): Flow<List<FavoriteEntity>>

    @Query("SELECT channelId FROM favorites")
    fun observeFavoriteIds(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE channelId = :channelId")
    suspend fun removeFavorite(channelId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE channelId = :channelId)")
    suspend fun isFavorite(channelId: String): Boolean
}

@Dao
interface WatchHistoryDao {
    @Query("SELECT * FROM watch_history ORDER BY lastWatchedAt DESC LIMIT :limit")
    fun observeRecent(limit: Int = 20): Flow<List<WatchHistoryEntity>>

    @Query("SELECT * FROM watch_history WHERE channelId = :channelId LIMIT 1")
    suspend fun get(channelId: String): WatchHistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: WatchHistoryEntity)

    @Query("DELETE FROM watch_history")
    suspend fun clearAll()
}

@Dao
interface ChannelCacheDao {
    @Query("SELECT * FROM channel_cache WHERE id = 1 LIMIT 1")
    suspend fun getCache(): ChannelCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveCache(entity: ChannelCacheEntity)

    @Query("DELETE FROM channel_cache")
    suspend fun clearCache()

    @Delete
    suspend fun delete(entity: ChannelCacheEntity)
}
