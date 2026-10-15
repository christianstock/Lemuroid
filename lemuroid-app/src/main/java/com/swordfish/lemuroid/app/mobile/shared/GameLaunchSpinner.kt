package com.swordfish.lemuroid.app.mobile.shared

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.LemuroidGameImage
import com.swordfish.lemuroid.lib.library.db.entity.Game

@Composable
fun GameLaunchSpinner(
    game: Game?,
    onLaunchTriggered: (Game) -> Unit,
    onAnimationComplete: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(4.dp)
) {
    if (game == null) return

    val animProgress = remember { Animatable(0f) }

    LaunchedEffect(game) {
        onLaunchTriggered(game)

        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 600, easing = LinearOutSlowInEasing)
        )

        onAnimationComplete()
    }

    val progress = animProgress.value

    // Use zIndex and absolute fill without interfering with parent Scaffold bounds
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = (progress * 1.2f).coerceIn(0f, 1f)))
            .zIndex(999f),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size((180 + progress * 160).dp)
                .graphicsLayer {
                    rotationZ = progress * 360f
                    alpha = (1f - (progress - 0.7f) * 3.3f).coerceIn(0f, 1f)
                }
                .clip(shape)
        ) {
            LemuroidGameImage(
                game = game,
                modifier = Modifier.fillMaxSize(),
                applyAspectRatio = false,
                contentScale = ContentScale.Crop
            )
        }
    }
}
