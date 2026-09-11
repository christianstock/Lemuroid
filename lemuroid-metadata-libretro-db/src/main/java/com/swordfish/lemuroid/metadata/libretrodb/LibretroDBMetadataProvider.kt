package com.swordfish.lemuroid.metadata.libretrodb

import com.swordfish.lemuroid.common.kotlin.cleanForArtUrl
import com.swordfish.lemuroid.common.kotlin.filterNullable
import com.swordfish.lemuroid.lib.library.GameSystem
import com.swordfish.lemuroid.lib.library.SystemID
import com.swordfish.lemuroid.lib.library.metadata.GameMetadata
import com.swordfish.lemuroid.lib.library.metadata.GameMetadataProvider
import com.swordfish.lemuroid.lib.storage.StorageFile
import com.swordfish.lemuroid.metadata.libretrodb.db.LibretroDBManager
import com.swordfish.lemuroid.metadata.libretrodb.db.LibretroDatabase
import com.swordfish.lemuroid.metadata.libretrodb.db.entity.LibretroRom

class LibretroDBMetadataProvider(private val ovgdbManager: LibretroDBManager) :
    GameMetadataProvider {
    companion object {
        private val THUMB_REPLACE = Regex("[&*/:`<>?\\\\|]")
    }

    override suspend fun retrieveMetadata(storageFile: StorageFile, onLog: (String) -> Unit): GameMetadata? {
        val db = ovgdbManager.dbInstance
        onLog("Metadata: Identifying [${storageFile.name}]")

        val metadata = runCatching {
            findByCRC(storageFile, db, onLog)
                ?: findBySerial(storageFile, db, onLog)
                ?: findByFilename(db, storageFile, onLog)
        }.getOrNull()
        
        if (metadata != null) {
            onLog("Metadata Match: ${metadata.name}")
            // RE-PROCESS WITH FILENAME CONTEXT
            val coverUrl = computeHighFidelityArtUrl(metadata, storageFile, onLog)
            val updatedMetadata = metadata.copy(
                thumbnail = coverUrl,
                debugInfo = (metadata.debugInfo ?: "") + "\nFinal Art URL: $coverUrl"
            )
            onLog("Final Art URL: $coverUrl")
            return updatedMetadata
        } else {
            onLog("Metadata FAIL: No DB match.")
        }
        
        return metadata
    }

    private fun convertToGameMetadata(rom: LibretroRom, method: String): GameMetadata {
        val system = GameSystem.findById(rom.system!!)
        val rawName = rom.name ?: ""
        
        val debugLines = mutableListOf<String>()
        debugLines.add("--- DB RECORD ---")
        debugLines.add("Method: $method")
        debugLines.add("DB Title: $rawName")
        debugLines.add("--------------------")

        return GameMetadata(
            name = rawName, 
            romName = rom.romName,
            thumbnail = null, // Will be computed with filename context
            system = rom.system,
            developer = rom.developer,
            publisher = null,
            thumbnailBack = null,
            releaseDate = null,
            summary = null,
            country = extractRegion(rawName),
            debugInfo = debugLines.joinToString("\n")
        )
    }

    private fun extractRegion(name: String?): String? {
        if (name == null) return null
        val regex = Regex("\\(([^)]+)\\)")
        return regex.findAll(name)
            .map { it.groupValues[1] }
            .filter { it.length <= 15 } // Allow En,Fr etc
            .joinToString(", ")
            .takeIf { it.isNotEmpty() }
    }

    private fun computeHighFidelityArtUrl(metadata: GameMetadata, file: StorageFile, onLog: (String) -> Unit): String? {
        val system = GameSystem.findById(metadata.system!!)
        val systemName = if (system.id == SystemID.MAME2003PLUS) "MAME" else system.libretroFullName
        val imageType = "Named_Boxarts"

        // 1. Clean the database name (strips hacks/versions)
        var artName = metadata.name!!.cleanForArtUrl()
        
        // 2. CONTEXT MERGE: If the cleaned name is missing a region, grab it from the filename
        val filenameRegion = extractRegion(file.name)
        if (!artName.contains("(") && filenameRegion != null) {
            onLog("Context: Merging region ($filenameRegion) from filename")
            artName = "$artName ($filenameRegion)"
        }

        // 3. Driver & Specific Fixes
        if (artName.contains("Driver", ignoreCase = true)) {
            artName = artName.replace("You Are the Wheelman", "You are the Wheelman", ignoreCase = true)
            artName = artName.replace("(Europe)", "(En,Fr,De,Es,It)", ignoreCase = true)
            artName = artName.replace("(USA, Europe)", "(USA) (En,Fr,De,Es,It)", ignoreCase = true)
        }

        // 4. Libretro strict character replacement
        val thumbName = artName.replace(THUMB_REPLACE, "_")
        
        // 5. URL Encoding (Preserving parentheses for Libretro server)
        val encodedSystem = systemName.replace(" ", "%20")
        val encodedName = thumbName
            .replace(" ", "%20")
            .replace("&", "%26")
            .replace("'", "%27")
            .replace("#", "%23")
        
        return "http://thumbnails.libretro.com/$encodedSystem/$imageType/$encodedName.png"
    }

    private suspend fun findByFilename(db: LibretroDatabase, file: StorageFile, onLog: (String) -> Unit): GameMetadata? {
        return db.gameDao().findByFileName(file.name)
            ?.filterNullable { extractGameSystem(it).scanOptions.scanByFilename }
            ?.let { convertToGameMetadata(it, "Filename") }
    }

    private suspend fun findByCRC(file: StorageFile, db: LibretroDatabase, onLog: (String) -> Unit): GameMetadata? {
        if (file.crc == null || file.crc == "0") return null
        return file.crc?.let { crc32 -> db.gameDao().findByCRC(crc32) }
            ?.let { convertToGameMetadata(it, "CRC") }
    }

    private suspend fun findBySerial(file: StorageFile, db: LibretroDatabase, onLog: (String) -> Unit): GameMetadata? {
        if (file.serial == null) return null
        return db.gameDao().findBySerial(file.serial!!)
            ?.let { convertToGameMetadata(it, "Serial") }
    }

    private fun extractGameSystem(rom: LibretroRom): GameSystem {
        return GameSystem.findById(rom.system!!)
    }
}
