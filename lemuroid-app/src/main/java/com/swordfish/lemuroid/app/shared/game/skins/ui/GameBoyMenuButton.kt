package com.swordfish.lemuroid.app.shared.game.skins.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swordfish.lemuroid.app.shared.game.skins.GameBoySkin
import com.swordfish.touchinput.radial.LocalLemuroidPadTheme
import com.swordfish.touchinput.radial.controls.GBControlButton
import gg.padkit.PadKitScope
import gg.padkit.ids.Id

@Composable
fun PadKitScope.GameBoyMenuButton(
    id: Id.Key,
    label: String,
    skin: GameBoySkin,
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
                GameBoyMenuButtonBackground(
                    label = label,
                    labelScale = labelScale,
                    expansion = expansion,
                )
            },
            foreground = { pressed ->
                GameBoyMenuButtonForeground(
                    pressed = pressed,
                    skin = skin,
                )
            }
        )
    }
}

@Composable
fun GameBoyMenuButtonBackground(
    modifier: Modifier = Modifier,
    label: String? = null,
    labelScale: Float = 1.0f,
    expansion: Float = 0.0f,
) {
    Column(
        modifier = modifier.fillMaxWidth(0.7f),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        GameBoyMenuButtonBevel(
            modifier = Modifier.fillMaxWidth(),
            expansion = expansion,
        )

        Spacer(modifier = Modifier.height(2.dp))

        if (label != null) {
            GameBoyMenuButtonLabel(
                label = label,
                labelScale = labelScale,
            )
        }
    }
}

@Composable
fun GameBoyMenuButtonBevel(
    modifier: Modifier = Modifier,
    expansion: Float = 0.0f,
) {
    Box(
        modifier = modifier
            .fillMaxWidth(0.7f)
            .aspectRatio(2.6f)
            .drawBehind {
                val expansionPx = expansion.dp.toPx()
                val outerWidth = size.width + (expansionPx * 2)
                val outerHeight = size.height + (expansionPx * 2)
                val cornerRadius = outerHeight / 2f

                drawRoundRect(
                    color = Color.Black.copy(alpha = 0.05f),
                    topLeft = Offset(-expansionPx, -expansionPx),
                    size = Size(outerWidth, outerHeight),
                    cornerRadius = CornerRadius(cornerRadius, cornerRadius)
                )
            }
    )
}

@Composable
fun GameBoyMenuButtonLabel(
    label: String,
    modifier: Modifier = Modifier,
    labelScale: Float = 1.0f,
) {
    val theme = LocalLemuroidPadTheme.current
    Text(
        text = label,
        modifier = modifier.layout { measurable, constraints ->
            val placeable = measurable.measure(constraints.copy(maxWidth = Int.MAX_VALUE))
            layout(constraints.maxWidth, placeable.height) {
                val xOffset = (constraints.maxWidth - placeable.width) / 2
                placeable.place(xOffset, 0)
            }
        },
        textAlign = TextAlign.Center,
        maxLines = 1,
        softWrap = false,
        fontWeight = FontWeight.Black,
        fontFamily = FontFamily.SansSerif,
        color = theme.labelColor ?: Color(0xFF3639a0),
        fontSize = (16f * labelScale).sp
    )
}

@Composable
fun GameBoyMenuButtonForeground(
    pressed: State<Boolean>,
    modifier: Modifier = Modifier,
    skin: GameBoySkin,
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
        modifier = modifier.fillMaxWidth(0.7f),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2.6f)
                .background(
                    color = buttonColor,
                    shape = RoundedCornerShape(percent = 50)
                )
                .border(
                    width = 2.dp,
                    color = outlineColor,
                    shape = RoundedCornerShape(percent = 50)
                )
        )

        Spacer(modifier = Modifier.height(2.dp))
    }
}

val Color.isVeryDark: Boolean
    get() {
        val maxChannel = maxOf(red, green, blue)
        return maxChannel < 0.25f
    }

fun Color.adjustBrightness(alpha: Float): Color {
    val overlayColor = if (isVeryDark) Color.White else Color.Black
    return overlayColor.copy(alpha = alpha).compositeOver(this)
}

