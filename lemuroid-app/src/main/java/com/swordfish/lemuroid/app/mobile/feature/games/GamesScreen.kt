package com.swordfish.lemuroid.app.mobile.feature.games

import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.LemuroidGameImage
import com.swordfish.lemuroid.lib.library.db.entity.Game
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.exp
import kotlin.math.hypot
import kotlin.random.Random

// retro colors
private val RetroLcdGreen = Color(0xFF9EA83B)
private val RetroDarkGreen = Color(0xFF1B3B1B)
private val RetroRedExit = Color(0xFFD32F2F)

val pressStart2PFontFamily = FontFamily(
    Font(R.font.press_start_2p, FontWeight.Normal)
)

@Stable
class GridRipple(
    private val speed: Float = 3f,
    private val bandWidth: Float = 1f,
    private val tailFactor: Float = 2f,
    private val strength: Float = 0.7f,
) {
    var origin by mutableStateOf(Offset.Zero)
    var coverWidthPx by mutableFloatStateOf(1f)

    private val front = Animatable(INACTIVE)
    private var maxDist = 0f

    val isRunning: Boolean
        get() = front.value != INACTIVE

    suspend fun run(gridSize: IntSize) {
        if (coverWidthPx <= 0f) return
        maxDist = hypot(gridSize.width.toFloat(), gridSize.height.toFloat()) / coverWidthPx
        val travel = maxDist + bandWidth * 3f
        try {
            front.snapTo(0f)
            front.animateTo(travel, tween((travel / speed * 1000).toInt(), easing = LinearEasing))
        } finally {
            withContext(NonCancellable) { front.snapTo(INACTIVE) }
        }
    }

    fun intensity(d: Float): Float {
        val f = front.value
        if (f == INACTIVE) return 0f
        val dl = d - f
        val s = if (dl < 0f) bandWidth * tailFactor else bandWidth
        val fade = (1f - f / (maxDist + bandWidth * tailFactor)).coerceIn(0f, 1f)
        val v = exp(-(dl / s) * (dl / s)) * (0.4f + 0.6f * fade)
        return v * strength
    }

    private companion object { const val INACTIVE = -1000f }
}

