package com.swordfish.lemuroid.app.mobile.feature.home

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.LemuroidGameImage
import com.swordfish.lemuroid.app.shared.game.skins.GbSkinManager
import com.swordfish.lemuroid.app.shared.game.skins.GbaSkinManager
import com.swordfish.lemuroid.app.shared.game.skins.GbcSkinManager
import com.swordfish.lemuroid.app.shared.game.skins.art.GbArt
import com.swordfish.lemuroid.app.shared.game.skins.art.GbaArt
import com.swordfish.lemuroid.app.shared.game.skins.art.GbcArt
import com.swordfish.lemuroid.app.shared.game.skins.art.PspArt
import com.swordfish.lemuroid.lib.library.db.entity.Game
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

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
    val currentSystemInternalIndex = systemLibraries.indexOfFirst { it.systemId.equals(selectedSystemId, ignoreCase = true) }.coerceAtLeast(0)
    val systemPagerState = rememberPagerState(initialPage = initialSystemIndex + currentSystemInternalIndex) { Int.MAX_VALUE }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(systemPagerState.currentPage, systemPagerState.isScrollInProgress) {
        if (!systemPagerState.isScrollInProgress) {
            val systemId = systemLibraries[systemPagerState.currentPage % systemsCount].systemId
            if (!systemId.equals(selectedSystemId, ignoreCase = true)) {
                onSystemSelected(systemId)
            }
        }
    }

    LaunchedEffect(selectedSystemId) {
        if (selectedSystemId != null) {
            val targetIndex = systemLibraries.indexOfFirst { it.systemId.equals(selectedSystemId, ignoreCase = true) }
            if (targetIndex != -1) {
                val targetPage = initialSystemIndex + targetIndex
                if (systemPagerState.currentPage != targetPage) {
                    systemPagerState.scrollToPage(targetPage)
                }
            }
        }
    }

    HorizontalPager(
        state = systemPagerState,
        modifier = modifier.fillMaxSize(),
        userScrollEnabled = false,
        key = { page -> systemLibraries[page % systemsCount].systemId }
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
            onSystemSwipe = { delta ->
                systemPagerState.dispatchRawDelta(-delta)
            },
            onSystemSwipeEnd = { velocity ->
                coroutineScope.launch {
                    val pageOffset = systemPagerState.currentPageOffsetFraction
                    var targetPage = systemPagerState.currentPage
                    if (velocity.absoluteValue > 500f) {
                        if (velocity > 0) targetPage-- else targetPage++
                    } else if (pageOffset.absoluteValue > 0.5f) {
                        if (pageOffset > 0) targetPage++ else targetPage--
                    }
                    systemPagerState.animateScrollToPage(targetPage)
                }
            }
        )
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
    onSystemSwipe: (Float) -> Unit,
    onSystemSwipeEnd: (Float) -> Unit
) {
    val games = library.games
    val gamePagerState = rememberPagerState(initialPage = scrollPage) { Int.MAX_VALUE }
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

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

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        if (games.isNotEmpty()) {
            val index = gamePagerState.currentPage % games.size
            val currentGame = games[index]
            val titleParts = currentGame.title.split(" - ", limit = 2).map { it.trim() }
            
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 48.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = titleParts[0],
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
                titleParts.getOrNull(1)?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(start = 32.dp, end = 32.dp)
                            .graphicsLayer { scaleX = 0.85f; scaleY = 0.9f }
                    )
                }
                
                val year = currentGame.releaseDate?.take(4) ?: ""
                val publisher = currentGame.publisher ?: ""

                if (publisher.isNotEmpty() || year.isNotEmpty()) {
                    Text(
                        text = listOfNotNull(publisher.takeIf { it.isNotEmpty() }, year.takeIf { it.isNotEmpty() }).joinToString(" | "),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        if (games.isNotEmpty()) {
            HorizontalPager(
                state = gamePagerState,
                contentPadding = PaddingValues(horizontal = 80.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 200.dp, top = 140.dp),
                key = { page -> "${library.systemId}_pg_$page" }
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
                            val scale = lerp(start = 0.75f, stop = 1f, fraction = 1f - pageOffset.coerceIn(0f, 1f))
                            scaleX = scale; scaleY = scale
                            alpha = if (locked) 1f else lerp(start = 0.5f, stop = 1f, fraction = 1f - pageOffset.coerceIn(0f, 1f))
                        }
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .offset { IntOffset(0, offsetY.value.roundToInt()) }
                            .draggable(
                                state = rememberDraggableState { delta ->
                                    if (isFocused && !locked) {
                                        coroutineScope.launch { offsetY.snapTo((offsetY.value + delta).coerceIn(0f, 250f)) }
                                    }
                                },
                                orientation = Orientation.Vertical,
                                onDragStopped = {
                                    if (isFocused && !locked) {
                                        if (offsetY.value > 120f) {
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
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth(if (library.systemId.lowercase() == "gba" || library.systemId.lowercase() == "psp") 1.0f else 0.8f)
                .height(180.dp)
                .align(Alignment.BottomCenter)
                .draggable(
                    state = rememberDraggableState { delta -> onSystemSwipe(delta) },
                    orientation = Orientation.Horizontal,
                    onDragStopped = { velocity -> onSystemSwipeEnd(velocity) }
                ),
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
        val bezelW = if (systemIdNorm == "psp") size.width * 0.95f else size.width * 0.9f
        val bezelH = size.height * 1.1f
        val bezelX = (size.width - bezelW) / 2
        val bezelY = 40.dp.toPx()
        val bezelRect = Rect(bezelX, bezelY, bezelX + bezelW, bezelY + bezelH)

        when (systemIdNorm) {
            "gb" -> GbArt.run { drawHandheld( bezelRect,bezelRect, GbSkinManager.getInstance(context).getSelectedSkin(),true) }
            "gbc" -> GbcArt.run { drawHandheld(caseColor, bezelRect, true) }
            "gba" -> GbaArt.run { drawHandheld(caseColor, bezelRect, true) }
            "psp" -> PspArt.run { drawHandheld(caseColor, bezelRect, true) }
            else -> {
                drawRect(Color(0xFF1A1A1A), topLeft = bezelRect.topLeft, size = bezelRect.size)
            }
        }
    }
}
