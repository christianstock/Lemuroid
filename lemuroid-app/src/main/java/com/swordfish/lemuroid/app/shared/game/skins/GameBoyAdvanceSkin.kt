package com.swordfish.lemuroid.app.shared.game.skins

import androidx.compose.ui.graphics.Color

enum class GameBoyAdvanceModel(
    val displayName: String,
    val widthMm: Float,
    val heightMm: Float,
    val preferredOrientation: SkinOrientation = SkinOrientation.LANDSCAPE,
) {
    AGB_001("Advance", 61.2f, 40.8f),
    AGS_001("Advance SP", 61.2f, 40.8f, SkinOrientation.PORTRAIT),
    AGS_101("Advance SP Light", 61.2f, 40.8f, SkinOrientation.PORTRAIT),
    OXY_001("Micro", 61.2f, 40.8f),
}

data class GameBoyAdvanceSkin(
    val id: String,
    val name: String,
    val model: GameBoyAdvanceModel = GameBoyAdvanceModel.AGB_001,
    val caseColor: Color,
    val buttonColor: Color = Color(0xFF333333),
) {
    val preferredOrientation: SkinOrientation get() = model.preferredOrientation
    companion object {
        val INDIGO = GameBoyAdvanceSkin(
            id = "gba_indigo",
            name = "Indigo",
            caseColor = Color(0xFF602E8A),
        )

        val ARCTIC = GameBoyAdvanceSkin(
            id = "gba_arctic",
            name = "Arctic",
            caseColor = Color(0xFFE8E8E8),
        )

        val BLACK = GameBoyAdvanceSkin(
            id = "gba_black",
            name = "Black",
            caseColor = Color(0xFF1C1C1C),
            buttonColor = Color(0xFF555555),
        )

        val ORANGE = GameBoyAdvanceSkin(
            id = "gba_orange",
            name = "Orange",
            caseColor = Color(0xFFE66E17),
        )

        val FUCHSIA = GameBoyAdvanceSkin(
            id = "gba_fuchsia",
            name = "Fuchsia",
            caseColor = Color(0xFFC81F55),
        )

        val GLACIER = GameBoyAdvanceSkin(
            id = "gba_glacier",
            name = "Glacier",
            caseColor = Color(0xFF90A4AE),
        )

        val ALL_SKINS = listOf(
            INDIGO,
            ARCTIC,
            BLACK,
            ORANGE,
            FUCHSIA,
            GLACIER,
        )

        fun getById(id: String): GameBoyAdvanceSkin? = ALL_SKINS.find { it.id == id }
    }
}
