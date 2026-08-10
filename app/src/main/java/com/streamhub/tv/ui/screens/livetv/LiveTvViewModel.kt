package com.streamhub.tv.ui.screens.livetv

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streamhub.tv.data.model.Channel
import com.streamhub.tv.data.model.ChannelCategory
import com.streamhub.tv.data.repository.ChannelRepository
import com.streamhub.tv.data.repository.FavoritesRepository
import com.streamhub.tv.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LiveTvUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val categorized: Map<ChannelCategory, List<Channel>> = emptyMap(),
    val selectedCategory: ChannelCategory? = null,
    val favoriteIds: Set<String> = emptySet()
)

@HiltViewModel
class LiveTvViewModel @Inject constructor(
    private val channelRepository: ChannelRepository,
    private val favoritesRepository: FavoritesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LiveTvUiState())
    val uiState: StateFlow<LiveTvUiState> = _uiState

    init {
        load()
        viewModelScope.launch {
            favoritesRepository.observeFavoriteIds().collect { ids ->
                _uiState.value = _uiState.value.copy(favoriteIds = ids)
            }
        }
    }

    fun load(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            when (val result = channelRepository.fetchChannels(forceRefresh)) {
                is Resource.Success -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    categorized = result.data.groupBy { ChannelCategory.from(it.category) }
                )
                is Resource.Error -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = result.message
                )
                Resource.Loading -> Unit
            }
        }
    }

    fun selectCategory(category: ChannelCategory?) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
    }

    fun toggleFavorite(channel: Channel) {
        viewModelScope.launch { favoritesRepository.toggleFavorite(channel) }
    }
}
