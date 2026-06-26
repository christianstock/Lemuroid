package com.swordfish.lemuroid.app.shared.game.skins

import androidx.compose.ui.graphics.Color

/**
 * Represents a Game Boy handheld skin with case and button colors
 */
data class GbSkin(
    val id: String,
    val name: String,
    val caseColor: Color,
    val buttonsColor: Color,
) {
    companion object {
        val GREY = GbSkin(
            id = "gb_grey",
            name = "Classic Grey",
            caseColor = Color(0xFFDDD8CC),
            buttonsColor = Color(0xFF730551), // Maroon
        )

        val RED = GbSkin(
            id = "gb_red",
            name = "Red",
            caseColor = Color(0xFFcf2d39),
            buttonsColor = Color(0xFF1C1C1C),
        )

        val YELLOW = GbSkin(
            id = "gb_yellow",
            name = "Yellow",
            caseColor = Color(0xFFedbc16),
            buttonsColor = Color(0xFF1C1C1C),
        )

        val GREEN = GbSkin(
            id = "gb_green",
            name = "Green",
            caseColor = Color(0xFF027d5d),
            buttonsColor = Color(0xFF1C1C1C),
        )

        val BLUE = GbSkin(
            id = "gb_blue",
            name = "Blue",
            caseColor = Color(0xFF1148a0),
            buttonsColor = Color(0xFF1C1C1C),
        )

        val BLACK = GbSkin(
            id = "gb_black",
            name = "Black",
            caseColor = Color(0xFF1C1C1C),
            buttonsColor = Color(0xFF555555),
        )

        val WHITE = GbSkin(
            id = "gb_white",
            name = "White",
            caseColor = Color(0xFFdbd9d5),
            buttonsColor = Color(0xFF1C1C1C),
        )

        val ALL_SKINS = listOf(
            GREY,
            RED,
            YELLOW,
            GREEN,
            BLUE,
            BLACK,
            WHITE,
        )

        fun getById(id: String): GbSkin? = ALL_SKINS.find { it.id == id }
    }
}
