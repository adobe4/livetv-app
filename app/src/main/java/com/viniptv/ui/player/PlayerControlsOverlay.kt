package com.viniptv.ui.player

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.viniptv.data.model.Channel
import com.viniptv.ui.theme.VinColors
import kotlinx.coroutines.delay

@Composable
fun PlayerControlsOverlay(
    channel: Channel,
    isPlaying: Boolean,
    bufferPercent: Int,
    isBuffering: Boolean,
    currentPosition: Long,
    duration: Long,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onSeekForward: () -> Unit,
    onSeekBackward: () -> Unit,
    onFullscreen: () -> Unit,
    onShowSettings: () -> Unit,
    onVolumeChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var controlsVisible by remember { mutableStateOf(true) }
    var volume by remember { mutableFloatStateOf(0.8f) }

    LaunchedEffect(Unit) {
        delay(5000)
        controlsVisible = false
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = if (controlsVisible) 0.4f else 0f))
            .clickable(enabled = controlsVisible) { controlsVisible = false }
            .then(
                if (!controlsVisible) Modifier.clickable { controlsVisible = true }
                else Modifier
            )
    ) {
        if (controlsVisible) {
            // Buffering indicator
            if (isBuffering) {
                Box(modifier = Modifier.align(Alignment.Center).padding(bottom = 80.dp)) {
                    CircularProgressIndicator(
                        color = VinColors.Accent,
                        modifier = Modifier.size(48.dp),
                        strokeWidth = 3.dp
                    )
                }
            }

            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopStart)
                    .background(VinColors.Overlay)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(
                            modifier = Modifier
                                .width(6.dp).height(6.dp)
                                .clip(CircleShape)
                                .background(VinColors.Live)
                        )
                        Text("LIVE", color = VinColors.Live, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(channel.name, color = VinColors.TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(channel.category, color = VinColors.TextSecondary, fontSize = 12.sp)
                }
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.1f))
                        .clickable(onClick = onFullscreen),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Fullscreen, "Fullscreen", tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }

            // Center controls
            Row(
                modifier = Modifier.align(Alignment.Center),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPrev, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Filled.SkipPrevious, "Prev", tint = Color.White, modifier = Modifier.size(32.dp))
                }
                IconButton(onClick = onSeekBackward, modifier = Modifier.size(44.dp)) {
                    Icon(Icons.Filled.Replay10, "Back 10s", tint = Color.White, modifier = Modifier.size(28.dp))
                }
                IconButton(
                    onClick = onPlayPause,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f))
                ) {
                    Icon(
                        if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        "Play/Pause",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }
                IconButton(onClick = onSeekForward, modifier = Modifier.size(44.dp)) {
                    Icon(Icons.Filled.Forward30, "Forward 30s", tint = Color.White, modifier = Modifier.size(28.dp))
                }
                IconButton(onClick = onNext, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Filled.SkipNext, "Next", tint = Color.White, modifier = Modifier.size(32.dp))
                }
            }

            // Bottom controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomStart)
                    .background(VinColors.Overlay)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Volume
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.VolumeUp, "Volume", tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(6.dp))
                    Slider(
                        value = volume,
                        onValueChange = { volume = it; onVolumeChange(it) },
                        modifier = Modifier.width(100.dp),
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = VinColors.Accent,
                            inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                        )
                    )
                }

                // Settings button
                IconButton(onClick = onShowSettings) {
                    Icon(Icons.Filled.Settings, "Settings", tint = Color.White.copy(alpha = 0.7f))
                }
            }
        }
    }
}
