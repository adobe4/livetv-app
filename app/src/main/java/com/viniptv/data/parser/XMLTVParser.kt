package com.viniptv.data.parser

import com.viniptv.data.model.EPGProgram
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader
import java.text.SimpleDateFormat
import java.util.*

object XMLTVParser {
    private val dateFormats = listOf(
        SimpleDateFormat("yyyyMMddHHmmss Z", Locale.US),
        SimpleDateFormat("yyyyMMddHHmmss ZZZZZ", Locale.US),
        SimpleDateFormat("yyyyMMddHHmmss", Locale.US)
    )

    fun parse(xmlContent: String): Map<String, List<EPGProgram>> {
        val epgMap = mutableMapOf<String, MutableList<EPGProgram>>()
        try {
            val factory = XmlPullParserFactory.newInstance()
            val parser = factory.newPullParser()
            parser.setInput(StringReader(xmlContent))
            var eventType = parser.eventType
            var currentChannelId = ""
            var currentTitle = ""
            var currentDesc = ""
            var currentStart = 0L
            var currentStop = 0L
            var inProgramme = false
            var inTitle = false
            var inDesc = false

            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        when (parser.name) {
                            "programme" -> {
                                inProgramme = true
                                currentChannelId = parser.getAttributeValue(null, "channel") ?: ""
                                currentStart = parseDate(parser.getAttributeValue(null, "start") ?: "")
                                currentStop = parseDate(parser.getAttributeValue(null, "stop") ?: "")
                                currentTitle = ""
                                currentDesc = ""
                            }
                            "title" -> if (inProgramme) inTitle = true
                            "desc" -> if (inProgramme) inDesc = true
                        }
                    }
                    XmlPullParser.TEXT -> {
                        if (inTitle) currentTitle = parser.text
                        if (inDesc) currentDesc = parser.text
                    }
                    XmlPullParser.END_TAG -> {
                        when (parser.name) {
                            "title" -> inTitle = false
                            "desc" -> inDesc = false
                            "programme" -> {
                                if (inProgramme && currentChannelId.isNotEmpty()) {
                                    val program = EPGProgram(
                                        channelId = currentChannelId,
                                        title = currentTitle.ifEmpty { "Unknown" },
                                        description = currentDesc,
                                        startTime = currentStart,
                                        endTime = currentStop
                                    )
                                    epgMap.getOrPut(currentChannelId) { mutableListOf() }.add(program)
                                }
                                inProgramme = false
                            }
                        }
                    }
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return epgMap
    }

    private fun parseDate(dateStr: String): Long {
        for (format in dateFormats) {
            try {
                return format.parse(dateStr)?.time ?: 0L
            } catch (_: Exception) {}
        }
        // Try cleaning the string
        val cleaned = dateStr.replace(Regex("([+-]\\d{2})(\\d{2})$"), "$1:$2")
        for (format in dateFormats) {
            try {
                return format.parse(cleaned)?.time ?: 0L
            } catch (_: Exception) {}
        }
        return 0L
    }
}
