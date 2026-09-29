package com.swordfish.lemuroid.app.shared.game.skins.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import com.swordfish.lemuroid.app.shared.game.skins.GameBoyColorSkin
import com.swordfish.lemuroid.app.shared.game.skins.GameBoyModel
import com.swordfish.lemuroid.app.shared.game.skins.GameBoySkin

@Composable
fun GameBoyColorActionButtonForeground(
    pressed: State<Boolean>,
    label: String,
    skin: GameBoyColorSkin,
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
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = label,
                color = Color.Black.copy(alpha = 0.8f),
                fontSize = 50.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = 0.75f
                    }
                    .offset(
                        x = (-2).dp,
                        y = 4.dp
                    )
            )
        }
    }
}
