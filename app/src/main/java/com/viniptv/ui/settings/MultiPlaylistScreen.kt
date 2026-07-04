package com.viniptv.ui.settings

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.viniptv.data.model.PlaylistSource
import com.viniptv.data.model.PlaylistType
import com.viniptv.ui.theme.VinColors

@Composable
fun MultiPlaylistScreen(
    playlists: List<PlaylistSource>,
    activePlaylistId: String,
    onActivate: (String) -> Unit,
    onAdd: () -> Unit,
    onEdit: (PlaylistSource) -> Unit,
    onDelete: (String) -> Unit,
    onBack: () -> Unit,
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
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, "Back", tint = VinColors.TextPrimary)
                }
                Spacer(Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("My Playlists", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = VinColors.TextPrimary)
                    Text("${playlists.size} source(s)", color = VinColors.TextTertiary, fontSize = 14.sp)
                }
                Button(
                    onClick = onAdd,
                    colors = ButtonDefaults.buttonColors(containerColor = VinColors.Accent),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.Add, "Add", modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Add", fontWeight = FontWeight.SemiBold)
                }
            }
        }

        if (playlists.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.PlaylistAdd, "No playlists", tint = VinColors.TextTertiary, modifier = Modifier.size(64.dp))
                        Spacer(Modifier.height(12.dp))
                        Text("No playlists added", color = VinColors.TextSecondary, fontSize = 16.sp)
                        Text("Add an M3U or Xtream source", color = VinColors.TextTertiary, fontSize = 13.sp)
                    }
                }
            }
        }

        items(playlists) { playlist ->
            val isActive = playlist.id == activePlaylistId
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isActive) VinColors.AccentDim else VinColors.Surface)
                    .border(1.dp, if (isActive) VinColors.Accent else VinColors.Separator, RoundedCornerShape(12.dp))
                    .clickable { onActivate(playlist.id) }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isActive) VinColors.Accent.copy(alpha = 0.2f) else VinColors.SurfaceLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        when (playlist.type) {
                            PlaylistType.XTREAM -> Icons.Filled.Api
                            PlaylistType.M3U_FILE -> Icons.Filled.UploadFile
                            PlaylistType.M3U_URL -> Icons.Filled.Link
                        },
                        "Type",
                        tint = if (isActive) VinColors.Accent else VinColors.TextSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(playlist.name, color = VinColors.TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                        if (isActive) {
                            Spacer(Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(VinColors.Accent)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("ACTIVE", color = VinColors.TextPrimary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Text(
                        when (playlist.type) {
                            PlaylistType.XTREAM -> "Xtream Codes"
                            PlaylistType.M3U_FILE -> "M3U File"
                            PlaylistType.M3U_URL -> "M3U URL"
                        },
                        color = VinColors.TextTertiary,
                        fontSize = 12.sp
                    )
                }
                IconButton(onClick = { onEdit(playlist) }) {
                    Icon(Icons.Filled.Edit, "Edit", tint = VinColors.TextSecondary, modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = { onDelete(playlist.id) }) {
                    Icon(Icons.Filled.Delete, "Delete", tint = VinColors.Live.copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
                }
            }
        }

        item { Spacer(Modifier.height(80.dp)) }
    }
}
