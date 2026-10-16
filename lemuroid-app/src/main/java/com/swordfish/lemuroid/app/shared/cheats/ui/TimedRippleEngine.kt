package com.swordfish.lemuroid.app.shared.cheats.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.exp
import kotlin.random.Random

@Stable
class TimedRippleEngine(
    private val speedDpPerSec: Float = 530f,
    private val leadDp: Float = 90f,
    private val tailDp: Float = 90f,
    private val intervalMs: Long = 10_000L,
) {
    /** Set by the list; row positions are measured relative to it. */
    var listTopPx: Float = 0f

    private var leadPx = 1f
    private var tailPx = 1f
    private val front = Animatable(INACTIVE)

    suspend fun startLoop(listHeightPx: Float, density: Float) {
        if (listHeightPx <= 0f) return
        leadPx = leadDp * density
        tailPx = tailDp * density
        val from = -leadPx * 2.5f
        val to = listHeightPx + tailPx * 2.5f
        val durationMs = ((to - from) / (speedDpPerSec * density) * 1000f)
            .toInt().coerceAtLeast(1)

        delay(700)
        while (true) {
            front.snapTo(from)
            front.animateTo(to, tween(durationMs, easing = LinearEasing))
            front.snapTo(INACTIVE)
            delay((intervalMs * (0.85f + Random.nextFloat() * 0.3f)).toLong())
        }
    }

    /** 0..1 for a row whose centre is [y] px below the top of the list. Call only from draw. */
    fun intensityAt(y: Float): Float {
        val f = front.value
        if (f == INACTIVE) return 0f
        val dl = y - f
        val s = if (dl < 0f) tailPx else leadPx
        return exp(-(dl / s) * (dl / s))
    }

    private companion object {
        const val INACTIVE = -1_000_000f
    }
}

private class RowPosition {
    var centerY = 0f
}

@Composable
fun Modifier.timedGridRippleLift(
    engine: TimedRippleEngine,
    maxScale: Float = 1.015f,
    liftDp: Float = 2f,
    maxAlpha: Float = 0.1f,
    tint: Color = Color.White,
): Modifier {
    val pos = remember { RowPosition() }

    return this
        .onGloballyPositioned {
            pos.centerY = it.positionInRoot().y + it.size.height / 2f
        }
        .drawWithContent {
            val k = engine.intensityAt(pos.centerY - engine.listTopPx)

            if (k < 0.004f) {
                drawContent()
            } else {
                val s = 1f + (maxScale - 1f) * k
                val liftPx = liftDp.dp.toPx() * k
                withTransform({
                    translate(top = -liftPx)
                    scale(1f, s, pivot = center)
                }) {
                    this@drawWithContent.drawContent()
                    drawRect(tint.copy(alpha = maxAlpha * k))
                }
            }
        }
}
