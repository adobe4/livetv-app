package com.viniptv.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.viniptv.ui.theme.VinColors

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onManagePlaylists: () -> Unit,
    onPlayerSettings: () -> Unit,
    onParentalControl: () -> Unit,
    onAbout: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(VinColors.Background)
            .padding(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, "Back", tint = VinColors.TextPrimary)
                }
                Spacer(Modifier.width(8.dp))
                Text("Settings", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = VinColors.TextPrimary)
            }
        }

        item { SettingsSection("Content") }
        item { SettingsItem(Icons.Filled.PlaylistPlay, "My Playlists", "Manage IPTV sources", onClick = onManagePlaylists) }
        item { SettingsItem(Icons.Filled.Movie, "VOD Categories", "Organize movies & series", onClick = { }) }
        item { SettingsItem(Icons.Filled.Tv, "EPG Guide", "Configure electronic program guide", onClick = { }) }
        item { HorizontalDivider(color = VinColors.Separator) }

        item { SettingsSection("Player") }
        item { SettingsItem(Icons.Filled.Speed, "Player Settings", "Buffer, decoder, aspect ratio", onClick = onPlayerSettings) }
        item { SettingsItem(Icons.Filled.Subtitles, "Audio & Subtitles", "Default audio track, subtitles", onClick = { }) }
        item { SettingsItem(Icons.Filled.AspectRatio, "Aspect Ratio", "Fit, fill, zoom, stretch", onClick = { }) }
        item { HorizontalDivider(color = VinColors.Separator) }

        item { SettingsSection("Interface") }
        item { SettingsItem(Icons.Filled.Palette, "Appearance", "Theme, colors, layout", onClick = { }) }
        item { SettingsItem(Icons.Filled.Language, "Language", "App language", onClick = { }) }
        item { SettingsItem(Icons.Filled.Sort, "Channel Order", "Sort by number, name, category", onClick = { }) }
        item { HorizontalDivider(color = VinColors.Separator) }

        item { SettingsSection("Security") }
        item { SettingsItem(Icons.Filled.Lock, "Parental Control", "PIN lock on channels", onClick = onParentalControl) }
        item { SettingsItem(Icons.Filled.Shield, "Adult Content Filter", "Filter mature content", onClick = { }) }
        item { HorizontalDivider(color = VinColors.Separator) }

        item { SettingsSection("System") }
        item { SettingsItem(Icons.Filled.Info, "About Vin IPTV", "Version 2.0.0", onClick = onAbout) }
        item { SettingsItem(Icons.Filled.Backup, "Backup & Restore", "Export settings & favorites", onClick = { }) }
        item { SettingsItem(Icons.Filled.Share, "Share App", "Tell your friends", onClick = { }) }

        item { Spacer(Modifier.height(40.dp)) }
    }
}

@Composable
private fun SettingsSection(title: String) {
    Text(
        text = title,
        color = VinColors.Accent,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    )
}

@Composable
private fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(VinColors.SurfaceLight),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, title, tint = VinColors.Accent, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = VinColors.TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Text(subtitle, color = VinColors.TextTertiary, fontSize = 12.sp)
        }
        Icon(Icons.Filled.ChevronRight, ">", tint = VinColors.TextTertiary, modifier = Modifier.size(20.dp))
    }
}
