package com.viniptv.data.model

data class VODItem(
    val id: String,
    val title: String,
    val plot: String = "",
    val year: Int = 0,
    val rating: Float = 0f,
    val posterUrl: String = "",
    val backdropUrl: String = "",
    val genres: List<String> = emptyList(),
    val category: String = "",
    val url: String = "",
    val duration: String = "",
    val isMovie: Boolean = true,
    val seasonNumber: Int = 0,
    val episodeNumber: Int = 0,
    val seriesId: String = "",
    val isFavorite: Boolean = false
)

data class VODCategory(
    val id: String,
    val name: String,
    val type: VODCategoryType,
    val items: List<VODItem> = emptyList()
)

enum class VODCategoryType {
    TRENDING, POPULAR, NEW_RELEASES, MOVIES, SERIES, CONTINUE_WATCHING, GENRE
}
