package com.swordfish.lemuroid.app.shared.cheats

import com.swordfish.lemuroid.lib.library.db.dao.GameCheatDao
import com.swordfish.lemuroid.lib.library.db.entity.GameCheatEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CheatManager(
    private val gameCheatDao: GameCheatDao
) {
    suspend fun getEnabledCheats(gameId: Int): List<Cheat> = withContext(Dispatchers.IO) {
        gameCheatDao.getCheatsForGame(gameId)
            .filter { it.enabled }
            .map { Cheat(it.cheatIndex, it.description, it.code, it.enabled) }
    }

    suspend fun applyCheat(gameId: Int, zipUri: String, entryName: String, cheat: Cheat) = withContext(Dispatchers.IO) {
        if (cheat.enabled) {
            gameCheatDao.insertCheat(
                GameCheatEntity(
                    gameId = gameId,
                    zipUri = zipUri,
                    entryName = entryName,
                    cheatIndex = cheat.index,
                    description = cheat.description,
                    code = cheat.code,
                    enabled = true
                )
            )
        } else {
            gameCheatDao.deleteCheat(gameId, cheat.index)
        }
    }

    suspend fun updateCheatEnabled(gameId: Int, cheatIndex: Int, enabled: Boolean) = withContext(Dispatchers.IO) {
        val cheat = gameCheatDao.getCheatsForGame(gameId).find { it.cheatIndex == cheatIndex }
        if (cheat != null) {
            if (enabled) {
                gameCheatDao.insertCheat(cheat.copy(enabled = true))
            } else {
                gameCheatDao.deleteCheat(gameId, cheatIndex)
            }
        }
    }
}
