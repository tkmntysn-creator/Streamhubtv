package com.streamhub.tv.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [FavoriteEntity::class, WatchHistoryEntity::class, ChannelCacheEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao
    abstract fun watchHistoryDao(): WatchHistoryDao
    abstract fun channelCacheDao(): ChannelCacheDao

    companion object {
        const val DATABASE_NAME = "streamhub_tv.db"
    }
}
