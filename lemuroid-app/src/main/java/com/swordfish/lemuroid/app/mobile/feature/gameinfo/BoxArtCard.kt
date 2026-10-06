package com.swordfish.lemuroid.app.mobile.feature.gameinfo

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.swordfish.lemuroid.app.mobile.shared.HoldGestureOverlay
import kotlinx.coroutines.launch

@Composable
fun BoxArtCard(
    frontImageUrl: String?,
    backImageUrl: String?,
    rotationDegrees: Float,
    idleRotationZ: Float,
    canSwipeBack: Boolean,
    tintColor: Color,
    onOpenFullScreen: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var isHolding by remember { mutableStateOf(false) }
    var pressOffset by remember { mutableStateOf(Offset.Unspecified) }
    val holdProgressAnim = remember { Animatable(0f) }

    Box(
        modifier = modifier
            .fillMaxWidth(0.85f)
            .graphicsLayer {
                cameraDistance = 12f * density
                if (canSwipeBack && rotationDegrees > 0f) {
                    rotationY = -rotationDegrees
                    rotationZ = idleRotationZ
                } else {
                    rotationX = 2f
                    rotationZ = idleRotationZ
                }
            }
            .background(Color.White)
            .padding(10.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { offset ->
                        pressOffset = offset
                        isHolding = true
                        val holdJob = coroutineScope.launch {
                            holdProgressAnim.snapTo(0f)
                            holdProgressAnim.animateTo(
                                targetValue = 1f,
                                animationSpec = tween(durationMillis = 400)
                            )
                            if (holdProgressAnim.value >= 1f) {
                                onOpenFullScreen()
                            }
                        }
                        tryAwaitRelease()
                        isHolding = false
                        holdJob.cancel()
                        coroutineScope.launch {
                            holdProgressAnim.animateTo(0f, tween(150))
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // Front Face
        if (frontImageUrl != null) {
            AsyncImage(
                model = frontImageUrl,
                contentDescription = "Front Box Art",
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        alpha = if (rotationDegrees <= 90f) 1f else 0f
                    },
                contentScale = ContentScale.FillWidth
            )
        }

        // Back Face
        if (backImageUrl != null) {
            AsyncImage(
                model = backImageUrl,
                contentDescription = "Back Box Art",
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        alpha = if (rotationDegrees > 90f) 1f else 0f
                        rotationY = 180f
                    },
                contentScale = ContentScale.FillWidth
            )
        }

        // Gesture Overlay
        if (isHolding || holdProgressAnim.value > 0f) {
            HoldGestureOverlay(
                pressOffset = pressOffset,
                holdProgress = holdProgressAnim.value,
                tintColor = tintColor,
                modifier = Modifier.matchParentSize()
            )
        }
    }
}
