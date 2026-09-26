package com.swordfish.lemuroid.app.mobile.feature.gameinfo

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.LemuroidGameImage
import com.swordfish.lemuroid.common.kotlin.cleanGameTitle
import com.swordfish.lemuroid.lib.library.db.entity.Game
import java.text.DateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun GameInfoScreen(
    viewModel: GameInfoViewModel,
    onPlay: (Game) -> Unit,
    onRestart: (Game) -> Unit,
    onCheats: (Game) -> Unit = {},
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {}
) {
    val game = viewModel.game.collectAsState().value
    val isRescanning = viewModel.isRescanning.collectAsState().value
    val pendingMetadata = viewModel.pendingMetadata.collectAsState().value
    var showEditScreen by remember { mutableStateOf(false) }

    if (game == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (showEditScreen) {
        EditDetailsScreen(
            game = game,
            onDismiss = { showEditScreen = false },
            onSave = { title, date, pub, dev, reg, ver ->
                viewModel.updateGameDetails(title, date, pub, dev, reg, ver)
                showEditScreen = false
            },
            onThumbnailSelected = { uri, deleteSource ->
                viewModel.saveLocalThumbnail(uri, deleteSource)
            }
        )
    } else if (pendingMetadata != null) {
        ScrapeResultScreen(
            game = game,
            metadata = pendingMetadata,
            onDismiss = { viewModel.clearPendingMetadata() },
            onAccept = { title, releaseDate, publisher, developer, region, coverFrontUrl, coverBackUrl, cartridgeUrl, manualUrl ->
                viewModel.applyCustomScrapedMetadata(
                    title = title,
                    releaseDate = releaseDate,
                    publisher = publisher,
                    developer = developer,
                    region = region,
                    coverFrontUrl = coverFrontUrl,
                    coverBackUrl = coverBackUrl,
                    cartridgeUrl = cartridgeUrl,
                    manualUrl = manualUrl
                )
            }
        )
    } else {
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
                    // Box Art Swiper (Front/Back)
                    BoxArtSwiper(
                        frontImageUrl = game.coverFrontUrl,
                        backImageUrl = game.coverBackUrl,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    val region = game.country ?: ""
                    
                    if (region.isNotEmpty()) {
                        Text(
                            text = region,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                item {
                    val formattedDate = formatLocalizedDate(game.releaseDate)
                    val publisher = game.publisher ?: ""
                    val developer = game.developer ?: ""
                    val metaText = listOfNotNull(
                        formattedDate.takeIf { it.isNotEmpty() },
                        publisher.takeIf { it.isNotEmpty() },
                        developer.takeIf { it.isNotEmpty() }
                    ).joinToString(" | ")
                    
                    if (metaText.isNotEmpty()) {
                        Text(
                            text = metaText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { showEditScreen = true },
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
                        OutlinedButton(
                            onClick = { onCheats(game) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Star, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Cheats")
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }

            // --- BOTTOM ACTIONS ---
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onPlay(game) },
                        modifier = Modifier.weight(1.5f).height(56.dp),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Resume", fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = { onRestart(game) },
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Icon(Icons.Default.RestartAlt, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Restart")
                    }
                }
                Button(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                ) {
                    Icon(Icons.Default.Close, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Close")
                }
            }
        }
    }
}

private fun formatLocalizedDate(dateString: String?): String {
    if (dateString.isNullOrBlank()) return ""
    return runCatching {
        val cleanDate = dateString.replace("-", "").trim()
        if (cleanDate.length >= 8) {
            val year = cleanDate.substring(0, 4).toInt()
            val month = cleanDate.substring(4, 6).toInt() - 1
            val day = cleanDate.substring(6, 8).toInt()
            val cal = Calendar.getInstance().apply {
                set(year, month, day)
            }
            DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault()).format(cal.time)
        } else {
            dateString
        }
    }.getOrDefault(dateString)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditDetailsScreen(
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Game Details") },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = { onSave(title, date, publisher, developer, region, version) }) {
                        Text("Save", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { imagePicker.launch("image/*") }
                        .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.medium)
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(80.dp)) {
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
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Change Box Art", style = MaterialTheme.typography.titleMedium)
                        Text("Tap to select image", style = MaterialTheme.typography.bodySmall)
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = deleteSource, onCheckedChange = { deleteSource = it })
                            Text("Delete original after copy", style = MaterialTheme.typography.labelSmall)
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
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
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
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = { onSave(title, date, publisher, developer, region, version) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Save Changes")
                }
            }
        }
    }
}
