package com.swordfish.lemuroid.app.mobile.shared

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swordfish.lemuroid.app.mobile.feature.home.PressStart2PFontFamily

/**
 * Shared retro loading screen with ambient glowing background.
 */
@Composable
fun LoadingScreen(
    text: String = "LOADING...",
    glowColor: Color = RetroLcdGreen,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        DefaultBackground(
            glowColor = glowColor,
            modifier = Modifier.fillMaxSize()
        )

        Text(
            text = text.uppercase(),
            fontFamily = PressStart2PFontFamily,
            fontSize = 16.sp,
            lineHeight = 22.sp,
            color = Color.White.copy(alpha = 0.85f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}
