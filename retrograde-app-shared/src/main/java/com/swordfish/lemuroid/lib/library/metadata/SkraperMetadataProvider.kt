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

        // Find all matching entries
        val matchedEntries = entries.filter { entry ->
            val isMatchByRomFileName = entry.romFileName?.equals(romFileName, ignoreCase = true) == true
            val isMatchByRomFileNameNoExt = entry.romFileName?.equals(extensionlessName, ignoreCase = true) == true
            val isMatchByTitleExact = entry.title.equals(extensionlessName, ignoreCase = true)
            val isMatchByTitleCleaned = entry.title.cleanGameTitle().equals(cleanedRomName, ignoreCase = true)

            isMatchByRomFileName || isMatchByRomFileNameNoExt || isMatchByTitleExact || isMatchByTitleCleaned
        }

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
        Log.d("SkraperMetadata", "Final manuals after filtering: $allManuals")
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
            manualUrl = allManuals.firstOrNull(),
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
                    val parentDocId = if (docId.contains("/")) docId.substringBeforeLast("/") else docId.substringBefore(":") + ":"
                    val targetDocId = "$parentDocId/$cleanPath"
                    val fileUri = DocumentsContract.buildDocumentUriUsingTree(storageFile.uri, targetDocId)
                    fileUri.toString()
                } catch (e: Exception) {
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
                            scanPdfFolder(File(systemDir, folderName), zipFileName)?.let { mediaManuals.add(it) }
                            scanPdfFolder(File(mediaDir, folderName), zipFileName)?.let { mediaManuals.add(it) }
                        }
                    }

                    // 5. Check manual directly in ROM directory
                    scanPdfFolder(romDir, zipFileName)?.let { mediaManuals.add(it) }
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
            Log.d("SkraperMetadata", "scanPdfFolder: folder does not exist or is not directory: ${folder.absolutePath}")
            return null
        }

        Log.d("SkraperMetadata", "scanPdfFolder: scanning folder ${folder.absolutePath} for rom: $romNameWithoutExt")
        
        // List files in folder for debugging
        val filesInFolder = folder.listFiles()?.map { it.name } ?: emptyList()
        Log.d("SkraperMetadata", "scanPdfFolder: files in folder: $filesInFolder")

        val exactPdf = File(folder, "$romNameWithoutExt.pdf")
        if (exactPdf.exists() && exactPdf.isFile) {
            Log.d("SkraperMetadata", "scanPdfFolder: found exact PDF: ${exactPdf.absolutePath}")
            return Uri.fromFile(exactPdf).toString()
        }

        val manualPdf = File(folder, "manual.pdf")
        if (manualPdf.exists() && manualPdf.isFile) {
            Log.d("SkraperMetadata", "scanPdfFolder: found manual.pdf: ${manualPdf.absolutePath}")
            return Uri.fromFile(manualPdf).toString()
        }

        val nameWithoutRegion = romNameWithoutExt.replaceFirst(Regex("\\s*\\([^)]*\\)\\s*$"), "")
        if (nameWithoutRegion != romNameWithoutExt) {
            val altPdf = File(folder, "$nameWithoutRegion.pdf")
            if (altPdf.exists() && altPdf.isFile) {
                Log.d("SkraperMetadata", "scanPdfFolder: found region-stripped PDF: ${altPdf.absolutePath}")
                return Uri.fromFile(altPdf).toString()
            }
        }

        Log.d("SkraperMetadata", "scanPdfFolder: no PDF found in ${folder.absolutePath} for rom: $romNameWithoutExt")
        return null
    }

    fun clearCache() {
        parsedDirectoryCache.clear()
    }
}
