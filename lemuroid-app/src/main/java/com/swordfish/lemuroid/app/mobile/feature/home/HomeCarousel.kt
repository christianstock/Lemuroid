package com.swordfish.lemuroid.app.mobile.feature.home

import android.graphics.Bitmap
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.ColorUtils
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

// Font Family Definition (Ensure press_start_2p.ttf is inside res/font)
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

    // Sync state back when system page changes
    LaunchedEffect(systemPagerState.currentPage) {
        val currentSystemId = systemLibraries[systemPagerState.currentPage % systemsCount].systemId
        if (!currentSystemId.equals(selectedSystemId, ignoreCase = true)) {
            onSystemSelected(currentSystemId)
        }
    }

    // Sync pager when selected system changes externally
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
                onNavigateToList = onNavigateToList
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
    onNavigateToList: (Game) -> Unit
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

    // Default vibrant fallback color (e.g. bright cyan/accent) instead of semi-transparent white
    val fallbackAccentColor = MaterialTheme.colorScheme.primary

    var accentColor by remember { mutableStateOf(fallbackAccentColor) }

    // Key on currentGame id to guarantee update on every carousel page change
    LaunchedEffect(currentGame?.id, currentGame?.coverFrontUrl) {
        val coverUrl = currentGame?.coverFrontUrl
        if (!coverUrl.isNullOrEmpty()) {
            withContext(Dispatchers.IO) {
                try {
                    val loader = ImageLoader(context)
                    val request = ImageRequest.Builder(context)
                        .data(coverUrl)
                        .allowHardware(false) // Palette extraction needs CPU bitmap
                        .build()

                    val result = loader.execute(request)
                    if (result is SuccessResult) {
                        val bitmap = result.drawable.toBitmap()
                        val palette = Palette.from(bitmap).generate()

                        // Calculate contrast against pure dark background (0.45 black scrim over dark background)
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
                                contrast >= 2.5 // Contrast ratio threshold for dark background readability
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

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        // 1. Fullscreen Dynamic Crossfading Blurred Cover Art Background
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
                    // Dark scrim overlay for legibility and smooth contrast
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .drawWithContent {
                                drawRect(Color.Black.copy(alpha = 0.45f))
                                drawContent()
                            }
                    )
                }
            }
        }

        // Top Game Info Header
        if (currentGame != null) {
            val titleParts = currentGame.title.split(" - ", limit = 2).map { it.trim() }

            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 48.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Main Game Title in "Press Start 2P"
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
                        text = listOfNotNull(publisher.takeIf { it.isNotEmpty() }, year.takeIf { it.isNotEmpty() }).joinToString(" | "),
                        style = MaterialTheme.typography.bodyMedium,
                        color = animatedAccentColor, // Dynamic accent color applied
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

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            val pageOffset = ((gamePagerState.currentPage - page) + gamePagerState.currentPageOffsetFraction).absoluteValue
                            val scale = lerp(start = 0.8f, stop = 1.0f, fraction = 1f - pageOffset.coerceIn(0f, 1f))
                            scaleX = scale
                            scaleY = scale
                            alpha = if (locked) 1f else lerp(start = 0.5f, stop = 1f, fraction = 1f - pageOffset.coerceIn(0f, 1f))
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
                                        coroutineScope.launch { offsetY.snapTo((offsetY.value + delta).coerceIn(0f, 500f)) }
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
                                enabled = !locked,
                                onClick = { if (isFocused) onShowContextMenu(game) },
                                onLongClick = { if (isFocused) onNavigateToList(game) }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        GameCartridge(game = game, modifier = Modifier.fillMaxSize())
                    }

                    val isSwiping = gamePagerState.isScrollInProgress || gamePagerState.currentPageOffsetFraction.absoluteValue > 0.001f
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

                    PullDownTriangleIndicator(
                        isVisible = isVisible,
                        color = animatedAccentColor, // Dynamic accent color applied
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .offset(y = 28.dp)
                            .graphicsLayer { alpha = totalAlpha }
                    )
                }
            }
        }

        // Bottom Console Overlay
        Box(
            modifier = Modifier
                .fillMaxWidth(if (library.systemId.lowercase() in listOf("gba", "psp")) 1.0f else 0.8f)
                .height(160.dp)
                .align(Alignment.BottomCenter),
            contentAlignment = Alignment.BottomCenter
        ) {
            SystemForegroundView(systemId = library.systemId, modifier = Modifier.fillMaxSize())
        }
    }
}

@Composable
private fun SystemForegroundView(
    systemId: String?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val systemIdNorm = systemId?.lowercase() ?: ""

    val caseColor = when (systemIdNorm) {
        "gb" -> GbSkinManager.getInstance(context).getSelectedSkin().caseColor
        "gbc" -> GbcSkinManager.getInstance(context).getSelectedSkin().caseColor
        "gba" -> GbaSkinManager.getInstance(context).getSelectedSkin().caseColor
        "psp" -> Color(0xFF1A1A1A)
        else -> Color(0xFF444448)
    }

    Canvas(modifier = modifier) {
        val cornerPx = 8.dp.toPx()
        val topOffset = 100.dp.toPx()

        clipRect(
            left = 0f,
            top = topOffset,
            right = size.width,
            bottom = size.height
        ) {
            drawRoundRect(
                color = caseColor,
                topLeft = Offset(0f, topOffset),
                size = Size(size.width, size.height - topOffset + cornerPx),
                cornerRadius = CornerRadius(cornerPx, cornerPx)
            )
        }
    }
}
