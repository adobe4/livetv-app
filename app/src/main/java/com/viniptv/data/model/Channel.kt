package com.viniptv.data.model

data class Channel(
    val id: String,
    val number: Int = 0,
    val name: String,
    val logoUrl: String = "",
    val category: String = "Uncategorized",
    val url: String,
    val epgChannelId: String = "",
    val epgChannelName: String = "",
    val isFavorite: Boolean = false,
    val isAdult: Boolean = false,
    val isActive: Boolean = true,
    val hasArchive: Boolean = false,
    val archiveDays: Int = 0
) {
    val displayNumber: String get() = if (number > 0) number.toString() else ""
}
