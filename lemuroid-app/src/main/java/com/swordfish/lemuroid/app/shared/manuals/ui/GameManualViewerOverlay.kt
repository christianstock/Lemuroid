package com.swordfish.lemuroid.app.shared.manuals.ui

import android.annotation.SuppressLint
import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.swordfish.lemuroid.app.shared.manuals.ManualPageInfo
import org.intellij.lang.annotations.Language
import java.io.File
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import androidx.compose.ui.zIndex
import androidx.compose.ui.geometry.isSpecified

// Tuning knobs
private const val PEEL_ANGLE = 0.14f        // tilt of the fold line in radians (0 = perfectly vertical)
private const val PEEL_RADIUS_FRACTION = 0.09f // roll radius as a fraction of page width

/**
 * Page curl. Works in pixels. For every output pixel it finds which piece of paper
 * is visible there and samples the page from the matching source position.
 *
 * q = position along the peel direction, fold = where the paper leaves the table,
 * x = q - fold. Paper beyond the fold rolls over a cylinder of the given radius
 * and lies back on top of the page, showing its back side.
 */
@Language("AGSL")
private val PAGE_CURL_AGSL = """
    uniform shader content;
    uniform float4 pageRect;
    uniform float2 dirN;
    uniform float fold;
    uniform float minQ;
    uniform float maxQ;
    uniform float radius;

    const float PI = 3.14159265;

    bool insidePage(float2 s) {
        return s.x >= pageRect.x && s.y >= pageRect.y &&
               s.x <= pageRect.z - 1.0 && s.y <= pageRect.w - 1.0;
    }

    float rectDist(float2 s) {
        float2 c = float2((pageRect.x + pageRect.z) * 0.5, (pageRect.y + pageRect.w) * 0.5);
        float2 h = float2((pageRect.z - pageRect.x) * 0.5, (pageRect.w - pageRect.y) * 0.5);
        float2 dd = max(abs(s - c) - h, float2(0.0));
        return length(dd);
    }

    half4 backSide(half4 c, float light) {
        half3 paper = half3(0.93, 0.92, 0.88) * c.a;
        c.rgb = mix(c.rgb, paper, half(0.55)) * half(light);
        return c;
    }

    half4 main(float2 p) {
        float q = dot(p, dirN);
        float x = q - fold;

        float lift = clamp((maxQ - fold) / (radius * 2.0), 0.0, 1.0);
        float remain = clamp((fold - minQ + radius) / (radius * 3.0), 0.0, 1.0);
        float shadowK = 0.45 * lift * remain;

        if (x > 0.0) {
            if (x <= radius) {
                float a = asin(clamp(x / radius, 0.0, 1.0));

                float2 sBack = p + dirN * (fold + radius * (PI - a) - q);
                if (insidePage(sBack)) {
                    float shade = 0.9 * (0.45 + 0.55 * cos(a));
                    return backSide(content.eval(sBack), shade);
                }

                float2 sFront = p + dirN * (fold + radius * a - q);
                if (insidePage(sFront)) {
                    half4 c = content.eval(sFront);
                    c.rgb *= half(0.72 + 0.28 * cos(a));
                    return c;
                }
            }

            float2 sEdge = p + dirN * (fold + radius * PI * 0.5 - q);
            float d = max(x - radius, 0.0);
            float dist = length(float2(d, rectDist(sEdge)));
            float s = shadowK * (1.0 - smoothstep(0.0, radius * 1.8, dist));
            return half4(0.0, 0.0, 0.0, half(s));
        }

        float uFlat = 2.0 * fold + PI * radius - q;
        float2 sFlat = p + dirN * (uFlat - q);
        if (insidePage(sFlat)) {
            float g = smoothstep(0.0, radius * 4.0, -x);
            return backSide(content.eval(sFlat), 0.9 - 0.08 * g);
        }

        half4 c = content.eval(p);
        float shEnd = exp(-rectDist(sFlat) / (radius * 0.7));
        float shFold = exp(-(fold - q) / (radius * 2.5));
        c.rgb *= half(1.0 - 0.0 * lift * remain * max(shEnd, shFold));
        return c;
    }
""".trimIndent()

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
private class PeelShader {
    private val shader = RuntimeShader(PAGE_CURL_AGSL)

    /** [aspect] is the page's width / height, or 0 if it isn't known yet. */
    fun effect(
        layerWidth: Float,
        layerHeight: Float,
        aspect: Float,
        progress: Float
    ): androidx.compose.ui.graphics.RenderEffect {
        // Same placement as ContentScale.Fit, centred in the layer
        val pageW: Float
        val pageH: Float
        if (aspect > 0f) {
            if (layerWidth / layerHeight < aspect) {
                pageW = layerWidth; pageH = layerWidth / aspect
            } else {
                pageH = layerHeight; pageW = layerHeight * aspect
            }
        } else {
            pageW = layerWidth; pageH = layerHeight
        }
        val left = (layerWidth - pageW) / 2f
        val top = (layerHeight - pageH) / 2f
        val right = left + pageW
        val bottom = top + pageH

        val nx = cos(PEEL_ANGLE)
        val ny = sin(PEEL_ANGLE)
        val minQ = left * nx + top * ny
        val maxQ = right * nx + bottom * ny
        val radius = pageW * PEEL_RADIUS_FRACTION
        val fold = maxQ + (minQ - radius - maxQ) * progress

        shader.setFloatUniform("pageRect", left, top, right, bottom)
        shader.setFloatUniform("dirN", nx, ny)
        shader.setFloatUniform("fold", fold)
        shader.setFloatUniform("minQ", minQ)
        shader.setFloatUniform("maxQ", maxQ)
        shader.setFloatUniform("radius", radius)
        return RenderEffect
            .createRuntimeShaderEffect(shader, "content")
            .asComposeRenderEffect()
    }
}

