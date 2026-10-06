package com.swordfish.lemuroid.app.mobile.shared

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swordfish.lemuroid.app.mobile.feature.gameinfo.PressStart2PFont

/**
 * Reusable retro-styled back button pinned to the bottom of the container.
 * Handles navigation bar padding internally.
 */
@Composable
fun RetroBackButton(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    bottomPadding: Dp = 12.dp,
    textColor: Color = Color.White.copy(alpha = 0.7f)
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(bottom = bottomPadding),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "< BACK",
            fontFamily = PressStart2PFont,
            fontSize = 10.sp,
            color = textColor,
            fontWeight = FontWeight.Normal,
            modifier = Modifier
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onBack
                )
                .padding(vertical = 12.dp, horizontal = 24.dp)
        )
    }
}
