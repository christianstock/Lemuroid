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
    val baseIndex = Int.MAX_VALUE / 2
    
    // 1. TOP-LEVEL SYSTEM PAGER
    val initialSystemIndex = baseIndex - (baseIndex % systemsCount)
    val currentSystemInternalIndex = systemLibraries.indexOfFirst { it.systemId.equals(selectedSystemId, ignoreCase = true) }.coerceAtLeast(0)
    val systemPagerState = rememberPagerState(initialPage = initialSystemIndex + currentSystemInternalIndex) { Int.MAX_VALUE }

    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    // Architecture Fix: Tracking the last ID emitted to prevent feedback loops
    var lastEmittedSystemId by remember { mutableStateOf<String?>(selectedSystemId) }

    // Sync Pager -> ViewModel (System Selection)
    LaunchedEffect(systemPagerState.currentPage, systemPagerState.isScrollInProgress) {
        if (!systemPagerState.isScrollInProgress) {
            val systemId = systemLibraries[systemPagerState.currentPage % systemsCount].systemId
            if (!systemId.equals(selectedSystemId, ignoreCase = true)) {
                onSystemSelected(systemId)
            }
        }
    }

    // Sync ViewModel -> Pager (External Selection)
    LaunchedEffect(selectedSystemId) {
        if (selectedSystemId != null && !selectedSystemId.equals(lastEmittedSystemId, ignoreCase = true)) {
            val targetIndex = systemLibraries.indexOfFirst { it.systemId.equals(selectedSystemId, ignoreCase = true) }
            if (targetIndex != -1) {
                val currentPage = systemPagerState.currentPage
                val targetPage = initialSystemIndex + targetIndex
                if (targetPage != currentPage) {
                    systemPagerState.scrollToPage(targetPage)
                }
            }
        }
    }

    HorizontalPager(
        state = systemPagerState,
        modifier = modifier.fillMaxSize(),
        userScrollEnabled = true,
        key = { page -> systemLibraries[page % systemsCount].systemId }
    ) { systemPage ->
        val library = systemLibraries[systemPage % systemsCount]
        
        SystemPage(
            library = library,
            refreshCount = refreshCount,
            initialGamePage = systemScrollPositions[library.systemId] ?: baseIndex,
            onScroll = { newIndex -> onSystemScroll(library.systemId, newIndex) },
            onGameClick = onGameClick,
            onShowContextMenu = onShowContextMenu,
            onNavigateToList = onNavigateToList
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SystemPage(
    library: HomeViewModel.SystemLibrary,
    refreshCount: Int,
    initialGamePage: Int,
    onScroll: (Int) -> Unit,
    onGameClick: (Game) -> Unit,
    onShowContextMenu: (Game) -> Unit,
    onNavigateToList: (Game) -> Unit
) {
    val games = library.games
    // Use systemId in key to ensure PagerState is preserved for this specific handheld unit
    val gamePagerState = rememberPagerState(initialPage = initialGamePage) { Int.MAX_VALUE }
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    // Sync local scroll back to ViewModel only when it changes via user interaction
    // We check if the pager is currently scrolling to avoid feedback loops
    LaunchedEffect(gamePagerState.currentPage, gamePagerState.isScrollInProgress) {
        if (!gamePagerState.isScrollInProgress && gamePagerState.currentPage != initialGamePage) {
            onScroll(gamePagerState.currentPage)
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        // --- LAYER 0: GAME INFO ---
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
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val year = currentGame.releaseDate?.take(4) ?: run {
                        val yearRegex = Regex("\\b(19|20)\\d{2}\\b")
                        currentGame.developer?.let { yearRegex.find(it)?.value }
                    }
                    
                    val developer = currentGame.developer?.let { dev ->
                        year?.let { y -> dev.replace(y, "").replace(Regex(",\\s*$"), "").trim() } ?: dev
                    }

                    if (developer != null || year != null) {
                        Text(
                            text = listOfNotNull(developer.takeIf { it?.isNotEmpty() == true }, year).joinToString(" | "),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }

        // --- LAYER 1: CARTRIDGE PAGER ---
        if (games.isNotEmpty()) {
            HorizontalPager(
                state = gamePagerState,
                contentPadding = PaddingValues(horizontal = 80.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 200.dp, top = 140.dp),
                key = { page -> "${library.systemId}_${games[page % games.size].id}" }
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

        // --- LAYER 2: CONSOLE ART (Foreground) ---
        Box(
            modifier = Modifier
                .fillMaxWidth(if (library.systemId.lowercase() == "gba" || library.systemId.lowercase() == "psp") 1.0f else 0.8f)
                .height(180.dp)
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
        val w = size.width
        val h = size.height
        val outerCorner = 16.dp.toPx()
        val path = Path().apply {
            addRoundRect(RoundRect(Rect(0f, 0f, w, h), topLeft = CornerRadius(outerCorner), topRight = CornerRadius(outerCorner), bottomLeft = CornerRadius.Zero, bottomRight = CornerRadius.Zero))
        }
        drawPath(path, caseColor)
        
        val bezelW = if (systemIdNorm == "psp") w * 0.95f else w * 0.9f
        val bezelH = h * 1.1f
        val bezelX = (w - bezelW) / 2
        val bezelY = 40.dp.toPx()
        val bezelRect = Rect(bezelX, bezelY, bezelX + bezelW, bezelY + bezelH)
        val bezelColor = Color(0xFF1A1A1A)

        when (systemIdNorm) {
            "gb" -> {
                val standardCorner = 12.dp.toPx()
                val extraRoundCorner = 56.dp.toPx()
                val bezelPath = Path().apply {
                    moveTo(bezelRect.left + standardCorner, bezelRect.top)
                    lineTo(bezelRect.right - standardCorner, bezelRect.top)
                    quadraticTo(bezelRect.right, bezelRect.top, bezelRect.right, bezelRect.top + standardCorner)
                    lineTo(bezelRect.right, bezelRect.bottom - extraRoundCorner)
                    quadraticTo(bezelRect.right, bezelRect.bottom, bezelRect.right - extraRoundCorner, bezelRect.bottom)
                    lineTo(bezelRect.left + standardCorner, bezelRect.bottom)
                    quadraticTo(bezelRect.left, bezelRect.bottom, bezelRect.left, bezelRect.bottom - standardCorner)
                    lineTo(bezelRect.left, bezelRect.top + standardCorner)
                    quadraticTo(bezelRect.left, bezelRect.top, bezelRect.left + standardCorner, bezelRect.top)
                    close()
                }
                drawPath(bezelPath, bezelColor)
            }
            "gbc" -> {
                val cornerPx = 20.dp.toPx()
                val bulgePx = 20.dp.toPx()
                val bezelPath = Path().apply {
                    moveTo(bezelRect.left + cornerPx, bezelRect.top)
                    lineTo(bezelRect.right - cornerPx, bezelRect.top)
                    quadraticTo(bezelRect.right, bezelRect.top, bezelRect.right, bezelRect.top + cornerPx)
                    lineTo(bezelRect.right, bezelRect.bottom - cornerPx)
                    quadraticTo(bezelRect.right, bezelRect.bottom, bezelRect.right - cornerPx, bezelRect.bottom)
                    quadraticTo(bezelRect.center.x, bezelRect.bottom + bulgePx, bezelRect.left + cornerPx, bezelRect.bottom)
                    quadraticTo(bezelRect.left, bezelRect.bottom, bezelRect.left, bezelRect.bottom - cornerPx)
                    lineTo(bezelRect.left, bezelRect.top + cornerPx)
                    quadraticTo(bezelRect.left, bezelRect.top, bezelRect.left + cornerPx, bezelRect.top)
                    close()
                }
                drawPath(bezelPath, bezelColor)
                drawRoundRect(color = Color.Black.copy(alpha = 0.8f), topLeft = Offset(w * 0.75f, 12.dp.toPx()), size = Size(40.dp.toPx(), 12.dp.toPx()), cornerRadius = CornerRadius(4.dp.toPx()))
            }
            "gba" -> {
                val cornerPx = 24.dp.toPx()
                val bulgePx = 16.dp.toPx()
                val bezelPath = Path().apply {
                    moveTo(bezelRect.left + cornerPx, bezelRect.top)
                    lineTo(bezelRect.right - cornerPx, bezelRect.top)
                    quadraticTo(bezelRect.right, bezelRect.top, bezelRect.right, bezelRect.top + cornerPx)
                    lineTo(bezelRect.right, bezelRect.bottom - cornerPx)
                    quadraticTo(bezelRect.right, bezelRect.bottom, bezelRect.right - cornerPx, bezelRect.bottom)
                    quadraticTo(bezelRect.center.x, bezelRect.bottom + bulgePx, bezelRect.left + cornerPx, bezelRect.bottom)
                    quadraticTo(bezelRect.left, bezelRect.bottom, bezelRect.left, bezelRect.bottom - cornerPx)
                    lineTo(bezelRect.left, bezelRect.top + cornerPx)
                    quadraticTo(bezelRect.left, bezelRect.top, bezelRect.left + cornerPx, bezelRect.top)
                    close()
                }
                drawPath(bezelPath, bezelColor)
                val shoulderW = w * 0.2f
                val shoulderH = 20.dp.toPx()
                drawRect(color = Color.Black.copy(alpha = 0.1f), topLeft = Offset(0f, 0f), size = Size(shoulderW, shoulderH))
                drawRect(color = Color.Black.copy(alpha = 0.1f), topLeft = Offset(w - shoulderW, 0f), size = Size(shoulderW, shoulderH))
            }
            "psp" -> {
                val cornerPx = 32.dp.toPx()
                val bezelPath = Path().apply { addRoundRect(RoundRect(bezelRect, CornerRadius(cornerPx))) }
                drawPath(bezelPath, bezelColor)
                val shoulderW = w * 0.25f
                val shoulderH = 12.dp.toPx()
                drawRect(color = Color.White.copy(alpha = 0.1f), topLeft = Offset(0f, 0f), size = Size(shoulderW, shoulderH))
                drawRect(color = Color.White.copy(alpha = 0.1f), topLeft = Offset(w - shoulderW, 0f), size = Size(shoulderW, shoulderH))
            }
            else -> {
                drawRect(color = bezelColor, topLeft = bezelRect.topLeft, size = bezelRect.size)
            }
        }
    }
}
