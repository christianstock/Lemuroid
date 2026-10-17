package com.swordfish.lemuroid.app.shared.achievements

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.swordfish.lemuroid.app.mobile.shared.DefaultBackground
import com.swordfish.lemuroid.app.mobile.shared.RetroBackButton

@Composable
fun GameAchievementsViewer(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        // 1. Default Background
        DefaultBackground(
            modifier = modifier.fillMaxSize(),
        )

        // 2. Bottom Retro Back Button
        RetroBackButton(
            onBack = onDismiss,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}
