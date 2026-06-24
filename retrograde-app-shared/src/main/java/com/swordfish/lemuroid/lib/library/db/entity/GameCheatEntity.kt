package com.swordfish.lemuroid.lib.library.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "game_cheats",
    indices = [
        Index("gameId"),
    ],
)
data class GameCheatEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val gameId: Int,
    val zipUri: String,
    val entryName: String,
    val cheatIndex: Int,
    val description: String,
    val code: String,
    val enabled: Boolean,
    val source: String? = null,
)
