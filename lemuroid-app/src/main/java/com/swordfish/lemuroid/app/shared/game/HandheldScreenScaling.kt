package com.swordfish.lemuroid.app.shared.game

import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.swordfish.lemuroid.lib.library.GameSystem
import timber.log.Timber

/**
 * Handles DPI-aware screen sizing for authentic handheld emulation (GB, GBC, GBA).
 * Scales the game screen to match real-world handheld dimensions in mm,
 * adjusted for device screen density and available space.
 */
object HandheldScreenScaling {

    /**
     * Physical dimensions of authentic handheld devices in millimeters
     */
    data class HandheldDimensions(
        val widthMm: Float,
        val heightMm: Float,
    )

    /**
     * Calculated scaling result with target dimensions
     */
    data class ScalingResult(
        val targetWidthDp: Float,
        val targetHeightDp: Float,
        val scaledToFit: Boolean, // true if scaled down due to screen constraints
    )

    /**
     * Get the physical dimensions (in mm) for a given game system
     */
    fun getHandheldDimensions(system: GameSystem): HandheldDimensions? {
        return when (system.id.name) {
            "GB" -> HandheldDimensions(widthMm = 47f, heightMm = 43f)
            "GBC" -> HandheldDimensions(widthMm = 43f, heightMm = 39f)  // Updated: 44x40 -> 43x39
            "GBA" -> HandheldDimensions(widthMm = 61.2f, heightMm = 40.8f)
            else -> null
        }
    }

    /**
     * Calculate the scaled screen dimensions based on device DPI and available space.
     *
     * @param system The game system (GB, GBC, GBA)
     * @param density The Compose Density for the current screen
     * @param maxAvailableWidthDp Maximum width available on screen in DP
     * @param maxAvailableHeightDp Maximum height available on screen in DP
     * @return ScalingResult with calculated dimensions, or null if system is not a handheld
     */
    fun calculateScreenDimensions(
        system: GameSystem,
        density: Density,
        maxAvailableWidthDp: Float,
        maxAvailableHeightDp: Float,
    ): ScalingResult? {
        val dimensions = getHandheldDimensions(system) ?: return null

        // Convert physical mm to pixels using DPI-aware calculation
        // Formula: pixels = mm * (density.density * 160f / 25.4f)
        // where 160 is standard DPI and 25.4 mm/inch
        val dotsPerMm = density.density * 160f / 25.4f

        var targetWidthDp = (dimensions.widthMm * dotsPerMm) / density.density
        var targetHeightDp = (dimensions.heightMm * dotsPerMm) / density.density

        var scaledToFit = false

        Timber.d("HandheldScreenScaling [${system.id.name}]: Physical=${dimensions.widthMm}mm×${dimensions.heightMm}mm, Density=${density.density}, DotsPerMm=$dotsPerMm")
        Timber.d("HandheldScreenScaling: Available=${maxAvailableWidthDp}dp×${maxAvailableHeightDp}dp")
        Timber.d("HandheldScreenScaling: Initial target=${targetWidthDp}dp×${targetHeightDp}dp")

        // Check if dimensions fit within available space, scale down if necessary
        if (targetWidthDp > maxAvailableWidthDp || targetHeightDp > maxAvailableHeightDp) {
            // Calculate scale factor to fit within constraints while maintaining aspect ratio
            val widthScale = maxAvailableWidthDp / targetWidthDp
            val heightScale = maxAvailableHeightDp / targetHeightDp
            val scale = minOf(widthScale, heightScale, 1f) // Don't scale up, only down

            targetWidthDp *= scale
            targetHeightDp *= scale
            scaledToFit = true
            Timber.d("HandheldScreenScaling: Scaled down by factor=${scale} (Width:$widthScale, Height:$heightScale)")
        }

        Timber.d("HandheldScreenScaling: Final=${targetWidthDp}dp×${targetHeightDp}dp (scaledToFit=$scaledToFit)")

        return ScalingResult(
            targetWidthDp = targetWidthDp,
            targetHeightDp = targetHeightDp,
            scaledToFit = scaledToFit,
        )
    }
}


