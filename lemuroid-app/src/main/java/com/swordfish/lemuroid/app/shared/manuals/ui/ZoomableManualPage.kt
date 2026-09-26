package com.swordfish.lemuroid.app.shared.manuals.ui

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.swordfish.lemuroid.app.shared.manuals.ManualPageInfo
import java.io.File

@Composable
fun ZoomableManualPage(
    page: ManualPageInfo,
    onZoomChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    // Reset zoom state on page change
    LaunchedEffect(page.filePath) {
        scale = 1f
        offset = Offset.Zero
        onZoomChanged(false)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(page.filePath) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    do {
                        val event = awaitPointerEvent()
                        val zoom = event.calculateZoom()
                        val pan = event.calculatePan()

                        // Calculate new scale
                        val targetScale = (scale * zoom).coerceIn(1f, 4f)
                        val isZoomed = targetScale > 1f

                        if (scale != targetScale) {
                            scale = targetScale
                            onZoomChanged(isZoomed)
                        }

                        if (isZoomed) {
                            // When zoomed in, consume touch events to allow panning
                            offset += pan
                            event.changes.forEach { change ->
                                if (change.positionChanged()) {
                                    change.consume()
                                }
                            }
                        } else {
                            // At 1x scale, do NOT consume horizontal movement so HorizontalPager can swipe
                            offset = Offset.Zero
                        }
                    } while (event.changes.any { it.pressed })
                }
            },
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = File(page.filePath),
            contentDescription = "Manual Page ${page.pageIndex + 1}",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = if (scale > 1f) offset.x else 0f,
                    translationY = if (scale > 1f) offset.y else 0f
                )
        )
    }
}
