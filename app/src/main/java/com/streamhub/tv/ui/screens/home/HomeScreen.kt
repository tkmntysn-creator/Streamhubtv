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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LiveTv
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.streamhub.tv.data.model.Channel
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 5.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Text(
                    text = "Explore",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Live channels, picked for you",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f))
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.32f), RoundedCornerShape(50))
                    .padding(horizontal = 11.dp, vertical = 7.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(7.dp).clip(CircleShape).background(Color(0xFFFF5964)))
                    Text(" LIVE", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium)
                }
            }
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
            CategoryChipsRow(categories = state.categoryNames, onCategoryClick = onCategoryClick)
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
        state.categoryNames.forEach { category ->
            val channels = state.categorized[category].orEmpty()
            if (channels.isNotEmpty()) {
                item(key = "category-$category", contentType = "channel-row") {
                    CategoryRow(
                        title = category,
                        channels = channels,
                        favoriteIds = state.favoriteIds,
                        onChannelClick = onChannelClick,
                        onToggleFavorite = onToggleFavorite,
                        onSeeAll = { onCategoryClick(category) }
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
private fun CategoryChipsRow(categories: List<String>, onCategoryClick: (String) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        items(
            items = categories,
            key = { "chip-$it" },
            contentType = { "category-chip" }
        ) { category ->
            Text(
                text = category,
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
                    .clickable { onCategoryClick(category) }
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
    val shape = RoundedCornerShape(22.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(224.dp)
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
                model = ImageRequest.Builder(LocalContext.current)
                    .data(channel.logo)
                    .size(600, 520)
                    .memoryCacheKey("spotlight-${channel.logo}")
                    .crossfade(false)
                    .build(),
                contentDescription = channel.name,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .width(142.dp)
                    .height(224.dp),
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
                .padding(16.dp)
                .width(220.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFFE5484D))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
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
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 6.dp)
            )
            Text(
                text = listOfNotNull(channel.country.ifBlank { null }, channel.language.ifBlank { null })
                    .joinToString("  •  "),
                color = Color.White.copy(alpha = 0.70f),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 3.dp, bottom = 9.dp),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                Button(
                    onClick = onWatch,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF10131A)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 7.dp)
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(19.dp))
                    Text("Watch", modifier = Modifier.padding(start = 5.dp), fontWeight = FontWeight.SemiBold)
                }
                OutlinedButton(
                    onClick = onToggleFavorite,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.42f)),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 11.dp, vertical = 7.dp)
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
