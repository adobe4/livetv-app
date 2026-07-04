package com.etvgo.data.model

data class Category(
    val name: String,
    val channelCount: Int = 0,
    val isSelected: Boolean = false
)
