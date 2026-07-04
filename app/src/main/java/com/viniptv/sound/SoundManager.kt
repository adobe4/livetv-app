package com.viniptv.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool

class SoundManager(context: Context) {
    private var soundPool: SoundPool? = null
    private var openSoundId: Int = 0
    private var clickSoundId: Int = 0
    private var channelChangeId: Int = 0

    init {
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        soundPool = SoundPool.Builder()
            .setMaxStreams(3)
            .setAudioAttributes(attrs)
            .build()

        try {
            val res = context.resources
            val pkg = context.packageName
            openSoundId = soundPool?.load(context, res.getIdentifier("open_sound", "raw", pkg), 1) ?: 0
            clickSoundId = soundPool?.load(context, res.getIdentifier("click_sound", "raw", pkg), 1) ?: 0
            channelChangeId = soundPool?.load(context, res.getIdentifier("channel_change", "raw", pkg), 1) ?: 0
        } catch (e: Exception) {}
    }

    fun playOpen() = play(openSoundId)
    fun playClick() = play(clickSoundId)
    fun playChannelChange() = play(channelChangeId)

    private fun play(soundId: Int) {
        if (soundId > 0) {
            try { soundPool?.play(soundId, 0.7f, 0.7f, 1, 0, 1f) } catch (_: Exception) {}
        }
    }

    fun release() {
        soundPool?.release()
        soundPool = null
    }
}
