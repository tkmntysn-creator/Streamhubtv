package com.streamhub.tv.data.repository

import com.streamhub.tv.data.local.WatchHistoryDao
import com.streamhub.tv.data.local.WatchHistoryEntity
import com.streamhub.tv.data.model.Channel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WatchHistoryRepository @Inject constructor(
    private val dao: WatchHistoryDao
) {
    fun observeRecentlyWatched(limit: Int = 20): Flow<List<WatchHistoryEntity>> =
        dao.observeRecent(limit)

    suspend fun recordWatch(channel: Channel) {
        val existing = dao.get(channel.id)
        dao.upsert(
            WatchHistoryEntity(
                channelId = channel.id,
                name = channel.name,
                logo = channel.logo,
                category = channel.category,
                streamUrl = channel.streamUrl,
                watchCount = (existing?.watchCount ?: 0) + 1
            )
        )
    }

    suspend fun clear() = dao.clearAll()
}