@SuppressLint("NewApi")
@Composable
fun GameManualViewerOverlay(
    pages: List<ManualPageInfo>,
    isVisible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (pages.isEmpty()) return

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        val pagerState = rememberPagerState(initialPage = 0, pageCount = { pages.size })
        var scale by remember { mutableFloatStateOf(1f) }
        var offset by remember { mutableStateOf(Offset.Zero) }
        var viewSize by remember { mutableStateOf(IntSize.Zero) }

        LaunchedEffect(pagerState.settledPage) {
            scale = 1f
            offset = Offset.Zero
        }

        // One continuous number: 4.3 means page 4 is 30% peeled away.
        val position = {
            (pagerState.currentPage + pagerState.currentPageOffsetFraction)
                .coerceIn(0f, pages.lastIndex.toFloat())
        }
        val base by remember {
            derivedStateOf { floor(position()).toInt().coerceIn(0, pages.lastIndex) }
        }

        Surface(
            modifier = modifier.fillMaxSize(),
            color = Color.Black.copy(alpha = 0.94f)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .onSizeChanged { viewSize = it }
            ) {
                // Visual layers: next page underneath (drawn first), current page on top.
                val window = ((base - 1)..(base + 2)).filter { it in pages.indices }
                for (index in window) {
                    key(index) {
                        PageLayer(
                            page = pages[index],
                            index = index,
                            position = position,
                            zoomedPage = { pagerState.currentPage },
                            scale = { scale },
                            offset = { offset }
                        )
                    }
                }

                // Invisible pager on top: it only provides swipe gestures and the position.
                HorizontalPager(
                    state = pagerState,
                    userScrollEnabled = scale <= 1.01f,
                    modifier = Modifier.fillMaxSize(),
                    pageSpacing = 0.dp
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                awaitEachGesture {
                                    awaitFirstDown(requireUnconsumed = false)
                                    do {
                                        val event = awaitPointerEvent()
                                        val zoom = event.calculateZoom()
                                        val pan = event.calculatePan()
                                        val newScale = (scale * zoom).coerceIn(1f, 4f)

                                        if (newScale > 1f) {
                                            scale = newScale
                                            val maxX = viewSize.width * (newScale - 1f) / 2f
                                            val maxY = viewSize.height * (newScale - 1f) / 2f
                                            offset = Offset(
                                                (offset.x + pan.x).coerceIn(-maxX, maxX),
                                                (offset.y + pan.y).coerceIn(-maxY, maxY)
                                            )
                                            event.changes.forEach {
                                                if (it.positionChanged()) it.consume()
                                            }
                                        } else {
                                            scale = 1f
                                            offset = Offset.Zero
                                        }
                                    } while (event.changes.any { it.pressed })
                                }
                            }
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .statusBarsPadding()
                        .padding(8.dp)
                ) {
                    Text(
                        text = "< BACK",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp)
                        .background(
                            color = Color.Black.copy(alpha = 0.75f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "${pagerState.currentPage + 1} / ${pages.size}",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@SuppressLint("NewApi")
@Composable
private fun PageLayer(
    page: ManualPageInfo,
    index: Int,
    position: () -> Float,
    zoomedPage: () -> Int,
    scale: () -> Float,
    offset: () -> Offset
) {
    val peel = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) PeelShader() else null
    }
    val file = remember(page.filePath) { File(page.filePath) }
    var aspect by remember(page.filePath) { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(-index.toFloat())
            .graphicsLayer {
                val pos = position()
                val t = (pos - index).coerceIn(0f, 1f)

                val shown = pos > index - 1f - 0.001f && t < 0.999f
                alpha = if (shown) 1f else 0f

                if (t <= 0.001f) {
                    renderEffect = null
                    translationX = 0f
                } else if (peel != null) {
                    renderEffect = peel.effect(size.width, size.height, aspect, t)
                    translationX = 0f
                } else {
                    translationX = -t * size.width
                }
            }
    ) {
        AsyncImage(
            model = file,
            contentDescription = "Manual Page ${page.pageIndex + 1}",
            contentScale = ContentScale.Fit,
            onSuccess = { state ->
                val size = state.painter.intrinsicSize
                if (size.isSpecified && size.height > 0f) {
                    aspect = size.width / size.height
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    if (index == zoomedPage()) {
                        scaleX = scale()
                        scaleY = scale()
                        translationX = offset().x
                        translationY = offset().y
                    }
                }
        )
    }
}
