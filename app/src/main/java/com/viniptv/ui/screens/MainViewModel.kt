package com.viniptv.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.viniptv.data.model.*
import com.viniptv.data.repository.IPTVRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = IPTVRepository()

    // Playlist state
    val playlists: StateFlow<List<PlaylistSource>> = repository.playlists
    val activePlaylistId: StateFlow<String> = repository.activePlaylistId
    val hasPlaylists: StateFlow<Boolean> = repository.playlists.map { it.isNotEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // Channel state
    val channels: StateFlow<List<Channel>> = repository.channels
    val categories: StateFlow<List<Category>> = repository.categories
    val favoriteChannelIds: StateFlow<Set<String>> = repository.favoriteChannelIds

    private val _selectedCategoryIndex = MutableStateFlow(0)
    val selectedCategoryIndex: StateFlow<Int> = _selectedCategoryIndex.asStateFlow()

    val filteredChannels: StateFlow<List<Channel>> = combine(
        channels, categories, _selectedCategoryIndex
    ) { chs, cats, idx ->
        if (cats.isEmpty() || idx < 0 || idx >= cats.size) chs
        else {
            val catName = cats[idx].name
            chs.filter { it.category == catName }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteChannels: StateFlow<List<Channel>> = combine(
        channels, favoriteChannelIds
    ) { chs, favIds ->
        chs.filter { it.id in favIds }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected channel for player
    private val _selectedChannel = MutableStateFlow<Channel?>(null)
    val selectedChannel: StateFlow<Channel?> = _selectedChannel.asStateFlow()

    // Player state
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _isFullScreen = MutableStateFlow(false)
    val isFullScreen: StateFlow<Boolean> = _isFullScreen.asStateFlow()

    private val _showChannelBar = MutableStateFlow(false)
    val showChannelBar: StateFlow<Boolean> = _showChannelBar.asStateFlow()

    // EPG
    val epgData: StateFlow<Map<String, List<EPGProgram>>> = repository.epgData

    // VOD
    private val _vodCategories = MutableStateFlow<List<VODCategory>>(emptyList())
    val vodCategories: StateFlow<List<VODCategory>> = _vodCategories.asStateFlow()

    // Search
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isSearchActive = MutableStateFlow(false)
    val isSearchActive: StateFlow<Boolean> = _isSearchActive.asStateFlow()

    val searchResults: StateFlow<List<Channel>> = combine(
        channels, _searchQuery
    ) { chs, query ->
        if (query.isBlank()) emptyList()
        else {
            val q = query.lowercase()
            chs.filter {
                it.name.lowercase().contains(q) ||
                it.category.lowercase().contains(q)
            }.take(30)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val searchVodResults: StateFlow<List<VODItem>> = combine(
        repository.vodMovies, repository.vodSeries, _searchQuery
    ) { movies, series, query ->
        if (query.isBlank()) emptyList()
        else {
            val q = query.lowercase()
            (movies + series).filter {
                it.title.lowercase().contains(q) ||
                it.plot.lowercase().contains(q)
            }.take(20)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        refreshVOD()
    }

    // --- Playlist Management ---
    fun addPlaylist(name: String, type: PlaylistType, url: String = "",
                    serverUrl: String = "", username: String = "", password: String = "") {
        val source = PlaylistSource(
            name = name.ifEmpty { type.name },
            type = type,
            url = url,
            serverUrl = serverUrl,
            username = username,
            password = password
        )
        repository.addPlaylist(source)
        refreshVOD()
    }

    fun removePlaylist(id: String) = repository.removePlaylist(id)
    fun setActivePlaylist(id: String) = repository.setActivePlaylist(id)

    // --- Channel Selection ---
    fun selectCategory(index: Int) {
        _selectedCategoryIndex.value = index
    }

    fun selectChannel(channel: Channel) {
        _selectedChannel.value = channel
        _showChannelBar.value = false
    }

    fun toggleFavorite(channelId: String) = repository.toggleFavorite(channelId)

    // --- Player ---
    fun play(channel: Channel) {
        _selectedChannel.value = channel
        _isPlaying.value = true
    }

    fun togglePlayPause() {
        _isPlaying.value = !_isPlaying.value
    }

    fun setFullScreen(full: Boolean) {
        _isFullScreen.value = full
    }

    fun toggleChannelBar() {
        _showChannelBar.value = !_showChannelBar.value
    }

    // --- Search ---
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSearchActive(active: Boolean) {
        _isSearchActive.value = active
        if (!active) _searchQuery.value = ""
    }

    // --- VOD ---
    fun refreshVOD() {
        viewModelScope.launch {
            _vodCategories.value = repository.getVODCategories()
        }
    }

    fun toggleFavoriteVod(vodId: String) = repository.toggleFavoriteVod(vodId)

    fun searchVod(query: String): List<VODItem> {
        return repository.searchVod(query)
    }
}
