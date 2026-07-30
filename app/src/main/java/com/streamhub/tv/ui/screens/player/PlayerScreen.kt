package com.streamhub.tv.ui.screens.player

import android.app.PictureInPictureParams
import android.content.pm.ActivityInfo
import android.util.Rational
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PictureInPicture
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.delay

/**
 * Full-featured live TV player screen built on Media3 ExoPlayer.
 *
 * Design goals (inspired by minimalist players like "Just Player"):
 *  - Very few visible buttons at any time - a clean, uncluttered look
 *  - Double-tap left/right to seek -10s / +10s (works when the stream exposes a
 *    seekable/DVR window; harmless no-op on pure live edge otherwise)
 *  - Long-press anywhere to lock the screen: hides every control and gesture except
 *    a small lock icon used to unlock
 *  - Tap once to show/hide the (few) controls; they also auto-hide after 5 seconds
 *  - Vertical swipe: right half = volume, left half = brightness
 *  - Multi-server (⚙) picker, fullscreen, Picture-in-Picture, auto-reconnect
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    channelId: String,
    onBack: () -> Unit,
    viewModel: PlayerViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val activity = context as? ComponentActivity
    val systemUiController = rememberSystemUiController()

    var isFullscreen by remember { mutableStateOf(false) }
    var volumeLevel by remember { mutableStateOf(0.5f) }
    var brightnessLevel by remember { mutableStateOf(0.5f) }
    var controlsVisible by remember { mutableStateOf(true) }
    var isLocked by remember { mutableStateOf(false) }
    var showVolumeIndicator by remember { mutableStateOf(false) }
    var showBrightnessIndicator by remember { mutableStateOf(false) }
    var seekFeedback by remember { mutableStateOf<Boolean?>(null) }
    var showSourceSheet by remember { mutableStateOf(false) }
    val sourceSheetState = rememberModalBottomSheetState()

    LaunchedEffect(channelId) { viewModel.loadChannel(channelId) }

    LaunchedEffect(controlsVisible, state.isBuffering, state.errorMessage) {
        if (controlsVisible && !state.isBuffering && state.errorMessage == null) {
            delay(5000)
            controlsVisible = false
        }
    }
    LaunchedEffect(showVolumeIndicator) {
        if (showVolumeIndicator) { delay(1200); showVolumeIndicator = false }
    }
    LaunchedEffect(showBrightnessIndicator) {
        if (showBrightnessIndicator) { delay(1200); showBrightnessIndicator = false }
    }
    LaunchedEffect(seekFeedback) {
        if (seekFeedback != null) { delay(600); seekFeedback = null }
    }
    LaunchedEffect(isLocked) {
        if (isLocked) controlsVisible = false
    }

    DisposableEffect(isFullscreen) {
        systemUiController.isSystemBarsVisible = !isFullscreen
        activity?.requestedOrientation = if (isFullscreen) {
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } else {
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
        onDispose {
            systemUiController.isSystemBarsVisible = true
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            factory = {
                PlayerView(it).apply {
                    player = viewModel.exoPlayer
                    useController = false
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        if (!isLocked) {
            Row(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onTap = { controlsVisible = !controlsVisible },
                                onDoubleTap = {
                                    viewModel.exoPlayer.seekTo(
                                        (viewModel.exoPlayer.currentPosition - 10_000).coerceAtLeast(0)
                                    )
                                    seekFeedback = false
                                },
                                onLongPress = { isLocked = true }
                            )
                        }
                        .pointerInput(Unit) {
                            detectVerticalDragGestures(
                                onVerticalDrag = { _, dragAmount ->
                                    brightnessLevel = (brightnessLevel - dragAmount / 1000f).coerceIn(0f, 1f)
                                    showBrightnessIndicator = true
                                    activity?.window?.let { window ->
                                        window.attributes = window.attributes.apply {
                                            screenBrightness = brightnessLevel
                                        }
                                    }
                                }
                            )
                        }
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onTap = { controlsVisible = !controlsVisible },
                                onDoubleTap = {
                                    viewModel.exoPlayer.seekTo(viewModel.exoPlayer.currentPosition + 10_000)
                                    seekFeedback = true
                                },
                                onLongPress = { isLocked = true }
                            )
                        }
                        .pointerInput(Unit) {
                            detectVerticalDragGestures(
                                onVerticalDrag = { _, dragAmount ->
                                    volumeLevel = (volumeLevel - dragAmount / 1000f).coerceIn(0f, 1f)
                                    showVolumeIndicator = true
                                    viewModel.exoPlayer.volume = volumeLevel
                                }
                            )
                        }
                )
            }
        }

        if (showVolumeIndicator) {
            GestureIndicator(Icons.Filled.VolumeUp, volumeLevel, Modifier.align(Alignment.Center))
        }
        if (showBrightnessIndicator) {
            GestureIndicator(Icons.Filled.BrightnessMedium, brightnessLevel, Modifier.align(Alignment.Center))
        }
        seekFeedback?.let { forward ->
            Box(
                modifier = Modifier
                    .align(if (forward) Alignment.CenterEnd else Alignment.CenterStart)
                    .padding(horizontal = 48.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.55f))
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = if (forward) Icons.Filled.Forward10 else Icons.Filled.Replay10,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        if (isLocked) {
            IconButton(
                onClick = { isLocked = false },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(20.dp)
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f))
            ) {
                Icon(Icons.Filled.Lock, contentDescription = "Unlock screen", tint = Color.White)
            }
        }

        val showChrome = controlsVisible && !isLocked

        AnimatedVisibility(
            visible = showChrome,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopStart)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.7f), Color.Transparent)))
                    .padding(horizontal = 8.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RoundIconButton(onClick = onBack) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                        state.channel?.let { channel ->
                            AsyncImage(
                                model = channel.logo,
                                contentDescription = null,
                                modifier = Modifier.size(36.dp).clip(CircleShape).padding(start = 4.dp)
                            )
                            Column(modifier = Modifier.padding(start = 10.dp)) {
                                Text(channel.name, color = Color.White, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    channel.category,
                                    color = Color.White.copy(alpha = 0.75f),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                    RoundIconButton(onClick = {
                        activity?.enterPictureInPictureMode(
                            PictureInPictureParams.Builder().setAspectRatio(Rational(16, 9)).build()
                        )
                    }) {
                        Icon(Icons.Filled.PictureInPicture, contentDescription = "Picture in Picture", tint = Color.White)
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = showChrome,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            IconButton(
                onClick = viewModel::togglePlayPause,
                modifier = Modifier.size(64.dp).clip(CircleShape).background(Color.White)
            ) {
                Icon(
                    imageVector = if (state.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = if (state.isPlaying) "Pause" else "Play",
                    tint = Color.Black,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        AnimatedVisibility(
            visible = showChrome,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f))))
                    .padding(horizontal = 12.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RoundIconButton(onClick = {
                    val index = state.allChannelsInCategory.indexOfFirst { it.id == state.channel?.id }
                    if (index > 0) viewModel.switchChannel(state.allChannelsInCategory[index - 1])
                }) {
                    Icon(Icons.Filled.SkipPrevious, contentDescription = "Previous channel", tint = Color.White)
                }

                RoundIconButton(onClick = viewModel::toggleFavorite) {
                    Icon(
                        imageVector = if (state.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "Toggle favorite",
                        tint = if (state.isFavorite) MaterialTheme.colorScheme.tertiary else Color.White
                    )
                }

                RoundIconButton(onClick = { showSourceSheet = true }) {
                    Icon(Icons.Filled.Settings, contentDescription = "Server settings", tint = Color.White)
                }

                RoundIconButton(onClick = { isFullscreen = !isFullscreen }) {
                    Icon(
                        imageVector = if (isFullscreen) Icons.Filled.FullscreenExit else Icons.Filled.Fullscreen,
                        contentDescription = "Toggle fullscreen",
                        tint = Color.White
                    )
                }

                RoundIconButton(onClick = {
                    val index = state.allChannelsInCategory.indexOfFirst { it.id == state.channel?.id }
                    if (index in 0 until state.allChannelsInCategory.size - 1) {
                        viewModel.switchChannel(state.allChannelsInCategory[index + 1])
                    }
                }) {
                    Icon(Icons.Filled.SkipNext, contentDescription = "Next channel", tint = Color.White)
                }
            }
        }

        if (state.isBuffering && state.errorMessage == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }

        state.errorMessage?.let { message ->
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.88f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Playback error", color = Color.White, style = MaterialTheme.typography.titleLarge)
                    Text(
                        message,
                        color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                    )
                    if (state.reconnectAttempt in 1..5) {
                        Text(
                            "Reconnecting... (attempt ${state.reconnectAttempt}/5)",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        Button(onClick = viewModel::retryNow) { Text("Retry") }
                    }
                }
            }
        }
    }

    if (showSourceSheet) {
        ModalBottomSheet(onDismissRequest = { showSourceSheet = false }, sheetState = sourceSheetState) {
            Column(modifier = Modifier.padding(bottom = 24.dp)) {
                Text(
                    "Select server",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
                state.availableSources.forEachIndexed { index, source ->
                    val selected = index == state.selectedSourceIndex
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.selectSource(index)
                                showSourceSheet = false
                            }
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(source.label, style = MaterialTheme.typography.titleMedium)
                        if (selected) {
                            Icon(Icons.Filled.Check, contentDescription = "Currently selected", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RoundIconButton(onClick: () -> Unit, content: @Composable () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(44.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.15f))
    ) {
        content()
    }
}

@Composable
private fun GestureIndicator(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    level: Float,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.large)
            .background(Color.Black.copy(alpha = 0.6f))
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
        LinearProgressIndicator(
            progress = { level },
            modifier = Modifier.padding(top = 8.dp).size(width = 80.dp, height = 4.dp).clip(CircleShape),
            color = Color.White,
            trackColor = Color.White.copy(alpha = 0.25f)
        )
    }
}
