package com.swordfish.touchinput.radial.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
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

@Composable
fun GbCrossForeground(
    allowDiagonals: Boolean,
    directionState: State<Offset>,
) {
    val theme = LocalLemuroidPadTheme.current
    val isPressed = directionState.value != Offset.Zero

    val touchOffset = directionState.value

    val isUpPressed = touchOffset.y > 0.1f
    val isDownPressed = touchOffset.y < -0.1f
    val isLeftPressed = touchOffset.x < -0.1f
    val isRightPressed = touchOffset.x > 0.1f

    // Outer box lets PadKit give it full layout space...
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // FIXED: ...but this inner Box shrinks the physical D-Pad drawing canvas down perfectly!
        Box(
            modifier = Modifier
                .size(125.dp) // Adjust this value up or down to change the size of the cross!
                .drawBehind {
                    val crossWidth = size.width * 0.33f
                    val crossHeight = size.height * 0.33f
                    val centerX = size.width / 2f
                    val centerY = size.height / 2f

                    // --- 1. Core D-Pad Path Construction ---
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
                        //color = theme.foregroundFill(isPressed),
                        color = Color(0xFF222222)
                    )

                    // Draw bevel highlight
                    theme.bevelColorLight?.let {
                        drawPath(
                            path = crossPath,
                            color = it,
                            style = Stroke(width = 1.dp.toPx())
                        )
                    }

                    // --- 2. Directional Triangles ---
                    val arrowWidth = crossWidth * 0.6f
                    val arrowHeight = crossHeight * 0.63f

                    val defaultColor = Color.Black.copy(alpha = 0.25f)
                    val pressedColor = Color.Black.copy(alpha = 0.55f)

                    // Pro-Tip: Scale the edge offset slightly down if you make the D-Pad tiny
                    val edgeOffset = 6.dp.toPx()

                    // Top Arrow (Points Up)
                    val topArrow = Path().apply {
                        moveTo(centerX, edgeOffset)
                        lineTo(centerX - arrowWidth / 2f, edgeOffset + arrowHeight)
                        lineTo(centerX + arrowWidth / 2f, edgeOffset + arrowHeight)
                        close()
                    }
                    drawPath(
                        path = topArrow,
                        color = if (isUpPressed) pressedColor else defaultColor
                    )

                    // Bottom Arrow (Points Down)
                    val bottomArrow = Path().apply {
                        moveTo(centerX, size.height - edgeOffset)
                        lineTo(centerX - arrowWidth / 2f, size.height - edgeOffset - arrowHeight)
                        lineTo(centerX + arrowWidth / 2f, size.height - edgeOffset - arrowHeight)
                        close()
                    }
                    drawPath(
                        path = bottomArrow,
                        color = if (isDownPressed) pressedColor else defaultColor
                    )

                    // Left Arrow (Points Left)
                    val leftArrow = Path().apply {
                        moveTo(edgeOffset, centerY)
                        lineTo(edgeOffset + arrowHeight, centerY - arrowWidth / 2f)
                        lineTo(edgeOffset + arrowHeight, centerY + arrowWidth / 2f)
                        close()
                    }
                    drawPath(
                        path = leftArrow,
                        color = if (isLeftPressed) pressedColor else defaultColor
                    )

                    // Right Arrow (Points Right)
                    val rightArrow = Path().apply {
                        moveTo(size.width - edgeOffset, centerY)
                        lineTo(size.width - edgeOffset - arrowHeight, centerY - arrowWidth / 2f)
                        lineTo(size.width - edgeOffset - arrowHeight, centerY + arrowWidth / 2f)
                        close()
                    }
                    drawPath(
                        path = rightArrow,
                        color = if (isRightPressed) pressedColor else defaultColor
                    )

                    // --- 3. Center Indentation ---
                    drawCircle(
                        color = Color.Black.copy(alpha = 0.1f),
                        radius = crossWidth / 3f,
                        center = Offset(centerX, centerY)
                    )
                }
        )
    }
}

@Composable
fun GbcCrossForeground(
    allowDiagonals: Boolean,
    directionState: State<Offset>,
) {
    val theme = LocalLemuroidPadTheme.current
    val isPressed = directionState.value != Offset.Zero

    val touchOffset = directionState.value

    val isUpPressed = touchOffset.y > 0.1f
    val isDownPressed = touchOffset.y < -0.1f
    val isLeftPressed = touchOffset.x < -0.1f
    val isRightPressed = touchOffset.x > 0.1f

    // Outer box lets PadKit give it full layout space...
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // FIXED: ...but this inner Box shrinks the physical D-Pad drawing canvas down perfectly!
        Box(
            modifier = Modifier
                .size(125.dp) // Adjust this value up or down to change the size of the cross!
                .drawBehind {
                    val crossWidth = size.width * 0.33f
                    val crossHeight = size.height * 0.33f
                    val centerX = size.width / 2f
                    val centerY = size.height / 2f

                    // --- 1. Core D-Pad Path Construction ---
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

                    // --- 2. Directional Triangles ---
                    val arrowWidth = crossWidth * 0.6f
                    val arrowHeight = crossHeight * 0.63f

                    val defaultColor = Color.Black.copy(alpha = 0.25f)
                    val pressedColor = Color.Black.copy(alpha = 0.55f)

                    // Pro-Tip: Scale the edge offset slightly down if you make the D-Pad tiny
                    val edgeOffset = 6.dp.toPx()

                    // Top Arrow (Points Up)
                    val topArrow = Path().apply {
                        moveTo(centerX, edgeOffset)
                        lineTo(centerX - arrowWidth / 2f, edgeOffset + arrowHeight)
                        lineTo(centerX + arrowWidth / 2f, edgeOffset + arrowHeight)
                        close()
                    }
                    drawPath(
                        path = topArrow,
                        color = if (isUpPressed) pressedColor else defaultColor
                    )

                    // Bottom Arrow (Points Down)
                    val bottomArrow = Path().apply {
                        moveTo(centerX, size.height - edgeOffset)
                        lineTo(centerX - arrowWidth / 2f, size.height - edgeOffset - arrowHeight)
                        lineTo(centerX + arrowWidth / 2f, size.height - edgeOffset - arrowHeight)
                        close()
                    }
                    drawPath(
                        path = bottomArrow,
                        color = if (isDownPressed) pressedColor else defaultColor
                    )

                    // Left Arrow (Points Left)
                    val leftArrow = Path().apply {
                        moveTo(edgeOffset, centerY)
                        lineTo(edgeOffset + arrowHeight, centerY - arrowWidth / 2f)
                        lineTo(edgeOffset + arrowHeight, centerY + arrowWidth / 2f)
                        close()
                    }
                    drawPath(
                        path = leftArrow,
                        color = if (isLeftPressed) pressedColor else defaultColor
                    )

                    // Right Arrow (Points Right)
                    val rightArrow = Path().apply {
                        moveTo(size.width - edgeOffset, centerY)
                        lineTo(size.width - edgeOffset - arrowHeight, centerY - arrowWidth / 2f)
                        lineTo(size.width - edgeOffset - arrowHeight, centerY + arrowWidth / 2f)
                        close()
                    }
                    drawPath(
                        path = rightArrow,
                        color = if (isRightPressed) pressedColor else defaultColor
                    )

                    // --- 3. Center Indentation ---
                    drawCircle(
                        color = Color.Black.copy(alpha = 0.1f),
                        radius = crossWidth / 3f,
                        center = Offset(centerX, centerY)
                    )
                }
        )
    }
}
