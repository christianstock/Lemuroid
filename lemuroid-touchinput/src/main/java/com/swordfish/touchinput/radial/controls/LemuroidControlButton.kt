package com.swordfish.touchinput.radial.controls

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.swordfish.touchinput.radial.LocalLemuroidPadTheme
import com.swordfish.touchinput.radial.ui.GbaButtonForeground
import com.swordfish.touchinput.radial.ui.GbaSideButtonForeground
import com.swordfish.touchinput.radial.ui.GbaSideControlBackground
import com.swordfish.touchinput.radial.ui.GbcButtonForeground
import com.swordfish.touchinput.radial.ui.LemuroidButtonForeground
import com.swordfish.touchinput.radial.ui.LemuroidControlBackground
import gg.padkit.PadKitScope
import gg.padkit.controls.ControlButton
import gg.padkit.ids.Id
import gg.padkit.layouts.radial.secondarydials.LayoutRadialSecondaryDialsScope
import androidx.compose.runtime.State

context(PadKitScope, LayoutRadialSecondaryDialsScope)
@Composable
fun LemuroidControlButton(
    modifier: Modifier = Modifier,
    id: Id.Key,
    label: String? = null,
    icon: Int? = null,
) {
    val theme = LocalLemuroidPadTheme.current
    ControlButton(
        modifier = modifier.padding(theme.padding),
        id = id,
        foreground = { LemuroidButtonForeground(pressed = it, icon = icon, label = label) },
        background = { LemuroidControlBackground() },
    )
}

@Composable
fun PadKitScope.GBControlButton(
    modifier: Modifier = Modifier,
    id: Id.Key,
    label: String? = null,
    icon: Int? = null,
    background: (@Composable (State<Boolean>) -> Unit) = { },
    foreground: (@Composable (State<Boolean>) -> Unit) = { },
) {
    val theme = LocalLemuroidPadTheme.current
    ControlButton(
        modifier = modifier.padding(theme.padding),
        id = id,
        background = background,
        foreground = foreground,
    )
}

@Composable
fun PadKitScope.GBCControlButton(
    modifier: Modifier = Modifier,
    id: Id.Key,
    label: String? = null,
    icon: Int? = null,
) {
    val theme = LocalLemuroidPadTheme.current
    ControlButton(
        modifier = modifier.padding(theme.padding),
        id = id,
        foreground = { GbcButtonForeground(pressed = it, icon = icon, label = label) },
        background = { }
    )
}

@Composable
fun PadKitScope.GBAControlButton(
    modifier: Modifier = Modifier,
    id: Id.Key,
    label: String? = null,
    icon: Int? = null,
) {
    val theme = LocalLemuroidPadTheme.current
    ControlButton(
        modifier = modifier.padding(theme.padding),
        id = id,
        foreground = { GbaButtonForeground(pressed = it, icon = icon, label = label) },
        background = { }
    )
}


context(PadKitScope, LayoutRadialSecondaryDialsScope)
@Composable
fun GbaSideControlButton(
    modifier: Modifier = Modifier,
    id: Id.Key,
    label: String? = null,
    icon: Int? = null,
) {
    val theme = LocalLemuroidPadTheme.current

    Box(
        modifier = modifier
            // 1. Force the outer container to be wider and noticeably taller than the button
            .requiredWidth(140.dp)
            .requiredHeight(75.dp), // 45dp (button) + 30dp (creates 15dp spacing top & bottom)
        contentAlignment = Alignment.Center
    ) {
        ControlButton(
            modifier = Modifier
                // 2. The button stays its perfect elongated size inside the taller container
                .requiredWidth(140.dp)
                .requiredHeight(45.dp)
                .padding(theme.padding),
            id = id,
            foreground = { GbaSideButtonForeground(pressed = it, icon = icon, label = label) },
            background = { GbaSideControlBackground() },
        )
    }
}
