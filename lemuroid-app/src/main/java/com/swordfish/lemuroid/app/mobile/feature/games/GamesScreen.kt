package com.swordfish.lemuroid.app.mobile.feature.games

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.LemuroidEmptyView
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.LemuroidGameImage
import com.swordfish.lemuroid.lib.library.db.entity.Game
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlin.math.exp
import kotlin.math.hypot
import kotlin.random.Random

/**
 * A highlight band that travels outward from the grid's top-left corner.
 * All distances are in "cover widths", so it scales with any column count or screen size.
 */
@Stable
class GridRipple(
    private val speed: Float = 3f,        // cover widths per second
    private val bandWidth: Float = 1f,    // leading edge width, in cover widths
    private val tailFactor: Float = 2f,      // trailing side factor
    private val strength: Float = 0.7f,     // max opacity/effect strength
) {
    var origin by mutableStateOf(Offset.Zero)
    var coverWidthPx by mutableFloatStateOf(1f)

    private val front = Animatable(INACTIVE)
    private var maxDist = 0f

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

    /** Returns 0..strength for a cover at distance [d] (in cover widths) from the origin. */
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

/** Lift-only look: scale up to +7% and a white overlay up to 40%, both multiplied by intensity. */
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
        .zIndex(if (kVal > 0.01f) 1f else 0f) // Keep expanding items on top of neighbors
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

@Composable
fun GamesScreen(
    modifier: Modifier = Modifier,
    viewModel: GamesViewModel,
    onGameClick: (Game) -> Unit,
    onGameLongClick: (Game) -> Unit,
    @Suppress("UNUSED_PARAMETER") onGameFavoriteToggle: (Game, Boolean) -> Unit,
    rippleIntervalMs: Long = 6_000L,
) {
    val games by viewModel.games.collectAsState()
    val query by viewModel.searchQuery.collectAsState()
    val showAll by viewModel.showAllSystems.collectAsState()
    val sortMode by viewModel.sortMode.collectAsState()

    val ripple = remember { GridRipple() }
    var gridSize by remember { mutableStateOf(IntSize.Zero) }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val coverShape = remember { RoundedCornerShape(4.dp) }

    // Periodically trigger the ripple when grid is populated and lifecycle is RESUMED
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

    Column(modifier = modifier.fillMaxSize()) {
        // --- TOP BAR: SEARCH & FILTERS ---
        Surface(
            tonalElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search Title or Metadata...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = if (query.isNotEmpty()) {
                        {
                            IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                Icon(Icons.Default.Close, contentDescription = null)
                            }
                        }
                    } else null,
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = !showAll,
                        onClick = { viewModel.toggleShowAllSystems(false) },
                        label = { Text(viewModel.currentMetaSystem.name) }
                    )

                    FilterChip(
                        selected = showAll,
                        onClick = { viewModel.toggleShowAllSystems(true) },
                        label = { Text("All") }
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    IconButton(
                        onClick = { viewModel.updateSortMode(GamesViewModel.SortMode.RECENTS) },
                        colors = if (sortMode == GamesViewModel.SortMode.RECENTS)
                            IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                        else IconButtonDefaults.iconButtonColors()
                    ) {
                        Icon(Icons.Default.Update, contentDescription = "Recents")
                    }

                    IconButton(
                        onClick = { viewModel.updateSortMode(GamesViewModel.SortMode.ALPHABETICAL) },
                        colors = if (sortMode == GamesViewModel.SortMode.ALPHABETICAL)
                            IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                        else IconButtonDefaults.iconButtonColors()
                    ) {
                        Icon(Icons.Default.SortByAlpha, contentDescription = "A-Z")
                    }

                    IconButton(
                        onClick = { viewModel.updateSortMode(GamesViewModel.SortMode.RELEASE) },
                        colors = if (sortMode == GamesViewModel.SortMode.RELEASE)
                            IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                        else IconButtonDefaults.iconButtonColors()
                    ) {
                        Icon(Icons.Default.CalendarToday, contentDescription = "Release Date")
                    }
                }
            }
        }

        if (games.isEmpty()) {
            LemuroidEmptyView()
        } else {
            LazyVerticalGrid(
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
                        onClick = { onGameClick(game) },
                        onLongClick = { onGameLongClick(game) }
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
