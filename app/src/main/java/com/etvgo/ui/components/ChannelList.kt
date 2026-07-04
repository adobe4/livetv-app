package com.etvgo.ui.components

import androidx.compose.animation.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.etvgo.data.model.Channel
import com.etvgo.ui.theme.*

@Composable
fun ChannelList(
    channels: List<Channel>,
    currentChannelId: String?,
    onChannelSelected: (Channel) -> Unit,
    onFavoriteToggle: (Channel) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxHeight()
            .background(DarkBackground),
        contentPadding = PaddingValues(vertical = 4.dp)
    ) {
        itemsIndexed(
            items = channels,
            key = { _, ch -> ch.id }
        ) { index, channel ->
            val isSelected = channel.id == currentChannelId
            val bgColor by animateColorAsState(
                if (isSelected) SelectedBackground else Color.Transparent,
                label = "chBg"
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(bgColor)
                    .clickable { onChannelSelected(channel) }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Channel logo
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkSurface),
                    contentAlignment = Alignment.Center
                ) {
                    if (channel.logoUrl.isNotEmpty()) {
                        AsyncImage(
                            model = channel.logoUrl,
                            contentDescription = channel.name,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(6.dp)),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(AccentBlueDim),
                            contentAlignment = Alignment.Center
                        ) {
                            androidx.compose.material3.Text(
                                text = channel.name.take(2).uppercase(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentBlue
                            )
                        }
                    }
                }

                Spacer(Modifier.width(10.dp))

                // Channel info
                Column(modifier = Modifier.weight(1f)) {
                    androidx.compose.material3.Text(
                        text = channel.name,
                        color = if (isSelected) TextPrimary else TextSecondary,
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (channel.category != "Uncategorized") {
                        androidx.compose.material3.Text(
                            text = channel.category,
                            color = TextTertiary,
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    }
                }

                // Favorite button
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .clickable { onFavoriteToggle(channel) }
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = if (channel.isFavorite)
                            Icons.Filled.Star
                        else
                            Icons.Outlined.StarOutline,
                        contentDescription = if (channel.isFavorite) "Remove favorite" else "Add favorite",
                        tint = if (channel.isFavorite) FavoriteGold else TextTertiary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
