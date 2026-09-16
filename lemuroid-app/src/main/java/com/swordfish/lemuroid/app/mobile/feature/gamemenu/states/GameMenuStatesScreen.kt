package com.swordfish.lemuroid.app.mobile.feature.gamemenu.states

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GameMenuStatesScreen(
    viewModel: GameMenuStatesViewModel,
    isSaveRoute: Boolean,
    onStateClicked: (Int) -> Unit,
    onCancel: () -> Unit,
) {
    val state = viewModel.uiStates.collectAsState(initial = GameMenuStatesViewModel.State())
    var selectedSlotEntry by remember { mutableStateOf<GameMenuStatesViewModel.StateEntry?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        val entryToName = selectedSlotEntry
        if (isSaveRoute && entryToName != null) {
            SaveSlotNamingScreen(
                entry = entryToName,
                onConfirm = { nameText ->
                    viewModel.saveSlotName(entryToName.slotIndex, nameText)
                    onStateClicked(entryToName.slotIndex)
                    selectedSlotEntry = null
                },
                onCancel = {
                    selectedSlotEntry = null
                }
            )
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                val quickSaveEntry = state.value.entries.firstOrNull { it.isQuickSave }
                val regularEntries = state.value.entries.filter { !it.isQuickSave }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    if (quickSaveEntry != null) {
                        Box(modifier = Modifier.width(160.dp)) {
                            StateGridItem(
                                entry = quickSaveEntry,
                                onClick = {
                                    onStateClicked(-1)
                                }
                            )
                        }
                    }

                    regularEntries.chunked(2).forEach { rowEntries ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            rowEntries.forEach { entry ->
                                Box(modifier = Modifier.weight(1f)) {
                                    StateGridItem(
                                        entry = entry,
                                        onClick = {
                                            if (isSaveRoute) {
                                                selectedSlotEntry = entry
                                            } else {
                                                onStateClicked(entry.slotIndex)
                                            }
                                        }
                                    )
                                }
                            }
                            if (rowEntries.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

                Button(
                    onClick = onCancel,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text("CANCEL", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun SaveSlotNamingScreen(
    entry: GameMenuStatesViewModel.StateEntry,
    onConfirm: (String) -> Unit,
    onCancel: () -> Unit
) {
    var nameText by remember { mutableStateOf(entry.customName ?: "") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = entry.description.ifEmpty { "New Save State" },
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )

        Box(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .aspectRatio(1f),
            contentAlignment = Alignment.Center
        ) {
            if (entry.preview != null) {
                Image(
                    bitmap = entry.preview.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            "EMPTY",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = nameText,
            onValueChange = { nameText = it },
            label = { Text("Name (Optional)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text("CANCEL", fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { onConfirm(nameText) },
                modifier = Modifier.weight(1f)
            ) {
                Text("OK", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun StateGridItem(
    entry: GameMenuStatesViewModel.StateEntry,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        enabled = entry.enabled,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (entry.enabled) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .aspectRatio(1f)
                    .padding(bottom = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                if (entry.preview != null) {
                    Image(
                        bitmap = entry.preview.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                "EMPTY",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }

            val displayName = when {
                entry.isQuickSave -> "QUICK SAVE"
                !entry.customName.isNullOrEmpty() -> entry.customName.uppercase()
                else -> null
            }

            if (displayName != null) {
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
            }

            Text(
                text = entry.description.ifEmpty { "No data" },
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                maxLines = 2,
                minLines = 2,
                lineHeight = 14.sp
            )
        }
    }
}
