package com.swordfish.lemuroid.app.shared.game.skins.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.swordfish.lemuroid.app.shared.game.skins.GameBoyModel
import com.swordfish.lemuroid.app.shared.game.skins.GameBoySkin
import com.swordfish.touchinput.radial.LocalLemuroidPadTheme
import com.swordfish.touchinput.radial.controls.GbControlCross
import com.swordfish.touchinput.radial.layouts.shared.ComposeTouchLayouts
import gg.padkit.PadKitScope
import gg.padkit.ids.Id

@Composable
fun PadKitScope.GameBoyDpad(
    skin: GameBoySkin,
    modifier: Modifier = Modifier,
) {
    val isDmg01 = skin.model == GameBoyModel.DMG_01

    GbControlCross(
        modifier = modifier
            .size(160.dp)
            .padding(4.dp),
        id = Id.DiscreteDirection(ComposeTouchLayouts.MOTION_SOURCE_DPAD),
        allowDiagonals = true,
        background = {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(125.dp)
                        .drawBehind {
                            if (isDmg01) {
                                drawDmg01Background()
                            } else {
                                drawPocketColorBackground()
                            }
                        }
                )
            }
        },
        foreground = { directionState ->
            GameBoyDpadForeground(
                isDmg01 = isDmg01,
                directionState = directionState,
                skin = skin,
            )
        }
    )
}

@Composable
private fun GameBoyDpadForeground(
    isDmg01: Boolean,
    directionState: State<Offset>,
    modifier: Modifier = Modifier,
    skin: GameBoySkin,
) {
    val touchOffset = directionState.value
    val isPressed = touchOffset != Offset.Zero

    val isUpPressed = touchOffset.y > 0.1f
    val isDownPressed = touchOffset.y < -0.1f
    val isLeftPressed = touchOffset.x < -0.1f
    val isRightPressed = touchOffset.x > 0.1f

    val dpadColor = skin.dPadColor

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(125.dp)
                .drawBehind {
                    drawDpadForeground(
                        dpadColor = dpadColor,
                        isUpPressed = isUpPressed,
                        isDownPressed = isDownPressed,
                        isLeftPressed = isLeftPressed,
                        isRightPressed = isRightPressed,
                        bars = isDmg01,
                    )
                }
        )
    }
}

private fun DrawScope.drawDmg01Background() {
    val centerX = size.width / 2f
    val centerY = size.height / 2f

    // Circular recess
    val circleRadius = size.minDimension / 1.6f
    drawCircle(
        color = Color.Black.copy(alpha = 0.05f),
        radius = circleRadius,
        center = Offset(centerX, centerY)
    )

    // Arrow clearing shapes
    val triWidth = 10.dp.toPx()
    val triHeight = 8.dp.toPx()
    val armOffset = 4.dp.toPx()

    val topTriPath = Path().apply {
        val baseY = -armOffset
        moveTo(centerX - triWidth / 2f, baseY)
        lineTo(centerX + triWidth / 2f, baseY)
        lineTo(centerX, baseY - triHeight)
        close()
    }

    val bottomTriPath = Path().apply {
        val baseY = size.height + armOffset
        moveTo(centerX - triWidth / 2f, baseY)
        lineTo(centerX + triWidth / 2f, baseY)
        lineTo(centerX, baseY + triHeight)
        close()
    }

    val leftTriPath = Path().apply {
        val baseX = -armOffset
        moveTo(baseX, centerY - triWidth / 2f)
        lineTo(baseX, centerY + triWidth / 2f)
        lineTo(baseX - triHeight, centerY)
        close()
    }

    val rightTriPath = Path().apply {
        val baseX = size.width + armOffset
        moveTo(baseX, centerY - triWidth / 2f)
        lineTo(baseX, centerY + triWidth / 2f)
        lineTo(baseX + triHeight, centerY)
        close()
    }

    listOf(topTriPath, bottomTriPath, leftTriPath, rightTriPath).forEach { triPath ->
        drawPath(
            path = triPath,
            color = Color.Black.copy(alpha = 0.1f),
        )
    }
}

