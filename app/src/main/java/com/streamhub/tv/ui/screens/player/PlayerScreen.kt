package com.streamhub.tv.ui.screens.player

import android.app.PictureInPictureParams
import android.content.pm.ActivityInfo
import android.util.Rational
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.PictureInPicture
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.streamhub.tv.R

/**
 * Native playback surface based on Media3's production PlayerView controls.
 * The interaction model follows Just (Video) Player's public-domain upstream:
 * native transport controls, fit/crop sizing, PiP, track actions and fast channel access.
 * The stream and channel business logic remains owned by StreamHub's ViewModel.
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
    var showChannelSheet by remember { mutableStateOf(false) }
    var showSourceSheet by remember { mutableStateOf(false) }
    val channelSheetState = rememberModalBottomSheetState()
    val sourceSheetState = rememberModalBottomSheetState()

    LaunchedEffect(channelId) { viewModel.loadChannel(channelId) }

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
            factory = { viewContext ->
                PlayerView(viewContext).apply {
                    player = viewModel.exoPlayer
                    useController = true
                    controllerShowTimeoutMs = 4_000
                    controllerHideOnTouch = true
                    controllerAutoShow = true
                    setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
                    setShowNextButton(false)
                    setShowPreviousButton(false)
                    setShowFastForwardButton(true)
                    setShowRewindButton(true)
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            update = { playerView ->
                playerView.player = viewModel.exoPlayer
                playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
            },
            modifier = Modifier.fillMaxSize()
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.78f), Color.Transparent)))
                .padding(horizontal = 14.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(R.string.back), tint = Color.White)
                }
                state.channel?.let { channel ->
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color.Black.copy(alpha = 0.45f))
                            .padding(horizontal = 9.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (channel.logo.isNotBlank()) {
                            AsyncImage(channel.logo, null, Modifier.size(30.dp).clip(CircleShape))
                        }
                        Column(modifier = Modifier.padding(start = 9.dp)) {
                            Text(channel.name, color = Color.White, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.LiveTv, null, tint = Color(0xFFFF5B62), modifier = Modifier.size(13.dp))
                                Text(" ${stringResource(R.string.live)} · ${channel.category}", color = Color.White.copy(alpha = 0.78f), style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
            }
            Row {
                IconButton(onClick = { showChannelSheet = true }) {
                    Icon(Icons.Filled.List, contentDescription = stringResource(R.string.live_guide), tint = Color.White)
                }
                IconButton(onClick = { showSourceSheet = true }) {
                    Icon(Icons.Filled.Tune, contentDescription = stringResource(R.string.playback_options), tint = Color.White)
                }
                IconButton(onClick = {
                    activity?.enterPictureInPictureMode(
                        PictureInPictureParams.Builder().setAspectRatio(Rational(16, 9)).build()
                    )
                }) {
                    Icon(Icons.Filled.PictureInPicture, contentDescription = "Picture in Picture", tint = Color.White)
                }
                IconButton(onClick = { isFullscreen = !isFullscreen }) {
                    Icon(
                        if (isFullscreen) Icons.Filled.FullscreenExit else Icons.Filled.Fullscreen,
                        contentDescription = "Fullscreen",
                        tint = Color.White
                    )
                }
            }
        }

        if (state.isBuffering && state.errorMessage == null) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        state.errorMessage?.let { message ->
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.Black.copy(alpha = 0.82f))
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Playback error", color = Color.White, style = MaterialTheme.typography.titleLarge)
                Text(message, color = Color.White.copy(alpha = 0.72f), modifier = Modifier.padding(top = 6.dp, bottom = 14.dp))
                if (state.reconnectAttempt in 1..5) {
                    Text("Reconnecting… ${state.reconnectAttempt}/5", color = Color.White)
                } else {
                    Button(onClick = viewModel::retryNow) { Text(stringResource(R.string.retry)) }
                }
            }
        }
    }

    if (showChannelSheet) {
        ModalBottomSheet(onDismissRequest = { showChannelSheet = false }, sheetState = channelSheetState) {
            Column(modifier = Modifier.padding(bottom = 24.dp)) {
                Text(stringResource(R.string.live_guide), style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(20.dp))
                state.allChannelsInCategory.forEach { channel ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable {
                            viewModel.switchChannel(channel)
                            showChannelSheet = false
                        }.padding(horizontal = 20.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (channel.logo.isNotBlank()) AsyncImage(channel.logo, null, Modifier.size(42.dp).clip(RoundedCornerShape(12.dp)))
                        Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                            Text(channel.name, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                            Text(channel.category, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (channel.id == state.channel?.id) Icon(Icons.Filled.Check, null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }

    if (showSourceSheet) {
        ModalBottomSheet(onDismissRequest = { showSourceSheet = false }, sheetState = sourceSheetState) {
            Column(modifier = Modifier.padding(bottom = 24.dp)) {
                Text(stringResource(R.string.select_server), style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(20.dp))
                state.availableSources.forEachIndexed { index, source ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable {
                            viewModel.selectSource(index)
                            showSourceSheet = false
                        }.padding(horizontal = 20.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(source.label, style = MaterialTheme.typography.titleMedium)
                        if (index == state.selectedSourceIndex) Icon(Icons.Filled.Check, null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}
