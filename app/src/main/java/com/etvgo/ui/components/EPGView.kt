package com.etvgo.ui.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.etvgo.data.model.EPGProgram
import com.etvgo.ui.theme.*

@Composable
fun EPGView(
    programs: List<EPGProgram>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(DarkBackground)
            .padding(vertical = 8.dp, horizontal = 12.dp)
    ) {
        Text(
            text = "EPG Guide",
            color = TextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        if (programs.isEmpty()) {
            Text(
                text = "No EPG data available",
                color = TextTertiary,
                fontSize = 12.sp,
                modifier = Modifier.padding(vertical = 12.dp)
            )
            return
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(programs.take(20)) { program ->
                val isLive = program.isCurrentlyPlaying
                val bgColor = if (isLive) AccentBlueDim else DarkSurface
                val borderColor = if (isLive) AccentBlue else DarkSurfaceVariant

                Column(
                    modifier = Modifier
                        .width(150.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(bgColor)
                        .border(1.dp, borderColor, RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    if (isLive) {
                        Text(
                            text = "● LIVE",
                            color = LiveRed,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = program.title,
                        color = if (isLive) TextPrimary else TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = formatTime(program.startTime),
                        color = TextTertiary,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

private fun formatTime(millis: Long): String {
    val sdf = java.text.SimpleDateFormat("HH:mm", java.util.Locale.US)
    return sdf.format(java.util.Date(millis))
}
