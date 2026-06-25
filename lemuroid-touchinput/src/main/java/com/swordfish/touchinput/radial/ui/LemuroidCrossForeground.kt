package com.swordfish.touchinput.radial.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.swordfish.touchinput.radial.LocalLemuroidPadTheme
import gg.padkit.ui.DefaultCrossForeground

@Composable
fun LemuroidCrossForeground(
    allowDiagonals: Boolean,
    directionState: State<Offset>,
) {
    val theme = LocalLemuroidPadTheme.current
    val isPressed = directionState.value != Offset.Zero

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                val crossWidth = size.width * 0.33f
                val crossHeight = size.height * 0.33f
                val centerX = size.width / 2f
                val centerY = size.height / 2f

                val crossPath = Path().apply {
                    // Top arm
                    moveTo(centerX - crossWidth / 2f, 0f)
                    lineTo(centerX + crossWidth / 2f, 0f)
                    lineTo(centerX + crossWidth / 2f, centerY - crossHeight / 2f)
                    // Right arm
                    lineTo(size.width, centerY - crossHeight / 2f)
                    lineTo(size.width, centerY + crossHeight / 2f)
                    lineTo(centerX + crossWidth / 2f, centerY + crossHeight / 2f)
                    // Bottom arm
                    lineTo(centerX + crossWidth / 2f, size.height)
                    lineTo(centerX - crossWidth / 2f, size.height)
                    lineTo(centerX - crossWidth / 2f, centerY + crossHeight / 2f)
                    // Left arm
                    lineTo(0f, centerY + crossHeight / 2f)
                    lineTo(0f, centerY - crossHeight / 2f)
                    lineTo(centerX - crossWidth / 2f, centerY - crossHeight / 2f)
                    close()
                }

                // Draw bevel shadow
                theme.bevelColorDark?.let {
                    drawPath(
                        path = crossPath,
                        color = it,
                        alpha = if (isPressed) 0.8f else 0.5f
                    )
                }

                // Draw main cross
                drawPath(
                    path = crossPath,
                    color = theme.foregroundFill(isPressed),
                )

                // Draw bevel highlight
                theme.bevelColorLight?.let {
                    drawPath(
                        path = crossPath,
                        color = it,
                        style = Stroke(width = 1.dp.toPx())
                    )
                }

                // Draw center indentation
                drawCircle(
                    color = Color.Black.copy(alpha = 0.1f),
                    radius = crossWidth / 3f,
                    center = Offset(centerX, centerY)
                )
            }
    )
}
