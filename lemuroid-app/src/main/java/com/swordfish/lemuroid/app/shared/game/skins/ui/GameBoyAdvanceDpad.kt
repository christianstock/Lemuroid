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
import com.swordfish.lemuroid.app.shared.game.skins.GameBoyAdvanceSkin
import com.swordfish.lemuroid.app.shared.game.skins.GameBoyColorSkin
import com.swordfish.lemuroid.app.shared.game.skins.GameBoyModel
import com.swordfish.lemuroid.app.shared.game.skins.GameBoySkin
import com.swordfish.touchinput.radial.LocalLemuroidPadTheme
import com.swordfish.touchinput.radial.controls.GbControlCross
import com.swordfish.touchinput.radial.layouts.shared.ComposeTouchLayouts
import gg.padkit.PadKitScope
import gg.padkit.ids.Id

@Composable
fun PadKitScope.GameBoyAdvanceDpad(
    skin: GameBoyAdvanceSkin,
    modifier: Modifier = Modifier,
) {
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
                            drawGameBoyAdvanceBackground()
                        }
                )
            }
        },
        foreground = { directionState ->
            GameBoyAdvanceDpadForeground(
                directionState = directionState,
                skin = skin,
            )
        }
    )
}

@Composable
private fun GameBoyAdvanceDpadForeground(
    directionState: State<Offset>,
    modifier: Modifier = Modifier,
    skin: GameBoyAdvanceSkin,
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
                    )
                }
        )
    }
}

private fun DrawScope.drawGameBoyAdvanceBackground() {
}

private fun DrawScope.drawDpadForeground(
    dpadColor: Color,
    isUpPressed: Boolean,
    isDownPressed: Boolean,
    isLeftPressed: Boolean,
    isRightPressed: Boolean,
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
