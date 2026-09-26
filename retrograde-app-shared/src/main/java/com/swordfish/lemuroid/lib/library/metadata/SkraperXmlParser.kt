package com.swordfish.lemuroid.lib.library.metadata

import android.util.Log
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
        var currentReleaseDate: String? = null
        var currentCoverFront: String? = null
        var currentCoverBack: String? = null
        var currentCartridge: String? = null

        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                when (parser.name.lowercase()) {
                    "game" -> {
                        currentTitle = null
                        currentAppPath = null
                        currentDev = null
                        currentPub = null
                        currentNotes = null
                        currentManual = null
                        currentGenre = null
                        currentReleaseDate = null
                        currentCoverFront = null
                        currentCoverBack = null
                        currentCartridge = null
                    }
                    "title" -> currentTitle = parser.nextText().trim()
                    "applicationpath" -> currentAppPath = parser.nextText().trim()
                    "developer" -> currentDev = parser.nextText().trim()
                    "publisher" -> currentPub = parser.nextText().trim()
                    "notes" -> currentNotes = parser.nextText().trim()
                    "manualpath" -> currentManual = parser.nextText().trim()
                    "genre" -> currentGenre = parser.nextText().trim()
                    "releasedate" -> currentReleaseDate = parser.nextText().trim()
                    "boxfront" -> currentCoverFront = parser.nextText().trim()
                    "boxback" -> currentCoverBack = parser.nextText().trim()
                    "cartridge", "support", "support2d", "cart2d", "cart3d" -> currentCartridge = currentCartridge ?: parser.nextText().trim()
                }
            } else if (eventType == XmlPullParser.END_TAG && parser.name.lowercase() == "game") {
                if (!currentTitle.isNullOrEmpty() || !currentAppPath.isNullOrEmpty()) {
                    val romName = currentAppPath?.substringAfterLast('\\')?.substringAfterLast('/')
                    entries.add(
                        SkraperGameEntry(
                            title = currentTitle ?: romName ?: "Unknown",
                            romFileName = romName,
                            developer = currentDev?.ifBlank { null },
                            publisher = currentPub?.ifBlank { null },
                            description = currentNotes?.ifBlank { null },
                            releaseDate = formatReleaseDate(currentReleaseDate),
                            coverFrontPath = currentCoverFront?.ifBlank { null },
                            coverBackPath = currentCoverBack?.ifBlank { null },
                            cartridgeImagePath = currentCartridge?.ifBlank { null },
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
        var currentImageBack: String? = null
        var currentImageCartridge: String? = null
        var currentManual: String? = null
        var currentGenre: String? = null

        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                when (parser.name.lowercase()) {
                    "game" -> {
                        currentTitle = null
                        currentPath = null
                        currentDesc = null
                        currentDev = null
                        currentPub = null
                        currentRelease = null
                        currentImage = null
                        currentImageBack = null
                        currentImageCartridge = null
                        currentManual = null
                        currentGenre = null
                    }
                    "name" -> currentTitle = parser.nextText().trim()
                    "path" -> currentPath = parser.nextText().trim()
                    "desc" -> currentDesc = parser.nextText().trim()
                    "developer" -> currentDev = parser.nextText().trim()
                    "publisher" -> currentPub = parser.nextText().trim()
                    "releasedate", "date" -> currentRelease = parser.nextText().trim()
                    "image", "box" -> currentImage = currentImage ?: parser.nextText().trim()
                    "imageback", "boxback" -> currentImageBack = currentImageBack ?: parser.nextText().trim()
                    "imagecartridge", "cartridge", "support", "support2d", "cart2d", "cart3d" -> currentImageCartridge = currentImageCartridge ?: parser.nextText().trim()
                    "manual", "manualpath" -> currentManual = currentManual ?: parser.nextText().trim()
                    "genre" -> currentGenre = parser.nextText().trim()
                }
            } else if (eventType == XmlPullParser.END_TAG && parser.name.lowercase() == "game") {
                val romName = currentPath?.substringAfterLast('/')
                Log.d("SkraperXmlParser", "EmulationStation: Game '${currentTitle}' found images - front: $currentImage, back: $currentImageBack, cartridge: $currentImageCartridge")
                entries.add(
                    SkraperGameEntry(
                        title = currentTitle ?: romName ?: "Unknown",
                        romFileName = romName,
                        developer = currentDev?.ifBlank { null },
                        publisher = currentPub?.ifBlank { null },
                        description = currentDesc?.ifBlank { null },
                        releaseDate = formatReleaseDate(currentRelease),
                        coverFrontPath = currentImage?.ifBlank { null },
                        coverBackPath = currentImageBack?.ifBlank { null },
                        cartridgeImagePath = currentImageCartridge?.ifBlank { null },
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
                when (parser.name.lowercase()) {
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
            } else if (eventType == XmlPullParser.END_TAG && parser.name.lowercase() == "game") {
                entries.add(
                    SkraperGameEntry(
                        title = currentGameName ?: "Unknown",
                        romFileName = currentRomName,
                        developer = currentManufacturer?.ifBlank { null },
                        publisher = currentManufacturer?.ifBlank { null },
                        description = currentDesc?.ifBlank { null },
                        releaseDate = formatReleaseDate(currentYear)
                    )
                )
            }
            eventType = parser.next()
        }
        return entries
    }

    /**
     * Converts raw date strings into standard ISO "YYYY-MM-DD" or "YYYY".
     * Handles "19930202T000000", "1993-02-02", "1993", etc.
     */
    private fun formatReleaseDate(rawDate: String?): String? {
        if (rawDate.isNullOrEmpty()) return null
        val clean = rawDate.trim()

        // Case 1: Skraper / EmulationStation format: "19930202T000000"
        if (clean.length >= 8 && clean.take(8).all { it.isDigit() }) {
            val year = clean.substring(0, 4)
            val month = clean.substring(4, 6)
            val day = clean.substring(6, 8)
            return "$year-$month-$day" // "1993-02-02"
        }

        // Case 2: Already standard ISO date "1993-02-02..."
        if (clean.length >= 10 && clean[4] == '-' && clean[7] == '-') {
            return clean.take(10)
        }

        // Case 3: Just the year "1993"
        if (clean.length >= 4 && clean.take(4).all { it.isDigit() }) {
            return clean.take(4)
        }

        return clean
    }
}
