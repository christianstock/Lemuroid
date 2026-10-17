package com.swordfish.lemuroid.lib.library.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "leaderboards",
    indices = [
        Index("gameId"),
    ],
)
data class LeaderboardEntity(
    @PrimaryKey
    val id: Int,
    val gameId: Int,
    val title: String,
    val description: String,
    val format: String = "TIME",
    val userRank: Int? = null,
    val userResult: String? = null,
    val topEntryUser: String? = null,
    val topEntryResult: String? = null,
    val displayOrder: Int = 0,
)
