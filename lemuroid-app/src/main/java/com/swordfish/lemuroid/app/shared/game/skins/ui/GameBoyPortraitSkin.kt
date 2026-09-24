package com.swordfish.lemuroid.app.shared.game.skins.ui

import android.view.KeyEvent
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.swordfish.lemuroid.app.shared.game.skins.GameBoyModel
import com.swordfish.lemuroid.app.shared.game.skins.GameBoySkin
import com.swordfish.lemuroid.app.shared.game.skins.art.GbArt
import com.swordfish.touchinput.radial.controls.GbControlFaceButtons
import com.swordfish.touchinput.radial.settings.TouchControllerSettingsManager
import gg.padkit.PadKitScope
import gg.padkit.ids.Id
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf

@Composable
fun PadKitScope.GameBoyPortraitSkin(
    skin: GameBoySkin,
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

    val deviceModifier = modifier
        .fillMaxSize()
        .onGloballyPositioned {
            rootOffset.value = it.boundsInRoot().topLeft
        }
        .graphicsLayer {
            compositingStrategy = CompositingStrategy.Offscreen
        }
        .drawBehind {
            GbArt.run {
                drawHandheld(gameScreenRect, bezelRect, skin, false)
            }
        }

    Column(
        modifier = deviceModifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .aspectRatio(160f / 144f),
            contentAlignment = Alignment.Center
        ) {
            gameScreen()
        }

        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 12.dp)
        ) {
            GameBoyDpad(
                skin = skin,
                modifier = Modifier.align(Alignment.TopStart)
            )

            GameBoyActionButtons(
                skin = skin,
                modifier = Modifier.align(Alignment.TopEnd)
            )
        }

        GameBoyMenuButtons(
            model = skin.model,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 12.dp),
            skin = skin,
        )

        GameBoyActionBar {
            actionBar()
        }
    }
}

@Composable
private fun PadKitScope.GameBoyActionButtons(
    skin: GameBoySkin,
    modifier: Modifier = Modifier,
) {
    val rotation = if (skin.model == GameBoyModel.DMG_01) -30f else 0f

    GbControlFaceButtons(
        modifier = modifier.size(180.dp),
        rotationInDegrees = -30f,
        ids = persistentListOf(
            Id.Key(KeyEvent.KEYCODE_BUTTON_A),
            Id.Key(KeyEvent.KEYCODE_BUTTON_B),
        ),
        background = {
            if (skin.model == GameBoyModel.DMG_01) {
                Box(
                    modifier = Modifier
                        .fillMaxSize(0.9f)
                        .aspectRatio(2.2f)
                        .graphicsLayer {
                            rotationZ = rotation
                        }
                        .offset(y = (-4).dp)
                        .background(
                            color = Color.Black.copy(alpha = 0.05f),
                            shape = RoundedCornerShape(percent = 50)
                        )
                )
            }
        },
        idsForegrounds = persistentMapOf<Id.Key, @Composable (State<Boolean>) -> Unit>(
            Id.Key(KeyEvent.KEYCODE_BUTTON_A) to { state ->
                GameBoyActionButtonForeground(
                    pressed = state,
                    label = "A",
                    rotation = rotation,
                    skin = skin,
                )
            },
            Id.Key(KeyEvent.KEYCODE_BUTTON_B) to { state ->
                GameBoyActionButtonForeground(
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
private fun PadKitScope.GameBoyMenuButtons(
    model: GameBoyModel,
    modifier: Modifier = Modifier,
    skin: GameBoySkin,
) {
    val isDmg = model == GameBoyModel.DMG_01
    val buttonBoxModifier = Modifier
        .size(width = 90.dp, height = 60.dp)
        .offset(y = if (isDmg) 0.dp else 50.dp)
    val rotation = if (isDmg) -30f else 0f
    val expansion = if (isDmg) 6.0f else 0.0f

    Box(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(bottom = 32.dp),
        ) {
            GameBoyMenuButton(
                id = Id.Key(KeyEvent.KEYCODE_BUTTON_SELECT),
                label = "SELECT",
                modifier = buttonBoxModifier,
                rotation = rotation,
                expansion = expansion,
                skin = skin,
            )
            GameBoyMenuButton(
                id = Id.Key(KeyEvent.KEYCODE_BUTTON_START),
                label = "START",
                modifier = buttonBoxModifier,
                rotation = rotation,
                expansion = expansion,
                skin = skin,
            )
        }
    }
}

@Composable
private fun GameBoyActionBar(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
