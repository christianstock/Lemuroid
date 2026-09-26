/*
 * GameLibrary.kt
 *
 * Copyright (C) 2017 Retrograde Project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.swordfish.lemuroid.lib.library

import com.swordfish.lemuroid.common.coroutines.batchWithSizeAndTime
import com.swordfish.lemuroid.lib.bios.BiosManager
import com.swordfish.lemuroid.lib.library.db.RetrogradeDatabase
import com.swordfish.lemuroid.lib.library.db.entity.DataFile
import com.swordfish.lemuroid.lib.library.db.entity.Game
import com.swordfish.lemuroid.lib.library.metadata.GameMetadata
import com.swordfish.lemuroid.lib.library.metadata.GameMetadataProvider
import com.swordfish.lemuroid.lib.library.metadata.SkraperMetadataProvider
import com.swordfish.lemuroid.lib.storage.BaseStorageFile
import com.swordfish.lemuroid.lib.storage.GroupedStorageFiles
import com.swordfish.lemuroid.lib.storage.RomFiles
import com.swordfish.lemuroid.lib.storage.StorageFile
import com.swordfish.lemuroid.lib.storage.StorageProvider
import com.swordfish.lemuroid.lib.storage.StorageProviderRegistry
import dagger.Lazy
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.mapNotNull

class LemuroidLibrary(
    private val retrogradedb: RetrogradeDatabase,
    private val storageProviderRegistry: Lazy<StorageProviderRegistry>,
    private val gameMetadataProvider: Lazy<GameMetadataProvider>,
    private val skraperMetadataProvider: Lazy<SkraperMetadataProvider>,
    private val biosManager: BiosManager,
) {
    suspend fun indexLibrary() {
        val startedAtMs = System.currentTimeMillis()

        try {
            // Clear Skraper cache to ensure fresh XML/DAT file scanning on rescan
            skraperMetadataProvider.get().clearCache()
            indexProviders(startedAtMs)
        } catch (e: Throwable) {
            // Ignored
        } finally {
            cleanUp(startedAtMs)
        }
    }

    @OptIn(FlowPreview::class)
    private suspend fun indexProviders(startedAtMs: Long) {
        val gameMetadata = gameMetadataProvider.get()
        val enabledProviders = storageProviderRegistry.get().enabledProviders
        enabledProviders.asFlow()
            .flatMapConcat { indexSingleProvider(it, startedAtMs, gameMetadata) }
            .collect()
    }

    @OptIn(FlowPreview::class)
    private fun indexSingleProvider(
        provider: StorageProvider,
        startedAtMs: Long,
        gameMetadata: GameMetadataProvider,
    ): Flow<Unit> {
        return provider.listBaseStorageFiles()
            .flatMapConcat { StorageFilesMerger.mergeDataFiles(provider, it).asFlow() }
            .batchWithSizeAndTime(MAX_BUFFER_SIZE, MAX_TIME)
            .flatMapMerge { processBatch(it, provider, startedAtMs, gameMetadata) }
    }

    private suspend fun processBatch(
        batch: List<GroupedStorageFiles>,
        provider: StorageProvider,
        startedAtMs: Long,
        gameMetadata: GameMetadataProvider,
    ) = flow<Unit> {
        val entries = batch.map { fetchEntriesFromDatabase(it) }

        val existingEntries = entries.filterIsInstance<ScanEntry.GameFile>()
        if (existingEntries.isNotEmpty()) {
            handleExistingEntries(existingEntries, startedAtMs)
        }

        val newEntries =
            entries.filterIsInstance<ScanEntry.File>()
        
        if (newEntries.isNotEmpty()) {
            val processedNewEntries = newEntries.map { buildEntryFromMetadata(it.file, provider, gameMetadata, startedAtMs) }
            handleNewEntries(processedNewEntries, startedAtMs, provider)
        }
    }

    private fun fetchEntriesFromDatabase(storageFile: GroupedStorageFiles): ScanEntry {
        val game = retrogradedb.gameDao().selectByFileUri(storageFile.primaryFile.uri.toString())
        return buildScanEntry(storageFile, game)
    }

    private fun buildScanEntry(
        storageFile: GroupedStorageFiles,
        game: Game?,
    ): ScanEntry {
        return if (game != null) {
            ScanEntry.GameFile(storageFile, game)
        } else {
            ScanEntry.File(storageFile)
        }
    }

    private fun handleExistingEntries(
        entries: List<ScanEntry.GameFile>,
        startedAtMs: Long,
    ) {
        updateGames(entries, startedAtMs)
        updateDataFiles(entries, startedAtMs)
    }

    private fun updateGames(
        entries: List<ScanEntry.GameFile>,
        startedAtMs: Long,
    ) {
        val updatedGames =
            entries
                .map { (storageFiles, game) ->
                    // Re-sync fileName from the actual file on disk for non-zipped games.
                    // This self-heals cases where fileName was accidentally corrupted in the DB
                    // (e.g. set to a cleaned title instead of the real ROM filename).
                    // Zipped games are skipped: we cannot recover the inner filename here.
                    val primaryName = storageFiles.primaryFile.name
                    val correctedFileName = if (!primaryName.endsWith(".zip", ignoreCase = true)) {
                        primaryName
                    } else {
                        game.fileName
                    }
                    game.copy(lastIndexedAt = startedAtMs, fileName = correctedFileName)
                }

        retrogradedb.gameDao().update(updatedGames)
    }

    private fun updateDataFiles(
        entries: List<ScanEntry.GameFile>,
        startedAtMs: Long,
    ) {
        val dataFiles =
            entries.flatMap { (storageFile, game) ->
                storageFile.dataFiles.map { convertIntoDataFile(game.id, it, startedAtMs) }
            }

        retrogradedb.dataFileDao().insert(dataFiles)
    }

    private fun convertIntoDataFile(
        gameId: Int,
        baseStorageFile: BaseStorageFile,
        startedAtMs: Long,
    ): DataFile {
        return DataFile(
            gameId = gameId,
            fileUri = baseStorageFile.uri.toString(),
            fileName = baseStorageFile.name,
            lastIndexedAt = startedAtMs,
            path = baseStorageFile.path,
        )
    }

    private fun handleNewEntries(
        entries: List<ScanEntry>,
        startedAtMs: Long,
        provider: StorageProvider,
    ) {
        val gameFiles =
            entries
                .filterIsInstance<ScanEntry.GameFile>()

        val unknownFiles =
            entries
                .filterIsInstance<ScanEntry.File>()
                .flatMap { it.file.allFiles() }

        handleNewGames(gameFiles, startedAtMs)
        handleUnknownFiles(provider, unknownFiles, startedAtMs)
    }

    private fun handleNewGames(
        pairs: List<ScanEntry.GameFile>,
        startedAtMs: Long,
    ) {
        val games =
            pairs
                .map { it.game }

        val gameIds = retrogradedb.gameDao().insert(games)
        val dataFiles =
            pairs
                .map { it.file.dataFiles }
                .zip(gameIds)
                .flatMap { (files, gameId) ->
                    files.map {
                        convertIntoDataFile(gameId.toInt(), it, startedAtMs)
                    }
                }

        retrogradedb.dataFileDao().insert(dataFiles)
    }

    private fun handleUnknownFiles(
        provider: StorageProvider,
        files: List<BaseStorageFile>,
        startedAtMs: Long,
    ) {
        files.forEach { baseStorageFile ->
            val storageFile = safeStorageFile(provider, baseStorageFile)
            val inputStream = storageFile?.uri?.let { provider.getInputStream(it) }

            if (storageFile != null && inputStream != null) {
                biosManager.tryAddBiosAfter(storageFile, inputStream, startedAtMs)
            }
        }
    }

    private suspend fun buildEntryFromMetadata(
        groupedStorageFile: GroupedStorageFiles,
        provider: StorageProvider,
        metadataProvider: GameMetadataProvider,
        startedAtMs: Long,
    ): ScanEntry {
        val game =
            sortedFilesForScanning(groupedStorageFile).asFlow()
                .mapNotNull { safeStorageFile(provider, it) }
                .mapNotNull { storageFile ->
                    val metadata = metadataProvider.retrieveMetadata(storageFile) { }
                    convertGameMetadataToGame(groupedStorageFile, storageFile, metadata, startedAtMs)
                }
                .firstOrNull()

        return buildScanEntry(groupedStorageFile, game)
    }

    private fun safeStorageFile(
        provider: StorageProvider,
        baseStorageFile: BaseStorageFile,
    ): StorageFile? {
        return runCatching { provider.getStorageFile(baseStorageFile) }
            .getOrNull()
    }

    private fun cleanUp(startedAtMs: Long) {
        kotlin.runCatching {
            removeDeletedBios(startedAtMs)
        }
        kotlin.runCatching {
            removeDeletedGames(startedAtMs)
        }
        kotlin.runCatching {
            removeDeletedDataFiles(startedAtMs)
        }
    }

    private fun removeDeletedBios(startedAtMs: Long) {
        biosManager.deleteBiosBefore(startedAtMs)
    }

    private fun sortedFilesForScanning(groupedStorageFile: GroupedStorageFiles): List<BaseStorageFile> {
        return groupedStorageFile.dataFiles.sortedBy { it.name } + listOf(groupedStorageFile.primaryFile)
    }

    private fun convertGameMetadataToGame(
        groupedStorageFile: GroupedStorageFiles,
        storageFile: StorageFile,
        gameMetadata: GameMetadata?,
        lastIndexedAt: Long,
    ): Game? {
        if (gameMetadata == null) {
            return null
        }

        val gameSystem = GameSystem.findById(gameMetadata.system!!)

        // If the databased matched a data file (as with bin/cue) we force link the primary filename
        val fileName =
            if (groupedStorageFile.dataFiles.isNotEmpty()) {
                groupedStorageFile.primaryFile.name
            } else {
                storageFile.name
            }

        return Game(
            fileName = fileName,
            fileUri = groupedStorageFile.primaryFile.uri.toString(),
            title = gameMetadata.name ?: groupedStorageFile.primaryFile.name,
            systemId = gameSystem.id.dbname,
            developer = gameMetadata.developer,
            publisher = gameMetadata.publisher,
            coverFrontUrl = gameMetadata.thumbnail,
            coverBackUrl = gameMetadata.thumbnailBack,
            cartridgeUrl = gameMetadata.cartridgeImage,
            manualUrl = gameMetadata.manualUrl,
            releaseDate = gameMetadata.releaseDate,
            summary = gameMetadata.summary,
            country = gameMetadata.country,
            lastIndexedAt = lastIndexedAt,
        )
    }

    private fun removeDeletedDataFiles(startedAtMs: Long) {
        val dataFiles = retrogradedb.dataFileDao().selectByLastIndexedAtLessThan(startedAtMs)
        retrogradedb.dataFileDao().delete(dataFiles)
    }

    private fun removeDeletedGames(startedAtMs: Long) {
        val games = retrogradedb.gameDao().selectByLastIndexedAtLessThan(startedAtMs)
        retrogradedb.gameDao().delete(games)
    }

    fun getGameFiles(
        game: Game,
        dataFiles: List<DataFile>,
        allowVirtualFiles: Boolean,
    ): RomFiles {
        val provider = storageProviderRegistry.get()
        return provider.getProvider(game).getGameRomFiles(game, dataFiles, allowVirtualFiles)
    }

    suspend fun scrapeAllMetadata() {
        val metadataProvider = gameMetadataProvider.get()
        val allGames = retrogradedb.gameDao().selectAll()
        val providerRegistry = storageProviderRegistry.get()

        val updatedGames = mutableListOf<Game>()

        for (game in allGames) {
            runCatching {
                val provider = providerRegistry.getProvider(game)
                val uri = android.net.Uri.parse(game.fileUri)
                val baseFile = BaseStorageFile(
                    name = game.fileName,
                    size = 0L,
                    uri = uri,
                    path = null
                )
                val storageFile = provider.getStorageFile(baseFile)
                if (storageFile != null) {
                    val metadata = metadataProvider.retrieveMetadata(storageFile)
                    if (metadata != null) {
                        // Non-overwriting rule: Only populate missing/null/blank fields!
                        val newTitle = if (game.title.isNotBlank()) game.title else (metadata.name ?: game.title)
                        val newDev = game.developer ?: metadata.developer
                        val newPub = game.publisher ?: metadata.publisher
                        val newDate = game.releaseDate ?: metadata.releaseDate
                        val newCountry = game.country ?: metadata.country
                        val newSummary = game.summary ?: metadata.summary
                        val newCover = game.coverFrontUrl ?: metadata.thumbnail

                        val updatedGame = game.copy(
                            title = newTitle,
                            developer = newDev,
                            publisher = newPub,
                            releaseDate = newDate,
                            country = newCountry,
                            summary = newSummary,
                            coverFrontUrl = newCover
                        )
                        if (updatedGame != game) {
                            updatedGames.add(updatedGame)
                        }
                    }
                }
            }
        }

        if (updatedGames.isNotEmpty()) {
            retrogradedb.gameDao().update(updatedGames)
        }
    }

    private sealed class ScanEntry {
        data class GameFile(val file: GroupedStorageFiles, val game: Game) : ScanEntry()

        data class File(val file: GroupedStorageFiles) : ScanEntry()
    }

    companion object {
        const val MAX_BUFFER_SIZE = 200
        const val MAX_TIME = 5000
    }
}
