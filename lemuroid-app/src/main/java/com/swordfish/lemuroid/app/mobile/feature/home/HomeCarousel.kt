package com.swordfish.lemuroid.app.mobile.feature.home

import android.graphics.Bitmap
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
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
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.shared.game.skins.GbSkinManager
import com.swordfish.lemuroid.app.shared.game.skins.GbaSkinManager
import com.swordfish.lemuroid.app.shared.game.skins.GbcSkinManager
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
        val coverUrl = currentGame?.coverFrontUrl
        if (!coverUrl.isNullOrEmpty()) {
            withContext(Dispatchers.IO) {
                try {
                    val loader = ImageLoader(context)
                    val request = ImageRequest.Builder(context)
                        .data(coverUrl)
                        .allowHardware(false)
                        .build()

                    val result = loader.execute(request)
                    if (result is SuccessResult) {
                        val bitmap = result.drawable.toBitmap()
                        val palette = Palette.from(bitmap).generate()
                        val compositeDarkBg = 0xFF121212.toInt()

                        val candidateSwatches = listOfNotNull(
                            palette.vibrantSwatch,
                            palette.lightVibrantSwatch,
                            palette.dominantSwatch,
                            palette.lightMutedSwatch,
                            palette.mutedSwatch
                        ) + palette.swatches.sortedByDescending { it.population }

                        val chosenColor = candidateSwatches
                            .map { Color(it.rgb) }
                            .firstOrNull { candidate ->
                                val contrast = ColorUtils.calculateContrast(candidate.toArgb(), compositeDarkBg)
                                contrast >= 2.5
                            } ?: fallbackAccentColor

                        withContext(Dispatchers.Main) {
                            accentColor = chosenColor
                        }
                    } else {
                        withContext(Dispatchers.Main) { accentColor = fallbackAccentColor }
                    }
                } catch (_: Exception) {
                    withContext(Dispatchers.Main) { accentColor = fallbackAccentColor }
                }
            }
        } else {
            accentColor = fallbackAccentColor
        }
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
        // Fullscreen Blurred Background with Vignette Gradient Fade to Black
        Crossfade(
            targetState = currentGame?.coverFrontUrl,
            animationSpec = tween(durationMillis = 600),
            label = "BackgroundBlurCrossfade",
            modifier = Modifier.fillMaxSize()
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
                            .blur(radius = 1000.dp)
                            .graphicsLayer {
                                scaleX = 1.2f
                                scaleY = 1.2f
                            }
                    )
                    // Radial Vignette Gradient: Semi-transparent in center, deep black towards edges
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

                        // Concentric Rings Touch Gesture Overlay
                        if (isPressed && isFocused) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val centerPx = if (pressOffset != Offset.Unspecified) pressOffset else Offset(
                                    size.width / 2f,
                                    size.height / 2f
                                )

                                val innerRadius = 24.dp.toPx()
                                val outerRadius = 40.dp.toPx()

                                // Inner thin circle
                                drawCircle(
                                    color = Color.White.copy(alpha = 0.65f),
                                    radius = innerRadius,
                                    center = centerPx,
                                    style = Stroke(width = 1.25.dp.toPx())
                                )

                                // Outer thin guide circle
                                drawCircle(
                                    color = Color.White.copy(alpha = 0.25f),
                                    radius = outerRadius,
                                    center = centerPx,
                                    style = Stroke(width = 1.25.dp.toPx())
                                )

                                // Thicker animated hold progress arc (4.dp)
                                if (holdProgress > 0f) {
                                    drawArc(
                                        color = animatedAccentColor,
                                        startAngle = -90f,
                                        sweepAngle = 360f * holdProgress,
                                        useCenter = false,
                                        topLeft = Offset(centerPx.x - outerRadius, centerPx.y - outerRadius),
                                        size = Size(outerRadius * 2, outerRadius * 2),
                                        style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                                    )
                                }
                            }
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
                            MinimalInlineTapHint(label = "INFO")
                            MinimalInlineHoldHint(label = "LIST", accentColor = animatedAccentColor)
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

/**
 * Minimalist inline TAP / INFO hint with opacity-only pulsing (size remains fixed).
 */
@Composable
private fun MinimalInlineTapHint(
    label: String,
    modifier: Modifier = Modifier
) {
    val pulseTransition = rememberInfiniteTransition(label = "TapOpacityPulse")
    val pulseAlpha by pulseTransition.animateFloat(
        initialValue = 0.05f,
        targetValue = 0.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseAlpha"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.graphicsLayer { alpha = pulseAlpha }
    ) {
        Canvas(modifier = Modifier.size(18.dp)) {
            val centerPx = Offset(size.width / 2f, size.height / 2f)
            drawCircle(
                color = Color.White,
                radius = size.width / 2f,
                style = Stroke(width = 1.25.dp.toPx())
            )
            drawCircle(
                color = Color.White,
                radius = size.width / 3.5f,
                style = Stroke(width = 1.25.dp.toPx())
            )
        }
        Text(
            text = label,
            fontFamily = PressStart2PFontFamily,
            fontSize = 11.sp,
            color = Color.White
        )
    }
}

