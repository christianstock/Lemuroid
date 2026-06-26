package com.swordfish.lemuroid.app.shared.game.skins

import androidx.compose.ui.graphics.Color

/**
 * Represents a Game Boy Advance handheld skin with case and button colors
 */
data class GbaSkin(
    val id: String,
    val name: String,
    val caseColor: Color,
    val buttonsColor: Color,
) {
    companion object {
        val INDIGO = GbaSkin(
            id = "gba_indigo",
            name = "Indigo",
            caseColor = Color(0xFF602E8A),
            buttonsColor = Color(0xFF333333),
        )

        val ARCTIC = GbaSkin(
            id = "gba_arctic",
            name = "Arctic",
            caseColor = Color(0xFFE8E8E8),
            buttonsColor = Color(0xFF333333),
        )

        val BLACK = GbaSkin(
            id = "gba_black",
            name = "Black",
            caseColor = Color(0xFF1C1C1C),
            buttonsColor = Color(0xFF555555),
        )

        val ORANGE = GbaSkin(
            id = "gba_orange",
            name = "Orange",
            caseColor = Color(0xFFE66E17),
            buttonsColor = Color(0xFF333333),
        )

        val FUCHSIA = GbaSkin(
            id = "gba_fuchsia",
            name = "Fuchsia",
            caseColor = Color(0xFFC81F55),
            buttonsColor = Color(0xFF333333),
        )

        val GLACIER = GbaSkin(
            id = "gba_glacier",
            name = "Glacier",
            caseColor = Color(0xFF90A4AE),
            buttonsColor = Color(0xFF333333),
        )

        val ALL_SKINS = listOf(
            INDIGO,
            ARCTIC,
            BLACK,
            ORANGE,
            FUCHSIA,
            GLACIER,
        )

        fun getById(id: String): GbaSkin? = ALL_SKINS.find { it.id == id }
    }
}
