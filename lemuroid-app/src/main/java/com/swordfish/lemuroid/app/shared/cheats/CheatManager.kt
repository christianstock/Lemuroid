package com.swordfish.lemuroid.app.shared.cheats

// Manages cheat database operations and LibRetro integration
import com.swordfish.lemuroid.app.shared.cheats.parser.CheatParser
import com.swordfish.lemuroid.app.shared.cheats.parser.CwCheatParser
import com.swordfish.lemuroid.lib.library.SystemScanProgress
import com.swordfish.lemuroid.lib.library.GameSystem
import com.swordfish.lemuroid.lib.library.db.RetrogradeDatabase
import com.swordfish.lemuroid.lib.library.db.dao.GameCheatDao
import com.swordfish.lemuroid.lib.library.db.entity.Game
import com.swordfish.lemuroid.lib.library.db.entity.GameCheatEntity
import com.swordfish.lemuroid.lib.storage.DirectoriesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.util.zip.ZipInputStream
import android.content.Context
import android.net.Uri

class CheatManager(
    private val gameCheatDao: GameCheatDao,
    private val directoriesManager: DirectoriesManager,
    private val retrogradeDatabase: RetrogradeDatabase,
    private val cheatDownloader: CheatDownloader? = null,
) {
    // Progress tracking StateFlows
    private val _systemProgress = MutableStateFlow<List<SystemScanProgress>>(emptyList())
    val systemProgress: StateFlow<List<SystemScanProgress>> = _systemProgress.asStateFlow()

    private val _totalGamesFound = MutableStateFlow(0)
    val totalGamesFound: StateFlow<Int> = _totalGamesFound.asStateFlow()

    private val _scanComplete = MutableStateFlow(false)
    val scanComplete: StateFlow<Boolean> = _scanComplete.asStateFlow()

    suspend fun getEnabledCheats(gameId: Int): List<GameCheatEntity> = withContext(Dispatchers.IO) {
        gameCheatDao.getCheatsForGame(gameId)
            .filter { it.enabled }
    }

    suspend fun getAllCheats(gameId: Int): List<GameCheatEntity> = withContext(Dispatchers.IO) {
        gameCheatDao.getCheatsForGame(gameId)
    }

    fun getAllCheatsFlow(gameId: Int): Flow<List<GameCheatEntity>> {
        return gameCheatDao.getCheatsForGameFlow(gameId)
    }

    suspend fun updateCheatEnabled(gameId: Int, cheatIndex: Int, enabled: Boolean) = withContext(Dispatchers.IO) {
        val cheats = gameCheatDao.getCheatsForGame(gameId)
        val cheat = cheats.find { it.cheatIndex == cheatIndex }
        if (cheat != null) {
            val updated = cheat.copy(
                enabled = enabled,
                lastUsed = System.currentTimeMillis()
            )
            gameCheatDao.insertCheat(updated)
        }
    }

    suspend fun insertCheat(cheat: GameCheatEntity) = withContext(Dispatchers.IO) {
        gameCheatDao.insertCheat(cheat)
    }

    suspend fun clearCheatsForGame(gameId: Int) = withContext(Dispatchers.IO) {
        gameCheatDao.clearCheatsForGame(gameId)
    }

    suspend fun importCheats(context: Context, gameId: Int, zipUri: Uri) = withContext(Dispatchers.IO) {
        // Clear existing cheats for this game before manual import to avoid duplicates/stale data
        gameCheatDao.clearCheatsForGame(gameId)
        
        context.contentResolver.openInputStream(zipUri)?.use { inputStream ->
            ZipInputStream(inputStream).use { zipInputStream ->
                var entry = zipInputStream.nextEntry
                var globalCheatIndex = 0
                var displayOrder = 0
                while (entry != null) {
                    if (entry.name.endsWith(".cht", ignoreCase = true)) {
                        val cheats = CheatParser.parse(zipInputStream)
                        val fileNameTypeTag = getCheatTypeTag(entry.name.substringAfterLast("/").substringBeforeLast("."))

                        cheats.forEach { cheat ->
                            // Prioritize filename-based detection, fallback to cheat file type if filename detection returns null
                            val source = (fileNameTypeTag ?: cheat.type ?: "Others").trim()
                            gameCheatDao.insertCheat(
                                GameCheatEntity(
                                    gameId = gameId,
                                    zipUri = zipUri.toString(),
                                    entryName = entry.name,
                                    cheatIndex = globalCheatIndex++,
                                    description = cheat.description,
                                    code = cheat.code,
                                    enabled = cheat.enabled,
                                    source = source,
                                    displayOrder = displayOrder++
                                )
                            )
                        }
                    }
                    entry = zipInputStream.nextEntry
                }
            }
        }
    }

    suspend fun scanLibraryForCheats(onProgress: (Float) -> Unit, onLog: (String) -> Unit): Int = withContext(Dispatchers.IO) {
        val cheatsDir = directoriesManager.getCheatsDirectory()
        onLog("Starting cheat scan...")
        
        // Reset progress tracking
        _systemProgress.value = emptyList()
        _totalGamesFound.value = 0
        _scanComplete.value = false

        if (!cheatsDir.exists()) {
            return@withContext 0
        }

        // Clear all existing cheats for a fresh scan
        gameCheatDao.clearAllCheats()

        val games = retrogradeDatabase.gameDao().selectAll()
        var totalMatchedGames = 0
        var totalCheats = 0

        // Track per-system games found (not cheats) for final summary
        val systemGamesCounts = mutableMapOf<String, Int>()

        // Group games by system and ONLY include systems that have games
        val gamesBySystem = games.groupBy { GameSystem.findById(it.systemId) }
            .filter { it.value.isNotEmpty() }
            .toSortedMap(compareBy { it.libretroFullName })

        var processedGames = 0
        gamesBySystem.forEach { (system, systemGames) ->
            val systemName = system.libretroFullName
            
            // Update progress for current system
            val currentSystemProgress = SystemScanProgress(
                systemName = systemName,
                gamesFound = 0,
                isCurrentlyScanning = true,
                isComplete = false
            )
            _systemProgress.value = _systemProgress.value.filter { it.systemName != systemName } + currentSystemProgress
            systemGamesCounts[systemName] = 0

            var systemGamesCount = 0
            var systemCheatCount = 0
            
            // Search in multiple possible folder names - only for systems with games
            val systemDirs = listOf(
                File(cheatsDir, system.libretroFullName),
                File(cheatsDir, system.libretroFullName.replace(" - ", " ")),
                File(cheatsDir, system.id.dbname.uppercase()),
                File(cheatsDir, system.id.dbname),
                File(cheatsDir, "PPSSPP"),
                File(cheatsDir, "PSP"),
                File(cheatsDir, "Playstation Portable"),
                File(cheatsDir, "PlayStation Portable"),
            ).distinct().filter { it.exists() }

            systemGames.forEachIndexed { index, game ->
                onProgress((processedGames + index).toFloat() / games.size.toFloat())

                val matchingFiles = mutableListOf<File>()
                systemDirs.forEach { dir ->
                    matchingFiles.addAll(findMatchingCheatFiles(game, dir))
                }

                val distinctMatchingFiles = matchingFiles.distinctBy { it.absolutePath }

                 if (distinctMatchingFiles.isNotEmpty()) {
                       var globalCheatIndex = 0
                       distinctMatchingFiles.forEach { chtFile ->
                           runCatching {
                                chtFile.inputStream().use { inputStream ->
                                    // Detect cheat file format
                                    val fileContent = inputStream.bufferedReader().readText()
                                    
                                    // Pure CWCheat format has lines starting with _S, _G, _C at beginning of line
                                    val hasCwCheatLines = fileContent.lines().any { line ->
                                        line.trimStart().matches(Regex("^_[SGC].*"))
                                    }
                                    val hasLibRetroKey = fileContent.contains("cheats")
                                    val isCwCheatFormat = hasCwCheatLines && !hasLibRetroKey
                                    
                                    val cheats = if (isCwCheatFormat) {
                                        fileContent.byteInputStream().use { 
                                            CwCheatParser.parse(it)
                                        }
                                    } else {
                                        fileContent.byteInputStream().use { 
                                            CheatParser.parse(it)
                                        }
                                    }
                                    
                                    val fileNameTypeTag = getCheatTypeTag(chtFile.nameWithoutExtension)

                                   cheats.forEach { cheat ->
                                       val source = (fileNameTypeTag ?: cheat.type ?: "Others").trim()
                                       try {
                                           gameCheatDao.insertCheat(
                                               GameCheatEntity(
                                                   gameId = game.id,
                                                   zipUri = chtFile.absolutePath,
                                                   entryName = chtFile.name,
                                                   cheatIndex = globalCheatIndex++,
                                                   description = cheat.description,
                                                   code = cheat.code,
                                                   enabled = false,
                                                   source = source,
                                                   displayOrder = totalCheats++
                                               )
                                           )
                                           systemCheatCount++
                                       } catch (e: Exception) {
                                           Timber.e(e, "Error inserting cheat for game ${game.id}")
                                       }
                                   }
                               }
                           }.onFailure {
                               // Silent fail
                           }
                       }
                       
                     if (globalCheatIndex > 0) {
                          totalMatchedGames++
                          systemGamesCount++
                          
                          systemGamesCounts[systemName] = systemGamesCount
                          _systemProgress.value = _systemProgress.value.map {
                              if (it.systemName == systemName) {
                                  it.copy(gamesFound = systemGamesCount)
                              } else it
                          }
                      }
                  }
             }

             // Mark system as complete
             _systemProgress.value = _systemProgress.value.map {
                 if (it.systemName == systemName) {
                     it.copy(isCurrentlyScanning = false, isComplete = true)
                 } else it
             }

             processedGames += systemGames.size
        }

        onProgress(1f)
        _scanComplete.value = true

        onLog("Scan complete:")
        systemGamesCounts.toSortedMap().forEach { (system, count) ->
            if (count > 0) {
                onLog("$system - $count game${if (count != 1) "s" else ""}")
            }
        }
        
        totalMatchedGames
    }

    private fun findMatchingCheatFiles(game: Game, systemCheatDir: File): List<File> {
        val gameTitleNorm = normalizeForMatching(game.title)
        val gameFileNorm = normalizeForMatching(game.fileName.substringBeforeLast("."))
        
        return systemCheatDir.listFiles()?.filter { file ->
            val chtNameNorm = normalizeForMatching(file.nameWithoutExtension)
            // Match if names are identical after normalization or if one contains the other
            chtNameNorm == gameTitleNorm || chtNameNorm == gameFileNorm || 
                    (gameTitleNorm.length > 5 && chtNameNorm.contains(gameTitleNorm)) || 
                    (chtNameNorm.length > 5 && gameTitleNorm.contains(chtNameNorm))
        } ?: emptyList()
    }

    private fun normalizeForMatching(name: String): String {
        return name.replace(Regex("\\(.*\\)|\\s*\\[.*\\]"), "") // Remove tags in () or []
            .replace(Regex("[^a-zA-Z0-9]"), "") // Remove non-alphanumeric
            .lowercase()
            .trim()
    }

    private fun getCheatTypeTag(fileName: String): String? {
        val name = fileName.lowercase()
        return when {
            // GameGenie patterns
            name.contains("gamegenie") || name.contains("game genie") ||
            name.contains("game-genie") || Regex("\\bgg\\b").containsMatchIn(name) -> "GameGenie"
            // CodeBreaker patterns
            name.contains("codebreaker") || name.contains("code breaker") ||
            name.contains("code-breaker") || Regex("\\bcb\\b").containsMatchIn(name) ||
            name.contains("pelican") -> "CodeBreaker"
            // ActionReplay patterns
            name.contains("actionreplay") || name.contains("action replay") ||
            name.contains("action-replay") || Regex("\\bar\\b").containsMatchIn(name) ||
            name.contains("armax") -> "ActionReplay"
            // GameShark patterns
            name.contains("gameshark") || name.contains("game shark") ||
            name.contains("game-shark") || Regex("\\bgs\\b").containsMatchIn(name) ||
            name.contains("x-terminator") -> "GameShark"
            else -> null
        }
    }

    suspend fun rescandGameForCheats(gameId: Int): Int = withContext(Dispatchers.IO) {
        val game = retrogradeDatabase.gameDao().selectById(gameId) ?: return@withContext 0
        val system = GameSystem.findById(game.systemId)
        val cheatsDir = directoriesManager.getCheatsDirectory()

        // If cheats directory does not exist or is empty, attempt download from LibRetro
        if (!cheatsDir.exists() || cheatsDir.listFiles().isNullOrEmpty()) {
            cheatDownloader?.downloadAndExtractCheats()
        }

        if (!cheatsDir.exists()) {
            return@withContext 0
        }

        // Get existing cheats for this game to prevent duplicate entries ("no doubleups")
        val existingCheats = gameCheatDao.getCheatsForGame(gameId)
        val existingKeys = existingCheats.map { "${it.description.trim().lowercase()}|${it.code.trim().lowercase()}" }.toSet()
        val existingCodes = existingCheats.map { it.code.trim().lowercase() }.toSet()

        val systemDirs = listOf(
            File(cheatsDir, system.libretroFullName),
            File(cheatsDir, system.libretroFullName.replace(" - ", " ")),
            File(cheatsDir, system.id.dbname.uppercase()),
            File(cheatsDir, system.id.dbname),
            File(cheatsDir, "PPSSPP"),
            File(cheatsDir, "PSP"),
            File(cheatsDir, "Playstation Portable"),
            File(cheatsDir, "PlayStation Portable"),
        ).distinct().filter { it.exists() }

        var insertedCount = 0
        var maxIndex = existingCheats.maxOfOrNull { it.cheatIndex } ?: -1
        var maxDisplayOrder = existingCheats.maxOfOrNull { it.displayOrder } ?: -1

        val matchingFiles = mutableListOf<File>()
        systemDirs.forEach { dir ->
            matchingFiles.addAll(findMatchingCheatFiles(game, dir))
        }

        val distinctMatchingFiles = matchingFiles.distinctBy { it.absolutePath }

        if (distinctMatchingFiles.isNotEmpty()) {
            distinctMatchingFiles.forEach { chtFile ->
                runCatching {
                    chtFile.inputStream().use { inputStream ->
                        val fileContent = inputStream.bufferedReader().readText()
                        val hasCwCheatLines = fileContent.lines().any { line ->
                            line.trimStart().matches(Regex("^_[SGC].*"))
                        }
                        val hasLibRetroKey = fileContent.contains("cheats")
                        val isCwCheatFormat = hasCwCheatLines && !hasLibRetroKey

                        val cheats = if (isCwCheatFormat) {
                            fileContent.byteInputStream().use { CwCheatParser.parse(it) }
                        } else {
                            fileContent.byteInputStream().use { CheatParser.parse(it) }
                        }

                        val fileNameTypeTag = getCheatTypeTag(chtFile.nameWithoutExtension)
                        cheats.forEach { cheat ->
                            val key = "${cheat.description.trim().lowercase()}|${cheat.code.trim().lowercase()}"
                            val normalizedCode = cheat.code.trim().lowercase()

                            // Skip inserting duplicate cheats
                            if (!existingKeys.contains(key) && !existingCodes.contains(normalizedCode)) {
                                val source = (fileNameTypeTag ?: cheat.type ?: "Others").trim()
                                try {
                                    maxIndex++
                                    maxDisplayOrder++
                                    gameCheatDao.insertCheat(
                                        GameCheatEntity(
                                            gameId = gameId,
                                            zipUri = chtFile.absolutePath,
                                            entryName = chtFile.name,
                                            cheatIndex = maxIndex,
                                            description = cheat.description,
                                            code = cheat.code,
                                            enabled = false,
                                            source = source,
                                            displayOrder = maxDisplayOrder,
                                            lastUsed = 0L
                                        )
                                    )
                                    insertedCount++
                                } catch (e: Exception) {
                                    Timber.e(e, "Error inserting cheat for game ${game.id}")
                                }
                            }
                        }
                    }
                }.onFailure {
                    // Silent fail
                }
            }
        }

        insertedCount
    }

    suspend fun deleteCheat(cheatId: Int) = withContext(Dispatchers.IO) {
        gameCheatDao.deleteCheatById(cheatId)
    }

    suspend fun updateCheatDisplayOrder(cheatId: Int, newOrder: Int) = withContext(Dispatchers.IO) {
        gameCheatDao.updateCheatDisplayOrder(cheatId, newOrder)
    }

    suspend fun disableAllCheats(gameId: Int) = withContext(Dispatchers.IO) {
        gameCheatDao.disableAllCheatsForGame(gameId)
    }
}
