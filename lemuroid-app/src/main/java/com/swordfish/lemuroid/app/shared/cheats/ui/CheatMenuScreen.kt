package com.swordfish.lemuroid.app.shared.cheats.ui

import android.graphics.Typeface
import android.net.Uri
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.mobile.shared.DefaultBackground
import com.swordfish.lemuroid.app.mobile.shared.RetroCoral
import com.swordfish.lemuroid.app.mobile.shared.RetroCoralEdge
import com.swordfish.lemuroid.app.mobile.shared.RetroCyan
import com.swordfish.lemuroid.app.mobile.shared.RetroCyanEdge
import com.swordfish.lemuroid.app.mobile.shared.RetroLime
import com.swordfish.lemuroid.app.mobile.shared.RetroLimeEdge
import com.swordfish.lemuroid.app.mobile.shared.RetroMagenta
import com.swordfish.lemuroid.app.mobile.shared.RetroMagentaEdge
import com.swordfish.lemuroid.app.mobile.shared.RetroMuted
import com.swordfish.lemuroid.app.mobile.shared.RetroPanel
import com.swordfish.lemuroid.app.mobile.shared.RetroSearchBar
import com.swordfish.lemuroid.app.mobile.shared.RetroText
import com.swordfish.lemuroid.app.mobile.shared.RetroViolet
import com.swordfish.lemuroid.app.mobile.shared.RetroVioletEdge
import com.swordfish.lemuroid.lib.library.db.entity.GameCheatEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

