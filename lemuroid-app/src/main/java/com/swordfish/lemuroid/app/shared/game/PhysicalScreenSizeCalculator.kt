package com.swordfish.lemuroid.app.shared.game

import android.util.DisplayMetrics
import com.swordfish.lemuroid.lib.library.SystemID

/**
 * Calculates physical screen dimensions for retro handhelds (GB, GBC, GBA) based on device DPI.
 * Renders games at their actual physical size on the phone's screen.
 */
object PhysicalScreenSizeCalculator {

    /**
     * Physical dimensions of retro handhelds in millimeters (Width x Height)
     */
    private data class PhysicalDimensions(
        val widthMm: Float,
        val heightMm: Float,
    )

    private val HANDHELD_DIMENSIONS = mapOf(
        SystemID.GB to PhysicalDimensions(widthMm = 45.5f, heightMm = 41.5f),
        SystemID.GBC to PhysicalDimensions(widthMm = 43.0f, heightMm = 39.0f),
        SystemID.GBA to PhysicalDimensions(widthMm = 61.2f, heightMm = 40.8f),
    )

    /**
     * Result of the screen size calculation
     */
    data class ScreenDimensions(
        val widthPx: Float,
        val heightPx: Float,
        val isScaledDown: Boolean, // true if dimensions were scaled down to fit available space
    )

    /**
     * Calculates the physical screen size in pixels for a handheld game.
     * Only returns dimensions if they fit within available space (no scaling/margin).
     * If the game screen would exceed boundaries, returns null to use default full-screen rendering.
     *
     * @param systemId The game system ID
     * @param displayMetrics Android display metrics (contains DPI info)
     * @param maxAvailableWidthPx Maximum available width in pixels
     * @param maxAvailableHeightPx Maximum available height in pixels
     * @return ScreenDimensions with calculated width/height in pixels, or null if not a handheld system or if it doesn't fit
     */
    fun calculateScreenDimensions(
        systemId: SystemID,
        displayMetrics: DisplayMetrics,
        maxAvailableWidthPx: Float,
        maxAvailableHeightPx: Float,
    ): ScreenDimensions? {
        val dimensions = HANDHELD_DIMENSIONS[systemId] ?: return null

        // Use xdpi/ydpi for more accurate physical measurements if available
        // Fall back to densityDpi if values are suspicious (e.g. 0 or extremely high/low)
        var xdpi = displayMetrics.xdpi
        var ydpi = displayMetrics.ydpi
        if (xdpi < 50f || xdpi > 1000f) {
            xdpi = displayMetrics.densityDpi.toFloat()
        }
        if (ydpi < 50f || ydpi > 1000f) {
            ydpi = displayMetrics.densityDpi.toFloat()
        }

        // Convert physical mm to pixels using device DPI
        // Formula: Pixels = (mm / 25.4) * DPI
        val widthPx = (dimensions.widthMm / 25.4f) * xdpi
        val heightPx = (dimensions.heightMm / 25.4f) * ydpi

        // If dimensions don't fit within available space, fall back to default full-screen rendering
        val fitsWidth = widthPx <= maxAvailableWidthPx
        val fitsHeight = heightPx <= maxAvailableHeightPx

        if (!fitsWidth || !fitsHeight) {
            return null
        }

        return ScreenDimensions(
            widthPx = widthPx,
            heightPx = heightPx,
            isScaledDown = false,
        )
    }

    /**
     * Checks if a system should use physical screen size rendering
     */
    fun isPhysicalSizeSupported(systemId: SystemID): Boolean {
        return systemId in HANDHELD_DIMENSIONS
    }
}

