package com.etvgo.network

import com.etvgo.data.parser.M3UParser
import com.etvgo.data.model.Channel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class M3ULoader {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    suspend fun loadFromUrl(url: String): Result<List<Channel>> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url(url)
                .header("User-Agent", "VLC/3.0.20 LibVLC/3.0.20")
                .build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: throw Exception("Empty response")
            Result.success(M3UParser.parse(body))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loadFromContent(content: String): List<Channel> {
        return M3UParser.parse(content)
    }
}
