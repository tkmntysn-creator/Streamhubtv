package com.streamhub.tv.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.streamhub.tv.data.model.Channel
import com.streamhub.tv.ui.theme.GradientHero

/** A titled horizontally-scrolling row of channel cards, e.g. for a category or "Favorites". */
@Composable
fun CategoryRow(
    title: String,
    channels: List<Channel>,
    favoriteIds: Set<String>,
    onChannelClick: (Channel) -> Unit,
    onToggleFavorite: (Channel) -> Unit,
    onSeeAll: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    if (channels.isEmpty()) return
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            if (onSeeAll != null) {
                TextButton(onClick = onSeeAll) { Text("See all") }
            }
        }
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(channels) { channel ->
                ChannelCard(
                    channel = channel,
                    isFavorite = favoriteIds.contains(channel.id),
                    onClick = { onChannelClick(channel) },
                    onToggleFavorite = { onToggleFavorite(channel) }
                )
            }
        }
    }
}

@Composable
fun FullScreenLoading(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
fun FullScreenError(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Something went wrong", style = MaterialTheme.typography.titleLarge)
            Text(
                message,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )
            Button(onClick = onRetry) { Text("Retry") }
        }
    }
}

/** Decorative hero gradient background used behind the Home screen header. */
@Composable
fun HeroGradientBackground(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(GradientHero))
    ) {
        content()
    }
}
