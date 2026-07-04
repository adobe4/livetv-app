package com.viniptv.ui.components

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.viniptv.ui.screens.MainTab
import com.viniptv.ui.theme.VinColors

@Composable
fun BottomNavBar(
    currentTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = VinColors.Surface,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavTab(
                icon = Icons.Filled.LiveTv,
                label = "Live TV",
                tab = MainTab.LIVE_TV,
                isSelected = currentTab == MainTab.LIVE_TV,
                onClick = { onTabSelected(MainTab.LIVE_TV) }
            )
            NavTab(
                icon = Icons.Filled.Movie,
                label = "VOD",
                tab = MainTab.VOD,
                isSelected = currentTab == MainTab.VOD,
                onClick = { onTabSelected(MainTab.VOD) }
            )
            NavTab(
                icon = Icons.Filled.ListAlt,
                label = "EPG",
                tab = MainTab.EPG,
                isSelected = currentTab == MainTab.EPG,
                onClick = { onTabSelected(MainTab.EPG) }
            )
            NavTab(
                icon = Icons.Filled.Settings,
                label = "Settings",
                tab = MainTab.SETTINGS,
                isSelected = currentTab == MainTab.SETTINGS,
                onClick = { onTabSelected(MainTab.SETTINGS) }
            )
        }
    }
}

@Composable
private fun NavTab(
    icon: ImageVector,
    label: String,
    tab: MainTab,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        if (isSelected) VinColors.AccentDim else VinColors.Surface,
        label = "navTab_$tab"
    )

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                icon,
                label,
                tint = if (isSelected) VinColors.Accent else VinColors.TextTertiary,
                modifier = Modifier.size(20.dp)
            )
            Text(
                label,
                color = if (isSelected) VinColors.Accent else VinColors.TextTertiary,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
            )
        }
    }
}
