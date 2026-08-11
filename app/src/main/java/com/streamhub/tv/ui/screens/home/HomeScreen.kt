package com.streamhub.tv.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.streamhub.tv.data.model.Channel
import com.streamhub.tv.data.model.ChannelCategory
import com.streamhub.tv.ui.components.CategoryRow
import com.streamhub.tv.ui.components.FullScreenError
import com.streamhub.tv.ui.components.FullScreenLoading

/**
 * Netflix-inspired home screen:
 *  - Top bar: wordmark logo + notifications bell only (no download icon)
 *  - Horizontal category chips (Sports, News, Movies, ...)
 *  - Hero banner for a featured channel with "Watch Live" + "My List" actions
 *  - "Continue Watching" row (with a centered play icon per thumbnail)
 *  - "My List" row (favorites)
 *  - One row per category
 */
@Composable
fun HomeScreen(
    onChannelClick: (String) -> Unit,
    onCategoryClick: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        HomeTopBar()

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

/** Minimal top bar: wordmark on the left, a single notifications bell on the right.
 *  Deliberately has NO download icon, per the current design direction. */
@Composable
private fun HomeTopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Filled.LiveTv,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(26.dp)
            )
            Text(
                "StreamHub",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        IconButton(onClick = { /* No notifications yet - reserved for future use */ }) {
            Icon(Icons.Filled.Notifications, contentDescription = "Notifications")
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
    val heroChannel = state.featured.firstOrNull()

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            CategoryChipsRow(onCategoryClick = onCategoryClick)
        }

        heroChannel?.let { hero ->
            item {
                HeroBanner(
                    channel = hero,
                    isFavorite = state.favoriteIds.contains(hero.id),
                    onWatch = { onChannelClick(hero) },
                    onToggleFavorite = { onToggleFavorite(hero) }
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
                    showPlayOverlay = true,
                    modifier = Modifier.padding(top = 20.dp)
                )
            }
        }

        if (state.favorites.isNotEmpty()) {
            item {
                CategoryRow(
                    title = "My List",
                    channels = state.favorites,
                    favoriteIds = state.favoriteIds,
                    onChannelClick = onChannelClick,
                    onToggleFavorite = onToggleFavorite,
                    modifier = Modifier.padding(top = 20.dp)
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
                    modifier = Modifier.padding(top = 20.dp)
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
                    modifier = Modifier.padding(top = 20.dp, bottom = 32.dp)
                )
            }
        }
    }
}

/** Horizontally scrolling category filter chips, right under the top bar. */
@Composable
private fun CategoryChipsRow(onCategoryClick: (String) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(ChannelCategory.orderedCategories) { category ->
            FilterChip(
                selected = false,
                onClick = { onCategoryClick(category.displayName) },
                label = { Text(category.displayName) }
            )
        }
    }
}

/** Big featured banner at the top of Home, styled after Netflix's hero card:
 *  full-width backdrop image, title, category/country tags, and two actions. */
@Composable
private fun HeroBanner(
    channel: Channel,
    isFavorite: Boolean,
    onWatch: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(420.dp)
    ) {
        AsyncImage(
            model = channel.logo,
            contentDescription = channel.name,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.4f),
                            Color.Black.copy(alpha = 0.95f)
                        )
                    )
                )
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Text(
                channel.name,
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White
            )
            Text(
                listOfNotNull(channel.category.ifBlank { null }, channel.country.ifBlank { null })
                    .joinToString("  •  "),
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onWatch,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null)
                    Text("Watch Live", modifier = Modifier.padding(start = 4.dp))
                }
                OutlinedButton(
                    onClick = onToggleFavorite,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Check else Icons.Filled.Add,
                        contentDescription = null
                    )
                    Text(
                        if (isFavorite) "In My List" else "My List",
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
        }
    }
}
