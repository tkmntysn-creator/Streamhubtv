package com.streamhub.tv.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streamhub.tv.data.model.Channel
import com.streamhub.tv.data.repository.ChannelRepository
import com.streamhub.tv.data.repository.FavoritesRepository
import com.streamhub.tv.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val allChannels: List<Channel> = emptyList(),
    val results: List<Channel> = emptyList(),
    val favoriteIds: Set<String> = emptySet(),
    val isLoading: Boolean = true
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val channelRepository: ChannelRepository,
    private val favoritesRepository: FavoritesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState

    init {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            when (val result = channelRepository.fetchChannels()) {
                is Resource.Success -> _uiState.value =
                    _uiState.value.copy(allChannels = result.data, isLoading = false)
                is Resource.Error -> _uiState.value = _uiState.value.copy(isLoading = false)
                Resource.Loading -> Unit
            }
        }
        viewModelScope.launch {
            favoritesRepository.observeFavoriteIds().collect { ids ->
                _uiState.value = _uiState.value.copy(favoriteIds = ids)
            }
        }
    }

    /** Instant search across channel name, category, country, and language. */
    fun onQueryChange(query: String) {
        val results = if (query.isBlank()) {
            emptyList()
        } else {
            val q = query.trim().lowercase()
            _uiState.value.allChannels.filter { channel ->
                channel.name.lowercase().contains(q) ||
                    channel.category.lowercase().contains(q) ||
                    channel.country.lowercase().contains(q) ||
                    channel.language.lowercase().contains(q)
            }
        }
        _uiState.value = _uiState.value.copy(query = query, results = results)
    }

    fun toggleFavorite(channel: Channel) {
        viewModelScope.launch { favoritesRepository.toggleFavorite(channel) }
    }
}
