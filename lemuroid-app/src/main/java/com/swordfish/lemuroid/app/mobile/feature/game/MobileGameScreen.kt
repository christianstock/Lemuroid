package com.swordfish.lemuroid.app.mobile.feature.game

import android.graphics.RectF
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.RotateLeft
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import android.view.KeyEvent
import com.swordfish.lemuroid.app.shared.game.ui.InteractiveTopBar
import gg.padkit.inputevents.InputEvent
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Density
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.launch
import com.swordfish.lemuroid.app.shared.game.BaseGameScreenViewModel
import com.swordfish.lemuroid.app.shared.game.PhysicalScreenSizeCalculator
import com.swordfish.lemuroid.app.shared.game.skins.GbcSkin
import com.swordfish.lemuroid.app.shared.game.skins.GbcSkinManager
import com.swordfish.lemuroid.app.shared.game.skins.ui.GbcLandscapeSkin
import com.swordfish.lemuroid.app.shared.game.skins.ui.GbcPortraitSkin
import com.swordfish.lemuroid.app.shared.game.viewmodel.GameViewModelTouchControls.Companion.MENU_LOADING_ANIMATION_MILLIS
import com.swordfish.lemuroid.app.shared.settings.HapticFeedbackMode
import com.swordfish.lemuroid.lib.controller.ControllerConfig
import com.swordfish.lemuroid.lib.library.SystemID
import com.swordfish.touchinput.controller.R
import com.swordfish.touchinput.radial.LocalLemuroidPadTheme
import com.swordfish.touchinput.radial.getThemeForSystem
import com.swordfish.touchinput.radial.sensors.TiltConfiguration
import com.swordfish.touchinput.radial.settings.TouchControllerSettingsManager
import com.swordfish.touchinput.radial.ui.GlassSurface
import com.swordfish.touchinput.radial.ui.LemuroidButtonPressFeedback
import gg.padkit.PadKit
import gg.padkit.config.HapticFeedbackType
import gg.padkit.inputstate.InputState

