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
    var loaded by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(60); loaded = true }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(VinColors.Background)
    ) {
        // ===== HEADER =====
        AnimatedVisibility(
            visible = loaded,
            enter = fadeIn(tween(300)) + slideInVertically(tween(300)) { -it }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // App icon + name
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
                                Brush.linearGradient(listOf(VinColors.Accent, Color(0xFF0044AA)))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("V", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text("Vin IPTV", color = VinColors.TextPrimary,
                            fontSize = 17.sp, fontWeight = FontWeight.Bold)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier.size(5.dp).clip(CircleShape)
                                    .background(
                                        when { isLoading -> VinColors.Accent
                                            channelCount > 0 -> VinColors.Success
                                            else -> VinColors.Warning }
                                    )
                            )
                            Text(
                                when {
                                    isLoading -> "Loading..."
                                    channelCount > 0 -> "${channelCount}ch • $playlistName"
                                    else -> "No channels loaded"
                                },
                                color = VinColors.TextTertiary, fontSize = 10.sp,
                                maxLines = 1, overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Actions
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Refresh
                    Box(
                        modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp))
                            .background(VinColors.SurfaceLight)
                            .clickable(onClick = onRefresh),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(14.dp),
                                color = VinColors.Accent, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Filled.Refresh, "Refresh",
                                tint = VinColors.TextSecondary, modifier = Modifier.size(16.dp))
                        }
                    }
                    // Settings
                    Box(
                        modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp))
                            .background(VinColors.SurfaceLight)
                            .clickable(onClick = onSettings),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Settings, "Settings",
                            tint = VinColors.TextSecondary, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        // ===== CATEGORY CARDS =====
        Box(
            modifier = Modifier.fillMaxWidth().weight(1f)
        ) {
            if (channelCount == 0 && !isLoading) {
                // Empty state
                Column(
                    modifier = Modifier.fillMaxSize().padding(20.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Filled.WifiOff, null,
                        tint = VinColors.TextTertiary, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(12.dp))
                    Text("Could not load channels", color = VinColors.TextSecondary, fontSize = 17.sp)
                    Text("Check your playlist URL and tap Refresh",
                        color = VinColors.TextTertiary, fontSize = 13.sp)
                    Spacer(Modifier.height(20.dp))
                    Button(
                        onClick = onRefresh,
                        colors = ButtonDefaults.buttonColors(containerColor = VinColors.Accent),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.Refresh, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Retry", fontWeight = FontWeight.SemiBold)
                    }
                }
            } else {
                // Cards grid
                Column(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Row 1
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        TileCard(
                            "live", "Live TV",
                            if (channelCount > 0) "$channelCount channels" else "Watch now",
                            Icons.Filled.LiveTv,
                            listOf(Color(0xFF007AFF), Color(0xFF0044AA)),
                            if (channelCount > 0) "$channelCount" else null,
                            delayMs = 100, loaded = loaded,
                            onClick = onLiveTv,
                            modifier = Modifier.weight(1f)
                        )
                        TileCard(
                            "vod", "Movies & Series",
                            "On-demand content", Icons.Filled.PlayCircle,
                            listOf(Color(0xFFFF2D55), Color(0xFFAA0022)),
                            null,
                            delayMs = 180, loaded = loaded,
                            onClick = onVod,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    // Row 2
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        TileCard(
                            "sports", "Sports",
                            "Live events", Icons.Filled.EmojiEvents,
                            listOf(Color(0xFFFF9500), Color(0xFFCC6600)),
                            null,
                            delayMs = 260, loaded = loaded,
                            onClick = onSports,
                            modifier = Modifier.weight(1f)
                        )
                        TileCard(
                            "epg", "TV Guide",
                            "Program schedule", Icons.Filled.DateRange,
                            listOf(Color(0xFF34C759), Color(0xFF009933)),
                            null,
                            delayMs = 340, loaded = loaded,
                            onClick = onEpg,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // ===== DOCK (bottom bar) =====
        AnimatedVisibility(
            visible = loaded,
            enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { it }
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = VinColors.Surface,
                shadowElevation = 12.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DockItem(Icons.Filled.Star, "Favorites", onFavorites)
                    DockItem(Icons.Filled.Search, "Search", onSearch)
                    DockItem(Icons.Filled.Add, "Add", onSettings)
                    if (lastRefreshTime > 0) {
                        DockItem(Icons.Filled.AccessTime, formatRefreshTime(lastRefreshTime)) { }
                    }
                }
            }
        }
    }
}

@Composable
private fun TileCard(
    id: String, title: String, subtitle: String,
    icon: ImageVector, gradient: List<Color>, badge: String?,
    delayMs: Int, loaded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(loaded) { if (loaded) { delay(delayMs.toLong()); visible = true } }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(300)) + scaleIn(tween(300), initialScale = 0.92f)
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth().fillMaxHeight()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.linearGradient(
                        gradient,
                        start = androidx.compose.ui.geometry.Offset(0f, 0f),
                        end = androidx.compose.ui.geometry.Offset(500f, 500f)
                    )
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                )
                .padding(20.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(icon, title, tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(30.dp))
                    if (badge != null) {
                        Box(
                            modifier = Modifier.clip(RoundedCornerShape(6.dp))
                                .background(Color.White.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(badge, color = Color.White,
                                fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(Modifier.weight(1f))
                Text(title, color = Color.White, fontSize = 18.sp,
                    fontWeight = FontWeight.Bold)
                Text(subtitle, color = Color.White.copy(alpha = 0.6f),
                    fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun DockItem(
    icon: ImageVector, label: String, onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp))
                .background(VinColors.Background),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, label, tint = VinColors.Accent, modifier = Modifier.size(16.dp))
        }
        Text(label, color = VinColors.TextTertiary, fontSize = 9.sp)
    }
}

private fun formatRefreshTime(millis: Long): String {
    val diff = System.currentTimeMillis() - millis
    val sec = diff / 1000
    val min = sec / 60
    return when {
        sec < 60 -> "Now"
        min < 60 -> "${min}m"
        else -> "${min / 60}h"
    }
}
