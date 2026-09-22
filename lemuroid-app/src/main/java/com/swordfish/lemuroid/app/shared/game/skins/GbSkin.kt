package com.swordfish.lemuroid.app.shared.game.skins

import androidx.compose.ui.graphics.Color

enum class GbModel { DMG, POCKET }

/**
 * Represents a Game Boy handheld skin with case and button colors
 */
data class GbSkin(
    val id: String,
    val name: String,
    val caseColor: Color,
    val screenLensColor: Color,
    val labelColor: Color,
    val lineBlue: Color,
    val lineRed: Color,
    val dPadColor: Color,
    val buttonsColor: Color,
    val menuColor: Color,
    val model: GbModel = GbModel.DMG
) {
    companion object {
        val GREY = GbSkin(
            id = "gb_grey",
            name = "Classic Grey",
            caseColor = Color(0xFFDBD3CD),
            screenLensColor = Color(0xFF656574),
            labelColor = Color(0xFF111b91),
            lineBlue = Color(0xFF111b91),
            lineRed = Color(0xFF930551),
            dPadColor = Color(0xFF222222),
            buttonsColor = Color(0xFF930551),
            menuColor = Color(0xFF555555),
        )

        val RED = GbSkin(
            id = "gb_red",
            name = "Radiant Red",
            caseColor = Color(0xFFD72424),
            screenLensColor = Color(0xFF222222),
            labelColor = Color(0xFF222222),
            lineBlue = Color(0xFF111b91),
            lineRed = Color(0xFF930551),
            dPadColor = Color(0xFF222222),
            buttonsColor = Color(0xFF222222),
            menuColor = Color(0xFF222222),
        )

        val YELLOW = GbSkin(
            id = "gb_yellow",
            name = "Vibrant Yellow",
            caseColor = Color(0xFFE9D514),
            screenLensColor = Color(0xFF222222),
            labelColor = Color(0xFF222222),
            lineBlue = Color(0xFF111b91),
            lineRed = Color(0xFF930551),
            dPadColor = Color(0xFF222222),
            buttonsColor = Color(0xFF222222),
            menuColor = Color(0xFF222222),
        )

        val GREEN = GbSkin(
            id = "gb_green",
            name = "Gorgeous Green",
            caseColor = Color(0xFF2A852F),
            screenLensColor = Color(0xFF222222),
            labelColor = Color(0xFF222222),
            lineBlue = Color(0xFF111b91),
            lineRed = Color(0xFF930551),
            dPadColor = Color(0xFF222222),
            buttonsColor = Color(0xFF222222),
            menuColor = Color(0xFF222222),
        )

        val BLUE = GbSkin(
            id = "gb_blue",
            name = "Cool Blue",
            caseColor = Color(0xFF7492B9),
            screenLensColor = Color(0xFF222222),
            labelColor = Color(0xFF222222),
            lineBlue = Color(0xFF111b91),
            lineRed = Color(0xFF930551),
            dPadColor = Color(0xFF222222),
            buttonsColor = Color(0xFF222222),
            menuColor = Color(0xFF222222),
        )

        val BLACK = GbSkin(
            id = "gb_black",
            name = "Deep Black",
            caseColor = Color(0xFF1C1C1C),
            screenLensColor = Color(0xFF656574),
            labelColor = Color(0xFF930551),
            lineBlue = Color(0xFF111b91),
            lineRed = Color(0xFF930551),
            dPadColor = Color(0xFF656574),
            buttonsColor = Color(0xFF656574),
            menuColor = Color(0xFF656574),
        )

        val WHITE = GbSkin(
            id = "gb_white",
            name = "Traditional White",
            caseColor = Color(0xFFdbd9d5),
            screenLensColor = Color(0xFF222222),
            labelColor = Color(0xFF222222),
            lineBlue = Color(0xFF111b91),
            lineRed = Color(0xFF930551),
            dPadColor = Color(0xFF222222),
            buttonsColor = Color(0xFF222222),
            menuColor = Color(0xFF222222),
        )

        val POCKET_SILVER = GbSkin(
            id = "pocket_silver",
            name = "Pocket Silver",
            caseColor = Color(0xFFc0c0c0),
            screenLensColor = Color(0xFFc9c9c9),
            labelColor = Color(0xFF222222),
            lineBlue = Color(0xFF111b91),
            lineRed = Color(0xFF930551),
            dPadColor = Color(0xFF222222),
            buttonsColor = Color(0xFF222222),
            menuColor = Color(0xFF222222),
            model = GbModel.POCKET
        )

        val ALL_SKINS = listOf(
            GREY,
            RED,
            YELLOW,
            GREEN,
            BLUE,
            BLACK,
            WHITE,
            POCKET_SILVER,
        )

        fun getById(id: String): GbSkin? = ALL_SKINS.find { it.id == id }
    }
}
