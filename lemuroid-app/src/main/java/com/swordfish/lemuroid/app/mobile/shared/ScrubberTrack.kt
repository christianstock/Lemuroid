package com.swordfish.lemuroid.app.mobile.shared

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

@Composable
fun ScrubberTrack(
    pagerState: PagerState,
    itemCount: Int,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    if (itemCount <= 1) {
        Box(modifier = modifier.height(28.dp))
        return
    }

    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current

    val minDotSpacingPx = with(density) { 8.dp.toPx() }
    val smallDotRadiusPx = with(density) { 2.dp.toPx() }
    val diamondRadiusPx = with(density) { 5.5.dp.toPx() }

    // Single reusable Path instance across composition frames
    val diamondPath = remember { Path() }

    Box(
        modifier = modifier
            .height(28.dp)
            .scrubberGestureInput(
                itemCount = itemCount,
                pagerState = pagerState,
                coroutineScope = coroutineScope
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cy = size.height / 2f
            val currentModuloIndex = pagerState.currentPage % itemCount
            val rawIndex = currentModuloIndex + pagerState.currentPageOffsetFraction
            val normalizedIndex = (rawIndex % itemCount + itemCount) % itemCount

            val requiredWidth = itemCount * minDotSpacingPx
            val renderDots = itemCount in 2..50 && requiredWidth <= size.width

            if (renderDots) {
                drawDotsTrack(
                    itemCount = itemCount,
                    normalizedIndex = normalizedIndex,
                    cy = cy,
                    smallDotRadiusPx = smallDotRadiusPx,
                    diamondRadiusPx = diamondRadiusPx,
                    accentColor = accentColor,
                    diamondPath = diamondPath
                )
            } else {
                drawLineTrack(
                    itemCount = itemCount,
                    normalizedIndex = normalizedIndex,
                    cy = cy,
                    diamondRadiusPx = diamondRadiusPx,
                    accentColor = accentColor,
                    diamondPath = diamondPath
                )
            }
        }
    }
}

// ============================================================================
// Gesture Handling
// ============================================================================

private fun Modifier.scrubberGestureInput(
    itemCount: Int,
    pagerState: PagerState,
    coroutineScope: CoroutineScope
): Modifier = this
    .pointerInput(itemCount, pagerState) {
        detectTapGestures { offset ->
            onPositionUpdate(offset.x, size.width.toFloat(), itemCount, pagerState, coroutineScope)
        }
    }
    .pointerInput(itemCount, pagerState) {
        detectDragGestures { change, _ ->
            change.consume()
            onPositionUpdate(change.position.x, size.width.toFloat(), itemCount, pagerState, coroutineScope)
        }
    }

private fun onPositionUpdate(
    xPx: Float,
    widthPx: Float,
    itemCount: Int,
    pagerState: PagerState,
    coroutineScope: CoroutineScope
) {
    if (itemCount <= 1 || widthPx <= 0f) return
    val targetPage = calculateTargetPage(xPx, widthPx, itemCount, pagerState.currentPage)
    coroutineScope.launch {
        pagerState.scrollToPage(targetPage)
    }
}

private fun calculateTargetPage(
    xPx: Float,
    widthPx: Float,
    itemCount: Int,
    currentPage: Int
): Int {
    val fraction = (xPx / widthPx).coerceIn(0f, 1f)
    val targetIndexWithinList = (fraction * (itemCount - 1)).roundToInt()
    val currentListIndex = currentPage % itemCount
    val delta = targetIndexWithinList - currentListIndex
    return currentPage + delta
}

// ============================================================================
// Canvas Drawing Components
// ============================================================================

private fun DrawScope.drawDotsTrack(
    itemCount: Int,
    normalizedIndex: Float,
    cy: Float,
    smallDotRadiusPx: Float,
    diamondRadiusPx: Float,
    accentColor: Color,
    diamondPath: Path
) {
    val step = size.width / (itemCount - 1)

    for (i in 0 until itemCount) {
        val cx = i * step
        val distToActive = (i - normalizedIndex).absoluteValue

        if (distToActive < 0.5f) {
            drawDiamond(
                cx = cx,
                cy = cy,
                radiusPx = diamondRadiusPx,
                color = accentColor,
                path = diamondPath
            )
        } else {
            drawCircle(
                color = Color.White.copy(alpha = 0.3f),
                radius = smallDotRadiusPx,
                center = Offset(cx, cy)
            )
        }
    }
}

private fun DrawScope.drawLineTrack(
    itemCount: Int,
    normalizedIndex: Float,
    cy: Float,
    diamondRadiusPx: Float,
    accentColor: Color,
    diamondPath: Path
) {
    val trackHeightPx = 2.dp.toPx()
    drawLine(
        color = Color.White.copy(alpha = 0.25f),
        start = Offset(0f, cy),
        end = Offset(size.width, cy),
        strokeWidth = trackHeightPx
    )

    val fraction = (normalizedIndex / (itemCount - 1)).coerceIn(0f, 1f)
    val diamondX = size.width * fraction
    drawDiamond(
        cx = diamondX,
        cy = cy,
        radiusPx = diamondRadiusPx,
        color = accentColor,
        path = diamondPath
    )
}

private fun DrawScope.drawDiamond(
    cx: Float,
    cy: Float,
    radiusPx: Float,
    color: Color,
    path: Path
) {
    path.reset()
    path.moveTo(cx, cy - radiusPx)
    path.lineTo(cx + radiusPx, cy)
    path.lineTo(cx, cy + radiusPx)
    path.lineTo(cx - radiusPx, cy)
    path.close()

    drawPath(path = path, color = color)
}
