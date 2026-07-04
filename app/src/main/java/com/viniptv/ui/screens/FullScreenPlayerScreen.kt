package com.viniptv.ui.screens

import android.view.View
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.viniptv.data.model.Channel
import com.viniptv.data.model.EPGProgram
import com.viniptv.player.PlayerManager
import com.viniptv.ui.theme.VinColors
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun FullScreenPlayerScreen(
    channel: Channel,
    isPlaying: Boolean,
    playerManager: PlayerManager?,
    channels: List<Channel>,
    favoriteChannelIds: Set<String>,
    epgData: List<EPGProgram>,
    onPlayPause: () -> Unit,
    onClose: () -> Unit,
    onChannelSelected: (Channel) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onToggleChannelBar: () -> Unit,
    showChannelBar: Boolean,
    modifier: Modifier = Modifier
) {
    var controlsVisible by remember { mutableStateOf(true) }
    var volume by remember { mutableFloatStateOf(0.8f) }

    // Auto-hide controls
    LaunchedEffect(Unit) {
        while (true) {
            if (controlsVisible) { delay(4000); controlsVisible = false }
            delay(1000)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                indication = null
            ) { controlsVisible = !controlsVisible }
    ) {
        // === PLAYER SURFACE ===
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                val pv = androidx.media3.ui.PlayerView(ctx).apply {
                    setBackgroundColor(android.graphics.Color.BLACK)
                    useController = false
                    player = playerManager?.player  // KEY: set the player
                }
                pv
            },
            update = { pv ->
                pv.player = playerManager?.player  // KEY: update on recomposition
            }
        )

        // === CHANNEL BAR OVERLAY ===
        AnimatedVisibility(
            visible = showChannelBar,
            enter = slideInHorizontally { -it },
            exit = slideOutHorizontally { -it }
        ) {
            Box(
                modifier = Modifier
                    .width(300.dp).fillMaxHeight()
                    .background(VinColors.Background.copy(alpha = 0.92f))
            ) {
                ChannelOverlayList(
                    channels = channels,
                    selectedChannelId = channel.id,
                    favoriteChannelIds = favoriteChannelIds,
                    onChannelSelected = { onChannelSelected(it); onToggleChannelBar() },
                    onToggleFavorite = onToggleFavorite,
                    onClose = onToggleChannelBar
                )
            }
        }

        // === CONTROLS OVERLAY ===
        AnimatedVisibility(
            visible = controlsVisible && !showChannelBar,
            enter = fadeIn(), exit = fadeOut()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Top bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth().align(Alignment.TopCenter)
                        .background(Brush.verticalGradient(
                            listOf(Color.Black.copy(alpha = 0.8f), Color.Transparent)))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(36.dp).clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.1f))
                            .clickable(onClick = onClose),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.ArrowBack, "Back",
                            tint = Color.White, modifier = Modifier.size(22.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(Color(0xFFFF3B30)))
                            Text("LIVE", color = Color(0xFFFF3B30), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(channel.name, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Box(
                        modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.1f))
                            .clickable(onClick = onToggleChannelBar),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.List, "Channels",
                            tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }

                // Center play controls
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalArrangement = Arrangement.spacedBy(28.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { }, modifier = Modifier.size(44.dp)) {
                        Icon(Icons.Filled.SkipPrevious, "Previous",
                            tint = Color.White, modifier = Modifier.size(30.dp))
                    }
                    IconButton(onClick = { }, modifier = Modifier.size(44.dp)) {
                        Icon(Icons.Filled.Replay10, "Back",
                            tint = Color.White, modifier = Modifier.size(26.dp))
                    }
                    IconButton(
                        onClick = onPlayPause,
                        modifier = Modifier.size(72.dp).clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f))
                    ) {
                        Icon(
                            if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            "Play/Pause", tint = Color.White, modifier = Modifier.size(40.dp)
                        )
                    }
                    IconButton(onClick = { }, modifier = Modifier.size(44.dp)) {
                        Icon(Icons.Filled.Forward30, "Forward",
                            tint = Color.White, modifier = Modifier.size(26.dp))
                    }
                    IconButton(onClick = { }, modifier = Modifier.size(44.dp)) {
                        Icon(Icons.Filled.SkipNext, "Next",
                            tint = Color.White, modifier = Modifier.size(30.dp))
                    }
                }

                // Bottom bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth().align(Alignment.BottomCenter)
                        .background(Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Volume
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.VolumeUp, "Volume",
                            tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Slider(
                            value = volume,
                            onValueChange = { volume = it },
                            modifier = Modifier.width(100.dp),
                            colors = SliderDefaults.colors(
                                thumbColor = Color.White, activeTrackColor = VinColors.Accent,
                                inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                            )
                        )
                    }

                    // EPG
                    val currentProg = epgData.firstOrNull { it.isCurrentlyPlaying }
                    if (currentProg != null) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(currentProg.title, color = Color.White, fontSize = 13.sp,
                                maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(formatTimeRange(currentProg.startTime, currentProg.endTime),
                                color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                        }
                    }

                    // Right actions
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier.size(36.dp).clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.1f))
                                .clickable { onToggleFavorite(channel.id) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                if (channel.id in favoriteChannelIds) Icons.Filled.Star else Icons.Filled.StarOutline,
                                "Favorite",
                                tint = if (channel.id in favoriteChannelIds) VinColors.Favorite
                                else Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChannelOverlayList(
    channels: List<Channel>,
    selectedChannelId: String,
    favoriteChannelIds: Set<String>,
    onChannelSelected: (Channel) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onClose: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Channels", color = VinColors.TextPrimary,
                fontSize = 16.sp, fontWeight = FontWeight.Bold)
            IconButton(onClick = onClose, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Filled.Close, "Close",
                    tint = VinColors.TextSecondary, modifier = Modifier.size(20.dp))
            }
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(1.dp)) {
            items(channels, key = { it.id }) { ch ->
                Row(
                    modifier = Modifier.fillMaxWidth().height(44.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (ch.id == selectedChannelId) VinColors.AccentDim else VinColors.Background)
                        .clickable { onChannelSelected(ch) }
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(ch.name, color = VinColors.TextPrimary,
                        fontSize = 13.sp, fontWeight = FontWeight.Medium,
                        maxLines = 1, overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f))
                    if (ch.id in favoriteChannelIds) {
                        Icon(Icons.Filled.Star, null, tint = VinColors.Favorite,
                            modifier = Modifier.size(12.dp))
                    }
                }
            }
        }
    }
}

private fun formatTimeRange(start: Long, end: Long): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.US)
    return "${sdf.format(Date(start))}-${sdf.format(Date(end))}"
}
