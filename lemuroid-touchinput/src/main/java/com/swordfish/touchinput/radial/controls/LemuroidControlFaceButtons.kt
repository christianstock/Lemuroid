package com.swordfish.touchinput.radial.controls

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swordfish.touchinput.radial.LocalLemuroidPadTheme
import com.swordfish.touchinput.radial.ui.LemuroidCompositeForeground
import com.swordfish.touchinput.radial.ui.LemuroidControlBackground
import gg.padkit.PadKitScope
import gg.padkit.anchors.Anchor
import gg.padkit.controls.ControlFaceButtons
import gg.padkit.ids.Id
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf

context(PadKitScope)
@Composable
fun LemuroidControlFaceButtons(
    modifier: Modifier = Modifier,
    rotationInDegrees: Float = 0f,
    ids: PersistentList<Id.Key>,
    includeComposite: Boolean = true,
    applyPadding: Boolean = true,
    trackPointers: Boolean = true,
    background: @Composable () -> Unit = { LemuroidControlBackground() },
    idsForegrounds: PersistentMap<Id.Key, @Composable (State<Boolean>) -> Unit>,
) {
    val theme = LocalLemuroidPadTheme.current
    ControlFaceButtons(
        modifier =
            modifier
                .run { if (applyPadding) padding(theme.padding) else modifier },
        includeComposite = includeComposite,
        ids = ids,
        trackPointers = trackPointers,
        rotationInDegrees = rotationInDegrees,
        foreground = { id, pressed -> (idsForegrounds[id]!!)(pressed) },
        background = background,
        foregroundComposite = { LemuroidCompositeForeground(it) },
    )
}

context(PadKitScope)
@Composable
fun LemuroidControlFaceButtons(
    modifier: Modifier = Modifier,
    primaryAnchors: PersistentList<Anchor<Id.Key>>,
    background: @Composable () -> Unit = { LemuroidControlBackground() },
    applyPadding: Boolean = true,
    trackPointers: Boolean = true,
    idsForegrounds: PersistentMap<Id.Key, @Composable (State<Boolean>) -> Unit>,
) {
    val theme = LocalLemuroidPadTheme.current
    ControlFaceButtons(
        modifier =
            modifier
                .run { if (applyPadding) padding(theme.padding) else modifier },
        primaryAnchors = primaryAnchors,
        compositeAnchors = persistentListOf(),
        trackPointers = trackPointers,
        foreground = { id, pressed -> (idsForegrounds[id]!!)(pressed) },
        background = background,
        foregroundComposite = { LemuroidCompositeForeground(it) },
    )
}


context(PadKitScope)
@Composable
fun GbcControlFaceButtons(
    modifier: Modifier = Modifier,
    rotationInDegrees: Float = 0f,
    ids: PersistentList<Id.Key>,
    includeComposite: Boolean = false,
    applyPadding: Boolean = true,
    trackPointers: Boolean = true,
    background: @Composable () -> Unit = { },
    idsForegrounds: PersistentMap<Id.Key, @Composable (State<Boolean>) -> Unit>,
) {
    val theme = LocalLemuroidPadTheme.current
    ControlFaceButtons(
        modifier =
            modifier
                .run { if (applyPadding) padding(theme.padding) else modifier },
        includeComposite = includeComposite,
        ids = ids,
        trackPointers = trackPointers,
        rotationInDegrees = rotationInDegrees,
        foreground = { id, pressed -> (idsForegrounds[id]!!)(pressed) },
        background = background,
        foregroundComposite = { LemuroidCompositeForeground(it) },
    )
}

context(PadKitScope)
@Composable
fun GbcControlFaceButtons(
    modifier: Modifier = Modifier,
    primaryAnchors: PersistentList<Anchor<Id.Key>>,
    background: @Composable () -> Unit = { },
    applyPadding: Boolean = true,
    trackPointers: Boolean = true,
    idsForegrounds: PersistentMap<Id.Key, @Composable (State<Boolean>) -> Unit>,
) {
    val theme = LocalLemuroidPadTheme.current
    ControlFaceButtons(
        modifier =
            modifier
                .run { if (applyPadding) padding(theme.padding) else modifier },
        primaryAnchors = primaryAnchors,
        compositeAnchors = persistentListOf(),
        trackPointers = trackPointers,
        foreground = { id, pressed -> (idsForegrounds[id]!!)(pressed) },
        background = background,
        foregroundComposite = { LemuroidCompositeForeground(it) },
    )
}
