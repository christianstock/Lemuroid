package com.swordfish.lemuroid.app.shared.game.skins

import androidx.compose.ui.graphics.Color

enum class GameBoyModel(
    val displayName: String,
    val widthMm: Float,
    val heightMm: Float,
    val preferredOrientation: SkinOrientation = SkinOrientation.PORTRAIT,
) {
    DMG_01("Classic", 45.5f, 40.95f),
    MGB_01("Pocket", 47.5f, 42.75f),
    MGB_101("Light", 47.5f, 42.75f),
}

data class GameBoySkin(
    val id: String,
    val name: String,
    val model: GameBoyModel = GameBoyModel.DMG_01,
    val caseColor: Color,
    val screenLensColor: Color = Color(0xFF222222),
    val dPadColor: Color = Color(0xFF222222),
    val actionButtonColor: Color = Color(0xFF222222),
    val menuButtonColor: Color = Color(0xFF222222),
    val labelColor: Color = Color(0xFF222222),
) {
    val preferredOrientation: SkinOrientation get() = model.preferredOrientation

    companion object {
        val GREY = GameBoySkin(
            id = "gb_grey",
            name = "Classic Grey",
            caseColor = Color(0xFFDBD3CD),
            screenLensColor = Color(0xFF656574),
            actionButtonColor = Color(0xFF930551),
            menuButtonColor = Color(0xFF555555),
            labelColor = Color(0xFF111b91),
        )

        val RED = GameBoySkin(
            id = "gb_red",
            name = "Radiant Red",
            caseColor = Color(0xFFD72424),
        )

        val YELLOW = GameBoySkin(
            id = "gb_yellow",
            name = "Vibrant Yellow",
            caseColor = Color(0xFFE9D514),
        )

        val GREEN = GameBoySkin(
            id = "gb_green",
            name = "Gorgeous Green",
            caseColor = Color(0xFF2A852F),
        )

        val BLUE = GameBoySkin(
            id = "gb_blue",
            name = "Cool Blue",
            caseColor = Color(0xFF7492B9),
        )

        val BLACK = GameBoySkin(
            id = "gb_black",
            name = "Deep Black",
            caseColor = Color(0xFF1C1C1C),
            screenLensColor = Color(0xFF656574),
            dPadColor = Color(0xFF656574),
            actionButtonColor = Color(0xFF656574),
            menuButtonColor = Color(0xFF656574),
            labelColor = Color(0xFF930551),
        )

        val WHITE = GameBoySkin(
            id = "gb_white",
            name = "Traditional White",
            caseColor = Color(0xFFdbd9d5),
        )

        val POCKET_SILVER = GameBoySkin(
            id = "pocket_silver",
            name = "Pocket Silver",
            model = GameBoyModel.MGB_01,
            caseColor = Color(0xFFc0c0c0),
            screenLensColor = Color(0xFFc9c9c9),
        )

        val LIGHT_SILVER = GameBoySkin(
            id = "light_silver",
            name = "Light Silver",
            model = GameBoyModel.MGB_101,
            caseColor = Color(0xFFc0c0c0),
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
            LIGHT_SILVER,
        )

        fun getById(id: String): GameBoySkin? = ALL_SKINS.find { it.id == id }
    }
}
