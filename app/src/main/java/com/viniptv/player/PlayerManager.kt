package com.viniptv.player

import android.content.Context
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.exoplayer.trackselection.TrackSelector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@UnstableApi
class PlayerManager(context: Context) {
    private val dataSourceFactory = DefaultHttpDataSource.Factory()
        .setUserAgent("VLC/3.0.21 LibVLC/3.0.21")
        .setAllowCrossProtocolRedirects(true)
        .setConnectTimeoutMs(15000)
        .setReadTimeoutMs(30000)

    private val trackSelector: TrackSelector = DefaultTrackSelector(context)

    val player: ExoPlayer = ExoPlayer.Builder(context)
        .setTrackSelector(trackSelector)
        .setSeekBackIncrementMs(10000)
        .setSeekForwardIncrementMs(30000)
        .build()

    private val _bufferPercent = MutableStateFlow(0)
    val bufferPercent: StateFlow<Int> = _bufferPercent.asStateFlow()

    private val _isBuffering = MutableStateFlow(false)
    val isBuffering: StateFlow<Boolean> = _isBuffering.asStateFlow()

    var currentUrl: String = ""
        private set

    var bufferSize: Int = 4096 * 1024
    var useHardwareDecoding: Boolean = true

    init {
        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                _isBuffering.value = state == Player.STATE_BUFFERING
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (isPlaying) _isBuffering.value = false
            }
        })
    }

    fun play(url: String) {
        if (url == currentUrl && player.isPlaying) return
        currentUrl = url
        player.stop()
        val mediaItem = MediaItem.Builder()
            .setUri(url)
            .setMediaMetadata(androidx.media3.common.MediaMetadata.Builder()
                .setMediaType(androidx.media3.common.MediaMetadata.MEDIA_TYPE_MOVIE)
                .build())
            .build()
        player.setMediaItem(mediaItem)
        player.prepare()
        player.playWhenReady = true
    }

    fun togglePlayPause() {
        if (player.isPlaying) player.pause() else player.play()
    }

    fun seekTo(positionMs: Long) = player.seekTo(positionMs)
    fun seekForward() = player.seekTo((player.currentPosition + 30000).coerceAtMost(player.duration))
    fun seekBackward() { player.seekTo((player.currentPosition - 10000).coerceAtLeast(0)) }

    fun setVolume(vol: Float) { player.volume = vol.coerceIn(0f, 1f) }
    fun setSpeed(speed: Float) { player.setPlaybackSpeed(speed) }

    val currentPosition: Long
        get() = player.currentPosition
    val duration: Long
        get() = player.duration
    val isPlaying: Boolean
        get() = player.isPlaying

    fun release() { player.release() }
}
