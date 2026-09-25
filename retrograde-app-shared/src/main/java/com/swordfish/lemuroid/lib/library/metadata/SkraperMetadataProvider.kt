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
        val thumbnailsFront = matchedEntries.mapNotNull { it.coverFrontPath?.takeIf { t -> t.isNotEmpty() && isValidImageUri(t) } }.distinct()
        val thumbnailsBack = matchedEntries.mapNotNull { it.coverBackPath?.takeIf { t -> t.isNotEmpty() && isValidImageUri(t) } }.distinct()
        val cartridges = matchedEntries.mapNotNull { it.cartridgeImagePath?.takeIf { t -> t.isNotEmpty() && isValidImageUri(t) } }.distinct()
        val developers = matchedEntries.mapNotNull { it.developer?.takeIf { d -> d.isNotEmpty() } }.distinct()
        val publishers = matchedEntries.mapNotNull { it.publisher?.takeIf { p -> p.isNotEmpty() } }.distinct()
        
        // Scan media folder for images
        Log.d("SkraperMetadata", "About to scan media folder for: $extensionlessName, uri=${storageFile.uri}, systemID=${storageFile.systemID?.dbname}")
        val (mediaFront, mediaBack, mediaCart) = scanMediaFolder(storageFile, extensionlessName)
        Log.d("SkraperMetadata", "Media folder scan completed: front=$mediaFront, back=$mediaBack, cart=$mediaCart")
        
        // Add media folder images to the lists
        val allThumbnailsFront = (thumbnailsFront + mediaFront).distinct()
        val allThumbnailsBack = (thumbnailsBack + mediaBack).distinct()
        val allCartridges = (cartridges + mediaCart).distinct()
        
        // Log all URLs found
        matchedEntries.forEach { entry ->
            val frontStatus = when {
                entry.coverFrontPath.isNullOrBlank() -> "BLANK"
                isValidImageUri(entry.coverFrontPath) -> "VALID"
                else -> "INVALID"
            }
            val backStatus = when {
                entry.coverBackPath.isNullOrBlank() -> "BLANK"
                isValidImageUri(entry.coverBackPath) -> "VALID"
                else -> "INVALID"
            }
            val cartStatus = when {
                entry.cartridgeImagePath.isNullOrBlank() -> "BLANK"
                isValidImageUri(entry.cartridgeImagePath) -> "VALID"
                else -> "INVALID"
            }
            Log.d("SkraperMetadata", "Entry '${entry.title}': front=$frontStatus ${entry.coverFrontPath} | back=$backStatus ${entry.coverBackPath} | cart=$cartStatus ${entry.cartridgeImagePath} | date=${entry.releaseDate}")
        }
        Log.d("SkraperMetadata", "Final thumbnails (front) after filtering: $allThumbnailsFront")
        Log.d("SkraperMetadata", "Final thumbnails (back) after filtering: $allThumbnailsBack")
        Log.d("SkraperMetadata", "Final cartridges after filtering: $allCartridges")
        Log.d("SkraperMetadata", "Final releaseDates after filtering: $releaseDates")

        // Store all options in debugInfo
        val optionsInfo = buildString {
            append("OPTIONS|")
            if (releaseDates.isNotEmpty()) append("dates:${releaseDates.joinToString("||")}|")
            if (allThumbnailsFront.isNotEmpty()) append("arts:${allThumbnailsFront.joinToString("||")}|")
            if (allThumbnailsBack.isNotEmpty()) append("backs:${allThumbnailsBack.joinToString("||")}|")
            if (allCartridges.isNotEmpty()) append("carts:${allCartridges.joinToString("||")}|")
            if (developers.isNotEmpty()) append("devs:${developers.joinToString("||")}|")
            if (publishers.isNotEmpty()) append("pubs:${publishers.joinToString("||")}")
        }

        return GameMetadata(
            name = matchedEntries.first().title,
            system = storageFile.systemID?.dbname,
            romName = matchedEntries.first().romFileName ?: storageFile.name,
            developer = developers.firstOrNull(),
            publisher = publishers.firstOrNull(),
            thumbnail = allThumbnailsFront.firstOrNull(),
            thumbnailBack = allThumbnailsBack.firstOrNull(),
            cartridgeImage = allCartridges.firstOrNull(),
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

    private fun scanMediaFolder(storageFile: StorageFile, romNameWithoutExt: String): Triple<List<String>, List<String>, List<String>> {
        val mediaFront = mutableListOf<String>()
        val mediaBack = mutableListOf<String>()
        val mediaCart = mutableListOf<String>()
        
        try {
            when (storageFile.uri.scheme) {
                "file" -> {
                    Log.d("SkraperMetadata", "Scanning media folder for file URI")
                    val romPath = storageFile.uri.path ?: return Triple(emptyList(), emptyList(), emptyList())
                    val romFile = File(romPath)
                    val zipFileName = romFile.nameWithoutExtension  // Get actual ZIP filename
                    val romDir = romFile.parentFile ?: return Triple(emptyList(), emptyList(), emptyList())
                    val mediaDir = File(romDir.parentFile, "media")
                    
                    Log.d("SkraperMetadata", "Media dir: ${mediaDir.absolutePath}, exists=${mediaDir.exists()}, zipFileName=$zipFileName")
                    
                    if (mediaDir.exists() && mediaDir.isDirectory) {
                        val systemName = storageFile.systemID?.dbname ?: "unknown"
                        val systemDir = File(mediaDir, systemName)
                        Log.d("SkraperMetadata", "System dir: ${systemDir.absolutePath}, exists=${systemDir.exists()}")
                        
                        scanImageFolder(File(systemDir, "box2dfront"), zipFileName)?.let { mediaFront.add(it) }
                        scanImageFolder(File(systemDir, "box2dback"), zipFileName)?.let { mediaBack.add(it) }
                        scanImageFolder(File(systemDir, "cartridge"), zipFileName)?.let { mediaCart.add(it) }
                    }
                }
                "content" -> {
                    Log.d("SkraperMetadata", "Scanning media folder for content URI")
                    // Navigate to parent directory using DocumentsContract
                    val docId = DocumentsContract.getDocumentId(storageFile.uri)
                    
                    // Extract ZIP filename from docId (e.g., "primary:Roms/gb/game.zip" -> "game")
                    val zipFileName = if (docId.contains("/")) {
                        docId.substringAfterLast("/").substringBeforeLast(".")
                    } else {
                        romNameWithoutExt  // Fallback to metadata name
                    }
                    
                    // Get the ROM folder path (e.g., "primary:Roms/gb")
                    val romFolderPath = if (docId.contains("/")) {
                        docId.substringBeforeLast("/")  // "primary:Roms/gb"
                    } else if (docId.contains(":")) {
                        val split = docId.split(":")
                        val path = split.getOrNull(1) ?: ""
                        if (path.contains("/")) {
                            "${split[0]}:${path.substringBeforeLast("/")}"
                        } else {
                            "${split[0]}:"
                        }
                    } else {
                        return Triple(emptyList(), emptyList(), emptyList())
                    }
                    
                    // Media folder is directly inside the ROM folder: "primary:Roms/gb/media"
                    val mediaFolderPath = "$romFolderPath/media"
                    
                    Log.d("SkraperMetadata", "romFolderPath=$romFolderPath, mediaFolderPath=$mediaFolderPath, zipFileName=$zipFileName")
                    
                    // Try to scan each image type folder
                    scanMediaFolderContent(storageFile.uri, mediaFolderPath, "box2dfront", zipFileName)?.let { mediaFront.add(it) }
                    scanMediaFolderContent(storageFile.uri, mediaFolderPath, "box2dback", zipFileName)?.let { mediaBack.add(it) }
                    scanMediaFolderContent(storageFile.uri, mediaFolderPath, "cartridge", zipFileName)?.let { mediaCart.add(it) }
                    
                    Log.d("SkraperMetadata", "Content URI media scan: front=$mediaFront, back=$mediaBack, cart=$mediaCart")
                }
                else -> {
                    Log.d("SkraperMetadata", "Unsupported URI scheme: ${storageFile.uri.scheme}")
                }
            }
        } catch (e: Exception) {
            Log.d("SkraperMetadata", "Media folder scan failed: ${e.message}", e)
        }
        
        return Triple(mediaFront, mediaBack, mediaCart)
    }
    
    private fun scanMediaFolderContent(baseUri: Uri, mediaFolderPath: String, subFolder: String, romNameWithoutExt: String): String? {
        try {
            val imageFolderPath = "$mediaFolderPath/$subFolder"
            Log.d("SkraperMetadata", "scanMediaFolderContent: subFolder=$subFolder, romNameWithoutExt='$romNameWithoutExt'")
            
            val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(baseUri, imageFolderPath)
            
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
                    
                    // Look for matching image file
                    val imageExts = listOf("png", "jpg", "jpeg", "bmp", "gif")
                    for (ext in imageExts) {
                        // Try exact match first
                        if (name.equals("$romNameWithoutExt.$ext", ignoreCase = true)) {
                            val fileUri = DocumentsContract.buildDocumentUriUsingTree(baseUri, childDocId)
                            Log.d("SkraperMetadata", "Found matching $subFolder image (exact): $name")
                            return fileUri.toString()
                        }
                        
                        // Try partial match: remove region codes like (USA), (EU), etc from ROM name
                        val nameWithoutRegion = romNameWithoutExt.replaceFirst(Regex("\\s*\\([^)]*\\)\\s*$"), "")
                        if (nameWithoutRegion != romNameWithoutExt && name.equals("$nameWithoutRegion.$ext", ignoreCase = true)) {
                            val fileUri = DocumentsContract.buildDocumentUriUsingTree(baseUri, childDocId)
                            Log.d("SkraperMetadata", "Found matching $subFolder image (no region): $name")
                            return fileUri.toString()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.d("SkraperMetadata", "scanMediaFolderContent($subFolder) failed: ${e.message}")
        }
        
        return null
    }
    
    private fun scanImageFolder(folder: File, romNameWithoutExt: String): String? {
        if (!folder.exists() || !folder.isDirectory) return null
        
        val files = folder.listFiles() ?: return null
        
        // Common image extensions
        val imageExts = listOf("png", "jpg", "jpeg", "bmp", "gif")
        
        // Look for exact name match with any image extension
        for (ext in imageExts) {
            val imageFile = File(folder, "$romNameWithoutExt.$ext")
            if (imageFile.exists() && imageFile.isFile) {
                return Uri.fromFile(imageFile).toString()
            }
        }
        
        return null
    }

    fun clearCache() {
        parsedDirectoryCache.clear()
    }
}
