package com.viniptv.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.viniptv.data.model.*
import com.viniptv.player.PlayerManager
import com.viniptv.ui.epg.EPGGrid
import com.viniptv.ui.settings.MultiPlaylistScreen
import com.viniptv.ui.settings.SettingsScreen
import com.viniptv.ui.theme.VinColors
import com.viniptv.ui.vod.VODScreen

// Screen states for navigation
sealed class AppScreen {
    object Home : AppScreen()
    object LiveTV : AppScreen()
    object Movies : AppScreen()
    object Series : AppScreen()
    object Sports : AppScreen()
    object EPG : AppScreen()
    object Settings : AppScreen()
    object ManagePlaylists : AppScreen()
    data class FullPlayer(val channel: Channel) : AppScreen()
}

@Composable
fun MainScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val hasPlaylists by viewModel.hasPlaylists.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    val channels by viewModel.channels.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val selectedCategoryIndex by viewModel.selectedCategoryIndex.collectAsState()
    val selectedChannel by viewModel.selectedChannel.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val isSearchActive by viewModel.isSearchActive.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val searchVodResults by viewModel.searchVodResults.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val favoriteChannelIds by viewModel.favoriteChannelIds.collectAsState()
    val epgData by viewModel.epgData.collectAsState()
    val vodCategories by viewModel.vodCategories.collectAsState()
    val filteredChannels by viewModel.filteredChannels.collectAsState()

    var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.Home) }
    var showAddPlaylistDialog by remember { mutableStateOf(false) }
    var importMode by remember { mutableStateOf(0) }
    var playlistName by remember { mutableStateOf("") }
    var m3uUrl by remember { mutableStateOf("") }
    var xtreamServer by remember { mutableStateOf("") }
    var xtreamUser by remember { mutableStateOf("") }
    var xtreamPass by remember { mutableStateOf("") }

    // PlayerManager
    val playerManager = remember { PlayerManager(context) }

    // Cleanup player
    DisposableEffect(Unit) {
        onDispose { playerManager.release() }
    }

    // Auto-play when channel selected in non-fullscreen mode
    LaunchedEffect(selectedChannel) {
        selectedChannel?.let { ch ->
            playerManager.play(ch.url)
        }
    }

    // Welcome screen - MUST add playlist, no skip
    if (!hasPlaylists) {
        WelcomeScreen(
            onAddM3uUrl = {
                importMode = 0
                playlistName = ""
                m3uUrl = ""
                showAddPlaylistDialog = true
            },
            onAddXtream = {
                importMode = 1
                playlistName = ""
                xtreamServer = ""
                xtreamUser = ""
                xtreamPass = ""
                showAddPlaylistDialog = true
            }
        )
        return
    }

    // Handle navigation
    when (val screen = currentScreen) {
        is AppScreen.FullPlayer -> {
            FullScreenPlayerScreen(
                channel = screen.channel,
                isPlaying = isPlaying,
                channels = filteredChannels,
                favoriteChannelIds = favoriteChannelIds,
                epgData = epgData[screen.channel.epgChannelId] ?: emptyList(),
                onPlayPause = { viewModel.togglePlayPause() },
                onClose = { currentScreen = AppScreen.LiveTV },
                onChannelSelected = { viewModel.selectChannel(it); viewModel.play(it) },
                onToggleFavorite = { viewModel.toggleFavorite(it) },
                onToggleChannelBar = { viewModel.toggleChannelBar() },
                showChannelBar = viewModel.showChannelBar.collectAsState().value
            )
            return // Exit early - full screen mode
        }

        is AppScreen.Sports -> {
            // Sports redirects to Live TV with Sports category pre-selected
            currentScreen = AppScreen.LiveTV
        }
        is AppScreen.Home -> {
            HomeScreen(
                onLiveTv = { currentScreen = AppScreen.LiveTV },
                onVodMovies = { viewModel.setSearchActive(true); currentScreen = AppScreen.Movies },
                onVodSeries = { viewModel.setSearchActive(true); currentScreen = AppScreen.Series },
                onSports = {
                    // Pre-select sports category
                    val sportsIdx = categories.indexOfFirst { it.name.contains("Sport", ignoreCase = true) }
                    if (sportsIdx >= 0) viewModel.selectCategory(sportsIdx)
                    currentScreen = AppScreen.LiveTV
                },
                onEpg = { currentScreen = AppScreen.EPG },
                onSettings = { currentScreen = AppScreen.Settings }
            )
        }

        is AppScreen.LiveTV -> {
            Box(modifier = Modifier.fillMaxSize().background(VinColors.Background)) {
                LiveTVScreen(
                    channels = filteredChannels,
                    categories = categories.map { it.name },
                    selectedCategoryIndex = selectedCategoryIndex,
                    selectedChannel = selectedChannel,
                    isPlaying = isPlaying,
                    favoriteChannelIds = favoriteChannelIds,
                    epgData = epgData,
                    playerManager = playerManager,
                    onCategorySelected = { viewModel.selectCategory(it) },
                    onChannelSelected = { viewModel.selectChannel(it); viewModel.play(it) },
                    onToggleFavorite = { viewModel.toggleFavorite(it) },
                    onPlayPause = { viewModel.togglePlayPause() },
                    onFullScreen = {
                        selectedChannel?.let { ch ->
                            currentScreen = AppScreen.FullPlayer(ch)
                        }
                    },
                    onSearchClick = { viewModel.setSearchActive(true) }
                )

                // Back to home button
                BackToHomeButton(
                    onClick = { currentScreen = AppScreen.Home },
                    modifier = Modifier.align(Alignment.TopStart).padding(4.dp)
                )
            }
        }

        is AppScreen.Movies -> {
            Box(modifier = Modifier.fillMaxSize()) {
                VODScreen(
                    categories = vodCategories.filter { it.type == VODCategoryType.MOVIES || it.type == VODCategoryType.TRENDING || it.type == VODCategoryType.POPULAR || it.type == VODCategoryType.NEW_RELEASES || it.type == VODCategoryType.GENRE },
                    onMovieClick = { },
                    onSeriesClick = { },
                    onSearch = { viewModel.setSearchActive(true) }
                )
                BackToHomeButton(
                    onClick = { currentScreen = AppScreen.Home },
                    modifier = Modifier.align(Alignment.TopStart).padding(16.dp)
                )
            }
        }

        is AppScreen.Series -> {
            Box(modifier = Modifier.fillMaxSize()) {
                VODScreen(
                    categories = vodCategories.filter { it.type == VODCategoryType.SERIES || it.type == VODCategoryType.TRENDING || it.type == VODCategoryType.POPULAR || it.type == VODCategoryType.NEW_RELEASES },
                    onMovieClick = { },
                    onSeriesClick = { },
                    onSearch = { viewModel.setSearchActive(true) }
                )
                BackToHomeButton(
                    onClick = { currentScreen = AppScreen.Home },
                    modifier = Modifier.align(Alignment.TopStart).padding(16.dp)
                )
            }
        }

        is AppScreen.EPG -> {
            Box(modifier = Modifier.fillMaxSize()) {
                EPGGrid(
                    channels = channels,
                    epgData = epgData,
                    currentChannelId = selectedChannel?.id,
                    onChannelSelected = { viewModel.selectChannel(it); viewModel.play(it) }
                )
                BackToHomeButton(
                    onClick = { currentScreen = AppScreen.Home },
                    modifier = Modifier.align(Alignment.TopStart).padding(16.dp)
                )
            }
        }

        is AppScreen.Settings -> {
            Box(modifier = Modifier.fillMaxSize()) {
                SettingsScreen(
                    onBack = { currentScreen = AppScreen.Home },
                    onManagePlaylists = { currentScreen = AppScreen.ManagePlaylists },
                    onPlayerSettings = { },
                    onParentalControl = { },
                    onAbout = { }
                )
            }
        }

        is AppScreen.ManagePlaylists -> {
            Box(modifier = Modifier.fillMaxSize()) {
                MultiPlaylistScreen(
                    playlists = playlists,
                    activePlaylistId = "",
                    onActivate = { viewModel.setActivePlaylist(it) },
                    onAdd = {
                        importMode = 0
                        playlistName = ""
                        m3uUrl = ""
                        showAddPlaylistDialog = true
                    },
                    onEdit = { playlist ->
                        // Open add dialog pre-filled with playlist info
                        when (playlist.type) {
                            PlaylistType.M3U_URL -> {
                                importMode = 0
                                m3uUrl = playlist.url
                            }
                            PlaylistType.XTREAM -> {
                                importMode = 1
                                xtreamServer = playlist.serverUrl
                                xtreamUser = playlist.username
                                xtreamPass = playlist.password
                            }
                            else -> { importMode = 0 }
                        }
                        playlistName = playlist.name
                        showAddPlaylistDialog = true
                    },
                    onDelete = { viewModel.removePlaylist(it) },
                    onBack = { currentScreen = AppScreen.Settings }
                )
            }
        }
    }

    // Search overlay on top of everything
    if (isSearchActive) {
        SearchOverlay(
            query = searchQuery,
            onQueryChange = { viewModel.setSearchQuery(it) },
            onClose = { viewModel.setSearchActive(false) },
            channelResults = searchResults,
            vodResults = searchVodResults,
            onChannelClick = {
                viewModel.selectChannel(it)
                viewModel.play(it)
                viewModel.setSearchActive(false)
                currentScreen = AppScreen.LiveTV
            },
            onVodClick = { viewModel.setSearchActive(false) },
            onToggleFavorite = { viewModel.toggleFavorite(it) }
        )
    }

    // Add playlist dialog
    if (showAddPlaylistDialog) {
        AddPlaylistDialog(
            importMode = importMode,
            playlistName = playlistName,
            m3uUrl = m3uUrl,
            xtreamServer = xtreamServer,
            xtreamUser = xtreamUser,
            xtreamPass = xtreamPass,
            onPlaylistNameChange = { playlistName = it },
            onModeChange = { importMode = it },
            onM3uUrlChange = { m3uUrl = it },
            onXtreamServerChange = { xtreamServer = it },
            onXtreamUserChange = { xtreamUser = it },
            onXtreamPassChange = { xtreamPass = it },
            onSubmit = {
                if (importMode == 0 && m3uUrl.isNotBlank()) {
                    viewModel.addPlaylist(
                        name = playlistName.ifBlank { "My M3U Playlist" },
                        type = PlaylistType.M3U_URL,
                        url = m3uUrl
                    )
                    showAddPlaylistDialog = false
                    m3uUrl = ""
                    playlistName = ""
                } else if (importMode == 1 && xtreamServer.isNotBlank() && xtreamUser.isNotBlank()) {
                    viewModel.addPlaylist(
                        name = playlistName.ifBlank { "My Xtream" },
                        type = PlaylistType.XTREAM,
                        serverUrl = xtreamServer,
                        username = xtreamUser,
                        password = xtreamPass
                    )
                    showAddPlaylistDialog = false
                    xtreamServer = ""
                    xtreamUser = ""
                    xtreamPass = ""
                    playlistName = ""
                }
            },
            onDismiss = { showAddPlaylistDialog = false }
        )
    }
}

