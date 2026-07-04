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
    onLiveTv: () -> Unit,
    onVodMovies: () -> Unit,
    onVodSeries: () -> Unit,
    onSports: () -> Unit,
    onEpg: () -> Unit,
    onSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tiles = listOf(
        HomeTile("live", "Live TV", "Watch live channels", Icons.Filled.LiveTv,
            listOf(Color(0xFF007AFF), Color(0xFF0055CC)), "HD"),
        HomeTile("movies", "Movies", "On-demand films", Icons.Filled.Movie,
            listOf(Color(0xFFFF2D55), Color(0xFFCC0044)),
            if (java.util.Calendar.getInstance().get(java.util.Calendar.YEAR) > 0) "NEW" else null),
        HomeTile("series", "Series", "TV shows & seasons", Icons.Filled.Tv,
            listOf(Color(0xFF34C759), Color(0xFF009933))),
        HomeTile("sports", "Sports", "Live sports events", Icons.Filled.FitnessCenter,
            listOf(Color(0xFFFF9500), Color(0xFFCC6600))),
        HomeTile("epg", "EPG Guide", "TV schedule", Icons.Filled.Schedule,
            listOf(Color(0xFF9B59B6), Color(0xFF6C3483))),
        HomeTile("settings", "Settings", "Manage playlists & more", Icons.Filled.Settings,
            listOf(Color(0xFF6E6E73), Color(0xFF3A3A3C)))
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(VinColors.Background)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Vin IPTV",
                    color = VinColors.TextPrimary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold)
                Text("Premium IPTV Experience",
                    color = VinColors.TextSecondary,
                    fontSize = 13.sp)
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(VinColors.Accent, VinColors.Accent.copy(alpha = 0.6f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text("V", color = Color.White,
                    fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(8.dp))

        // Category Grid
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Row 1: Live TV, Movies, Series
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                tiles.take(3).forEach { tile ->
                    HomeTileCard(
                        tile = tile,
                        onClick = when (tile.id) {
                            "live" -> onLiveTv
                            "movies" -> onVodMovies
                            "series" -> onVodSeries
                            else -> { {} }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Row 2: Sports, EPG, Settings
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                tiles.drop(3).forEach { tile ->
                    HomeTileCard(
                        tile = tile,
                        onClick = when (tile.id) {
                            "sports" -> onSports
                            "epg" -> onEpg
                            "settings" -> onSettings
                            else -> { {} }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Bottom bar with quick actions
        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            QuickAction(Icons.Filled.Favorite, "Favorites") { }
            QuickAction(Icons.Filled.History, "Recent") { }
            QuickAction(Icons.Filled.Search, "Search") { }
            QuickAction(Icons.Filled.Add, "Add Playlist") { }
        }
    }
}

@Composable
private fun HomeTileCard(
    tile: HomeTile,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    tile.gradient,
                    start = androidx.compose.ui.geometry.Offset(0f, 0f),
                    end = androidx.compose.ui.geometry.Offset(300f, 300f)
                )
            )
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Icon at top
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(tile.icon, tile.title,
                    tint = Color.White, modifier = Modifier.size(24.dp))
            }

            // Text at bottom
            Column {
                if (tile.badge != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.White.copy(alpha = 0.3f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(tile.badge, color = Color.White,
                            fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(4.dp))
                }
                Text(tile.title, color = Color.White,
                    fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Text(tile.subtitle, color = Color.White.copy(alpha = 0.7f),
                    fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun QuickAction(
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
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(VinColors.SurfaceLight),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, label, tint = VinColors.Accent, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.height(4.dp))
        Text(label, color = VinColors.TextTertiary, fontSize = 10.sp)
    }
}
