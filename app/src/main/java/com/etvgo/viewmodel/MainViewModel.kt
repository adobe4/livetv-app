package com.etvgo.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.etvgo.data.model.*
import com.etvgo.data.parser.EPGParser
import com.etvgo.network.M3ULoader
import com.etvgo.network.XtreamClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

data class MainUiState(
    val channels: List<Channel> = emptyList(),
    val filteredChannels: List<Channel> = emptyList(),
    val categories: List<Category> = emptyList(),
    val selectedCategory: String = "All",
    val currentChannel: Channel? = null,
    val isPlaying: Boolean = false,
    val isLoading: Boolean = false,
    val loadingMessage: String = "",
    val searchQuery: String = "",
    val epgPrograms: List<EPGProgram> = emptyList(),
    val showPicker: Boolean = false
)

class MainViewModel : ViewModel() {
    private val _state = MutableStateFlow(MainUiState())
    val state: StateFlow<MainUiState> = _state.asStateFlow()

    private val m3uLoader = M3ULoader()
    private val xtreamClient = XtreamClient()
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private var allChannels: List<Channel> = emptyList()

    fun loadM3uUrl(url: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, loadingMessage = "Loading playlist...") }
            val result = m3uLoader.loadFromUrl(url)
            result.onSuccess { channels ->
                allChannels = channels
                updateState()
                _state.update { it.copy(isLoading = false) }
            }.onFailure { e ->
                _state.update { it.copy(isLoading = false, loadingMessage = "") }
            }
        }
    }

    fun loadM3uFile(context: Context, uri: Uri) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, loadingMessage = "Reading file...") }
            try {
                val content = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.bufferedReader()?.readText() ?: ""
                }
                allChannels = m3uLoader.loadFromContent(content)
                updateState()
            } catch (e: Exception) { }
            _state.update { it.copy(isLoading = false) }
        }
    }

    fun connectXtream(server: String, username: String, password: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, loadingMessage = "Authenticating...") }
            val result = xtreamClient.authenticate(server, username, password)
            result.onSuccess { auth ->
                _state.update { it.copy(loadingMessage = "Loading channels...") }
                try {
                    val response = withContext(Dispatchers.IO) {
                        httpClient.newCall(Request.Builder()
                            .url("${auth.playerApi}&action=get_live_streams")
                            .build()).execute()
                    }
                    val body = response.body?.string() ?: ""
                    val channels = parseXtreamLiveStreams(body, auth.liveStreamsUrl)
                    allChannels = channels
                    updateState()
                } catch (e: Exception) { }
            }.onFailure { }
            _state.update { it.copy(isLoading = false) }
        }
    }

    private fun parseXtreamLiveStreams(json: String, baseUrl: String): List<Channel> {
        val channels = mutableListOf<Channel>()
        try {
            val jsonObj = org.json.JSONObject(json)
            val liveStreams = jsonObj.optJSONArray("live_streams") ?: return channels
            for (i in 0 until liveStreams.length()) {
                val stream = liveStreams.getJSONObject(i)
                val streamId = stream.optInt("stream_id")
                val name = stream.optString("name")
                val logo = stream.optString("stream_icon", "")
                val categoryName = stream.optString("category_name", "Uncategorized")
                channels.add(Channel(
                    id = "xtream_$streamId",
                    name = name,
                    logoUrl = logo,
                    category = categoryName,
                    url = "$baseUrl/$streamId.ts"
                ))
            }
        } catch (e: Exception) { }
        return channels
    }

    fun selectCategory(category: String) {
        _state.update { it.copy(selectedCategory = category) }
        filterChannels()
    }

    fun search(query: String) {
        _state.update { it.copy(searchQuery = query) }
        filterChannels()
    }

    private fun filterChannels() {
        val s = _state.value
        var filtered = allChannels
        if (s.selectedCategory != "All") {
            filtered = filtered.filter { it.category == s.selectedCategory }
        }
        if (s.searchQuery.isNotEmpty()) {
            val q = s.searchQuery.lowercase()
            filtered = filtered.filter { it.name.lowercase().contains(q) }
        }
        val categories = listOf(Category("All", allChannels.size)) +
                allChannels.groupBy { it.category }
                    .entries.sortedBy { it.key }
                    .map { (key, value) -> Category(key, value.size) }
        _state.update { it.copy(filteredChannels = filtered, categories = categories) }
    }

    private fun updateState() {
        val categories = listOf(Category("All", allChannels.size)) +
                allChannels.groupBy { it.category }
                    .entries.sortedBy { it.key }
                    .map { (key, value) -> Category(key, value.size) }
        _state.update {
            it.copy(
                channels = allChannels,
                filteredChannels = allChannels,
                categories = categories,
                selectedCategory = "All"
            )
        }
    }

    fun playChannel(channel: Channel) {
        _state.update { it.copy(currentChannel = channel, isPlaying = true, showPicker = false) }
    }

    fun togglePlayPause() {
        _state.update { it.copy(isPlaying = !it.value.isPlaying) }
    }

    fun nextChannel() {
        val s = _state.value
        val list = s.filteredChannels
        if (list.isEmpty() || s.currentChannel == null) return
        val idx = list.indexOfFirst { it.id == s.currentChannel.id }
        val next = (idx + 1) % list.size
        playChannel(list[next])
    }

    fun prevChannel() {
        val s = _state.value
        val list = s.filteredChannels
        if (list.isEmpty() || s.currentChannel == null) return
        val idx = list.indexOfFirst { it.id == s.currentChannel.id }
        val prev = if (idx <= 0) list.size - 1 else idx - 1
        playChannel(list[prev])
    }

    fun toggleFavorite(channel: Channel) {
        allChannels = allChannels.map {
            if (it.id == channel.id) it.copy(isFavorite = !it.isFavorite) else it
        }
        filterChannels()
    }

    fun togglePicker() {
        _state.update { it.copy(showPicker = !it.showPicker) }
    }
}
