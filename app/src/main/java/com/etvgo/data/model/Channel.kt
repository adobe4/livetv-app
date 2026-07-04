package com.etvgo.data.model

data class Channel(
    val id: String,
    val name: String,
    val logoUrl: String = "",
    val category: String = "Uncategorized",
    val url: String,
    val epgChannelId: String = "",
    val isFavorite: Boolean = false,
    val isActive: Boolean = true
)
