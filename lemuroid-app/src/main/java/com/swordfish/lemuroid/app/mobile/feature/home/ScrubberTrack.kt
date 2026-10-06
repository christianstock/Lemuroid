package com.swordfish.lemuroid.app.mobile.feature.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

/**
 * Adaptive Scrubber Track: Renders small dots for items with active selection always shown as a diamond.
 * Dynamically falls back to a continuous line track when items exceed available space.
 *
 * @param pagerState The Compose [PagerState] driving the current page selection and offset.
 * @param itemCount Total number of items/pages to render in the scrubber loop.
 * @param accentColor Active selection diamond color.
 * @param modifier Standard layout modifier.
 */

/**
 * Adaptive Scrubber Track: Renders small dots for items with active selection always shown as a diamond.
 */
@Composable
fun ScrubberTrack(
    pagerState: PagerState,
    itemCount: Int,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    if (itemCount <= 0) return

    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current

    val minDotSpacingPx = with(density) { 8.dp.toPx() }
    val smallDotRadiusPx = with(density) { 2.dp.toPx() }
    val diamondRadiusPx = with(density) { 5.5.dp.toPx() }

    Box(
        modifier = modifier
            .height(28.dp)
            .pointerInput(itemCount, pagerState) {
                fun updatePosition(xPx: Float) {
                    if (itemCount <= 1 || size.width <= 0) return
                    val fraction = (xPx / size.width.toFloat()).coerceIn(0f, 1f)
                    val targetIndexWithinList = (fraction * (itemCount - 1)).roundToInt()

                    val currentListIndex = pagerState.currentPage % itemCount
                    val delta = targetIndexWithinList - currentListIndex
                    val targetPage = pagerState.currentPage + delta

                    coroutineScope.launch {
                        pagerState.scrollToPage(targetPage)
                    }
                }

                detectTapGestures { offset -> updatePosition(offset.x) }
            }
            .pointerInput(itemCount, pagerState) {
                detectDragGestures { change, _ ->
                    change.consume()
                    if (itemCount <= 1 || size.width <= 0) return@detectDragGestures
                    val fraction = (change.position.x / size.width.toFloat()).coerceIn(0f, 1f)
                    val targetIndexWithinList = (fraction * (itemCount - 1)).roundToInt()

                    val currentListIndex = pagerState.currentPage % itemCount
                    val delta = targetIndexWithinList - currentListIndex
                    val targetPage = pagerState.currentPage + delta

                    coroutineScope.launch {
                        pagerState.scrollToPage(targetPage)
                    }
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val cy = size.height / 2f

            val currentModuloIndex = pagerState.currentPage % itemCount
            val rawIndex = currentModuloIndex + pagerState.currentPageOffsetFraction
            val normalizedIndex = (rawIndex % itemCount + itemCount) % itemCount

            val requiredWidth = itemCount * minDotSpacingPx
            val renderDots = itemCount in 2..50 && requiredWidth <= width

            if (renderDots) {
                val step = if (itemCount > 1) width / (itemCount - 1) else 0f

                for (i in 0 until itemCount) {
                    val cx = i * step
                    val distToActive = (i - normalizedIndex).absoluteValue
                    val isSelected = distToActive < 0.5f

                    if (isSelected) {
                        val diamondPath = Path().apply {
                            moveTo(cx, cy - diamondRadiusPx)
                            lineTo(cx + diamondRadiusPx, cy)
                            lineTo(cx, cy + diamondRadiusPx)
                            lineTo(cx - diamondRadiusPx, cy)
                            close()
                        }
                        drawPath(path = diamondPath, color = accentColor)
                    } else {
                        drawCircle(
                            color = Color.White.copy(alpha = 0.3f),
                            radius = smallDotRadiusPx,
                            center = Offset(cx, cy)
                        )
                    }
                }
            } else {
                val trackHeightPx = 2.dp.toPx()
                drawLine(
                    color = Color.White.copy(alpha = 0.25f),
                    start = Offset(0f, cy),
                    end = Offset(width, cy),
                    strokeWidth = trackHeightPx
                )

                val fraction = if (itemCount <= 1) 0.5f else (normalizedIndex / (itemCount - 1)).coerceIn(0f, 1f)
                val diamondX = width * fraction

                val diamondPath = Path().apply {
                    moveTo(diamondX, cy - diamondRadiusPx)
                    lineTo(diamondX + diamondRadiusPx, cy)
                    lineTo(diamondX, cy + diamondRadiusPx)
                    lineTo(diamondX - diamondRadiusPx, cy)
                    close()
                }

                drawPath(
                    path = diamondPath,
                    color = accentColor
                )
            }
        }
    }
}
