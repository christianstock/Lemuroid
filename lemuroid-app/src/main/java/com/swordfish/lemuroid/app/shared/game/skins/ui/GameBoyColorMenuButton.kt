package com.swordfish.lemuroid.app.shared.game.skins.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swordfish.lemuroid.app.shared.game.skins.GameBoyColorSkin
import com.swordfish.touchinput.radial.controls.GBControlButton
import gg.padkit.PadKitScope
import gg.padkit.ids.Id

// Made slightly longer (45.dp) with standard height (17.dp)
private val OVAL_WIDTH: Dp = 45.dp
private val OVAL_HEIGHT: Dp = 17.dp

// Background bezel expansion padding
private val BEZEL_EXPANSION: Dp = 3.dp

@Composable
fun PadKitScope.GameBoyColorMenuButton(
    id: Id.Key,
    label: String,
    skin: GameBoyColorSkin,
    modifier: Modifier = Modifier,
    rotation: Float = 0f,
    expansion: Float = 0.0f,
    labelScale: Float = 1.0f,
) {
    Box(
        modifier = modifier.graphicsLayer { rotationZ = rotation },
        contentAlignment = Alignment.TopCenter,
    ) {
        GBControlButton(
            id = id,
            background = {
                GameBoyColorMenuButtonBackground(
                    label = label,
                    labelScale = labelScale,
                    expansion = expansion,
                    skin = skin,
                )
            },
            foreground = { pressed ->
                GameBoyColorMenuButtonForeground(
                    pressed = pressed,
                    skin = skin,
                )
            }
        )
    }
}

@Composable
fun GameBoyColorMenuButtonBackground(
    modifier: Modifier = Modifier,
    label: String? = null,
    labelScale: Float = 1.0f,
    expansion: Float = 0.0f,
    skin: GameBoyColorSkin,
) {
    // Reduced minimum width constraint to allow buttons to sit closer together
    Column(
        modifier = modifier.widthIn(min = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(OVAL_HEIGHT + (BEZEL_EXPANSION * 2))
        ) {
            val bgW = (OVAL_WIDTH + (BEZEL_EXPANSION * 2) + expansion.dp).toPx()
            val bgH = (OVAL_HEIGHT + (BEZEL_EXPANSION * 2) + expansion.dp).toPx()

            val left = (size.width - bgW) / 2f
            val top = (size.height - bgH) / 2f

            drawOval(
                color = Color.Black.copy(alpha = 0.18f),
                topLeft = Offset(left, top),
                size = Size(bgW, bgH)
            )
        }

        Spacer(modifier = Modifier.height(3.dp))

        if (label != null) {
            GameBoyColorMenuButtonLabel(
                label = label,
                labelScale = labelScale,
                skin = skin,
            )
        }
    }
}

@Composable
fun GameBoyColorMenuButtonLabel(
    label: String,
    modifier: Modifier = Modifier,
    labelScale: Float = 1.0f,
    skin: GameBoyColorSkin,
) {
    Text(
        text = label,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 1.dp),
        textAlign = TextAlign.Center,
        maxLines = 1,
        softWrap = false,
        overflow = TextOverflow.Visible,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.SansSerif,
        color = skin.labelColor,
        // Increased font size from 12f to 14.5f
        fontSize = (14.5f * labelScale).sp
    )
}

@Composable
fun GameBoyColorMenuButtonForeground(
    pressed: State<Boolean>,
    modifier: Modifier = Modifier,
    skin: GameBoyColorSkin,
) {
    val baseColor = skin.menuButtonColor
    val isPressed = pressed.value

    val buttonColor = if (isPressed) {
        baseColor.adjustBrightness(0.1f)
    } else {
        baseColor
    }

    val outlineAlpha = if (isPressed) 0.3f else 0.2f
    val outlineColor = baseColor.adjustBrightness(outlineAlpha)

    Column(
        modifier = modifier.widthIn(min = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(OVAL_HEIGHT + (BEZEL_EXPANSION * 2))
        ) {
            val fgW = OVAL_WIDTH.toPx()
            val fgH = OVAL_HEIGHT.toPx()

            val left = (size.width - fgW) / 2f
            val top = (size.height - fgH) / 2f

            // Mathematical ellipse body
            drawOval(
                color = buttonColor,
                topLeft = Offset(left, top),
                size = Size(fgW, fgH)
            )

            // Outer border
            drawOval(
                color = outlineColor,
                topLeft = Offset(left, top),
                size = Size(fgW, fgH),
                style = Stroke(width = 1.2.dp.toPx())
            )
        }
    }
}
