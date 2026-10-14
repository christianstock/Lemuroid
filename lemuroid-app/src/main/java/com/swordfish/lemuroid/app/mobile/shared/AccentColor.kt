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

            val ambientBgColor = calculateAverageColor(bitmap)
            val compositeBgColor = ColorUtils.blendARGB(ambientBgColor, 0xFF000000.toInt(), 0.55f)

            val candidateSwatches = listOfNotNull(
                palette.vibrantSwatch,
                palette.lightVibrantSwatch,
                palette.dominantSwatch,
                palette.lightMutedSwatch,
                palette.mutedSwatch
            ) + palette.swatches.sortedByDescending { it.population }

            candidateSwatches
                .map { Color(it.rgb) }
                .map { candidate -> adjustCandidateForReadability(candidate, compositeBgColor) }
                .firstOrNull { candidate ->
                    passesPerceptualContrast(candidate.toArgb(), compositeBgColor)
                } ?: fallbackColor
        } else {
            fallbackColor
        }
    } catch (_: Exception) {
        fallbackColor
    }
}

private fun calculateAverageColor(bitmap: Bitmap): Int {
    val scaled = Bitmap.createScaledBitmap(bitmap, 1, 1, true)
    val color = scaled.getPixel(0, 0)
    if (scaled != bitmap) {
        scaled.recycle()
    }
    return color
}

private fun adjustCandidateForReadability(color: Color, bgArgb: Int): Color {
    val hsl = FloatArray(3)
    ColorUtils.colorToHSL(color.toArgb(), hsl)

    val hue = hsl[0]        // 0..360
    val saturation = hsl[1] // 0..1
    val lightness = hsl[2]  // 0..1

    val isWarmHue = hue in 0f..30f || hue in 340f..360f

    if (ColorUtils.calculateContrast(color.toArgb(), bgArgb) >= 3.0) {
        return color
    }

    if (isWarmHue && lightness < 0.7f) {
        hsl[2] = (lightness + 0.2f).coerceAtMost(0.85f) // brighten warm colors
        hsl[1] = saturation.coerceAtLeast(0.6f)         // keep high saturation

        val adjustedArgb = ColorUtils.HSLToColor(hsl)
        if (ColorUtils.calculateContrast(adjustedArgb, bgArgb) >= 3.0) {
            return Color(adjustedArgb)
        }
    }

    hsl[2] = lightness.coerceAtLeast(0.70f)
    return Color(ColorUtils.HSLToColor(hsl))
}

private fun passesPerceptualContrast(candidateArgb: Int, bgArgb: Int): Boolean {
    val contrast = ColorUtils.calculateContrast(candidateArgb, bgArgb)

    val hsl = FloatArray(3)
    ColorUtils.colorToHSL(candidateArgb, hsl)
    val hue = hsl[0]
    val saturation = hsl[1]

    val isWarmHue = hue in 0f..35f || hue in 335f..360f

    val minThreshold = if (isWarmHue && saturation > 0.5f) 2.4 else 3.0

    return contrast >= minThreshold
}
