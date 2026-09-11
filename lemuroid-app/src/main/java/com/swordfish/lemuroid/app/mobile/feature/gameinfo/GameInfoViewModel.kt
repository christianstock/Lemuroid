package com.swordfish.lemuroid.app.mobile.feature.gameinfo

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import coil.imageLoader
import com.swordfish.lemuroid.common.kotlin.calculateCrc32
import com.swordfish.lemuroid.common.kotlin.cleanGameTitle
import com.swordfish.lemuroid.lib.library.SystemID
import com.swordfish.lemuroid.lib.library.db.RetrogradeDatabase
import com.swordfish.lemuroid.lib.library.db.entity.Game
import com.swordfish.lemuroid.lib.library.metadata.GameMetadataProvider
import com.swordfish.lemuroid.lib.storage.BaseStorageFile
import com.swordfish.lemuroid.lib.storage.StorageProviderRegistry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GameInfoViewModel(
    private val appContext: Context,
    private val gameId: Int,
    private val retrogradeDb: RetrogradeDatabase,
    private val metadataProvider: GameMetadataProvider,
    private val storageProviderRegistry: StorageProviderRegistry
) : ViewModel() {
    private val _game = MutableStateFlow<Game?>(null)
    val game = _game.asStateFlow()

    private val _isRescanning = MutableStateFlow(false)
    val isRescanning = _isRescanning.asStateFlow()

    private val _logs = MutableStateFlow<List<String>>(emptyList())
    val logs = _logs.asStateFlow()

    init {
        loadGame()
    }

    private fun addLog(message: String) {
        val current = _logs.value.toMutableList()
        current.add(message)
        _logs.value = current
        Log.e("GameInfoVM", message) // High visibility in Logcat
    }

    private fun loadGame() {
        viewModelScope.launch {
            val game = retrogradeDb.gameDao().selectById(gameId)
            _game.value = game
            if (game != null) {
                Log.d("GameInfoVM", "Loaded Game: ID=${game.id}, Title=${game.title}, Art=${game.coverFrontUrl}")
            }
        }
    }

    fun updateGameDetails(releaseDate: String?, publisher: String?, developer: String?) {
        val currentGame = _game.value ?: return
        viewModelScope.launch {
            val updatedGame = currentGame.copy(
                releaseDate = releaseDate,
                publisher = publisher,
                developer = developer
            )
            retrogradeDb.gameDao().update(updatedGame)
            _game.value = updatedGame
            addLog("Manual Edit Saved.")
        }
    }

    fun rescan() {
        val currentGame = _game.value ?: return
        _logs.value = emptyList()
        viewModelScope.launch {
            _isRescanning.value = true
            addLog("Rescan Initialized: ${currentGame.title}")
            
            withContext(Dispatchers.IO) {
                try {
                    val uri = Uri.parse(currentGame.fileUri)
                    addLog("Target URI: $uri")
                    
                    val document = DocumentFile.fromSingleUri(appContext, uri)
                    if (document == null || !document.exists()) {
                        addLog("CRITICAL FAIL: Physical ROM not found at URI.")
                        return@withContext
                    }
                    
                    val baseFile = BaseStorageFile(
                        name = currentGame.fileName,
                        size = document.length(),
                        uri = uri,
                        path = null
                    )
                    addLog("ROM File Verified: ${baseFile.name} (${baseFile.size} bytes)")
                    
                    val provider = storageProviderRegistry.getProvider(currentGame)
                    var storageFile = provider.getStorageFile(baseFile)
                    
                    if (storageFile != null) {
                        if (storageFile.systemID == null) {
                            addLog("Info: System ID missing, reconstruction started...")
                            val reconstructedSystem = SystemID.entries.find { it.dbname == currentGame.systemId }
                            storageFile = storageFile.copy(systemID = reconstructedSystem)
                        }

                        if (storageFile.crc == null || storageFile.crc == "0") {
                            addLog("Computing Hardware Hash (CRC32)...")
                            val crc = appContext.contentResolver.openInputStream(uri)?.use { it.calculateCrc32() }
                            storageFile = storageFile.copy(crc = crc)
                            addLog("Hardware CRC: $crc")
                        }

                        addLog("Contacting Libretro Scraper...")
                        val metadata = metadataProvider.retrieveMetadata(storageFile) { logMsg ->
                            viewModelScope.launch { addLog("Provider: $logMsg") }
                        }
                        
                        if (metadata != null) {
                            val newArt = metadata.thumbnail
                            val currentArt = currentGame.coverFrontUrl
                            
                            addLog("Scraper Found Match: ${metadata.name}")
                            addLog("Scraper Proposed Art: $newArt")
                            
                            // FORCE PURGE COIL CACHE
                            if (newArt != null) {
                                addLog("Purging Image Cache for URL...")
                                withContext(Dispatchers.Main) {
                                    @Suppress("OPT_IN_USAGE")
                                    appContext.imageLoader.diskCache?.remove(newArt)
                                    appContext.imageLoader.memoryCache?.remove(coil.memory.MemoryCache.Key(newArt))
                                }
                            }
                            
                            val cleanedTitle = metadata.name?.cleanGameTitle() ?: currentGame.title
                            addLog("Cleaned Title: $cleanedTitle")
                            
                            val updatedGame = currentGame.copy(
                                title = cleanedTitle,
                                coverFrontUrl = newArt ?: currentArt,
                                developer = if (currentGame.developer.isNullOrBlank()) metadata.developer else currentGame.developer,
                                publisher = if (currentGame.publisher.isNullOrBlank()) metadata.publisher else currentGame.publisher,
                                releaseDate = if (currentGame.releaseDate.isNullOrBlank()) metadata.releaseDate else currentGame.releaseDate,
                                summary = if (currentGame.summary.isNullOrBlank()) metadata.summary else currentGame.summary,
                                country = if (currentGame.country.isNullOrBlank()) metadata.country else currentGame.country
                            )
                            
                            addLog("Writing to Database...")
                            retrogradeDb.gameDao().update(updatedGame)
                            _game.value = updatedGame
                            addLog("SUCCESS: Database record updated.")
                            addLog("> Saved Art URL: ${updatedGame.coverFrontUrl}")
                        } else {
                            addLog("FAIL: Scraper returned no metadata for this file.")
                        }
                    } else {
                        addLog("FAIL: Technical storage provider error.")
                    }
                } catch (e: Exception) {
                    addLog("EXCEPTION TRAP: ${e.message}")
                    Log.e("GameInfoVM", "Fatal error during rescan", e)
                    e.printStackTrace()
                }
            }
            _isRescanning.value = false
        }
    }

    class Factory(
        private val appContext: Context,
        private val gameId: Int,
        private val retrogradeDb: RetrogradeDatabase,
        private val metadataProvider: GameMetadataProvider,
        private val storageProviderRegistry: StorageProviderRegistry
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return GameInfoViewModel(appContext, gameId, retrogradeDb, metadataProvider, storageProviderRegistry) as T
        }
    }
}
