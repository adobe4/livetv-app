package com.viniptv.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.viniptv.ui.theme.VinColors

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
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(VinColors.Background)
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(VinColors.Accent, VinColors.Accent.copy(alpha = 0.6f))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("V", color = Color.White,
                            fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text("Vin IPTV",
                            color = VinColors.TextPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold)
                        if (isLoading) {
                            Text("Loading channels...",
                                color = VinColors.Accent, fontSize = 11.sp)
                        } else if (channelCount > 0) {
                            Text("$channelCount channels • $playlistName",
                                color = VinColors.TextTertiary, fontSize = 11.sp,
                                maxLines = 1, overflow = TextOverflow.Ellipsis)
                        } else {
                            Text(playlistName,
                                color = VinColors.TextTertiary, fontSize = 11.sp)
                        }
                    }
                }
            }

            // Refresh and settings buttons
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (channelCount > 0) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(VinColors.SurfaceLight)
                            .clickable(onClick = onRefresh),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = VinColors.Accent,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Filled.Refresh, "Refresh",
                                tint = VinColors.TextSecondary, modifier = Modifier.size(20.dp))
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(VinColors.SurfaceLight)
                        .clickable(onClick = onSettings),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Settings, "Settings",
                        tint = VinColors.TextSecondary, modifier = Modifier.size(20.dp))
                }
            }
        }

        // Main content - horizontal scroll of category cards
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Main categories column (2 big cards)
            Column(
                modifier = Modifier.weight(1.5f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Row 1: Live TV + Movies
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CategoryCard(
                        title = "Live TV",
                        subtitle = if (channelCount > 0) "$channelCount channels" else "Browse channels",
                        icon = Icons.Filled.LiveTv,
                        gradient = listOf(Color(0xFF007AFF), Color(0xFF0055CC)),
                        badge = if (channelCount > 0) "$channelCount" else null,
                        onClick = onLiveTv,
                        modifier = Modifier.weight(1f)
                    )
                    CategoryCard(
                        title = "VOD",
                        subtitle = if (channelCount > 0) "Movies & Series" else "Coming soon",
                        icon = Icons.Filled.Movie,
                        gradient = listOf(Color(0xFFFF2D55), Color(0xFFCC0044)),
                        badge = null,
                        onClick = onVod,
                        modifier = Modifier.weight(1f)
                    )
                }
                // Row 2: Sports + EPG
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CategoryCard(
                        title = "Sports",
                        subtitle = "Live sports events",
                        icon = Icons.Filled.FitnessCenter,
                        gradient = listOf(Color(0xFFFF9500), Color(0xFFCC6600)),
                        onClick = onSports,
                        modifier = Modifier.weight(1f)
                    )
                    CategoryCard(
                        title = "EPG Guide",
                        subtitle = "TV schedule",
                        icon = Icons.Filled.Schedule,
                        gradient = listOf(Color(0xFF9B59B6), Color(0xFF6C3483)),
                        onClick = onEpg,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Bottom status bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(VinColors.Surface)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (channelCount > 0) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(VinColors.Success)
                    )
                    Text("Connected", color = VinColors.TextSecondary, fontSize = 12.sp)
                } else {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(VinColors.Warning)
                    )
                    Text("No channels loaded", color = VinColors.TextTertiary, fontSize = 12.sp)
                }
                if (lastRefreshTime > 0) {
                    Text("•", color = VinColors.TextTertiary, fontSize = 12.sp)
                    Text(formatRefreshTime(lastRefreshTime),
                        color = VinColors.TextTertiary, fontSize = 11.sp)
                }
            }

            if (channelCount == 0 && !isLoading) {
                TextButton(onClick = onSettings) {
                    Icon(Icons.Filled.Add, null, modifier = Modifier.size(14.dp),
                        tint = VinColors.Accent)
                    Spacer(Modifier.width(4.dp))
                    Text("Add Playlist", color = VinColors.Accent, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun CategoryCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    gradient: List<Color>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badge: String? = null
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    listOf(gradient[0], gradient.getOrElse(1) { gradient[0] }),
                    start = androidx.compose.ui.geometry.Offset(0f, 0f),
                    end = androidx.compose.ui.geometry.Offset(600f, 600f)
                )
            )
            .clickable(onClick = onClick)
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Icon with badge
            Box {
                Icon(icon, title, tint = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.size(32.dp))
                if (badge != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 4.dp, y = (-4).dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.White.copy(alpha = 0.25f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(badge, color = Color.White,
                            fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Title and subtitle
            Column {
                Text(title, color = Color.White,
                    fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(subtitle, color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp)
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
