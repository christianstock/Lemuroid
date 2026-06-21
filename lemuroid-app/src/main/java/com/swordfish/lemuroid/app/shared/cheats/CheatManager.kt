package com.swordfish.lemuroid.app.shared.cheats

// Manages cheat database operations and LibRetro integration
import com.swordfish.lemuroid.lib.library.db.dao.GameCheatDao
import com.swordfish.lemuroid.lib.library.db.entity.GameCheatEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CheatManager(
    private val gameCheatDao: GameCheatDao
) {
    suspend fun getEnabledCheats(gameId: Int): List<GameCheatEntity> = withContext(Dispatchers.IO) {
        gameCheatDao.getCheatsForGame(gameId)
            .filter { it.enabled }
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
