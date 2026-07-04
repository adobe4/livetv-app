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
    val color: Color,
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
    modifier: Modifier = Modifier
) {
    var showContent by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(50)
        showContent = true
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(VinColors.Background)
    ) {
        // ===== TOP BAR =====
        AnimatedVisibility(
            visible = showContent,
            enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { -it / 2 }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Logo + title
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(VinColors.Accent, VinColors.Accent.copy(alpha = 0.5f))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("V", color = Color.White,
                            fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text("Vin IPTV",
                            color = VinColors.TextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold)
                        if (channelCount > 0 && !isLoading) {
                            Text("$channelCount channels • $playlistName",
                                color = VinColors.TextTertiary, fontSize = 10.sp,
                                maxLines = 1, overflow = TextOverflow.Ellipsis)
                        } else if (isLoading) {
                            Text("Loading...",
                                color = VinColors.Accent, fontSize = 10.sp)
                        } else if (channelCount == 0) {
                            Text("Tap Refresh to load channels",
                                color = VinColors.Warning, fontSize = 10.sp)
                        }
                    }
                }

                // Action buttons
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Refresh
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .background(VinColors.SurfaceLight)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onRefresh
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = VinColors.Accent, strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Filled.Refresh, "Refresh",
                                tint = VinColors.TextSecondary, modifier = Modifier.size(18.dp))
                        }
                    }
                    // Settings
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .background(VinColors.SurfaceLight)
                            .clickable(onClick = onSettings),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Settings, "Settings",
                            tint = VinColors.TextSecondary, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        // ===== CATEGORY CARDS =====
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Row 1: Live TV + VOD
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AnimatedCard(
                    tile = HomeTile("live", "Live TV",
                        if (channelCount > 0) "$channelCount channels" else "Browse channels",
                        Icons.Filled.LiveTv, Color(0xFF007AFF),
                        if (channelCount > 0) "$channelCount" else null),
                    delayMs = 100,
                    onClick = onLiveTv,
                    modifier = Modifier.weight(1f)
                )
                AnimatedCard(
                    tile = HomeTile("vod", "VOD",
                        "Movies & Series", Icons.Filled.Movie, Color(0xFFFF2D55),
                        null),
                    delayMs = 200,
                    onClick = onVod,
                    modifier = Modifier.weight(1f)
                )
            }
            // Row 2: Sports + EPG
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AnimatedCard(
                    tile = HomeTile("sports", "Sports",
                        "Live sports", Icons.Filled.FitnessCenter, Color(0xFFFF9500)),
                    delayMs = 300,
                    onClick = onSports,
                    modifier = Modifier.weight(1f)
                )
                AnimatedCard(
                    tile = HomeTile("epg", "EPG",
                        "TV Guide", Icons.Filled.Schedule, Color(0xFF9B59B6)),
                    delayMs = 400,
                    onClick = onEpg,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // ===== BOTTOM STATUS BAR =====
        AnimatedVisibility(
            visible = showContent,
            enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { it / 2 }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(VinColors.Surface)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Status
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(
                                if (channelCount > 0) VinColors.Success
                                else VinColors.Warning
                            )
                    )
                    Text(
                        if (channelCount > 0) "Connected"
                        else if (isLoading) "Loading..."
                        else "No channels",
                        color = VinColors.TextSecondary, fontSize = 11.sp
                    )
                    if (lastRefreshTime > 0) {
                        Text("•", color = VinColors.TextTertiary, fontSize = 11.sp)
                        Text(formatRefreshTime(lastRefreshTime),
                            color = VinColors.TextTertiary, fontSize = 10.sp)
                    }
                }

                // Add playlist button (if no channels)
                if (channelCount == 0 && !isLoading) {
                    TextButton(
                        onClick = onSettings,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Icon(Icons.Filled.Add, null, modifier = Modifier.size(14.dp),
                            tint = VinColors.Accent)
                        Spacer(Modifier.width(4.dp))
                        Text("Manage Playlists", color = VinColors.Accent, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun AnimatedCard(
    tile: HomeTile,
    delayMs: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(delayMs.toLong())
        visible = true
    }

    val scale = 1f

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(400)) +
                scaleIn(initialScale = 0.85f, animationSpec = tween(400, delayMillis = delayMs))
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .clip(RoundedCornerShape(14.dp))
                .background(
                    Brush.linearGradient(
                        listOf(tile.color, tile.color.copy(alpha = 0.5f)),
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
                // Icon row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(tile.icon, tile.title,
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(28.dp))
                    if (tile.badge != null) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(5.dp))
                                .background(Color.White.copy(alpha = 0.25f))
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(tile.badge, color = Color.White,
                                fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                // Title
                Column {
                    Text(tile.title, color = Color.White,
                        fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text(tile.subtitle, color = Color.White.copy(alpha = 0.65f),
                        fontSize = 11.sp)
                }
            }
        }
    }
}



private fun formatRefreshTime(millis: Long): String {
    val diff = System.currentTimeMillis() - millis
    val seconds = diff / 1000
    val minutes = seconds / 60
    return when {
        seconds < 60 -> "Just now"
        minutes < 60 -> "${minutes}m ago"
        else -> "${minutes / 60}h ago"
    }
}
