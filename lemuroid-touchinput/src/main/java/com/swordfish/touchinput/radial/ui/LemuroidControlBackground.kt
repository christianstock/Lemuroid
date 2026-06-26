package com.swordfish.touchinput.radial.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.swordfish.touchinput.radial.LocalLemuroidPadTheme

@Composable
fun LemuroidControlBackground(modifier: Modifier = Modifier) {
    val theme = LocalLemuroidPadTheme.current
    GlassSurface(
        modifier = modifier.fillMaxSize(),
        fillColor = theme.level1Fill,
        shadowColor = theme.level1Shadow,
        shadowWidth = theme.level1ShadowWidth,
    )
}

@Composable
fun GbaSideControlBackground(modifier: Modifier = Modifier) {
    val theme = LocalLemuroidPadTheme.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .drawBehind {
                val strokeWidthPx = 2.dp.toPx() // Adjust thickness here
                val outlineColor = theme.level1Shadow // Uses the theme outline/shadow tint
                val cornerRadiusPx = CornerRadius(12.dp.toPx()) // Define the roundness here

                // Draws a clean, rounded outline matching the exact size of the container
                drawRoundRect(
                    color = outlineColor,
                    style = Stroke(width = strokeWidthPx),
                    cornerRadius = cornerRadiusPx
                )
            }
    )
}
