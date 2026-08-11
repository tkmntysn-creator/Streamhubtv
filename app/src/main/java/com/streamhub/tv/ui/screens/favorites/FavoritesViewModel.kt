package com.streamhub.tv.ui.screens.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streamhub.tv.data.model.Channel
import com.streamhub.tv.data.repository.FavoritesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val favoritesRepository: FavoritesRepository
) : ViewModel() {

    private val _favorites = MutableStateFlow<List<Channel>>(emptyList())
    val favorites: StateFlow<List<Channel>> = _favorites

    init {
        viewModelScope.launch {
            favoritesRepository.observeFavorites().collect { entities ->
                _favorites.value = entities.map {
                    Channel(
                        id = it.channelId,
                        name = it.name,
                        logo = it.logo,
                        category = it.category,
                        streamUrl = it.streamUrl
                    )
                }
            }
        }
    }

    fun removeFavorite(channel: Channel) {
        viewModelScope.launch { favoritesRepository.toggleFavorite(channel) }
    }
}
