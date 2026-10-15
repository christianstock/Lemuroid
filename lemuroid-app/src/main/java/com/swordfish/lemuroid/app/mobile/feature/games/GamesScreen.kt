package com.swordfish.lemuroid.app.mobile.feature.games

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.mobile.shared.DefaultBackground
import com.swordfish.lemuroid.app.mobile.shared.RetroLcdGreen
import com.swordfish.lemuroid.app.mobile.shared.RetroLcdGreenDark
import com.swordfish.lemuroid.app.mobile.shared.RetroSearchBar
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.LemuroidGameImage
import com.swordfish.lemuroid.lib.library.db.entity.Game
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

val pressStart2PFontFamily = FontFamily(Font(R.font.press_start_2p))

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GamesScreen(
    modifier: Modifier = Modifier,
    viewModel: GamesViewModel,
    onGameClick: (Game) -> Unit,
    onGameLongClick: (Game) -> Unit,
    onExitClick: () -> Unit,
    @Suppress("UNUSED_PARAMETER") onGameFavoriteToggle: (Game, Boolean) -> Unit = { _, _ -> },
    rippleIntervalMs: Long = 6_000L,
) {
    val games by viewModel.games.collectAsStateWithLifecycle()
    val query by viewModel.searchQuery.collectAsStateWithLifecycle()
    val sortMode by viewModel.sortMode.collectAsStateWithLifecycle()
    val currentMetaSystem by viewModel.currentMetaSystem.collectAsStateWithLifecycle()
    val showAllSystems by viewModel.showAllSystems.collectAsStateWithLifecycle()

    val activeSystemCount = remember(games) {
        games.map { it.systemId }.distinct().size
    }

    val canSwipeSystems = remember(showAllSystems, activeSystemCount) {
        !showAllSystems && activeSystemCount > 1
    }

    val ripple = remember { GridRipple() }
    var gridSize by remember { mutableStateOf(IntSize.Zero) }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val coverShape = remember { RoundedCornerShape(4.dp) }
    val coroutineScope = rememberCoroutineScope()
    val gridState = rememberLazyGridState()

    val infiniteTransition = rememberInfiniteTransition(label = "SideArrowPulse")
    val sideArrowScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "SideArrowScale"
    )

    val sideArrowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "SideArrowAlpha"
    )

    LaunchedEffect(gridSize, games.size) {
        if (gridSize == IntSize.Zero || games.isEmpty()) return@LaunchedEffect
        lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            delay(600)
            while (isActive) {
                ripple.run(gridSize)
                delay((rippleIntervalMs * (0.8f + Random.nextFloat() * 0.4f)).toLong())
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            //.statusBarsPadding()
    ) {
        // LCD Green Top-Weighted Glow Background
        DefaultBackground(
            glowColor = RetroLcdGreen,
            isCentered = false
        )

        Column(modifier = Modifier.fillMaxSize()) {
            RetroSearchBar(
                query = query,
                onQueryChange = viewModel::updateSearchQuery,
                onExitClick = onExitClick
            )

            SortFilterRow(
                currentSortMode = sortMode,
                onSelectSortMode = { mode ->
                    viewModel.updateSortMode(mode)
                    coroutineScope.launch { gridState.scrollToItem(0) }
                }
            )

            // Main Grid / Content Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .then(
                        if (canSwipeSystems) {
                            Modifier.pointerInput(currentMetaSystem, showAllSystems) {
                                detectHorizontalDragGestures { change, dragAmount ->
                                    change.consume()
                                    if (dragAmount > 50) {
                                        viewModel.switchToPreviousSystem()
                                    } else if (dragAmount < -50) {
                                        viewModel.switchToNextSystem()
                                    }
                                }
                            }
                        } else Modifier
                    )
            ) {
                if (games.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "NO GAMES FOUND",
                            fontFamily = pressStart2PFontFamily,
                            fontSize = 12.sp,
                            color = RetroLcdGreenDark,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(24.dp)
                        )
                    }
                } else {
                    LazyVerticalGrid(
                        state = gridState,
                        modifier = Modifier
                            .fillMaxSize()
                            .onSizeChanged { gridSize = it }
                            .onGloballyPositioned { ripple.origin = it.positionInRoot() },
                        columns = GridCells.Fixed(3),
                        contentPadding = PaddingValues(3.dp),
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        items(games, key = { it.id }) { game ->
                            GameGridItem(
                                game = game,
                                ripple = ripple,
                                shape = coverShape,
                                onClick = {
                                    viewModel.onGameLaunched(game)
                                    onGameClick(game)
                                },
                                onLongClick = { onGameLongClick(game) }
                            )
                        }
                    }
                }

                if (ripple.isRunning && canSwipeSystems) {
                    RippleSideArrows(
                        scale = sideArrowScale,
                        alpha = sideArrowAlpha
                    )
                }
            }
        }
    }
}

@Composable
private fun SortFilterRow(
    currentSortMode: GamesViewModel.SortMode,
    onSelectSortMode: (GamesViewModel.SortMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val sortOptions = listOf(
            GamesViewModel.SortMode.ALPHABETICAL to "A-Z",
            GamesViewModel.SortMode.RECENTS to "LAST PLAYED",
            GamesViewModel.SortMode.RELEASE to "RELEASE"
        )

        sortOptions.forEach { (mode, label) ->
            val isSelected = currentSortMode == mode
            TextButton(onClick = { onSelectSortMode(mode) }) {
                Text(
                    text = label,
                    fontFamily = pressStart2PFontFamily,
                    fontSize = 9.sp,
                    color = if (isSelected) RetroLcdGreen else RetroLcdGreenDark
                )
            }
        }
    }
}

@Composable
private fun RippleSideArrows(
    scale: Float,
    alpha: Float,
    modifier: Modifier = Modifier
) {
    val leftArrowPath = remember { Path() }
    val rightArrowPath = remember { Path() }

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 12.dp)
                .width(14.dp)
                .height(24.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    this.alpha = alpha
                }
        ) {
            leftArrowPath.reset()
            leftArrowPath.moveTo(0f, size.height / 2f)
            leftArrowPath.lineTo(size.width, 0f)
            leftArrowPath.lineTo(size.width, size.height)
            leftArrowPath.close()
            drawPath(leftArrowPath, color = Color.White)
        }

        Canvas(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 12.dp)
                .width(14.dp)
                .height(24.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    this.alpha = alpha
                }
        ) {
            rightArrowPath.reset()
            rightArrowPath.moveTo(size.width, size.height / 2f)
            rightArrowPath.lineTo(0f, 0f)
            rightArrowPath.lineTo(0f, size.height)
            rightArrowPath.close()
            drawPath(rightArrowPath, color = Color.White)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GameGridItem(
    game: Game,
    ripple: GridRipple,
    shape: Shape,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .rippleLift(ripple, shape)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
    ) {
        LemuroidGameImage(
            game = game,
            modifier = Modifier.fillMaxSize(),
            applyAspectRatio = false,
            contentScale = ContentScale.Crop
        )
    }
}
