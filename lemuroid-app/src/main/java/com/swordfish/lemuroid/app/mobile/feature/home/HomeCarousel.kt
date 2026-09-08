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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.LemuroidGameImage
import com.swordfish.lemuroid.app.shared.game.skins.GbSkinManager
import com.swordfish.lemuroid.app.shared.game.skins.GbaSkinManager
import com.swordfish.lemuroid.app.shared.game.skins.GbcSkinManager
import com.swordfish.lemuroid.lib.library.db.entity.Game
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeCarousel(
    games: List<Game>,
    selectedSystemId: String?,
    refreshCount: Int,
    onGameClick: (Game) -> Unit,
    onShowContextMenu: (Game) -> Unit,
    onNavigateToList: (Game) -> Unit,
    modifier: Modifier = Modifier
) {
    if (games.isEmpty()) return

    val gamesCount = games.size
    val initialPage = (Int.MAX_VALUE / 2) - (Int.MAX_VALUE / 2 % gamesCount)
    val pagerState = rememberPagerState(initialPage = initialPage) { Int.MAX_VALUE }
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(games) {
        pagerState.scrollToPage(initialPage)
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        // --- LAYER 0: GLOBAL GAME INFO (TOP) ---
        val currentGame = games[pagerState.currentPage % gamesCount]
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 48.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = currentGame.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
            currentGame.developer?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        // --- LAYER 1: CARTRIDGE CAROUSEL (Behind the System Slot) ---
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 80.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 120.dp, top = 140.dp),
            userScrollEnabled = true
        ) { page ->
            // Use key(refreshCount) to force a total reset of cartridge state (e.g. popping them out)
            // when we return to home screen or change systems.
            key(refreshCount) {
                val index = page % gamesCount
                val game = games[index]
                val isFocused = pagerState.currentPage == page
                
                val offsetY = remember { Animatable(0f) }
                var locked by remember { mutableStateOf(false) }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            val pageOffset = (
                                    (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
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
                                        if (offsetY.value > 120f) { // Trigger at ~halfway insertion
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
                            systemId = game.systemId,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            LemuroidGameImage(
                                game = game,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }

        // --- LAYER 2: SYSTEM TOP (Foreground Plate) ---
        SystemSlotView(
            systemId = selectedSystemId,
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp) // Taller console art area
                .align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun SystemSlotView(
    systemId: String?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    // Determine console colors based on system theme/skin
    val systemColor = when (systemId) {
        "gb" -> GbSkinManager.getInstance(context).getSelectedSkin().caseColor
        "gbc" -> GbcSkinManager.getInstance(context).getSelectedSkin().caseColor
        "gba" -> GbaSkinManager.getInstance(context).getSelectedSkin().caseColor
        else -> Color(0xFF444448)
    }
    
    val slotColor = Color.Black.copy(alpha = 0.5f)
    
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        
        // Shift art down
        val topOffset = 60.dp.toPx()
        
        // 1. Solid background box for bottom
        drawRect(
            color = systemColor,
            topLeft = Offset(0f, topOffset),
            size = Size(w, h - topOffset)
        )
        
        // 2. Silhouette/Shape of the console top
        val path = Path().apply {
            moveTo(0f, topOffset)
            
            if (systemId == "gba") {
                // Wide GBA silhouette with shoulder trigger bumps
                quadraticTo(w * 0.1f, topOffset - 40.dp.toPx(), w * 0.25f, topOffset - 20.dp.toPx())
                lineTo(w * 0.75f, topOffset - 20.dp.toPx())
                quadraticTo(w * 0.9f, topOffset - 40.dp.toPx(), w, topOffset)
            } else if (systemId == "gb") {
                // Classic DMG with power switch notch on left
                lineTo(w * 0.05f, topOffset)
                lineTo(w * 0.05f, topOffset - 16.dp.toPx())
                lineTo(w * 0.15f, topOffset - 16.dp.toPx())
                lineTo(w * 0.15f, topOffset)
                quadraticTo(w / 2, topOffset - 40.dp.toPx(), w, topOffset)
            } else {
                // GBC/Generic rounded top
                quadraticTo(w / 2, topOffset - 50.dp.toPx(), w, topOffset)
            }
            
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(path, systemColor)
        
        // 3. The Slot opening
        val slotW = 180.dp.toPx()
        val slotH = 44.dp.toPx()
        val slotX = (w - slotW) / 2
        val slotY = topOffset - 10.dp.toPx()
        
        drawRoundRect(
            color = slotColor,
            topLeft = Offset(slotX, slotY),
            size = Size(slotW, slotH),
            cornerRadius = CornerRadius(4.dp.toPx())
        )
        
        // IR glass piece for GBC
        if (systemId == "gbc") {
            drawRoundRect(
                color = Color.Black.copy(alpha = 0.8f),
                topLeft = Offset(w * 0.75f, topOffset - 30.dp.toPx()),
                size = Size(40.dp.toPx(), 10.dp.toPx()),
                cornerRadius = CornerRadius(4.dp.toPx())
            )
        }
    }
}
