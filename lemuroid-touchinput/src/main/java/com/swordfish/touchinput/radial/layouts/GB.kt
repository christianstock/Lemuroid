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
import com.swordfish.touchinput.radial.controls.LemuroidControlCross
import com.swordfish.touchinput.radial.controls.LemuroidControlFaceButtons
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

// Custom button foreground for GB A/B buttons - slimmer font, left-offset
@Composable
private fun GBButtonForeground(pressed: State<Boolean>, label: String) {
    val theme = LocalLemuroidPadTheme.current
    val buttonActiveColor = theme.foregroundFill(pressed.value)

    // Rotate the entire component (button + label) 30 degrees counter-clockwise
    Column(
        modifier = Modifier
            .graphicsLayer {
                rotationZ = -30f
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. The Circular Physical Button Base
        Box(
            modifier = Modifier
                .size(56.dp) // Restored original 48.dp bounding size
                .background(
                    // Fall back to theme button color (level0Fill) or a pressed variant for visual feedback
                    color = buttonActiveColor,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            // If there's an inner icon or specialized asset inside the circle, it goes here
        }

        // 2. Snug layout gap below the circle
        Spacer(modifier = Modifier.height(4.dp))

        // 3. The Label below the button that pokes out without wrapping
        Text(
            text = label,
            color = Color(0xff3639a0), // Matches the theme color rule
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            textAlign = TextAlign.Center,
            maxLines = 1,
            softWrap = false,
            modifier = Modifier
                .layout { measurable, constraints ->
                    // Measure text with unconstrained width so it never line-wraps
                    val placeable = measurable.measure(constraints.copy(maxWidth = Int.MAX_VALUE))

                    // Lie about the width to prevent pushing the parent container bounds around
                    layout(constraints.maxWidth, placeable.height) {
                        val xOffset = (constraints.maxWidth - placeable.width) / 2
                        placeable.place(xOffset, 0)
                    }
                }
        )
    }
}


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
                        Id.Key(KeyEvent.KEYCODE_BUTTON_A) to { GBButtonForeground(pressed = it, label = "A") },
                        Id.Key(KeyEvent.KEYCODE_BUTTON_B) to { GBButtonForeground(pressed = it, label = "B") },
                    ),
            )
        },
        secondaryDials = {
            SecondaryButtonStartGB(position = 30)
        },
    )
}
