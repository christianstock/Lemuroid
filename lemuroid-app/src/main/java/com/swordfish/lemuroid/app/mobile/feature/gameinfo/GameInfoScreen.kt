package com.swordfish.lemuroid.app.mobile.feature.gameinfo

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    val logs = viewModel.logs.collectAsState().value
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
            onSave = { date, pub, dev ->
                viewModel.updateGameDetails(date, pub, dev)
                showEditDialog = false
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
            // 1. Title at top with carousel-style subtitles
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

            // 2. Box art centered below
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

            // 3. Year | Publisher | Developer below the art
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

            // 4. Action Buttons
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

            // Technical Console
            if (logs.isNotEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .background(Color(0xFF121212), MaterialTheme.shapes.small)
                            .padding(8.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = "METADATA ENGINE TRACE",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Green,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        logs.forEach { log ->
                            Text(
                                text = "> $log",
                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                                color = if (log.contains("ERROR", true) || log.contains("FAIL")) Color.Red else Color.LightGray,
                                softWrap = true
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // Close button at bottom
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
    onSave: (String?, String?, String?) -> Unit
) {
    var date by remember { mutableStateOf(game.releaseDate ?: "") }
    var publisher by remember { mutableStateOf(game.publisher ?: "") }
    var developer by remember { mutableStateOf(game.developer ?: "") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = "Edit Game Details", style = MaterialTheme.typography.titleLarge)
                
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Release Year/Date") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = publisher,
                    onValueChange = { publisher = it },
                    label = { Text("Publisher") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = developer,
                    onValueChange = { developer = it },
                    label = { Text("Developer") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { onSave(date, publisher, developer) }) {
                        Text("Save")
                    }
                }
            }
        }
    }
}
