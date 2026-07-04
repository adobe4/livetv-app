package com.viniptv.data.parser

import com.viniptv.data.model.Channel
import org.json.JSONObject

class XtreamParser {
    fun parseLiveStreams(json: String): List<Channel> {
        val channels = mutableListOf<Channel>()
        try {
            val jsonObj = JSONObject(json)
            val data = jsonObj.optJSONArray("live_streams") ?: return channels
            for (i in 0 until data.length()) {
                val item = data.getJSONObject(i)
                val chNum = item.optInt("num", 0)
                channels.add(Channel(
                    id = "xt_${item.optInt("stream_id")}",
                    number = if (chNum > 0) chNum else i + 1,
                    name = item.optString("name", "Channel ${i + 1}"),
                    logoUrl = item.optString("stream_icon", ""),
                    category = item.optString("category_name", "Uncategorized"),
                    url = item.optString("direct_source", ""),
                    epgChannelId = item.optString("epg_channel_id", ""),
                    epgChannelName = item.optString("name", "")
                ))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return channels
    }

    fun parseVodStreams(json: String, baseUrl: String): List<com.viniptv.data.model.VODItem> {
        val items = mutableListOf<com.viniptv.data.model.VODItem>()
        try {
            val jsonObj = JSONObject(json)
            val data = jsonObj.optJSONArray("vod_streams") ?: return items
            for (i in 0 until data.length()) {
                val item = data.getJSONObject(i)
                items.add(com.viniptv.data.model.VODItem(
                    id = "vod_${item.optInt("stream_id")}",
                    title = item.optString("name", "Movie ${i + 1}"),
                    plot = item.optString("plot", ""),
                    year = item.optInt("year", 0),
                    rating = item.optString("rating", "0").toFloatOrNull() ?: 0f,
                    posterUrl = item.optString("stream_icon", ""),
                    backdropUrl = item.optString("cover", ""),
                    genres = item.optString("genre", "").split(",").map { it.trim() }.filter { it.isNotEmpty() },
                    category = item.optString("category_name", ""),
                    url = "$baseUrl/movie/${item.optInt("stream_id")}.${item.optString("container_extension", "mp4")}",
                    duration = item.optString("duration", ""),
                    isMovie = true
                ))
            }
        } catch (e: Exception) {}
        return items
    }

    fun parseSeriesStreams(json: String, baseUrl: String): List<com.viniptv.data.model.VODItem> {
        val items = mutableListOf<com.viniptv.data.model.VODItem>()
        try {
            val jsonObj = JSONObject(json)
            val data = jsonObj.optJSONArray("series_streams") ?: return items
            for (i in 0 until data.length()) {
                val item = data.getJSONObject(i)
                items.add(com.viniptv.data.model.VODItem(
                    id = "series_${item.optInt("series_id")}",
                    title = item.optString("name", "Series ${i + 1}"),
                    plot = item.optString("plot", ""),
                    year = item.optInt("year", 0),
                    rating = item.optString("rating", "0").toFloatOrNull() ?: 0f,
                    posterUrl = item.optString("cover", ""),
                    backdropUrl = item.optString("backdrop", ""),
                    genres = item.optString("genre", "").split(",").map { it.trim() }.filter { it.isNotEmpty() },
                    category = item.optString("category_name", ""),
                    url = "$baseUrl/series/${item.optInt("series_id")}.ts",
                    isMovie = false
                ))
            }
        } catch (e: Exception) {}
        return items
    }
}
