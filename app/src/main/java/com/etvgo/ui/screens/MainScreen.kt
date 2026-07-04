package com.etvgo.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import com.etvgo.data.model.Category
import com.etvgo.player.ETVPlayerView
import com.etvgo.data.model.Channel
import com.etvgo.data.model.EPGProgram
import com.etvgo.ui.components.*
import com.etvgo.ui.theme.*
import com.etvgo.viewmodel.MainUiState
import com.etvgo.viewmodel.MainViewModel

@Composable
fun MainScreen(
    viewModel: MainViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    var showSettings by remember { mutableStateOf(false) }
    var searchVisible by remember { mutableStateOf(false) }
    var showChannelPicker by remember { mutableStateOf(false) }
    var isFullscreen by remember { mutableStateOf(false) }

    // File picker for M3U upload
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.loadM3uFile(context, it) }
    }

    Box(modifier = Modifier.fillMaxSize().background(DarkBackground)) {
        if (state.channels.isEmpty() && !state.isLoading) {
            // Empty state - welcome screen
            WelcomeScreen(
                onAddM3uUrl = { showSettings = true },
                onFileUpload = { filePickerLauncher.launch("*/*") }
            )
        } else {
            // Main two-panel layout
            if (isFullscreen) {
                // Fullscreen player
                FullscreenPlayer(
                    viewModel = viewModel,
                    state = state,
                    onExitFullscreen = { isFullscreen = false },
                    onTogglePicker = { showChannelPicker = !showChannelPicker }
                )
            } else {
                // Two-panel layout
                Column(modifier = Modifier.fillMaxSize()) {
                    // Top bar
                    TopBar(
                        searchVisible = searchVisible,
                        appName = "ETV Go",
                        channelCount = state.filteredChannels.size,
                        onSearchToggle = { searchVisible = !searchVisible },
                        onSettingsClick = { showSettings = true },
                        onFullscreenClick = { isFullscreen = true }
                    )

                    // Search
                    SearchBar(
                        query = state.searchQuery,
                        onQueryChange = { viewModel.search(it) },
                        onClose = { searchVisible = false; viewModel.search("") },
                        isVisible = searchVisible
                    )

                    // Categories
                    CategoryBar(
                        categories = state.categories,
                        selectedCategory = state.selectedCategory,
                        onCategorySelected = { viewModel.selectCategory(it) }
                    )

                    // Main content: Channels (left) + Player (right)
                    Row(modifier = Modifier.fillMaxSize().weight(1f)) {
                        // Left panel - Channel list
                        ChannelListPanel(
                            channels = state.filteredChannels,
                            currentChannelId = state.currentChannel?.id,
                            isLoading = state.isLoading,
                            onChannelSelected = { viewModel.playChannel(it) },
                            onFavoriteToggle = { viewModel.toggleFavorite(it) },
                            modifier = Modifier.width(340.dp)
                        )

                        // Divider
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .fillMaxHeight()
                                .background(Separator)
                        )

                        // Right panel - Player + EPG
                        RightPanel(
                            currentChannel = state.currentChannel,
                            isPlaying = state.isPlaying,
                            onPlayPause = { viewModel.togglePlayPause() },
                            onNext = { viewModel.nextChannel() },
                            onPrev = { viewModel.prevChannel() },
                            programs = state.epgPrograms,
                            onFullscreenClick = { isFullscreen = true },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Settings dialog
        SettingsDialog(
            isVisible = showSettings,
            onDismiss = { showSettings = false },
            onM3uUrlSubmit = { url ->
                viewModel.loadM3uUrl(url)
                showSettings = false
            },
            onXtreamSubmit = { server, user, pass ->
                viewModel.connectXtream(server, user, pass)
                showSettings = false
            },
            onFileUpload = { filePickerLauncher.launch("*/*") }
        )

        // Loading overlay
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = AccentBlue)
                    Spacer(Modifier.height(12.dp))
                    Text(state.loadingMessage, color = TextSecondary, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
private fun WelcomeScreen(
    onAddM3uUrl: () -> Unit,
    onFileUpload: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Logo
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(AccentBlueDim),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.LiveTv,
                    contentDescription = null,
                    tint = AccentBlue,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(Modifier.height(20.dp))
            Text("ETV Go", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(Modifier.height(8.dp))
            Text("Premium IPTV Player", fontSize = 15.sp, color = TextSecondary)

            Spacer(Modifier.height(40.dp))

            // Buttons
            Button(
                onClick = onAddM3uUrl,
                modifier = Modifier.width(300.dp).height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Filled.Link, "URL", modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Add M3U URL", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(12.dp))

            OutlinedButton(
                onClick = onFileUpload,
                modifier = Modifier.width(300.dp).height(52.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Separator)
            ) {
                Icon(Icons.Filled.UploadFile, "Upload", tint = TextSecondary)
                Spacer(Modifier.width(8.dp))
                Text("Upload M3U File", color = TextSecondary, fontSize = 16.sp)
            }

            Spacer(Modifier.height(12.dp))

            OutlinedButton(
                onClick = onAddM3uUrl,
                modifier = Modifier.width(300.dp).height(52.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Separator)
            ) {
                Icon(Icons.Filled.Api, "Xtream", tint = TextSecondary)
                Spacer(Modifier.width(8.dp))
                Text("Xtream Codes", color = TextSecondary, fontSize = 16.sp)
            }
        }
    }
}

@Composable
private fun TopBar(
    searchVisible: Boolean,
    appName: String,
    channelCount: Int,
    onSearchToggle: () -> Unit,
    onSettingsClick: () -> Unit,
    onFullscreenClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkBackground)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = appName,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = "$channelCount channels",
            color = TextTertiary,
            fontSize = 13.sp
        )

        Spacer(Modifier.width(12.dp))

        // Search button
        val searchIcon = if (searchVisible) Icons.Filled.SearchOff else Icons.Filled.Search
        IconButton(onClick = onSearchToggle) {
            Icon(searchIcon, "Search", tint = TextSecondary)
        }

        // Fullscreen
        IconButton(onClick = onFullscreenClick) {
            Icon(Icons.Filled.Fullscreen, "Fullscreen", tint = TextSecondary)
        }

        // Settings
        IconButton(onClick = onSettingsClick) {
            Icon(Icons.Filled.Add, "Add Playlist", tint = TextSecondary)
        }
    }
}

@Composable
private fun ChannelListPanel(
    channels: List<Channel>,
    currentChannelId: String?,
    isLoading: Boolean,
    onChannelSelected: (Channel) -> Unit,
    onFavoriteToggle: (Channel) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxHeight().background(DarkBackground)) {
        // Section header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurface)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Channels", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
            Text("${channels.size}", fontSize = 12.sp, color = TextTertiary)
        }

        if (channels.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (isLoading) "Loading..." else "No channels found",
                    color = TextTertiary,
                    fontSize = 14.sp
                )
            }
        } else {
            ChannelList(
                channels = channels,
                currentChannelId = currentChannelId,
                onChannelSelected = onChannelSelected,
                onFavoriteToggle = onFavoriteToggle
            )
        }
    }
}

