package com.streamhub.tv.data.repository

import com.streamhub.tv.data.local.FavoriteDao
import com.streamhub.tv.data.local.FavoriteEntity
import com.streamhub.tv.data.model.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoritesRepository @Inject constructor(
    private val favoriteDao: FavoriteDao
) {
    fun observeFavorites(): Flow<List<FavoriteEntity>> = favoriteDao.observeFavorites()

    fun observeFavoriteIds(): Flow<Set<String>> =
        favoriteDao.observeFavoriteIds().map { it.toSet() }

    suspend fun toggleFavorite(channel: Channel) {
        if (favoriteDao.isFavorite(channel.id)) {
            favoriteDao.removeFavorite(channel.id)
        } else {
            favoriteDao.addFavorite(
                FavoriteEntity(
                    channelId = channel.id,
                    name = channel.name,
                    logo = channel.logo,
                    category = channel.category,
                    streamUrl = channel.streamUrl
                )
            )
        }
    }

    suspend fun isFavorite(channelId: String): Boolean = favoriteDao.isFavorite(channelId)
}
