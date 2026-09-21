package com.swordfish.touchinput.radial.controls

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import com.swordfish.touchinput.radial.LocalLemuroidPadTheme
import com.swordfish.touchinput.radial.ui.GbDpadBackground
import com.swordfish.touchinput.radial.ui.GbDpadForeground
import com.swordfish.touchinput.radial.ui.GbaCrossForeground
import com.swordfish.touchinput.radial.ui.GbcCrossForeground
import com.swordfish.touchinput.radial.ui.LemuroidCrossForeground
import gg.padkit.PadKitScope
import gg.padkit.controls.ControlCross
import gg.padkit.ids.Id

@Composable
fun PadKitScope.LemuroidControlCross(
    modifier: Modifier = Modifier,
    id: Id.DiscreteDirection,
    allowDiagonals: Boolean = true,
    background: @Composable () -> Unit = {
        //LemuroidControlBackground()
    },
    foreground: @Composable (State<Offset>) -> Unit = {
        LemuroidCrossForeground(
            allowDiagonals = allowDiagonals,
            directionState = it,
        )
    },
) {
    val theme = LocalLemuroidPadTheme.current
    ControlCross(
        modifier = modifier.padding(theme.padding),
        id = id,
        allowDiagonals = allowDiagonals,
        background = background,
        foreground = foreground,
    )
}

@Composable
fun PadKitScope.GbControlCross(
    modifier: Modifier = Modifier,
    id: Id.DiscreteDirection,
    allowDiagonals: Boolean = true,
    background: @Composable () -> Unit = {
        GbDpadBackground()
    },
    foreground: @Composable (State<Offset>) -> Unit = {
        GbDpadForeground(
            allowDiagonals = allowDiagonals,
            directionState = it,
        )
    },
) {
    val theme = LocalLemuroidPadTheme.current
    ControlCross(
        modifier = modifier.padding(theme.padding),
        id = id,
        allowDiagonals = allowDiagonals,
        background = background,
        foreground = foreground,
    )
}

@Composable
fun PadKitScope.GbcControlCross(
    modifier: Modifier = Modifier,
    id: Id.DiscreteDirection,
    allowDiagonals: Boolean = true,
    background: @Composable () -> Unit = {
        //LemuroidControlBackground()
    },
    foreground: @Composable (State<Offset>) -> Unit = {
        GbcCrossForeground(
            allowDiagonals = allowDiagonals,
            directionState = it,
        )
    },
) {
    val theme = LocalLemuroidPadTheme.current
    ControlCross(
        modifier = modifier.padding(theme.padding),
        id = id,
        allowDiagonals = allowDiagonals,
        background = background,
        foreground = foreground,
    )
}


@Composable
fun PadKitScope.GbaControlCross(
    modifier: Modifier = Modifier,
    id: Id.DiscreteDirection,
    allowDiagonals: Boolean = true,
    background: @Composable () -> Unit = {
        //LemuroidControlBackground()
    },
    foreground: @Composable (State<Offset>) -> Unit = {
        GbaCrossForeground(
            allowDiagonals = allowDiagonals,
            directionState = it,
        )
    },
) {
    val theme = LocalLemuroidPadTheme.current
    ControlCross(
        modifier = modifier.padding(theme.padding),
        id = id,
        allowDiagonals = allowDiagonals,
        background = background,
        foreground = foreground,
    )
}
