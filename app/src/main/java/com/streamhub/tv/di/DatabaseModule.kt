package com.streamhub.tv.di

import android.content.Context
import androidx.room.Room
import com.streamhub.tv.data.local.AppDatabase
import com.streamhub.tv.data.local.ChannelCacheDao
import com.streamhub.tv.data.local.FavoriteDao
import com.streamhub.tv.data.local.PreferencesManager
import com.streamhub.tv.data.local.WatchHistoryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.DATABASE_NAME)
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideFavoriteDao(db: AppDatabase): FavoriteDao = db.favoriteDao()

    @Provides
    fun provideWatchHistoryDao(db: AppDatabase): WatchHistoryDao = db.watchHistoryDao()

    @Provides
    fun provideChannelCacheDao(db: AppDatabase): ChannelCacheDao = db.channelCacheDao()

    @Provides
    @Singleton
    fun providePreferencesManager(@ApplicationContext context: Context): PreferencesManager =
        PreferencesManager(context)
}
