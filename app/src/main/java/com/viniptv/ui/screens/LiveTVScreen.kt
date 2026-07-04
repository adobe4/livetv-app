package com.viniptv.ui.screens

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.viniptv.data.model.Channel
import com.viniptv.data.model.EPGProgram
import com.viniptv.player.PlayerManager
import com.viniptv.ui.theme.VinColors
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun LiveTVScreen(
    channels: List<Channel>,
    categories: List<String>,
    selectedCategoryIndex: Int,
    selectedChannel: Channel?,
    isPlaying: Boolean,
    favoriteChannelIds: Set<String>,
    epgData: Map<String, List<EPGProgram>>,
    playerManager: PlayerManager?,
    onCategorySelected: (Int) -> Unit,
    onChannelSelected: (Channel) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onPlayPause: () -> Unit,
    onFullScreen: () -> Unit,
    onSearchClick: () -> Unit,
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxSize()
            .background(VinColors.Background)
    ) {
        // === LEFT PANEL: Channel List ===
        ChannelListPanel(
            channels = channels,
            categories = categories,
            selectedCategoryIndex = selectedCategoryIndex,
            selectedChannelId = selectedChannel?.id,
            favoriteChannelIds = favoriteChannelIds,
            onCategorySelected = onCategorySelected,
            onChannelSelected = onChannelSelected,
            onToggleFavorite = onToggleFavorite,
            onSearchClick = onSearchClick,
            modifier = Modifier.fillMaxHeight()
        )

        // === RIGHT PANEL: Player + EPG ===
        RightPanel(
            selectedChannel = selectedChannel,
            isPlaying = isPlaying,
            epgData = if (selectedChannel != null) epgData[selectedChannel.epgChannelId] ?: emptyList() else emptyList(),
            playerManager = playerManager,
            onPlayPause = onPlayPause,
            onFullScreen = onFullScreen,
            modifier = Modifier.fillMaxHeight()
        )
    }
}

