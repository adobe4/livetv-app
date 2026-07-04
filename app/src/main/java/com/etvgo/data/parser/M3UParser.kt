package com.etvgo.data.parser

import com.etvgo.data.model.Channel
import java.net.URLDecoder

object M3UParser {
    fun parse(content: String): List<Channel> {
        val channels = mutableListOf<Channel>()
        val lines = content.split("\n").map { it.trim() }
        var i = 0

        while (i < lines.size) {
            val line = lines[i]
            if (line.startsWith("#EXTINF:")) {
                val name = Regex(",(.+)$").find(line)?.groupValues?.get(1)?.trim() ?: "Unknown"
                val logo = Regex("""tvg-logo="([^"]*)"""").find(line)?.groupValues?.get(1) ?: ""
                val category = Regex("""group-title="([^"]*)"""").find(line)?.groupValues?.get(1) ?: "Uncategorized"
                val tvgId = Regex("""tvg-id="([^"]*)"""").find(line)?.groupValues?.get(1) ?: ""
                val tvgName = Regex("""tvg-name="([^"]*)"""").find(line)?.groupValues?.get(1) ?: ""

                // Look for URL on next non-empty, non-comment line
                var url = ""
                for (j in i + 1 until lines.size) {
                    val next = lines[j]
                    if (next.isEmpty()) continue
                    if (next.startsWith("#")) continue
                    url = next
                    i = j
                    break
                }

                if (url.isNotEmpty()) {
                    channels.add(Channel(
                        id = "ch_${channels.size}",
                        name = if (tvgName.isNotEmpty()) tvgName else name,
                        logoUrl = logo,
                        category = category,
                        url = url,
                        epgChannelId = tvgId
                    ))
                }
            }
            i++
        }

        return channels
    }
}
