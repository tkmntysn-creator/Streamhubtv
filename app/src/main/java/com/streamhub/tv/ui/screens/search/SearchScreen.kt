package com.streamhub.tv.ui.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.streamhub.tv.ui.components.ChannelCard
import com.streamhub.tv.R

@Composable
fun SearchScreen(
    onChannelClick: (String) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp)
    ) {
        Text(
            text = stringResource(R.string.search_title),
            style = MaterialTheme.typography.displaySmall,
            modifier = Modifier.padding(top = 20.dp, bottom = 18.dp)
        )
        OutlinedTextField(
            value = state.query,
            onValueChange = viewModel::onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(stringResource(R.string.search_placeholder)) },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            trailingIcon = { Icon(Icons.Filled.Tune, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            singleLine = true,
            shape = RoundedCornerShape(19.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f),
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color.Transparent
            )
        )

        when {
            state.query.isBlank() -> SearchEmptyState(
                title = stringResource(R.string.search_empty_title),
                subtitle = stringResource(R.string.search_empty_subtitle)
            )
            state.results.isEmpty() -> SearchEmptyState(
                title = stringResource(R.string.search_no_results),
                subtitle = stringResource(R.string.search_no_results_subtitle)
            )
            else -> LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 148.dp),
                contentPadding = PaddingValues(top = 20.dp, bottom = 118.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(
                    items = state.results,
                    key = { it.id },
                    contentType = { "search-channel" }
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
private fun SearchEmptyState(title: String, subtitle: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 14.dp)
            )
            Text(title, style = MaterialTheme.typography.titleLarge)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
}
