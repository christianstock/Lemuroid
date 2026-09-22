import android.view.KeyEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.swordfish.lemuroid.app.shared.game.skins.GbModel
import com.swordfish.lemuroid.app.shared.game.skins.GbSkin
import com.swordfish.lemuroid.app.shared.game.skins.art.GbArt
import com.swordfish.touchinput.radial.controls.GbControlFaceButtons
import com.swordfish.touchinput.radial.controls.GBControlButton
import com.swordfish.touchinput.radial.controls.GbControlCross
import com.swordfish.touchinput.radial.layouts.shared.ComposeTouchLayouts
import com.swordfish.touchinput.radial.settings.TouchControllerSettingsManager
import com.swordfish.touchinput.radial.ui.DmgRoundButtonForeground
import gg.padkit.PadKitScope
import gg.padkit.ids.Id
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import androidx.compose.ui.graphics.Path

@Composable
fun PadKitScope.GbPortraitSkin(
    skin: GbSkin,
    gameScreen: @Composable () -> Unit,
    actionBar: @Composable () -> Unit,
    touchControllerSettings: TouchControllerSettingsManager.Settings,
    gameScreenPos: Rect?,
    modifier: Modifier = Modifier,
) {
    val rootOffset = remember { mutableStateOf(Offset.Zero) }

    val gameScreenRect = remember(gameScreenPos, rootOffset.value) {
        gameScreenPos?.translate(-rootOffset.value)
    }

    val density = LocalDensity.current
    val topBezelPx = with(density) { 36.dp.toPx() }
    val bottomBezelPx = with(density) { 36.dp.toPx() }
    val sideBezelPx = with(density) { 50.dp.toPx() }

    val bezelRect = remember(gameScreenRect) {
        gameScreenRect?.let { rect ->
            Rect(
                left = rect.left - sideBezelPx,
                top = rect.top - topBezelPx,
                right = rect.right + sideBezelPx,
                bottom = rect.bottom + bottomBezelPx
            )
        }
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
                drawHandheld(gameScreenRect, bezelRect, skin, false)
            }
        }

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
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .aspectRatio(160f / 144f),
            contentAlignment = Alignment.Center
        ) {
            gameScreen()
        }

        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 12.dp)
        ) {
            GbControlCross(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .size(160.dp),
                id = Id.DiscreteDirection(ComposeTouchLayouts.MOTION_SOURCE_DPAD),
                bars = skin.model == GbModel.DMG,
                background = {
                    // Center-aligned inner Box to control physical background/canvas size
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(125.dp) // Adjust cross background size independently from the 160.dp touch target
                            .drawBehind {
                                val crossWidth = size.width * 0.33f
                                val crossHeight = size.height * 0.33f
                                val centerX = size.width / 2f
                                val centerY = size.height / 2f
                                val armVal = 1.8f

                                // --- 0. Background Circle & Alpha Reversion Triangles ---
                                // Draw full background circle
                                val circleRadius = size.minDimension / 1.6f
                                if (skin.model == GbModel.DMG) {
                                    drawCircle(
                                        color = Color.Black.copy(alpha = 0.05f),
                                        radius = circleRadius,
                                        center = Offset(centerX, centerY)
                                    )


                                    // Draw clearing triangles to revert the circle alpha at the end of each arm
                                    val triWidth = 10.dp.toPx()
                                    val triHeight = 8.dp.toPx()
                                    val armOffset = 4.dp.toPx()

                                    val topTriPath = Path().apply {
                                        val baseY = -armOffset
                                        moveTo(centerX - triWidth / 2f, baseY)
                                        lineTo(centerX + triWidth / 2f, baseY)
                                        lineTo(centerX, baseY - triHeight) // Pointing UP (Away)
                                        close()
                                    }

                                    val bottomTriPath = Path().apply {
                                        val baseY = size.height + armOffset
                                        moveTo(centerX - triWidth / 2f, baseY)
                                        lineTo(centerX + triWidth / 2f, baseY)
                                        lineTo(centerX, baseY + triHeight) // Pointing DOWN (Away)
                                        close()
                                    }

                                    val leftTriPath = Path().apply {
                                        val baseX = -armOffset
                                        moveTo(baseX, centerY - triWidth / 2f)
                                        lineTo(baseX, centerY + triWidth / 2f)
                                        lineTo(baseX - triHeight, centerY) // Pointing LEFT (Away)
                                        close()
                                    }

                                    val rightTriPath = Path().apply {
                                        val baseX = size.width + armOffset
                                        moveTo(baseX, centerY - triWidth / 2f)
                                        lineTo(baseX, centerY + triWidth / 2f)
                                        lineTo(baseX + triHeight, centerY) // Pointing RIGHT (Away)
                                        close()
                                    }

                                    // Clear the circle alpha in the triangle shapes
                                    listOf(topTriPath, bottomTriPath, leftTriPath, rightTriPath).forEach { triPath ->
                                        drawPath(
                                            path = triPath,
                                            color = Color.Black.copy(alpha = 0.1f),
                                        )
                                    }
                                } else {
// Game Boy Pocket style: 4 small indicator dots at the tips of the D-Pad arms
                                    val dotRadius = 4.dp.toPx()
                                    val armOffset = 8.dp.toPx()
                                    val dotColor = Color.Black.copy(alpha = 0.08f)

                                    // Top dot
                                    drawCircle(
                                        color = dotColor,
                                        radius = dotRadius,
                                        center = Offset(centerX, -armOffset)
                                    )

                                    // Bottom dot
                                    drawCircle(
                                        color = dotColor,
                                        radius = dotRadius,
                                        center = Offset(centerX, size.height + armOffset)
                                    )

                                    // Left dot
                                    drawCircle(
                                        color = dotColor,
                                        radius = dotRadius,
                                        center = Offset(-armOffset, centerY)
                                    )

                                    // Right dot
                                    drawCircle(
                                        color = dotColor,
                                        radius = dotRadius,
                                        center = Offset(size.width + armOffset, centerY)
                                    )
                                }
                            }
                    )
                }
            )

            // A/B Buttons
            GbControlFaceButtons(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(180.dp),
                rotationInDegrees = -30f,
                ids = persistentListOf(
                    Id.Key(KeyEvent.KEYCODE_BUTTON_A),
                    Id.Key(KeyEvent.KEYCODE_BUTTON_B),
                ),
                background = {
                    if (skin.model == GbModel.DMG) {
                        // Shared oval background behind both A and B buttons
                        Box(
                            modifier = Modifier
                                .fillMaxSize(0.9f)
                                .aspectRatio(2.2f)
                                .graphicsLayer {
                                    rotationZ = -30f // Rotates oval to match button angle
                                }
                                .offset(y = (-4).dp)
                                .background(
                                    color = Color.Black.copy(alpha = 0.05f),
                                    shape = RoundedCornerShape(percent = 50) // Creates an oval/pill shape
                                )
                        )
                    }
                },
                idsForegrounds = persistentMapOf<Id.Key, @Composable (State<Boolean>) -> Unit>(
                    Id.Key(KeyEvent.KEYCODE_BUTTON_A) to {
                        DmgRoundButtonForeground(
                            pressed = it,
                            label = "A",
                            rotation = if (skin.model == GbModel.DMG) -30f else 0f,
                        )
                    },
                    Id.Key(KeyEvent.KEYCODE_BUTTON_B) to {
                        DmgRoundButtonForeground(
                            pressed = it,
                            label = "B",
                            rotation = if (skin.model == GbModel.DMG) -30f else 0f,
                        )
                    },
                ),
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 12.dp)
        ) {
            // Start/Select Buttons
            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(bottom = 32.dp),
            ) {
                // 1. Constrain SELECT Button
                Box(
                    modifier = Modifier
                        .size(width = 90.dp, height = 60.dp)
                        .offset(y = if (skin.model == GbModel.DMG) 0.dp else 50.dp), // Adjust these bounds to your liking!
                    contentAlignment = Alignment.TopCenter,
                ) {
                    GBControlButton(
                        id = Id.Key(KeyEvent.KEYCODE_BUTTON_SELECT),
                        label = "SELECT",
                        rotation = if (skin.model == GbModel.DMG) -30f else 0f,
                        expansion = if (skin.model == GbModel.DMG) 6.0f else 0.0f,
                    )
                }

                // 2. Constrain START Button
                Box(
                    modifier = Modifier
                        .size(width = 90.dp, height = 60.dp)
                        .offset(y = if (skin.model == GbModel.DMG) 0.dp else 50.dp), // Keeps them completely uniform
                    contentAlignment = Alignment.TopCenter,
                ) {
                    GBControlButton(
                        id = Id.Key(KeyEvent.KEYCODE_BUTTON_START),
                        label = "START",
                        rotation = if (skin.model == GbModel.DMG) -30f else 0f,
                        expansion = if (skin.model == GbModel.DMG) 6.0f else 0.0f,
                    )
                }
            }
        }

        Box(
            modifier = actionBarModifier,
            contentAlignment = Alignment.Center
        ) {
            actionBar()
        }
    }
}
