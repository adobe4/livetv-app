package com.viniptv.data.model

data class Category(
    val id: String,
    val name: String,
    val type: CategoryType = CategoryType.LIVE,
    val channelCount: Int = 0,
    val isSelected: Boolean = false
)

enum class CategoryType { LIVE, MOVIES, SERIES, ALL }