private fun DrawScope.drawPocketColorBackground() {
    val centerX = size.width / 2f
    val centerY = size.height / 2f
    val dotRadius = 4.dp.toPx()
    val armOffset = 8.dp.toPx()
    val dotColor = Color.Black.copy(alpha = 0.08f)

    drawCircle(
        color = dotColor,
        radius = dotRadius,
        center = Offset(centerX, -armOffset)
    )

    drawCircle(
        color = dotColor,
        radius = dotRadius,
        center = Offset(centerX, size.height + armOffset)
    )

    drawCircle(
        color = dotColor,
        radius = dotRadius,
        center = Offset(-armOffset, centerY)
    )

    drawCircle(
        color = dotColor,
        radius = dotRadius,
        center = Offset(size.width + armOffset, centerY)
    )
}

private fun DrawScope.drawDpadForeground(
    dpadColor: Color,
    isUpPressed: Boolean,
    isDownPressed: Boolean,
    isLeftPressed: Boolean,
    isRightPressed: Boolean,
    bars: Boolean,
) {
    val crossWidth = size.width * 0.33f
    val crossHeight = size.height * 0.33f
    val centerX = size.width / 2f
    val centerY = size.height / 2f

    val isAnyPressed = isUpPressed || isDownPressed || isLeftPressed || isRightPressed

    val cornerRadius = 4.dp.toPx()
    val crossPath = createRoundedDpadCrossPath(
        width = size.width,
        height = size.height,
        crossWidth = crossWidth,
        crossHeight = crossHeight,
        centerX = centerX,
        centerY = centerY,
    )

    drawPath(
        path = crossPath,
        color = dpadColor,
    )

    val outlineAlpha = if (isAnyPressed) 0.3f else 0.2f
    val outlineColor = dpadColor.adjustBrightness(outlineAlpha)
    val outlineWidth = 2.dp.toPx()

    drawPath(
        path = crossPath,
        color = outlineColor,
        style = Stroke(
            width = outlineWidth,
            pathEffect = PathEffect.cornerPathEffect(cornerRadius)
        )
    )

    if (bars) {
        drawBarAccents(
            crossWidth = crossWidth,
            centerX = centerX,
            centerY = centerY,
            isUpPressed = isUpPressed,
            isDownPressed = isDownPressed,
            isLeftPressed = isLeftPressed,
            isRightPressed = isRightPressed
        )
    } else {
        drawTriangleAccents(
            crossWidth = crossWidth,
            crossHeight = crossHeight,
            centerX = centerX,
            centerY = centerY,
            isUpPressed = isUpPressed,
            isDownPressed = isDownPressed,
            isLeftPressed = isLeftPressed,
            isRightPressed = isRightPressed
        )
    }

    drawCircle(
        color = Color.Black.copy(alpha = 0.2f),
        radius = crossWidth / 2.2f,
        center = Offset(centerX, centerY)
    )
}

private fun createRoundedDpadCrossPath(
    width: Float,
    height: Float,
    crossWidth: Float,
    crossHeight: Float,
    centerX: Float,
    centerY: Float,
    armVal: Float = 1.8f
): Path {
    val rawPath = Path().apply {
        // Top arm
        moveTo(centerX - crossWidth / armVal, 0f)
        lineTo(centerX + crossWidth / armVal, 0f)
        lineTo(centerX + crossWidth / armVal, centerY - crossHeight / armVal)
        // Right arm
        lineTo(width, centerY - crossHeight / armVal)
        lineTo(width, centerY + crossHeight / armVal)
        lineTo(centerX + crossWidth / armVal, centerY + crossHeight / armVal)
        // Bottom arm
        lineTo(centerX + crossWidth / armVal, height)
        lineTo(centerX - crossWidth / armVal, height)
        lineTo(centerX - crossWidth / armVal, centerY + crossHeight / armVal)
        // Left arm
        lineTo(0f, centerY + crossHeight / armVal)
        lineTo(0f, centerY - crossHeight / armVal)
        lineTo(centerX - crossWidth / armVal, centerY - crossHeight / armVal)
        close()
    }

    return Path().apply {
        addPath(rawPath)
    }
}

