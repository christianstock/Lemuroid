package com.swordfish.lemuroid.app.shared.game.skins.ui

import android.view.KeyEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swordfish.lemuroid.app.shared.game.skins.GameBoyAdvanceSkin
import com.swordfish.touchinput.radial.controls.GBControlButton
import gg.padkit.PadKitScope
import gg.padkit.ids.Id

private val LABEL_CONTAINER_WIDTH = 50.dp
private val BUTTON_SIZE = 30.dp
private val HORIZONTAL_PADDING = 2.dp
private val VERTICAL_PADDING = 2.dp

@Composable
fun PadKitScope.GameBoyAdvanceMenuButton(
    id: Id.Key,
    label: String,
    skin: GameBoyAdvanceSkin,
    modifier: Modifier = Modifier,
    rotation: Float = 10f,
    labelScale: Float = 1.0f,
) {
    Box(
        modifier = modifier
            .graphicsLayer {
                rotationZ = rotation
            },
        contentAlignment = Alignment.Center,
    ) {
        GBControlButton(
            id = id,
            background = {
                GameBoyAdvanceMenuButtonBackground(
                    label = label,
                    labelScale = labelScale,
                )
            },
            foreground = { pressed ->
                GameBoyAdvanceMenuButtonForeground(
                    label = label,
                    labelScale = labelScale,
                    pressed = pressed,
                    skin = skin,
                )
            }
        )
    }
}

@Composable
fun GameBoyAdvanceMenuButtonBackground(
    label: String,
    modifier: Modifier = Modifier,
    labelScale: Float = 1.0f,
) {
    Row(
        modifier = modifier
            .background(
                color = Color.Black.copy(alpha = 0.1f),
                shape = RoundedCornerShape(percent = 50)
            )
            .padding(
                start = HORIZONTAL_PADDING * 4,
                end = HORIZONTAL_PADDING,
                top = VERTICAL_PADDING,
                bottom = VERTICAL_PADDING
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = label,
            modifier = Modifier.width(LABEL_CONTAINER_WIDTH),
            textAlign = TextAlign.Center,
            maxLines = 1,
            softWrap = false,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.SansSerif,
            color = Color.White.copy(alpha = 0.1f),
            fontSize = (12f * labelScale).sp
        )

        Spacer(modifier = Modifier.width(4.dp))

        // Recessed slot background
        Box(
            modifier = Modifier.size(BUTTON_SIZE)
        )
    }
}

@Composable
fun GameBoyAdvanceMenuButtonForeground(
    label: String,
    pressed: State<Boolean>,
    skin: GameBoyAdvanceSkin,
    modifier: Modifier = Modifier,
    labelScale: Float = 1.0f,
) {
    val isPressed = pressed.value
    val baseColor = skin.menuButtonColor
    val buttonColor = if (isPressed) baseColor.adjustBrightness(-0.15f) else baseColor

    Row(
        modifier = modifier
            .padding(
                start = HORIZONTAL_PADDING * 4,
                end = HORIZONTAL_PADDING,
                top = VERTICAL_PADDING,
                bottom = VERTICAL_PADDING
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        // Invisible text matching the exact line-height, font size, and vertical alignment of the background
        Text(
            text = label,
            modifier = Modifier
                .width(LABEL_CONTAINER_WIDTH)
                .alpha(0f),
            textAlign = TextAlign.Center,
            maxLines = 1,
            softWrap = false,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.SansSerif,
            fontSize = (10f * labelScale).sp
        )

        Spacer(modifier = Modifier.width(4.dp))

        // Foreground button sharing the exact same baseline and vertical placement
        Box(
            modifier = Modifier
                .size(BUTTON_SIZE)
                .background(
                    color = buttonColor,
                    shape = CircleShape
                )
        )
    }
}
