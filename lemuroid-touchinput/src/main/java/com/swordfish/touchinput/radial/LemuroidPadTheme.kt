package com.swordfish.touchinput.radial

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.DefaultShadowColor
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Base theme class for the Lemuroid pad touch controls.
 * Can be extended for system-specific themes.
 */
open class LemuroidPadTheme {
    protected fun gray(
        luminosity: Float,
        opacity: Float,
    ): Color {
        return Color(luminosity, luminosity, luminosity, opacity)
    }

    open val foregroundPadding: Dp = 8.dp
    open val padding: Dp = 4.dp

    protected open val icons = gray(0.0f, 0.50f)
    protected open val iconsPressed = gray(1.0f, 0.50f)

    protected open val level3Fill = gray(1.0f, 0.50f)
    protected open val level3FillPressed = gray(0.0f, 0.50f)
    open val level3Shadow = DefaultShadowColor.copy(0.05f)
    open val level3ShadowWidth = 4.dp

    protected open val level2Fill = gray(1.0f, 0.125f)
    protected open val level2FillPressed = gray(0.0f, 0.125f)
    open val level2Shadow = DefaultShadowColor.copy(0.05f)
    open val level2ShadowWidth = 4.dp

    open val level1Fill = gray(1.0f, 0.10f)
    open val level1Shadow = DefaultShadowColor.copy(0.10f)
    open val level1ShadowWidth = 4.dp

    open val level0CornerRadius = 0.dp
    open val level0Fill = gray(1.0f, 0.05f)
    open val level0Shadow = DefaultShadowColor.copy(0.10f)
    open val level0ShadowWidth = 2.dp

    open val buttonCornerRadius: Dp = Dp.Infinity

    open val bevelColorLight: Color? = null
    open val bevelColorDark: Color? = null

    fun compositeFill(pressed: Boolean): Color {
        return if (pressed) {
            level2FillPressed
        } else {
            level2Fill
        }
    }

    fun foregroundFill(pressed: Boolean): Color {
        return if (pressed) {
            level3FillPressed
        } else {
            level3Fill
        }
    }

    fun icons(pressed: Boolean): Color {
        return if (pressed) {
            iconsPressed
        } else {
            icons
        }
    }
}

/**
 * GBC-specific theme with Game Boy Color colors and styling.
 */
class GBCTheme(
    shellColor: Color? = null,
    buttonColor: Color? = null,
) : LemuroidPadTheme() {
    // GBC shell colors: predominantly purple/indigo with some silver accents
    private val gbcPurple = shellColor ?: Color(0xFF6B2F9C)
    private val gbcLightPurple = gbcPurple.copy(alpha = 1f).let { 
        // Simple way to get a lighter color for highlights
        Color(
            (it.red * 1.2f).coerceAtMost(1f),
            (it.green * 1.2f).coerceAtMost(1f),
            (it.blue * 1.2f).coerceAtMost(1f),
            it.alpha
        )
    }
    private val gbcDarkPurple = gbcPurple.copy(alpha = 1f).let {
        // Simple way to get a darker color for shadows
        Color(
            (it.red * 0.7f).coerceAtMost(1f),
            (it.green * 0.7f).coerceAtMost(1f),
            (it.blue * 0.7f).coerceAtMost(1f),
            it.alpha
        )
    }
    
    // Button colors: GBC had distinctive colored buttons
    private val gbcButtonGray = buttonColor ?: Color(0xFFE8E8E8)
    private val gbcButtonGrayDark = gbcButtonGray.copy(alpha = 1f).let {
        Color(
            (it.red * 0.8f).coerceAtMost(1f),
            (it.green * 0.8f).coerceAtMost(1f),
            (it.blue * 0.8f).coerceAtMost(1f),
            it.alpha
        )
    }

    override val level0Fill = gbcDarkPurple.copy(alpha = 0.3f)
    override val level0Shadow = Color.Black.copy(alpha = 0.4f)

    override val level1Fill = gbcPurple.copy(alpha = 0.2f)
    override val level1Shadow = Color.Black.copy(alpha = 0.3f)

    override val level2Fill = gbcLightPurple.copy(alpha = 0.15f)
    override val level2FillPressed = gbcDarkPurple.copy(alpha = 0.15f)

    override val level3Fill = gbcButtonGray.copy(alpha = 0.6f)
    override val level3FillPressed = gbcButtonGrayDark.copy(alpha = 0.6f)

    override val bevelColorLight = gbcLightPurple.copy(alpha = 0.4f)
    override val bevelColorDark = gbcDarkPurple.copy(alpha = 0.6f)
}