@Composable
private fun RightPanel(
    currentChannel: Channel?,
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    programs: List<EPGProgram>,
    onFullscreenClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize().background(DarkBackground)) {
        // Player area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color.Black)
        ) {
            if (currentChannel != null) {
                // The ExoPlayer composable will be placed here natively
                // For now, show the player info overlay
                AndroidView<android.view.View>(
                    factory = { ctx ->
                        com.etvgo.player.ETVPlayerView(ctx).apply {
                            layoutParams = android.view.ViewGroup.LayoutParams(
                                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                                android.view.ViewGroup.LayoutParams.MATCH_PARENT
                            )
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // No channel selected
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Filled.LiveTv,
                            contentDescription = null,
                            tint = TextTertiary,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text("Select a channel to start watching", color = TextTertiary, fontSize = 15.sp)
                    }
                }
            }

            // Player overlay controls
            if (currentChannel != null) {
                PlayerOverlayControls(
                    channel = currentChannel,
                    isPlaying = isPlaying,
                    onPlayPause = onPlayPause,
                    onNext = onNext,
                    onPrev = onPrev,
                    onFullscreen = onFullscreenClick
                )
            }
        }

        // EPG section
        EPGView(programs = programs)
    }
}

@Composable
private fun PlayerOverlayControls(
    channel: Channel,
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onFullscreen: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.3f))
            .padding(16.dp)
    ) {
        // Top info
        Row(
            modifier = Modifier.fillMaxWidth().align(Alignment.TopStart),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .width(6.dp).height(6.dp)
                            .clip(CircleShape)
                            .background(LiveRed)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("LIVE", color = LiveRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(2.dp))
                Text(channel.name, color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Center controls
        Row(
            modifier = Modifier.align(Alignment.Center),
            horizontalArrangement = Arrangement.spacedBy(32.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPrev, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Filled.SkipPrevious, "Previous", tint = Color.White, modifier = Modifier.size(32.dp))
            }
            IconButton(
                onClick = onPlayPause,
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f))
            ) {
                Icon(
                    if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    "Play/Pause",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }
            IconButton(onClick = onNext, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Filled.SkipNext, "Next", tint = Color.White, modifier = Modifier.size(32.dp))
            }
        }
    }
}

