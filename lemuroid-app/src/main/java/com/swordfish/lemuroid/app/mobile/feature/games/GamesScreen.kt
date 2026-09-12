package com.swordfish.lemuroid.app.mobile.feature.games

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.LemuroidEmptyView
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.LemuroidGameImage
import com.swordfish.lemuroid.common.kotlin.cleanGameTitle
import com.swordfish.lemuroid.lib.library.db.entity.Game

@Composable
fun GamesScreen(
    modifier: Modifier = Modifier,
    viewModel: GamesViewModel,
    onGameClick: (Game) -> Unit,
    onGameLongClick: (Game) -> Unit,
    @Suppress("UNUSED_PARAMETER") onGameFavoriteToggle: (Game, Boolean) -> Unit,
) {
    val games by viewModel.games.collectAsState()
    val query by viewModel.searchQuery.collectAsState()
    val showAll by viewModel.showAllSystems.collectAsState()
    val sortMode by viewModel.sortMode.collectAsState()

    Column(modifier = modifier.fillMaxSize()) {
        // --- TOP BAR: SEARCH & FILTERS ---
        Surface(
            tonalElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search Title or Metadata...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = if (query.isNotEmpty()) {
                        {
                            IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                Icon(Icons.Default.Close, contentDescription = null)
                            }
                        }
                    } else null,
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // System Toggle
                    FilterChip(
                        selected = !showAll,
                        onClick = { viewModel.toggleShowAllSystems(false) },
                        label = { Text(viewModel.currentMetaSystem.name) }
                    )
                    
                    FilterChip(
                        selected = showAll,
                        onClick = { viewModel.toggleShowAllSystems(true) },
                        label = { Text("All") }
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    // Sort Modes
                    IconButton(
                        onClick = { viewModel.updateSortMode(GamesViewModel.SortMode.RECENTS) },
                        colors = if (sortMode == GamesViewModel.SortMode.RECENTS) 
                            IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.primary) 
                            else IconButtonDefaults.iconButtonColors()
                    ) {
                        Icon(Icons.Default.Update, contentDescription = "Recents")
                    }
                    
                    IconButton(
                        onClick = { viewModel.updateSortMode(GamesViewModel.SortMode.ALPHABETICAL) },
                        colors = if (sortMode == GamesViewModel.SortMode.ALPHABETICAL) 
                            IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.primary) 
                            else IconButtonDefaults.iconButtonColors()
                    ) {
                        Icon(Icons.Default.SortByAlpha, contentDescription = "A-Z")
                    }

                    IconButton(
                        onClick = { viewModel.updateSortMode(GamesViewModel.SortMode.RELEASE) },
                        colors = if (sortMode == GamesViewModel.SortMode.RELEASE) 
                            IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.primary) 
                            else IconButtonDefaults.iconButtonColors()
                    ) {
                        Icon(Icons.Default.CalendarToday, contentDescription = "Release Date")
                    }
                }
            }
        }

        if (games.isEmpty()) {
            LemuroidEmptyView()
        } else {
            LazyVerticalGrid(
                modifier = Modifier.fillMaxSize(),
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(games, key = { it.id }) { game ->
                    GameGridItem(
                        game = game,
                        onClick = { onGameClick(game) },
                        onLongClick = { onGameLongClick(game) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GameGridItem(
    game: Game,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val isPsp = game.systemId.lowercase() == "psp"
        
        // Container for cover and badge
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
            contentAlignment = Alignment.Center
        ) {
            // Use a nested box to wrap the image content for better badge alignment
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(if (!isPsp) Modifier.background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.medium) else Modifier),
                contentAlignment = Alignment.Center
            ) {
                LemuroidGameImage(
                    game = game,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(MaterialTheme.shapes.medium),
                    applyAspectRatio = false,
                    contentScale = ContentScale.Fit
                )
                
                // System Badge - Over the cover
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f),
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(if (isPsp) 8.dp else 4.dp)
                ) {
                    Text(
                        text = game.systemId.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        val cleanedTitle = game.title.cleanGameTitle()
        val titleParts = cleanedTitle.split(" - ", limit = 2).map { it.trim() }

        Text(
            text = titleParts[0],
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 8.dp),
            textAlign = TextAlign.Center
        )
        
        val subtitle = if (titleParts.size > 1) titleParts[1] else ""
        val year = game.releaseDate?.take(4) ?: ""
        val details = listOfNotNull(
            subtitle.takeIf { it.isNotEmpty() },
            year.takeIf { it.isNotEmpty() }
        ).joinToString(" | ")

        if (details.isNotEmpty()) {
            Text(
                text = details,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}
