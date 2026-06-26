package com.swordfish.touchinput.radial.layouts

import android.view.KeyEvent
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.swordfish.touchinput.radial.controls.GbaControlCross
import com.swordfish.touchinput.radial.controls.GbaControlFaceButtons
import com.swordfish.touchinput.radial.controls.LemuroidControlCross
import com.swordfish.touchinput.radial.controls.LemuroidControlFaceButtons
import com.swordfish.touchinput.radial.layouts.shared.ComposeTouchLayouts
import com.swordfish.touchinput.radial.layouts.shared.GbaSecondaryButtonL
import com.swordfish.touchinput.radial.layouts.shared.GbaSecondaryButtonR
import com.swordfish.touchinput.radial.layouts.shared.SecondaryButtonL
import com.swordfish.touchinput.radial.layouts.shared.SecondaryButtonMenu
import com.swordfish.touchinput.radial.layouts.shared.SecondaryButtonMenuPlaceholder
import com.swordfish.touchinput.radial.layouts.shared.SecondaryButtonR
import com.swordfish.touchinput.radial.layouts.shared.SecondaryButtonSelect
import com.swordfish.touchinput.radial.layouts.shared.SecondaryButtonStart
import com.swordfish.touchinput.radial.layouts.shared.SecondaryButtonStartAndSelectStacked
import com.swordfish.touchinput.radial.settings.TouchControllerSettingsManager
import com.swordfish.touchinput.radial.ui.GbaButtonForeground
import com.swordfish.touchinput.radial.ui.GbaRoundButton
import com.swordfish.touchinput.radial.ui.LemuroidButtonForeground
import gg.padkit.PadKitScope
import gg.padkit.ids.Id
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf



@Composable
fun PadKitScope.GBALeft(
    modifier: Modifier = Modifier,
    settings: TouchControllerSettingsManager.Settings,
) {
    BaseLayoutLeft(
        settings = settings,
        // FIXED: Force the core pad layout framework to align its content bounds
        // to the top edge and add a slight top buffer padding
        modifier = modifier
            .padding(top = 16.dp)
            .graphicsLayer {
                // Alternately nudge the layer calculation upwards if parent bounds clip it
                translationY = -40f.dp.toPx()
            },
        primaryDial = { GbaControlCross(id = Id.DiscreteDirection(ComposeTouchLayouts.MOTION_SOURCE_DPAD)) },
        secondaryDials = {
            GbaSecondaryButtonL()
            SecondaryButtonStartAndSelectStacked(position = 30)
        },
    )
}

@Composable
fun PadKitScope.GBARight(
    modifier: Modifier = Modifier,
    settings: TouchControllerSettingsManager.Settings,
) {
    BaseLayoutRight(
        settings = settings,
        // FIXED: Applied identical alignment offsets so the buttons and D-pad line up perfectly
        modifier = modifier
            .padding(top = 16.dp)
            .graphicsLayer {
                translationY = -40f.dp.toPx()
            },
        primaryDial = {
            GbaControlFaceButtons(
                rotationInDegrees = -30f,
                ids =
                    persistentListOf(
                        Id.Key(KeyEvent.KEYCODE_BUTTON_A),
                        Id.Key(KeyEvent.KEYCODE_BUTTON_B),
                    ),
                idsForegrounds =
                    persistentMapOf<Id.Key, @Composable (State<Boolean>) -> Unit>(
                        Id.Key(KeyEvent.KEYCODE_BUTTON_A) to { GbaRoundButton(pressed = it, label = "A") },
                        Id.Key(KeyEvent.KEYCODE_BUTTON_B) to { GbaRoundButton(pressed = it, label = "B") },
                    ),
            )
        },
        secondaryDials = {
            GbaSecondaryButtonR()
        },
    )
}
