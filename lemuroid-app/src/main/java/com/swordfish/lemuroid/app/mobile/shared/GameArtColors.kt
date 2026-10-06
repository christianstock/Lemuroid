package com.swordfish.lemuroid.app.mobile.shared

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.drawable.toBitmap
import androidx.palette.graphics.Palette
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult

suspend fun extractAccentColor(
    context: Context,
    coverUrl: String?,
    fallbackColor: Color
): Color {
    if (coverUrl.isNullOrEmpty()) return fallbackColor

    return try {
        val loader = ImageLoader(context)
        val request = ImageRequest.Builder(context)
            .data(coverUrl)
            .allowHardware(false)
            .build()

        val result = loader.execute(request)
        if (result is SuccessResult) {
            val bitmap = result.drawable.toBitmap()
            val palette = Palette.from(bitmap).generate()

            // 1. Calculate the average color of the cover art (simulates 1000dp blur)
            val ambientBgColor = calculateAverageColor(bitmap)

            // 2. Blend with the dark vignette overlay from GameInfoBackground
            // (0.25f black overlay + radial gradient (~0.5f average darkening))
            val compositeBgColor = ColorUtils.blendARGB(ambientBgColor, 0xFF000000.toInt(), 0.55f)

            val candidateSwatches = listOfNotNull(
                palette.vibrantSwatch,
                palette.lightVibrantSwatch,
                palette.dominantSwatch,
                palette.lightMutedSwatch,
                palette.mutedSwatch
            ) + palette.swatches.sortedByDescending { it.population }

            // 3. Find candidate with adequate contrast (>= 3.0:1 for large UI headers)
            candidateSwatches
                .map { Color(it.rgb) }
                .firstOrNull { candidate ->
                    ColorUtils.calculateContrast(candidate.toArgb(), compositeBgColor) >= 3.0
                } ?: fallbackColor
        } else {
            fallbackColor
        }
    } catch (_: Exception) {
        fallbackColor
    }
}

/**
 * Calculates the average ARGB color of a bitmap by downscaling it to 1x1.
 * This effectively computes the asymptotic color of an infinite/heavy blur.
 */
private fun calculateAverageColor(bitmap: Bitmap): Int {
    val scaled = Bitmap.createScaledBitmap(bitmap, 1, 1, true)
    val color = scaled.getPixel(0, 0)
    if (scaled != bitmap) {
        scaled.recycle()
    }
    return color
}
