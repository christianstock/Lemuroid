package com.swordfish.lemuroid.lib.library.metadata

import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.InputStream

class SkraperXmlParser {

    fun parse(inputStream: InputStream): List<SkraperGameEntry> {
        val factory = XmlPullParserFactory.newInstance().apply {
            isNamespaceAware = false
        }
        val parser = factory.newPullParser()
        parser.setInput(inputStream, "UTF-8")

        var eventType = parser.eventType
        while (eventType != XmlPullParser.START_TAG && eventType != XmlPullParser.END_DOCUMENT) {
            eventType = parser.next()
        }

        if (eventType != XmlPullParser.START_TAG) {
            return emptyList()
        }

        return when (parser.name) {
            "LaunchBox" -> parseLaunchBoxFormat(parser)
            "gameList" -> parseEmulationStationFormat(parser)
            "datafile" -> parseLogiqxFormat(parser)
            else -> emptyList()
        }
    }

    // 1. LaunchBox XML Format
    private fun parseLaunchBoxFormat(parser: XmlPullParser): List<SkraperGameEntry> {
        val entries = mutableListOf<SkraperGameEntry>()
        var eventType = parser.eventType

        var currentTitle: String? = null
        var currentAppPath: String? = null
        var currentDev: String? = null
        var currentPub: String? = null
        var currentNotes: String? = null
        var currentManual: String? = null
        var currentGenre: String? = null

        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                when (parser.name) {
                    "Game" -> {
                        currentTitle = null
                        currentAppPath = null
                        currentDev = null
                        currentPub = null
                        currentNotes = null
                        currentManual = null
                        currentGenre = null
                    }
                    "Title" -> currentTitle = parser.nextText().trim()
                    "ApplicationPath" -> currentAppPath = parser.nextText().trim()
                    "Developer" -> currentDev = parser.nextText().trim()
                    "Publisher" -> currentPub = parser.nextText().trim()
                    "Notes" -> currentNotes = parser.nextText().trim()
                    "ManualPath" -> currentManual = parser.nextText().trim()
                    "Genre" -> currentGenre = parser.nextText().trim()
                }
            } else if (eventType == XmlPullParser.END_TAG && parser.name == "Game") {
                if (!currentTitle.isNullOrEmpty() || !currentAppPath.isNullOrEmpty()) {
                    val romName = currentAppPath?.substringAfterLast('\\')?.substringAfterLast('/')
                    entries.add(
                        SkraperGameEntry(
                            title = currentTitle ?: romName ?: "Unknown",
                            romFileName = romName,
                            developer = currentDev?.ifBlank { null },
                            publisher = currentPub?.ifBlank { null },
                            description = currentNotes?.ifBlank { null },
                            manualPath = currentManual?.ifBlank { null },
                            genre = currentGenre?.ifBlank { null }
                        )
                    )
                }
            }
            eventType = parser.next()
        }
        return entries
    }

    // 2. EmulationStation (gameList) Format
    private fun parseEmulationStationFormat(parser: XmlPullParser): List<SkraperGameEntry> {
        val entries = mutableListOf<SkraperGameEntry>()
        var eventType = parser.eventType

        var currentTitle: String? = null
        var currentPath: String? = null
        var currentDesc: String? = null
        var currentDev: String? = null
        var currentPub: String? = null
        var currentRelease: String? = null
        var currentImage: String? = null
        var currentManual: String? = null
        var currentGenre: String? = null

        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                when (parser.name) {
                    "game" -> {
                        currentTitle = null
                        currentPath = null
                        currentDesc = null
                        currentDev = null
                        currentPub = null
                        currentRelease = null
                        currentImage = null
                        currentManual = null
                        currentGenre = null
                    }
                    "name" -> currentTitle = parser.nextText().trim()
                    "path" -> currentPath = parser.nextText().trim()
                    "desc" -> currentDesc = parser.nextText().trim()
                    "developer" -> currentDev = parser.nextText().trim()
                    "publisher" -> currentPub = parser.nextText().trim()
                    "releasedate" -> currentRelease = parser.nextText().trim()
                    "image", "box" -> currentImage = currentImage ?: parser.nextText().trim()
                    "manual" -> currentManual = parser.nextText().trim()
                    "genre" -> currentGenre = parser.nextText().trim()
                }
            } else if (eventType == XmlPullParser.END_TAG && parser.name == "game") {
                val romName = currentPath?.substringAfterLast('/')
                entries.add(
                    SkraperGameEntry(
                        title = currentTitle ?: romName ?: "Unknown",
                        romFileName = romName,
                        developer = currentDev?.ifBlank { null },
                        publisher = currentPub?.ifBlank { null },
                        description = currentDesc?.ifBlank { null },
                        releaseDate = currentRelease?.take(8)?.ifBlank { null },
                        coverFrontPath = currentImage?.ifBlank { null },
                        manualPath = currentManual?.ifBlank { null },
                        genre = currentGenre?.ifBlank { null }
                    )
                )
            }
            eventType = parser.next()
        }
        return entries
    }

    // 3. Logiqx XML DAT Format
    private fun parseLogiqxFormat(parser: XmlPullParser): List<SkraperGameEntry> {
        val entries = mutableListOf<SkraperGameEntry>()
        var eventType = parser.eventType

        var currentGameName: String? = null
        var currentDesc: String? = null
        var currentYear: String? = null
        var currentManufacturer: String? = null
        var currentRomName: String? = null

        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                when (parser.name) {
                    "game" -> {
                        currentGameName = parser.getAttributeValue(null, "name")
                        currentDesc = null
                        currentYear = null
                        currentManufacturer = null
                        currentRomName = null
                    }
                    "description" -> currentDesc = parser.nextText().trim()
                    "year" -> currentYear = parser.nextText().trim()
                    "manufacturer" -> currentManufacturer = parser.nextText().trim()
                    "rom" -> {
                        currentRomName = parser.getAttributeValue(null, "name")
                    }
                }
            } else if (eventType == XmlPullParser.END_TAG && parser.name == "game") {
                entries.add(
                    SkraperGameEntry(
                        title = currentGameName ?: "Unknown",
                        romFileName = currentRomName,
                        developer = currentManufacturer?.ifBlank { null },
                        publisher = currentManufacturer?.ifBlank { null },
                        description = currentDesc?.ifBlank { null },
                        releaseDate = currentYear?.ifBlank { null }
                    )
                )
            }
            eventType = parser.next()
        }
        return entries
    }
}
