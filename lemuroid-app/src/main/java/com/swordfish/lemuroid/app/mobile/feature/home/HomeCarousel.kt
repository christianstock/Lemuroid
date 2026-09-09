package com.swordfish.lemuroid.app.mobile.feature.home

import androidx.compose.animation.core.Animatable
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
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
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
    games: List<Game>,
    availableSystems: List<String>,
    selectedSystemId: String?,
    refreshCount: Int,
    onGameClick: (Game) -> Unit,
    onShowContextMenu: (Game) -> Unit,
    onNavigateToList: (Game) -> Unit,
    onSystemSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (availableSystems.isEmpty()) return

    val systemsCount = availableSystems.size
    
    // System Pager State
    val initialSystemIndex = (Int.MAX_VALUE / 2) - (Int.MAX_VALUE / 2 % systemsCount)
    val currentSystemInternalIndex = availableSystems.indexOfFirst { it.equals(selectedSystemId, ignoreCase = true) }.coerceAtLeast(0)
    val systemPagerState = rememberPagerState(initialPage = initialSystemIndex + currentSystemInternalIndex) { Int.MAX_VALUE }

    // Game Pager State
    val gamesCountInternal = games.size
    val initialGamePage = (Int.MAX_VALUE / 2) - (Int.MAX_VALUE / 2 % (if (gamesCountInternal > 0) gamesCountInternal else 1))
    val gamePagerState = rememberPagerState(initialPage = initialGamePage) { Int.MAX_VALUE }

    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    // Architecture Fix: Tracking the last ID emitted to prevent feedback loops
    var lastEmittedSystemId by remember { mutableStateOf<String?>(selectedSystemId) }

    // 1. Sync System Pager -> ViewModel (User Swiping)
    LaunchedEffect(systemPagerState, availableSystems, selectedSystemId, onSystemSelected) {
        snapshotFlow { 
            val page = systemPagerState.currentPage
            availableSystems.getOrNull(page % availableSystems.size)
        }
        .distinctUntilChanged()
        .collect { systemId ->
            if (systemId != null) {
                lastEmittedSystemId = systemId
                if (!systemId.equals(selectedSystemId, ignoreCase = true)) {
                    onSystemSelected(systemId)
                }
            }
        }
    }

    // 2. Sync selectedSystemId -> System Pager
    LaunchedEffect(selectedSystemId, availableSystems) {
        if (selectedSystemId != null && availableSystems.isNotEmpty() && 
            !selectedSystemId.equals(lastEmittedSystemId, ignoreCase = true)) {
            
            val targetIndex = availableSystems.indexOfFirst { it.equals(selectedSystemId, ignoreCase = true) }
            if (targetIndex != -1) {
                val currentPage = systemPagerState.currentPage
                val currentSystemIndex = currentPage % availableSystems.size
                if (targetIndex != currentSystemIndex) {
                    val delta = targetIndex - currentSystemIndex
                    systemPagerState.scrollToPage(currentPage + delta)
                }
            }
        }
    }

    // 3. Reset game pager when identity changes
    LaunchedEffect(selectedSystemId) {
        if (games.isNotEmpty()) {
            gamePagerState.scrollToPage(initialGamePage)
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        // --- LAYER 0: GLOBAL GAME INFO (TOP) ---
        if (games.isNotEmpty()) {
            val index = gamePagerState.currentPage % (if (gamesCountInternal > 0) gamesCountInternal else 1)
            val currentGame = games.getOrNull(index) ?: games[0]
            val titleParts = currentGame.title.split(" - ", limit = 2).map { it.trim() }
            val mainTitle = titleParts[0]
            val subTitle = titleParts.getOrNull(1)

            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 48.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = mainTitle,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
                
                subTitle?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(start = 32.dp, end = 32.dp, top = 0.dp)
                            .graphicsLayer {
                                scaleX = 0.85f
                                scaleY = 0.90f
                            }
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    currentGame.developer?.let { developer ->
                        val yearRegex = Regex("\\b(19|20)\\d{2}\\b")
                        val year = yearRegex.find(developer)?.value
                        val displayDev = if (year != null) developer.replace(year, "").replace(Regex(",\\s*$"), "").trim() else developer
                        
                        Text(
                            text = listOfNotNull(displayDev.takeIf { it.isNotEmpty() }, year).joinToString(" | "),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }

        // --- LAYER 1: CARTRIDGE CAROUSEL ---
        if (games.isNotEmpty()) {
            HorizontalPager(
                state = gamePagerState,
                contentPadding = PaddingValues(horizontal = 80.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 200.dp, top = 160.dp),
                userScrollEnabled = true
            ) { page ->
                key(refreshCount, selectedSystemId) {
                    val index = page % (if (gamesCountInternal > 0) gamesCountInternal else 1)
                    val game = games[index]
                    val isFocused = gamePagerState.currentPage == page
                    
                    val offsetY = remember { Animatable(0f) }
                    var locked by remember { mutableStateOf(false) }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer {
                                val pageOffset = (
                                        (gamePagerState.currentPage - page) + gamePagerState.currentPageOffsetFraction
                                        ).absoluteValue
                                
                                val scale = lerp(start = 0.75f, stop = 1f, fraction = 1f - pageOffset.coerceIn(0f, 1f))
                                scaleX = scale
                                scaleY = scale
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
                                            coroutineScope.launch {
                                                offsetY.snapTo((offsetY.value + delta).coerceIn(0f, 250f))
                                            }
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
                                                coroutineScope.launch {
                                                    offsetY.animateTo(0f)
                                                }
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
                            GameCartridge(
                                game = game,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize().padding(bottom = 200.dp), contentAlignment = Alignment.Center) {
                Text(text = "No games for ${selectedSystemId?.uppercase()}", style = MaterialTheme.typography.bodyLarge)
            }
        }

        // --- LAYER 2: SYSTEM FOREGROUND CAROUSEL ---
        HorizontalPager(
            state = systemPagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .align(Alignment.BottomCenter),
            userScrollEnabled = true
        ) { page ->
            val sysId = availableSystems[page % availableSystems.size]
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomCenter
            ) {
                SystemForegroundView(
                    systemId = sysId,
                    modifier = Modifier
                        .fillMaxWidth(if (sysId.lowercase() == "gba" || sysId.lowercase() == "psp") 1.0f else 0.8f) // PSP/GBA wide
                        .height(180.dp)
                )
            }
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
        "psp" -> Color(0xFF1A1A1A) // PSP Classic Black
        else -> Color(0xFF444448)
    }
    
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        
        // 1. Draw solid background case with rounded corners only at the TOP
        val outerCorner = 16.dp.toPx()
        val path = Path().apply {
            addRoundRect(
                RoundRect(
                    rect = Rect(0f, 0f, w, h),
                    topLeft = CornerRadius(outerCorner),
                    topRight = CornerRadius(outerCorner),
                    bottomLeft = CornerRadius.Zero,
                    bottomRight = CornerRadius.Zero
                )
            )
        }
        drawPath(path, caseColor)
        
        // 2. The Bezel/Lens (The "Front" part of the skin)
        val bezelW = if (systemIdNorm == "psp") w * 0.95f else w * 0.9f
        val bezelH = h * 1.1f // Stretch high to fill the peek view
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
                
                // IR glass
                drawRoundRect(
                    color = Color.Black.copy(alpha = 0.8f),
                    topLeft = Offset(w * 0.75f, 12.dp.toPx()),
                    size = Size(40.dp.toPx(), 12.dp.toPx()),
                    cornerRadius = CornerRadius(4.dp.toPx())
                )
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
                
                // Triggers
                val shoulderW = w * 0.2f
                val shoulderH = 20.dp.toPx()
                drawRect(color = Color.Black.copy(alpha = 0.1f), topLeft = Offset(0f, 0f), size = Size(shoulderW, shoulderH))
                drawRect(color = Color.Black.copy(alpha = 0.1f), topLeft = Offset(w - shoulderW, 0f), size = Size(shoulderW, shoulderH))
            }
            "psp" -> {
                val cornerPx = 32.dp.toPx()
                val bezelPath = Path().apply {
                    addRoundRect(RoundRect(bezelRect, CornerRadius(cornerPx)))
                }
                drawPath(bezelPath, bezelColor)
                
                // Wide shoulder triggers
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
