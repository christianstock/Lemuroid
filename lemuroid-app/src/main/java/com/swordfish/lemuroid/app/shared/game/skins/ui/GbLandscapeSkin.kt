package com.swordfish.lemuroid.app.shared.game.skins.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import com.swordfish.lemuroid.app.shared.game.skins.GbSkin

/**
 * GB skin renderer for landscape mode with punch-a-hole strategy.
 */
@Composable
fun GbLandscapeSkin(
    skin: GbSkin,
    gameScreenContent: @Composable () -> Unit,
    leftPad: @Composable (Modifier) -> Unit,
    rightPad: @Composable (Modifier) -> Unit,
    interactiveBar: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bezelRect = remember { mutableStateOf<Rect?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer {
                compositingStrategy = CompositingStrategy.Offscreen
            }
            .drawBehind {
                drawRect(color = skin.caseColor)
                bezelRect.value?.let {
                    drawRect(
                        color = Color.Transparent,
                        topLeft = it.topLeft,
                        size = it.size,
                        blendMode = BlendMode.Clear
                    )
                }
            }
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .border(2.dp, Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
        ) {
            Box(
                modifier = Modifier
                    .weight(0.25f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                leftPad(Modifier.fillMaxSize())
            }

            Box(
                modifier = Modifier
                    .weight(0.5f)
                    .fillMaxHeight()
                    .onGloballyPositioned {
                        bezelRect.value = it.boundsInParent()
                    },
                contentAlignment = Alignment.Center
            ) {
                gameScreenContent()
            }

            Box(
                modifier = Modifier
                    .weight(0.25f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                rightPad(Modifier.fillMaxSize())
            }
        }

        Box(modifier = Modifier.fillMaxWidth().height(84.dp)) {
            interactiveBar()
        }
    }
}
