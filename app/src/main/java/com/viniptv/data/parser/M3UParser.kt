package com.viniptv.data.parser

import com.viniptv.data.model.Channel
import java.io.BufferedReader
import java.io.StringReader

object M3UParser {
    fun parse(m3uContent: String): List<Channel> {
        val channels = mutableListOf<Channel>()
        val reader = BufferedReader(StringReader(m3uContent))
        var line: String?
        var channelNumber = 1
        var currentExtInf: String? = null

        while (reader.readLine().also { line = it } != null) {
            val l = line!!.trim()
            if (l.startsWith("#EXTINF:")) {
                currentExtInf = l
            } else if (l.isNotEmpty() && !l.startsWith("#")) {
                val url = l
                if (currentExtInf != null) {
                    channels.add(parseExtInf(currentExtInf, url, channelNumber))
                    channelNumber++
                }
                currentExtInf = null
            }
        }
        return channels
    }

    fun parseFromUrl(url: String, content: String): List<Channel> {
        return parse(content)
    }

    private fun parseExtInf(extInf: String, url: String, number: Int): Channel {
        val name = extractAttribute(extInf, "tvg-name")
            ?: extractAttribute(extInf, "tvg-id")
            ?: extInf.substringAfterLast(",").trim()
            .takeIf { it.isNotEmpty() }
            ?: "Channel $number"

        val logo = extractAttribute(extInf, "tvg-logo") ?: ""
        val groupTitle = extractAttribute(extInf, "group-title") ?: "Uncategorized"
        val epgId = extractAttribute(extInf, "tvg-id") ?: ""
        val epgName = extractAttribute(extInf, "tvg-name") ?: name
        val tvgShift = extractAttribute(extInf, "tvg-shift")?.toIntOrNull() ?: 0
        val radio = extInf.contains("radio=", ignoreCase = true)

        return Channel(
            id = "ch_$number",
            number = number,
            name = name,
            logoUrl = logo,
            category = groupTitle.ifEmpty { "Uncategorized" },
            url = url,
            epgChannelId = epgId,
            epgChannelName = epgName,
            isAdult = extInf.contains("+18", ignoreCase = true) || extInf.contains("adult", ignoreCase = true),
            hasArchive = tvgShift > 0
        )
    }

    private fun extractAttribute(line: String, attr: String): String? {
        val regex = """$attr="([^"]*)"""".toRegex()
        return regex.find(line)?.groupValues?.getOrNull(1)?.takeIf { it.isNotEmpty() }
    }
}
