package com.swordfish.lemuroid.app.shared.game.skins.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swordfish.lemuroid.app.shared.game.skins.GameBoySkin

@Composable
fun GameBoyActionButtonForeground(
    pressed: State<Boolean>,
    label: String,
    skin: GameBoySkin,
    modifier: Modifier = Modifier,
    rotation: Float = 0.0f,
) {
    val isPressed = pressed.value

    val baseColor = skin.actionButtonColor
    val buttonColor = if (isPressed) {
        baseColor.adjustBrightness(0.1f)
    } else {
        baseColor
    }

    val outlineAlpha = if (isPressed) 0.3f else 0.2f
    val outlineColor = baseColor.adjustBrightness(outlineAlpha)

    Column(
        modifier = modifier.graphicsLayer { rotationZ = rotation },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(
                    color = buttonColor,
                    shape = CircleShape
                )
                .border(
                    width = 2.dp,
                    color = outlineColor,
                    shape = CircleShape
                )
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label,
            color = skin.labelColor,
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.SansSerif,
            textAlign = TextAlign.Center,
            maxLines = 1,
            softWrap = false,
            modifier = Modifier
                .layout { measurable, constraints ->
                    val placeable = measurable.measure(constraints.copy(maxWidth = Int.MAX_VALUE))
                    layout(constraints.maxWidth, placeable.height) {
                        val xOffset = (constraints.maxWidth - placeable.width) / 2
                        placeable.place(xOffset, 0)
                    }
                }
        )
    }
}
