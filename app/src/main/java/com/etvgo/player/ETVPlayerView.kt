package com.etvgo.player

import android.content.Context
import android.view.ViewGroup
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView

class ETVPlayerView(context: Context) : PlayerView(context) {
    private var playerManager: PlayerManager? = null

    init {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        setUseController(false)
        setShowBuffering(SHOW_BUFFERING_ALWAYS)
        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
        playerManager = PlayerManager(context)
        player = playerManager?.player
    }

    fun play(url: String) {
        playerManager?.play(url)
    }

    fun releasePlayer() {
        playerManager?.release()
        player = null
    }
}