@Composable
fun Modifier.rippleLift(ripple: GridRipple, shape: Shape): Modifier {
    var center by remember { mutableStateOf(Offset.Zero) }

    fun k(): Float {
        if (ripple.coverWidthPx <= 0f) return 0f
        val d = (center - ripple.origin).getDistance() / ripple.coverWidthPx
        return ripple.intensity(d)
    }

    val kVal = k()

    return this
        .onGloballyPositioned { c ->
            center = c.positionInRoot() + Offset(c.size.width / 2f, c.size.height / 2f)
            if (c.size.width > 0) {
                ripple.coverWidthPx = c.size.width.toFloat()
            }
        }
        .zIndex(if (kVal > 0.01f) 1f else 0f)
        .graphicsLayer {
            val s = 1f + 0.02f * kVal
            scaleX = s
            scaleY = s
        }
        .clip(shape)
        .drawWithContent {
            drawContent()
            val a = kVal * 0.6f
            if (a > 0.005f) {
                drawRect(Color.White.copy(alpha = a))
            }
        }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GamesScreen(
    modifier: Modifier = Modifier,
    viewModel: GamesViewModel,
    onGameClick: (Game) -> Unit,
    onGameLongClick: (Game) -> Unit,
    onExitClick: () -> Unit,
    @Suppress("UNUSED_PARAMETER") onGameFavoriteToggle: (Game, Boolean) -> Unit,
    rippleIntervalMs: Long = 6_000L,
) {
    val games by viewModel.games.collectAsState()
    val query by viewModel.searchQuery.collectAsState()
    val sortMode by viewModel.sortMode.collectAsState()

    val ripple = remember { GridRipple() }
    var gridSize by remember { mutableStateOf(IntSize.Zero) }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val coverShape = remember { RoundedCornerShape(4.dp) }
    val coroutineScope = rememberCoroutineScope()
    val gridState = rememberLazyGridState()

    // Transition animation state when tapping a cover
    var animatingGame by remember { mutableStateOf<Game?>(null) }
    val animProgress = remember { Animatable(0f) }

    // Periodically trigger the ripple
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

    // Single game pulse arrow animation
    val infiniteTransition = rememberInfiniteTransition(label = "ArrowPulse")
    val arrowOffset by infiniteTransition.animateFloat(
        initialValue = -12f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ArrowOffset"
    )

    // Side arrows pulsing animations (scale & opacity)
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

    val launchGameWithAnimation: (Game) -> Unit = { game ->
        coroutineScope.launch {
            animatingGame = game
            animProgress.snapTo(0f)
            animProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1200, easing = LinearOutSlowInEasing)
            )
            onGameClick(game)
            animatingGame = null
        }
    }

    Box(modifier = modifier.fillMaxSize().background(Color.Black)) {
        Column(modifier = Modifier.fillMaxSize()) {

            // --- TOP HEADER: RETRO LCD SEARCH & RED EXIT BUTTON ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // LCD Green Search Input
                Surface(
                    color = RetroLcdGreen,
                    shape = RoundedCornerShape(32.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp) // Strictly 42dp height
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp), // Controlled side padding, NO fillMaxSize()
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = RetroDarkGreen,
                            modifier = Modifier.size(20.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // BasicTextField takes no extra vertical space and won't clip text
                        BasicTextField(
                            value = query,
                            onValueChange = { viewModel.updateSearchQuery(it) },
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontFamily = pressStart2PFontFamily,
                                fontSize = 12.sp,
                                color = RetroDarkGreen
                            ),
                            modifier = Modifier.weight(1f),
                            decorationBox = { innerTextField ->
                                Box(
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    if (query.isEmpty()) {
                                        Text(
                                            text = "SEARCH...",
                                            fontFamily = pressStart2PFontFamily,
                                            fontSize = 12.sp,
                                            color = RetroDarkGreen.copy(alpha = 0.6f)
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        )

                        if (query.isNotEmpty()) {
                            IconButton(
                                onClick = { viewModel.updateSearchQuery("") },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear",
                                    tint = RetroDarkGreen
                                )
                            }
                        }
                    }
                }

                // Circular Red Exit Button
                Surface(
                    color = RetroRedExit,
                    shape = CircleShape,
                    modifier = Modifier
                        .size(38.dp)
                        .combinedClickable(onClick = onExitClick)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Exit Screen",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // --- FILTER SELECTORS ROW ---
            Row(
                modifier = Modifier
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
                    val isSelected = sortMode == mode
                    TextButton(
                        onClick = {
                            viewModel.updateSortMode(mode)
                            coroutineScope.launch {
                                gridState.scrollToItem(0)
                            }
                        }
                    ) {
                        Text(
                            text = label,
                            fontFamily = pressStart2PFontFamily,
                            fontSize = 10.sp,
                            color = if (isSelected) RetroLcdGreen else Color.Gray
                        )
                    }
                }
            }

            // --- MAIN GRID / CONTENT AREA ---
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures { change, dragAmount ->
                            change.consume()
                            if (dragAmount > 50) {
                                viewModel.switchToPreviousSystem()
                            } else if (dragAmount < -50) {
                                viewModel.switchToNextSystem()
                            }
                        }
                    }
            ) {
                when {
                    games.isEmpty() -> {
                        // Empty State
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "NO GAMES FOUND",
                                fontFamily = pressStart2PFontFamily,
                                fontSize = 12.sp,
                                color = RetroDarkGreen,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(24.dp)
                            )
                        }
                    }

                    /*games.size == 1 -> {
                        // Single Game Centered View with Pulsing Selector Arrow
                        val game = games.first()
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            // Bouncing 8-Bit Arrow
                            androidx.compose.foundation.Canvas(
                                modifier = Modifier
                                    .size(32.dp)
                                    .offset(y = arrowOffset.dp)
                            ) {
                                val path = Path().apply {
                                    moveTo(size.width / 2f, size.height)
                                    lineTo(0f, 0f)
                                    lineTo(size.width, 0f)
                                    close()
                                }
                                drawPath(path, color = RetroLcdGreen)
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Box(
                                modifier = Modifier
                                    .size(200.dp)
                                    .rippleLift(ripple, coverShape)
                                    .combinedClickable(
                                        onClick = { launchGameWithAnimation(game) },
                                        onLongClick = { onGameLongClick(game) }
                                    )
                            ) {
                                LemuroidGameImage(
                                    game = game,
                                    modifier = Modifier.fillMaxSize(),
                                    applyAspectRatio = false,
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }*/

                    else -> {
                        // Regular 3-Column Grid
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
                                    onClick = { launchGameWithAnimation(game) },
                                    onLongClick = { onGameLongClick(game) }
                                )
                            }
                        }
                    }
                }

                // --- RIPPLE SIDE INDICATOR ARROWS ---
                if (ripple.isRunning) {
                    // Left White Pulsing Triangle
                    androidx.compose.foundation.Canvas(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 12.dp)
                            .width(14.dp)
                            .height(24.dp)
                            .graphicsLayer {
                                scaleX = sideArrowScale
                                scaleY = sideArrowScale
                                alpha = sideArrowAlpha
                            }
                    ) {
                        val path = Path().apply {
                            moveTo(0f, size.height / 2f)
                            lineTo(size.width, 0f)
                            lineTo(size.width, size.height)
                            close()
                        }
                        drawPath(path, color = Color.White)
                    }

                    // Right White Pulsing Triangle
                    androidx.compose.foundation.Canvas(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 12.dp)
                            .width(14.dp)
                            .height(24.dp)
                            .graphicsLayer {
                                scaleX = sideArrowScale
                                scaleY = sideArrowScale
                                alpha = sideArrowAlpha
                            }
                    ) {
                        val path = Path().apply {
                            moveTo(size.width, size.height / 2f)
                            lineTo(0f, 0f)
                            lineTo(0f, size.height)
                            close()
                        }
                        drawPath(path, color = Color.White)
                    }
                }
            }
        }

        // --- TAP COVER FULL-SCREEN LAUNCH ANIMATION OVERLAY ---
        animatingGame?.let { game ->
            val p = animProgress.value
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = p.coerceIn(0f, 1f)))
                    .zIndex(100f),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size((180 + p * 160).dp)
                        .graphicsLayer {
                            rotationZ = p * 360f
                            alpha = (1f - p * 0.8f).coerceIn(0f, 1f)
                        }
                        .clip(coverShape)
                ) {
                    LemuroidGameImage(
                        game = game,
                        modifier = Modifier.fillMaxSize(),
                        applyAspectRatio = false,
                        contentScale = ContentScale.Crop
                    )
                }
            }
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
