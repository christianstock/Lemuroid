package com.swordfish.lemuroid.app.shared.game.skins.ui

import android.view.KeyEvent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.swordfish.lemuroid.app.shared.game.skins.GameBoyAdvanceModel
import com.swordfish.lemuroid.app.shared.game.skins.GameBoyAdvanceSkin
import com.swordfish.lemuroid.app.shared.game.skins.art.GameBoyAdvanceArt
import com.swordfish.touchinput.radial.controls.GbControlFaceButtons
import com.swordfish.touchinput.radial.settings.TouchControllerSettingsManager
import gg.padkit.PadKitScope
import gg.padkit.ids.Id
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf

@Composable
fun PadKitScope.GameBoyAdvanceLandscapeSkin(
    skin: GameBoyAdvanceSkin,
    gameScreen: @Composable () -> Unit,
    actionBar: @Composable () -> Unit,
    touchControllerSettings: TouchControllerSettingsManager.Settings,
    gameScreenPos: Rect?,
    modifier: Modifier = Modifier,
) {
    val rootOffset = remember { mutableStateOf(Offset.Zero) }
    val gameScreenRect = remember(gameScreenPos, rootOffset.value) {
        gameScreenPos?.translate(-rootOffset.value)
    }

    val density = LocalDensity.current
    val topBezelPx = with(density) { 36.dp.toPx() }
    val bottomBezelPx = with(density) { 36.dp.toPx() }
    val sideBezelPx = with(density) { 50.dp.toPx() }

    val bezelRect = remember(gameScreenRect) {
        gameScreenRect?.let { rect ->
            Rect(
                left = rect.left - sideBezelPx,
                top = rect.top - topBezelPx,
                right = rect.right + sideBezelPx,
                bottom = rect.bottom + bottomBezelPx
            )
        }
    }

    val batteryLevel = rememberBatteryLevel()

    Box(
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned {
                rootOffset.value = it.boundsInRoot().topLeft
            }
            .graphicsLayer {
                compositingStrategy = CompositingStrategy.Offscreen
            }
            .drawBehind {
                GameBoyAdvanceArt.run {
                    drawHandheld(gameScreenRect, bezelRect, skin, false, batteryLevel = batteryLevel)
                }
            }
    ) {
        // 1. FLUSH TOP SHOULDER BUTTON OVERLAY LAYER
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left Shoulder Button (Flush Top-Left)
            GameBoyAdvanceShoulderButton(
                id = Id.Key(KeyEvent.KEYCODE_BUTTON_L1),
                side = ShoulderSide.LEFT,
                label = "L",
                buttonColor = skin.actionButtonColor,
            )

            // Right Shoulder Button (Flush Top-Right)
            GameBoyAdvanceShoulderButton(
                id = Id.Key(KeyEvent.KEYCODE_BUTTON_R1),
                side = ShoulderSide.RIGHT,
                label = "R",
                buttonColor = skin.actionButtonColor,
            )
        }

        // 2. MAIN CONTROLS & SCREEN CONTENT LAYER
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Control Panel (D-Pad & Menu Buttons)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Spacer(modifier = Modifier.height(20.dp))

                    GameBoyAdvanceDpad(
                        skin = skin,
                    )

                    GameBoyAdvanceMenuButtons(
                        model = skin.model,
                        skin = skin,
                    )
                }
            }

            // Center Screen & Controls
            Column(
                modifier = Modifier
                    .weight(2f)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.5f)
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    gameScreen()
                }

                Spacer(modifier = Modifier.height(24.dp))

                actionBar()
            }

            // Right Action Buttons
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Spacer(modifier = Modifier.height(70.dp))

                    GameBoyAdvanceActionButtons(
                        skin = skin,
                    )
                }
            }
        }
    }
}

@Composable
private fun PadKitScope.GameBoyAdvanceActionButtons(
    skin: GameBoyAdvanceSkin,
    modifier: Modifier = Modifier,
) {
    val rotation = 0f

    GbControlFaceButtons(
        modifier = modifier.size(180.dp),
        rotationInDegrees = -30f,
        ids = persistentListOf(
            Id.Key(KeyEvent.KEYCODE_BUTTON_A),
            Id.Key(KeyEvent.KEYCODE_BUTTON_B),
        ),
        background = { },
        idsForegrounds = persistentMapOf<Id.Key, @Composable (androidx.compose.runtime.State<Boolean>) -> Unit>(
            Id.Key(KeyEvent.KEYCODE_BUTTON_A) to { state ->
                GameBoyAdvanceActionButtonForeground(
                    pressed = state,
                    label = "A",
                    rotation = rotation,
                    skin = skin,
                )
            },
            Id.Key(KeyEvent.KEYCODE_BUTTON_B) to { state ->
                GameBoyAdvanceActionButtonForeground(
                    pressed = state,
                    label = "B",
                    rotation = rotation,
                    skin = skin,
                )
            },
        ),
    )
}

@Composable
private fun PadKitScope.GameBoyAdvanceMenuButtons(
    model: GameBoyAdvanceModel,
    skin: GameBoyAdvanceSkin,
    modifier: Modifier = Modifier,
) {
    val rotation = 10.0f
    val buttonModifier = Modifier.size(width = 120.dp, height = 46.dp)

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        GameBoyAdvanceMenuButton(
            id = Id.Key(KeyEvent.KEYCODE_BUTTON_START),
            label = "START",
            skin = skin,
            modifier = buttonModifier,
            rotation = rotation,
        )

        GameBoyAdvanceMenuButton(
            id = Id.Key(KeyEvent.KEYCODE_BUTTON_SELECT),
            label = "SELECT",
            skin = skin,
            modifier = buttonModifier,
            rotation = rotation,
        )
    }
}
