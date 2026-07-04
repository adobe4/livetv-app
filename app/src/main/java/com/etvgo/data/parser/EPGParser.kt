package com.etvgo.data.parser

import com.etvgo.data.model.EPGProgram
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader
import java.text.SimpleDateFormat
import java.util.Locale

object EPGParser {
    private val dateFormats = listOf(
        "yyyyMMddHHmmss Z",
        "yyyyMMddHHmmss ZZZZZ",
        "yyyyMMddHHmmss zzzz"
    )

    fun parse(xml: String): Map<String, List<EPGProgram>> {
        val epgMap = mutableMapOf<String, MutableList<EPGProgram>>()

        try {
            val factory = XmlPullParserFactory.newInstance()
            val parser = factory.newPullParser()
            parser.setInput(StringReader(xml))

            var eventType = parser.eventType
            var currentChannelId = ""
            var currentTitle = ""
            var currentDesc = ""
            var currentStart = 0L
            var currentEnd = 0L
            var insideProgramme = false

            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        when (parser.name) {
                            "programme" -> {
                                insideProgramme = true
                                currentChannelId = parser.getAttributeValue(null, "channel") ?: ""
                                val start = parser.getAttributeValue(null, "start") ?: ""
                                val stop = parser.getAttributeValue(null, "stop") ?: ""
                                currentStart = parseDate(start)
                                currentEnd = parseDate(stop)
                                currentTitle = ""
                                currentDesc = ""
                            }
                            "title" -> { if (insideProgramme) currentTitle = parser.nextText() }
                            "desc" -> { if (insideProgramme) currentDesc = parser.nextText() }
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        if (parser.name == "programme" && insideProgramme) {
                            val program = EPGProgram(
                                channelId = currentChannelId,
                                title = currentTitle,
                                description = currentDesc,
                                startTime = currentStart,
                                endTime = currentEnd
                            )
                            epgMap.getOrPut(currentChannelId) { mutableListOf() }.add(program)
                            insideProgramme = false
                        }
                    }
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            // Return whatever we parsed so far
        }

        return epgMap
    }

    private fun parseDate(dateStr: String): Long {
        for (format in dateFormats) {
            try {
                val sdf = SimpleDateFormat(format, Locale.US)
                return sdf.parse(dateStr)?.time ?: 0L
            } catch (_: Exception) {}
        }
        return 0L
    }
}
