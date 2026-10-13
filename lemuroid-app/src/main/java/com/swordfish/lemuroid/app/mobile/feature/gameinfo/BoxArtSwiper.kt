package com.swordfish.lemuroid.app.mobile.feature.gameinfo

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.AnchoredDraggableDefaults
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.abs

enum class ArtSide {
    FRONT, BACK
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BoxArtSwiper(
    frontImageUrl: String?,
    backImageUrl: String?,
    tintColor: Color = Color.White,
    modifier: Modifier = Modifier
) {
    if (frontImageUrl == null && backImageUrl == null) return

    var isFullScreenOpen by remember { mutableStateOf(false) }
    var containerWidthPx by remember { mutableFloatStateOf(0f) }
    val canSwipeBack = backImageUrl != null

    var currentSide by remember { mutableStateOf(ArtSide.FRONT) }
    val coroutineScope = rememberCoroutineScope()

    val state = remember { AnchoredDraggableState(initialValue = ArtSide.FRONT) }

    val flingBehavior = AnchoredDraggableDefaults.flingBehavior(
        state = state,
        positionalThreshold = { distance: Float -> distance * 0.5f },
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    SideEffect {
        val newAnchors = DraggableAnchors {
            ArtSide.FRONT at 0f
            if (canSwipeBack && containerWidthPx > 0f) {
                ArtSide.BACK at -containerWidthPx
            }
        }
        state.updateAnchors(newAnchors)
    }

    val currentOffset = if (state.offset.isNaN()) 0f else state.offset
    val currentImageUrl = if (currentSide == ArtSide.FRONT) frontImageUrl else backImageUrl

    val rotationDegrees = if (containerWidthPx > 0f) {
        (abs(currentOffset) / containerWidthPx * 180f).coerceIn(0f, 180f)
    } else 0f

    val infiniteTransition = rememberInfiniteTransition(label = "box-art-rotation")
    val idleRotationZ by infiniteTransition.animateFloat(
        initialValue = -1.5f,
        targetValue = -1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "idle-rotation"
    )

    val chevronAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "chevron-pulse"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Left Chevron
            if (canSwipeBack && currentSide == ArtSide.BACK) {
                AnimatedVisibility(
                    visible = containerWidthPx > 0f,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ChevronLeft,
                        contentDescription = "Tap or swipe to see front art",
                        tint = tintColor,
                        modifier = Modifier
                            .alpha(chevronAlpha)
                            .padding(end = 4.dp)
                            .size(36.dp)
                            .clickable {
                                coroutineScope.launch { state.animateTo(ArtSide.FRONT) }
                            }
                    )
                }
            }

            // Central Card Container
            Box(
                modifier = Modifier
                    .weight(1f)
                    .onSizeChanged { containerWidthPx = it.width.toFloat() }
                    .anchoredDraggable(
                        state = state,
                        orientation = Orientation.Horizontal,
                        enabled = canSwipeBack && containerWidthPx > 0f,
                        flingBehavior = flingBehavior
                    ),
                contentAlignment = Alignment.Center
            ) {
                BoxArtCard(
                    frontImageUrl = frontImageUrl,
                    backImageUrl = backImageUrl,
                    rotationDegrees = rotationDegrees,
                    idleRotationZ = idleRotationZ,
                    canSwipeBack = canSwipeBack,
                    tintColor = tintColor,
                    onOpenFullScreen = { isFullScreenOpen = true }
                )
            }

            // Sync side state
            LaunchedEffect(currentOffset) {
                if (containerWidthPx > 0f) {
                    val dragThreshold = containerWidthPx * 0.5f
                    currentSide = if (abs(currentOffset) >= dragThreshold) ArtSide.BACK else ArtSide.FRONT
                }
            }

            // Right Chevron
            if (canSwipeBack && currentSide == ArtSide.FRONT) {
                AnimatedVisibility(
                    visible = containerWidthPx > 0f,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ChevronRight,
                        contentDescription = "Tap or swipe to see back art",
                        tint = tintColor,
                        modifier = Modifier
                            .alpha(chevronAlpha)
                            .padding(start = 4.dp)
                            .size(36.dp)
                            .clickable {
                                coroutineScope.launch { state.animateTo(ArtSide.BACK) }
                            }
                    )
                }
            }
        }
    }

    // Fullscreen Overlay
    if (isFullScreenOpen && currentImageUrl != null) {
        FullBoxArtScreen(
            imageUrl = currentImageUrl,
            onDismiss = { isFullScreenOpen = false }
        )
    }
}
