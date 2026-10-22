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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.mobile.feature.home.onboarding.EmptyLibraryCard
import com.swordfish.lemuroid.app.mobile.feature.home.onboarding.GameGrlTitleHeader
import com.swordfish.lemuroid.app.mobile.feature.home.onboarding.HomeNotification
import com.swordfish.lemuroid.app.mobile.feature.home.onboarding.LibraryOnboardingScreen
import com.swordfish.lemuroid.app.mobile.feature.home.onboarding.LibraryScanScreen
import com.swordfish.lemuroid.app.mobile.feature.home.onboarding.ScanProgressState
import com.swordfish.lemuroid.app.mobile.shared.DefaultBackground
import com.swordfish.lemuroid.app.mobile.shared.LoadingScreen
import com.swordfish.lemuroid.app.mobile.shared.RetroText
import com.swordfish.lemuroid.app.mobile.shared.pressStart2PFontFamily
import com.swordfish.lemuroid.app.utils.android.ComposableLifecycle
import com.swordfish.lemuroid.common.displayDetailsSettingsScreen
import com.swordfish.lemuroid.lib.library.db.entity.Game

// Explicit Onboarding/Scan Flow State Machine
private enum class HomeFlowStage {
    HOME_DEFAULT,
    ONBOARDING,
    SCANNING
}

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
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    var currentStage by remember { mutableStateOf(HomeFlowStage.HOME_DEFAULT) }
    var cheatsEnabled by remember { mutableStateOf(true) }
    var trophiesEnabled by remember { mutableStateOf(true) }

    // Automatically transition from SCANNING to HOME_DEFAULT when scan finishes and games are loaded
    LaunchedEffect(state.indexInProgress, state.systemLibraries.isNotEmpty()) {
        if (!state.indexInProgress && state.systemLibraries.isNotEmpty() && currentStage == HomeFlowStage.SCANNING) {
            // Scan finished, user clicked start playing in scan screen
        }
    }

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

    // ==========================================
    // STAGE 2: ONBOARDING SCREEN
    // ==========================================
    if (currentStage == HomeFlowStage.ONBOARDING) {
        LibraryOnboardingScreen(
            indexInProgress = state.indexInProgress,
            trophiesEnabled = trophiesEnabled,
            onSelectDirectory = {
                // Trigger system directory picker and advance to Scanning Stage
                viewModel.prepareForNewScan()
                viewModel.changeLocalStorageFolder(context)
                currentStage = HomeFlowStage.SCANNING
            }
        )
        return
    }

    // ==========================================
    // STAGE 3 & 4: SCANNING PROGRESS / SCAN COMPLETE SCREEN
    // ==========================================
    if (currentStage == HomeFlowStage.SCANNING) {
        LibraryScanScreen(
            state = ScanProgressState(
                isComplete = !state.indexInProgress && state.systemLibraries.isNotEmpty(),
                gamesFound = state.scannedGamesCount, // Wire to your ViewModel stats
                boxArtFound = state.scannedBoxArtCount,
                cartridgesFound = state.scannedCartridgesCount,
                manualsFound = state.scannedManualsCount,
                recentArtworks = state.recentScannedArtworks
            ),
            onStartPlaying = {
                // Exit scanning flow back to main Home Carousel
                currentStage = HomeFlowStage.HOME_DEFAULT
            }
        )
        return
    }

    // ==========================================
    // STAGE 1: DEFAULT HOME SCREEN (EMPTY CARD / CAROUSEL)
    // ==========================================
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        val hasTopBanner = state.showNoNotificationPermissionCard ||
                state.showNoMicrophonePermissionCard ||
                state.showNoGamesCard

        if (hasTopBanner) {
            Box(modifier = Modifier.fillMaxWidth()) {
                DefaultBackground(modifier = Modifier.fillMaxSize())

                Box(modifier = Modifier.statusBarsPadding()) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        GameGrlTitleHeader()

                        Box(
                            modifier = Modifier.padding(horizontal = 20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "HEY! JUST A COUPLE OF THINGS AND WE CAN START PLAYING.",
                                fontFamily = pressStart2PFontFamily,
                                fontSize = 8.sp,
                                color = RetroText,
                                textAlign = TextAlign.Center
                            )
                        }

                        // 1. EMPTY LIBRARY CARD -> Tapping triggers STAGE 2 (ONBOARDING)
                        EmptyLibraryCard(
                            indexInProgress = state.indexInProgress,
                            onSelectDirectory = { currentStage = HomeFlowStage.ONBOARDING },
                            cheatsEnabled = cheatsEnabled,
                            onCheatsToggled = { cheatsEnabled = it },
                            trophiesEnabled = trophiesEnabled,
                            onTrophiesToggled = { trophiesEnabled = it }
                        )

                        if (state.showNoNotificationPermissionCard || state.showNoMicrophonePermissionCard) {
                            Box(
                                modifier = Modifier.padding(horizontal = 20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "SIDE QUESTS",
                                    fontFamily = pressStart2PFontFamily,
                                    fontSize = 8.sp,
                                    color = RetroText,
                                )
                            }
                        }

                        // 2. NOTIFICATION PERMISSION CARD
                        if (state.showNoNotificationPermissionCard) {
                            HomeNotification(
                                imageId = R.drawable.game_grl_launcher_round,
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

                        // 3. MICROPHONE PERMISSION CARD
                        if (state.showNoMicrophonePermissionCard) {
                            HomeNotification(
                                imageId = R.drawable.game_grl_mic,
                                titleId = R.string.home_microphone_title,
                                messageId = R.string.home_microphone_message,
                                actionId = R.string.home_microphone_action,
                                onAction = { permissionsLauncher.launch(Manifest.permission.RECORD_AUDIO) }
                            )
                        }
                    }
                }
            }
        }

        // Main Stage Carousel or Loading Fallback
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
