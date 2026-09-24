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
    MGB_101("Light", 47.0f, 42.3f),
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
    val brandingColor: Color = Color(0xFF222222),
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
            brandingColor = Color(0xFF111b91),
        )

        val RED = GameBoySkin(
            id = "gb_red",
            name = "Radiant Red",
            caseColor = Color(0xFFb5363f),
        )

        val YELLOW = GameBoySkin(
            id = "gb_yellow",
            name = "Vibrant Yellow",
            caseColor = Color(0xFFf7b702),
        )

        val GREEN = GameBoySkin(
            id = "gb_green",
            name = "Gorgeous Green",
            caseColor = Color(0xFF26b48a),
        )

        val BLUE = GameBoySkin(
            id = "gb_blue",
            name = "Cool Blue",
            caseColor = Color(0xFF3049e2),
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
            brandingColor = Color(0xFF111b91),
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


        val POCKET_RED = GameBoySkin(
            id = "pocket_red",
            name = "Red",
            model = GameBoyModel.MGB_01,
            caseColor = Color(0xFFb5363f),
            brandingColor = Color(0xFFaaaaaa),
        )

        val POCKET_YELLOW = GameBoySkin(
            id = "pocket_yellow",
            name = "Yellow",
            model = GameBoyModel.MGB_01,
            caseColor = Color(0xFFf7b702),
            brandingColor = Color(0xFFaaaaaa),
        )

        val POCKET_GREEN = GameBoySkin(
            id = "pocket_green",
            name = "Green",
            model = GameBoyModel.MGB_01,
            caseColor = Color(0xFF26b48a),
            brandingColor = Color(0xFFaaaaaa),
        )

        val POCKET_BLUE = GameBoySkin(
            id = "pocket_blue",
            name = "Blue",
            model = GameBoyModel.MGB_01,
            caseColor = Color(0xFF3049e2),
            brandingColor = Color(0xFFaaaaaa),
        )

        val POCKET_BLACK = GameBoySkin(
            id = "pocket_black",
            name = "Black",
            model = GameBoyModel.MGB_01,
            caseColor = Color(0xFF1C1C1C),
            dPadColor = Color(0xFF656574),
            actionButtonColor = Color(0xFF656574),
            menuButtonColor = Color(0xFF656574),
            labelColor = Color(0xFF930551),
            brandingColor = Color(0xFFaaaaaa),
        )

        val POCKET_PINK = GameBoySkin(
            id = "pocket_pink",
            name = "Pink",
            model = GameBoyModel.MGB_01,
            caseColor = Color(0xFFe4939a),
            brandingColor = Color(0xFFaaaaaa),
        )

        val LIGHT_SILVER = GameBoySkin(
            id = "light_silver",
            name = "Light Silver",
            model = GameBoyModel.MGB_101,
            caseColor = Color(0xFFd0d0d0),
            labelColor = Color.Black.copy(alpha = 0.2f),
            brandingColor = Color(0xFFaaaaaa),
        )

        val LIGHT_GOLD = GameBoySkin(
            id = "light_gold",
            name = "Light Gold",
            model = GameBoyModel.MGB_101,
            caseColor = Color(0xFFc7c3b7),
            labelColor = Color.Black.copy(alpha = 0.2f),
            brandingColor = Color(0xFFaaaaaa),
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
            POCKET_RED,
            POCKET_YELLOW,
            POCKET_GREEN,
            POCKET_BLUE,
            POCKET_BLACK,
            POCKET_PINK,
            LIGHT_SILVER,
            LIGHT_GOLD
        )

        fun getById(id: String): GameBoySkin? = ALL_SKINS.find { it.id == id }
    }
}
