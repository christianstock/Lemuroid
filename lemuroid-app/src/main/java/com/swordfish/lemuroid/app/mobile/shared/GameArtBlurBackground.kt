package com.swordfish.lemuroid.app.mobile.shared

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
fun GameArtBlurBackground(
    coverFrontUrl: String?,
    modifier: Modifier = Modifier
) {
    var isInitialRender by remember { mutableStateOf(true) }

    val animationSpec: FiniteAnimationSpec<Float> = if (isInitialRender) {
        snap()
    } else {
        tween(durationMillis = 600)
    }

    SideEffect {
        isInitialRender = false
    }

    Crossfade(
        targetState = coverFrontUrl,
        animationSpec = animationSpec,
        label = "BackgroundBlurCrossfade",
        modifier = modifier.fillMaxSize()
    ) { coverUrl ->
        if (!coverUrl.isNullOrEmpty()) {
            BlurredCoverBackground(coverUrl = coverUrl)
        } else {
            FallbackGradientBackground()
        }
    }
}

@Composable
private fun BlurredCoverBackground(
    coverUrl: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .radialVignette(
                centerColor = Color.Transparent,
                radiusMultiplier = 0.75f
            )
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(coverUrl)
                .crossfade(true)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .blur(radius = 500.dp)
                .graphicsLayer {
                    scaleX = 1.2f
                    scaleY = 1.2f
                }
        )
    }
}

@Composable
private fun FallbackGradientBackground(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .radialVignette(
                centerColor = RetroLcdGreen,
                radiusMultiplier = 0.75f
            )
    )
}

fun Modifier.radialVignette(
    centerColor: Color = Color.Transparent,
    radiusMultiplier: Float = 0.75f
): Modifier = this.drawWithContent {
    drawContent()
    drawRect(Color.Black.copy(alpha = 0.25f))
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(
                centerColor,
                Color.Black.copy(alpha = 0.5f),
                Color.Black.copy(alpha = 0.95f)
            ),
            center = center,
            radius = size.maxDimension * radiusMultiplier
        )
    )
}
