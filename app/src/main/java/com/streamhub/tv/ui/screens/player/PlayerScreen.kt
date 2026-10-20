package com.streamhub.tv.ui.screens.player

import android.content.pm.ActivityInfo
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.streamhub.tv.R
import com.google.accompanist.systemuicontroller.rememberSystemUiController

/**
 * Minimal Just Player-style screen: native Media3 controls, sensor rotation, and no
 * app-specific sheets layered over the video.
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

    LaunchedEffect(channelId) {
        viewModel.loadChannel(channelId)
    }

    DisposableEffect(Unit) {
        systemUiController.isSystemBarsVisible = false
        // Just Player follows the device sensor while a video is open.
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
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
        AndroidView(
            factory = { viewContext ->
                PlayerView(viewContext).apply {
                    player = viewModel.exoPlayer
                    useController = true
                    controllerShowTimeoutMs = 3_500
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
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Black.copy(alpha = 0.78f), Color.Transparent)
                    )
                )
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back),
                    tint = Color.White
                )
            }
            state.channel?.let { channel ->
                Column(modifier = Modifier.padding(start = 4.dp)) {
                    Text(
                        text = channel.name,
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1
                    )
                    Text(
                        text = channel.category,
                        color = Color.White.copy(alpha = 0.70f),
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1
                    )
                }
            }
        }

        if (state.isBuffering && state.errorMessage == null) {
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(36.dp)
            )
        }

        state.errorMessage?.let { message ->
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.Black.copy(alpha = 0.86f))
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Playback error", color = Color.White, style = MaterialTheme.typography.titleLarge)
                Text(
                    text = message,
                    color = Color.White.copy(alpha = 0.72f),
                    modifier = Modifier.padding(top = 6.dp, bottom = 14.dp)
                )
                Button(onClick = viewModel::retryNow) {
                    Text(stringResource(R.string.retry))
                }
            }
        }
    }
}
