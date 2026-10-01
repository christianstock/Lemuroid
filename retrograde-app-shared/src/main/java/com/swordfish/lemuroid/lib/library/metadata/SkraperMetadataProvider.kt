package com.swordfish.lemuroid.lib.library.metadata

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import android.util.Log
import com.swordfish.lemuroid.common.kotlin.cleanGameTitle
import com.swordfish.lemuroid.lib.storage.StorageFile
import java.io.File

class SkraperMetadataProvider(
    private val appContext: Context,
    private val xmlParser: SkraperXmlParser = SkraperXmlParser()
) : GameMetadataProvider {

    private val parsedDirectoryCache = mutableMapOf<String, List<SkraperGameEntry>>()

    data class MediaScanResult(
        val frontCovers: List<String> = emptyList(),
        val backCovers: List<String> = emptyList(),
        val cartridges: List<String> = emptyList(),
        val manuals: List<String> = emptyList()
    )

    override suspend fun retrieveMetadata(
        storageFile: StorageFile,
        onLog: (String) -> Unit
    ): GameMetadata? {
        val entries = getOrParseEntriesFile(storageFile)
        val romFileName = storageFile.name
        val extensionlessName = storageFile.extensionlessName
        val cleanedRomName = extensionlessName.cleanGameTitle()
        val normalizedRomName = normalizeForMatching(extensionlessName)

        // Try to find the original ZIP file in the ROM directory (if the current file is extracted)
        val zipFileName = findZipFileInDirectory(storageFile)
        val zipFileNameNormalized = zipFileName?.let { normalizeForMatching(it.substringBeforeLast(".")) }
        
        Log.d("SkraperMetadata", "DEBUG: Matching ROM: romFileName='$romFileName', extensionlessName='$extensionlessName', zipFileName='$zipFileName', normalized='$normalizedRomName'")
        Log.d("SkraperMetadata", "DEBUG: Total entries to search: ${entries.size}")

        // Find all matching entries - use normalized comparison for better matching
        val matchedEntries = entries.filter { entry ->
            val isMatchByRomFileName = entry.romFileName?.equals(romFileName, ignoreCase = true) == true
            val isMatchByRomFileNameNoExt = entry.romFileName?.equals(extensionlessName, ignoreCase = true) == true
            val isMatchByNormalizedRomName = entry.romFileName?.let { normalizeForMatching(it.substringBeforeLast(".")) }
                ?.equals(normalizedRomName, ignoreCase = true) == true
            
            // NEW: Match against ZIP filename if found
            val isMatchByZipFileName = zipFileName != null && 
                entry.romFileName?.equals(zipFileName, ignoreCase = true) == true
            val isMatchByNormalizedZipFileName = zipFileNameNormalized != null &&
                entry.romFileName?.let { normalizeForMatching(it.substringBeforeLast(".")) }
                    ?.equals(zipFileNameNormalized, ignoreCase = true) == true
            
            val isMatchByTitleExact = entry.title.equals(extensionlessName, ignoreCase = true)
            val isMatchByTitleCleaned = entry.title.cleanGameTitle().equals(cleanedRomName, ignoreCase = true)
            val isMatchByNormalizedTitle = normalizeForMatching(entry.title).equals(normalizedRomName, ignoreCase = true)

            val matched = isMatchByRomFileName || isMatchByRomFileNameNoExt || isMatchByNormalizedRomName || 
                         isMatchByZipFileName || isMatchByNormalizedZipFileName ||
                         isMatchByTitleExact || isMatchByTitleCleaned || isMatchByNormalizedTitle
            if (matched || entry.title.contains("Adventure Island", ignoreCase = true)) {
                Log.d("SkraperMetadata", "DEBUG ENTRY: title='${entry.title}', romFileName='${entry.romFileName}'")
                Log.d("SkraperMetadata", "  - romFileName match: $isMatchByRomFileName")
                Log.d("SkraperMetadata", "  - romFileNameNoExt match: $isMatchByRomFileNameNoExt")
                Log.d("SkraperMetadata", "  - normalizedRomName match: $isMatchByNormalizedRomName")
                Log.d("SkraperMetadata", "  - zipFileName match: $isMatchByZipFileName (zipFile='$zipFileName')")
                Log.d("SkraperMetadata", "  - normalizedZipFileName match: $isMatchByNormalizedZipFileName (normalized='$zipFileNameNormalized')")
                Log.d("SkraperMetadata", "  - titleExact match: $isMatchByTitleExact")
                Log.d("SkraperMetadata", "  - titleCleaned match: $isMatchByTitleCleaned (cleaned='${entry.title.cleanGameTitle()}')")
                Log.d("SkraperMetadata", "  - normalizedTitle match: $isMatchByNormalizedTitle (normalized='${normalizeForMatching(entry.title)}')")
                Log.d("SkraperMetadata", "  - MATCHED: $matched")
            }

            matched
        }
        Log.d("SkraperMetadata", "DEBUG: Matched entries: ${matchedEntries.size} of ${entries.size}")

        // Resolve relative paths from XML entries into absolute URIs
        val resolvedEntries = matchedEntries.map { entry ->
            entry.copy(
                coverFrontPath = resolveRelativePath(entry.coverFrontPath, storageFile),
                coverBackPath = resolveRelativePath(entry.coverBackPath, storageFile),
                cartridgeImagePath = resolveRelativePath(entry.cartridgeImagePath, storageFile),
                manualPath = resolveRelativePath(entry.manualPath, storageFile)
            )
        }

        // Collect all non-blank unique values for each field
        val releaseDates = resolvedEntries.mapNotNull { it.releaseDate?.takeIf { d -> d.isNotEmpty() } }.distinct()
        val thumbnailsFront = resolvedEntries.mapNotNull { it.coverFrontPath?.takeIf { t -> t.isNotEmpty() && isValidImageUri(t) } }.distinct()
        val thumbnailsBack = resolvedEntries.mapNotNull { it.coverBackPath?.takeIf { t -> t.isNotEmpty() && isValidImageUri(t) } }.distinct()
        val cartridges = resolvedEntries.mapNotNull { it.cartridgeImagePath?.takeIf { t -> t.isNotEmpty() && isValidImageUri(t) } }.distinct()
        val manualsFromXml = resolvedEntries.mapNotNull { it.manualPath?.takeIf { m -> m.isNotEmpty() && isValidUri(m) } }.distinct()
        val developers = resolvedEntries.mapNotNull { it.developer?.takeIf { d -> d.isNotEmpty() } }.distinct()
        val publishers = resolvedEntries.mapNotNull { it.publisher?.takeIf { p -> p.isNotEmpty() } }.distinct()

        // Scan media folder for images and manuals
        Log.d("SkraperMetadata", "About to scan media folder for: $extensionlessName, uri=${storageFile.uri}, systemID=${storageFile.systemID?.dbname}")
        val mediaResult = scanMediaFolder(storageFile, extensionlessName)
        Log.d("SkraperMetadata", "Media folder scan completed: front=${mediaResult.frontCovers}, back=${mediaResult.backCovers}, cart=${mediaResult.cartridges}, manuals=${mediaResult.manuals}")

        // Add media folder images and manuals to the lists
        val allThumbnailsFront = (thumbnailsFront + mediaResult.frontCovers).distinct()
        val allThumbnailsBack = (thumbnailsBack + mediaResult.backCovers).distinct()
        val allCartridges = (cartridges + mediaResult.cartridges).distinct()
        val allManuals = (manualsFromXml + mediaResult.manuals).distinct()

        Log.d("SkraperMetadata", "Final thumbnails (front) after filtering: $allThumbnailsFront")
        Log.d("SkraperMetadata", "Final thumbnails (back) after filtering: $allThumbnailsBack")
        Log.d("SkraperMetadata", "Final cartridges after filtering: $allCartridges")
        Log.d("SkraperMetadata", "MANUAL-DETECTOR: Final manuals after filtering: $allManuals")
        Log.d("SkraperMetadata", "MANUAL-DETECTOR: XML manuals: $manualsFromXml")
        Log.d("SkraperMetadata", "MANUAL-DETECTOR: Media folder manuals: ${mediaResult.manuals}")
        Log.d("SkraperMetadata", "Final releaseDates after filtering: $releaseDates")

        if (matchedEntries.isEmpty() && allThumbnailsFront.isEmpty() && allCartridges.isEmpty() && allManuals.isEmpty()) {
            return null
        }

        // Store all options in debugInfo
        val optionsInfo = buildString {
            append("OPTIONS|")
            if (releaseDates.isNotEmpty()) append("dates:${releaseDates.joinToString("||")}|")
            if (allThumbnailsFront.isNotEmpty()) append("arts:${allThumbnailsFront.joinToString("||")}|")
            if (allThumbnailsBack.isNotEmpty()) append("backs:${allThumbnailsBack.joinToString("||")}|")
            if (allCartridges.isNotEmpty()) append("carts:${allCartridges.joinToString("||")}|")
            if (allManuals.isNotEmpty()) append("manuals:${allManuals.joinToString("||")}|")
            if (developers.isNotEmpty()) append("devs:${developers.joinToString("||")}|")
            if (publishers.isNotEmpty()) append("pubs:${publishers.joinToString("||")}")
        }

        val primaryTitle = matchedEntries.firstOrNull()?.title ?: extensionlessName

        return GameMetadata(
            name = primaryTitle,
            system = storageFile.systemID?.dbname,
            romName = matchedEntries.firstOrNull()?.romFileName ?: storageFile.name,
            developer = developers.firstOrNull(),
            publisher = publishers.firstOrNull(),
            thumbnail = allThumbnailsFront.firstOrNull(),
            thumbnailBack = allThumbnailsBack.firstOrNull(),
            cartridgeImage = allCartridges.firstOrNull(),
            manualUrl = allManuals.firstOrNull().also { 
                Log.d("SkraperMetadata", "MANUAL-DETECTOR: STORING manualUrl=$it (from ${allManuals.size} options)")
            },
            releaseDate = releaseDates.firstOrNull(),
            summary = matchedEntries.mapNotNull { it.description?.takeIf { d -> d.isNotEmpty() } }.distinct().firstOrNull(),
            country = extractCountryFromFileName(romFileName),
            debugInfo = optionsInfo
        )
    }

    private fun resolveRelativePath(rawPath: String?, storageFile: StorageFile): String? {
        if (rawPath.isNullOrBlank()) return null
        if (isValidUri(rawPath)) return rawPath

        val cleanPath = rawPath.replace("\\", "/").removePrefix("./")

        return when (storageFile.uri.scheme) {
            "file" -> {
                val romPath = storageFile.uri.path ?: return null
                val romFile = File(romPath)
                val parentDir = romFile.parentFile ?: return null

                val targetFile = File(parentDir, cleanPath)
                if (targetFile.exists()) {
                    Uri.fromFile(targetFile).toString()
                } else {
                    // Try relative to grand-parent directory
                    val grandParent = parentDir.parentFile
                    if (grandParent != null) {
                        val altFile = File(grandParent, cleanPath)
                        if (altFile.exists()) Uri.fromFile(altFile).toString() else null
                    } else null
                }
            }
            "content" -> {
                try {
                    val docId = DocumentsContract.getDocumentId(storageFile.uri)
                    
                    // If cleanPath is an absolute Windows path (e.g., C:/Games/...), extract only the relative portion
                    val relativePath = if (cleanPath.contains(":") && cleanPath[1] == ':') {
                        // Absolute Windows path - find where "media" starts (for manuals/images)
                        val mediaIndex = cleanPath.lowercase().indexOf("media/")
                        if (mediaIndex >= 0) {
                            // Extract from "media" onwards
                            cleanPath.substring(mediaIndex)
                        } else {
                            // Fallback: extract everything after the last "Roms" folder
                            val lastRomsIndex = cleanPath.lowercase().lastIndexOf("roms")
                            if (lastRomsIndex >= 0) {
                                cleanPath.substring(lastRomsIndex + 5).removePrefix("/")
                            } else {
                                cleanPath
                            }
                        }
                    } else {
                        cleanPath
                    }
                    
                    Log.d("SkraperMetadata", "MANUAL-DETECTOR: resolveRelativePath: rawPath=$rawPath, cleanPath=$cleanPath, relativePath=$relativePath")
                    
                    val parentDocId = if (docId.contains("/")) docId.substringBeforeLast("/") else docId.substringBefore(":") + ":"
                    val targetDocId = "$parentDocId/$relativePath"
                    val fileUri = DocumentsContract.buildDocumentUriUsingTree(storageFile.uri, targetDocId)
                    fileUri.toString()
                } catch (e: Exception) {
                    Log.e("SkraperMetadata", "MANUAL-DETECTOR: resolveRelativePath error: ${e.message}")
                    null
                }
            }
            else -> null
        }
    }

    private fun getOrParseEntriesFile(storageFile: StorageFile): List<SkraperGameEntry> {
        val uri = storageFile.uri

        return when (uri.scheme) {
            "file" -> {
                val file = File(uri.path ?: return emptyList())
                val dir = file.parentFile ?: return emptyList()
                Log.d("SkraperMetadata", "CACHE_DEBUG: file scheme, cacheKey=${dir.absolutePath}")
                parsedDirectoryCache.getOrPut(dir.absolutePath) {
                    Log.d("SkraperMetadata", "CACHE_DEBUG: cache miss for $dir, scanning...")
                    scanAndParseDirectoryFiles(dir)
                }
            }
            "content" -> {
                // Extract the parent directory from content URI
                // URI format: content://com.android.externalstorage.documents/tree/primary%3ARoms/document/primary%3ARoms%2Fgb%2FRomName.zip
                // We need to extract: primary:Roms/gb (the system folder path)
                val uriString = uri.toString()
                // Find the document ID part: primary%3ARoms%2Fgb%2FRomName.zip
                val documentIdStart = uriString.indexOf("document/")
                if (documentIdStart < 0) {
                    Log.d("SkraperMetadata", "CACHE_DEBUG: could not find document/ in uri=$uriString")
                    return emptyList()
                }
                val documentId = uriString.substring(documentIdStart + 9) // "document/".length = 9
                // Decode URL-encoded characters: %3A -> :, %2F -> /
                val decodedPath = java.net.URLDecoder.decode(documentId, "UTF-8")
                // Get parent directory: primary:Roms/gb/RomName.zip -> primary:Roms/gb
                val parentPath = decodedPath.substringBeforeLast("/")
                // Use parent path as cache key so each system folder has its own cache
                Log.d("SkraperMetadata", "CACHE_DEBUG: content scheme, documentId=$documentId, decodedPath=$decodedPath, parentPath=$parentPath")
                parsedDirectoryCache.getOrPut(parentPath) {
                    Log.d("SkraperMetadata", "CACHE_DEBUG: cache miss for $parentPath, scanning...")
                    scanAndParseContentDirectory(uri)
                }
            }
            else -> {
                Log.d("SkraperMetadata", "CACHE_DEBUG: unknown scheme ${uri.scheme}")
                emptyList()
            }
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

    private fun isValidUri(path: String?): Boolean = isValidImageUri(path)

    private fun scanMediaFolder(storageFile: StorageFile, romNameWithoutExt: String): MediaScanResult {
        val mediaFront = mutableListOf<String>()
        val mediaBack = mutableListOf<String>()
        val mediaCart = mutableListOf<String>()
        val mediaManuals = mutableListOf<String>()

        val cartridgeFolderNames = listOf("support", "support2d", "cart2d", "cart3d", "cartridge")
        val manualFolderNames = listOf("manual", "manuals", "pdf", "pdfs")

        try {
            when (storageFile.uri.scheme) {
                "file" -> {
                    val romPath = storageFile.uri.path ?: return MediaScanResult()
                    val romFile = File(romPath)
                    val zipFileName = romFile.nameWithoutExtension
                    val romDir = romFile.parentFile ?: return MediaScanResult()
                    val mediaDir = File(romDir.parentFile, "media")

                    if (mediaDir.exists() && mediaDir.isDirectory) {
                        val systemName = storageFile.systemID?.dbname ?: "unknown"
                        val systemDir = File(mediaDir, systemName)

                        // 1. Front Cover
                        scanImageFolder(File(systemDir, "box2dfront"), zipFileName)?.let { mediaFront.add(it) }
                        scanImageFolder(File(mediaDir, "box2dfront"), zipFileName)?.let { mediaFront.add(it) }

                        // 2. Back Cover
                        scanImageFolder(File(systemDir, "box2dback"), zipFileName)?.let { mediaBack.add(it) }
                        scanImageFolder(File(mediaDir, "box2dback"), zipFileName)?.let { mediaBack.add(it) }

                        // 3. Cartridge / Support Art
                        cartridgeFolderNames.forEach { folderName ->
                            scanImageFolder(File(systemDir, folderName), zipFileName)?.let { mediaCart.add(it) }
                            scanImageFolder(File(mediaDir, folderName), zipFileName)?.let { mediaCart.add(it) }
                        }

                        // 4. Manuals inside media folders
                        manualFolderNames.forEach { folderName ->
                            scanPdfFolder(File(systemDir, folderName), zipFileName)?.let { 
                                Log.d("SkraperMetadata", "MANUAL-DETECTOR: Found manual in systemDir/$folderName: $it")
                                mediaManuals.add(it) 
                            }
                            scanPdfFolder(File(mediaDir, folderName), zipFileName)?.let { 
                                Log.d("SkraperMetadata", "MANUAL-DETECTOR: Found manual in mediaDir/$folderName: $it")
                                mediaManuals.add(it) 
                            }
                        }
                    }

                    // 5. Check manual directly in ROM directory
                    scanPdfFolder(romDir, zipFileName)?.let { 
                        Log.d("SkraperMetadata", "MANUAL-DETECTOR: Found manual in romDir: $it")
                        mediaManuals.add(it) 
                    }
                }
                "content" -> {
                    val docId = DocumentsContract.getDocumentId(storageFile.uri)

                    val zipFileName = if (docId.contains("/")) {
                        docId.substringAfterLast("/").substringBeforeLast(".")
                    } else {
                        romNameWithoutExt
                    }

                    val romFolderPath = if (docId.contains("/")) {
                        docId.substringBeforeLast("/")
                    } else if (docId.contains(":")) {
                        val split = docId.split(":")
                        val path = split.getOrNull(1) ?: ""
                        if (path.contains("/")) "${split[0]}:${path.substringBeforeLast("/")}" else "${split[0]}:"
                    } else {
                        return MediaScanResult()
                    }

                    val mediaFolderPath = "$romFolderPath/media"

                    // Front
                    scanMediaFolderContent(storageFile.uri, mediaFolderPath, "box2dfront", zipFileName)?.let { mediaFront.add(it) }

                    // Back
                    scanMediaFolderContent(storageFile.uri, mediaFolderPath, "box2dback", zipFileName)?.let { mediaBack.add(it) }

                    // Cartridges / Support
                    cartridgeFolderNames.forEach { folderName ->
                        scanMediaFolderContent(storageFile.uri, mediaFolderPath, folderName, zipFileName)?.let { mediaCart.add(it) }
                        scanMediaFolderContent(storageFile.uri, romFolderPath, folderName, zipFileName)?.let { mediaCart.add(it) }
                    }

                    // Manuals
                    manualFolderNames.forEach { folderName ->
                        scanMediaFolderContentForPdf(storageFile.uri, mediaFolderPath, folderName, zipFileName)?.let { mediaManuals.add(it) }
                        scanMediaFolderContentForPdf(storageFile.uri, romFolderPath, folderName, zipFileName)?.let { mediaManuals.add(it) }
                    }
                    scanMediaFolderContentForPdf(storageFile.uri, romFolderPath, "", zipFileName)?.let { mediaManuals.add(it) }
                }
            }
        } catch (e: Exception) {
            Log.d("SkraperMetadata", "Media folder scan failed: ${e.message}", e)
        }

        return MediaScanResult(
            frontCovers = mediaFront.distinct(),
            backCovers = mediaBack.distinct(),
            cartridges = mediaCart.distinct(),
            manuals = mediaManuals.distinct()
        )
    }

    private fun scanMediaFolderContent(baseUri: Uri, mediaFolderPath: String, subFolder: String, romNameWithoutExt: String): String? {
        try {
            val imageFolderPath = if (subFolder.isEmpty()) mediaFolderPath else "$mediaFolderPath/$subFolder"

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

                    val imageExts = listOf("png", "jpg", "jpeg", "bmp", "gif")
                    for (ext in imageExts) {
                        if (name.equals("$romNameWithoutExt.$ext", ignoreCase = true)) {
                            val fileUri = DocumentsContract.buildDocumentUriUsingTree(baseUri, childDocId)
                            return fileUri.toString()
                        }

                        val nameWithoutRegion = romNameWithoutExt.replaceFirst(Regex("\\s*\\([^)]*\\)\\s*$"), "")
                        if (nameWithoutRegion != romNameWithoutExt && name.equals("$nameWithoutRegion.$ext", ignoreCase = true)) {
                            val fileUri = DocumentsContract.buildDocumentUriUsingTree(baseUri, childDocId)
                            return fileUri.toString()
                        }
                    }
                }
            }
        } catch (_: Exception) {
        }
        return null
    }

    private fun scanMediaFolderContentForPdf(baseUri: Uri, mediaFolderPath: String, subFolder: String, romNameWithoutExt: String): String? {
        try {
            val pdfFolderPath = if (subFolder.isEmpty()) mediaFolderPath else "$mediaFolderPath/$subFolder"
            Log.d("SkraperMetadata", "scanMediaFolderContentForPdf: checking path $pdfFolderPath for rom: $romNameWithoutExt")

            val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(baseUri, pdfFolderPath)
            val projection = arrayOf(
                DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                DocumentsContract.Document.COLUMN_DISPLAY_NAME
            )

            appContext.contentResolver.query(childrenUri, projection, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
                val idIndex = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID)

                val filesFound = mutableListOf<String>()
                while (cursor.moveToNext()) {
                    val name = cursor.getString(nameIndex) ?: continue
                    val childDocId = cursor.getString(idIndex) ?: continue
                    filesFound.add(name)

                    if (name.endsWith(".pdf", ignoreCase = true)) {
                        val basePdfName = name.substringBeforeLast(".")
                        Log.d("SkraperMetadata", "scanMediaFolderContentForPdf: found PDF '$name', baseName='$basePdfName', romName='$romNameWithoutExt'")
                        
                        if (basePdfName.equals(romNameWithoutExt, ignoreCase = true) ||
                            basePdfName.equals("manual", ignoreCase = true) ||
                            romNameWithoutExt.startsWith(basePdfName, ignoreCase = true)) {
                            Log.d("SkraperMetadata", "scanMediaFolderContentForPdf: matched PDF '$name'")
                            val fileUri = DocumentsContract.buildDocumentUriUsingTree(baseUri, childDocId)
                            return fileUri.toString()
                        }
                    }
                }
                Log.d("SkraperMetadata", "scanMediaFolderContentForPdf: no matching PDF in path $pdfFolderPath, files found: $filesFound")
            }
        } catch (e: Exception) {
            Log.d("SkraperMetadata", "scanMediaFolderContentForPdf: exception scanning $mediaFolderPath/$subFolder: ${e.message}")
        }
        return null
    }

    private fun scanImageFolder(folder: File, romNameWithoutExt: String): String? {
        if (!folder.exists() || !folder.isDirectory) return null

        val imageExts = listOf("png", "jpg", "jpeg", "bmp", "gif")

        for (ext in imageExts) {
            val imageFile = File(folder, "$romNameWithoutExt.$ext")
            if (imageFile.exists() && imageFile.isFile) {
                return Uri.fromFile(imageFile).toString()
            }

            val nameWithoutRegion = romNameWithoutExt.replaceFirst(Regex("\\s*\\([^)]*\\)\\s*$"), "")
            if (nameWithoutRegion != romNameWithoutExt) {
                val altFile = File(folder, "$nameWithoutRegion.$ext")
                if (altFile.exists() && altFile.isFile) {
                    return Uri.fromFile(altFile).toString()
                }
            }
        }

        return null
    }

    private fun scanPdfFolder(folder: File, romNameWithoutExt: String): String? {
        if (!folder.exists() || !folder.isDirectory) {
            Log.d("SkraperMetadata", "MANUAL-DETECTOR: scanPdfFolder: folder does not exist or is not directory: ${folder.absolutePath}")
            return null
        }

        Log.d("SkraperMetadata", "MANUAL-DETECTOR: scanPdfFolder: scanning folder ${folder.absolutePath} for rom: $romNameWithoutExt")
        
        // List files in folder for debugging
        val filesInFolder = folder.listFiles()?.map { it.name } ?: emptyList()
        Log.d("SkraperMetadata", "MANUAL-DETECTOR: scanPdfFolder: files in folder: $filesInFolder")

        val exactPdf = File(folder, "$romNameWithoutExt.pdf")
        if (exactPdf.exists() && exactPdf.isFile) {
            val uri = Uri.fromFile(exactPdf).toString()
            Log.d("SkraperMetadata", "MANUAL-DETECTOR: scanPdfFolder: found exact PDF: ${exactPdf.absolutePath} → URI: $uri")
            return uri
        }

        val manualPdf = File(folder, "manual.pdf")
        if (manualPdf.exists() && manualPdf.isFile) {
            val uri = Uri.fromFile(manualPdf).toString()
            Log.d("SkraperMetadata", "MANUAL-DETECTOR: scanPdfFolder: found manual.pdf: ${manualPdf.absolutePath} → URI: $uri")
            return uri
        }

        val nameWithoutRegion = romNameWithoutExt.replaceFirst(Regex("\\s*\\([^)]*\\)\\s*$"), "")
        if (nameWithoutRegion != romNameWithoutExt) {
            val altPdf = File(folder, "$nameWithoutRegion.pdf")
            if (altPdf.exists() && altPdf.isFile) {
                val uri = Uri.fromFile(altPdf).toString()
                Log.d("SkraperMetadata", "MANUAL-DETECTOR: scanPdfFolder: found region-stripped PDF: ${altPdf.absolutePath} → URI: $uri")
                return uri
            }
        }

        Log.d("SkraperMetadata", "MANUAL-DETECTOR: scanPdfFolder: no PDF found in ${folder.absolutePath} for rom: $romNameWithoutExt")
        return null
    }

    fun clearCache() {
        parsedDirectoryCache.clear()
    }

    /**
     * Normalize a game name for matching by:
     * - Removing region information in parentheses
     * - Treating underscores and colons as equivalent
     * - Removing extra whitespace
     * - Converting to lowercase for comparison
     * 
     * Examples:
     * "Adventure Island (USA, Europe)" → "adventure island"
     * "Adventure Island II_ Aliens in Paradise" → "adventure island 2 aliens in paradise"
     * "Adventure Island II: Aliens in Paradise" → "adventure island 2 aliens in paradise"
     */
    private suspend fun findZipFileInDirectory(storageFile: StorageFile): String? {
       return try {
           when (storageFile.uri.scheme) {
               "file" -> {
                   val romPath = storageFile.uri.path ?: return null
                   val romFile = File(romPath)
                   val romDir = romFile.parentFile ?: return null
                    
                   // Get normalized name without extension for matching
                   val extensionlessName = storageFile.extensionlessName
                   val normalizedName = normalizeForMatching(extensionlessName)
                    
                   // Search for .zip files in the same directory
                   val zipFiles = romDir.listFiles { file ->
                       file.isFile && file.extension.equals("zip", ignoreCase = true)
                   } ?: emptyArray()
                    
                   // Find a ZIP that matches the ROM name (normalized)
                   val matchedZip = zipFiles.firstOrNull { zipFile ->
                       val zipNameNormalized = normalizeForMatching(zipFile.nameWithoutExtension)
                       zipNameNormalized.equals(normalizedName, ignoreCase = true)
                   }
                    
                   if (matchedZip != null) {
                       Log.d("SkraperMetadata", "DEBUG: Found matching ZIP file: ${matchedZip.name}")
                       matchedZip.name
                   } else {
                       Log.d("SkraperMetadata", "DEBUG: No matching ZIP file found in directory")
                       null
                   }
               }
               "content" -> {
                   // For content:// URIs, we'd need to scan the directory via DocumentsContract
                   // For now, return null - the mediaFolderScanning will handle images
                   Log.d("SkraperMetadata", "DEBUG: ZIP search not supported for content:// URIs yet")
                   null
               }
               else -> null
           }
       } catch (e: Exception) {
           Log.d("SkraperMetadata", "DEBUG: Error searching for ZIP file: ${e.message}")
           null
       }
    }

    private fun normalizeForMatching(input: String): String {
        return input
            .replace(Regex("\\s*\\([^)]*\\)\\s*"), " ") // Remove parentheses and their content
            .replace(Regex("\\s*\\[[^]]*\\]\\s*"), " ") // Remove brackets and their content
            .replace("_", " ") // Convert underscores to spaces
            .replace(":", " ") // Convert colons to spaces
            .replace(Regex("\\s+"), " ") // Normalize multiple spaces
            .trim()
            .lowercase()
    }
}
