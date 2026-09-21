package com.swordfish.touchinput.radial.layouts

import android.view.KeyEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swordfish.touchinput.radial.LocalLemuroidPadTheme
import com.swordfish.touchinput.radial.controls.GbControlCross
import com.swordfish.touchinput.radial.controls.GbControlFaceButtons
import com.swordfish.touchinput.radial.ui.DmgRoundButtonForeground
import com.swordfish.touchinput.radial.layouts.shared.ComposeTouchLayouts
import com.swordfish.touchinput.radial.layouts.shared.SecondaryButtonMenuPlaceholder
import com.swordfish.touchinput.radial.layouts.shared.SecondaryButtonSelectGB
import com.swordfish.touchinput.radial.layouts.shared.SecondaryButtonStartGB
import com.swordfish.touchinput.radial.settings.TouchControllerSettingsManager
import gg.padkit.PadKitScope
import gg.padkit.ids.Id
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import androidx.compose.foundation.layout.Column

@Composable
fun PadKitScope.GBLeft(
    modifier: Modifier = Modifier,
    settings: TouchControllerSettingsManager.Settings,
) {
    BaseLayoutLeft(
        settings = settings,
        modifier = modifier,
        primaryDial = {
            GbControlCross(
                id = Id.DiscreteDirection(ComposeTouchLayouts.MOTION_SOURCE_DPAD),
                background = { } // No background circle
            )
        },
        secondaryDials = {
            SecondaryButtonSelectGB(position = 30)
        },
    )
}

@Composable
fun PadKitScope.GBRight(
    modifier: Modifier = Modifier,
    settings: TouchControllerSettingsManager.Settings,
) {
    BaseLayoutRight(
        settings = settings,
        modifier = modifier,
        primaryDial = {
            GbControlFaceButtons(
                rotationInDegrees = -30f,
                ids =
                    persistentListOf(
                        Id.Key(KeyEvent.KEYCODE_BUTTON_A),
                        Id.Key(KeyEvent.KEYCODE_BUTTON_B),
                    ),
                background = { }, // Empty background - no circles
                idsForegrounds =
                    persistentMapOf<Id.Key, @Composable (State<Boolean>) -> Unit>(
                        Id.Key(KeyEvent.KEYCODE_BUTTON_A) to { DmgRoundButtonForeground(pressed = it, label = "A") },
                        Id.Key(KeyEvent.KEYCODE_BUTTON_B) to { DmgRoundButtonForeground(pressed = it, label = "B") },
                    ),
            )
        },
        secondaryDials = {
            SecondaryButtonStartGB(position = 30)
        },
    )
}
