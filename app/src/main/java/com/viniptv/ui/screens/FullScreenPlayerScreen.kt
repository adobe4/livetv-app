package com.viniptv.ui.screens

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
import com.viniptv.ui.theme.VinColors
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun FullScreenPlayerScreen(
    channel: Channel,
    isPlaying: Boolean,
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

    // Auto-hide controls after 4s
    LaunchedEffect(Unit) {
        while (true) {
            if (controlsVisible) {
                delay(4000)
                controlsVisible = false
            }
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
        // Player surface
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                val playerView = androidx.media3.ui.PlayerView(ctx)
                playerView.setBackgroundColor(android.graphics.Color.BLACK)
                playerView.useController = false
                playerView
            },
            update = { }
        )

        // Semi-transparent channel bar overlay
        AnimatedVisibility(
            visible = showChannelBar,
            enter = slideInHorizontally { -it },
            exit = slideOutHorizontally { -it }
        ) {
            Box(
                modifier = Modifier
                    .width(300.dp)
                    .fillMaxHeight()
                    .background(VinColors.Background.copy(alpha = 0.92f))
            ) {
                ChannelBarContent(
                    channels = channels,
                    selectedChannelId = channel.id,
                    favoriteChannelIds = favoriteChannelIds,
                    onChannelSelected = {
                        onChannelSelected(it)
                        onToggleChannelBar()
                    },
                    onToggleFavorite = onToggleFavorite,
                    onClose = onToggleChannelBar
                )
            }
        }

        // Controls overlay
        AnimatedVisibility(
            visible = controlsVisible && !showChannelBar,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Top bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Black.copy(alpha = 0.8f), Color.Transparent)
                            )
                        )
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Back button
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.1f))
                            .clickable(onClick = onClose),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.ArrowBack, "Back",
                            tint = Color.White, modifier = Modifier.size(22.dp))
                    }

                    Spacer(Modifier.width(12.dp))

                    // Channel info
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(VinColors.Live))
                            Text("LIVE", color = VinColors.Live,
                                fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(channel.name, color = Color.White,
                                fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Channel list toggle
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.1f))
                            .clickable(onClick = onToggleChannelBar),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.List, "Channels",
                            tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }

                // Center playback controls
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
                        Icon(Icons.Filled.Replay10, "Replay 10s",
                            tint = Color.White, modifier = Modifier.size(26.dp))
                    }
                    IconButton(
                        onClick = onPlayPause,
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f))
                    ) {
                        Icon(
                            if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            "Play/Pause",
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    IconButton(onClick = { }, modifier = Modifier.size(44.dp)) {
                        Icon(Icons.Filled.Forward30, "Forward 30s",
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
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                            )
                        )
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
                            modifier = Modifier.width(120.dp),
                            colors = SliderDefaults.colors(
                                thumbColor = Color.White,
                                activeTrackColor = VinColors.Accent,
                                inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                            )
                        )
                    }

                    // EPG info center
                    val currentProgram = epgData.firstOrNull { it.isCurrentlyPlaying }
                    if (currentProgram != null) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(currentProgram.title, color = Color.White,
                                fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text("${formatTime(currentProgram.startTime)} - ${formatTime(currentProgram.endTime)}",
                                color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                        }
                    }

                    // Right controls
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.1f))
                                .clickable { onToggleFavorite(channel.id) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                if (channel.id in favoriteChannelIds) Icons.Filled.Star
                                else Icons.Filled.StarOutline,
                                "Favorite",
                                tint = if (channel.id in favoriteChannelIds) VinColors.Favorite
                                else Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Cast, "Cast",
                                tint = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChannelBarContent(
    channels: List<Channel>,
    selectedChannelId: String,
    favoriteChannelIds: Set<String>,
    onChannelSelected: (Channel) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
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

        // Search field
        OutlinedTextField(
            value = "",
            onValueChange = { },
            placeholder = { Text("Search channels...", fontSize = 13.sp) },
            modifier = Modifier.fillMaxWidth().height(40.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = VinColors.Accent,
                unfocusedBorderColor = VinColors.Separator,
                cursorColor = VinColors.Accent,
                focusedContainerColor = VinColors.SurfaceLight,
                unfocusedContainerColor = VinColors.SurfaceLight
            ),
            textStyle = androidx.compose.ui.text.TextStyle(
                fontSize = 13.sp, color = VinColors.TextPrimary),
            leadingIcon = {
                Icon(Icons.Filled.Search, "Search",
                    tint = VinColors.TextTertiary, modifier = Modifier.size(18.dp))
            }
        )

        Spacer(Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            items(channels, key = { it.id }) { channel ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (channel.id == selectedChannelId) VinColors.AccentDim
                            else VinColors.Background
                        )
                        .clickable { onChannelSelected(channel) }
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(channel.displayNumber, color = VinColors.TextTertiary,
                        fontSize = 11.sp, modifier = Modifier.width(24.dp))
                    if (channel.logoUrl.isNotBlank()) {
                        AsyncImage(
                            model = channel.logoUrl,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp).clip(RoundedCornerShape(4.dp)),
                            contentScale = ContentScale.Fit
                        )
                        Spacer(Modifier.width(6.dp))
                    }
                    Text(channel.name, color = VinColors.TextPrimary,
                        fontSize = 12.sp, fontWeight = FontWeight.Medium,
                        maxLines = 1, overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f))
                    if (channel.id in favoriteChannelIds) {
                        Icon(Icons.Filled.Star, null, tint = VinColors.Favorite,
                            modifier = Modifier.size(12.dp))
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
