package com.streamhub.tv.ui.screens.livetv

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.streamhub.tv.data.model.Channel
import com.streamhub.tv.data.model.ChannelCategory
import com.streamhub.tv.ui.components.ChannelCard
import com.streamhub.tv.ui.components.FullScreenError
import com.streamhub.tv.ui.components.FullScreenLoading

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveTvScreen(
    onChannelClick: (String) -> Unit,
    viewModel: LiveTvViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    androidx.compose.foundation.layout.Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("Live TV", style = MaterialTheme.typography.headlineMedium) })

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = state.selectedCategory == null,
                    onClick = { viewModel.selectCategory(null) },
                    label = { Text("All") }
                )
            }
            items(ChannelCategory.orderedCategories) { category ->
                if (state.categorized[category]?.isNotEmpty() == true) {
                    FilterChip(
                        selected = state.selectedCategory == category,
                        onClick = { viewModel.selectCategory(category) },
                        label = { Text(category.displayName) }
                    )
                }
            }
        }

        when {
            state.isLoading && state.categorized.isEmpty() -> FullScreenLoading()
            state.errorMessage != null && state.categorized.isEmpty() -> FullScreenError(
                message = state.errorMessage ?: "Unknown error",
                onRetry = { viewModel.load(forceRefresh = true) }
            )
            else -> {
                val channels: List<Channel> = if (state.selectedCategory != null) {
                    state.categorized[state.selectedCategory].orEmpty()
                } else {
                    ChannelCategory.orderedCategories.flatMap { state.categorized[it].orEmpty() }
                }
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 150.dp),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(channels, key = { it.id }) { channel ->
                        ChannelCard(
                            channel = channel,
                            isFavorite = state.favoriteIds.contains(channel.id),
                            onClick = { onChannelClick(channel.id) },
                            onToggleFavorite = { viewModel.toggleFavorite(channel) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
