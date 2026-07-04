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
            } else if (l.isNotEmpty() && !l.startsWith("#") && l.startsWith("http")) {
                val url = l
                if (currentExtInf != null) {
                    channels.add(parseExtInf(currentExtInf, url, channelNumber))
                    channelNumber++
                }
                currentExtInf = null
            } else if (l.isNotEmpty() && !l.startsWith("#") && currentExtInf != null) {
                // Non-http URL - still valid for some IPTV providers
                val url = l
                channels.add(parseExtInf(currentExtInf, url, channelNumber))
                channelNumber++
                currentExtInf = null
            }
        }
        return channels
    }

    private fun parseExtInf(extInf: String, url: String, number: Int): Channel {
        // Extract attributes
        val tvgName = extractAttribute(extInf, "tvg-name")
        val tvgId = extractAttribute(extInf, "tvg-id")
        val logo = extractAttribute(extInf, "tvg-logo") ?: ""
        val groupTitle = extractAttribute(extInf, "group-title") ?: "Uncategorized"
        val epgId = extractAttribute(extInf, "tvg-id") ?: ""

        // Get the display name - text after the last comma in EXTINF
        val displayName = extInf.substringAfterLast(",").trim()

        // Determine the best channel name
        // Priority: tvg-name > display name (if it's not just a number) > tvg-id > "Channel N"
        val name = when {
            !tvgName.isNullOrBlank() -> cleanName(tvgName)
            displayName.isNotBlank() && !displayName.all { it.isDigit() } -> cleanName(displayName)
            !tvgId.isNullOrBlank() -> cleanName(tvgId)
            else -> "Channel $number"
        }

        // Determine EPG channel ID
        val epgChannelId = epgId.ifBlank {
            tvgName?.takeIf { it.isNotBlank() } ?: displayName
        }

        return Channel(
            id = "ch_$number",
            number = number,
            name = name,
            logoUrl = logo,
            category = groupTitle.ifEmpty { "Uncategorized" },
            url = url,
            epgChannelId = epgChannelId,
            epgChannelName = tvgName ?: name,
            isAdult = extInf.contains("+18", ignoreCase = true) || extInf.contains("adult", ignoreCase = true)
        )
    }

    /**
     * Clean channel name - remove extra whitespace, quotes, etc.
     */
    private fun cleanName(name: String): String {
        return name
            .trim()
            .trim('"')
            .trim('\'')
            .replace(Regex("\\s+"), " ")  // collapse multiple spaces
            .ifBlank { "Unknown" }
    }

    private fun extractAttribute(line: String, attr: String): String? {
        val regex = """$attr\s*=\s*"([^"]*)"""".toRegex()
        return regex.find(line)?.groupValues?.getOrNull(1)?.takeIf { it.isNotEmpty() }
    }
}
