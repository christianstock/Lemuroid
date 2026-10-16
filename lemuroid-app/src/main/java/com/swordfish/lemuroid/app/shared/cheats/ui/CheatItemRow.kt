package com.swordfish.lemuroid.app.shared.cheats.ui

import android.graphics.Typeface
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.StartOffsetType
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swordfish.lemuroid.app.mobile.shared.RetroBorder
import com.swordfish.lemuroid.app.mobile.shared.RetroCoral
import com.swordfish.lemuroid.app.mobile.shared.RetroGold
import com.swordfish.lemuroid.app.mobile.shared.RetroLime
import com.swordfish.lemuroid.app.mobile.shared.RetroLimeEdge
import com.swordfish.lemuroid.app.mobile.shared.RetroMuted
import com.swordfish.lemuroid.app.mobile.shared.RetroPanel
import com.swordfish.lemuroid.app.mobile.shared.RetroText
import com.swordfish.lemuroid.lib.library.db.entity.GameCheatEntity
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheatItemRow(
    cheat: GameCheatEntity,
    rippleEngine: TimedRippleEngine,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dismissState = rememberSwipeToDismissBoxState(
        positionalThreshold = { totalDistance -> totalDistance * 0.4f },
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.StartToEnd) {
                onDelete()
                true
            } else false
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromEndToStart = false,
        backgroundContent = { CheatDeleteBackground() },
        modifier = modifier
    ) {
        CheatItemContent(
            cheat = cheat,
            rippleEngine = rippleEngine,
            onToggle = onToggle
        )
    }
}

/**
 * Red swipe-to-delete background revealed when dragging right.
 */
@Composable
private fun CheatDeleteBackground() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RetroCoral)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Icon(
            imageVector = Icons.Default.Clear,
            contentDescription = "Delete Cheat",
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun CheatItemContent(
    cheat: GameCheatEntity,
    rippleEngine: TimedRippleEngine,
    onToggle: () -> Unit,
) {
    val sourceColor = remember(cheat.source) { getSourceEdgeColor(cheat.source) }

    // c) Dynamic lighter background color when selected
    val rowBackgroundColor by animateColorAsState(
        targetValue = if (cheat.enabled) Color(0xFF152530) else RetroPanel,
        animationSpec = tween(durationMillis = 200),
        label = "RowBackgroundColor"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .timedGridRippleLift(engine = rippleEngine)
            .background(color = rowBackgroundColor)
            .drawWithContent {
                drawContent()

                val stripePaddingPx = 1.dp.toPx()
                val defaultTopBorderPx = 1.dp.toPx()
                val selectedTopBorderPx = 1.dp.toPx()

                // Default Top separator line
                drawLine(
                    color = RetroBorder,
                    start = Offset(x = 0f, y = 0f),
                    end = Offset(x = size.width, y = 0f),
                    strokeWidth = defaultTopBorderPx
                )

                // b) Additional top line in RetroLime when cheat is selected
                if (cheat.enabled) {
                    drawLine(
                        color = RetroLimeEdge,
                        start = Offset(x = 0f, y = 0f),
                        end = Offset(x = size.width, y = 0f),
                        strokeWidth = selectedTopBorderPx
                    )
                }

                // a) Left system accent stripe with top & bottom inset padding
                drawRect(
                    color = sourceColor,
                    topLeft = Offset(x = 0f, y = stripePaddingPx),
                    size = Size(
                        width = 2.dp.toPx(),
                        height = (size.height - (stripePaddingPx * 2f)).coerceAtLeast(0f)
                    )
                )
            }
            .clickable { onToggle() }
            .padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CheatStarToggleIcon(enabled = cheat.enabled)

        Text(
            text = cheat.description,
            color = RetroText,
            fontFamily = remember { FontFamily(Typeface.create("sans-serif-condensed", Typeface.NORMAL)) },
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            modifier = Modifier
                .weight(1f)
                .padding(start = 4.dp)
        )
    }
}

/**
 * Animated star icon reflecting the cheat's enabled state.
 */
@Composable
private fun CheatStarToggleIcon(
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    // Generate a stable random initial offset (between 0 and 2000ms) for this instance
    val animationOffsetMs = remember { Random.nextInt(0, 2000) }

    val infiniteTransition = rememberInfiniteTransition(label = "StarRotationTransition")
    val starRotation by infiniteTransition.animateFloat(
        initialValue = -1.5f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
            // Offsets each star's timeline uniquely so they aren't in sync
            initialStartOffset = StartOffset(animationOffsetMs, StartOffsetType.FastForward)
        ),
        label = "StarRotation"
    )

    Box(
        modifier = modifier.size(38.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (enabled) Icons.Filled.Star else Icons.Rounded.StarBorder,
            contentDescription = if (enabled) "Cheat enabled" else "Cheat disabled",
            tint = if (enabled) RetroLime else Color.DarkGray,
            modifier = Modifier
                .size(38.dp)
                .graphicsLayer {
                    rotationZ = if (enabled) starRotation else 0f
                }
        )
    }
}
