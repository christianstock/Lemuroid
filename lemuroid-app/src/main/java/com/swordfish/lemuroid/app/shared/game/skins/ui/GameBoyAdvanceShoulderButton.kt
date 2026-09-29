package com.swordfish.lemuroid.app.shared.game.skins.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swordfish.touchinput.radial.controls.GBControlButton
import gg.padkit.PadKitScope
import gg.padkit.ids.Id

enum class ShoulderSide { LEFT, RIGHT }

@Composable
fun PadKitScope.GameBoyAdvanceShoulderButton(
    id: Id.Key,
    side: ShoulderSide,
    modifier: Modifier = Modifier,
    label: String? = null,
    buttonColor: Color = Color(0xFF8B8B98),
) {
    Box(
        modifier = modifier
            .requiredWidth(250.dp)
            .requiredHeight(50.dp),
        contentAlignment = Alignment.Center
    ) {
        GBControlButton(
            modifier = Modifier
                .requiredWidth(250.dp)
                .requiredHeight(50.dp),
            id = id,
            background = { _ ->  },
            foreground = { pressed ->
                // Custom path fill pass (meets in center at a sharp point)
                GbaShoulderForeground(
                    side = side,
                    pressed = pressed,
                    label = label,
                    buttonColor = buttonColor,
                )
            }
        )
    }
}

@Composable
private fun GbaShoulderForeground(
    side: ShoulderSide,
    pressed: State<Boolean>,
    label: String?,
    buttonColor: Color,
) {
    val isPressed = pressed.value
    val drawColor = if (isPressed) buttonColor.adjustBrightness(-0.2f) else buttonColor

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            val outerH = h * 0.90f
            val outerRadius = h * 0.25f

            val path = Path().apply {
                if (side == ShoulderSide.LEFT) {
                    // LEFT: Meets at a single sharp point at top-right (w, 0f)
                    moveTo(w, 0f)
                    lineTo(outerRadius, 0f)
                    quadraticTo(0f, 0f, 0f, outerRadius)
                    lineTo(0f, outerH)
                    cubicTo(
                        x1 = w * 0.25f, y1 = outerH,
                        x2 = w * 0.65f, y2 = 0f,
                        x3 = w, y3 = 0f
                    )
                    close()
                } else {
                    // RIGHT: Meets at a single sharp point at top-left (0f, 0f)
                    moveTo(0f, 0f)
                    lineTo(w - outerRadius, 0f)
                    quadraticTo(w, 0f, w, outerRadius)
                    lineTo(w, outerH)
                    cubicTo(
                        x1 = w * 0.75f, y1 = outerH,
                        x2 = w * 0.35f, y2 = 0f,
                        x3 = 0f, y3 = 0f
                    )
                    close()
                }
            }

            // Pure solid fill pass (no stroke/outline)
            drawIntoCanvas { canvas ->
                val fillPaint = Paint().apply {
                    color = drawColor
                    style = PaintingStyle.Fill
                    isAntiAlias = true
                }
                canvas.drawPath(path, fillPaint)
            }
        }

        if (label != null) {
            Text(
                text = label,
                modifier = Modifier
                    .graphicsLayer {
                        translationX = if (side == ShoulderSide.LEFT) -170f else 170f
                        translationY = -10f
                    },
                color = Color.Black.copy(alpha = 0.2f),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif
            )
        }
    }
}

@Composable
private fun GbaShoulderBackground(
    side: ShoulderSide,
    expansion: Dp = 2.dp
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val exp = expansion.toPx()
        val w = size.width
        val h = size.height
        val outerH = h * 0.90f + exp

        val path = Path().apply {
            if (side == ShoulderSide.LEFT) {
                moveTo(w, -exp)
                lineTo(-exp, -exp)
                lineTo(-exp, outerH)
                cubicTo(
                    x1 = w * 0.25f, y1 = outerH,
                    x2 = w * 0.65f, y2 = 0f,
                    x3 = w, y3 = 0f
                )
                close()
            } else {
                moveTo(0f, -exp)
                lineTo(w + exp, -exp)
                lineTo(w + exp, outerH)
                cubicTo(
                    x1 = w * 0.75f, y1 = outerH,
                    x2 = w * 0.35f, y2 = 0f,
                    x3 = 0f, y3 = 0f
                )
                close()
            }
        }

        drawPath(path = path, color = Color.Black.copy(alpha = 0.2f))
    }
}


