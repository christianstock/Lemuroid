package com.swordfish.lemuroid.app.mobile.feature.games

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.zIndex
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import kotlin.math.exp
import kotlin.math.hypot

@Stable
class GridRipple(
    private val speed: Float = 3f,
    private val bandWidth: Float = 1f,
    private val tailFactor: Float = 2f,
    private val strength: Float = 0.7f,
) {
    var origin by mutableStateOf(Offset.Zero)
    var coverWidthPx by mutableFloatStateOf(1f)

    private val front = Animatable(INACTIVE)
    private var maxDist = 0f

    val isRunning: Boolean
        get() = front.value != INACTIVE

    suspend fun run(gridSize: IntSize) {
        if (coverWidthPx <= 0f) return
        maxDist = hypot(gridSize.width.toFloat(), gridSize.height.toFloat()) / coverWidthPx
        val travel = maxDist + bandWidth * 3f
        try {
            front.snapTo(0f)
            front.animateTo(
                targetValue = travel,
                animationSpec = tween((travel / speed * 1000).toInt(), easing = LinearEasing)
            )
        } finally {
            withContext(NonCancellable) { front.snapTo(INACTIVE) }
        }
    }

    fun intensity(d: Float): Float {
        val f = front.value
        if (f == INACTIVE) return 0f
        val dl = d - f
        val s = if (dl < 0f) bandWidth * tailFactor else bandWidth
        val fade = (1f - f / (maxDist + bandWidth * tailFactor)).coerceIn(0f, 1f)
        val v = exp(-(dl / s) * (dl / s)) * (0.4f + 0.6f * fade)
        return v * strength
    }

    private companion object {
        const val INACTIVE = -1000f
    }
}

@Composable
fun Modifier.rippleLift(ripple: GridRipple, shape: Shape): Modifier {
    var center by remember { mutableStateOf(Offset.Zero) }

    fun calculateIntensity(): Float {
        if (ripple.coverWidthPx <= 0f) return 0f
        val d = (center - ripple.origin).getDistance() / ripple.coverWidthPx
        return ripple.intensity(d)
    }

    return this
        .onGloballyPositioned { coordinates ->
            center = coordinates.positionInRoot() + Offset(
                x = coordinates.size.width / 2f,
                y = coordinates.size.height / 2f
            )
            if (coordinates.size.width > 0) {
                ripple.coverWidthPx = coordinates.size.width.toFloat()
            }
        }
        .zIndex(if (calculateIntensity() > 0.01f) 1f else 0f)
        .graphicsLayer {
            val kVal = calculateIntensity()
            val scale = 1f + 0.02f * kVal
            scaleX = scale
            scaleY = scale
        }
        .clip(shape)
        .drawWithContent {
            drawContent()
            val kVal = calculateIntensity()
            val alpha = kVal * 0.6f
            if (alpha > 0.005f) {
                drawRect(Color.White.copy(alpha = alpha))
            }
        }
}
