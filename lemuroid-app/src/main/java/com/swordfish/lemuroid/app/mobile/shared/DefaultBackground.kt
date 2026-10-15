package com.swordfish.lemuroid.app.mobile.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun DefaultBackground(
    modifier: Modifier = Modifier,
    glowColor: Color = RetroLcdGreen,
    isCentered: Boolean = true,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(glowColor)
            .background(Color.Black.copy(alpha = 0.5f))
            .radialVignette(
                centerColor = Color.Transparent,
                radiusMultiplier = 0.85f,
                verticalBias = if (isCentered) 0.5f else 0.35f
            )
    )
}
