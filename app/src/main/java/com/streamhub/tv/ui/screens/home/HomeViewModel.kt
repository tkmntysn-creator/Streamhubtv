package com.streamhub.tv.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streamhub.tv.data.model.Channel
import com.streamhub.tv.data.model.ChannelCategory
import com.streamhub.tv.data.repository.ChannelRepository
import com.streamhub.tv.data.repository.FavoritesRepository
import com.streamhub.tv.data.repository.WatchHistoryRepository
import com.streamhub.tv.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val featured: List<Channel> = emptyList(),
    val recentlyWatched: List<Channel> = emptyList(),
    val favorites: List<Channel> = emptyList(),
    val recommended: List<Channel> = emptyList(),
    val categorized: Map<ChannelCategory, List<Channel>> = emptyMap(),
    val favoriteIds: Set<String> = emptySet()
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val channelRepository: ChannelRepository,
    private val favoritesRepository: FavoritesRepository,
    private val watchHistoryRepository: WatchHistoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    init {
        loadChannels()
        observeFavoriteIds()
    }

    fun loadChannels(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            when (val result = channelRepository.fetchChannels(forceRefresh)) {
                is Resource.Success -> applyChannels(result.data)
                is Resource.Error -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = result.message
                )
                Resource.Loading -> Unit
            }
        }
    }

    private fun applyChannels(channels: List<Channel>) {
        val categorized = channels.groupBy { ChannelCategory.from(it.category) }
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            errorMessage = null,
            featured = channels.shuffled().take(8),
            recommended = channels.shuffled().take(10),
            categorized = categorized
        )
        observeRecentlyWatched(channels)
    }

    private fun observeRecentlyWatched(allChannels: List<Channel>) {
        viewModelScope.launch {
            watchHistoryRepository.observeRecentlyWatched().collect { history ->
                val ids = history.map { it.channelId }.toSet()
                val recent = allChannels.filter { it.id in ids }
                _uiState.value = _uiState.value.copy(recentlyWatched = recent)
            }
        }
    }

    private fun observeFavoriteIds() {
        viewModelScope.launch {
            favoritesRepository.observeFavoriteIds().collect { ids ->
                _uiState.value = _uiState.value.copy(favoriteIds = ids)
            }
        }
        viewModelScope.launch {
            favoritesRepository.observeFavorites().collect { favs ->
                val favChannels = favs.map {
                    Channel(
                        id = it.channelId,
                        name = it.name,
                        logo = it.logo,
                        category = it.category,
                        streamUrl = it.streamUrl
                    )
                }
                _uiState.value = _uiState.value.copy(favorites = favChannels)
            }
        }
    }

    fun toggleFavorite(channel: Channel) {
        viewModelScope.launch { favoritesRepository.toggleFavorite(channel) }
    }
}
