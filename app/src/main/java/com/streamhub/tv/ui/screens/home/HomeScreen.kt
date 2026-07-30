package com.streamhub.tv.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.streamhub.tv.data.model.Channel
import com.streamhub.tv.data.model.ChannelCategory
import com.streamhub.tv.ui.components.CategoryRow
import com.streamhub.tv.ui.components.FullScreenError
import com.streamhub.tv.ui.components.FullScreenLoading
import com.streamhub.tv.ui.theme.GradientHero

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onChannelClick: (String) -> Unit,
    onCategoryClick: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Text(
                    "StreamHub TV",
                    style = MaterialTheme.typography.headlineMedium
                )
            },
            actions = {
                IconButton(onClick = { viewModel.loadChannels(forceRefresh = true) }) {
                    Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
                }
            }
        )

        when {
            state.isLoading && state.categorized.isEmpty() -> FullScreenLoading()
            state.errorMessage != null && state.categorized.isEmpty() -> FullScreenError(
                message = state.errorMessage ?: "Unknown error",
                onRetry = { viewModel.loadChannels(forceRefresh = true) }
            )
            else -> HomeContent(
                state = state,
                onChannelClick = { onChannelClick(it.id) },
                onToggleFavorite = viewModel::toggleFavorite,
                onCategoryClick = onCategoryClick
            )
        }
    }
}

@Composable
private fun HomeContent(
    state: HomeUiState,
    onChannelClick: (Channel) -> Unit,
    onToggleFavorite: (Channel) -> Unit,
    onCategoryClick: (String) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            // Hero banner featuring a rotating highlight of top channels
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(Brush.verticalGradient(GradientHero)),
                contentAlignment = Alignment.BottomStart
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "Watch Live. Anywhere.",
                        style = MaterialTheme.typography.headlineLarge
                    )
                    Text(
                        "Sports, News, Movies, Series & more - all in one place",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }

        if (state.featured.isNotEmpty()) {
            item {
                CategoryRow(
                    title = "Featured",
                    channels = state.featured,
                    favoriteIds = state.favoriteIds,
                    onChannelClick = onChannelClick,
                    onToggleFavorite = onToggleFavorite,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }

        if (state.recentlyWatched.isNotEmpty()) {
            item {
                CategoryRow(
                    title = "Continue Watching",
                    channels = state.recentlyWatched,
                    favoriteIds = state.favoriteIds,
                    onChannelClick = onChannelClick,
                    onToggleFavorite = onToggleFavorite,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }

        if (state.favorites.isNotEmpty()) {
            item {
                CategoryRow(
                    title = "Your Favorites",
                    channels = state.favorites,
                    favoriteIds = state.favoriteIds,
                    onChannelClick = onChannelClick,
                    onToggleFavorite = onToggleFavorite,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }

        items(ChannelCategory.orderedCategories) { category ->
            val channels = state.categorized[category].orEmpty()
            if (channels.isNotEmpty()) {
                CategoryRow(
                    title = category.displayName,
                    channels = channels,
                    favoriteIds = state.favoriteIds,
                    onChannelClick = onChannelClick,
                    onToggleFavorite = onToggleFavorite,
                    onSeeAll = { onCategoryClick(category.displayName) },
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }

        if (state.recommended.isNotEmpty()) {
            item {
                CategoryRow(
                    title = "Recommended For You",
                    channels = state.recommended,
                    favoriteIds = state.favoriteIds,
                    onChannelClick = onChannelClick,
                    onToggleFavorite = onToggleFavorite,
                    modifier = Modifier.padding(top = 16.dp, bottom = 32.dp)
                )
            }
        }
    }
}
