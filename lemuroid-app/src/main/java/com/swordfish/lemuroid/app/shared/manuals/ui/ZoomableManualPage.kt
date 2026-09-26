package com.swordfish.lemuroid.app.shared.manuals.ui

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.swordfish.lemuroid.app.shared.manuals.ManualPageInfo

@Composable
fun ZoomableManualPage(
    page: ManualPageInfo,
    onZoomChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = { tapOffset ->
                        if (scale > 1f) {
                            scale = 1f
                            offset = Offset.Zero
                            onZoomChanged(false)
                        } else {
                            scale = 2.5f
                            // Center zoom pivot on tap location
                            offset = Offset(
                                x = (size.width / 2f - tapOffset.x) * (scale - 1f),
                                y = (size.height / 2f - tapOffset.y) * (scale - 1f)
                            )
                            onZoomChanged(true)
                        }
                    }
                )
            }
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    val newScale = (scale * zoom).coerceIn(1f, 5f)
                    val isZoomed = newScale > 1.05f
                    onZoomChanged(isZoomed)

                    if (!isZoomed) {
                        scale = 1f
                        offset = Offset.Zero
                    } else {
                        scale = newScale
                        // Bound pan offset so user can't drag image completely off-screen
                        val maxOffsetX = (size.width * (scale - 1f)) / 2f
                        val maxOffsetY = (size.height * (scale - 1f)) / 2f

                        val newOffsetX = (offset.x + pan.x).coerceIn(-maxOffsetX, maxOffsetX)
                        val newOffsetY = (offset.y + pan.y).coerceIn(-maxOffsetY, maxOffsetY)

                        offset = Offset(newOffsetX, newOffsetY)
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = page.filePath,
            contentDescription = "Manual Page ${page.pageIndex + 1}",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y
                )
        )
    }
}
