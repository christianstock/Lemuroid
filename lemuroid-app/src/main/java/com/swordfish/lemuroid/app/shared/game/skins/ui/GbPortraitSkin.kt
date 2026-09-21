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

@Composable
fun GbPortraitSkin(
    skin: GbSkin,
    gameScreen: @Composable () -> Unit,
    leftPad: @Composable (Modifier) -> Unit,
    rightPad: @Composable (Modifier) -> Unit,
    actionBar: @Composable () -> Unit,
    gameScreenPos: Rect?,
    modifier: Modifier = Modifier,
) {
    val bezelRect = remember { mutableStateOf<Rect?>(null) }
    val rootOffset = remember { mutableStateOf(Offset.Zero) }

    val gameScreenRect = remember(gameScreenPos, rootOffset.value) {
        gameScreenPos?.translate(-rootOffset.value)
    }

    val deviceModifier = modifier
        .fillMaxSize()
        .onGloballyPositioned {
            rootOffset.value = it.boundsInRoot().topLeft
        }
        .graphicsLayer {
            compositingStrategy = CompositingStrategy.Offscreen
        }
        .drawBehind {
            GbArt.run {
                drawHandheld(gameScreenRect, bezelRect.value, skin, false)
            }
        }

    val gameScreenModifier = Modifier
        .fillMaxWidth()
        .aspectRatio(160f / 144f)
        .padding(16.dp)
        .onGloballyPositioned {
            bezelRect.value = it.boundsInParent()
        }

    val controlsModifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 8.dp)

    val actionBarModifier = Modifier
        .fillMaxWidth()
        .height(56.dp)
        .padding(horizontal = 12.dp)

    Column(
        modifier = deviceModifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
        )

        Box(
            modifier = gameScreenModifier,
            contentAlignment = Alignment.Center
        ) {
            gameScreen()
        }

        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        )

        Row(
            modifier = controlsModifier.weight(1f),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            leftPad(Modifier.weight(1f))
            rightPad(Modifier.weight(1f))
        }

        Box(
            modifier = actionBarModifier,
            contentAlignment = Alignment.Center
        ) {
            actionBar()
        }
    }
}
