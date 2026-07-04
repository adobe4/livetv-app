package com.viniptv.data.parser

import com.viniptv.data.model.VODItem
import com.viniptv.data.model.VODCategory
import com.viniptv.data.model.VODCategoryType
import org.json.JSONObject

object VODParser {
    fun parseXtreamMovies(json: String, baseUrl: String): List<VODItem> {
        val items = mutableListOf<VODItem>()
        try {
            val jsonObj = JSONObject(json)
            val data = jsonObj.optJSONArray("data") ?: return items
            for (i in 0 until data.length()) {
                val movie = data.getJSONObject(i)
                items.add(VODItem(
                    id = "vod_${movie.optInt("stream_id")}",
                    title = movie.optString("name"),
                    plot = movie.optString("plot", ""),
                    year = movie.optInt("year", 0),
                    rating = movie.optString("rating", "0").toFloatOrNull() ?: 0f,
                    posterUrl = movie.optString("stream_icon", ""),
                    backdropUrl = movie.optString("cover", ""),
                    genres = movie.optString("genre", "").split(",").map { it.trim() }.filter { it.isNotEmpty() },
                    category = movie.optString("category_name", ""),
                    url = "$baseUrl/${movie.optInt("stream_id")}.ts",
                    duration = movie.optString("duration", ""),
                    isMovie = true
                ))
            }
        } catch (e: Exception) {}
        return items
    }

    fun parseXtreamSeries(json: String, baseUrl: String): List<VODItem> {
        val items = mutableListOf<VODItem>()
        try {
            val jsonObj = JSONObject(json)
            val data = jsonObj.optJSONArray("data") ?: return items
            for (i in 0 until data.length()) {
                val series = data.getJSONObject(i)
                items.add(VODItem(
                    id = "series_${series.optInt("series_id")}",
                    title = series.optString("name"),
                    plot = series.optString("plot", ""),
                    year = series.optInt("year", 0),
                    rating = series.optString("rating", "0").toFloatOrNull() ?: 0f,
                    posterUrl = series.optString("cover", ""),
                    backdropUrl = series.optString("backdrop", ""),
                    genres = series.optString("genre", "").split(",").map { it.trim() }.filter { it.isNotEmpty() },
                    category = series.optString("category_name", ""),
                    url = "$baseUrl/${series.optInt("series_id")}.ts",
                    isMovie = false
                ))
            }
        } catch (e: Exception) {}
        return items
    }

    fun categorizeMovies(items: List<VODItem>): List<VODCategory> {
        val categories = mutableListOf<VODCategory>()
        val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)

        // New Releases (last 2 years)
        val newReleases = items.filter { it.year >= currentYear - 2 && it.year > 0 }.sortedByDescending { it.year }
        if (newReleases.isNotEmpty()) categories.add(VODCategory("new", "New Releases", VODCategoryType.NEW_RELEASES, newReleases.take(20)))

        // Trending (sorted by rating)
        val trending = items.filter { it.rating > 0 }.sortedByDescending { it.rating }
        if (trending.isNotEmpty()) categories.add(VODCategory("trending", "Trending", VODCategoryType.TRENDING, trending.take(20)))

        // Popular (top rated)
        val popular = items.filter { it.rating > 7 }.sortedByDescending { it.rating }
        if (popular.isNotEmpty()) categories.add(VODCategory("popular", "Popular", VODCategoryType.POPULAR, popular.take(20)))

        // Movies (all movies sorted by year)
        val movies = items.filter { it.isMovie }.sortedByDescending { it.year }
        if (movies.isNotEmpty()) categories.add(VODCategory("movies", "Movies", VODCategoryType.MOVIES, movies.take(30)))

        // Series
        val series = items.filter { !it.isMovie }.sortedByDescending { it.year }
        if (series.isNotEmpty()) categories.add(VODCategory("series", "Series", VODCategoryType.SERIES, series.take(30)))

        // Genre-based
        val genres = items.flatMap { it.genres }.distinct().sorted().take(10)
        for (genre in genres) {
            val genreItems = items.filter { genre in it.genres }.sortedByDescending { it.year }.take(20)
            if (genreItems.isNotEmpty()) {
                categories.add(VODCategory("genre_$genre", genre, VODCategoryType.GENRE, genreItems))
            }
        }

        return categories
    }
}
