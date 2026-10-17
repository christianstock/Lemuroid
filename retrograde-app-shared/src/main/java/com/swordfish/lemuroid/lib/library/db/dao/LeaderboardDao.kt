package com.swordfish.lemuroid.lib.library.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.swordfish.lemuroid.lib.library.db.entity.LeaderboardEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LeaderboardDao {
    @Query("SELECT * FROM leaderboards WHERE gameId = :gameId ORDER BY displayOrder ASC, id ASC")
    suspend fun getLeaderboardsForGame(gameId: Int): List<LeaderboardEntity>

    @Query("SELECT * FROM leaderboards WHERE gameId = :gameId ORDER BY displayOrder ASC, id ASC")
    fun getLeaderboardsForGameFlow(gameId: Int): Flow<List<LeaderboardEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(leaderboards: List<LeaderboardEntity>)

    @Query("DELETE FROM leaderboards WHERE gameId = :gameId")
    suspend fun deleteForGame(gameId: Int)

    @Query("DELETE FROM leaderboards")
    suspend fun clearAll()
}
