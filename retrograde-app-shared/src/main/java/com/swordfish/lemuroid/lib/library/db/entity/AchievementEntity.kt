package com.swordfish.lemuroid.lib.library.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "achievements",
    indices = [
        Index("gameId"),
    ],
)
data class AchievementEntity(
    @PrimaryKey
    val id: Int,
    val gameId: Int,
    val title: String,
    val description: String,
    val points: Int,
    val badgeUrl: String,
    val isUnlocked: Boolean = false,
    val unlockTimestamp: Long? = null,
    val type: String? = null,
    val displayOrder: Int = 0,
)
