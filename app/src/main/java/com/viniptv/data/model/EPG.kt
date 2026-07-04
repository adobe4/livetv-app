package com.viniptv.data.model

data class EPGProgram(
    val channelId: String,
    val title: String,
    val description: String = "",
    val startTime: Long,
    val endTime: Long,
    val isLive: Boolean = false,
    val category: String = ""
) {
    val isCurrentlyPlaying: Boolean get() {
        val now = System.currentTimeMillis()
        return now >= startTime && now < endTime
    }

    val progress: Float get() {
        if (!isCurrentlyPlaying) return 0f
        val total = endTime - startTime
        val elapsed = System.currentTimeMillis() - startTime
        return (elapsed.toFloat() / total).coerceIn(0f, 1f)
    }

    val durationMinutes: Int get() = ((endTime - startTime) / 60000).toInt()
}

data class EPGData(val channelId: String, val programs: List<EPGProgram> = emptyList())
