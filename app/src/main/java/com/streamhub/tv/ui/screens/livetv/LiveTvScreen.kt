package com.streamhub.tv.ui.screens.livetv

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.streamhub.tv.R
import com.streamhub.tv.data.model.Channel
import com.streamhub.tv.ui.components.ChannelCard
import com.streamhub.tv.ui.components.FullScreenError
import com.streamhub.tv.ui.components.FullScreenLoading

@Composable
fun LiveTvScreen(
    onChannelClick: (String) -> Unit,
    viewModel: LiveTvViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val channels = visibleChannels(state)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp)
    ) {
        Text(
            text = stringResource(R.string.channels_title),
            style = MaterialTheme.typography.displaySmall,
            modifier = Modifier.padding(top = 20.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.live_destinations, state.channels.size),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            SortMenu(selected = state.sort, onSelected = viewModel::setSort)
        }

        CategoryFilterRow(state = state, onSelect = viewModel::selectCategory)

        when {
            state.isLoading && state.categorized.isEmpty() -> FullScreenLoading()
            state.errorMessage != null && state.categorized.isEmpty() -> FullScreenError(
                message = state.errorMessage ?: "Unknown error",
                onRetry = { viewModel.load(forceRefresh = true) }
            )
            channels.isEmpty() -> EmptyChannels()
            else -> LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 148.dp),
                contentPadding = PaddingValues(top = 20.dp, bottom = 118.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(
                    items = channels,
                    key = { it.id },
                    contentType = { "channel-grid-card" }
                ) { channel ->
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

@Composable
private fun CategoryFilterRow(
    state: LiveTvUiState,
    onSelect: (String?) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        item(key = "all") { FilterPill(stringResource(R.string.all), state.selectedCategory == null) { onSelect(null) } }
        items(
            items = state.categoryNames,
            key = { "category-$it" },
            contentType = { "category-pill" }
        ) { category ->
            FilterPill(
                label = category,
                selected = state.selectedCategory == category,
                onClick = { onSelect(category) }
            )
        }
    }
}

@Composable
private fun FilterPill(label: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelLarge,
        color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f))
            .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(horizontal = 15.dp, vertical = 10.dp)
    )
}

@Composable
private fun SortMenu(selected: ChannelSort, onSelected: (ChannelSort) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f))
                .clickable { expanded = true }
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Tune, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(17.dp))
            Text(selected.label, style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(start = 6.dp))
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            ChannelSort.entries.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.label) },
                    onClick = { onSelected(option); expanded = false },
                    trailingIcon = if (option == selected) {
                        { Text("✓", color = MaterialTheme.colorScheme.primary) }
                    } else null
                )
            }
        }
    }
}

private fun visibleChannels(state: LiveTvUiState): List<Channel> {
    val base = if (state.selectedCategory == null) {
        state.categoryNames.flatMap { state.categorized[it].orEmpty() }
    } else {
        state.categorized[state.selectedCategory].orEmpty()
    }
    return when (state.sort) {
        ChannelSort.SMART -> base
        ChannelSort.ALPHABETICAL -> base.sortedBy { it.name.lowercase() }
        ChannelSort.PROVIDER -> base.sortedWith(compareBy({ it.provider.ifBlank { "Other" }.lowercase() }, { it.name.lowercase() }))
        ChannelSort.COUNTRY -> base.sortedWith(compareBy({ it.country.ifBlank { "Global" }.lowercase() }, { it.name.lowercase() }))
    }
}

@Composable
private fun EmptyChannels() {
    Column(
        modifier = Modifier.fillMaxSize().padding(top = 70.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Filled.Tune, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp))
        Text(stringResource(R.string.no_channels), style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 14.dp))
        Text(stringResource(R.string.add_streams), color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 6.dp))
    }
}
