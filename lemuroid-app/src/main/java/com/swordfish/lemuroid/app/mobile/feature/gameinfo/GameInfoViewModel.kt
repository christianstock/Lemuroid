package com.swordfish.lemuroid.app.mobile.feature.gameinfo

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import coil.imageLoader
import com.swordfish.lemuroid.common.kotlin.calculateCrc32
import com.swordfish.lemuroid.common.kotlin.cleanGameTitle
import com.swordfish.lemuroid.common.kotlin.extractRomRegion
import com.swordfish.lemuroid.common.kotlin.extractRomVersion
import com.swordfish.lemuroid.lib.library.SystemID
import com.swordfish.lemuroid.lib.library.db.RetrogradeDatabase
import com.swordfish.lemuroid.lib.library.db.entity.Game
import com.swordfish.lemuroid.lib.library.metadata.GameMetadata
import com.swordfish.lemuroid.lib.library.metadata.GameMetadataProvider
import com.swordfish.lemuroid.lib.storage.BaseStorageFile
import com.swordfish.lemuroid.lib.storage.StorageProviderRegistry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

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

    private val _pendingMetadata = MutableStateFlow<GameMetadata?>(null)
    val pendingMetadata = _pendingMetadata.asStateFlow()

    init {
        loadGame()
    }

    private fun loadGame() {
        viewModelScope.launch {
            _game.value = retrogradeDb.gameDao().selectById(gameId)
        }
    }

    fun updateGameDetails(
        title: String,
        releaseDate: String?,
        publisher: String?,
        developer: String?,
        region: String?,
        version: String?
    ) {
        val currentGame = _game.value ?: return
        viewModelScope.launch {
            val updatedGame = currentGame.copy(
                title = title,
                releaseDate = releaseDate?.ifBlank { null },
                publisher = publisher?.ifBlank { null },
                developer = developer?.ifBlank { null },
                country = region?.ifBlank { null },
                summary = version?.ifBlank { null }
            )
            retrogradeDb.gameDao().update(updatedGame)
            _game.value = updatedGame
        }
    }

    fun rescan() {
        val currentGame = _game.value ?: return
        viewModelScope.launch {
            _isRescanning.value = true

            val metadata = withContext(Dispatchers.IO) {
                fetchMetadata(currentGame)
            }

            if (metadata != null) {
                // Ensure debugInfo OPTIONS only contains unique, non-blank valid images (max 2)
                _pendingMetadata.value = sanitizeMetadataOptions(metadata, currentGame.coverFrontUrl)
            }

            _isRescanning.value = false
        }
    }

    fun clearPendingMetadata() {
        _pendingMetadata.value = null
    }

    fun applyCustomScrapedMetadata(
        title: String,
        releaseDate: String?,
        publisher: String?,
        developer: String?,
        region: String?,
        coverFrontUrl: String?,
        coverBackUrl: String?,
        cartridgeUrl: String?,
        manualUrl: String? = null
    ) {
        val currentGame = _game.value ?: return
        viewModelScope.launch {
            val validFrontUrl = coverFrontUrl?.takeIf { isValidImageUri(it) }
                ?: currentGame.coverFrontUrl?.takeIf { isValidImageUri(it) }
            val validBackUrl = coverBackUrl?.takeIf { isValidImageUri(it) }
                ?: currentGame.coverBackUrl?.takeIf { isValidImageUri(it) }
            val validCartridgeUrl = cartridgeUrl?.takeIf { isValidImageUri(it) }
                ?: currentGame.cartridgeUrl?.takeIf { isValidImageUri(it) }
            val validManualUrl = manualUrl?.takeIf { it.isNotBlank() }
                ?: currentGame.manualUrl?.takeIf { it.isNotBlank() }

            val updatedGame = currentGame.copy(
                title = title,
                releaseDate = releaseDate?.ifBlank { null },
                publisher = publisher?.ifBlank { null },
                developer = developer?.ifBlank { null },
                country = region?.ifBlank { null },
                coverFrontUrl = validFrontUrl,
                coverBackUrl = validBackUrl,
                cartridgeUrl = validCartridgeUrl,
                manualUrl = validManualUrl
            )
            retrogradeDb.gameDao().update(updatedGame)
            _game.value = updatedGame
            _pendingMetadata.value = null
        }
    }

    private suspend fun fetchMetadata(currentGame: Game): GameMetadata? {
        try {
            val uri = Uri.parse(currentGame.fileUri)
            val document = DocumentFile.fromSingleUri(appContext, uri)
            if (document == null || !document.exists()) {
                return null
            }

            val baseFile = BaseStorageFile(
                name = currentGame.fileName,
                size = document.length(),
                uri = uri,
                path = null
            )

            val provider = storageProviderRegistry.getProvider(currentGame)
            var storageFile = provider.getStorageFile(baseFile)

            if (storageFile != null) {
                if (storageFile.systemID == null) {
                    val reconstructedSystem = SystemID.entries.find { it.dbname == currentGame.systemId }
                    storageFile = storageFile.copy(systemID = reconstructedSystem)
                }

                if (storageFile.crc == null || storageFile.crc == "0") {
                    val crc = appContext.contentResolver.openInputStream(uri)?.use { it.calculateCrc32() }
                    storageFile = storageFile.copy(crc = crc)
                }

                return metadataProvider.retrieveMetadata(storageFile) { }
            }
        } catch (e: Exception) {
            // Silently ignore
        }
        return null
    }

    /**
     * Sanitizes the metadata debugInfo OPTIONS block before handing it to the UI dialog,
     * guaranteeing at most 2 unique, loadable image URLs/URIs (Existing + LibretroDB).
     */
    private fun sanitizeMetadataOptions(metadata: GameMetadata, currentCoverUrl: String?): GameMetadata {
        val validCurrent = currentCoverUrl?.takeIf { isValidImageUri(it) }
        val validNew = metadata.thumbnail?.takeIf { isValidImageUri(it) }

        // Strictly combine at most 2 distinct valid images
        val uniqueArts = listOfNotNull(validCurrent, validNew).distinct()

        val debugInfo = metadata.debugInfo
        if (debugInfo == null || !debugInfo.startsWith("OPTIONS|")) {
            val optionsStr = "OPTIONS|arts:${uniqueArts.joinToString(",")}"
            return metadata.copy(debugInfo = optionsStr)
        }

        // Re-pack arts inside the debugInfo string safely
        val parts = debugInfo.split("|").toMutableList()
        val updatedParts = parts.map { part ->
            if (part.startsWith("arts:")) {
                "arts:${uniqueArts.joinToString(",")}"
            } else {
                part
            }
        }.toMutableList()

        if (parts.none { it.startsWith("arts:") } && uniqueArts.isNotEmpty()) {
            updatedParts.add("arts:${uniqueArts.joinToString(",")}")
        }

        return metadata.copy(
            thumbnail = uniqueArts.firstOrNull(),
            debugInfo = updatedParts.joinToString("|")
        )
    }

    private fun isValidImageUri(path: String?): Boolean {
        if (path.isNullOrBlank()) return false
        return path.startsWith("http://", ignoreCase = true) ||
                path.startsWith("https://", ignoreCase = true) ||
                path.startsWith("file://", ignoreCase = true) ||
                path.startsWith("content://", ignoreCase = true)
    }

    fun saveLocalThumbnail(uri: Uri, deleteSource: Boolean) {
        val currentGame = _game.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val inputStream = appContext.contentResolver.openInputStream(uri) ?: return@launch
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream.close()

                if (bitmap == null) return@launch

                val maxDim = 512
                val ratio = bitmap.width.toFloat() / bitmap.height.toFloat()
                val (targetW, targetH) = if (bitmap.width > bitmap.height) {
                    maxDim to (maxDim / ratio).toInt()
                } else {
                    (maxDim * ratio).toInt() to maxDim
                }

                val scaledBitmap = Bitmap.createScaledBitmap(bitmap, targetW, targetH, true)

                val coversDir = File(appContext.getExternalFilesDir(null), "covers").apply { mkdirs() }
                val coverFile = File(coversDir, "cover_${currentGame.id}.png")

                FileOutputStream(coverFile).use { out ->
                    scaledBitmap.compress(Bitmap.CompressFormat.PNG, 90, out)
                }

                val coverUri = Uri.fromFile(coverFile).toString()

                withContext(Dispatchers.Main) {
                    val updatedGame = currentGame.copy(coverFrontUrl = coverUri)
                    retrogradeDb.gameDao().update(updatedGame)
                    _game.value = updatedGame

                    if (deleteSource) {
                        try {
                            appContext.contentResolver.delete(uri, null, null)
                        } catch (e: Exception) {
                            // Ignore permission issue
                        }
                    }
                }
            } catch (e: Exception) {
                // Silently ignore
            }
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
