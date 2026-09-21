import android.view.KeyEvent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import com.swordfish.lemuroid.app.shared.game.skins.GbSkin
import com.swordfish.lemuroid.app.shared.game.skins.art.GbArt
import com.swordfish.touchinput.radial.controls.GbControlFaceButtons
import com.swordfish.touchinput.radial.controls.GBControlButton
import com.swordfish.touchinput.radial.controls.GbControlCross
import com.swordfish.touchinput.radial.layouts.shared.ComposeTouchLayouts
import com.swordfish.touchinput.radial.settings.TouchControllerSettingsManager
import com.swordfish.touchinput.radial.ui.DmgRoundButtonForeground
import gg.padkit.PadKitScope
import gg.padkit.ids.Id
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf

@Composable
fun PadKitScope.GbPortraitSkin(
    skin: GbSkin,
    gameScreen: @Composable () -> Unit,
    actionBar: @Composable () -> Unit,
    touchControllerSettings: TouchControllerSettingsManager.Settings,
    gameScreenPos: Rect?,
    modifier: Modifier = Modifier,
) {
    val bezelRect = remember { mutableStateOf<Rect?>(null) }
    val rootOffset = remember { mutableStateOf(Offset.Zero) }

    val gameScreenRect = remember(gameScreenPos, rootOffset.value) {
        gameScreenPos?.translate(-rootOffset.value)
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
                drawHandheld(gameScreenRect, bezelRect.value, skin, false)
            }
        }

    val gameScreenModifier = Modifier
        .fillMaxWidth()
        .aspectRatio(160f / 144f)
        .padding(16.dp)
        .onGloballyPositioned {
            bezelRect.value = it.boundsInParent()
        }

    val actionBarModifier = Modifier
        .fillMaxWidth()
        .height(56.dp)
        .padding(horizontal = 12.dp)

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
            modifier = gameScreenModifier,
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
            GbControlCross(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .size(160.dp),
                id = Id.DiscreteDirection(ComposeTouchLayouts.MOTION_SOURCE_DPAD),
                background = { }
            )

            // A/B Buttons
            GbControlFaceButtons(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(180.dp),
                rotationInDegrees = -30f,
                ids = persistentListOf(
                    Id.Key(KeyEvent.KEYCODE_BUTTON_A),
                    Id.Key(KeyEvent.KEYCODE_BUTTON_B),
                ),
                background = { },
                idsForegrounds = persistentMapOf<Id.Key, @Composable (State<Boolean>) -> Unit>(
                    Id.Key(KeyEvent.KEYCODE_BUTTON_A) to {
                        DmgRoundButtonForeground(
                            pressed = it,
                            label = "A"
                        )
                    },
                    Id.Key(KeyEvent.KEYCODE_BUTTON_B) to {
                        DmgRoundButtonForeground(
                            pressed = it,
                            label = "B"
                        )
                    },
                ),
            )


        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 12.dp)
        ) {
            // Start/Select Buttons
            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(bottom = 32.dp),
            ) {
                // 1. Constrain SELECT Button
                Box(
                    modifier = Modifier.size(width = 90.dp, height = 60.dp), // Adjust these bounds to your liking!
                    contentAlignment = Alignment.TopCenter
                ) {
                    GBControlButton(
                        id = Id.Key(KeyEvent.KEYCODE_BUTTON_SELECT),
                        label = "SELECT"
                    )
                }

                // 2. Constrain START Button
                Box(
                    modifier = Modifier.size(width = 90.dp, height = 60.dp), // Keeps them completely uniform
                    contentAlignment = Alignment.TopCenter
                ) {
                    GBControlButton(
                        id = Id.Key(KeyEvent.KEYCODE_BUTTON_START),
                        label = "START"
                    )
                }
            }
        }

        Box(
            modifier = actionBarModifier,
            contentAlignment = Alignment.Center
        ) {
            actionBar()
        }
    }
}
