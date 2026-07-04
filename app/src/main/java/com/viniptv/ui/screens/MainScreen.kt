package com.viniptv.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.viniptv.data.model.VODCategory
import com.viniptv.data.model.VODCategoryType
import com.viniptv.ui.components.BottomNavBar
import com.viniptv.ui.epg.EPGGrid
import com.viniptv.ui.settings.SettingsScreen
import com.viniptv.ui.settings.MultiPlaylistScreen
import com.viniptv.ui.theme.VinColors
import com.viniptv.ui.vod.VODScreen

enum class MainTab { LIVE_TV, VOD, EPG, SETTINGS }

@Composable
fun MainScreen() {
    var currentTab by remember { mutableStateOf(MainTab.VOD) }
    var showSettings by remember { mutableStateOf(false) }
    var showMultiPlaylist by remember { mutableStateOf(false) }

    // TODO: These will come from ViewModel
    val sampleVODCategories = remember { mutableStateListOf<VODCategory>() }

    Box(modifier = Modifier.fillMaxSize().background(VinColors.Background)) {

        AnimatedContent(
            targetState = currentTab,
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
            },
            label = "tabContent"
        ) { tab ->
            when (tab) {
                MainTab.LIVE_TV -> {
                    // TODO: Live TV screen with channel list + player
                    Box(modifier = Modifier.fillMaxSize().padding(bottom = 64.dp),
                        contentAlignment = Alignment.Center) {
                        Text("Live TV", color = VinColors.TextSecondary, fontSize = 18.sp)
                    }
                }
                MainTab.VOD -> {
                    VODScreen(
                        categories = sampleVODCategories,
                        onMovieClick = { },
                        onSeriesClick = { },
                        onSearch = { },
                        modifier = Modifier.padding(bottom = 64.dp)
                    )
                }
                MainTab.EPG -> {
                    EPGGrid(
                        channels = emptyList(),
                        epgData = emptyMap(),
                        currentChannelId = null,
                        onChannelSelected = { },
                        modifier = Modifier.padding(bottom = 64.dp)
                    )
                }
                MainTab.SETTINGS -> {
                    if (showMultiPlaylist) {
                        MultiPlaylistScreen(
                            playlists = emptyList(),
                            activePlaylistId = "",
                            onActivate = { },
                            onAdd = { },
                            onEdit = { },
                            onDelete = { },
                            onBack = { showMultiPlaylist = false },
                            modifier = Modifier.padding(bottom = 64.dp)
                        )
                    } else {
                        SettingsScreen(
                            onBack = { },
                            onManagePlaylists = { showMultiPlaylist = true },
                            onPlayerSettings = { },
                            onParentalControl = { },
                            onAbout = { },
                            modifier = Modifier.padding(bottom = 64.dp)
                        )
                    }
                }
            }
        }

        // Bottom Navigation
        BottomNavBar(
            currentTab = currentTab,
            onTabSelected = { currentTab = it },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
