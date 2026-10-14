package com.swordfish.lemuroid.app.mobile.feature.home.cartridge

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.swordfish.lemuroid.app.mobile.shared.HoldGestureOverlay
import com.swordfish.lemuroid.lib.library.db.entity.Game
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CartridgeItem(
    game: Game,
    page: Int,
    totalGames: Int,
    pagerState: PagerState,
    isFocused: Boolean,
    idleFloatDp: Float,
    refreshCount: Int,
    accentColor: Color,
    hintAlpha: Float,
    onGameClick: (Game) -> Unit,
    onShowContextMenu: (Game) -> Unit,
    onNavigateToList: (Game) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val offsetY = remember(refreshCount, page) { Animatable(0f) }
    var locked by remember(refreshCount, page) { mutableStateOf(false) }

    val activeFloatOffset = if (isFocused && offsetY.value == 0f && !locked) {
        with(density) { idleFloatDp.dp.toPx() }
    } else {
        0f
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .carouselGraphicsLayer(
                page = page,
                totalGames = totalGames,
                pagerState = pagerState,
                locked = locked,
                activeFloatOffset = activeFloatOffset
            )
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        DraggableCartridgeView(
            game = game,
            isFocused = isFocused,
            locked = locked,
            offsetY = offsetY,
            accentColor = accentColor,
            onLockChanged = { locked = it },
            onGameClick = onGameClick,
            onShowContextMenu = onShowContextMenu,
            onNavigateToList = onNavigateToList
        )

        // Read PagerState scrolling in derived lambda to prevent parent recompositions
        val isHintVisible = remember(pagerState.isScrollInProgress, pagerState.currentPageOffsetFraction, isFocused, locked) {
            val isSwiping = pagerState.isScrollInProgress || pagerState.currentPageOffsetFraction.absoluteValue > 0.001f
            !isSwiping && isFocused && !locked
        }

        GestureHints(
            isVisible = isHintVisible,
            dragOffsetY = offsetY.value,
            hintAlpha = hintAlpha,
            accentColor = accentColor,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = (-50).dp)
                .fillMaxWidth()
        )
    }
}

// ============================================================================
// 1. Interactive Draggable Body
// ============================================================================

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DraggableCartridgeView(
    game: Game,
    isFocused: Boolean,
    locked: Boolean,
    offsetY: Animatable<Float, *>,
    accentColor: Color,
    onLockChanged: (Boolean) -> Unit,
    onGameClick: (Game) -> Unit,
    onShowContextMenu: (Game) -> Unit,
    onNavigateToList: (Game) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    val interactionSource = remember { MutableInteractionSource() }
    var isPressed by remember { mutableStateOf(false) }
    var pressOffset by remember { mutableStateOf(Offset.Unspecified) }

    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is PressInteraction.Press -> {
                    pressOffset = interaction.pressPosition
                    isPressed = true
                }
                is PressInteraction.Release, is PressInteraction.Cancel -> {
                    isPressed = false
                }
            }
        }
    }

    val holdProgress by animateFloatAsState(
        targetValue = if (isPressed && isFocused) 1f else 0f,
        animationSpec = tween(durationMillis = 500, easing = LinearOutSlowInEasing),
        label = "ConcentricCircleHoldAnim"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .offset { IntOffset(0, offsetY.value.roundToInt()) }
            .cartridgeDragGesture(
                isFocused = isFocused,
                locked = locked,
                offsetY = offsetY,
                coroutineScope = coroutineScope,
                onPullDownThresholdReached = {
                    onLockChanged(true)
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onGameClick(game)
                }
            )
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = !locked,
                onClick = { if (isFocused) onShowContextMenu(game) },
                onLongClick = { if (isFocused) onNavigateToList(game) }
            ),
        contentAlignment = Alignment.Center
    ) {
        GameCartridge(game = game, modifier = Modifier.fillMaxSize())

        if (isPressed && isFocused) {
            HoldGestureOverlay(
                pressOffset = pressOffset,
                holdProgress = holdProgress,
                tintColor = accentColor,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun Modifier.cartridgeDragGesture(
    isFocused: Boolean,
    locked: Boolean,
    offsetY: Animatable<Float, *>,
    coroutineScope: CoroutineScope,
    onPullDownThresholdReached: () -> Unit
): Modifier = this.draggable(
    state = rememberDraggableState { delta ->
        if (isFocused && !locked) {
            coroutineScope.launch {
                offsetY.snapTo((offsetY.value + delta).coerceIn(0f, 750f))
            }
        }
    },
    orientation = Orientation.Vertical,
    onDragStopped = {
        if (isFocused && !locked) {
            if (offsetY.value > 240f) {
                onPullDownThresholdReached()
            } else {
                coroutineScope.launch { offsetY.animateTo(0f) }
            }
        }
    }
)

// ============================================================================
// 2. Custom Graphics Transformation Modifier (Deferred State Reads)
// ============================================================================

private fun Modifier.carouselGraphicsLayer(
    page: Int,
    totalGames: Int,
    pagerState: PagerState,
    locked: Boolean,
    activeFloatOffset: Float
): Modifier = this.graphicsLayer {
    if (totalGames > 1) {
        // State reads performed inside the graphicsLayer block defer execution to Draw phase
        val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).absoluteValue
        val scale = lerp(start = 0.8f, stop = 1.0f, fraction = 1f - pageOffset.coerceIn(0f, 1f))
        scaleX = scale
        scaleY = scale
        alpha = if (locked) 1f else lerp(start = 0.5f, stop = 1f, fraction = 1f - pageOffset.coerceIn(0f, 1f))
    } else {
        scaleX = 1.0f
        scaleY = 1.0f
        alpha = 1.0f
    }
    translationY = activeFloatOffset
}

// ============================================================================
// 3. Hints & Gestures Overlay
// ============================================================================

@Composable
private fun GestureHints(
    isVisible: Boolean,
    dragOffsetY: Float,
    hintAlpha: Float,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val swipeAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 300, easing = LinearOutSlowInEasing),
        label = "IndicatorSwipeAlpha"
    )
    val dragAlpha = (1f - (dragOffsetY / 120f)).coerceIn(0f, 1f)
    val totalAlpha = (swipeAlpha * dragAlpha).coerceIn(0f, 1f)

    Box(
        modifier = modifier.graphicsLayer { alpha = totalAlpha },
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .graphicsLayer { alpha = hintAlpha },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            InlineTapHint(label = "INFO")
            InlineHoldHint(label = "LIST", accentColor = accentColor)
        }

        PullDownTriangleIndicator(
            isVisible = isVisible,
            color = accentColor
        )
    }
}
