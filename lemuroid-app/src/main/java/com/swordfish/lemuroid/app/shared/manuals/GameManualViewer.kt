package com.swordfish.lemuroid.app.shared.manuals

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.swordfish.lemuroid.app.mobile.shared.DefaultBackground
import com.swordfish.lemuroid.app.mobile.shared.RetroBackButton
import com.swordfish.lemuroid.app.mobile.shared.ScrubberTrack
import kotlin.math.floor

@SuppressLint("NewApi")
@Composable
fun GameManualViewer(
    pages: List<ManualPageInfo>,
    isVisible: Boolean,
    onDismiss: () -> Unit,
    accentColor: Color = Color.White,
    modifier: Modifier = Modifier.fillMaxSize()
) {
    if (pages.isEmpty()) return

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        val pagerState = rememberPagerState(initialPage = 0, pageCount = { pages.size })
        var scale by remember { mutableFloatStateOf(1f) }
        var offset by remember { mutableStateOf(Offset.Zero) }
        var viewSize by remember { mutableStateOf(IntSize.Zero) }

        LaunchedEffect(pagerState.settledPage) {
            scale = 1f
            offset = Offset.Zero
        }

        val position = {
            (pagerState.currentPage + pagerState.currentPageOffsetFraction)
                .coerceIn(0f, pages.lastIndex.toFloat())
        }
        val base by remember {
            derivedStateOf { floor(position()).toInt().coerceIn(0, pages.lastIndex) }
        }

        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black)
                .onSizeChanged { viewSize = it }
        ) {
            // 1. Atmospheric Background pinned to the absolute bottom z-index
            DefaultBackground(
                //glowColor = accentColor,
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(-999f) // 👈 Ensures it sits behind ALL negative zIndex pages
            )

            // 2. Manual Page Window Stack (Next pages underneath, current page on top)
            val window = ((base - 1)..(base + 2)).filter { it in pages.indices }
            for (index in window) {
                key(index) {
                    PageLayer(
                        page = pages[index],
                        index = index,
                        position = position,
                        zoomedPage = { pagerState.currentPage },
                        scale = { scale },
                        offset = { offset }
                    )
                }
            }

            // 3. Gesture Pager overlay
            HorizontalPager(
                state = pagerState,
                userScrollEnabled = scale <= 1.01f,
                modifier = Modifier.fillMaxSize(),
                pageSpacing = 0.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            awaitEachGesture {
                                awaitFirstDown(requireUnconsumed = false)
                                do {
                                    val event = awaitPointerEvent()
                                    val zoom = event.calculateZoom()
                                    val pan = event.calculatePan()
                                    val newScale = (scale * zoom).coerceIn(1f, 4f)

                                    if (newScale > 1f) {
                                        scale = newScale
                                        val maxX = viewSize.width * (newScale - 1f) / 2f
                                        val maxY = viewSize.height * (newScale - 1f) / 2f
                                        offset = Offset(
                                            (offset.x + pan.x).coerceIn(-maxX, maxX),
                                            (offset.y + pan.y).coerceIn(-maxY, maxY)
                                        )
                                        event.changes.forEach {
                                            if (it.positionChanged()) it.consume()
                                        }
                                    } else {
                                        scale = 1f
                                        offset = Offset.Zero
                                    }
                                } while (event.changes.any { it.pressed })
                            }
                        }
                )
            }

            // 4. Top Scrubber Track
            ScrubberTrack(
                pagerState = pagerState,
                itemCount = pages.size,
                accentColor = accentColor,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
                    .fillMaxWidth()
            )

            // 5. Bottom Retro Back Button
            RetroBackButton(
                onBack = onDismiss,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}
