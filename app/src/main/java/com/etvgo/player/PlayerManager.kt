package com.etvgo.player

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.common.MimeTypes
import androidx.media3.datasource.DefaultHttpDataSource

class PlayerManager(context: Context) {
    private val dataSourceFactory = DefaultHttpDataSource.Factory()
        .setUserAgent("VLC/3.0.20 LibVLC/3.0.20")
        .setAllowCrossProtocolRedirects(true)
        .setConnectTimeoutMs(15000)
        .setReadTimeoutMs(30000)

    var player: ExoPlayer = ExoPlayer.Builder(context)
        .setSeekBackIncrementMs(10000)
        .setSeekForwardIncrementMs(30000)
        .build()

    var currentUrl: String = ""
        private set

    fun play(url: String) {
        if (url == currentUrl && player.isPlaying) return
        currentUrl = url

        player.stop()

        val mediaItem = MediaItem.Builder()
            .setUri(url)
            .setMimeType(MimeTypes.APPLICATION_M3U8)
            .build()

        player.setMediaItem(mediaItem)
        player.prepare()
        player.playWhenReady = true
    }

    fun togglePlayPause() {
        if (player.isPlaying) {
            player.pause()
        } else {
            player.play()
        }
    }

    fun seekForward() {
        player.seekTo(player.currentPosition + 30000)
    }

    fun seekBackward() {
        player.seekTo((player.currentPosition - 10000).coerceAtLeast(0))
    }

    fun release() {
        player.release()
    }
}
