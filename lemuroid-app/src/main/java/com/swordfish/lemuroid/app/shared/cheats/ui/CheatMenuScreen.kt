package com.swordfish.lemuroid.app.shared.cheats.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.swordfish.lemuroid.lib.library.db.entity.GameCheatEntity
import kotlinx.coroutines.flow.Flow

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CheatMenuScreen(
    modifier: Modifier = Modifier,
    cheatsFlow: Flow<List<GameCheatEntity>>,
    onCheatToggle: (GameCheatEntity, Boolean) -> Unit,
    onImportCheats: (Uri) -> Unit,
    onClose: () -> Unit,
) {
    val cheats = cheatsFlow.collectAsState(initial = emptyList()).value
    val groupedCheats = remember(cheats) {
        // Sort cheats within groups: Master codes first
        val sortedCheats = cheats.sortedWith(compareByDescending<GameCheatEntity> {
            it.description.contains("Master", ignoreCase = true) || 
            it.description.contains("(M)", ignoreCase = true) ||
            it.description.contains("Must Be On", ignoreCase = true)
        }.thenBy { it.description })

        // Sort groups: Known types first (in order), then Others last
        val knownOrder = listOf("GameShark", "CodeBreaker", "ActionReplay", "GameGenie")
        val grouped = sortedCheats.groupBy { it.source ?: "Others" }
        grouped.toList()
            .sortedWith(compareBy({
                val index = knownOrder.indexOf(it.first)
                if (index >= 0) index else if (it.first == "Others") knownOrder.size else knownOrder.size + 1
            }, { it.first }))
            .toMap()
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onImportCheats(it) }
    }

    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        shadowElevation = 8.dp,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Cheats",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(modifier = Modifier.weight(1f))
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
                ) {
                    groupedCheats.forEach { (group, cheatsInGroup) ->
                        stickyHeader {
                            Text(
                                text = group,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.secondaryContainer)
                                    .padding(vertical = 12.dp, horizontal = 16.dp),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                        items(cheatsInGroup) { cheat ->
                            CheatItemRow(
                                cheat = cheat,
                                onToggle = { enabled ->
                                    onCheatToggle(cheat, enabled)
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun CheatItemRow(
    cheat: GameCheatEntity,
    onToggle: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = cheat.enabled,
            onCheckedChange = { enabled ->
                // Toggle cheat state
                onToggle(enabled)
            },
        )
        Text(
            text = cheat.description,
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
