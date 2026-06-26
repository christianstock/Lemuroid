package com.swordfish.touchinput.radial.layouts.shared

import android.R.attr.translationY
import android.view.KeyEvent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.swordfish.touchinput.controller.R
import com.swordfish.touchinput.radial.controls.GBCControlButton
import com.swordfish.touchinput.radial.controls.LemuroidControlAnalog
import com.swordfish.touchinput.radial.controls.LemuroidControlButton
import com.swordfish.touchinput.radial.settings.TouchControllerSettingsManager
import gg.padkit.PadKitScope
import gg.padkit.ids.Id
import gg.padkit.layouts.radial.secondarydials.LayoutRadialSecondaryDialsScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import com.swordfish.touchinput.radial.controls.GBAControlButton
import com.swordfish.touchinput.radial.controls.GBControlButton
import com.swordfish.touchinput.radial.controls.GbaSideControlButton

context(PadKitScope, LayoutRadialSecondaryDialsScope)
@Composable
fun SecondaryButtonSelect(position: Int = 0) {
    LemuroidControlButton(
        modifier = Modifier.radialPosition(120f - 30f * position),
        id = Id.Key(KeyEvent.KEYCODE_BUTTON_SELECT),
        icon = R.drawable.button_select,
    )
}

context(PadKitScope, LayoutRadialSecondaryDialsScope)
@Composable
fun SecondaryButtonSelectGB(position: Int = 0) {
    GBControlButton(
        modifier = Modifier
            .radialPosition(120f - 30f * position)
            .graphicsLayer {
                // This pushes the button further out along its layout axis
                translationY = 30.dp.toPx()
            },
        id = Id.Key(KeyEvent.KEYCODE_BUTTON_SELECT),
        label = "SELECT",
        icon = null,
    )
}

context(PadKitScope, LayoutRadialSecondaryDialsScope)
@Composable
fun SecondaryButtonSelectGBC(position: Int = 0) {
    GBCControlButton(
        modifier = Modifier
            .radialPosition(120f - 30f * position)
            .graphicsLayer {
                // This pushes the button further out along its layout axis
                translationY = 60.dp.toPx()
            },
        id = Id.Key(KeyEvent.KEYCODE_BUTTON_SELECT),
        label = "SELECT",
        icon = null,
    )
}

context(PadKitScope, LayoutRadialSecondaryDialsScope)
@Composable
fun SecondaryButtonL1() {
    LemuroidControlButton(
        modifier = Modifier.radialPosition(90f),
        id = Id.Key(KeyEvent.KEYCODE_BUTTON_L1),
        label = "L1",
    )
}

context(PadKitScope, LayoutRadialSecondaryDialsScope)
@Composable
fun SecondaryButtonL2() {
    LemuroidControlButton(
        modifier = Modifier.radialPosition(120f),
        id = Id.Key(KeyEvent.KEYCODE_BUTTON_L2),
        label = "L2",
    )
}

context(PadKitScope, LayoutRadialSecondaryDialsScope)
@Composable
fun SecondaryButtonR1() {
    LemuroidControlButton(
        modifier = Modifier.radialPosition(90f),
        id = Id.Key(KeyEvent.KEYCODE_BUTTON_R1),
        label = "R1",
    )
}

context(PadKitScope, LayoutRadialSecondaryDialsScope)
@Composable
fun SecondaryButtonR2() {
    LemuroidControlButton(
        modifier = Modifier.radialPosition(60f),
        id = Id.Key(KeyEvent.KEYCODE_BUTTON_R2),
        label = "R2",
    )
}


context(PadKitScope, LayoutRadialSecondaryDialsScope)
@Composable
fun SecondaryButtonL() {
    LemuroidControlButton(
        modifier = Modifier.radialPosition(120f),
        id = Id.Key(KeyEvent.KEYCODE_BUTTON_L1),
        label = "L",
    )
}

context(PadKitScope, LayoutRadialSecondaryDialsScope)
@Composable
fun SecondaryButtonR() {
    LemuroidControlButton(
        modifier = Modifier.radialPosition(60f),
        id = Id.Key(KeyEvent.KEYCODE_BUTTON_R1),
        label = "R",
    )
}


context(PadKitScope, LayoutRadialSecondaryDialsScope)
@Composable
fun GbaSecondaryButtonL() {
    GbaSideControlButton(
        modifier = Modifier.radialPosition(120f),
        id = Id.Key(KeyEvent.KEYCODE_BUTTON_L1),
        label = "L",
    )
}

context(PadKitScope, LayoutRadialSecondaryDialsScope)
@Composable
fun GbaSecondaryButtonR() {
    GbaSideControlButton(
        modifier = Modifier.radialPosition(60f),
        id = Id.Key(KeyEvent.KEYCODE_BUTTON_R1),
        label = "R",
    )
}

