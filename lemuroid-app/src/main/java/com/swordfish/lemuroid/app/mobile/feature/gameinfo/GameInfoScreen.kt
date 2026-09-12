package com.swordfish.lemuroid.app.mobile.feature.gameinfo

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.LemuroidGameImage
import com.swordfish.lemuroid.common.kotlin.cleanGameTitle
import com.swordfish.lemuroid.lib.library.db.entity.Game

@Composable
fun GameInfoScreen(
    viewModel: GameInfoViewModel,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {}
) {
    val game = viewModel.game.collectAsState().value
    val isRescanning = viewModel.isRescanning.collectAsState().value
    val pendingMetadata = viewModel.pendingMetadata.collectAsState().value
    var showEditDialog by remember { mutableStateOf(false) }

    if (game == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (showEditDialog) {
        EditDetailsDialog(
            game = game,
            onDismiss = { showEditDialog = false },
            onSave = { title, date, pub, dev, reg, ver ->
                viewModel.updateGameDetails(title, date, pub, dev, reg, ver)
                showEditDialog = false
            },
            onThumbnailSelected = { uri, deleteSource ->
                viewModel.saveLocalThumbnail(uri, deleteSource)
            }
        )
    }

    if (pendingMetadata != null) {
        AlertDialog(
            onDismissRequest = { viewModel.confirmOverwrite(false) },
            icon = { Icon(Icons.Default.Warning, contentDescription = null) },
            title = { Text("Overwrite Data?") },
            text = { Text("Matching metadata found. Do you want to overwrite your existing game details with server data?") },
            confirmButton = {
                Button(onClick = { viewModel.confirmOverwrite(true) }) {
                    Text("Overwrite")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.confirmOverwrite(false) }) {
                    Text("Keep Current")
                }
            }
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                val title = game.title.cleanGameTitle()
                val titleParts = title.split(" - ", limit = 2).map { it.trim() }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = titleParts[0],
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center
                    )
                    if (titleParts.size > 1) {
                        Text(
                            text = titleParts[1],
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .aspectRatio(1f)
                        .clip(MaterialTheme.shapes.medium)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    LemuroidGameImage(
                        game = game,
                        modifier = Modifier.fillMaxSize(),
                        applyAspectRatio = false,
                        contentScale = ContentScale.Fit
                    )
                }
            }

            item {
                val region = game.country ?: "Unknown Region"
                val version = game.summary ?: ""
                val infoText = listOfNotNull(region.takeIf { it.isNotEmpty() }, version.takeIf { it.isNotEmpty() }).joinToString(" | ")
                
                if (infoText.isNotEmpty()) {
                    Text(
                        text = infoText,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }

            item {
                val year = game.releaseDate?.take(4) ?: "Unknown"
                val publisher = game.publisher ?: "Unknown"
                val developer = game.developer ?: "Unknown"
                Text(
                    text = "$year | $publisher | $developer",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = androidx.compose.material3.CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("ROM File", style = MaterialTheme.typography.labelMedium)
                        }
                        Text(
                            text = game.fileName,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { showEditDialog = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Edit")
                    }
                    OutlinedButton(
                        onClick = { viewModel.rescan() },
                        enabled = !isRescanning,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (isRescanning) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Rescan")
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        Button(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Icon(Icons.Default.Close, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Close")
        }
    }
}

@Composable
private fun EditDetailsDialog(
    game: Game,
    onDismiss: () -> Unit,
    onSave: (String, String?, String?, String?, String?, String?) -> Unit,
    onThumbnailSelected: (Uri, Boolean) -> Unit
) {
    var title by remember { mutableStateOf(game.title) }
    var date by remember { mutableStateOf(game.releaseDate ?: "") }
    var publisher by remember { mutableStateOf(game.publisher ?: "") }
    var developer by remember { mutableStateOf(game.developer ?: "") }
    var region by remember { mutableStateOf(game.country ?: "") }
    var version by remember { mutableStateOf(game.summary ?: "") }
    var deleteSource by remember { mutableStateOf(false) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onThumbnailSelected(it, deleteSource) }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = MaterialTheme.shapes.large
        ) {
            LazyColumn(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(text = "Edit Game Details", style = MaterialTheme.typography.titleLarge)
                }

                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("ROM Filename (Read-only)", style = MaterialTheme.typography.labelSmall)
                        Text(game.fileName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                }
                
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { imagePicker.launch("image/*") }
                            .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.medium)
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(60.dp)) {
                            LemuroidGameImage(game = game, applyAspectRatio = true)
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.3f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = androidx.compose.ui.graphics.Color.White)
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Change Box Art", style = MaterialTheme.typography.titleSmall)
                            Text("Tap to select image", style = MaterialTheme.typography.bodySmall)
                            
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(checked = deleteSource, onCheckedChange = { deleteSource = it })
                                Text("Delete original after copy", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp)
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = region,
                            onValueChange = { region = it },
                            label = { Text("Region") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = version,
                            onValueChange = { version = it },
                            label = { Text("Version") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = date,
                        onValueChange = { date = it },
                        label = { Text("Release Year/Date") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                item {
                    OutlinedTextField(
                        value = publisher,
                        onValueChange = { publisher = it },
                        label = { Text("Publisher") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                item {
                    OutlinedTextField(
                        value = developer,
                        onValueChange = { developer = it },
                        label = { Text("Developer") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = { onSave(title, date, publisher, developer, region, version) }) {
                            Text("Save")
                        }
                    }
                }
            }
        }
    }
}
