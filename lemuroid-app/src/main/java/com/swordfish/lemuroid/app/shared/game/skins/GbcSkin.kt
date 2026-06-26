package com.swordfish.lemuroid.app.shared.game.skins

import androidx.compose.ui.graphics.Color

/**
 * Represents a Game Boy Color handheld skin with case and button colors
 */
data class GbcSkin(
    val id: String,
    val name: String,
    val caseColor: Color,
    val buttonsColor: Color,
    val isDefault: Boolean = false,
) {
    companion object {
        // Official Game Boy Color skins
        val BERRY = GbcSkin(
            id = "berry",
            name = "Berry",
            caseColor = Color(0xFFC81F55), // #C81F55
            buttonsColor = Color(0xFF1C1C1C), // #1C1C1C
        )

        val GRAPE = GbcSkin(
            id = "grape",
            name = "Grape",
            caseColor = Color(0xFF4D3380), // #4D3380
            buttonsColor = Color(0xFF1C1C1C), // #1C1C1C
        )

        val KIWI = GbcSkin(
            id = "kiwi",
            name = "Kiwi",
            caseColor = Color(0xFF76C043), // #76C043
            buttonsColor = Color(0xFF1C1C1C), // #1C1C1C
        )

        val DANDELION = GbcSkin(
            id = "dandelion",
            name = "Dandelion",
            caseColor = Color(0xFFF9C623), // #F9C623
            buttonsColor = Color(0xFF1C1C1C), // #1C1C1C
        )

        val TEAL = GbcSkin(
            id = "teal",
            name = "Teal",
            caseColor = Color(0xFF008B9B), // #008B9B
            buttonsColor = Color(0xFF1C1C1C), // #1C1C1C
        )

        val ATOMIC_PURPLE = GbcSkin(
            id = "atomic_purple",
            name = "Atomic Purple",
            caseColor = Color(0xFF6C5E8A), // #6C5E8A
            buttonsColor = Color(0xFF1C1C1C), // #1C1C1C
        )

        val ALL_SKINS = listOf(
            BERRY,
            GRAPE,
            KIWI,
            DANDELION,
            TEAL,
            ATOMIC_PURPLE,
        )

        fun getById(id: String): GbcSkin? = ALL_SKINS.find { it.id == id }
    }
}

