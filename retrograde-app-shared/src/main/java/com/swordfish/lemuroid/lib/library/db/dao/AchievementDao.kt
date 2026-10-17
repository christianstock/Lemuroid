package com.swordfish.lemuroid.lib.library.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.swordfish.lemuroid.lib.library.db.entity.AchievementEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AchievementDao {
    @Query("SELECT * FROM achievements WHERE gameId = :gameId ORDER BY displayOrder ASC, id ASC")
    suspend fun getAchievementsForGame(gameId: Int): List<AchievementEntity>

    @Query("SELECT * FROM achievements WHERE gameId = :gameId ORDER BY displayOrder ASC, id ASC")
    fun getAchievementsForGameFlow(gameId: Int): Flow<List<AchievementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(achievements: List<AchievementEntity>)

    @Query("DELETE FROM achievements WHERE gameId = :gameId")
    suspend fun deleteForGame(gameId: Int)

    @Query("DELETE FROM achievements")
    suspend fun clearAll()
}
