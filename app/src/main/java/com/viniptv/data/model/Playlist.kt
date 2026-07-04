package com.viniptv.data.model

data class PlaylistSource(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val type: PlaylistType,
    val url: String = "",
    val username: String = "",
    val password: String = "",
    val serverUrl: String = "",
    val epgUrl: String = "",
    val isActive: Boolean = true,
    val lastUpdated: Long = System.currentTimeMillis()
)

enum class PlaylistType { M3U_URL, M3U_FILE, XTREAM }

data class AppConfig(
    val playlists: List<PlaylistSource> = emptyList(),
    val activePlaylistId: String = "",
    val bufferSize: Int = 4096,
    val useHardwareDecoding: Boolean = true,
    val audioTrack: Int = 0,
    val subtitleEnabled: Boolean = false,
    val subtitleTrack: Int = 0,
    val aspectRatio: String = "fit",
    val parentalPin: String = "",
    val adultFilter: Boolean = false,
    val themeMode: String = "dark"
)
