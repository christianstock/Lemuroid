package com.swordfish.lemuroid.app.shared.cheats

// Manages cheat database operations and LibRetro integration
import com.swordfish.lemuroid.app.shared.cheats.parser.CheatParser
import com.swordfish.lemuroid.lib.library.GameSystem
import com.swordfish.lemuroid.lib.library.db.RetrogradeDatabase
import com.swordfish.lemuroid.lib.library.db.dao.GameCheatDao
import com.swordfish.lemuroid.lib.library.db.entity.Game
import com.swordfish.lemuroid.lib.library.db.entity.GameCheatEntity
import com.swordfish.lemuroid.lib.storage.DirectoriesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.zip.ZipInputStream
import android.content.Context
import android.net.Uri

class CheatManager(
    private val gameCheatDao: GameCheatDao,
    private val directoriesManager: DirectoriesManager,
    private val retrogradeDatabase: RetrogradeDatabase,
) {
    suspend fun getEnabledCheats(gameId: Int): List<GameCheatEntity> = withContext(Dispatchers.IO) {
        gameCheatDao.getCheatsForGame(gameId)
            .filter { it.enabled }
    }

    suspend fun getAllCheats(gameId: Int): List<GameCheatEntity> = withContext(Dispatchers.IO) {
        gameCheatDao.getCheatsForGame(gameId)
    }

    suspend fun updateCheatEnabled(gameId: Int, cheatIndex: Int, enabled: Boolean) = withContext(Dispatchers.IO) {
        val cheats = gameCheatDao.getCheatsForGame(gameId)
        val cheat = cheats.find { it.cheatIndex == cheatIndex }
        if (cheat != null) {
            gameCheatDao.insertCheat(cheat.copy(enabled = enabled))
        }
    }

    suspend fun importCheats(context: Context, gameId: Int, zipUri: Uri) = withContext(Dispatchers.IO) {
        // Clear existing cheats for this game before manual import to avoid duplicates/stale data
        gameCheatDao.clearCheatsForGame(gameId)
        
        context.contentResolver.openInputStream(zipUri)?.use { inputStream ->
            ZipInputStream(inputStream).use { zipInputStream ->
                var entry = zipInputStream.nextEntry
                var globalCheatIndex = 0
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
                                    source = source
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
        onLog("═══════════════════════════════════════════════════════════════")
        onLog("🎮 STARTING CHEAT IMPORT SCAN")
        onLog("═══════════════════════════════════════════════════════════════")
        onLog("Scanning cheats directory: ${cheatsDir.absolutePath}")
        
        if (!cheatsDir.exists()) {
            onLog("ERROR: Cheat directory not found.")
            return@withContext 0
        }

        // Clear all existing cheats for a fresh scan
        gameCheatDao.clearAllCheats()
        onLog("✓ Cleared local cheat database.")

        val games = retrogradeDatabase.gameDao().selectAll()
        var totalMatchedGames = 0
        var totalCheats = 0
        val gamesWithNoCheats = mutableListOf<String>()

        onLog("📊 Processing ${games.size} games...")
        onLog("─────────────────────────────────────────────────────────────")

        games.forEachIndexed { index, game ->
            onProgress(index.toFloat() / games.size.toFloat())
            val system = GameSystem.findById(game.systemId)
            
            // Search in multiple possible folder names
            val systemDirs = listOf(
                File(cheatsDir, system.libretroFullName),
                File(cheatsDir, system.libretroFullName.replace(" - ", " ")),
                File(cheatsDir, system.id.dbname.uppercase())
            ).distinct().filter { it.exists() }

            val matchingFiles = mutableListOf<File>()
            systemDirs.forEach { dir ->
                matchingFiles.addAll(findMatchingCheatFiles(game, dir))
            }

            val distinctMatchingFiles = matchingFiles.distinctBy { it.absolutePath }

             if (distinctMatchingFiles.isNotEmpty()) {
                 // Special logging for Shantae
                 if (game.title.contains("Shantae", ignoreCase = true)) {
                     onLog("[🔍 SHANTAE] MATCH: ${game.title} (${distinctMatchingFiles.size} files)")
                     distinctMatchingFiles.forEach { file ->
                         onLog("[🔍 SHANTAE]   📄 File: ${file.name}")
                     }
                 } else {
                     onLog("✓ MATCH: ${game.title} (${distinctMatchingFiles.size} files)")
                 }

                  var globalCheatIndex = 0
                  distinctMatchingFiles.forEach { chtFile ->
                      runCatching {
                           chtFile.inputStream().use { inputStream ->
                               val cheats = CheatParser.parse(inputStream)
                               val fileNameTypeTag = getCheatTypeTag(chtFile.nameWithoutExtension)

                              cheats.forEach { cheat ->
                                  // Prioritize filename-based detection, fallback to cheat file type if filename detection returns null
                                  val source = (fileNameTypeTag ?: cheat.type ?: "Others").trim()
                                  gameCheatDao.insertCheat(
                                      GameCheatEntity(
                                          gameId = game.id,
                                          zipUri = chtFile.absolutePath,
                                          entryName = chtFile.name,
                                          cheatIndex = globalCheatIndex++,
                                          description = cheat.description,
                                          code = cheat.code,
                                          enabled = false,
                                          source = source
                                      )
                                  )
                                  totalCheats++
                              }
                          }
                      }.onFailure {
                          onLog("❌ ERROR parsing ${chtFile.name}: ${it.message}")
                      }
                  }
                if (globalCheatIndex > 0) {
                    totalMatchedGames++
                }
            } else {
                // Track games with no cheats
                gamesWithNoCheats.add(game.title)
            }
        }

        onProgress(1f)
        onLog("─────────────────────────────────────────────────────────────")
        onLog("✓ Scan Complete!")
        onLog("📊 Summary:")
        onLog("  • Games with cheats: $totalMatchedGames")
        onLog("  • Total cheats found: $totalCheats")
        onLog("  • Games with NO cheats: ${gamesWithNoCheats.size}")

        if (gamesWithNoCheats.isNotEmpty()) {
            onLog("─────────────────────────────────────────────────────────────")
            onLog("🔸 Games without cheats:")
            gamesWithNoCheats.sorted().forEach { gameName ->
                onLog("   ○ $gameName")
            }
        }

        onLog("═══════════════════════════════════════════════════════════════")
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
}
