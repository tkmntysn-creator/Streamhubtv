package com.streamhub.tv.ui.screens.player

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.dash.DashMediaSource
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import com.streamhub.tv.data.model.Channel
import com.streamhub.tv.data.model.StreamSource
import com.streamhub.tv.data.model.StreamType
import com.streamhub.tv.data.repository.ChannelRepository
import com.streamhub.tv.data.repository.FavoritesRepository
import com.streamhub.tv.data.repository.WatchHistoryRepository
import com.streamhub.tv.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import javax.inject.Inject

data class PlayerUiState(
    val channel: Channel? = null,
    val allChannelsInCategory: List<Channel> = emptyList(),
    val isBuffering: Boolean = true,
    val isFavorite: Boolean = false,
    val errorMessage: String? = null,
    val reconnectAttempt: Int = 0,
    val isPlaying: Boolean = true,
    val availableSources: List<StreamSource> = emptyList(),
    val selectedSourceIndex: Int = 0
)

private const val MAX_RECONNECT_ATTEMPTS = 5

@HiltViewModel
class PlayerViewModel @Inject constructor(
    application: Application,
    private val channelRepository: ChannelRepository,
    private val favoritesRepository: FavoritesRepository,
    private val watchHistoryRepository: WatchHistoryRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState

    private val okHttpClient = OkHttpClient.Builder().build()

    val exoPlayer: ExoPlayer by lazy {
        ExoPlayer.Builder(application).build().apply {
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    _uiState.value = _uiState.value.copy(
                        isBuffering = playbackState == Player.STATE_BUFFERING
                    )
                    if (playbackState == Player.STATE_READY) {
                        _uiState.value = _uiState.value.copy(errorMessage = null, reconnectAttempt = 0)
                    }
                }

                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _uiState.value = _uiState.value.copy(isPlaying = isPlaying)
                }

                override fun onPlayerError(error: PlaybackException) {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = error.message ?: "Playback error",
                        isBuffering = false
                    )
                    attemptReconnect()
                }
            })
        }
    }

    private var reconnectJob: Job? = null

    fun loadChannel(channelId: String) {
        viewModelScope.launch {
            val cached = channelRepository.getCachedChannels()
            val list = if (cached is Resource.Success) cached.data else emptyList()
            val channel = list.firstOrNull { it.id == channelId }
            val sameCategory = list.filter { it.category == channel?.category }

            _uiState.value = _uiState.value.copy(
                channel = channel,
                allChannelsInCategory = sameCategory,
                availableSources = channel?.allSources.orEmpty(),
                selectedSourceIndex = 0
            )

            channel?.let {
                _uiState.value = _uiState.value.copy(isFavorite = favoritesRepository.isFavorite(it.id))
                watchHistoryRepository.recordWatch(it)
                playSource(it.allSources.first().url)
            }
        }
    }

    private fun playSource(url: String) {
        val mediaSource = buildMediaSource(url)
        exoPlayer.setMediaSource(mediaSource)
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
    }

    private fun buildMediaSource(url: String): MediaSource {
        val dataSourceFactory = OkHttpDataSource.Factory(okHttpClient)
        val mediaItem = MediaItem.fromUri(url)
        val type = _uiState.value.channel?.streamTypeOf(url) ?: StreamType.OTHER
        return when (type) {
            StreamType.HLS -> HlsMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem)
            StreamType.DASH -> DashMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem)
            else -> ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem)
        }
    }

    /** Switch to a different mirror/server for the *same* channel (from the ⚙ source picker). */
    fun selectSource(index: Int) {
        val sources = _uiState.value.availableSources
        if (index !in sources.indices) return
        _uiState.value = _uiState.value.copy(
            selectedSourceIndex = index,
            errorMessage = null,
            reconnectAttempt = 0
        )
        playSource(sources[index].url)
    }

    fun togglePlayPause() {
        exoPlayer.playWhenReady = !exoPlayer.playWhenReady
    }

    /** Auto-reconnect with exponential backoff, up to [MAX_RECONNECT_ATTEMPTS] attempts.
     *  If more than one source is available, each retry also rotates to the next
     *  server, so a dead mirror doesn't get retried forever while a working one sits idle. */
    private fun attemptReconnect() {
        reconnectJob?.cancel()
        val attempt = _uiState.value.reconnectAttempt + 1
        if (attempt > MAX_RECONNECT_ATTEMPTS) return
        _uiState.value = _uiState.value.copy(reconnectAttempt = attempt)
        reconnectJob = viewModelScope.launch {
            delay(1500L * attempt)
            val sources = _uiState.value.availableSources
            if (sources.size > 1) {
                val nextIndex = (_uiState.value.selectedSourceIndex + 1) % sources.size
                _uiState.value = _uiState.value.copy(selectedSourceIndex = nextIndex)
                playSource(sources[nextIndex].url)
            } else {
                sources.firstOrNull()?.let { playSource(it.url) }
            }
        }
    }

    fun retryNow() {
        _uiState.value = _uiState.value.copy(reconnectAttempt = 0, errorMessage = null)
        val sources = _uiState.value.availableSources
        sources.getOrNull(_uiState.value.selectedSourceIndex)?.let { playSource(it.url) }
    }

    fun switchChannel(channel: Channel) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                channel = channel,
                errorMessage = null,
                reconnectAttempt = 0,
                availableSources = channel.allSources,
                selectedSourceIndex = 0
            )
            _uiState.value = _uiState.value.copy(isFavorite = favoritesRepository.isFavorite(channel.id))
            watchHistoryRepository.recordWatch(channel)
            playSource(channel.allSources.first().url)
        }
    }

    fun toggleFavorite() {
        val channel = _uiState.value.channel ?: return
        viewModelScope.launch {
            favoritesRepository.toggleFavorite(channel)
            _uiState.value = _uiState.value.copy(isFavorite = favoritesRepository.isFavorite(channel.id))
        }
    }

    override fun onCleared() {
        reconnectJob?.cancel()
        exoPlayer.release()
        super.onCleared()
    }
}