@Composable
private fun FullscreenPlayer(
    viewModel: MainViewModel,
    state: com.etvgo.viewmodel.MainUiState,
    onExitFullscreen: () -> Unit,
    onTogglePicker: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        // Fullscreen player view
        AndroidView(
            factory = { ctx ->
                com.etvgo.player.ETVPlayerView(ctx).apply {
                    layoutParams = android.view.ViewGroup.LayoutParams(
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Overlay controls
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f))
                .padding(16.dp)
        ) {
            // Top bar
            Row(
                modifier = Modifier.fillMaxWidth().align(Alignment.TopStart),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onExitFullscreen) {
                    Icon(Icons.Filled.ArrowBack, "Exit", tint = Color.White)
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    state.currentChannel?.name ?: "",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onTogglePicker) {
                    Icon(Icons.Filled.List, "Channels", tint = Color.White)
                }
            }

            // Center controls
            Row(
                modifier = Modifier.align(Alignment.Center),
                horizontalArrangement = Arrangement.spacedBy(40.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.prevChannel() }, modifier = Modifier.size(56.dp)) {
                    Icon(Icons.Filled.SkipPrevious, "Prev", tint = Color.White, modifier = Modifier.size(40.dp))
                }
                IconButton(
                    onClick = { viewModel.togglePlayPause() },
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f))
                ) {
                    Icon(
                        if (state.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        "Play/Pause",
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }
                IconButton(onClick = { viewModel.nextChannel() }, modifier = Modifier.size(56.dp)) {
                    Icon(Icons.Filled.SkipNext, "Next", tint = Color.White, modifier = Modifier.size(40.dp))
                }
            }

            // Channel info bottom
            if (state.currentChannel != null) {
                Column(
                    modifier = Modifier.align(Alignment.BottomStart).padding(24.dp)
                ) {
                    Text(state.currentChannel.name, color = Color.White, fontSize = 16.sp)
                    Text(state.currentChannel.category, color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp)
                }
            }
        }

        // Channel picker overlay
        ChannelPickerOverlay(
            channels = state.filteredChannels,
            currentChannelId = state.currentChannel?.id,
            isVisible = state.showPicker,
            onChannelSelected = { viewModel.playChannel(it); viewModel.togglePicker() },
            onDismiss = { viewModel.togglePicker() },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
