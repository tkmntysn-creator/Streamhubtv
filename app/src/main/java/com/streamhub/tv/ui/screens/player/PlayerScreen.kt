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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.PictureInPicture
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
 * Features implemented here:
 *  - HLS / DASH / MP4 playback (source selection happens in [PlayerViewModel])
 *  - Fullscreen landscape toggle with immersive system bars
 *  - Picture-in-Picture
 *  - Vertical swipe gestures: right half = volume, left half = screen brightness
 *  - Tap anywhere on the video to show/hide the controls; controls auto-hide after
 *    5 seconds of inactivity, just like YouTube / Netflix-style players
 *  - Buffering spinner + error overlay with automatic reconnect and manual retry
 *  - Channel logo / name / category overlay
 *  - Next/previous channel switching within the same category
 */
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
    var showVolumeIndicator by remember { mutableStateOf(false) }
    var showBrightnessIndicator by remember { mutableStateOf(false) }

    LaunchedEffect(channelId) { viewModel.loadChannel(channelId) }

    // Auto-hide the control overlay after 5 seconds, same as premium streaming apps.
    // Any tap that re-shows the controls restarts this timer.
    LaunchedEffect(controlsVisible, state.isBuffering, state.errorMessage) {
        if (controlsVisible && !state.isBuffering && state.errorMessage == null) {
            delay(5000)
            controlsVisible = false
        }
    }

    // Briefly show the volume/brightness indicator, then auto-hide it shortly after
    // the user stops dragging.
    LaunchedEffect(showVolumeIndicator) {
        if (showVolumeIndicator) {
            delay(1200)
            showVolumeIndicator = false
        }
    }
    LaunchedEffect(showBrightnessIndicator) {
        if (showBrightnessIndicator) {
            delay(1200)
            showBrightnessIndicator = false
        }
    }

    // Immersive fullscreen handling
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Video surface
        AndroidView(
            factory = {
                PlayerView(it).apply {
                    player = viewModel.exoPlayer
                    useController = false // custom controls drawn in Compose below
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Gesture layer: tap anywhere toggles the controls. Vertical drag on the
        // left half adjusts brightness, right half adjusts volume.
        Row(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = { controlsVisible = !controlsVisible })
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
                        detectTapGestures(onTap = { controlsVisible = !controlsVisible })
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

        // Volume / brightness quick indicators (center-screen, auto-hide)
        if (showVolumeIndicator) {
            GestureIndicator(icon = Icons.Filled.VolumeUp, level = volumeLevel, modifier = Modifier.align(Alignment.Center))
        }
        if (showBrightnessIndicator) {
            GestureIndicator(icon = Icons.Filled.BrightnessMedium, level = brightnessLevel, modifier = Modifier.align(Alignment.Center))
        }

        // Top overlay: back button + channel info - fades in/out with controlsVisible
        AnimatedVisibility(
            visible = controlsVisible,
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RoundIconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    state.channel?.let { channel ->
                        AsyncImage(
                            model = channel.logo,
                            contentDescription = null,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .padding(start = 4.dp)
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
            }
        }

        // Bottom overlay: playback + favorite + fullscreen + PiP + next/prev controls
        AnimatedVisibility(
            visible = controlsVisible,
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

                RoundIconButton(onClick = {
                    activity?.enterPictureInPictureMode(
                        PictureInPictureParams.Builder().setAspectRatio(Rational(16, 9)).build()
                    )
                }) {
                    Icon(Icons.Filled.PictureInPicture, contentDescription = "Picture in Picture", tint = Color.White)
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

        // Loading spinner - always visible regardless of controls state
        if (state.isBuffering && state.errorMessage == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }

        // Error + auto-reconnect overlay - always visible regardless of controls state
        state.errorMessage?.let { message ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.88f)),
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
}

/** A circular, semi-transparent glass button used for every player control icon. */
@Composable
private fun RoundIconButton(onClick: () -> Unit, content: @Composable () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.15f))
    ) {
        content()
    }
}

/** Small center-screen pill showing the current volume/brightness level while dragging. */
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
            modifier = Modifier
                .padding(top = 8.dp)
                .size(width = 80.dp, height = 4.dp)
                .clip(CircleShape),
            color = Color.White,
            trackColor = Color.White.copy(alpha = 0.25f)
        )
    }
}