/**
 * DMG-specific theme (Classic Game Boy).
 */
class DMGTheme(
    shellColor: Color? = null,
    buttonColor: Color? = null,
) : LemuroidPadTheme() {
    private val dmgGray = shellColor ?: Color(0xFF999999)
    private val dmgMaroon = buttonColor ?: Color(0xFF8B0000)

    private val dmgLightGray = dmgGray.copy(alpha = 1f).let {
        Color(
            (it.red * 1.2f).coerceAtMost(1f),
            (it.green * 1.2f).coerceAtMost(1f),
            (it.blue * 1.2f).coerceAtMost(1f),
            it.alpha
        )
    }
    private val dmgDarkGray = dmgGray.copy(alpha = 1f).let {
        Color(
            (it.red * 0.7f).coerceAtMost(1f),
            (it.green * 0.7f).coerceAtMost(1f),
            (it.blue * 0.7f).coerceAtMost(1f),
            it.alpha
        )
    }

    override val level0Fill = dmgDarkGray.copy(alpha = 0.3f)
    override val level1Fill = dmgGray.copy(alpha = 0.2f)
    override val level2Fill = dmgLightGray.copy(alpha = 0.15f)
    override val level3Fill = dmgMaroon.copy(alpha = 0.7f)
    override val level3FillPressed = dmgMaroon.copy(alpha = 0.9f)
    
    override val bevelColorLight = Color.White.copy(alpha = 0.3f)
    override val bevelColorDark = Color.Black.copy(alpha = 0.4f)
}

/**
 * GBA-specific theme (Game Boy Advance).
 */
class GBATheme(
    shellColor: Color? = null,
    buttonColor: Color? = null,
) : LemuroidPadTheme() {
    private val gbaIndigo = shellColor ?: Color(0xFF602E8A)
    private val gbaButtonColor = buttonColor ?: Color(0xFF333333)

    private val gbaLightIndigo = gbaIndigo.copy(alpha = 1f).let {
        Color(
            (it.red * 1.2f).coerceAtMost(1f),
            (it.green * 1.2f).coerceAtMost(1f),
            (it.blue * 1.2f).coerceAtMost(1f),
            it.alpha
        )
    }
    private val gbaDarkIndigo = gbaIndigo.copy(alpha = 1f).let {
        Color(
            (it.red * 0.7f).coerceAtMost(1f),
            (it.green * 0.7f).coerceAtMost(1f),
            (it.blue * 0.7f).coerceAtMost(1f),
            it.alpha
        )
    }

    override val level0Fill = gbaDarkIndigo.copy(alpha = 0.3f)
    override val level1Fill = gbaIndigo.copy(alpha = 0.2f)
    override val level2Fill = gbaLightIndigo.copy(alpha = 0.15f)
    override val level3Fill = gbaButtonColor.copy(alpha = 0.7f)
    override val level3FillPressed = gbaButtonColor.copy(alpha = 0.9f)
    
    override val bevelColorLight = Color.White.copy(alpha = 0.25f)
    override val bevelColorDark = Color.Black.copy(alpha = 0.5f)
    
    override val buttonCornerRadius = 12.dp // More rectangular/oval
}

/**
 * Gets a system-specific theme based on the system ID name (db name).
 * Since LemuroidPadTheme lives in the touchinput module, we can't import SystemID directly.
 * Instead, we use the system name string for comparison.
 */
fun getThemeForSystem(
    systemIdName: String?,
    shellColor: Color? = null,
    buttonColor: Color? = null,
): LemuroidPadTheme {
    return when (systemIdName) {
        "gb" -> DMGTheme(shellColor, buttonColor)
        "gbc" -> GBCTheme(shellColor, buttonColor)
        "gba" -> GBATheme(shellColor, buttonColor)
        else -> LemuroidPadTheme()
    }
}

val LocalLemuroidPadTheme =
    compositionLocalOf<LemuroidPadTheme> {
        error("LemuroidPadTheme is missing")
    }
