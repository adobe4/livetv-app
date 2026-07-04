package com.viniptv.ui.epg

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.viniptv.data.model.Channel
import com.viniptv.data.model.EPGProgram
import com.viniptv.ui.theme.VinColors
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun EPGGrid(
    channels: List<Channel>,
    epgData: Map<String, List<EPGProgram>>,
    currentChannelId: String?,
    onChannelSelected: (Channel) -> Unit,
    modifier: Modifier = Modifier
) {
    val timeSlots = remember { generateTimeSlots() }
    val currentTime = remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime.value = System.currentTimeMillis()
            kotlinx.coroutines.delay(60000)
        }
    }

    Column(modifier = modifier.fillMaxSize().background(VinColors.Background)) {
        // Header row with time slots
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(VinColors.Surface)
                .padding(start = 140.dp)
        ) {
            items(timeSlots) { time ->
                Box(
                    modifier = Modifier
                        .width(120.dp)
                        .padding(horizontal = 4.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = formatTimeSlot(time),
                        color = VinColors.TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Channel list with EPG
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(channels.take(50)) { channel ->
                val programs = epgData[channel.epgChannelId] ?: emptyList()
                EPGChannelRow(
                    channel = channel,
                    programs = programs,
                    isSelected = channel.id == currentChannelId,
                    timeSlots = timeSlots,
                    currentTime = currentTime.value,
                    onClick = { onChannelSelected(channel) }
                )
            }
        }
    }
}

@Composable
private fun EPGChannelRow(
    channel: Channel,
    programs: List<EPGProgram>,
    isSelected: Boolean,
    timeSlots: List<Long>,
    currentTime: Long,
    onClick: () -> Unit
) {
    val bg = if (isSelected) VinColors.AccentDim else VinColors.Surface

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(bg)
            .clickable(onClick = onClick)
    ) {
        // Channel info
        Row(
            modifier = Modifier
                .width(140.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = channel.displayNumber.ifEmpty { "" },
                color = VinColors.TextTertiary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.width(28.dp)
            )
            Text(
                text = channel.name,
                color = if (isSelected) VinColors.TextPrimary else VinColors.TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // EPG programs
        LazyRow(modifier = Modifier.fillMaxSize()) {
            items(programs.take(20)) { program ->
                val isLive = program.isCurrentlyPlaying
                val width = calculateProgramWidth(program, timeSlots)

                Box(
                    modifier = Modifier
                        .width(width.dp)
                        .padding(horizontal = 2.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            if (isLive) VinColors.AccentDim
                            else VinColors.SurfaceLight
                        )
                        .border(
                            1.dp,
                            if (isLive) VinColors.Accent else VinColors.SeparatorLight,
                            RoundedCornerShape(4.dp)
                        )
                        .padding(4.dp)
                ) {
                    Column {
                        Text(
                            text = program.title,
                            color = if (isLive) VinColors.TextPrimary else VinColors.TextSecondary,
                            fontSize = 10.sp,
                            fontWeight = if (isLive) FontWeight.SemiBold else FontWeight.Normal,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = formatTimeRange(program.startTime, program.endTime),
                            color = VinColors.TextTertiary,
                            fontSize = 9.sp
                        )
                    }
                }
            }
        }
    }
}

private fun generateTimeSlots(): List<Long> {
    val slots = mutableListOf<Long>()
    val cal = Calendar.getInstance()
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    val startOfDay = cal.timeInMillis
    for (i in 0..48) {
        slots.add(startOfDay + i * 1800000L) // 30-min slots
    }
    return slots
}

private fun formatTimeSlot(millis: Long): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.US)
    return sdf.format(Date(millis))
}

private fun formatTimeRange(start: Long, end: Long): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.US)
    return "${sdf.format(Date(start))} - ${sdf.format(Date(end))}"
}

private fun calculateProgramWidth(program: EPGProgram, timeSlots: List<Long>): Float {
    val duration = (program.endTime - program.startTime).toFloat()
    // Each time slot is 30 min = 120.dp
    return (duration / 1800000f) * 120f
}
