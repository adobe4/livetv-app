package com.viniptv.data.repository

import com.viniptv.data.model.*
import com.viniptv.data.parser.M3UParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class IPTVRepository {
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    private val _playlists = MutableStateFlow<List<PlaylistSource>>(emptyList())
    val playlists: StateFlow<List<PlaylistSource>> = _playlists.asStateFlow()

    private val _activePlaylistId = MutableStateFlow("")
    val activePlaylistId: StateFlow<String> = _activePlaylistId.asStateFlow()

    private val _channels = MutableStateFlow<List<Channel>>(emptyList())
    val channels: StateFlow<List<Channel>> = _channels.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private val _epgData = MutableStateFlow<Map<String, List<EPGProgram>>>(emptyMap())
    val epgData: StateFlow<Map<String, List<EPGProgram>>> = _epgData.asStateFlow()

    private val _vodMovies = MutableStateFlow<List<VODItem>>(emptyList())
    val vodMovies: StateFlow<List<VODItem>> = _vodMovies.asStateFlow()

    private val _vodSeries = MutableStateFlow<List<VODItem>>(emptyList())
    val vodSeries: StateFlow<List<VODItem>> = _vodSeries.asStateFlow()

    private val _favoriteChannelIds = MutableStateFlow<Set<String>>(emptySet())
    val favoriteChannelIds: StateFlow<Set<String>> = _favoriteChannelIds.asStateFlow()

    private val _favoriteVodIds = MutableStateFlow<Set<String>>(emptySet())
    val favoriteVodIds: StateFlow<Set<String>> = _favoriteVodIds.asStateFlow()

    // Loading and error states
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _lastRefreshTime = MutableStateFlow<Long>(0L)
    val lastRefreshTime: StateFlow<Long> = _lastRefreshTime.asStateFlow()

    fun addPlaylist(source: PlaylistSource) {
        val current = _playlists.value.toMutableList()
        current.add(source)
        _playlists.value = current
        if (_activePlaylistId.value.isEmpty()) {
            _activePlaylistId.value = source.id
        }
        _error.value = null
    }

    fun removePlaylist(id: String) {
        _playlists.value = _playlists.value.filter { it.id != id }
        if (_activePlaylistId.value == id) {
            _activePlaylistId.value = _playlists.value.firstOrNull()?.id ?: ""
        }
        // Clear channels if no playlists left
        if (_playlists.value.isEmpty()) {
            _channels.value = emptyList()
            _categories.value = emptyList()
            _vodMovies.value = emptyList()
            _vodSeries.value = emptyList()
        }
    }

    fun setActivePlaylist(id: String) {
        _activePlaylistId.value = id
    }

    suspend fun refreshPlaylist(): Boolean {
        val playlistId = _activePlaylistId.value
        val playlist = _playlists.value.find { it.id == playlistId } ?: return false

        if (playlist.type != PlaylistType.M3U_URL || playlist.url.isBlank()) {
            return false
        }

        _isLoading.value = true

        try {
            val content = fetchUrl(playlist.url)
            val parsedChannels = withContext(Dispatchers.Default) {
                M3UParser.parse(content)
            }
            if (parsedChannels.isEmpty()) {
                _isLoading.value = false
                return false
            }
            _channels.value = parsedChannels
            val catMap = parsedChannels.groupBy { it.category }
            _categories.value = catMap.entries.mapIndexed { index, (name, chs) ->
                Category(
                    id = "cat_$index",
                    name = name,
                    type = CategoryType.LIVE,
                    channelCount = chs.size,
                    isSelected = index == 0
                )
            }
            _lastRefreshTime.value = System.currentTimeMillis()
            _isLoading.value = false
            return true
        } catch (e: Exception) {
            _isLoading.value = false
            return false
        }
    }

    suspend fun fetchAndParseM3U(url: String): Boolean {
        _isLoading.value = true
        // Don't clear error here - we just want to try fetching
        try {
            val content = fetchUrl(url)
            val parsedChannels = withContext(Dispatchers.Default) {
                M3UParser.parse(content)
            }
            if (parsedChannels.isEmpty()) {
                _isLoading.value = false
                return false
            }
            _channels.value = parsedChannels
            val catMap = parsedChannels.groupBy { it.category }
            _categories.value = catMap.entries.mapIndexed { index, (name, chs) ->
                Category(
                    id = "cat_$index",
                    name = name,
                    type = CategoryType.LIVE,
                    channelCount = chs.size,
                    isSelected = index == 0
                )
            }
            _lastRefreshTime.value = System.currentTimeMillis()
            _isLoading.value = false
            return true
        } catch (e: Exception) {
            // Fetch failed - don't set error, just silently fail
            // User can tap refresh to retry
            _isLoading.value = false
            return false
        }
    }

    suspend fun fetchXtreamData(serverUrl: String, username: String, password: String): Boolean {
        _isLoading.value = true
        _error.value = null
        try {
            // Xtream API endpoints
            val baseUrl = "${serverUrl.trimEnd('/')}/player_api.php"
            val liveUrl = "$baseUrl?username=$username&password=$password&action=live_streams"

            val content = fetchUrl(liveUrl)

            // Parse xtream JSON response for channels
            val parser = com.viniptv.data.parser.XtreamParser()
            val parsedChannels = withContext(Dispatchers.Default) {
                parser.parseLiveStreams(content)
            }

            if (parsedChannels.isEmpty()) {
                _error.value = "No channels found. Check your Xtream credentials"
                _isLoading.value = false
                return false
            }

            _channels.value = parsedChannels
            val catMap = parsedChannels.groupBy { it.category }
            _categories.value = catMap.entries.mapIndexed { index, (name, chs) ->
                Category(
                    id = "cat_$index",
                    name = name,
                    type = CategoryType.LIVE,
                    channelCount = chs.size,
                    isSelected = index == 0
                )
            }
            _lastRefreshTime.value = System.currentTimeMillis()
            _isLoading.value = false
            return true
        } catch (e: Exception) {
            _error.value = "Xtream failed: ${e.message ?: "Unknown error"}"
            _isLoading.value = false
            return false
        }
    }

    fun clearError() {
        _error.value = null
    }

    // Simple getter functions
    fun getChannelsByCategory(categoryName: String): List<Channel> {
        return _channels.value.filter { it.category == categoryName }
    }

    fun getFavoriteChannels(): List<Channel> {
        return _channels.value.filter { it.id in _favoriteChannelIds.value }
    }

    fun searchChannels(query: String): List<Channel> {
        if (query.isBlank()) return emptyList()
        val q = query.lowercase()
        return _channels.value.filter { ch ->
            ch.name.lowercase().contains(q) ||
            ch.category.lowercase().contains(q)
        }
    }

    fun toggleFavorite(channelId: String) {
        val current = _favoriteChannelIds.value.toMutableSet()
        if (channelId in current) current.remove(channelId)
        else current.add(channelId)
        _favoriteChannelIds.value = current
    }

    fun toggleFavoriteVod(vodId: String) {
        val current = _favoriteVodIds.value.toMutableSet()
        if (vodId in current) current.remove(vodId)
        else current.add(vodId)
        _favoriteVodIds.value = current
    }

    private suspend fun fetchUrl(url: String): String {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "VLC/3.0.21 LibVLC/3.0.21")
            .header("Accept", "*/*")
            .build()
        val response = httpClient.newCall(request).execute()
        if (!response.isSuccessful) {
            throw Exception("HTTP ${response.code}")
        }
        return response.body?.string() ?: throw Exception("Empty response")
    }
}