@Composable
fun MobileGameScreen(viewModel: BaseGameScreenViewModel) {
    val coroutineScope = rememberCoroutineScope()
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isLandscape = constraints.maxWidth > constraints.maxHeight
        val density = LocalDensity.current
        val context = LocalContext.current

        LaunchedEffect(isLandscape) {
            val orientation =
                if (isLandscape) {
                    TouchControllerSettingsManager.Orientation.LANDSCAPE
                } else {
                    TouchControllerSettingsManager.Orientation.PORTRAIT
                }
            viewModel.onScreenOrientationChanged(orientation)
        }

        val controllerConfigState = viewModel.getTouchControllerConfig().collectAsState(null)
        val touchControlsVisibleState = viewModel.isTouchControllerVisible().collectAsState(false)
        val touchControllerSettingsState =
            viewModel
                .getTouchControlsSettings(LocalDensity.current, WindowInsets.displayCutout)
                .collectAsState(null)

        val touchControllerSettings = touchControllerSettingsState.value
        val currentControllerConfig = controllerConfigState.value

        val tiltConfiguration = viewModel.getTiltConfiguration().collectAsState(TiltConfiguration.Disabled)
        val tiltSimulatedStates = viewModel.getSimulatedTiltEvents().collectAsState(InputState())
        val tiltSimulatedControls = remember { derivedStateOf { tiltConfiguration.value.controlIds() } }

        val touchGamePads = currentControllerConfig?.getTouchControllerConfig()
        val leftGamePad = touchGamePads?.leftComposable
        val rightGamePad = touchGamePads?.rightComposable

        val hapticFeedbackMode =
            viewModel
                .getTouchHapticFeedbackMode()
                .collectAsState(HapticFeedbackMode.NONE)

        val isPlayingState = viewModel.getIsPlaying().collectAsState(true)
        val rewindAvailableState = viewModel.getRewindAvailable().collectAsState(false)
        val rewindProgressState = viewModel.getRewindProgress().collectAsState(0f)
        val menuPressedState = viewModel.isMenuPressed().collectAsState(false)

        val padHapticFeedback =
            when (hapticFeedbackMode.value) {
                HapticFeedbackMode.NONE -> HapticFeedbackType.NONE
                HapticFeedbackMode.PRESS -> HapticFeedbackType.PRESS
                HapticFeedbackMode.PRESS_RELEASE -> HapticFeedbackType.PRESS_RELEASE
            }

        PadKit(
            modifier = Modifier.fillMaxSize(),
            onInputEvents = { viewModel.handleVirtualInputEvent(it) },
            hapticFeedbackType = padHapticFeedback,
            simulatedState = tiltSimulatedStates,
            simulatedControlIds = tiltSimulatedControls,
        ) {
            val localContext = LocalContext.current
            val lifecycle = LocalLifecycleOwner.current

            val fullScreenPosition = remember { mutableStateOf<Rect?>(null) }
            val viewportPosition = remember { mutableStateOf<Rect?>(null) }

            // Root game view that fills the whole PadKit area
            AndroidView(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .onGloballyPositioned {
                            fullScreenPosition.value = it.boundsInRoot()
                        },
                factory = {
                    viewModel.createRetroView(localContext, lifecycle)
                },
            )

            val fullPos = fullScreenPosition.value
            val viewPos = viewportPosition.value

            LaunchedEffect(fullPos, viewPos) {
                val gameView = viewModel.retroGameView.retroGameViewFlow()
                if (fullPos == null || viewPos == null) return@LaunchedEffect

                // Calculate normalized viewport coordinates relative to full screen
                val viewport =
                    RectF(
                        (viewPos.left - fullPos.left) / fullPos.width,
                        (viewPos.top - fullPos.top) / fullPos.height,
                        (viewPos.right - fullPos.left) / fullPos.width,
                        (viewPos.bottom - fullPos.top) / fullPos.height,
                    )
                gameView.viewport = viewport
            }

            val isVisible =
                touchControllerSettings != null &&
                    currentControllerConfig != null &&
                    touchControlsVisibleState.value

            if (isVisible) {
                val gbcSkinManager = remember { GbcSkinManager.getInstance(context) }
                val currentSkin = gbcSkinManager.getSelectedSkinFlow().collectAsState(GbcSkin.LEMUROID_DEFAULT).value
                val theme = remember(viewModel.game.systemId, currentSkin) {
                    getThemeForSystem(
                        viewModel.game.systemId,
                        shellColor = if (viewModel.game.systemId == "gbc") currentSkin.caseColor else null,
                        buttonColor = if (viewModel.game.systemId == "gbc") currentSkin.buttonsColor else null
                    )
                }

                CompositionLocalProvider(LocalLemuroidPadTheme provides theme) {
                    val gameScreenContent: @Composable () -> Unit = {
                        // This placeholder box measures where the game should appear
                        GameViewWithPhysicalSizingPlaceholder(
                            viewModel = viewModel,
                            density = density,
                            context = context,
                            viewportPosition = viewportPosition
                        )
                    }

                    val interactiveBarContent: @Composable (Modifier) -> Unit = { mod ->
                        InteractiveTopBar(
                            isPlaying = isPlayingState.value,
                            isRewindAvailable = rewindAvailableState.value,
                            onSaveClick = { viewModel.saveQuickSave() },
                            onSaveLongClick = { viewModel.showSaveMenu() },
                            onLoadClick = { viewModel.loadQuickSave() },
                            onLoadLongClick = { viewModel.showLoadMenu() },
                            onRewindClick = { pressed ->
                                coroutineScope.launch {
                                    if (pressed) viewModel.startRewind() else viewModel.stopRewind()
                                }
                            },
                            onPauseToggle = { viewModel.togglePause() },
                            onMenuClick = { pressed ->
                                viewModel.handleVirtualInputEvent(
                                    listOf(InputEvent.Button(KeyEvent.KEYCODE_BUTTON_MODE, pressed)),
                                )
                            },
                            modifier = mod,
                            isMenuPressed = menuPressedState.value,
                        )
                    }

                    val overlaysContent: @Composable (Modifier) -> Unit = { mod ->
                        Box(modifier = mod) {
                            GameScreenRunningCentralMenu(
                                modifier = Modifier.align(Alignment.Center),
                                controllerConfig = currentControllerConfig,
                                touchControllerSettings = touchControllerSettings,
                                viewModel = viewModel,
                            )

                            if (!isPlayingState.value && rewindProgressState.value <= 0f) {
                                BlinkingPausedOverlay(
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            if (rewindProgressState.value > 0f) {
                                RewindProgressOverlay(
                                    progress = rewindProgressState.value,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }

                    if (viewModel.game.systemId == "gbc") {
                        if (isLandscape) {
                            GbcLandscapeSkin(
                                skin = currentSkin,
                                gameScreenContent = {
                                    Box(modifier = Modifier.fillMaxSize()) {
                                        gameScreenContent()
                                        overlaysContent(Modifier.matchParentSize())
                                    }
                                },
                                leftPad = { mod ->
                                    leftGamePad?.invoke(this, mod, touchControllerSettings)
                                },
                                rightPad = { mod ->
                                    rightGamePad?.invoke(this, mod, touchControllerSettings)
                                },
                                interactiveBar = {
                                    interactiveBarContent(Modifier.fillMaxWidth().height(84.dp))
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            GbcPortraitSkin(
                                skin = currentSkin,
                                gameScreenContent = {
                                    Box(modifier = Modifier.fillMaxSize()) {
                                        gameScreenContent()
                                        overlaysContent(Modifier.matchParentSize())
                                    }
                                },
                                leftPad = { mod ->
                                    leftGamePad?.invoke(this, mod, touchControllerSettings)
                                },
                                rightPad = { mod ->
                                    rightGamePad?.invoke(this, mod, touchControllerSettings)
                                },
                                interactiveBar = {
                                    interactiveBarContent(Modifier.fillMaxWidth().fillMaxHeight())
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    } else {
                        ConstraintLayout(
                            modifier = Modifier.fillMaxSize(),
                            constraintSet =
                                GameScreenLayout.buildConstraintSet(
                                    isLandscape,
                                    currentControllerConfig.allowTouchOverlay,
                                ),
                        ) {
                            Box(
                                modifier =
                                    Modifier
                                        .fillMaxSize()
                                        .layoutId(GameScreenLayout.CONSTRAINTS_GAME_VIEW)
                                        .windowInsetsPadding(WindowInsets.displayCutout.only(WindowInsetsSides.Top)),
                                contentAlignment = Alignment.Center
                            ) {
                                gameScreenContent()
                            }

                            if (!isLandscape) {
                                PadContainer(
                                    modifier = Modifier.layoutId(GameScreenLayout.CONSTRAINTS_BOTTOM_CONTAINER),
                                )
                            } else if (!currentControllerConfig.allowTouchOverlay) {
                                PadContainer(
                                    modifier = Modifier.layoutId(GameScreenLayout.CONSTRAINTS_LEFT_CONTAINER),
                                )
                                PadContainer(
                                    modifier = Modifier.layoutId(GameScreenLayout.CONSTRAINTS_RIGHT_CONTAINER),
                                )
                            }

                            leftGamePad?.invoke(
                                this,
                                Modifier.layoutId(GameScreenLayout.CONSTRAINTS_LEFT_PAD),
                                touchControllerSettings,
                            )
                            rightGamePad?.invoke(
                                this,
                                Modifier.layoutId(GameScreenLayout.CONSTRAINTS_RIGHT_PAD),
                                touchControllerSettings,
                            )

                            overlaysContent(Modifier.layoutId(GameScreenLayout.CONSTRAINTS_GAME_CONTAINER))

                            interactiveBarContent(Modifier.layoutId(GameScreenLayout.CONSTRAINTS_INTERACTIVE_BAR))
                        }
                    }
                }
            } else {
                // When UI is hidden, reset viewport to full screen
                LaunchedEffect(fullPos) {
                    viewportPosition.value = fullPos
                }
            }
        }

        val isLoading =
            viewModel.loadingState
                .collectAsState(true)
                .value

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
private fun GameViewWithPhysicalSizingPlaceholder(
    viewModel: BaseGameScreenViewModel,
    density: Density,
    context: android.content.Context,
    viewportPosition: androidx.compose.runtime.MutableState<Rect?>,
) {
    var slotSize by remember { mutableStateOf<IntSize?>(null) }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .onSizeChanged { slotSize = it },
        contentAlignment = Alignment.Center
    ) {
        val availableWidth = slotSize?.width?.toFloat() ?: 0f
        val availableHeight = slotSize?.height?.toFloat() ?: 0f

        val displayMetrics = context.resources.displayMetrics
        val systemIdEnum = remember<SystemID?>(viewModel.game.systemId) {
            SystemID.entries.find { it.dbname == viewModel.game.systemId }
        }

        val physicalDimensions = if (slotSize != null) {
            remember<PhysicalScreenSizeCalculator.ScreenDimensions?>(availableWidth, availableHeight, systemIdEnum) {
                systemIdEnum?.let {
                    PhysicalScreenSizeCalculator.calculateScreenDimensions(
                        systemId = it,
                        displayMetrics = displayMetrics,
                        maxAvailableWidthPx = availableWidth,
                        maxAvailableHeightPx = availableHeight,
                    )
                }
            }
        } else null

        val gameViewModifier = if (physicalDimensions != null) {
            Modifier.size(
                width = with(density) { physicalDimensions.widthPx.toDp() },
                height = with(density) { physicalDimensions.heightPx.toDp() }
            )
        } else {
            Modifier.fillMaxSize()
        }

        // Just a transparent box to measure the slot
        Box(
            modifier =
                gameViewModifier
                    .onGloballyPositioned {
                        val bounds = it.boundsInRoot()
                        viewportPosition.value = bounds
                    },
        )
    }
}


@Composable
private fun PadContainer(modifier: Modifier = Modifier) {
    val theme = LocalLemuroidPadTheme.current
    GlassSurface(
        modifier = modifier,
        cornerRadius = theme.level0CornerRadius,
        fillColor = theme.level0Fill,
        shadowColor = theme.level0Shadow,
        shadowWidth = theme.level0ShadowWidth,
    )
}

@Composable
private fun GameScreenRunningCentralMenu(
    modifier: Modifier = Modifier,
    viewModel: BaseGameScreenViewModel,
    touchControllerSettings: TouchControllerSettingsManager.Settings,
    controllerConfig: ControllerConfig,
) {
    val menuPressed = viewModel.isMenuPressed().collectAsState(false)
    Box(
        modifier = modifier.wrapContentSize(),
        contentAlignment = Alignment.Center,
    ) {
        LemuroidButtonPressFeedback(
            pressed = menuPressed.value,
            animationDurationMillis = MENU_LOADING_ANIMATION_MILLIS,
            icon = R.drawable.button_menu,
        )
        MenuEditTouchControls(viewModel, controllerConfig, touchControllerSettings)
    }
}

@Composable
private fun MenuEditTouchControls(
    viewModel: BaseGameScreenViewModel,
    controllerConfig: ControllerConfig,
    touchControllerSettings: TouchControllerSettingsManager.Settings,
) {
    val showEditControls = viewModel.isEditControlShown().collectAsState(false)
    if (!showEditControls.value) return

    Dialog(onDismissRequest = { viewModel.showEditControls(false) }) {
        Card(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                MenuEditTouchControlRow(Icons.Default.OpenInFull, "Scale", 0f) {
                    Slider(
                        value = touchControllerSettings.scale,
                        onValueChange = {
                            viewModel.updateTouchControllerSettings(
                                touchControllerSettings.copy(scale = it),
                            )
                        },
                    )
                }
                MenuEditTouchControlRow(Icons.Default.Height, "Horizontal Margin", 90f) {
                    Slider(
                        value = touchControllerSettings.marginX,
                        onValueChange = {
                            viewModel.updateTouchControllerSettings(
                                touchControllerSettings.copy(marginX = it),
                            )
                        },
                    )
                }
                MenuEditTouchControlRow(Icons.Default.Height, "Vertical Margin", 0f) {
                    Slider(
                        value = touchControllerSettings.marginY,
                        onValueChange = {
                            viewModel.updateTouchControllerSettings(
                                touchControllerSettings.copy(marginY = it),
                            )
                        },
                    )
                }
                if (controllerConfig.allowTouchRotation) {
                    MenuEditTouchControlRow(Icons.AutoMirrored.Filled.RotateLeft, "Rotate", 0f) {
                        Slider(
                            value = touchControllerSettings.rotation,
                            onValueChange = {
                                viewModel.updateTouchControllerSettings(
                                    touchControllerSettings.copy(rotation = it),
                                )
                            },
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    TextButton(
                        onClick = { viewModel.resetTouchControls() },
                        modifier = Modifier.padding(8.dp),
                    ) {
                        Text(text = stringResource(R.string.touch_customize_button_reset))
                    }
                    TextButton(
                        onClick = { viewModel.showEditControls(false) },
                        modifier = Modifier.padding(8.dp),
                    ) {
                        Text(text = stringResource(R.string.touch_customize_button_done))
                    }
                }
            }
        }
    }
}

@Composable
private fun MenuEditTouchControlRow(
    icon: ImageVector,
    label: String,
    rotation: Float,
    slider: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            modifier = Modifier.rotate(rotation),
            imageVector = icon,
            contentDescription = label,
        )
        slider()
    }
}

@Composable
private fun BlinkingPausedOverlay(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "pause_blink")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pause_alpha"
    )

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Simple shadow effect by layering
        Box(contentAlignment = Alignment.Center, modifier = Modifier.alpha(alpha)) {
            Text(
                text = "PAUSED",
                color = Color.Black,
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(top = 2.dp, start = 2.dp)
            )
            Text(
                text = "PAUSED",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
private fun RewindProgressOverlay(
    progress: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
     ) {
         Box(contentAlignment = Alignment.Center) {
             val text = "REWIND %.1f SECS".format(java.util.Locale.US, progress)
             Text(
                text = text,
                color = Color.Black,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(top = 2.dp, start = 2.dp)
            )
            Text(
                text = text,
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}
