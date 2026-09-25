package com.swordfish.lemuroid.lib.library.metadata

import android.content.Context
import androidx.documentfile.provider.DocumentFile
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
        val entries = getOrParseEntriesForFile(storageFile)
        val romFileName = storageFile.name

        val matchedEntry = entries.firstOrNull { entry ->
            entry.romFileName?.equals(romFileName, ignoreCase = true) == true ||
            entry.romFileName?.equals(storageFile.extensionlessName, ignoreCase = true) == true ||
            entry.title.equals(storageFile.extensionlessName, ignoreCase = true)
        } ?: return null

        return GameMetadata(
            name = matchedEntry.title,
            system = storageFile.systemID?.dbname,
            romName = matchedEntry.romFileName ?: storageFile.name,
            developer = matchedEntry.developer,
            publisher = matchedEntry.publisher,
            thumbnail = matchedEntry.coverFrontPath,
            thumbnailBack = null,
            releaseDate = matchedEntry.releaseDate,
            summary = matchedEntry.description,
            country = extractCountryFromFileName(romFileName),
            debugInfo = "Skraper XML/DAT"
        )
    }

    fun matchMetadata(
        localRomFileName: String,
        xmlInputStream: InputStream
    ): GameMetadata? {
        val parsedEntries = xmlParser.parse(xmlInputStream)

        val matchedEntry = parsedEntries.firstOrNull { entry ->
            entry.romFileName?.equals(localRomFileName, ignoreCase = true) == true ||
            entry.title.equals(localRomFileName.substringBeforeLast("."), ignoreCase = true)
        } ?: return null

        return GameMetadata(
            name = matchedEntry.title,
            system = null,
            romName = matchedEntry.romFileName ?: localRomFileName,
            developer = matchedEntry.developer,
            publisher = matchedEntry.publisher,
            thumbnail = matchedEntry.coverFrontPath,
            thumbnailBack = null,
            releaseDate = matchedEntry.releaseDate,
            summary = matchedEntry.description,
            country = extractCountryFromFileName(localRomFileName),
            debugInfo = "Skraper Direct Stream"
        )
    }

    fun parseStream(xmlInputStream: InputStream): List<SkraperGameEntry> {
        return xmlParser.parse(xmlInputStream)
    }

    private fun getOrParseEntriesForFile(storageFile: StorageFile): List<SkraperGameEntry> {
        val uri = storageFile.uri
        val scheme = uri.scheme

        if (scheme == "file") {
            val file = File(uri.path ?: return emptyList())
            val dir = file.parentFile ?: return emptyList()
            val dirKey = dir.absolutePath
            return parsedDirectoryCache.getOrPut(dirKey) {
                scanAndParseDirectoryFiles(dir)
            }
        } else if (scheme == "content") {
            val docFile = runCatching { DocumentFile.fromSingleUri(appContext, uri) }.getOrNull()
            val parentDoc = docFile?.parentFile
            if (parentDoc != null) {
                val dirKey = parentDoc.uri.toString()
                return parsedDirectoryCache.getOrPut(dirKey) {
                    scanAndParseDocumentDirectory(parentDoc)
                }
            }
        }

        return emptyList()
    }

    private fun scanAndParseDirectoryFiles(dir: File): List<SkraperGameEntry> {
        val results = mutableListOf<SkraperGameEntry>()
        val files = dir.listFiles() ?: return results
        for (f in files) {
            if (f.isFile && (f.extension.equals("xml", ignoreCase = true) || f.extension.equals("dat", ignoreCase = true))) {
                runCatching {
                    f.inputStream().use { stream ->
                        results.addAll(xmlParser.parse(stream))
                    }
                }
            }
        }
        return results
    }

    private fun scanAndParseDocumentDirectory(dirDoc: DocumentFile): List<SkraperGameEntry> {
        val results = mutableListOf<SkraperGameEntry>()
        val files = dirDoc.listFiles()
        for (f in files) {
            if (f.isFile && (f.name?.endsWith(".xml", ignoreCase = true) == true || f.name?.endsWith(".dat", ignoreCase = true) == true)) {
                runCatching {
                    appContext.contentResolver.openInputStream(f.uri)?.use { stream ->
                        results.addAll(xmlParser.parse(stream))
                    }
                }
            }
        }
        return results
    }

    private fun extractCountryFromFileName(fileName: String): String? {
        val regex = Regex("\\((.*?)\\)")
        return regex.find(fileName)?.groupValues?.get(1)
    }

    fun clearCache() {
        parsedDirectoryCache.clear()
    }
}
