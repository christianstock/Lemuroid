package com.swordfish.lemuroid.app.shared.game.skins

import androidx.compose.ui.graphics.Color

enum class GameBoyColorModel(
    val displayName: String,
    val widthMm: Float,
    val heightMm: Float,
    val preferredOrientation: SkinOrientation = SkinOrientation.PORTRAIT,
) {
    CBG_01("Game Boy Color", 43.0f, 39.0f),
}

data class GameBoyColorSkin(
    val id: String,
    val name: String,
    val caseColor: Color,
    val buttonColor: Color = Color(0xFF1C1C1C),
    val model: GameBoyColorModel = GameBoyColorModel.CBG_01,
) {
    val preferredOrientation: SkinOrientation get() = model.preferredOrientation
    companion object {
        val BERRY = GameBoyColorSkin(
            id = "berry",
            name = "Berry",
            caseColor = Color(0xFFC81F55), // #C81F55
        )

        val GRAPE = GameBoyColorSkin(
            id = "grape",
            name = "Grape",
            caseColor = Color(0xFF4D3380), // #4D3380
        )

        val KIWI = GameBoyColorSkin(
            id = "kiwi",
            name = "Kiwi",
            caseColor = Color(0xFF76C043), // #76C043
        )

        val DANDELION = GameBoyColorSkin(
            id = "dandelion",
            name = "Dandelion",
            caseColor = Color(0xFFF9C623), // #F9C623
        )

        val TEAL = GameBoyColorSkin(
            id = "teal",
            name = "Teal",
            caseColor = Color(0xFF008B9B), // #008B9B
        )

        val ATOMIC_PURPLE = GameBoyColorSkin(
            id = "atomic_purple",
            name = "Atomic Purple",
            caseColor = Color(0xFF6C5E8A), // #6C5E8A
        )

        val ALL_SKINS = listOf(
            BERRY,
            GRAPE,
            KIWI,
            DANDELION,
            TEAL,
            ATOMIC_PURPLE,
        )

        fun getById(id: String): GameBoyColorSkin? = ALL_SKINS.find { it.id == id }
    }
}

