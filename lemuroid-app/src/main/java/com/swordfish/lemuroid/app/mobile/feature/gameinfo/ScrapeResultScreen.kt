package com.swordfish.lemuroid.app.mobile.feature.gameinfo

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.swordfish.lemuroid.lib.library.db.entity.Game
import com.swordfish.lemuroid.lib.library.metadata.GameMetadata

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScrapeResultScreen(
    game: Game,
    metadata: GameMetadata,
    onDismiss: () -> Unit,
    onAccept: (title: String, releaseDate: String?, publisher: String?, developer: String?, region: String?, summary: String?, coverUrl: String?) -> Unit
) {
    var title by remember { mutableStateOf(metadata.name ?: game.title) }
    var releaseDate by remember { mutableStateOf(metadata.releaseDate ?: game.releaseDate ?: "") }
    var publisher by remember { mutableStateOf(metadata.publisher ?: game.publisher ?: "") }
    var developer by remember { mutableStateOf(metadata.developer ?: game.developer ?: "") }
    var region by remember { mutableStateOf(metadata.country ?: game.country ?: "") }
    var summary by remember { mutableStateOf(metadata.summary ?: game.summary ?: "") }
    var selectedCoverUrl by remember { mutableStateOf(metadata.thumbnail ?: game.coverFrontUrl) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scraped Metadata Results") },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Cancel")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            onAccept(
                                title,
                                releaseDate.ifBlank { null },
                                publisher.ifBlank { null },
                                developer.ifBlank { null },
                                region.ifBlank { null },
                                summary.ifBlank { null },
                                selectedCoverUrl
                            )
                        }
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Accept", fontWeight = FontWeight.Bold)
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
                Text(
                    text = "Review and edit the scraped metadata below. You can pick values or freely edit any field.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // --- COVER ART COMPARISON ---
            if (metadata.thumbnail != null || game.coverFrontUrl != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Box Art", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                game.coverFrontUrl?.let { existingArt ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .clip(MaterialTheme.shapes.medium)
                                            .background(
                                                if (selectedCoverUrl == existingArt) MaterialTheme.colorScheme.primaryContainer
                                                else MaterialTheme.colorScheme.surface
                                            )
                                            .clickable { selectedCoverUrl = existingArt }
                                            .padding(4.dp)
                                    ) {
                                        AsyncImage(
                                            model = existingArt,
                                            contentDescription = "Existing Cover",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Fit
                                        )
                                        Text(
                                            "Existing",
                                            style = MaterialTheme.typography.labelSmall,
                                            modifier = Modifier
                                                .align(Alignment.BottomCenter)
                                                .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.6f))
                                                .padding(horizontal = 4.dp),
                                            color = androidx.compose.ui.graphics.Color.White
                                        )
                                    }
                                }

                                metadata.thumbnail?.let { scrapedArt ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .clip(MaterialTheme.shapes.medium)
                                            .background(
                                                if (selectedCoverUrl == scrapedArt) MaterialTheme.colorScheme.primaryContainer
                                                else MaterialTheme.colorScheme.surface
                                            )
                                            .clickable { selectedCoverUrl = scrapedArt }
                                            .padding(4.dp)
                                    ) {
                                        AsyncImage(
                                            model = scrapedArt,
                                            contentDescription = "Scraped Cover",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Fit
                                        )
                                        Text(
                                            "Scraped",
                                            style = MaterialTheme.typography.labelSmall,
                                            modifier = Modifier
                                                .align(Alignment.BottomCenter)
                                                .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.6f))
                                                .padding(horizontal = 4.dp),
                                            color = androidx.compose.ui.graphics.Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // --- TITLE ---
            item {
                ScrapeFieldEditor(
                    label = "Title",
                    existingValue = game.title,
                    scrapedValue = metadata.name,
                    currentValue = title,
                    onValueChange = { title = it }
                )
            }

            // --- RELEASE DATE ---
            item {
                ScrapeFieldEditor(
                    label = "Release Date",
                    existingValue = game.releaseDate,
                    scrapedValue = metadata.releaseDate,
                    currentValue = releaseDate,
                    onValueChange = { releaseDate = it }
                )
            }

            // --- PUBLISHER ---
            item {
                ScrapeFieldEditor(
                    label = "Publisher",
                    existingValue = game.publisher,
                    scrapedValue = metadata.publisher,
                    currentValue = publisher,
                    onValueChange = { publisher = it }
                )
            }

            // --- DEVELOPER ---
            item {
                ScrapeFieldEditor(
                    label = "Developer",
                    existingValue = game.developer,
                    scrapedValue = metadata.developer,
                    currentValue = developer,
                    onValueChange = { developer = it }
                )
            }

            // --- REGION / COUNTRY ---
            item {
                ScrapeFieldEditor(
                    label = "Region",
                    existingValue = game.country,
                    scrapedValue = metadata.country,
                    currentValue = region,
                    onValueChange = { region = it }
                )
            }

            // --- SUMMARY / VERSION ---
            item {
                ScrapeFieldEditor(
                    label = "Version / Summary",
                    existingValue = game.summary,
                    scrapedValue = metadata.summary,
                    currentValue = summary,
                    onValueChange = { summary = it }
                )
            }

            // --- BOTTOM ACTIONS ---
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(50.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            onAccept(
                                title,
                                releaseDate.ifBlank { null },
                                publisher.ifBlank { null },
                                developer.ifBlank { null },
                                region.ifBlank { null },
                                summary.ifBlank { null },
                                selectedCoverUrl
                            )
                        },
                        modifier = Modifier.weight(1f).height(50.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Accept")
                    }
                }
            }
        }
    }
}

@Composable
private fun ScrapeFieldEditor(
    label: String,
    existingValue: String?,
    scrapedValue: String?,
    currentValue: String,
    onValueChange: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(label, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

        val existing = existingValue?.takeIf { it.isNotBlank() }
        val scraped = scrapedValue?.takeIf { it.isNotBlank() }

        if (existing != null || scraped != null) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (existing != null) {
                    FilterChip(
                        selected = currentValue == existing,
                        onClick = { onValueChange(existing) },
                        label = { Text("Existing: $existing") }
                    )
                }
                if (scraped != null && scraped != existing) {
                    FilterChip(
                        selected = currentValue == scraped,
                        onClick = { onValueChange(scraped) },
                        label = { Text("Scraped: $scraped") }
                    )
                }
            }
        }

        OutlinedTextField(
            value = currentValue,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
    }
}

private fun String?.isNull_or_Empty(): Boolean = this.isNullOrEmpty()