context(PadKitScope, LayoutRadialSecondaryDialsScope)
@Composable
fun SecondaryButtonStartAndSelectStacked(position: Int) {
    // 1. Top Button: Start
    GBAControlButton(
        // Kept as a direct child so the engine renders it normally
        modifier = Modifier.radialPosition(120f - 30f * position),
        id = Id.Key(KeyEvent.KEYCODE_BUTTON_START),
        icon = null,
        label = "START"
    )

    // 2. Bottom Button: Select
    GBAControlButton(
        modifier = Modifier
            // Direct child at the exact same position slot
            .radialPosition(120f - 30f * position)
            // FIXED: Forcefully push this button DOWN vertically (e.g., 50dp or 60dp)
            // and adjust left/right to counter the engine's side-by-side arrangement
            .graphicsLayer {
                translationY = 55f.dp.toPx()  // Slides it down directly below Start
                translationX = 0f              // Adjust this if it needs to center perfectly
            },
        id = Id.Key(KeyEvent.KEYCODE_BUTTON_SELECT),
        icon = null,
        label = "SELECT"
    )
}

context(PadKitScope, LayoutRadialSecondaryDialsScope)
@Composable
fun SecondaryButtonStart(position: Int = 0) {
    LemuroidControlButton(
        modifier = Modifier.radialPosition(60f + 30f * position),
        id = Id.Key(KeyEvent.KEYCODE_BUTTON_START),
        icon = R.drawable.button_start,
    )
}

context(PadKitScope, LayoutRadialSecondaryDialsScope)
@Composable
fun SecondaryButtonStartGB(position: Int = 0) {
    GBControlButton(
        modifier = Modifier
            .radialPosition(60f + 30f * position)
            .graphicsLayer {
                // This pushes the button further out along its layout axis
                translationY = 30.dp.toPx()
            },
        id = Id.Key(KeyEvent.KEYCODE_BUTTON_START),
        label = "START",
        icon = null,
    )
}

context(PadKitScope, LayoutRadialSecondaryDialsScope)
@Composable
fun SecondaryButtonStartGBC(position: Int = 0) {
    GBCControlButton(
        modifier = Modifier
            .radialPosition(60f + 30f * position)
            .graphicsLayer {
                // This pushes the button further out along its layout axis
                translationY = 60.dp.toPx()
            },
        id = Id.Key(KeyEvent.KEYCODE_BUTTON_START),
        label = "START",
        icon = null,
    )
}

context(PadKitScope, LayoutRadialSecondaryDialsScope)
@Composable
fun SecondaryButtonMenu(settings: TouchControllerSettingsManager.Settings) {
    LemuroidControlButton(
        modifier = Modifier.radialPosition(-60f + 2f * settings.rotation * TouchControllerSettingsManager.MAX_ROTATION),
        id = Id.Key(KeyEvent.KEYCODE_BUTTON_MODE),
        icon = R.drawable.button_menu,
    )
}

context(PadKitScope, LayoutRadialSecondaryDialsScope)
@Composable
fun SecondaryButtonMenuPlaceholder(settings: TouchControllerSettingsManager.Settings) {
    Box(
        modifier =
            Modifier.radialPosition(
                -120f - 2f * settings.rotation * TouchControllerSettingsManager.MAX_ROTATION,
            ),
    )
}

context(PadKitScope, LayoutRadialSecondaryDialsScope)
@Composable
fun SecondaryAnalogLeft() {
    LemuroidControlAnalog(
        modifier =
            Modifier
                .radialPosition(-80f)
                .radialScale(2.0f),
        id = Id.ContinuousDirection(ComposeTouchLayouts.MOTION_SOURCE_LEFT_STICK),
        analogPressId = Id.Key(KeyEvent.KEYCODE_BUTTON_THUMBL),
    )
}

context(PadKitScope, LayoutRadialSecondaryDialsScope)
@Composable
fun SecondaryAnalogRight() {
    LemuroidControlAnalog(
        modifier =
            Modifier
                .radialPosition(+80f - 180f)
                .radialScale(2.0f),
        id = Id.ContinuousDirection(ComposeTouchLayouts.MOTION_SOURCE_RIGHT_STICK),
        analogPressId = Id.Key(KeyEvent.KEYCODE_BUTTON_THUMBR),
    )
}

context(PadKitScope, LayoutRadialSecondaryDialsScope)
@Composable
fun SecondaryButtonCoin() {
    LemuroidControlButton(
        modifier = Modifier.radialPosition(120f),
        id = Id.Key(KeyEvent.KEYCODE_BUTTON_SELECT),
        icon = R.drawable.button_coin,
    )
}

object ComposeTouchLayouts {
    const val MOTION_SOURCE_DPAD = 0
    const val MOTION_SOURCE_LEFT_STICK = 1
    const val MOTION_SOURCE_RIGHT_STICK = 2
    const val MOTION_SOURCE_DPAD_AND_LEFT_STICK = 3
    const val MOTION_SOURCE_RIGHT_DPAD = 4
}
