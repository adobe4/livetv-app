package com.viniptv.ui.vod

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.viniptv.data.model.VODItem
import com.viniptv.data.model.VODCategory
import com.viniptv.data.model.VODCategoryType
import com.viniptv.ui.theme.VinColors

@Composable
fun VODScreen(
    categories: List<VODCategory>,
    onMovieClick: (VODItem) -> Unit,
    onSeriesClick: (VODItem) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(VinColors.Background),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Hero header
        item {
            HeroHeader(onSearch = onSearch)
        }

        // Movie categories
        categories.filter { it.type == VODCategoryType.TRENDING || it.type == VODCategoryType.POPULAR }
            .forEach { category ->
                item {
                    VODCategoryRow(
                        title = category.name,
                        items = category.items.take(10),
                        onItemClick = onMovieClick,
                        isHero = category.type == VODCategoryType.TRENDING
                    )
                }
            }

        // Continue Watching
        item {
            VODCategoryRow(
                title = "Continue Watching",
                items = categories.find { it.type == VODCategoryType.CONTINUE_WATCHING }?.items ?: emptyList(),
                onItemClick = onMovieClick
            )
        }

        // Movies
        item {
            SectionHeader("Movies", onSeeAll = { })
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(horizontal = 16.dp)) {
                items(categories.find { it.type == VODCategoryType.MOVIES }?.items?.take(15) ?: emptyList()) { movie ->
                    MovieCard(movie, onClick = { onMovieClick(movie) })
                }
            }
        }

        // Series
        item {
            SectionHeader("TV Series", onSeeAll = { })
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(horizontal = 16.dp)) {
                items(categories.find { it.type == VODCategoryType.SERIES }?.items?.take(15) ?: emptyList()) { series ->
                    SeriesCard(series, onClick = { onSeriesClick(series) })
                }
            }
        }

        // New Releases
        item {
            SectionHeader("New Releases", onSeeAll = { })
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(horizontal = 16.dp)) {
                items(categories.find { it.type == VODCategoryType.NEW_RELEASES }?.items?.take(15) ?: emptyList()) { movie ->
                    MovieCard(movie, onClick = { onMovieClick(movie) })
                }
            }
        }

        item { Spacer(Modifier.height(80.dp)) }
    }
}

@Composable
private fun HeroHeader(onSearch: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .background(
                Brush.verticalGradient(
                    listOf(VinColors.SurfaceLight, VinColors.Background)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            Text("Vin IPTV", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = VinColors.TextPrimary)
            Spacer(Modifier.height(4.dp))
            Text("Movies • Series • Live TV", fontSize = 15.sp, color = VinColors.TextSecondary)
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { },
                    colors = ButtonDefaults.buttonColors(containerColor = VinColors.Accent),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(44.dp)
                ) {
                    Icon(Icons.Filled.PlayArrow, "Play", modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Browse All", fontWeight = FontWeight.SemiBold)
                }
                OutlinedButton(
                    onClick = onSearch,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, VinColors.CardBorder),
                    modifier = Modifier.height(44.dp)
                ) {
                    Icon(Icons.Filled.Search, "Search", tint = VinColors.TextSecondary)
                    Spacer(Modifier.width(6.dp))
                    Text("Search", color = VinColors.TextSecondary)
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, onSeeAll: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = VinColors.TextPrimary)
        TextButton(onClick = onSeeAll) {
            Text("See All", color = VinColors.Accent, fontSize = 13.sp)
        }
    }
}

@Composable
private fun VODCategoryRow(
    title: String,
    items: List<VODItem>,
    onItemClick: (VODItem) -> Unit,
    isHero: Boolean = false
) {
    if (items.isEmpty()) return
    Column {
        SectionHeader(title, onSeeAll = { })
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(items) { item ->
                if (isHero) HeroCard(item, onClick = { onItemClick(item) })
                else PosterCard(item, onClick = { onItemClick(item) })
            }
        }
    }
}

@Composable
private fun HeroCard(item: VODItem, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .width(260.dp)
            .height(150.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(VinColors.SurfaceCard)
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = item.backdropUrl.ifEmpty { item.posterUrl },
            contentDescription = item.title,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                        startY = 0.6f
                    )
                )
                .padding(12.dp),
            contentAlignment = Alignment.BottomStart
        ) {
            Column {
                Text(item.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 1)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (item.rating > 0) {
                        Text("★ ${item.rating}/10", color = VinColors.StarRating, fontSize = 11.sp)
                    }
                    if (item.year > 0) {
                        Text("${item.year}", color = VinColors.TextSecondary, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun PosterCard(item: VODItem, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(140.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .width(140.dp)
                .height(200.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(VinColors.SurfaceCard)
        ) {
            AsyncImage(
                model = item.posterUrl,
                contentDescription = item.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            if (item.rating > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                        .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text("★ ${item.rating}", color = VinColors.StarRating, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(item.title, color = VinColors.TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        if (item.year > 0) {
            Text("${item.year}", color = VinColors.TextTertiary, fontSize = 11.sp)
        }
    }
}

@Composable
private fun MovieCard(item: VODItem, onClick: () -> Unit) {
    PosterCard(item, onClick)
}

@Composable
private fun SeriesCard(item: VODItem, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(140.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .width(140.dp)
                .height(200.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(VinColors.SurfaceCard)
        ) {
            AsyncImage(
                model = item.posterUrl,
                contentDescription = item.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            // Season/Episode badge
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(6.dp)
                    .background(VinColors.Accent.copy(alpha = 0.9f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Text("S${item.seasonNumber}:E${item.episodeNumber}", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(item.title, color = VinColors.TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}