@Composable
private fun BackToHomeButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(VinColors.Overlay)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Filled.Home, "Home", tint = VinColors.TextPrimary,
            modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun WelcomeScreen(
    onAddM3uUrl: () -> Unit,
    onAddXtream: () -> Unit
) {
    var showAnimation by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { kotlinx.coroutines.delay(100); showAnimation = true }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF080808), Color(0xFF0A0A1A), Color(0xFF080808))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = showAnimation,
            enter = fadeIn(animationSpec = androidx.compose.animation.core.tween(600)) +
                    androidx.compose.animation.slideInVertically(
                        animationSpec = androidx.compose.animation.core.tween(600)
                    ) { it / 4 }
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp)
            ) {
                // Logo
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(VinColors.Accent, VinColors.Accent.copy(alpha = 0.6f))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("V", fontSize = 52.sp,
                        fontWeight = FontWeight.Bold, color = Color.White)
                }

                Spacer(Modifier.height(24.dp))
                Text("Vin IPTV",
                    fontSize = 36.sp, fontWeight = FontWeight.Bold,
                    color = VinColors.TextPrimary, letterSpacing = 1.sp)
                Text("Premium IPTV Experience",
                    fontSize = 15.sp, color = VinColors.TextSecondary)
                Spacer(Modifier.height(6.dp))
                Text("Add your playlist to get started",
                    fontSize = 13.sp, color = VinColors.TextTertiary)

                Spacer(Modifier.height(40.dp))

                // Add M3U button
                Button(
                    onClick = onAddM3uUrl,
                    modifier = Modifier.fillMaxWidth(0.5f).height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VinColors.Accent)
                ) {
                    Icon(Icons.Filled.Link, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Add M3U URL", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }

                Spacer(Modifier.height(12.dp))

                // Xtream button
                OutlinedButton(
                    onClick = onAddXtream,
                    modifier = Modifier.fillMaxWidth(0.5f).height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, VinColors.Accent.copy(alpha = 0.5f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = VinColors.Accent)
                ) {
                    Icon(Icons.Filled.Dns, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Xtream Codes", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }

                Spacer(Modifier.height(24.dp))

                // Help text
                Text("Enter your IPTV provider details",
                    color = VinColors.TextTertiary, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun SearchOverlay(
    query: String,
    onQueryChange: (String) -> Unit,
    onClose: () -> Unit,
    channelResults: List<Channel>,
    vodResults: List<VODItem>,
    onChannelClick: (Channel) -> Unit,
    onVodClick: (VODItem) -> Unit,
    onToggleFavorite: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VinColors.Background.copy(alpha = 0.96f))
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            // Search bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    placeholder = { Text("Search channels, movies, series...", fontSize = 15.sp) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VinColors.Accent,
                        unfocusedBorderColor = VinColors.SeparatorLight,
                        cursorColor = VinColors.Accent,
                        focusedContainerColor = VinColors.Surface,
                        unfocusedContainerColor = VinColors.Surface
                    ),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontSize = 15.sp, color = VinColors.TextPrimary
                    ),
                    leadingIcon = {
                        Icon(Icons.Filled.Search, "Search", tint = VinColors.TextSecondary)
                    },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { onQueryChange("") }) {
                                Icon(Icons.Filled.Clear, "Clear", tint = VinColors.TextSecondary)
                            }
                        }
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
                )

                TextButton(onClick = onClose) {
                    Text("Cancel", color = VinColors.Accent, fontSize = 14.sp)
                }
            }

            Spacer(Modifier.height(16.dp))

            if (query.isBlank()) {
                // Popular searches
                Text("Popular Channels", color = VinColors.TextSecondary,
                    fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(8.dp))
                val tags = listOf("News", "Sports", "Movies", "Kids", "Music", "Documentary")
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        tags.take(3).forEach { tag ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(VinColors.SurfaceLight)
                                    .clickable { onQueryChange(tag) }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(tag, color = VinColors.TextSecondary, fontSize = 13.sp)
                            }
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        tags.drop(3).forEach { tag ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(VinColors.SurfaceLight)
                                    .clickable { onQueryChange(tag) }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(tag, color = VinColors.TextSecondary, fontSize = 13.sp)
                            }
                        }
                    }
                }
            } else {
                // Results
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (channelResults.isNotEmpty()) {
                        item {
                            Text("Channels", color = VinColors.Accent,
                                fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(4.dp))
                        }
                        items(channelResults) { channel ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onChannelClick(channel) }
                                    .padding(horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(channel.displayNumber, color = VinColors.TextTertiary,
                                    fontSize = 11.sp, modifier = Modifier.width(24.dp))
                                Text(channel.name, color = VinColors.TextPrimary,
                                    fontSize = 13.sp, fontWeight = FontWeight.Medium,
                                    modifier = Modifier.weight(1f))
                                Text(channel.category, color = VinColors.TextTertiary,
                                    fontSize = 11.sp)
                            }
                        }
                    }

                    if (vodResults.isNotEmpty()) {
                        item {
                            Spacer(Modifier.height(12.dp))
                            Text("Movies & Series", color = VinColors.Accent,
                                fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(4.dp))
                        }
                        items(vodResults) { vod ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onVodClick(vod) }
                                    .padding(horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(VinColors.SurfaceCard)
                                ) {
                                    if (vod.posterUrl.isNotBlank()) {
                                        coil.compose.AsyncImage(
                                            model = vod.posterUrl,
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                        )
                                    }
                                }
                                Spacer(Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(vod.title, color = VinColors.TextPrimary,
                                        fontSize = 13.sp, fontWeight = FontWeight.Medium,
                                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(
                                        if (vod.isMovie) "Movie • ${vod.year}" else "Series • ${vod.year}",
                                        color = VinColors.TextTertiary, fontSize = 11.sp)
                                }
                                if (vod.rating > 0) {
                                    Text("★ ${vod.rating}", color = VinColors.StarRating,
                                        fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    if (channelResults.isEmpty() && vodResults.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Filled.SearchOff, null,
                                        tint = VinColors.TextTertiary, modifier = Modifier.size(40.dp))
                                    Spacer(Modifier.height(8.dp))
                                    Text("No results found", color = VinColors.TextTertiary,
                                        fontSize = 15.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AddPlaylistDialog(
    importMode: Int,
    playlistName: String,
    m3uUrl: String,
    xtreamServer: String,
    xtreamUser: String,
    xtreamPass: String,
    onPlaylistNameChange: (String) -> Unit,
    onModeChange: (Int) -> Unit,
    onM3uUrlChange: (String) -> Unit,
    onXtreamServerChange: (String) -> Unit,
    onXtreamUserChange: (String) -> Unit,
    onXtreamPassChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = VinColors.Surface,
        titleContentColor = VinColors.TextPrimary,
        textContentColor = VinColors.TextSecondary,
        shape = RoundedCornerShape(16.dp),
        title = {
            Text("Add Playlist", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Mode tabs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("M3U URL", "Xtream Codes").forEachIndexed { i, label ->
                        val selected = importMode == i
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (selected) VinColors.Accent else VinColors.SurfaceLight)
                                .clickable { onModeChange(i) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(label,
                                color = if (selected) Color.White else VinColors.TextSecondary,
                                fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                // Playlist name
                OutlinedTextField(
                    value = playlistName,
                    onValueChange = onPlaylistNameChange,
                    label = { Text("Playlist Name (optional)") },
                    placeholder = { Text("My IPTV") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VinColors.Accent,
                        unfocusedBorderColor = VinColors.SeparatorLight,
                        cursorColor = VinColors.Accent,
                        focusedContainerColor = VinColors.Background,
                        unfocusedContainerColor = VinColors.Background,
                        focusedLabelColor = VinColors.Accent,
                        unfocusedLabelColor = VinColors.TextTertiary
                    )
                )

                if (importMode == 0) {
                    OutlinedTextField(
                        value = m3uUrl,
                        onValueChange = onM3uUrlChange,
                        label = { Text("M3U URL") },
                        placeholder = { Text("https://example.com/playlist.m3u") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VinColors.Accent,
                            unfocusedBorderColor = VinColors.SeparatorLight,
                            cursorColor = VinColors.Accent,
                            focusedContainerColor = VinColors.Background,
                            unfocusedContainerColor = VinColors.Background,
                            focusedLabelColor = VinColors.Accent,
                            unfocusedLabelColor = VinColors.TextTertiary
                        )
                    )
                } else {
                    OutlinedTextField(
                        value = xtreamServer,
                        onValueChange = onXtreamServerChange,
                        label = { Text("Server URL") },
                        placeholder = { Text("http://your-server.com:8080") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VinColors.Accent,
                            unfocusedBorderColor = VinColors.SeparatorLight,
                            cursorColor = VinColors.Accent,
                            focusedContainerColor = VinColors.Background,
                            unfocusedContainerColor = VinColors.Background,
                            focusedLabelColor = VinColors.Accent,
                            unfocusedLabelColor = VinColors.TextTertiary
                        )
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = xtreamUser,
                            onValueChange = onXtreamUserChange,
                            label = { Text("Username") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = VinColors.Accent,
                                unfocusedBorderColor = VinColors.SeparatorLight,
                                cursorColor = VinColors.Accent,
                                focusedContainerColor = VinColors.Background,
                                unfocusedContainerColor = VinColors.Background,
                                focusedLabelColor = VinColors.Accent,
                                unfocusedLabelColor = VinColors.TextTertiary
                            )
                        )
                        OutlinedTextField(
                            value = xtreamPass,
                            onValueChange = onXtreamPassChange,
                            label = { Text("Password") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = VinColors.Accent,
                                unfocusedBorderColor = VinColors.SeparatorLight,
                                cursorColor = VinColors.Accent,
                                focusedContainerColor = VinColors.Background,
                                unfocusedContainerColor = VinColors.Background,
                                focusedLabelColor = VinColors.Accent,
                                unfocusedLabelColor = VinColors.TextTertiary
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onSubmit,
                colors = ButtonDefaults.buttonColors(containerColor = VinColors.Accent),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Connect", fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = VinColors.TextSecondary)
            }
        }
    )
}
