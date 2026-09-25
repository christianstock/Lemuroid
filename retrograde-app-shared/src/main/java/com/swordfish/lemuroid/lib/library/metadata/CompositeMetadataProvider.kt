package com.swordfish.lemuroid.lib.library.metadata

import android.util.Log
import com.swordfish.lemuroid.lib.storage.StorageFile

class CompositeMetadataProvider(
    private val skraperMetadataProvider: SkraperMetadataProvider,
    private val fallbackMetadataProvider: GameMetadataProvider
) : GameMetadataProvider {

    override suspend fun retrieveMetadata(
        storageFile: StorageFile,
        onLog: (String) -> Unit
    ): GameMetadata? {
        val skraperMetadata = runCatching {
            skraperMetadataProvider.retrieveMetadata(storageFile, onLog)
        }.getOrNull()

        val libretroMetadata = runCatching {
            fallbackMetadataProvider.retrieveMetadata(storageFile, onLog)
        }.getOrNull()

        if (skraperMetadata == null && libretroMetadata == null) {
            return null
        }

        val skraperOptions = skraperMetadata?.let { parseOptionsAndField(it) } ?: emptyMap()
        val libretroOptions = libretroMetadata?.let { parseOptionsAndField(it) } ?: emptyMap()

        // Combine and pick the longest date string (e.g. "1993-02-02" over "1993")
        val allDates = (skraperOptions["dates"].orEmpty() + libretroOptions["dates"].orEmpty())
            .filter { it.isNotBlank() }
            .distinct()
            .sortedByDescending { it.length }

        val allDevs = (skraperOptions["devs"].orEmpty() + libretroOptions["devs"].orEmpty())
            .filter { it.isNotBlank() }
            .distinct()

        val allPubs = (skraperOptions["pubs"].orEmpty() + libretroOptions["pubs"].orEmpty())
            .filter { it.isNotBlank() }
            .distinct()

        // --- ART SELECTION (MAX 2 UNIQUE, VALID IMAGES) ---
        // 1. Existing metadata image (if valid and non-blank)
        val existingArt = skraperMetadata?.thumbnail?.takeIf { isValidImageUri(it) }
        // 2. LibretroDB box art (if valid and non-blank)
        val libretroArt = libretroMetadata?.thumbnail?.takeIf { isValidImageUri(it) }

        Log.d("CompositeMetadata", "Skraper art: ${skraperMetadata?.thumbnail} (valid: ${existingArt != null})")
        Log.d("CompositeMetadata", "LibretroDB art: ${libretroMetadata?.thumbnail} (valid: ${libretroArt != null})")

        // Combine and keep distinct entries to guarantee at most 2 items with no blanks
        val allArts = listOfNotNull(existingArt, libretroArt).distinct()
        
        Log.d("CompositeMetadata", "Final allArts after filtering: $allArts")

        // Primary selected thumbnail is Libretro if available, otherwise existing
        val finalThumbnail = libretroArt ?: existingArt

        // Build debug OPTIONS string
        val mergedOptions = buildString {
            append("OPTIONS|")
            if (allDates.isNotEmpty()) append("dates:${allDates.joinToString("||")}|")
            if (allArts.isNotEmpty()) append("arts:${allArts.joinToString("||")}|")
            if (allDevs.isNotEmpty()) append("devs:${allDevs.joinToString("||")}|")
            if (allPubs.isNotEmpty()) append("pubs:${allPubs.joinToString("||")}")
        }.removeSuffix("|")

        val baseMetadata = skraperMetadata ?: libretroMetadata!!

        return baseMetadata.copy(
            name = skraperMetadata?.name?.ifBlank { null } ?: libretroMetadata?.name,
            system = skraperMetadata?.system?.ifBlank { null } ?: libretroMetadata?.system,
            romName = skraperMetadata?.romName?.ifBlank { null } ?: libretroMetadata?.romName,
            developer = skraperMetadata?.developer?.ifBlank { null }
                ?: libretroMetadata?.developer?.ifBlank { null }
                ?: allDevs.firstOrNull(),
            publisher = skraperMetadata?.publisher?.ifBlank { null }
                ?: libretroMetadata?.publisher?.ifBlank { null }
                ?: allPubs.firstOrNull(),
            thumbnail = finalThumbnail,
            thumbnailBack = null,
            releaseDate = allDates.firstOrNull()
                ?: skraperMetadata?.releaseDate?.ifBlank { null }
                ?: libretroMetadata?.releaseDate?.ifBlank { null },
            summary = skraperMetadata?.summary?.ifBlank { null } ?: libretroMetadata?.summary,
            country = skraperMetadata?.country?.ifBlank { null } ?: libretroMetadata?.country,
            debugInfo = mergedOptions
        )
    }

    private fun isValidImageUri(path: String?): Boolean {
        if (path.isNullOrBlank()) return false
        // Exclude dummy placeholders or unparsed relative strings
        return path.startsWith("http://", ignoreCase = true) ||
                path.startsWith("https://", ignoreCase = true) ||
                path.startsWith("file://", ignoreCase = true) ||
                path.startsWith("content://", ignoreCase = true)
    }

    private fun parseOptionsAndField(metadata: GameMetadata): Map<String, List<String>> {
        val result = mutableMapOf<String, MutableList<String>>()
        result["dates"] = mutableListOf()
        result["devs"] = mutableListOf()
        result["pubs"] = mutableListOf()
        result["arts"] = mutableListOf()

        metadata.releaseDate?.takeIf { it.isNotBlank() }?.let { result["dates"]?.add(it) }
        metadata.developer?.takeIf { it.isNotBlank() }?.let { result["devs"]?.add(it) }
        metadata.publisher?.takeIf { it.isNotBlank() }?.let { result["pubs"]?.add(it) }
        metadata.thumbnail?.takeIf { isValidImageUri(it) }?.let { result["arts"]?.add(it) }

        val debugInfo = metadata.debugInfo
        if (debugInfo != null && debugInfo.startsWith("OPTIONS|")) {
            val keys = listOf("dates", "devs", "pubs", "arts")
            for (key in keys) {
                // Match "key:" followed by anything that's not a single pipe (but allows ||)
                val pattern = Regex("$key:([^|]*(?:\\|\\|[^|]*)*)")
                val match = pattern.find(debugInfo)
                if (match != null) {
                    val values = match.groupValues[1].split("||").filter { it.isNotBlank() }
                    Log.d("CompositeMetadata", "parseOptionsAndField: key=$key, extracted values=$values from debugInfo")
                    if (key == "arts") {
                        // Filter dead URLs from arts
                        result[key]?.addAll(values.filter { isValidImageUri(it) })
                    } else {
                        result[key]?.addAll(values)
                    }
                }
            }
        }
        
        val finalResult = result.mapValues { (_, list) -> list.distinct() }
        Log.d("CompositeMetadata", "parseOptionsAndField final result: $finalResult")
        return finalResult
    }
}
