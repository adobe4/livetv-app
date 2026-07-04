package com.etvgo.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.etvgo.ui.theme.*

enum class ImportTab { M3U_URL, M3U_FILE, XTREAM }

@Composable
fun SettingsDialog(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onM3uUrlSubmit: (String) -> Unit,
    onXtreamSubmit: (String, String, String) -> Unit,
    onFileUpload: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(ImportTab.M3U_URL) }
    var m3uUrl by remember { mutableStateOf("") }
    var xtreamServer by remember { mutableStateOf("") }
    var xtreamUser by remember { mutableStateOf("") }
    var xtreamPass by remember { mutableStateOf("") }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground.copy(alpha = 0.7f))
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .width(500.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkSurface)
                    .border(1.dp, Separator, RoundedCornerShape(16.dp))
                    .clickable(enabled = false) { }
                    .padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Add Playlist",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .clickable(onClick = onDismiss)
                            .focusable()
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Close, "Close", tint = TextSecondary)
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Tabs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkBackground)
                        .padding(3.dp)
                ) {
                    ImportTab.entries.forEach { tab ->
                        val isSelected = selectedTab == tab
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) AccentBlue else Color.Transparent)
                                .clickable { selectedTab = tab }
                                .focusable()
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = when (tab) {
                                    ImportTab.M3U_URL -> "M3U URL"
                                    ImportTab.M3U_FILE -> "Upload"
                                    ImportTab.XTREAM -> "Xtream"
                                },
                                color = if (isSelected) TextPrimary else TextSecondary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Content based on tab
                when (selectedTab) {
                    ImportTab.M3U_URL -> {
                        Text("M3U Playlist URL", color = TextSecondary, fontSize = 13.sp)
                        Spacer(Modifier.height(6.dp))
                        BasicTextField(
                            value = m3uUrl,
                            onValueChange = { m3uUrl = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkBackground)
                                .border(1.dp, Separator, RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            textStyle = androidx.compose.ui.text.TextStyle(
                                color = TextPrimary, fontSize = 15.sp
                            ),
                            singleLine = true,
                            decorationBox = { inner ->
                                if (m3uUrl.isEmpty()) {
                                    Text("https://example.com/playlist.m3u", color = TextTertiary, fontSize = 15.sp)
                                }
                                inner()
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Go),
                            keyboardActions = KeyboardActions(onGo = { onM3uUrlSubmit(m3uUrl) })
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = { onM3uUrlSubmit(m3uUrl) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                            shape = RoundedCornerShape(10.dp),
                            enabled = m3uUrl.isNotBlank()
                        ) {
                            Text("Load Playlist", fontWeight = FontWeight.Semibold)
                        }
                    }

                    ImportTab.M3U_FILE -> {
                        Text("Upload an M3U file from your device", color = TextSecondary, fontSize = 13.sp)
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = onFileUpload,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DarkBackground),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Separator)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Filled.CloudUpload, "Upload", tint = AccentBlue, modifier = Modifier.size(32.dp))
                                Spacer(Modifier.height(6.dp))
                                Text("Select M3U File", color = AccentBlue, fontWeight = FontWeight.Medium)
                            }
                        }
                    }

                    ImportTab.XTREAM -> {
                        Text("Server URL", color = TextSecondary, fontSize = 13.sp)
                        Spacer(Modifier.height(6.dp))
                        BasicTextField(
                            value = xtreamServer,
                            onValueChange = { xtreamServer = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkBackground)
                                .border(1.dp, Separator, RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            textStyle = androidx.compose.ui.text.TextStyle(color = TextPrimary, fontSize = 15.sp),
                            singleLine = true,
                            decorationBox = { inner ->
                                if (xtreamServer.isEmpty()) Text("http://your-server.com:8080", color = TextTertiary, fontSize = 15.sp)
                                inner()
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
                        )
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Username", color = TextSecondary, fontSize = 13.sp)
                                Spacer(Modifier.height(6.dp))
                                BasicTextField(
                                    value = xtreamUser,
                                    onValueChange = { xtreamUser = it },
                                    modifier = Modifier.fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(DarkBackground)
                                        .border(1.dp, Separator, RoundedCornerShape(8.dp))
                                        .padding(12.dp),
                                    textStyle = androidx.compose.ui.text.TextStyle(color = TextPrimary, fontSize = 15.sp),
                                    singleLine = true
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Password", color = TextSecondary, fontSize = 13.sp)
                                Spacer(Modifier.height(6.dp))
                                BasicTextField(
                                    value = xtreamPass,
                                    onValueChange = { xtreamPass = it },
                                    modifier = Modifier.fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(DarkBackground)
                                        .border(1.dp, Separator, RoundedCornerShape(8.dp))
                                        .padding(12.dp),
                                    textStyle = androidx.compose.ui.text.TextStyle(color = TextPrimary, fontSize = 15.sp),
                                    singleLine = true,
                                    visualTransformation = PasswordVisualTransformation()
                                )
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = { onXtreamSubmit(xtreamServer, xtreamUser, xtreamPass) },
                            modifier = Modifier.fillMaxWidth().height(44.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                            shape = RoundedCornerShape(10.dp),
                            enabled = xtreamServer.isNotBlank() && xtreamUser.isNotBlank() && xtreamPass.isNotBlank()
                        ) {
                            Text("Connect", fontWeight = FontWeight.Semibold)
                        }
                    }
                }
            }
        }
    }
}
