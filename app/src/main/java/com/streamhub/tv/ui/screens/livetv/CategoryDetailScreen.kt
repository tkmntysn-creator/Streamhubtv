package com.streamhub.tv.ui.screens.livetv

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.streamhub.tv.data.model.ChannelCategory
import com.streamhub.tv.ui.components.ChannelCard
import com.streamhub.tv.ui.components.FullScreenLoading

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDetailScreen(
    categoryName: String,
    onChannelClick: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: LiveTvViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val category = ChannelCategory.from(categoryName)

    LaunchedEffect(Unit) {
        if (state.categorized.isEmpty()) viewModel.load()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(category.displayName, style = MaterialTheme.typography.headlineMedium) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        )

        if (state.isLoading && state.categorized.isEmpty()) {
            FullScreenLoading()
        } else {
            val channels = state.categorized[category].orEmpty()
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
