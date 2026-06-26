package com.swordfish.lemuroid.app.shared.cheats.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.StateFlow

data class SystemScanProgress(
    val systemName: String,
    val gamesFound: Int,
    val isCurrentlyScanning: Boolean = false,
    val isComplete: Boolean = false
)

@Composable
fun CheatProgressScreen(
    systemProgressList: StateFlow<List<SystemScanProgress>>,
    totalGamesFound: StateFlow<Int>,
    isScanningComplete: StateFlow<Boolean>,
    modifier: Modifier = Modifier,
) {
    val progressList = systemProgressList.collectAsState().value
    val totalGames = totalGamesFound.collectAsState().value
    val scanComplete = isScanningComplete.collectAsState().value

    CheatProgressScreenContent(
        progressList = progressList,
        totalGames = totalGames,
        scanComplete = scanComplete,
        modifier = modifier
    )
}

@Composable
fun CheatProgressScreen(
    systemProgress: List<SystemScanProgress>,
    totalCheatsFound: Int,
    isComplete: Boolean,
    onDismiss: () -> Unit = { },
    modifier: Modifier = Modifier,
) {
    CheatProgressScreenContent(
        progressList = systemProgress,
        totalGames = totalCheatsFound,
        scanComplete = isComplete,
        modifier = modifier
    )
}

@Composable
private fun CheatProgressScreenContent(
    progressList: List<SystemScanProgress>,
    totalGames: Int,
    scanComplete: Boolean,
    modifier: Modifier = Modifier,
) {
    val currentSystem = progressList.find { it.isCurrentlyScanning }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp)
    ) {
        // Title
        Text(
            text = "Cheat Database Scan Progress",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Current system being scanned - Tab style
        if (currentSystem != null && !scanComplete) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                    )
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Currently Scanning",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = currentSystem.systemName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.CenterVertically),
                        strokeWidth = 2.dp,
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Overall progress
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = if (scanComplete) "Total Cheats Found" else "Cheats Found So Far",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Text(
                    text = "$totalGames",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (!scanComplete) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterVertically),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // System-by-system breakdown
        Text(
            text = "System Progress",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(progressList) { systemProgress ->
                SystemProgressRow(systemProgress)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Completion message with per-system breakdown
        if (scanComplete) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(
                    text = "Scan complete:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Per-system summary
                val systemSummary = progressList
                    .filter { it.gamesFound > 0 }
                    .sortedBy { it.systemName }

                systemSummary.forEach { system ->
                    Text(
                        text = "${system.systemName} - ${system.gamesFound} game${if (system.gamesFound != 1) "s" else ""}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SystemProgressRow(
    progress: SystemScanProgress,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = if (progress.isCurrentlyScanning) {
                    MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                } else {
                    MaterialTheme.colorScheme.surface
                },
                shape = RoundedCornerShape(8.dp)
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = progress.systemName,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${progress.gamesFound} games",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }

        if (progress.isCurrentlyScanning) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.CenterVertically),
                strokeWidth = 2.dp,
            )
        } else if (progress.isComplete) {
            Text(
                text = "✓",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Green
            )
        }
    }
}

/**
 * Integration Guide:
 *
 * To use this progress screen, your CheatManager or LibraryIndexWork should expose StateFlows:
 *
 * ```kotlin
 * // In your scan/load logic:
 * private val _systemProgress = MutableStateFlow<List<SystemScanProgress>>(emptyList())
 * val systemProgress: StateFlow<List<SystemScanProgress>> = _systemProgress.asStateFlow()
 *
 * private val _totalGamesFound = MutableStateFlow(0)
 * val totalGamesFound: StateFlow<Int> = _totalGamesFound.asStateFlow()
 *
 * private val _scanComplete = MutableStateFlow(false)
 * val scanComplete: StateFlow<Boolean> = _scanComplete.asStateFlow()
 *
 * // During scanning:
 * fun scanCheatsForSystem(system: GameSystem) {
 *     _systemProgress.value += SystemScanProgress(
 *         systemName = system.name,
 *         gamesFound = 0,
 *         isCurrentlyScanning = true
 *     )
 *
 *     // Load cheats...
 *     val count = cheats.size
 *
 *     _systemProgress.value = _systemProgress.value.map {
 *         if (it.systemName == system.name) {
 *             it.copy(gamesFound = count, isCurrentlyScanning = false, isComplete = true)
 *         } else it
 *     }
 *
 *     _totalGamesFound.value += count
 * }
 *
 * // At end of all scans:
 * _scanComplete.value = true
 * ```
 */




