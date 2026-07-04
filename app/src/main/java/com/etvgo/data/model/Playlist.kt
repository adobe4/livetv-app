package com.etvgo.data.model

sealed class PlaylistSource {
    data class M3uUrl(val url: String) : PlaylistSource()
    data class M3uFile(val fileName: String, val content: String) : PlaylistSource()
    data class XtreamCodes(
        val serverUrl: String,
        val username: String,
        val password: String
    ) : PlaylistSource()
}

data class Playlist(
    val id: String,
    val name: String,
    val source: PlaylistSource,
    val channels: List<Channel> = emptyList(),
    val epgUrl: String = "",
    val lastUpdated: Long = System.currentTimeMillis()
)