private fun DrawScope.drawBarAccents(
    crossWidth: Float,
    centerX: Float,
    centerY: Float,
    isUpPressed: Boolean,
    isDownPressed: Boolean,
    isLeftPressed: Boolean,
    isRightPressed: Boolean,
) {
    val lineStrokeWidth = 3.dp.toPx()
    val lineLength = crossWidth * 0.8f
    val lineSpacing = 10.dp.toPx()
    val defaultColor = Color.Black.copy(alpha = 0.3f)
    val pressedColor = Color.Black.copy(alpha = 0.6f)
    val edgeOffset = 8.dp.toPx()

    data class BarArmSpec(
        val isPressed: Boolean,
        val isHorizontal: Boolean,
        val startOffset: Float,
        val stepMultiplier: Float, // 1f for growing inwards, -1f for growing upwards/leftwards
    )

    val arms = arrayOf(
        BarArmSpec(isUpPressed, isHorizontal = true, startOffset = edgeOffset, stepMultiplier = 1f),
        BarArmSpec(isDownPressed, isHorizontal = true, startOffset = size.height - edgeOffset, stepMultiplier = -1f),
        BarArmSpec(isLeftPressed, isHorizontal = false, startOffset = edgeOffset, stepMultiplier = 1f),
        BarArmSpec(isRightPressed, isHorizontal = false, startOffset = size.width - edgeOffset, stepMultiplier = -1f),
    )

    arms.forEach { arm ->
        val color = if (arm.isPressed) pressedColor else defaultColor

        repeat(3) { i ->
            val pos = arm.startOffset + (i * lineSpacing * arm.stepMultiplier)

            val (start, end) = if (arm.isHorizontal) {
                Offset(centerX - lineLength / 2f, pos) to Offset(centerX + lineLength / 2f, pos)
            } else {
                Offset(pos, centerY - lineLength / 2f) to Offset(pos, centerY + lineLength / 2f)
            }

            drawLine(
                color = color,
                start = start,
                end = end,
                strokeWidth = lineStrokeWidth
            )
        }
    }
}

private fun DrawScope.drawTriangleAccents(
    crossWidth: Float,
    crossHeight: Float,
    centerX: Float,
    centerY: Float,
    isUpPressed: Boolean,
    isDownPressed: Boolean,
    isLeftPressed: Boolean,
    isRightPressed: Boolean,
) {
    val arrowWidth = crossWidth * 0.6f
    val arrowHeight = crossHeight * 0.63f
    val defaultColor = Color.Black.copy(alpha = 0.25f)
    val pressedColor = Color.Black.copy(alpha = 0.55f)
    val edgeOffset = 6.dp.toPx()

    data class TriangleArmSpec(
        val isPressed: Boolean,
        val tip: Offset,
        val baseLeft: Offset,
        val baseRight: Offset,
    )

    val arms = arrayOf(
        // Top Arrow
        TriangleArmSpec(
            isPressed = isUpPressed,
            tip = Offset(centerX, edgeOffset),
            baseLeft = Offset(centerX - arrowWidth / 2f, edgeOffset + arrowHeight),
            baseRight = Offset(centerX + arrowWidth / 2f, edgeOffset + arrowHeight)
        ),
        // Bottom Arrow
        TriangleArmSpec(
            isPressed = isDownPressed,
            tip = Offset(centerX, size.height - edgeOffset),
            baseLeft = Offset(centerX - arrowWidth / 2f, size.height - edgeOffset - arrowHeight),
            baseRight = Offset(centerX + arrowWidth / 2f, size.height - edgeOffset - arrowHeight)
        ),
        // Left Arrow
        TriangleArmSpec(
            isPressed = isLeftPressed,
            tip = Offset(edgeOffset, centerY),
            baseLeft = Offset(edgeOffset + arrowHeight, centerY - arrowWidth / 2f),
            baseRight = Offset(edgeOffset + arrowHeight, centerY + arrowWidth / 2f)
        ),
        // Right Arrow
        TriangleArmSpec(
            isPressed = isRightPressed,
            tip = Offset(size.width - edgeOffset, centerY),
            baseLeft = Offset(size.width - edgeOffset - arrowHeight, centerY - arrowWidth / 2f),
            baseRight = Offset(size.width - edgeOffset - arrowHeight, centerY + arrowWidth / 2f)
        )
    )

    arms.forEach { arm ->
        val arrowPath = Path().apply {
            moveTo(arm.tip.x, arm.tip.y)
            lineTo(arm.baseLeft.x, arm.baseLeft.y)
            lineTo(arm.baseRight.x, arm.baseRight.y)
            close()
        }

        drawPath(
            path = arrowPath,
            color = if (arm.isPressed) pressedColor else defaultColor
        )
    }
}
