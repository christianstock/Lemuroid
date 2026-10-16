package com.swordfish.lemuroid.app.mobile.feature.home

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.mobile.shared.LoadingScreen
import com.swordfish.lemuroid.app.utils.android.ComposableLifecycle
import com.swordfish.lemuroid.common.displayDetailsSettingsScreen
import com.swordfish.lemuroid.lib.library.db.entity.Game

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel,
    onGameClick: (Game) -> Unit,
    onGameLongClick: (Game) -> Unit,
    onNavigateToSystemList: (Game) -> Unit,
    onOpenSettings: () -> Unit = {},
) {
    val context = LocalContext.current

    // Collect directly from the read-only StateFlow for optimal lifecycle safety
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    ComposableLifecycle { _, event ->
        if (event == Lifecycle.Event.ON_RESUME) {
            viewModel.updatePermissions(context.applicationContext)
        }
    }

    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            context.displayDetailsSettingsScreen()
        } else {
            viewModel.updatePermissions(context.applicationContext)
        }
    }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // 1. Conditionally render the top wrapper ONLY when top banners/cards exist
        val hasTopBanner = state.showNoNotificationPermissionCard ||
                state.showNoMicrophonePermissionCard ||
                state.showNoGamesCard

        if (hasTopBanner) {
            Box(modifier = modifier.statusBarsPadding()) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    when {
                        state.showNoNotificationPermissionCard -> {
                            HomeNotification(
                                titleId = R.string.home_notification_title,
                                messageId = R.string.home_notification_message,
                                actionId = R.string.home_notification_action,
                                onAction = {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        permissionsLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                }
                            )
                        }

                        state.showNoMicrophonePermissionCard -> {
                            HomeNotification(
                                titleId = R.string.home_microphone_title,
                                messageId = R.string.home_notification_message,
                                actionId = R.string.home_notification_action,
                                onAction = { permissionsLauncher.launch(Manifest.permission.RECORD_AUDIO) }
                            )
                        }
                    }

                    if (state.showNoGamesCard) {
                        EmptyLibraryCard(
                            indexInProgress = state.indexInProgress,
                            onSelectDirectory = { viewModel.changeLocalStorageFolder(context) }
                        )
                    }
                }
            }
        }

        // 2. Main Stage Carousel or Loading Screen (Pads nothing, fills screen edge-to-edge when cards are gone)
        if (state.systemLibraries.isNotEmpty()) {
            HomeCarousel(
                systemLibraries = state.systemLibraries,
                selectedSystemId = state.selectedSystemId,
                systemScrollPositions = state.systemScrollPositions,
                refreshCount = state.refreshCount,
                onGameClick = { game ->
                    viewModel.onGameLaunched(game)
                    onGameClick(game)
                },
                onShowContextMenu = onGameLongClick,
                onNavigateToList = onNavigateToSystemList,
                onSystemSelected = viewModel::setSelectedSystem,
                onSystemScroll = viewModel::setSystemScrollPosition,
                onOpenSettings = onOpenSettings,
                modifier = Modifier.weight(1f)
            )
        } else if (!state.showNoGamesCard) {
            LoadingScreen(
                text = "LOADING...",
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun EmptyLibraryCard(
    indexInProgress: Boolean,
    onSelectDirectory: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.home_empty_title),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = stringResource(R.string.home_empty_message),
                style = MaterialTheme.typography.bodyMedium
            )
            OutlinedButton(
                modifier = Modifier.align(Alignment.End),
                onClick = onSelectDirectory,
                enabled = !indexInProgress
            ) {
                Text(
                    text = if (indexInProgress) "Scanning..." else stringResource(R.string.home_empty_action)
                )
            }
        }
    }
}

@Composable
private fun HomeNotification(
    titleId: Int,
    messageId: Int,
    actionId: Int,
    enabled: Boolean = true,
    onAction: () -> Unit = {}
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(titleId),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = stringResource(messageId),
                style = MaterialTheme.typography.bodyMedium
            )
            OutlinedButton(
                modifier = Modifier.align(Alignment.End),
                onClick = onAction,
                enabled = enabled
            ) {
                Text(stringResource(id = actionId))
            }
        }
    }
}