val pressStart2PFontFamily = FontFamily(
    Font(R.font.press_start_2p, FontWeight.Normal)
)

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CheatMenuScreen(
    modifier: Modifier = Modifier,
    cheatsFlow: Flow<List<GameCheatEntity>>,
    searchQueryFlow: Flow<String>,
    selectedSourcesFlow: Flow<Set<String>>,
    isRescanningFlow: Flow<Boolean>,
    deletedCheatsFlow: Flow<Map<Int, GameCheatEntity>>,
    onCheatToggle: (GameCheatEntity, Boolean) -> Unit,
    onDeleteCheat: (GameCheatEntity) -> Unit,
    onUndoDeleteCheat: (Int) -> Unit,
    onUpdateCheatOrder: (cheatId: Int, newOrder: Int) -> Unit,
    onImportCheats: (Uri) -> Unit = {},
    onSetSearchQuery: (String) -> Unit,
    onToggleSourceFilter: (String) -> Unit,
    onClearSourceFilter: () -> Unit,
    onRescanCheats: () -> Unit,
    onClearCheats: () -> Unit = {},
    onDisableAllCheats: () -> Unit = {},
    onClose: () -> Unit,
) {
    val cheats by cheatsFlow.collectAsState(initial = emptyList())
    val searchQuery by searchQueryFlow.collectAsState(initial = "")
    val selectedSources by selectedSourcesFlow.collectAsState(initial = emptySet())
    val isRescanning by isRescanningFlow.collectAsState(initial = false)
    val deletedCheats by deletedCheatsFlow.collectAsState(initial = emptyMap())
    val snackbarHostState = remember { SnackbarHostState() }

    val rippleEngine = remember { TimedRippleEngine() }
    var listSize by remember { mutableStateOf(IntSize.Zero) }

    val density = LocalDensity.current.density
    LaunchedEffect(listSize) {
        if (listSize.height > 0) {
            rippleEngine.startLoop(listSize.height.toFloat(), density)
        }
    }

    // Freeze list item order while screen is active so toggling stars doesn't jump items
    val currentCheatIds = remember(cheats.size) { cheats.map { it.id } }
    val displayCheats = remember(cheats, currentCheatIds) {
        val cheatMap = cheats.associateBy { it.id }
        currentCheatIds.mapNotNull { cheatMap[it] } + (cheats - cheatMap.values.toSet())
    }

    val cachedSources = remember { mutableStateOf<List<String>>(emptyList()) }
    val allSources = remember(cheats) {
        val currentSources = cheats.mapNotNull { it.source }.distinct()
        val combined = (cachedSources.value + currentSources).distinct().sortedWith { a, b ->
            val isOthersA = a.equals("Others", ignoreCase = true) || a.equals("Other", ignoreCase = true)
            val isOthersB = b.equals("Others", ignoreCase = true) || b.equals("Other", ignoreCase = true)
            if (isOthersA != isOthersB) {
                if (isOthersA) 1 else -1
            } else {
                a.compareTo(b, ignoreCase = true)
            }
        }
        cachedSources.value = combined
        combined
    }

    val lazyListState = rememberLazyListState()

    LaunchedEffect(selectedSources) {
        if (cheats.isNotEmpty()) {
            lazyListState.scrollToItem(0)
        }
    }

    var activeNotification by remember { mutableStateOf<Pair<Int, String>?>(null) }
    var handledCheatId by remember { mutableStateOf<Int?>(null) }
    var activeUndoCheat by remember { mutableStateOf<Pair<Int, String>?>(null) }

    var dismissJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(deletedCheats) {
        if (deletedCheats.isNotEmpty()) {
            val newestId = deletedCheats.keys.maxOrNull()

            // Only trigger if this is a NEW deletion that hasn't been undone or dismissed yet
            if (newestId != null && newestId != handledCheatId) {
                val cheat = deletedCheats[newestId]
                activeNotification = Pair(newestId, cheat?.description ?: "Cheat")

                // Auto-dismiss notification after 4 seconds
                kotlinx.coroutines.delay(4000L)
                if (activeNotification?.first == newestId) {
                    handledCheatId = newestId
                    activeNotification = null
                }
            }
        } else {
            activeNotification = null
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        DefaultBackground(modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            CheatMenuHeader(
                searchQuery = searchQuery,
                onSetSearchQuery = onSetSearchQuery,
                onClose = onClose,
                isRescanning = isRescanning,
                allSources = allSources,
                selectedSources = selectedSources,
                onClearSourceFilter = onClearSourceFilter,
                onToggleSourceFilter = onToggleSourceFilter
            )

            val hasSearchOrFilter = searchQuery.isNotBlank() || selectedSources.isNotEmpty()

            CheatMenuList(
                displayCheats = displayCheats,
                isRescanning = isRescanning,
                hasSearchOrFilter = hasSearchOrFilter,
                rippleEngine = rippleEngine,
                lazyListState = lazyListState,
                onCheatToggle = onCheatToggle,
                onDeleteCheat = { cheat ->
                    // 1. Trigger actual delete logic
                    onDeleteCheat(cheat)

                    // 2. Set active undo banner to THIS specific cheat
                    activeUndoCheat = Pair(cheat.id, cheat.description ?: "Cheat")

                    // 3. Reset auto-dismiss timer
                    dismissJob?.cancel()
                    dismissJob = coroutineScope.launch {
                        kotlinx.coroutines.delay(4000L)
                        activeUndoCheat = null
                    }
                },
                onRescanCheats = onRescanCheats,
                onListSizeChanged = { listSize = it },
                modifier = Modifier.weight(1f)
            )

            CheatMenuBottomBar(
                isRescanning = isRescanning,
                onRescanCheats = onRescanCheats,
                onDisableAllCheats = onDisableAllCheats
            )
        }

        CheatMenuSnackbar(
            deletedCheat = activeUndoCheat,
            onUndo = { id ->
                dismissJob?.cancel()
                activeUndoCheat = null      // Clear UI state immediately
                onUndoDeleteCheat(id)       // Restore ONLY this single cheat
            },
            onDismiss = {
                dismissJob?.cancel()
                activeUndoCheat = null
            }
        )
    }
}

// --- Private Helper Composables ---

@Composable
private fun CheatMenuHeader(
    searchQuery: String,
    onSetSearchQuery: (String) -> Unit,
    onClose: () -> Unit,
    isRescanning: Boolean,
    allSources: List<String>,
    selectedSources: Set<String>,
    onClearSourceFilter: () -> Unit,
    onToggleSourceFilter: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        RetroSearchBar(
            query = searchQuery,
            onQueryChange = onSetSearchQuery,
            onExitClick = onClose,
            placeholderText = "SEARCH CHEATS..."
        )

        // Source Filter Row
        if (allSources.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 4.dp, horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val isAllSelected = selectedSources.isEmpty()

                TextButton(
                    onClick = onClearSourceFilter,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "ALL",
                        fontFamily = pressStart2PFontFamily,
                        fontSize = 9.sp,
                        fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isAllSelected) RetroText else RetroMuted
                    )
                }

                allSources.forEach { source ->
                    val isSelected = source in selectedSources
                    val vibrantColor = getSourceColor(source)
                    val edgeColor = getSourceEdgeColor(source)

                    TextButton(
                        onClick = {
                            onClearSourceFilter()
                            if (!isSelected) {
                                onToggleSourceFilter(source)
                            }
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = source.uppercase(),
                            fontFamily = pressStart2PFontFamily,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) vibrantColor else edgeColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CheatMenuList(
    displayCheats: List<GameCheatEntity>,
    isRescanning: Boolean,
    hasSearchOrFilter: Boolean,
    rippleEngine: TimedRippleEngine,
    lazyListState: LazyListState,
    onCheatToggle: (GameCheatEntity, Boolean) -> Unit,
    onDeleteCheat: (GameCheatEntity) -> Unit,
    onRescanCheats: () -> Unit,
    onListSizeChanged: (IntSize) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxWidth()) {
        when {
            isRescanning -> {
                CheatMenuRescanLoadingState()
            }

            displayCheats.isEmpty() -> {
                CheatMenuEmptyState(
                    hasSearchOrFilter = hasSearchOrFilter,
                    onRescanCheats = onRescanCheats
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .onGloballyPositioned {
                            onListSizeChanged(it.size)
                            rippleEngine.listTopPx = it.positionInRoot().y
                        },
                    state = lazyListState,
                ) {
                    itemsIndexed(
                        items = displayCheats,
                        key = { _, cheat -> cheat.id }
                    ) { _, cheat ->
                        CheatItemRow(
                            cheat = cheat,
                            rippleEngine = rippleEngine,
                            onToggle = { onCheatToggle(cheat, !cheat.enabled) },
                            onDelete = { onDeleteCheat(cheat) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CheatMenuEmptyState(
    hasSearchOrFilter: Boolean,
    onRescanCheats: () -> Unit
) {
    val messageText = if (hasSearchOrFilter) {
        "NO CHEATS FOUND."
    } else {
        "NO CHEATS\nAVAILABLE FOR\nTHIS GAME."
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.offset(y = (-120).dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.game_grl_gum),
                contentDescription = "Game Girl Gum Graphic",
                modifier = Modifier
                    .height(200.dp)
                    .graphicsLayer(
                        scaleX = 1.5f,
                        scaleY = 1.5f
                    )
                    .offset(x = (-12).dp)
            )

            Text(
                text = messageText,
                fontFamily = pressStart2PFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = RetroLimeEdge,
                textAlign = TextAlign.Center,
                modifier = Modifier.offset(y = (-50).dp)
            )
        }
    }
}

@Composable
private fun CheatMenuRescanLoadingState() {
    val infiniteTransition = rememberInfiniteTransition(label = "RescanStarRotation")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "StarRingRotation"
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Rotating circle of violet stars container
            Box(
                modifier = Modifier.size(96.dp).offset(y = (-80).dp),
                contentAlignment = Alignment.Center
            ) {
                val starCount = 6
                val radiusPx = with(LocalDensity.current) { 32.dp.toPx() }

                // Draw rotating ring of stars
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .graphicsLayer { rotationZ = rotationAngle },
                    contentAlignment = Alignment.Center
                ) {
                    for (i in 0 until starCount) {
                        val angle = (i * (360f / starCount)) * (Math.PI / 180f)
                        val xOffset = (radiusPx * cos(angle)).dp
                        val yOffset = (radiusPx * sin(angle)).dp

                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = RetroViolet,
                            modifier = Modifier
                                .size(46.dp)
                                .offset(x = xOffset, y = yOffset)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "SCANNING DATABASE...",
                fontFamily = pressStart2PFontFamily,
                fontSize = 10.sp,
                color = RetroVioletEdge,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun BoxScope.CheatMenuSnackbar(
    deletedCheat: Pair<Int, String>?,
    onUndo: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    if (deletedCheat != null) {
        val (cheatId, cheatDescription) = deletedCheat

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(RetroPanel)
                .drawWithContent {
                    drawContent()
                    val stroke = 2.dp.toPx()
                    val cornerRadius = 8.dp.toPx()
                    drawRoundRect(
                        color = RetroLime,
                        topLeft = Offset(stroke / 2f, stroke / 2f),
                        size = Size(size.width - stroke, size.height - stroke),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius, cornerRadius),
                        style = Stroke(width = stroke)
                    )
                }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left group: Label + Description sitting tight next to each other
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "DELETED: ",
                        fontFamily = pressStart2PFontFamily,
                        fontSize = 9.sp,
                        color = RetroLime
                    )

                    Text(
                        text = cheatDescription.uppercase(),
                        fontFamily = remember {
                            FontFamily(
                                Typeface.create(
                                    "sans-serif-condensed",
                                    Typeface.NORMAL
                                )
                            )
                        },
                        fontSize = 12.sp, // Slightly increased for sans-serif readability
                        color = RetroText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                TextButton(
                    onClick = {
                        onUndo(cheatId)
                        onDismiss()
                    }
                ) {
                    Text(
                        text = "UNDO",
                        fontFamily = pressStart2PFontFamily,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = RetroMagenta
                    )
                }
            }
        }
    }
}

// --- Source Color Resolvers ---

fun getSourceColor(source: String?): Color {
    return when (source?.lowercase()) {
        "actionreplay" -> RetroViolet
        "gamegenie" -> RetroCoral
        "gameshark" -> RetroLime
        "codebreaker" -> RetroCyan
        else -> RetroMagenta
    }
}

fun getSourceEdgeColor(source: String?): Color {
    return when (source?.lowercase()) {
        "actionreplay" -> RetroVioletEdge
        "gamegenie" -> RetroCoralEdge
        "gameshark" -> RetroLimeEdge
        "codebreaker" -> RetroCyanEdge
        else -> RetroMagentaEdge
    }
}
