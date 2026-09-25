package com.swordfish.lemuroid.lib.library.metadata

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import android.util.Log
import com.swordfish.lemuroid.common.kotlin.cleanGameTitle
import com.swordfish.lemuroid.lib.storage.StorageFile
import java.io.File
import java.io.InputStream

class SkraperMetadataProvider(
    private val appContext: Context,
    private val xmlParser: SkraperXmlParser = SkraperXmlParser()
) : GameMetadataProvider {

    private val parsedDirectoryCache = mutableMapOf<String, List<SkraperGameEntry>>()

    override suspend fun retrieveMetadata(
        storageFile: StorageFile,
        onLog: (String) -> Unit
    ): GameMetadata? {
        val entries = getOrParseEntriesFile(storageFile)
        val romFileName = storageFile.name
        val extensionlessName = storageFile.extensionlessName
        val cleanedRomName = extensionlessName.cleanGameTitle()

        // Find all matching entries
        val matchedEntries = entries.filter { entry ->
            val isMatchByRomFileName = entry.romFileName?.equals(romFileName, ignoreCase = true) == true
            val isMatchByRomFileNameNoExt = entry.romFileName?.equals(extensionlessName, ignoreCase = true) == true
            val isMatchByTitleExact = entry.title.equals(extensionlessName, ignoreCase = true)
            val isMatchByTitleCleaned = entry.title.cleanGameTitle().equals(cleanedRomName, ignoreCase = true)

            isMatchByRomFileName || isMatchByRomFileNameNoExt || isMatchByTitleExact || isMatchByTitleCleaned
        }
        
        if (matchedEntries.isEmpty()) return null

        // Collect all non-blank unique values for each field
        val releaseDates = matchedEntries.mapNotNull { it.releaseDate?.takeIf { d -> d.isNotEmpty() } }.distinct()
        val thumbnails = matchedEntries.mapNotNull { it.coverFrontPath?.takeIf { t -> t.isNotEmpty() && isValidImageUri(t) } }.distinct()
        val developers = matchedEntries.mapNotNull { it.developer?.takeIf { d -> d.isNotEmpty() } }.distinct()
        val publishers = matchedEntries.mapNotNull { it.publisher?.takeIf { p -> p.isNotEmpty() } }.distinct()
        
        // Log all URLs found
        matchedEntries.forEach { entry ->
            val urlStatus = when {
                entry.coverFrontPath.isNullOrBlank() -> "BLANK"
                isValidImageUri(entry.coverFrontPath) -> "VALID"
                else -> "INVALID"
            }
            Log.d("SkraperMetadata", "Entry '${entry.title}': image=$urlStatus ${entry.coverFrontPath} date=${entry.releaseDate}")
        }
        Log.d("SkraperMetadata", "Final thumbnails after filtering: $thumbnails")
        Log.d("SkraperMetadata", "Final releaseDates after filtering: $releaseDates")

        // Store all options in debugInfo
        val optionsInfo = buildString {
            append("OPTIONS|")
            if (releaseDates.isNotEmpty()) append("dates:${releaseDates.joinToString("||")}|")
            if (thumbnails.isNotEmpty()) append("arts:${thumbnails.joinToString("||")}|")
            if (developers.isNotEmpty()) append("devs:${developers.joinToString("||")}|")
            if (publishers.isNotEmpty()) append("pubs:${publishers.joinToString("||")}")
        }

        return GameMetadata(
            name = matchedEntries.first().title,
            system = storageFile.systemID?.dbname,
            romName = matchedEntries.first().romFileName ?: storageFile.name,
            developer = developers.firstOrNull(),
            publisher = publishers.firstOrNull(),
            thumbnail = thumbnails.firstOrNull(),
            thumbnailBack = null,
            releaseDate = releaseDates.firstOrNull(),
            summary = matchedEntries.mapNotNull { it.description?.takeIf { d -> d.isNotEmpty() } }.distinct().firstOrNull(),
            country = extractCountryFromFileName(romFileName),
            debugInfo = optionsInfo
        )
    }

    private fun getOrParseEntriesFile(storageFile: StorageFile): List<SkraperGameEntry> {
        val uri = storageFile.uri

        return when (uri.scheme) {
            "file" -> {
                val file = File(uri.path ?: return emptyList())
                val dir = file.parentFile ?: return emptyList()
                parsedDirectoryCache.getOrPut(dir.absolutePath) {
                    scanAndParseDirectoryFiles(dir)
                }
            }
            "content" -> {
                val cacheKey = uri.toString().substringBeforeLast("%2F").substringBeforeLast("/")
                parsedDirectoryCache.getOrPut(cacheKey) {
                    scanAndParseContentDirectory(uri)
                }
            }
            else -> emptyList()
        }
    }

    private fun scanAndParseContentDirectory(romUri: Uri): List<SkraperGameEntry> {
        val results = mutableListOf<SkraperGameEntry>()

        try {
            if (!DocumentsContract.isDocumentUri(appContext, romUri)) return results

            val docId = DocumentsContract.getDocumentId(romUri)
            val parentDocId = if (docId.contains("/")) {
                docId.substringBeforeLast("/")
            } else if (docId.contains(":")) {
                val split = docId.split(":")
                val path = split.getOrNull(1) ?: ""
                if (path.contains("/")) {
                    "${split[0]}:${path.substringBeforeLast("/")}"
                } else {
                    "${split[0]}:"
                }
            } else {
                return results
            }

            val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(romUri, parentDocId)
            val projection = arrayOf(
                DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                DocumentsContract.Document.COLUMN_DISPLAY_NAME
            )

            appContext.contentResolver.query(childrenUri, projection, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
                val idIndex = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID)

                while (cursor.moveToNext()) {
                    val name = cursor.getString(nameIndex) ?: continue
                    val childDocId = cursor.getString(idIndex) ?: continue

                    if (name.endsWith(".xml", ignoreCase = true) || name.endsWith(".dat", ignoreCase = true)) {
                        val fileUri = DocumentsContract.buildDocumentUriUsingTree(romUri, childDocId)
                        runCatching {
                            appContext.contentResolver.openInputStream(fileUri)?.use { stream ->
                                results.addAll(xmlParser.parse(stream))
                            }
                        }
                    }
                }
            }
        } catch (_: Exception) {
            // Silently handle read exceptions
        }

        return results
    }

    private fun scanAndParseDirectoryFiles(dir: File): List<SkraperGameEntry> {
        val results = mutableListOf<SkraperGameEntry>()
        val files = dir.listFiles() ?: return results
        val xmlFiles = files.filter { f ->
            f.isFile && (f.extension.equals("xml", ignoreCase = true) || f.extension.equals("dat", ignoreCase = true))
        }

        for (f in xmlFiles) {
            runCatching {
                f.inputStream().use { stream ->
                    results.addAll(xmlParser.parse(stream))
                }
            }
        }
        return results
    }

    private fun extractCountryFromFileName(fileName: String): String? {
        val regex = Regex("\\((.*?)\\)")
        return regex.find(fileName)?.groupValues?.get(1)
    }

    private fun isValidImageUri(path: String?): Boolean {
        if (path.isNullOrBlank()) return false
        return path.startsWith("http://", ignoreCase = true) ||
                path.startsWith("https://", ignoreCase = true) ||
                path.startsWith("file://", ignoreCase = true) ||
                path.startsWith("content://", ignoreCase = true)
    }

    fun clearCache() {
        parsedDirectoryCache.clear()
    }
}
