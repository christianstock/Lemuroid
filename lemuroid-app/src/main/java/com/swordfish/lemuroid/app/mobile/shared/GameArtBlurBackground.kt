package com.swordfish.lemuroid.app.mobile.shared

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
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
    Crossfade(
        targetState = coverFrontUrl,
        animationSpec = tween(durationMillis = 600),
        label = "BackgroundBlurCrossfade",
        modifier = modifier.fillMaxSize()
    ) { coverUrl ->
        if (!coverUrl.isNullOrEmpty()) {
            Box(modifier = Modifier.fillMaxSize()) {
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
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .drawWithContent {
                            drawContent()
                            drawRect(Color.Black.copy(alpha = 0.25f))
                            drawRect(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.5f),
                                        Color.Black.copy(alpha = 0.95f)
                                    ),
                                    center = center,
                                    radius = size.maxDimension * 0.7f
                                )
                            )
                        }
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawWithContent {
                        drawRect(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF1E1E24),
                                    Color.Black
                                ),
                                center = center,
                                radius = size.maxDimension * 0.75f
                            )
                        )
                    }
            )
        }
    }
}
