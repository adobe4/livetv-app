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
import com.viniptv.ui.theme.VinColors

data class NavTabInfo(
    val id: String,
    val icon: ImageVector,
    val label: String
)

@Composable
fun NavBarRow(
    selectedId: String,
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val tabs = listOf(
        NavTabInfo("home", Icons.Filled.Home, "Home"),
        NavTabInfo("live", Icons.Filled.LiveTv, "Live"),
        NavTabInfo("vod", Icons.Filled.Movie, "VOD"),
        NavTabInfo("epg", Icons.Filled.ListAlt, "EPG"),
        NavTabInfo("settings", Icons.Filled.Settings, "Settings")
    )

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
            tabs.forEach { tab ->
                NavTabItem(
                    icon = tab.icon,
                    label = tab.label,
                    isSelected = selectedId == tab.id,
                    onClick = { onTabSelected(tab.id) }
                )
            }
        }
    }
}

@Composable
private fun NavTabItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        if (isSelected) VinColors.AccentDim else VinColors.Surface,
        label = "navTab"
    )

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
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
