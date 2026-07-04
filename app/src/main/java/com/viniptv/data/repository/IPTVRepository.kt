package com.viniptv.data.repository

import com.viniptv.data.model.*
import com.viniptv.data.parser.M3UParser
import com.viniptv.data.parser.VODParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class IPTVRepository {
    private val _playlists = MutableStateFlow<List<PlaylistSource>>(emptyList())
    val playlists: StateFlow<List<PlaylistSource>> = _playlists.asStateFlow()

    private val _activePlaylistId = MutableStateFlow("")
    val activePlaylistId: StateFlow<String> = _activePlaylistId.asStateFlow()

    private val _channels = MutableStateFlow<List<Channel>>(emptyList())
    val channels: StateFlow<List<Channel>> = _channels.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private val _epgData = MutableStateFlow<Map<String, List<EPGProgram>>>(emptyMap())
    val epgData: StateFlow<Map<String, List<EPGProgram>>> = _epgData.asStateFlow()

    private val _vodMovies = MutableStateFlow<List<VODItem>>(emptyList())
    val vodMovies: StateFlow<List<VODItem>> = _vodMovies.asStateFlow()

    private val _vodSeries = MutableStateFlow<List<VODItem>>(emptyList())
    val vodSeries: StateFlow<List<VODItem>> = _vodSeries.asStateFlow()

    private val _favoriteChannelIds = MutableStateFlow<Set<String>>(emptySet())
    val favoriteChannelIds: StateFlow<Set<String>> = _favoriteChannelIds.asStateFlow()

    private val _favoriteVodIds = MutableStateFlow<Set<String>>(emptySet())
    val favoriteVodIds: StateFlow<Set<String>> = _favoriteVodIds.asStateFlow()

    init {
        // No sample data - user must add their own playlist
    }

    fun addPlaylist(source: PlaylistSource) {
        val current = _playlists.value.toMutableList()
        current.add(source)
        _playlists.value = current
        if (_activePlaylistId.value.isEmpty()) {
            _activePlaylistId.value = source.id
        }
        // In real app, would fetch and parse playlist URL here
        // For now, load sample data for demo
        loadSampleChannels()
        loadSampleEPG()
        loadSampleVOD()
        loadCategories()
    }

    fun removePlaylist(id: String) {
        _playlists.value = _playlists.value.filter { it.id != id }
        if (_activePlaylistId.value == id) {
            _activePlaylistId.value = _playlists.value.firstOrNull()?.id ?: ""
        }
    }

    fun setActivePlaylist(id: String) {
        _activePlaylistId.value = id
    }

    fun toggleFavorite(channelId: String) {
        val current = _favoriteChannelIds.value.toMutableSet()
        if (channelId in current) current.remove(channelId)
        else current.add(channelId)
        _favoriteChannelIds.value = current
    }

    fun toggleFavoriteVod(vodId: String) {
        val current = _favoriteVodIds.value.toMutableSet()
        if (vodId in current) current.remove(vodId)
        else current.add(vodId)
        _favoriteVodIds.value = current
    }

    fun getChannelsByCategory(categoryName: String): List<Channel> {
        return _channels.value.filter { it.category == categoryName }
    }

    fun getFavoriteChannels(): List<Channel> {
        return _channels.value.filter { it.id in _favoriteChannelIds.value }
    }

    fun searchChannels(query: String): List<Channel> {
        if (query.isBlank()) return emptyList()
        val q = query.lowercase()
        return _channels.value.filter {
            it.name.lowercase().contains(q) ||
            it.category.lowercase().contains(q)
        }
    }

    fun searchVod(query: String): List<VODItem> {
        if (query.isBlank()) return _vodMovies.value + _vodSeries.value
        val q = query.lowercase()
        return (_vodMovies.value + _vodSeries.value).filter {
            it.title.lowercase().contains(q) ||
            it.plot.lowercase().contains(q) ||
            it.genres.any { g -> g.lowercase().contains(q) }
        }
    }

    fun getVODCategories(): List<VODCategory> {
        val allItems = _vodMovies.value + _vodSeries.value
        val movies = _vodMovies.value
        val series = _vodSeries.value
        val categories = mutableListOf<VODCategory>()
        val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)

        // Trending (high rating)
        val trending = allItems.filter { it.rating > 0 }
            .sortedByDescending { it.rating }
            .take(10)
        if (trending.isNotEmpty()) categories.add(
            VODCategory("trending", "Trending Now", VODCategoryType.TRENDING, trending)
        )

        // Popular
        val popular = allItems.filter { it.rating >= 7.5f }
            .sortedByDescending { it.rating }
            .take(10)
        if (popular.isNotEmpty()) categories.add(
            VODCategory("popular", "Most Popular", VODCategoryType.POPULAR, popular)
        )

        // Continue Watching (would use actual progress in real app)
        val continueWatching = allItems.shuffled().take(5)
        if (continueWatching.isNotEmpty()) categories.add(
            VODCategory("continue", "Continue Watching", VODCategoryType.CONTINUE_WATCHING, continueWatching)
        )

        // New Releases
        val newMovies = movies.filter { it.year >= currentYear - 1 }
            .sortedByDescending { it.year }
            .take(10)
        if (newMovies.isNotEmpty()) categories.add(
            VODCategory("new", "New Releases", VODCategoryType.NEW_RELEASES, newMovies)
        )

        // Movies
        if (movies.isNotEmpty()) categories.add(
            VODCategory("movies", "Movies", VODCategoryType.MOVIES, movies.take(20))
        )

        // Series
        if (series.isNotEmpty()) categories.add(
            VODCategory("series", "TV Series", VODCategoryType.SERIES, series.take(20))
        )

        // Genre
        val genres = allItems.flatMap { it.genres }.distinct().sorted().take(8)
        for (genre in genres) {
            val genreItems = allItems.filter { genre in it.genres }.take(10)
            if (genreItems.isNotEmpty()) {
                categories.add(VODCategory("genre_$genre", genre, VODCategoryType.GENRE, genreItems))
            }
        }

        return categories
    }

    fun parseM3UContent(content: String) {
        val parsed = M3UParser.parse(content)
        _channels.value = parsed
        loadCategories()
        loadSampleEPG()
    }

    private fun loadSampleData() {
        // Only called explicitly when user chooses demo
        loadSampleChannels()
        loadSampleEPG()
        loadSampleVOD()
        loadCategories()
    }

    private fun loadCategories() {
        val cats = _channels.value
            .map { it.category }
            .distinct()
            .filter { it.isNotBlank() }
            .sorted()
        _categories.value = cats.mapIndexed { index, name ->
            Category(
                id = "cat_$index",
                name = name,
                type = CategoryType.LIVE,
                channelCount = _channels.value.count { it.category == name },
                isSelected = index == 0
            )
        }
    }

    fun loadDemoData() {
        loadSampleData()
        val demoPlaylist = PlaylistSource(
            id = "demo_1",
            name = "Demo IPTV",
            type = PlaylistType.M3U_URL,
            url = "https://demo-iptv.com/playlist.m3u",
            isActive = true
        )
        _playlists.value = listOf(demoPlaylist)
        _activePlaylistId.value = demoPlaylist.id
    }

    private fun loadSampleChannels() {
        _channels.value = sampleChannels
    }

    private fun loadSampleEPG() {
        _epgData.value = sampleEPG
    }

    private fun loadSampleVOD() {
        val movies = sampleMovies
        val series = sampleSeries
        _vodMovies.value = movies
        _vodSeries.value = series
    }

    companion object {
        val sampleChannels = listOf(
            Channel("ch_1", 1, "BBC One", "https://img.icons8.com/color/48/bbc.png", "News", "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.mp4", "bbc1.uk", "BBC One"),
            Channel("ch_2", 2, "CNN International", "https://img.icons8.com/color/48/cnn.png", "News", "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.mp4", "cnn.us", "CNN"),
            Channel("ch_3", 3, "Sky Sports", "https://img.icons8.com/color/48/football2.png", "Sports", "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.mp4", "skysports.uk", "Sky Sports"),
            Channel("ch_4", 4, "ESPN", "https://img.icons8.com/color/48/espn.png", "Sports", "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.mp4", "espn.us", "ESPN"),
            Channel("ch_5", 5, "HBO", "https://img.icons8.com/color/48/hbo.png", "Entertainment", "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.mp4", "hbo.us", "HBO"),
            Channel("ch_6", 6, "Netflix", "https://img.icons8.com/color/48/netflix.png", "Entertainment", "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.mp4", "netflix.us", "Netflix"),
            Channel("ch_7", 7, "Discovery Channel", "https://img.icons8.com/color/48/discovery.png", "Documentary", "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.mp4", "discovery.us", "Discovery"),
            Channel("ch_8", 8, "National Geographic", "https://img.icons8.com/color/48/natgeo.png", "Documentary", "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.mp4", "natgeo.us", "Nat Geo"),
            Channel("ch_9", 9, "Cartoon Network", "https://img.icons8.com/color/48/cartoon-network.png", "Kids", "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.mp4", "cartoon.us", "Cartoon Network"),
            Channel("ch_10", 10, "Disney Channel", "https://img.icons8.com/color/48/disney.png", "Kids", "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.mp4", "disney.us", "Disney"),
            Channel("ch_11", 11, "MTV", "https://img.icons8.com/color/48/mtv.png", "Music", "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.mp4", "mtv.us", "MTV"),
            Channel("ch_12", 12, "VH1", "https://img.icons8.com/color/48/vh1.png", "Music", "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.mp4", "vh1.us", "VH1"),
            Channel("ch_13", 13, "Fox News", "https://img.icons8.com/color/48/fox-news.png", "News", "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.mp4", "foxnews.us", "Fox News"),
            Channel("ch_14", 14, "Al Jazeera", "https://img.icons8.com/color/48/al-jazeera.png", "News", "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.mp4", "aljazeera.int", "Al Jazeera"),
            Channel("ch_15", 15, "Eurosport", "https://img.icons8.com/color/48/euro-football.png", "Sports", "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.mp4", "eurosport.uk", "Eurosport"),
            Channel("ch_16", 16, "Comedy Central", "https://img.icons8.com/color/48/comedy.png", "Entertainment", "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.mp4", "comedycentral.us", "Comedy Central"),
            Channel("ch_17", 17, "BBC Two", "https://img.icons8.com/color/48/bbc.png", "Entertainment", "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.mp4", "bbc2.uk", "BBC Two"),
            Channel("ch_18", 18, "ITV", "https://img.icons8.com/color/48/itv.png", "Entertainment", "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.mp4", "itv.uk", "ITV"),
            Channel("ch_19", 19, "Channel 4", "https://img.icons8.com/color/48/channel-4.png", "Entertainment", "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.mp4", "channel4.uk", "Channel 4"),
            Channel("ch_20", 20, "PBS", "https://img.icons8.com/color/48/pbs.png", "Documentary", "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.mp4", "pbs.us", "PBS"),
            Channel("ch_21", 21, "TNT", "https://img.icons8.com/color/48/tnt.png", "Movies", "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.mp4", "tnt.us", "TNT"),
            Channel("ch_22", 22, "AMC", "https://img.icons8.com/color/48/amc.png", "Movies", "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.mp4", "amc.us", "AMC"),
            Channel("ch_23", 23, "History Channel", "https://img.icons8.com/color/48/history.png", "Documentary", "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.mp4", "history.us", "History"),
            Channel("ch_24", 24, "Animal Planet", "https://img.icons8.com/color/48/animal-planet.png", "Documentary", "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.mp4", "animalplanet.us", "Animal Planet"),
            Channel("ch_25", 25, "Nickelodeon", "https://img.icons8.com/color/48/nickelodeon.png", "Kids", "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.mp4", "nick.us", "Nickelodeon"),
        )

        val sampleEPG: Map<String, List<EPGProgram>> = run {
            val cal = java.util.Calendar.getInstance()
            cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
            cal.set(java.util.Calendar.MINUTE, 0)
            cal.set(java.util.Calendar.SECOND, 0)
            cal.set(java.util.Calendar.MILLISECOND, 0)
            val todayStart = cal.timeInMillis
            val programs = mutableMapOf<String, MutableList<EPGProgram>>()
            val titles = listOf(
                "Morning News" to "Latest headlines and breaking news from around the world",
                "World Report" to "In-depth coverage of global events",
                "Sports Today" to "Today's sports highlights and live coverage",
                "Movie Time" to "Blockbuster movies and cinema classics",
                "Evening Drama" to "Award-winning drama series",
                "Late Show" to "Entertainment talk show with celebrity guests",
                "Documentary" to "Fascinating stories from around the world",
                "Kids Hour" to "Fun and educational programming for children",
                "Music Countdown" to "Top music videos and artist interviews",
                "Science Hour" to "Exploring the frontiers of science and technology",
                "Business Report" to "Market analysis and business news",
                "Cooking Show" to "Delicious recipes from top chefs",
                "Nature Walk" to "Exploring the beauty of the natural world",
                "Tech Review" to "Latest gadgets and technology reviews",
                "Comedy Night" to "Stand-up comedy and funny shows"
            )
            for (ch in sampleChannels) {
                val channelPrograms = mutableListOf<EPGProgram>()
                for (i in 0 until 12) {
                    val start = todayStart + i * 7200000L
                    val end = start + 7200000L
                    val (title, desc) = titles[(ch.number + i) % titles.size]
                    channelPrograms.add(EPGProgram(
                        channelId = ch.epgChannelId,
                        title = "$title (${ch.name} ${i+1})",
                        description = desc,
                        startTime = start,
                        endTime = end,
                        category = if (i % 3 == 0) "Live" else "Regular"
                    ))
                }
                programs[ch.epgChannelId] = channelPrograms
            }
            programs
        }

        val sampleMovies = listOf(
            VODItem("vod_1", "Inception", "A thief who steals corporate secrets through dream-sharing technology", 2010, 8.8f,
                "https://image.tmdb.org/t/p/w500/edv5CZvWj09upOsy2Y6IwDhK8bt.jpg",
                "https://image.tmdb.org/t/p/w1280/8ZTVqvKDQ8emSGUEMjsS4yHAwrp.jpg",
                listOf("Action", "Sci-Fi", "Thriller"), "Movies", "", "2h 28m"),
            VODItem("vod_2", "The Dark Knight", "When the menace known as the Joker wreaks havoc on Gotham", 2008, 9.0f,
                "https://image.tmdb.org/t/p/w500/qJ2tW6WMUDux911BytVLYQdhZT.jpg",
                "https://image.tmdb.org/t/p/w1280/nMKdUUepR0i5zn0y1T4CsSB5ez.jpg",
                listOf("Action", "Crime", "Drama"), "Movies", "", "2h 32m"),
            VODItem("vod_3", "Interstellar", "A team of explorers travel through a wormhole in space", 2014, 8.7f,
                "https://image.tmdb.org/t/p/w500/gEU2QniE6E77NI6lCU6MxlNBvIx.jpg",
                "https://image.tmdb.org/t/p/w1280/rAiYTfKGqDCRIIqo664sY9XZIvQ.jpg",
                listOf("Adventure", "Drama", "Sci-Fi"), "Movies", "", "2h 49m"),
            VODItem("vod_4", "The Matrix", "A computer hacker learns about the true nature of reality", 1999, 8.7f,
                "https://image.tmdb.org/t/p/w500/f89U3ADr1oiB1s9GkdPOEpXUk5H.jpg",
                "https://image.tmdb.org/t/p/w1280/7u3pxc0Kj4aD9G9EaZ8k3VZnRq.jpg",
                listOf("Action", "Sci-Fi"), "Movies", "", "2h 16m"),
            VODItem("vod_5", "Pulp Fiction", "The lives of two mob hitmen, a boxer, a gangster and his wife", 1994, 8.9f,
                "https://image.tmdb.org/t/p/w500/d5iIlFn5s0ImszYzBPb8JPIfbXD.jpg",
                "https://image.tmdb.org/t/p/w1280/suaEOtk1N1sgg2MTM7oZd2cfVp3.jpg",
                listOf("Crime", "Drama"), "Movies", "", "2h 34m"),
            VODItem("vod_6", "The Shawshank Redemption", "Two imprisoned men bond over a number of years", 1994, 9.3f,
                "https://image.tmdb.org/t/p/w500/9cjIGRQL1m4E87ZTV2h24C3VrM.jpg",
                "https://image.tmdb.org/t/p/w1280/9Xp3gwYUnPlKkmwoTpCO2FXhE.jpg",
                listOf("Drama"), "Movies", "", "2h 22m"),
            VODItem("vod_7", "Game of Thrones", "Nine noble families fight for control over the lands of Westeros", 2011, 9.2f,
                "https://image.tmdb.org/t/p/w500/u3bZgnGQ9T8s5WqFqJ7W2W8Qzq.jpg",
                "https://image.tmdb.org/t/p/w1280/gfJt5vjDZGjmGRvTQq7YrCqE4.jpg",
                listOf("Action", "Adventure", "Drama"), "Series", "", "", false, 1, 1, "series_1"),
            VODItem("vod_8", "Stranger Things", "When a young boy disappears, his mother and friends uncover a mystery", 2016, 8.7f,
                "https://image.tmdb.org/t/p/w500/49WJfeN0m4b2V2hW3KjK8X2WQj.jpg",
                "https://image.tmdb.org/t/p/w1280/56v2KjBlU4XaOv9DOi6F9Vj8P.jpg",
                listOf("Drama", "Fantasy", "Horror"), "Series", "", "", false, 1, 1, "series_2"),
            VODItem("vod_9", "Breaking Bad", "A high school chemistry teacher turned meth kingpin", 2008, 9.5f,
                "https://image.tmdb.org/t/p/w500/ggFHVNu6YYI5L3y2J0oK8VJ8Y.jpg",
                "https://image.tmdb.org/t/p/w1280/7z6N6ZkUeRTy9P1cXzuM1y6VXq.jpg",
                listOf("Crime", "Drama", "Thriller"), "Series", "", "", false, 1, 1, "series_3"),
            VODItem("vod_10", "The Crown", "Follows the political rivalries and romance of Queen Elizabeth II's reign", 2016, 8.7f,
                "https://image.tmdb.org/t/p/w500/1nPvG6gH4LSKj8q5kOkK8RXhW.jpg",
                "https://image.tmdb.org/t/p/w1280/cG0k5R9GJ5aGvX7c3k3T3Y7X6k.jpg",
                listOf("Drama", "History"), "Series", "", "", false, 1, 1, "series_4"),
            VODItem("vod_11", "Avatar: The Way of Water", "Jake Sully lives with his newfound family formed on the extrasolar moon Pandora", 2022, 7.8f,
                "https://image.tmdb.org/t/p/w500/t6HIqrRAclMCA60Ns2nUq3dDJ0.jpg",
                "https://image.tmdb.org/t/p/w1280/ovM4wV1jK8XjQXj7mRqT0X9g8.jpg",
                listOf("Action", "Adventure", "Fantasy"), "Movies", "", "3h 12m"),
            VODItem("vod_12", "Oppenheimer", "The story of American scientist J. Robert Oppenheimer and his role in the atomic bomb", 2023, 8.5f,
                "https://image.tmdb.org/t/p/w500/8Gxv8gSFCU0XGDykEGv7zR1n2ua.jpg",
                "https://image.tmdb.org/t/p/w1280/fk1WqMZqGJ7VKQmX5Q7z9p8k6y.jpg",
                listOf("Drama", "History", "Biography"), "Movies", "", "3h 0m"),
            VODItem("vod_13", "Dune: Part Two", "Paul Atreides unites with the Fremen to seek revenge against those who destroyed his family", 2024, 8.6f,
                "https://image.tmdb.org/t/p/w500/1pdfLvkbY9ohJlCjQH2CZjjYVvJ.jpg",
                "https://image.tmdb.org/t/p/w1280/8b8R8l88fje9n9Q8kY9K7z9p8k6y.jpg",
                listOf("Action", "Adventure", "Drama", "Sci-Fi"), "Movies", "", "2h 46m"),
            VODItem("vod_14", "The Last of Us", "After a global pandemic destroys civilization, a hardened survivor takes care of a 14-year-old girl", 2023, 8.8f,
                "https://image.tmdb.org/t/p/w500/uKvVjHNqB5VmOrdxqMw2uEjRy.jpg",
                "https://image.tmdb.org/t/p/w1280/t7m9U3FgKj8XjQXj7mRqT0X9g8.jpg",
                listOf("Action", "Adventure", "Drama"), "Series", "", "", false, 1, 1, "series_5"),
            VODItem("vod_15", "Wednesday", "Smart, sarcastic and a little dead inside, Wednesday Addams investigates a murder spree", 2022, 8.2f,
                "https://image.tmdb.org/t/p/w500/9PFonBhy4cQy7Qz9q3v7W8Qzq.jpg",
                "https://image.tmdb.org/t/p/w1280/56v2KjBlU4XaOv9DOi6F9Vj8P.jpg",
                listOf("Comedy", "Crime", "Fantasy"), "Series", "", "", false, 1, 1, "series_6"),
        )

        val sampleSeries = listOf(
            VODItem("series_1", "Game of Thrones", "Nine noble families fight for control over the lands of Westeros", 2011, 9.2f,
                "https://image.tmdb.org/t/p/w500/u3bZgnGQ9T8s5WqFqJ7W2W8Qzq.jpg",
                "https://image.tmdb.org/t/p/w1280/gfJt5vjDZGjmGRvTQq7YrCqE4.jpg",
                listOf("Action", "Adventure", "Drama"), "Series", "", "", false, 1, 1, "series_1"),
            VODItem("series_2", "Stranger Things", "When a young boy disappears, his mother and friends uncover a mystery", 2016, 8.7f,
                "https://image.tmdb.org/t/p/w500/49WJfeN0m4b2V2hW3KjK8X2WQj.jpg",
                "https://image.tmdb.org/t/p/w1280/56v2KjBlU4XaOv9DOi6F9Vj8P.jpg",
                listOf("Drama", "Fantasy", "Horror"), "Series", "", "", false, 1, 1, "series_2"),
            VODItem("series_3", "Breaking Bad", "A high school chemistry teacher turned meth kingpin", 2008, 9.5f,
                "https://image.tmdb.org/t/p/w500/ggFHVNu6YYI5L3y2J0oK8VJ8Y.jpg",
                "https://image.tmdb.org/t/p/w1280/7z6N6ZkUeRTy9P1cXzuM1y6VXq.jpg",
                listOf("Crime", "Drama", "Thriller"), "Series", "", "", false, 1, 1, "series_3"),
            VODItem("series_4", "The Crown", "Follows the political rivalries of Queen Elizabeth II's reign", 2016, 8.7f,
                "https://image.tmdb.org/t/p/w500/1nPvG6gH4LSKj8q5kOkK8RXhW.jpg",
                "https://image.tmdb.org/t/p/w1280/cG0k5R9GJ5aGvX7c3k3T3Y7X6k.jpg",
                listOf("Drama", "History"), "Series", "", "", false, 1, 1, "series_4"),
            VODItem("series_5", "The Last of Us", "After a global pandemic destroys civilization", 2023, 8.8f,
                "https://image.tmdb.org/t/p/w500/uKvVjHNqB5VmOrdxqMw2uEjRy.jpg",
                "https://image.tmdb.org/t/p/w1280/t7m9U3FgKj8XjQXj7mRqT0X9g8.jpg",
                listOf("Action", "Adventure", "Drama"), "Series", "", "", false, 1, 1, "series_5"),
            VODItem("series_6", "Wednesday", "Smart, sarcastic and a little dead inside", 2022, 8.2f,
                "https://image.tmdb.org/t/p/w500/9PFonBhy4cQy7Qz9q3v7W8Qzq.jpg",
                "https://image.tmdb.org/t/p/w1280/56v2KjBlU4XaOv9DOi6F9Vj8P.jpg",
                listOf("Comedy", "Crime", "Fantasy"), "Series", "", "", false, 1, 1, "series_6"),
            VODItem("series_7", "Succession", "The Roy family battles for control of a global media empire", 2018, 8.9f,
                "https://image.tmdb.org/t/p/w500/7qU0N1X5K8VjKQmX5Q7z9p8k6y.jpg",
                "https://image.tmdb.org/t/p/w1280/8b8R8l88fje9n9Q8kY9K7z9p8k6y.jpg",
                listOf("Drama", "Comedy"), "Series", "", "", false, 1, 1, "series_7"),
            VODItem("series_8", "The Boys", "A group of vigilantes set out to take down corrupt superheroes", 2019, 8.7f,
                "https://image.tmdb.org/t/p/w500/2tHizB4oG8V7Qz9q3v7W8Qzq.jpg",
                "https://image.tmdb.org/t/p/w1280/56v2KjBlU4XaOv9DOi6F9Vj8P.jpg",
                listOf("Action", "Comedy", "Crime"), "Series", "", "", false, 1, 1, "series_8"),
        )
    }
}
