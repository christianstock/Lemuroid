package com.swordfish.lemuroid.app.shared.cheats.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swordfish.lemuroid.lib.library.db.entity.GameCheatEntity
import kotlinx.coroutines.flow.Flow

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
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
    onImportCheats: (Uri) -> Unit,
    onSetSearchQuery: (String) -> Unit,
    onToggleSourceFilter: (String) -> Unit,
    onClearSourceFilter: () -> Unit,
    onRescanCheats: () -> Unit,
    onDisableAllCheats: () -> Unit,
    onClose: () -> Unit,
) {
    val cheats = cheatsFlow.collectAsState(initial = emptyList()).value
    val searchQuery = searchQueryFlow.collectAsState(initial = "").value
    val selectedSources = selectedSourcesFlow.collectAsState(initial = emptySet()).value
    val isRescanning = isRescanningFlow.collectAsState(initial = false).value
    val deletedCheats = deletedCheatsFlow.collectAsState(initial = emptyMap()).value
    val snackbarHostState = remember { SnackbarHostState() }

    // Extract all unique sources
    val allSources = remember(cheats) {
        cheats.mapNotNull { it.source }.distinct().sorted()
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onImportCheats(it) }
    }

    val lazyListState = rememberLazyListState()

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header with title and action buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Cheats",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(modifier = Modifier.weight(1f))

                IconButton(
                    onClick = onRescanCheats,
                    enabled = !isRescanning && cheats.isNotEmpty()
                ) {
                    if (isRescanning) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(8.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Rescan cheats",
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }

                IconButton(
                    onClick = onDisableAllCheats,
                    enabled = cheats.isNotEmpty()
                ) {
                    Text(
                        text = "∅",
                        style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                IconButton(onClick = { filePickerLauncher.launch("application/zip") }) {
                    Icon(
                        imageVector = Icons.Default.FileDownload,
                        contentDescription = "Import cheats",
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }

                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close cheats menu",
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
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

            // Filter chips
            if (allSources.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = selectedSources.isEmpty(),
                        onClick = onClearSourceFilter,
                        label = { Text("All") }
                    )

                    allSources.forEach { source ->
                        FilterChip(
                            selected = source in selectedSources,
                            onClick = { onToggleSourceFilter(source) },
                            label = { Text(source) }
                        )
                    }
                }
            }

            // Cheat list
            if (cheats.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No cheats available",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.padding(8.dp))
                        Button(onClick = { filePickerLauncher.launch("application/zip") }) {
                            Text("Import from ZIP")
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
                    itemsIndexed(cheats) { index, cheat ->
                        CheatItemRow(
                            cheat = cheat,
                            displayOrder = index,
                            onToggle = { enabled ->
                                onCheatToggle(cheat, enabled)
                            },
                            onDelete = {
                                onDeleteCheat(cheat)
                            },
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
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit,
) {
    val backgroundColor = getSourceColor(cheat.source)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 8.dp)
            .background(
                color = backgroundColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(8.dp),
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
