package com.swordfish.lemuroid.lib.library.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.swordfish.lemuroid.lib.library.db.entity.GameCheatEntity

@Dao
interface GameCheatDao {
    @Query("SELECT * FROM game_cheats WHERE gameId = :gameId")
    suspend fun getCheatsForGame(gameId: Int): List<GameCheatEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheat(cheat: GameCheatEntity)

    @Query("DELETE FROM game_cheats WHERE gameId = :gameId AND cheatIndex = :cheatIndex")
    suspend fun deleteCheat(gameId: Int, cheatIndex: Int)

    @Query("DELETE FROM game_cheats WHERE gameId = :gameId")
    suspend fun clearCheatsForGame(gameId: Int)
}
