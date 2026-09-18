package com.swordfish.lemuroid.app.shared.game.skins.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import com.swordfish.lemuroid.app.shared.game.skins.GbSkin
import com.swordfish.lemuroid.app.shared.game.skins.art.GbArt
import com.swordfish.lemuroid.app.shared.game.skins.art.GbaArt.drawHandheld

@Composable
fun GbPortraitSkin(
    skin: GbSkin,
    gameScreenContent: @Composable () -> Unit,
    leftPad: @Composable (Modifier) -> Unit,
    rightPad: @Composable (Modifier) -> Unit,
    interactiveBar: @Composable () -> Unit,
    viewportPositionInRoot: Rect?,
    modifier: Modifier = Modifier,
) {
    val bezelRect = remember { mutableStateOf<Rect?>(null) }
    val rootOffset = remember { mutableStateOf(Offset.Zero) }

    val gameRect = remember(viewportPositionInRoot, rootOffset.value) {
        viewportPositionInRoot?.translate(-rootOffset.value)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned { rootOffset.value = it.boundsInRoot().topLeft }
            .graphicsLayer {
                compositingStrategy = CompositingStrategy.Offscreen
            }
            .drawBehind {
                GbArt.run {
                    drawHandheld(skin.caseColor, bezelRect.value, gameRect,false)
                }
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.fillMaxWidth().height(64.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(160f / 144f)
                .padding(16.dp)
                .onGloballyPositioned {
                    bezelRect.value = it.boundsInParent()
                },
            contentAlignment = Alignment.Center
        ) {
            gameScreenContent()
        }

        Spacer(modifier = Modifier.fillMaxWidth().height(48.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            leftPad(Modifier.weight(1f))
            rightPad(Modifier.weight(1f))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            interactiveBar()
        }
    }
}
