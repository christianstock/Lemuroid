package com.swordfish.lemuroid.app.mobile.feature.home

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.mobile.shared.DefaultBackground
import com.swordfish.lemuroid.app.mobile.shared.LoadingScreen
import com.swordfish.lemuroid.app.mobile.shared.RetroBorderLight
import com.swordfish.lemuroid.app.mobile.shared.RetroGold
import com.swordfish.lemuroid.app.mobile.shared.RetroLime
import com.swordfish.lemuroid.app.mobile.shared.RetroLimeEdge
import com.swordfish.lemuroid.app.mobile.shared.RetroMagenta
import com.swordfish.lemuroid.app.mobile.shared.RetroPanel
import com.swordfish.lemuroid.app.mobile.shared.RetroText
import com.swordfish.lemuroid.app.mobile.shared.RetroViolet
import com.swordfish.lemuroid.app.mobile.shared.pressStart2PFontFamily
import com.swordfish.lemuroid.app.shared.cheats.ui.RetroBottomActionButton
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

    // Collect state safely from ViewModel
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Local mutable states for Extras checkboxes (or persist to ViewModel/Preferences as needed)
    var cheatsEnabled by remember { mutableStateOf(true) }
    var trophiesEnabled by remember { mutableStateOf(true) }

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
        val hasTopBanner = state.showNoNotificationPermissionCard ||
                state.showNoMicrophonePermissionCard ||
                !state.showNoGamesCard // Preserved for testing library card overlay

        if (hasTopBanner) {
            Box(modifier = Modifier.fillMaxWidth()) {
                DefaultBackground(
                    modifier = Modifier.fillMaxSize()
                )
                Box(modifier = Modifier.statusBarsPadding()) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        // GAME GRL Retro Header with Pixel Shadow
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "GAME GRL",
                                fontFamily = pressStart2PFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = RetroMagenta,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.offset(x = 3.dp, y = 3.dp)
                            )
                            Text(
                                text = "GAME GRL",
                                fontFamily = pressStart2PFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = RetroLime,
                                textAlign = TextAlign.Center
                            )
                        }

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

                        // 1. EMPTY LIBRARY CARD (Shows First)
                        EmptyLibraryCard(
                            indexInProgress = state.indexInProgress,
                            onSelectDirectory = { viewModel.changeLocalStorageFolder(context) },
                            cheatsEnabled = cheatsEnabled,
                            onCheatsToggled = { cheatsEnabled = it },
                            trophiesEnabled = trophiesEnabled,
                            onTrophiesToggled = { trophiesEnabled = it }
                        )

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

                        // 2. NOTIFICATION PERMISSION CARD (Shows Second)
                        if (!state.showNoNotificationPermissionCard) {
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

                        // 3. MICROPHONE PERMISSION CARD (Shows Third)
                        if (!state.showNoMicrophonePermissionCard) {
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

        // Main Stage Carousel or Loading Screen
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
    modifier: Modifier = Modifier,
    cheatsEnabled: Boolean = true,
    onCheatsToggled: (Boolean) -> Unit = {},
    trophiesEnabled: Boolean = true,
    onTrophiesToggled: (Boolean) -> Unit = {}
) {
    val condensedFontFamily = remember {
        FontFamily(android.graphics.Typeface.create("sans-serif-condensed", android.graphics.Typeface.NORMAL))
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(RetroPanel)
                .drawWithContent {
                    drawContent()
                    val stroke = 2.dp.toPx()
                    val cornerRadius = 12.dp.toPx()
                    drawRoundRect(
                        color = RetroBorderLight,
                        topLeft = Offset(stroke / 2f, stroke / 2f),
                        size = Size(size.width - stroke, size.height - stroke),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius, cornerRadius),
                        style = Stroke(width = stroke)
                    )
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 180.dp, top = 16.dp, end = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "MAIN QUEST",
                    fontFamily = pressStart2PFontFamily,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = RetroLime
                )

                Text(
                    text = stringResource(R.string.home_empty_title).uppercase(),
                    fontFamily = pressStart2PFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = stringResource(R.string.home_empty_message),
                    fontFamily = condensedFontFamily,
                    fontSize = 14.sp,
                    color = RetroText
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "EXTRAS",
                    fontFamily = pressStart2PFontFamily,
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Bold,
                    color = RetroText
                )

                // Checkbox Row 1: CHEATS (Entire row is clickable for easy tapping)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { onCheatsToggled(!cheatsEnabled) }
                        .padding(vertical = 2.dp)
                ) {
                    RetroCheckbox(
                        checked = cheatsEnabled,
                        color = RetroViolet
                    )
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = RetroViolet,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "CHEATS",
                        fontFamily = pressStart2PFontFamily,
                        fontSize = 9.sp,
                        color = RetroViolet
                    )
                }

                // Checkbox Row 2: TROPHIES (Entire row is clickable)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { onTrophiesToggled(!trophiesEnabled) }
                        .padding(vertical = 2.dp)
                ) {
                    RetroCheckbox(
                        checked = trophiesEnabled,
                        color = RetroGold
                    )
                    Icon(
                        imageVector = Icons.Filled.EmojiEvents,
                        contentDescription = null,
                        tint = Color(0xFFFFB703),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "TROPHIES",
                        fontFamily = pressStart2PFontFamily,
                        fontSize = 9.sp,
                        color = Color(0xFFFFB703)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                RetroBottomActionButton(
                    label = if (indexInProgress) "SCANNING..." else stringResource(R.string.home_empty_action).uppercase(),
                    onClick = onSelectDirectory,
                    isLoading = indexInProgress,
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = RetroLime,
                    borderColor = RetroText,
                    edgeColor = RetroLimeEdge,
                    textColor = RetroPanel
                )
            }
        }

        Image(
            painter = painterResource(id = R.drawable.game_grl_casual),
            contentDescription = "Game GRL",
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = 20.dp, y = (-80).dp)
                .height(235.dp)
                .graphicsLayer(
                    scaleX = 1.5f,
                    scaleY = 1.5f
                )
        )
    }
}

@Composable
private fun RetroCheckbox(
    checked: Boolean,
    modifier: Modifier = Modifier,
    color: Color
) {
    Box(
        modifier = modifier
            .size(20.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(if (checked) color else RetroPanel)
            .drawWithContent {
                drawContent()
                val stroke = 2.dp.toPx()
                drawRect(
                    color = color,
                    topLeft = Offset(stroke / 2f, stroke / 2f),
                    size = Size(size.width - stroke, size.height - stroke),
                    style = Stroke(width = stroke)
                )
            },
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            Text(
                text = "✓",
                fontFamily = pressStart2PFontFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.offset(y = (-4).dp)
            )
        }
    }
}
