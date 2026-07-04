package com.viniptv.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.viniptv.ui.theme.VinColors
import kotlinx.coroutines.delay

data class HomeTile(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val gradient: List<Color>,
    val badge: String? = null
)

@Composable
fun HomeScreen(
    playlistName: String,
    channelCount: Int,
    isLoading: Boolean,
    lastRefreshTime: Long,
    onLiveTv: () -> Unit,
    onVod: () -> Unit,
    onSports: () -> Unit,
    onEpg: () -> Unit,
    onSettings: () -> Unit,
    onRefresh: () -> Unit,
    onSearch: () -> Unit,
    onFavorites: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showContent by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(80); showContent = true }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(VinColors.Background)
    ) {
        // === TOP BAR ===
        AnimatedVisibility(
            visible = showContent,
            enter = fadeIn(tween(350)) + slideInVertically(tween(350)) { -it }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Logo + name + status
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Brush.linearGradient(
                                listOf(VinColors.Accent, VinColors.Accent.copy(alpha = 0.5f))
                            )),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("V", color = Color.White,
                            fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text("Vin IPTV", color = VinColors.TextPrimary,
                            fontSize = 17.sp, fontWeight = FontWeight.Bold)
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (channelCount > 0) VinColors.Success
                                        else if (isLoading) VinColors.Accent
                                        else VinColors.Warning
                                    )
                            )
                            Text(
                                when {
                                    isLoading -> "Loading channels..."
                                    channelCount > 0 -> "$channelCount channels"
                                    else -> "No channels"
                                },
                                color = VinColors.TextTertiary, fontSize = 10.sp
                            )
                        }
                    }
                }

                // Actions
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier.size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(VinColors.SurfaceLight)
                            .clickable(onClick = onRefresh),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(15.dp),
                                color = VinColors.Accent, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Filled.Refresh, "Refresh",
                                tint = VinColors.TextSecondary, modifier = Modifier.size(17.dp))
                        }
                    }
                    Box(
                        modifier = Modifier.size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(VinColors.SurfaceLight)
                            .clickable(onClick = onSettings),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Settings, "Settings",
                            tint = VinColors.TextSecondary, modifier = Modifier.size(17.dp))
                    }
                }
            }
        }

        // === CATEGORY CARDS (2x2 Grid) ===
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Row 1
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TileCard(
                    tile = HomeTile("live", "Live TV",
                        if (channelCount > 0) "$channelCount channels" else "Browse channels",
                        Icons.Filled.LiveTv,
                        listOf(Color(0xFF007AFF), Color(0xFF0044AA)),
                        if (channelCount > 0) "$channelCount" else null),
                    delayMs = 100, showContent = showContent,
                    onClick = onLiveTv,
                    modifier = Modifier.weight(1f)
                )
                TileCard(
                    tile = HomeTile("vod", "VOD", "Movies & Series",
                        Icons.Filled.Movie, listOf(Color(0xFFFF2D55), Color(0xFFAA0022))),
                    delayMs = 180, showContent = showContent,
                    onClick = onVod,
                    modifier = Modifier.weight(1f)
                )
            }
            // Row 2
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TileCard(
                    tile = HomeTile("sports", "Sports", "Live events",
                        Icons.Filled.FitnessCenter, listOf(Color(0xFFFF9500), Color(0xFFCC6600))),
                    delayMs = 260, showContent = showContent,
                    onClick = onSports,
                    modifier = Modifier.weight(1f)
                )
                TileCard(
                    tile = HomeTile("epg", "EPG Guide", "TV Schedule",
                        Icons.Filled.Schedule, listOf(Color(0xFF9B59B6), Color(0xFF6C3483))),
                    delayMs = 340, showContent = showContent,
                    onClick = onEpg,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // === BOTTOM STATUS BAR ===
        AnimatedVisibility(
            visible = showContent,
            enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { it }
        ) {
            Column {
                // Refresh hint if no channels
                if (channelCount == 0 && !isLoading) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(VinColors.AccentDim)
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Filled.Info, null,
                                tint = VinColors.Accent, modifier = Modifier.size(14.dp))
                            Text("Could not load channels. Tap Refresh to retry.",
                                color = VinColors.Accent, fontSize = 11.sp)
                        }
                        TextButton(onClick = onRefresh,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)) {
                            Text("Retry", color = VinColors.Accent, fontSize = 11.sp)
                        }
                    }
                }

                // Quick actions
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(VinColors.Surface)
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    QuickActionBtn(Icons.Filled.Star, "Favorites", onFavorites)
                    QuickActionBtn(Icons.Filled.Search, "Search", onSearch)
                    QuickActionBtn(Icons.Filled.Add, "Add Playlist", onSettings)
                    if (lastRefreshTime > 0) {
                        QuickActionBtn(Icons.Filled.History, formatRefreshTime(lastRefreshTime)) { }
                    }
                }
            }
        }
    }
}

@Composable
private fun TileCard(
    tile: HomeTile,
    delayMs: Int,
    showContent: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(showContent) {
        if (showContent) {
            delay(delayMs.toLong())
            visible = true
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(300)) + scaleIn(tween(300), initialScale = 0.9f)
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .clip(RoundedCornerShape(14.dp))
                .background(
                    Brush.linearGradient(
                        tile.gradient,
                        start = androidx.compose.ui.geometry.Offset(0f, 0f),
                        end = androidx.compose.ui.geometry.Offset(400f, 400f)
                    )
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                )
                .padding(18.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(tile.icon, tile.title,
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(26.dp))
                    if (tile.badge != null) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(5.dp))
                                .background(Color.White.copy(alpha = 0.2f))
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(tile.badge, color = Color.White,
                                fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Column {
                    Text(tile.title, color = Color.White,
                        fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    Text(tile.subtitle, color = Color.White.copy(alpha = 0.6f),
                        fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun QuickActionBtn(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(VinColors.Background),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, label, tint = VinColors.Accent, modifier = Modifier.size(18.dp))
        }
        Text(label, color = VinColors.TextTertiary, fontSize = 9.sp)
    }
}

private fun formatRefreshTime(millis: Long): String {
    val diff = System.currentTimeMillis() - millis
    val sec = diff / 1000
    val min = sec / 60
    return when {
        sec < 60 -> "Just now"
        min < 60 -> "${min}m ago"
        else -> "${min / 60}h ago"
    }
}
