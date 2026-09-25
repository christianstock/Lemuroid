package com.swordfish.lemuroid.lib.library.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.swordfish.lemuroid.lib.library.db.entity.GameCheatEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GameCheatDao {
    @Query("SELECT * FROM game_cheats WHERE gameId = :gameId ORDER BY displayOrder ASC, id ASC")
    suspend fun getCheatsForGame(gameId: Int): List<GameCheatEntity>

    @Query("SELECT * FROM game_cheats WHERE gameId = :gameId ORDER BY displayOrder ASC, id ASC")
    fun getCheatsForGameFlow(gameId: Int): Flow<List<GameCheatEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheat(cheat: GameCheatEntity)

    @Query("DELETE FROM game_cheats WHERE gameId = :gameId AND cheatIndex = :cheatIndex")
    suspend fun deleteCheat(
        gameId: Int,
        cheatIndex: Int,
    )

    @Query("DELETE FROM game_cheats WHERE id = :cheatId")
    suspend fun deleteCheatById(cheatId: Int)

    @Query("DELETE FROM game_cheats WHERE gameId = :gameId")
    suspend fun clearCheatsForGame(gameId: Int)

    @Query("UPDATE game_cheats SET enabled = 0 WHERE gameId = :gameId")
    suspend fun disableAllCheatsForGame(gameId: Int)

    @Query("DELETE FROM game_cheats")
    suspend fun clearAllCheats()

    @Query("UPDATE game_cheats SET displayOrder = :displayOrder WHERE id = :cheatId")
    suspend fun updateCheatDisplayOrder(cheatId: Int, displayOrder: Int)
}
