package com.swordfish.lemuroid.app.shared.cheats.ui

import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.mobile.shared.DefaultBackground
import com.swordfish.lemuroid.app.mobile.shared.RetroBorder
import com.swordfish.lemuroid.app.mobile.shared.RetroBorderLight
import com.swordfish.lemuroid.app.mobile.shared.RetroCoral
import com.swordfish.lemuroid.app.mobile.shared.RetroCoralEdge
import com.swordfish.lemuroid.app.mobile.shared.RetroCyan
import com.swordfish.lemuroid.app.mobile.shared.RetroCyanEdge
import com.swordfish.lemuroid.app.mobile.shared.RetroLcdGreen
import com.swordfish.lemuroid.app.mobile.shared.RetroLcdGreenDark
import com.swordfish.lemuroid.app.mobile.shared.RetroLime
import com.swordfish.lemuroid.app.mobile.shared.RetroLimeEdge
import com.swordfish.lemuroid.app.mobile.shared.RetroMagenta
import com.swordfish.lemuroid.app.mobile.shared.RetroMagentaEdge
import com.swordfish.lemuroid.app.mobile.shared.RetroMuted
import com.swordfish.lemuroid.app.mobile.shared.RetroPanel
import com.swordfish.lemuroid.app.mobile.shared.RetroPanelLight
import com.swordfish.lemuroid.app.mobile.shared.RetroSearchBar
import com.swordfish.lemuroid.app.mobile.shared.RetroText
import com.swordfish.lemuroid.app.mobile.shared.RetroViolet
import com.swordfish.lemuroid.app.mobile.shared.RetroVioletEdge
import com.swordfish.lemuroid.lib.library.db.entity.GameCheatEntity
import kotlinx.coroutines.flow.Flow

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

    Box(modifier = modifier.fillMaxSize()) {
        DefaultBackground(
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            RetroSearchBar(
                query = searchQuery,
                onQueryChange = onSetSearchQuery,
                onExitClick = onClose,
                placeholderText = "SEARCH CHEATS..."
            )

            if (isRescanning) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Scanning Libretro cheat database...",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }

            // Source Filter Row with system-specific vibrant/edge colors
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
                            fontSize = 10.sp,
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

            // Main cheat list container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = true)
            ) {
                if (displayCheats.isEmpty() && !isRescanning) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "No cheats available for this game",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(onClick = onRescanCheats) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.size(4.dp))
                                Text("Rescan Libretro Database")
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .onGloballyPositioned {
                                listSize = it.size
                                rippleEngine.listTopPx = it.positionInRoot().y
                            },
                        state = lazyListState,
                    ) {
                        itemsIndexed(
                            items = displayCheats,
                            key = { _, cheat -> cheat.id }
                        ) { index, cheat ->
                            CheatItemRow(
                                cheat = cheat,
                                rippleEngine = rippleEngine,
                                onToggle = {
                                    onCheatToggle(cheat, !cheat.enabled)
                                },
                                onDelete = {
                                    onDeleteCheat(cheat)
                                }
                            )
                        }
                    }
                }
            }

            // Tab-style Bottom Action Bar (Rescan & Unselect All)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(RetroPanelLight)
                    .drawWithContent {
                        drawContent()
                        drawLine(
                            color = RetroBorder,
                            start = Offset(0f, 0f),
                            end = Offset(size.width, 0f),
                            strokeWidth = 4.dp.toPx()
                        )
                    }
                    .padding(top = 12.dp)
                    .padding(horizontal = 16.dp)
                    .navigationBarsPadding(), // Pushes the entire bottom row container cleanly above the nav bar!
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                RetroBottomActionButton(
                    label = "RESCAN",
                    isLoading = isRescanning,
                    onClick = onRescanCheats,
                    modifier = Modifier.weight(1f)
                )

                RetroBottomActionButton(
                    label = "UNSELECT ALL",
                    onClick = onDisableAllCheats,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (deletedCheats.isNotEmpty()) {
            val lastDeletedId = deletedCheats.keys.maxOrNull() ?: 0
            val lastDeletedCheat = deletedCheats[lastDeletedId]

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                Snackbar(
                    action = {
                        TextButton(onClick = { onUndoDeleteCheat(lastDeletedId) }) {
                            Text("Undo")
                        }
                    }
                ) {
                    Text("Cheat deleted: ${lastDeletedCheat?.description ?: "Unknown"}")
                }
            }
        }
    }
}

@Composable
private fun RetroBottomActionButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
) {
    val outerShape = RoundedCornerShape(8.dp)
    val faceShape = RoundedCornerShape(
        topStart = 6.dp, topEnd = 6.dp,
        bottomStart = 2.dp, bottomEnd = 2.dp,
    )

    Box(
        modifier = modifier
            .height(60.dp)
            .alpha(if (isLoading) 0.5f else 1f)
            .clip(outerShape)
            .background(RetroBorderLight)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = !isLoading,
                role = Role.Button,
                onClick = onClick,
            ),
    ) {
        // Bottom band, drawn over the outline so it has no border of its own
        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(8.dp)
                .background(RetroPanel)
        )

        // Face: 2 dp inside on top and sides, 6 dp on the bottom
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 2.dp, top = 2.dp, end = 2.dp, bottom = 8.dp)
                .clip(faceShape)
                .background(RetroPanelLight),
            contentAlignment = Alignment.Center,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = label,
                    fontFamily = pressStart2PFontFamily,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = RetroText,
                )
            }
        }
    }
}

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