@Composable
private fun ChannelListPanel(
    channels: List<Channel>,
    categories: List<String>,
    selectedCategoryIndex: Int,
    selectedChannelId: String?,
    favoriteChannelIds: Set<String>,
    onCategorySelected: (Int) -> Unit,
    onChannelSelected: (Channel) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(300.dp)
            .background(VinColors.Surface)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(VinColors.Background)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Channels",
                color = VinColors.TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = onSearchClick, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Filled.Search, "Search",
                        tint = VinColors.TextSecondary, modifier = Modifier.size(18.dp))
                }
            }
        }

        // Category tabs
        CategoryTabs(
            categories = categories,
            selectedIndex = selectedCategoryIndex,
            onSelected = onCategorySelected
        )

        // Channel count
        Text(
            "${channels.size} channels",
            color = VinColors.TextTertiary,
            fontSize = 11.sp,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )

        // Channel list
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            items(channels, key = { it.id }) { channel ->
                ChannelRow(
                    channel = channel,
                    isSelected = channel.id == selectedChannelId,
                    isFavorite = channel.id in favoriteChannelIds,
                    onClick = { onChannelSelected(channel) },
                    onFavorite = { onToggleFavorite(channel.id) }
                )
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun CategoryTabs(
    categories: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit
) {
    val scrollState = rememberScrollState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        categories.forEachIndexed { index, category ->
            val isSelected = index == selectedIndex
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) VinColors.Accent else VinColors.SurfaceLight)
                    .clickable { onSelected(index) }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    category,
                    color = if (isSelected) Color.White else VinColors.TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun ChannelRow(
    channel: Channel,
    isSelected: Boolean,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onFavorite: () -> Unit
) {
    val bg = if (isSelected) VinColors.AccentDim else VinColors.Background.copy(alpha = 0.5f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Channel number
        Text(
            channel.displayNumber.ifEmpty { "" },
            color = VinColors.TextTertiary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(24.dp)
        )

        // Logo
        if (channel.logoUrl.isNotBlank()) {
            AsyncImage(
                model = channel.logoUrl,
                contentDescription = channel.name,
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(VinColors.SurfaceCard),
                contentScale = ContentScale.Fit
            )
            Spacer(Modifier.width(8.dp))
        }

        // Channel name
        Text(
            channel.name,
            color = if (isSelected) VinColors.Accent else VinColors.TextPrimary,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        // Favorite indicator
        if (isFavorite) {
            Icon(Icons.Filled.Star, "Favorite",
                tint = VinColors.Favorite,
                modifier = Modifier.size(14.dp))
        }

        // Live indicator
        Box(
            modifier = Modifier
                .size(6.dp)
                .padding(start = 4.dp)
                .clip(CircleShape)
                .background(if (isSelected) VinColors.Accent else VinColors.TextTertiary.copy(alpha = 0.3f))
        )
    }
}

@Composable
private fun RightPanel(
    selectedChannel: Channel?,
    isPlaying: Boolean,
    epgData: List<EPGProgram>,
    playerManager: PlayerManager?,
    onPlayPause: () -> Unit,
    onFullScreen: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        // === PLAYER AREA ===
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.65f)
                .clip(RoundedCornerShape(12.dp))
                .background(VinColors.SurfaceCard)
        ) {
            if (selectedChannel != null) {
                // Player Surface using AndroidView
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        val playerView = androidx.media3.ui.PlayerView(ctx)
                        playerView.setBackgroundColor(android.graphics.Color.BLACK)
                        playerView.useController = false
                        if (playerManager != null) {
                            playerView.player = playerManager.player
                        }
                        playerView
                    },
                    update = { view ->
                        if (playerManager != null) {
                            view.player = playerManager.player
                        }
                    }
                )

                // Gradient overlay at bottom
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .height(80.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, VinColors.Overlay)
                            )
                        )
                )

                // Channel info bottom-left
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(VinColors.Live))
                        Text("LIVE", color = VinColors.Live,
                            fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(selectedChannel.name,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold)
                }

                // Controls bottom-right
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ControlButton(if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        "Play/Pause") { onPlayPause() }
                    ControlButton(Icons.Filled.Fullscreen, "Fullscreen") { onFullScreen() }
                }
            } else {
                // No channel selected placeholder
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Filled.LiveTv, null,
                        tint = VinColors.TextTertiary, modifier = Modifier.size(64.dp))
                    Spacer(Modifier.height(12.dp))
                    Text("Select a channel to start watching",
                        color = VinColors.TextSecondary, fontSize = 15.sp)
                    Spacer(Modifier.height(4.dp))
                    Text("← Pick from the channel list",
                        color = VinColors.TextTertiary, fontSize = 13.sp)
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // === EPG NOW INFO ===
        val currentProgram = epgData.firstOrNull { it.isCurrentlyPlaying }

        if (selectedChannel != null && epgData.isNotEmpty()) {
            EPGNowInfo(
                currentProgram = currentProgram,
                nextPrograms = epgData.take(6),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.35f)
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.35f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(VinColors.Surface.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Text("No EPG data available",
                    color = VinColors.TextTertiary, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun ControlButton(icon: androidx.compose.ui.graphics.vector.ImageVector, desc: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.15f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, desc, tint = Color.White, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun EPGNowInfo(
    currentProgram: EPGProgram?,
    nextPrograms: List<EPGProgram>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(VinColors.Surface)
            .padding(12.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Filled.Schedule, null,
                    tint = VinColors.Accent, modifier = Modifier.size(16.dp))
                Text("EPG Guide", color = VinColors.Accent,
                    fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
            Text("Now → Next", color = VinColors.TextTertiary, fontSize = 11.sp)
        }

        Spacer(Modifier.height(8.dp))

        if (currentProgram != null) {
            // Current program with live badge
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(VinColors.AccentDim)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(VinColors.Live)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("LIVE", color = Color.White, fontSize = 9.sp,
                        fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(currentProgram.title, color = VinColors.TextPrimary,
                        fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("${formatTime(currentProgram.startTime)} - ${formatTime(currentProgram.endTime)}",
                        color = VinColors.TextTertiary, fontSize = 11.sp)
                }
                // Progress bar
                val progress = currentProgram.progress
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(VinColors.ProgressBg)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(progress)
                            .clip(RoundedCornerShape(2.dp))
                            .background(VinColors.Accent)
                    )
                }
            }
        }

        if (nextPrograms.isNotEmpty()) {
            Spacer(Modifier.height(6.dp))
            Text("Up Next", color = VinColors.TextTertiary,
                fontSize = 11.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(4.dp))

            nextPrograms.take(4).forEach { program ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(formatTime(program.startTime), color = VinColors.TextSecondary,
                        fontSize = 11.sp, modifier = Modifier.width(48.dp))
                    Text(program.title, color = VinColors.TextSecondary,
                        fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .width(48.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(VinColors.ProgressBg)
                    ) {
                        val p = program.progress
                        if (p > 0 && p < 1) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(p)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(if (program.isCurrentlyPlaying) VinColors.Accent else VinColors.TextTertiary.copy(alpha = 0.3f))
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatTime(millis: Long): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.US)
    return sdf.format(Date(millis))
}
