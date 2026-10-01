package com.swordfish.lemuroid.app.shared.cheats.ui

import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swordfish.lemuroid.lib.library.db.entity.GameCheatEntity
import kotlinx.coroutines.flow.Flow

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
    val cheats = cheatsFlow.collectAsState(initial = emptyList()).value
    val searchQuery = searchQueryFlow.collectAsState(initial = "").value
    val selectedSources = selectedSourcesFlow.collectAsState(initial = emptySet()).value
    val isRescanning = isRescanningFlow.collectAsState(initial = false).value
    val deletedCheats = deletedCheatsFlow.collectAsState(initial = emptyMap()).value
    val snackbarHostState = remember { SnackbarHostState() }

    // Extract all unique sources sorted alphabetically, with "Others" / "Other" last
    val allSources = remember(cheats) {
        cheats.mapNotNull { it.source }
            .distinct()
            .sortedWith { a, b ->
                val isOthersA = a.equals("Others", ignoreCase = true) || a.equals("Other", ignoreCase = true)
                val isOthersB = b.equals("Others", ignoreCase = true) || b.equals("Other", ignoreCase = true)
                if (isOthersA != isOthersB) {
                    if (isOthersA) 1 else -1
                } else {
                    a.compareTo(b, ignoreCase = true)
                }
            }
    }

    /*
    // ZIP import functionality commented out per user request
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onImportCheats(it) }
    }
    */

    val lazyListState = rememberLazyListState()
    val draggedItemIndex = remember { mutableStateOf<Int?>(null) }
    val mutableCheats = remember(cheats) { mutableStateOf(cheats) }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header with action buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Rescan button with label
                OutlinedButton(
                    onClick = onRescanCheats,
                    enabled = !isRescanning
                ) {
                    if (isRescanning) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Rescan cheats",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.size(4.dp))
                    Text("Rescan")
                }

                // Unselect All button with label
                OutlinedButton(
                    onClick = onDisableAllCheats,
                    enabled = cheats.isNotEmpty() && !isRescanning
                ) {
                    Icon(
                        imageVector = Icons.Default.RadioButtonUnchecked,
                        contentDescription = "Unselect all cheats",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Text("Unselect All")
                }
            }

            // Progress Bar during database rescan
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

            // Search box
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSetSearchQuery,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                placeholder = { Text("Search cheats...") },
                singleLine = true,
                maxLines = 1,
            )

            // Source Filter Chips - FlowRow wraps tabs onto a new line nicely
            if (allSources.isNotEmpty()) {
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    FilterChip(
                        selected = selectedSources.isEmpty(),
                        onClick = onClearSourceFilter,
                        label = { Text("All", maxLines = 1) }
                    )

                    allSources.forEach { source ->
                        FilterChip(
                            selected = source in selectedSources,
                            onClick = { onToggleSourceFilter(source) },
                            label = { Text(source, maxLines = 1) }
                        )
                    }
                }
            }

            // Cheat list
            if (cheats.isEmpty() && !isRescanning) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
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
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 8.dp),
                    state = lazyListState,
                ) {
                    itemsIndexed(mutableCheats.value) { index, cheat ->
                        CheatItemRow(
                            cheat = cheat,
                            displayOrder = index,
                            isDragged = draggedItemIndex.value == index,
                            onToggle = { enabled ->
                                onCheatToggle(cheat, enabled)
                            },
                            onDelete = {
                                onDeleteCheat(cheat)
                            },
                            onReorder = { fromIndex, toIndex ->
                                val newList = mutableCheats.value.toMutableList()
                                val item = newList.removeAt(fromIndex)
                                newList.add(toIndex, item)
                                mutableCheats.value = newList
                                onUpdateCheatOrder(cheat.id, toIndex)
                            },
                            onDragStart = { draggedItemIndex.value = index },
                            onDragEnd = { draggedItemIndex.value = null },
                        )
                    }
                }
            }
        }

        // Snackbar for undo delete
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
                        TextButton(onClick = {
                            onUndoDeleteCheat(lastDeletedId)
                        }) {
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
private fun CheatItemRow(
    cheat: GameCheatEntity,
    displayOrder: Int,
    isDragged: Boolean = false,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit,
    onReorder: (fromIndex: Int, toIndex: Int) -> Unit = { _, _ -> },
    onDragStart: () -> Unit = {},
    onDragEnd: () -> Unit = {},
) {
    val backgroundColor = getSourceColor(cheat.source)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 8.dp)
            .background(
                color = if (isDragged) 
                    backgroundColor.copy(alpha = 0.3f) 
                else 
                    backgroundColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(8.dp)
            .pointerInput(Unit) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { onDragStart() },
                    onDrag = { change, dragAmount ->
                        change.consume()
                    },
                    onDragEnd = { onDragEnd() }
                )
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Checkbox(
            checked = cheat.enabled,
            onCheckedChange = { enabled ->
                onToggle(enabled)
            },
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 4.dp)
        ) {
            Text(
                text = cheat.description,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
            )

            if (cheat.source != null) {
                Text(
                    text = cheat.source!!,
                    color = backgroundColor,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                )
            }
        }

        IconButton(
            onClick = onDelete,
            modifier = Modifier.wrapContentHeight()
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete cheat",
                tint = MaterialTheme.colorScheme.error,
            )
        }
    }
}

private fun getSourceColor(source: String?): Color {
    return when (source?.lowercase()) {
        "gameshark" -> Color(0xFF4CAF50)      // Green
        "codebreaker" -> Color(0xFF2196F3)    // Blue
        "actionreplay" -> Color(0xFF9C27B0)   // Purple
        "gamegenie" -> Color(0xFFFF9800)      // Orange
        else -> Color(0xFF757575)              // Gray for Others
    }
}