/**
 * Minimalist inline HOLD / LIST hint with opacity-only pulsing (size remains fixed).
 */
@Composable
private fun MinimalInlineHoldHint(
    label: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val durationMs = 5000 // Matches the overall hint visibility window

    val transition = rememberInfiniteTransition(label = "SingleHoldWindupTransition")

    // Synchronized opacity: Fades in, holds, then fades out smoothly over the single duration
    val pulseTransition = rememberInfiniteTransition(label = "TapOpacityPulse")
    val pulseAlpha by pulseTransition.animateFloat(
        initialValue = 0.05f,
        targetValue = 0.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseAlpha"
    )

    // Exactly ONE 360° rotation over the full time span
    val sweepAngle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = durationMs,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "Single360Windup"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.graphicsLayer { alpha = pulseAlpha }
    ) {
        Canvas(modifier = Modifier.size(18.dp)) {
            val centerPx = Offset(size.width / 2f, size.height / 2f)
            val innerRadius = size.width / 3.5f
            val outerRadius = size.width / 2f

            // Inner guide circle
            drawCircle(
                color = Color.White,
                radius = innerRadius,
                center = centerPx,
                style = Stroke(width = 1.25.dp.toPx())
            )

            // Outer arc executing one full 360° windup
            if (sweepAngle > 0f) {
                drawArc(
                    color = accentColor,
                    startAngle = -90f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(centerPx.x - outerRadius, centerPx.y - outerRadius),
                    size = Size(outerRadius * 2, outerRadius * 2),
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }
        Text(
            text = label,
            fontFamily = PressStart2PFontFamily,
            fontSize = 11.sp,
            color = Color.White
        )
    }
}

/**
 * Adaptive Scrubber Track: Renders small dots for items with active selection always shown as a diamond.
 */
@Composable
private fun ScrubberTrack(
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

@Composable
private fun SystemForegroundView(
    systemId: String?,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val systemIdNorm = systemId?.lowercase() ?: ""

    // Safely load active skin colors with fallbacks
    val caseColor = remember(systemIdNorm, context) {
        when (systemIdNorm) {
            "gb" -> runCatching { GbSkinManager.getInstance(context).getSelectedSkin().caseColor }.getOrNull()
            "gbc" -> runCatching { GbcSkinManager.getInstance(context).getSelectedSkin().caseColor }.getOrNull()
            "gba" -> runCatching { GbaSkinManager.getInstance(context).getSelectedSkin().caseColor }.getOrNull()
            else -> null
        } ?: when (systemIdNorm) {
            "psp" -> Color(0xFF1A1A1A)
            "gb" -> Color(0xFFC4C2B8) // Classic Game Boy DMG case color
            "gbc" -> Color(0xFF7B2CBF) // Atomic Purple / Purple GBC
            "gba" -> Color(0xFF5E50A1) // Indigo GBA
            else -> Color(0xFF444448)
        }
    }

    val bevelColor = remember(systemIdNorm, context) {
        when (systemIdNorm) {
            "gb" -> runCatching { GbSkinManager.getInstance(context).getSelectedSkin().screenLensColor }.getOrNull()
            else -> null
        } ?: Color(0xFF000000)
    }

    val isGbDmg = systemIdNorm == "gb"
    val isGba = systemIdNorm == "gba"

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val topOffset = 75.dp.toPx()
            val bevelOffset = topOffset + 75.dp.toPx()
            val caseHeight = 75.dp.toPx()
            val arcLift = 14.dp.toPx() // Height of upward arc for GBA

            clipRect(
                left = 0f,
                top = topOffset,
                right = size.width,
                bottom = size.height
            ) {
                // 1. Base Case Surface
                drawRect(
                    color = caseColor,
                    topLeft = Offset(0f, topOffset),
                    size = Size(size.width, caseHeight)
                )

                // Top Edge Shadow Overlays
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.8f), Color.Transparent),
                        startY = topOffset,
                        endY = topOffset + 6.dp.toPx()
                    ),
                    topLeft = Offset(0f, topOffset),
                    size = Size(size.width, 6.dp.toPx())
                )
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.3f), Color.Transparent),
                        startY = topOffset,
                        endY = topOffset + 16.dp.toPx()
                    ),
                    topLeft = Offset(0f, topOffset),
                    size = Size(size.width, 16.dp.toPx())
                )



                // 2. Bottom Case Lip Highlight
                if (isGba) {
                    // Curved White Highlight Ribbon directly above GBA Bevel
                    val curvedHighlightPath = Path().apply {
                        moveTo(0f, bevelOffset - 8.dp.toPx())
                        quadraticTo(
                            size.width / 2f, (bevelOffset - arcLift) - 8.dp.toPx(),
                            size.width, bevelOffset - 8.dp.toPx()
                        )
                        lineTo(size.width, bevelOffset)
                        quadraticTo(
                            size.width / 2f, bevelOffset - arcLift,
                            0f, bevelOffset
                        )
                        close()
                    }
                    drawPath(
                        path = curvedHighlightPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.3f)),
                            startY = bevelOffset - arcLift - 8.dp.toPx(),
                            endY = bevelOffset
                        )
                    )
                } else {
                    // Straight White Highlight
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.3f)),
                            startY = bevelOffset - 8.dp.toPx(),
                            endY = bevelOffset
                        ),
                        topLeft = Offset(0f, bevelOffset - 8.dp.toPx()),
                        size = Size(size.width, 8.dp.toPx())
                    )
                }

                // 3. Bevel Layer & Top Drop Shadow
                if (isGba) {
                    val bevelPath = Path().apply {
                        moveTo(0f, size.height)
                        lineTo(0f, bevelOffset)
                        quadraticTo(
                            size.width / 2f, bevelOffset - arcLift,
                            size.width, bevelOffset
                        )
                        lineTo(size.width, size.height)
                        close()
                    }
                    drawPath(path = bevelPath, color = bevelColor)

                    // Curved Drop Shadow onto the Bevel
                    val shadowPath = Path().apply {
                        moveTo(0f, bevelOffset)
                        quadraticTo(
                            size.width / 2f, bevelOffset - arcLift,
                            size.width, bevelOffset
                        )
                        lineTo(size.width, bevelOffset + 10.dp.toPx())
                        quadraticTo(
                            size.width / 2f, (bevelOffset - arcLift) + 10.dp.toPx(),
                            0f, bevelOffset + 10.dp.toPx()
                        )
                        close()
                    }
                    drawPath(
                        path = shadowPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Black.copy(alpha = 0.5f), Color.Transparent),
                            startY = bevelOffset - arcLift,
                            endY = bevelOffset + 10.dp.toPx()
                        )
                    )
                } else {
                    // Straight Bevel
                    drawRect(
                        color = bevelColor,
                        topLeft = Offset(0f, bevelOffset),
                        size = Size(size.width, size.height - bevelOffset)
                    )

                    // Straight Drop Shadow onto the Bevel
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Black.copy(alpha = 0.45f), Color.Transparent),
                            startY = bevelOffset,
                            endY = bevelOffset + 8.dp.toPx()
                        ),
                        topLeft = Offset(0f, bevelOffset),
                        size = Size(size.width, 8.dp.toPx())
                    )
                }
            }

            // DMG-01 Branding Accent Lines (For Game Boy DMG)
            if (isGbDmg) {
                val lineY = topOffset + 120.dp.toPx()
                val stroke = 7.dp.toPx()
                val textPadding = 200.dp.toPx()
                val margin = 0.dp.toPx()

                // Magenta Line
                drawLine(
                    color = Color(0xFF930551),
                    start = Offset(margin, lineY - 7.dp.toPx()),
                    end = Offset(size.width - textPadding, lineY - 7.dp.toPx()),
                    strokeWidth = stroke
                )
                // Blue Line
                drawLine(
                    color = Color(0xFF111B91),
                    start = Offset(margin, lineY + 7.dp.toPx()),
                    end = Offset(size.width - textPadding, lineY + 7.dp.toPx()),
                    strokeWidth = stroke
                )
            }
        }


        // DMG-01 Branding Text Overlay
        if (isGbDmg) {
            Text(
                text = "DOT MATRIX WITH STEREO SOUND",
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Normal,
                fontSize = 20.sp,
                color = Color.White.copy(alpha = 0.55f),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 180.dp, end = 0.dp)
                    .graphicsLayer {
                        translationX = 180.dp.toPx()
                    }
            )
        }

        // Interactive SYSTEM Text Label
        Text(
            text = "SYSTEM",
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Black,
            fontSize = 26.sp,
            letterSpacing = 1.sp,
            color = Color.Black.copy(alpha = 0.12f),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 101.dp, end = 28.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onOpenSettings
                )
        )
    }
}


