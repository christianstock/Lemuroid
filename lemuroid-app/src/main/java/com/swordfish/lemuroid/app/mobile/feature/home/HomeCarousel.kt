package com.swordfish.lemuroid.app.mobile.feature.home

import android.content.Context
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.drawable.toBitmap
import androidx.palette.graphics.Palette
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.mobile.shared.GameArtBlurBackground
import com.swordfish.lemuroid.app.mobile.shared.HoldGestureOverlay
import com.swordfish.lemuroid.app.mobile.shared.extractAccentColor
import com.swordfish.lemuroid.lib.library.db.entity.Game
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

val PressStart2PFontFamily = FontFamily(
    Font(R.font.press_start_2p, FontWeight.Normal)
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeCarousel(
    systemLibraries: List<HomeViewModel.SystemLibrary>,
    selectedSystemId: String?,
    systemScrollPositions: Map<String, Int>,
    refreshCount: Int,
    onGameClick: (Game) -> Unit,
    onShowContextMenu: (Game) -> Unit,
    onNavigateToList: (Game) -> Unit,
    onSystemSelected: (String) -> Unit,
    onSystemScroll: (String, Int) -> Unit,
    onOpenSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (systemLibraries.isEmpty()) return

    val systemsCount = systemLibraries.size
    val baseIndex = HomeViewModel.BASE_PAGE_INDEX

    val initialSystemIndex = baseIndex - (baseIndex % systemsCount)
    val currentSystemInternalIndex = systemLibraries
        .indexOfFirst { it.systemId.equals(selectedSystemId, ignoreCase = true) }
        .coerceAtLeast(0)

    val systemPagerState = rememberPagerState(
        initialPage = initialSystemIndex + currentSystemInternalIndex
    ) { Int.MAX_VALUE }

    LaunchedEffect(systemPagerState.currentPage) {
        val currentSystemId = systemLibraries[systemPagerState.currentPage % systemsCount].systemId
        if (!currentSystemId.equals(selectedSystemId, ignoreCase = true)) {
            onSystemSelected(currentSystemId)
        }
    }

    LaunchedEffect(selectedSystemId) {
        if (selectedSystemId != null) {
            val targetIndex = systemLibraries.indexOfFirst { it.systemId.equals(selectedSystemId, ignoreCase = true) }
            if (targetIndex != -1) {
                val currentModulo = systemPagerState.currentPage % systemsCount
                val difference = targetIndex - currentModulo
                val targetPage = systemPagerState.currentPage + difference
                if (systemPagerState.currentPage != targetPage) {
                    systemPagerState.animateScrollToPage(targetPage)
                }
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        HorizontalPager(
            state = systemPagerState,
            modifier = Modifier.fillMaxSize(),
            userScrollEnabled = true
        ) { systemPage ->
            val library = systemLibraries[systemPage % systemsCount]
            val scrollPage = systemScrollPositions[library.systemId] ?: baseIndex

            SystemPage(
                library = library,
                refreshCount = refreshCount,
                scrollPage = scrollPage,
                onScroll = { newIndex -> onSystemScroll(library.systemId, newIndex) },
                onGameClick = onGameClick,
                onShowContextMenu = onShowContextMenu,
                onNavigateToList = onNavigateToList,
                onOpenSettings = onOpenSettings
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SystemPage(
    library: HomeViewModel.SystemLibrary,
    refreshCount: Int,
    scrollPage: Int,
    onScroll: (Int) -> Unit,
    onGameClick: (Game) -> Unit,
    onShowContextMenu: (Game) -> Unit,
    onNavigateToList: (Game) -> Unit,
    onOpenSettings: () -> Unit = {}
) {
    val games = library.games
    val gamePagerState = rememberPagerState(initialPage = scrollPage) { Int.MAX_VALUE }
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(scrollPage) {
        if (gamePagerState.currentPage != scrollPage) {
            gamePagerState.scrollToPage(scrollPage)
        }
    }

    LaunchedEffect(gamePagerState.currentPage, gamePagerState.isScrollInProgress) {
        if (!gamePagerState.isScrollInProgress) {
            onScroll(gamePagerState.currentPage)
        }
    }

    val currentGame = if (games.isNotEmpty()) {
        games[gamePagerState.currentPage % games.size]
    } else null

    val fallbackAccentColor = MaterialTheme.colorScheme.primary
    var accentColor by remember { mutableStateOf(fallbackAccentColor) }

    LaunchedEffect(currentGame?.id, currentGame?.coverFrontUrl) {
        accentColor = extractAccentColor(
            context = context,
            coverUrl = currentGame?.coverFrontUrl,
            fallbackColor = fallbackAccentColor
        )
    }

    val animatedAccentColor by animateColorAsState(
        targetValue = accentColor,
        animationSpec = tween(durationMillis = 500),
        label = "AccentColorCrossfade"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "CartridgeFloatTransition")
    val idleFloatDp by infiniteTransition.animateFloat(
        initialValue = -1.5f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dyFloat"
    )

    val hintAlphaAnim = remember { Animatable(0f) }
    LaunchedEffect(library.systemId) {
        hintAlphaAnim.snapTo(0f)
        hintAlphaAnim.animateTo(0.85f, animationSpec = tween(600))
        delay(3500)
        hintAlphaAnim.animateTo(0f, animationSpec = tween(800))
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Fullscreen Blurred Background using GameArtBlurBackground
        GameArtBlurBackground(
            coverFrontUrl = currentGame?.coverFrontUrl,
            modifier = Modifier.fillMaxSize()
        )

        // Top Game Info Header
        if (currentGame != null) {
            val titleParts = currentGame.title.split(" - ", limit = 2).map { it.trim() }

            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 12.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ScrubberTrack(
                    pagerState = gamePagerState,
                    itemCount = games.size,
                    accentColor = animatedAccentColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 48.dp, vertical = 8.dp)
                )

                Text(
                    text = titleParts[0],
                    fontFamily = PressStart2PFontFamily,
                    fontSize = 20.sp,
                    lineHeight = 26.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
                titleParts.getOrNull(1)?.let {
                    Text(
                        text = it,
                        fontFamily = PressStart2PFontFamily,
                        fontSize = 16.sp,
                        lineHeight = 20.sp,
                        color = Color.White.copy(alpha = 0.85f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(start = 32.dp, top = 8.dp, end = 32.dp)
                    )
                }

                val year = currentGame.releaseDate?.take(4) ?: ""
                val publisher = currentGame.publisher ?: ""

                if (publisher.isNotEmpty() || year.isNotEmpty()) {
                    Text(
                        text = listOfNotNull(
                            publisher.takeIf { it.isNotEmpty() },
                            year.takeIf { it.isNotEmpty() }).joinToString(" | "),
                        style = MaterialTheme.typography.bodyMedium,
                        color = animatedAccentColor,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }

        // Middle Games Carousel
        if (games.isNotEmpty()) {
            val cartridgeWidth = 300.dp
            val screenWidth = LocalConfiguration.current.screenWidthDp.dp
            val dynamicPadding = ((screenWidth - cartridgeWidth) / 2).coerceAtLeast(0.dp)

            HorizontalPager(
                state = gamePagerState,
                pageSize = PageSize.Fixed(cartridgeWidth),
                pageSpacing = (-60).dp,
                contentPadding = PaddingValues(horizontal = dynamicPadding),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 160.dp, top = 140.dp)
            ) { page ->
                val index = page % games.size
                val game = games[index]
                val isFocused = gamePagerState.currentPage == page
                val offsetY = remember(refreshCount, page) { Animatable(0f) }
                var locked by remember(refreshCount, page) { mutableStateOf(false) }

                val density = LocalDensity.current
                val activeFloatOffset = if (isFocused && offsetY.value == 0f && !locked) {
                    with(density) { idleFloatDp.dp.toPx() }
                } else {
                    0f
                }

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
                        .fillMaxWidth()
                        .graphicsLayer {
                            val pageOffset =
                                ((gamePagerState.currentPage - page) + gamePagerState.currentPageOffsetFraction).absoluteValue
                            val scale = lerp(start = 0.8f, stop = 1.0f, fraction = 1f - pageOffset.coerceIn(0f, 1f))
                            scaleX = scale
                            scaleY = scale
                            alpha = if (locked) 1f else lerp(
                                start = 0.5f,
                                stop = 1f,
                                fraction = 1f - pageOffset.coerceIn(0f, 1f)
                            )

                            translationY = activeFloatOffset
                        }
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .offset { IntOffset(0, offsetY.value.roundToInt()) }
                            .draggable(
                                state = rememberDraggableState { delta ->
                                    if (isFocused && !locked) {
                                        coroutineScope.launch {
                                            offsetY.snapTo(
                                                (offsetY.value + delta).coerceIn(
                                                    0f,
                                                    750f
                                                )
                                            )
                                        }
                                    }
                                },
                                orientation = Orientation.Vertical,
                                onDragStopped = {
                                    if (isFocused && !locked) {
                                        if (offsetY.value > 240f) {
                                            locked = true
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            onGameClick(game)
                                        } else {
                                            coroutineScope.launch { offsetY.animateTo(0f) }
                                        }
                                    }
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

                        // Concentric Rings Touch Gesture Overlay using HoldGestureOverlay
                        if (isPressed && isFocused) {
                            HoldGestureOverlay(
                                pressOffset = pressOffset,
                                holdProgress = holdProgress,
                                tintColor = animatedAccentColor,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    val isSwiping =
                        gamePagerState.isScrollInProgress || gamePagerState.currentPageOffsetFraction.absoluteValue > 0.001f
                    val isVisible = !isSwiping && isFocused && !locked
                    val swipeAlpha by animateFloatAsState(
                        targetValue = if (isVisible) 1f else 0f,
                        animationSpec = tween(
                            durationMillis = 300,
                            easing = LinearOutSlowInEasing
                        ),
                        label = "IndicatorSwipeAlpha"
                    )
                    val dragAlpha = (1f - (offsetY.value / 120f)).coerceIn(0f, 1f)
                    val totalAlpha = (swipeAlpha * dragAlpha).coerceIn(0f, 1f)

                    // Bottom Area: Inline Opacity-Only Pulsating Gesture Hints + Down Arrow
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .offset(y = -50.dp)
                            .fillMaxWidth()
                            .graphicsLayer { alpha = totalAlpha },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .graphicsLayer { alpha = hintAlphaAnim.value },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            InlineTapHint(label = "INFO")
                            InlineHoldHint(label = "LIST", accentColor = animatedAccentColor)
                        }

                        // Center Animated Down Arrow
                        PullDownTriangleIndicator(
                            isVisible = isVisible,
                            color = animatedAccentColor
                        )
                    }
                }
            }
        }

        // Bottom Console Overlay
        Box(
            modifier = Modifier
                .fillMaxWidth(if (library.systemId.lowercase() in listOf("gba", "psp")) 1.0f else 1.0f)
                .height(250.dp)
                .align(Alignment.BottomCenter),
            contentAlignment = Alignment.BottomCenter
        ) {
            SystemForegroundView(
                systemId = library.systemId,
                onOpenSettings = onOpenSettings,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

