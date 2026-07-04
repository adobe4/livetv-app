package com.etvgo.network

import com.etvgo.data.model.Channel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class XtreamClient {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun authenticate(server: String, username: String, password: String): Result<XtreamAuthResponse> =
        withContext(Dispatchers.IO) {
            try {
                val baseUrl = server.trimEnd('/')
                val url = "$baseUrl/player_api.php?username=$username&password=$password"
                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()
                val body = response.body?.string() ?: throw Exception("Empty response")
                val json = JSONObject(body)

                if (json.optString("user_info").isNotEmpty()) {
                    val userInfo = json.getJSONObject("user_info")
                    val serverInfo = json.optJSONObject("server_info")
                    Result.success(XtreamAuthResponse(
                        username = userInfo.optString("username"),
                        password = userInfo.optString("password"),
                        auth = userInfo.optInt("auth", 0) == 1,
                        expires = userInfo.optString("exp_date", "0").toLongOrNull() ?: 0,
                        serverUrl = baseUrl,
                        playerApi = "$baseUrl/player_api.php?username=$username&password=$password",
                        liveStreamsUrl = "$baseUrl/live/$username/$password",
                        vodStreamsUrl = "$baseUrl/vod/$username/$password",
                        seriesUrl = "$baseUrl/series/$username/$password"
                    ))
                } else {
                    Result.failure(Exception("Invalid credentials"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}

data class XtreamAuthResponse(
    val username: String,
    val password: String,
    val auth: Boolean,
    val expires: Long,
    val serverUrl: String,
    val playerApi: String,
    val liveStreamsUrl: String,
    val vodStreamsUrl: String,
    val seriesUrl: String
)
