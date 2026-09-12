package com.swordfish.lemuroid.metadata.libretrodb

import com.swordfish.lemuroid.common.kotlin.cleanForArtUrl
import com.swordfish.lemuroid.common.kotlin.extractRomRegion
import com.swordfish.lemuroid.common.kotlin.extractRomVersion
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

        var metadata = runCatching {
            findByCRC(storageFile, db)
                ?: findBySerial(storageFile, db)
                ?: findByFilename(db, storageFile)
        }.getOrNull()
        
        if (metadata != null) {
            val coverUrl = computeHighFidelityArtUrl(metadata, storageFile)
            val region = storageFile.name.extractRomRegion() ?: metadata.country
            val version = storageFile.name.extractRomVersion() ?: metadata.summary
            
            metadata = metadata.copy(
                thumbnail = coverUrl,
                country = region,
                summary = version
            )
        }
        
        return metadata
    }

    private fun convertToGameMetadata(rom: LibretroRom): GameMetadata {
        return GameMetadata(
            name = rom.name ?: "", 
            romName = rom.romName,
            thumbnail = null,
            system = rom.system,
            developer = rom.developer,
            publisher = null,
            thumbnailBack = null,
            releaseDate = null,
            summary = null,
            country = extractRegion(rom.name),
            debugInfo = null
        )
    }

    private fun extractRegion(name: String?): String? {
        if (name == null) return null
        val regex = Regex("\\(([^)]+)\\)")
        return regex.findAll(name)
            .map { it.groupValues[1] }
            .filter { it.length <= 15 }
            .joinToString(", ")
            .takeIf { it.isNotEmpty() }
    }

    private fun computeHighFidelityArtUrl(metadata: GameMetadata, file: StorageFile): String? {
        val system = GameSystem.findById(metadata.system!!)
        val systemName = if (system.id == SystemID.MAME2003PLUS) "MAME" else system.libretroFullName
        val imageType = "Named_Boxarts"

        var artName = metadata.name!!.cleanForArtUrl()
        
        val filenameRegion = extractRegion(file.name)
        if (!artName.contains("(") && filenameRegion != null) {
            artName = "$artName ($filenameRegion)"
        }

        if (artName.contains("Driver", ignoreCase = true)) {
            artName = artName.replace("You Are the Wheelman", "You are the Wheelman", ignoreCase = true)
            artName = artName.replace("(Europe)", "(En,Fr,De,Es,It)", ignoreCase = true)
            artName = artName.replace("(USA, Europe)", "(USA) (En,Fr,De,Es,It)", ignoreCase = true)
        }

        val thumbName = artName.replace(THUMB_REPLACE, "_")
        
        val encodedSystem = systemName.replace(" ", "%20")
        val encodedName = thumbName
            .replace(" ", "%20")
            .replace("&", "%26")
            .replace("'", "%27")
            .replace("#", "%23")
        
        return "http://thumbnails.libretro.com/$encodedSystem/$imageType/$encodedName.png"
    }

    private suspend fun findByFilename(db: LibretroDatabase, file: StorageFile): GameMetadata? {
        return db.gameDao().findByFileName(file.name)
            ?.filterNullable { extractGameSystem(it).scanOptions.scanByFilename }
            ?.let { convertToGameMetadata(it) }
    }

    private suspend fun findByCRC(file: StorageFile, db: LibretroDatabase): GameMetadata? {
        if (file.crc == null || file.crc == "0") return null
        return file.crc?.let { crc32 -> db.gameDao().findByCRC(crc32) }
            ?.let { convertToGameMetadata(it) }
    }

    private suspend fun findBySerial(file: StorageFile, db: LibretroDatabase): GameMetadata? {
        if (file.serial == null) return null
        return db.gameDao().findBySerial(file.serial!!)
            ?.let { convertToGameMetadata(it) }
    }

    private fun extractGameSystem(rom: LibretroRom): GameSystem {
        return GameSystem.findById(rom.system!!)
    }
}
