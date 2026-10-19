package com.streamhub.tv.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.streamhub.tv.data.model.Channel
import com.streamhub.tv.data.model.ChannelCategory
import com.streamhub.tv.ui.components.CategoryRow
import com.streamhub.tv.ui.components.FullScreenError
import com.streamhub.tv.ui.components.FullScreenLoading

/** Midnight Broadcast home: compact spotlight, clear live hierarchy, quiet content rows. */
@Composable
fun HomeScreen(
    onChannelClick: (String) -> Unit,
    onCategoryClick: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
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

@Composable
private fun HomeTopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.LiveTv,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column(modifier = Modifier.padding(start = 11.dp)) {
                Text(
                    text = "STREAMHUB",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.4.sp
                )
                Text(
                    text = "LIVE TELEVISION",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.2.sp
                )
            }
        }
        IconButton(onClick = { /* Reserved for live alerts. */ }) {
            Icon(
                imageVector = Icons.Filled.NotificationsNone,
                contentDescription = "Notifications",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
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
    val heroChannel = state.featured.firstOrNull()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 118.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item(key = "category-chips", contentType = "category-chips") {
            CategoryChipsRow(onCategoryClick = onCategoryClick)
        }
        if (heroChannel != null) {
            item(key = "spotlight", contentType = "spotlight") {
                SpotlightHero(
                    channel = heroChannel,
                    isFavorite = state.favoriteIds.contains(heroChannel.id),
                    onWatch = { onChannelClick(heroChannel) },
                    onToggleFavorite = { onToggleFavorite(heroChannel) }
                )
            }
        }
        if (state.recentlyWatched.isNotEmpty()) {
            item(key = "continue-watching", contentType = "channel-row") {
                CategoryRow(
                    title = "Continue Watching",
                    channels = state.recentlyWatched,
                    favoriteIds = state.favoriteIds,
                    onChannelClick = onChannelClick,
                    onToggleFavorite = onToggleFavorite,
                    showPlayOverlay = true
                )
            }
        }
        if (state.favorites.isNotEmpty()) {
            item(key = "my-list", contentType = "channel-row") {
                CategoryRow(
                    title = "My List",
                    channels = state.favorites,
                    favoriteIds = state.favoriteIds,
                    onChannelClick = onChannelClick,
                    onToggleFavorite = onToggleFavorite
                )
            }
        }
        ChannelCategory.orderedCategories.forEach { category ->
            val channels = state.categorized[category].orEmpty()
            if (channels.isNotEmpty()) {
                item(key = "category-${category.name}", contentType = "channel-row") {
                    CategoryRow(
                        title = category.displayName,
                        channels = channels,
                        favoriteIds = state.favoriteIds,
                        onChannelClick = onChannelClick,
                        onToggleFavorite = onToggleFavorite,
                        onSeeAll = { onCategoryClick(category.displayName) }
                    )
                }
            }
        }
        if (state.recommended.isNotEmpty()) {
            item(key = "recommended", contentType = "channel-row") {
                CategoryRow(
                    title = "Recommended For You",
                    channels = state.recommended,
                    favoriteIds = state.favoriteIds,
                    onChannelClick = onChannelClick,
                    onToggleFavorite = onToggleFavorite
                )
            }
        }
        if (!state.isLoading && state.categorized.isEmpty()) {
            item(key = "empty-catalog", contentType = "empty-state") {
                EmptyCatalogState()
            }
        }
    }
}

@Composable
private fun CategoryChipsRow(onCategoryClick: (String) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        items(
            items = ChannelCategory.orderedCategories,
            key = { it.name },
            contentType = { "category-chip" }
        ) { category ->
            Text(
                text = category.displayName,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f))
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                        RoundedCornerShape(50)
                    )
                    .clickable { onCategoryClick(category.displayName) }
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            )
        }
    }
}

@Composable
private fun SpotlightHero(
    channel: Channel,
    isFavorite: Boolean,
    onWatch: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    val shape = RoundedCornerShape(28.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .height(292.dp)
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(
                        MaterialTheme.colorScheme.surfaceVariant,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.34f),
                        Color(0xFF090B12)
                    )
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.11f), shape)
    ) {
        if (channel.logo.isNotBlank()) {
            AsyncImage(
                model = channel.logo,
                contentDescription = channel.name,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .width(190.dp)
                    .height(292.dp),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF11141D), Color.Transparent, Color(0x66080A0F))
                        )
                    )
            )
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(20.dp)
                .width(250.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFFE5484D))
                        .padding(horizontal = 9.dp, vertical = 5.dp)
                ) {
                    Text("LIVE", color = Color.White, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }
                Text(
                    text = channel.category.uppercase(),
                    color = Color.White.copy(alpha = 0.72f),
                    style = MaterialTheme.typography.labelMedium,
                    letterSpacing = 1.sp
                )
            }
            Text(
                text = channel.name,
                color = Color.White,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 10.dp)
            )
            Text(
                text = listOfNotNull(channel.country.ifBlank { null }, channel.language.ifBlank { null })
                    .joinToString("  •  "),
                color = Color.White.copy(alpha = 0.70f),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 5.dp, bottom = 14.dp)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                Button(
                    onClick = onWatch,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF10131A)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 15.dp, vertical = 9.dp)
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(19.dp))
                    Text("Watch", modifier = Modifier.padding(start = 5.dp), fontWeight = FontWeight.SemiBold)
                }
                OutlinedButton(
                    onClick = onToggleFavorite,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.42f)),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 13.dp, vertical = 9.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Check else Icons.Filled.Add,
                        contentDescription = null,
                        modifier = Modifier.size(19.dp)
                    )
                    Text(if (isFavorite) "Saved" else "Save", modifier = Modifier.padding(start = 5.dp))
                }
            }
        }
    }
}

@Composable
private fun EmptyCatalogState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Filled.LiveTv,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(42.dp)
        )
        Text(
            text = "Your live catalog is empty",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(top = 14.dp)
        )
        Text(
            text = "Add channels to channels.json, then refresh.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}
