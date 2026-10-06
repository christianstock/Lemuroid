import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import coil.compose.AsyncImage
import com.swordfish.lemuroid.app.shared.manuals.ManualPageInfo
import java.io.File

/**
 * Zoomable Manual Page integrated cleanly with gesture handling and bounds checking
 */
@Composable
fun ZoomableManualPage(
    page: ManualPageInfo,
    onZoomChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var containerSize by remember { mutableStateOf(IntSize.Zero) }

    LaunchedEffect(page.filePath) {
        scale = 1f
        offset = Offset.Zero
        onZoomChanged(false)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { containerSize = it }
            .pointerInput(page.filePath) {
                detectTransformGestures { _, pan, zoom, _ ->
                    val newScale = (scale * zoom).coerceIn(1f, 4f)
                    val isZoomed = newScale > 1f

                    if (scale != newScale) {
                        scale = newScale
                        onZoomChanged(isZoomed)
                    }

                    if (isZoomed) {
                        // Calculate maximum pan bounds to prevent image from panning into empty space
                        val maxOffsetX = (containerSize.width * (scale - 1f)) / 2f
                        val maxOffsetY = (containerSize.height * (scale - 1f)) / 2f

                        val newX = (offset.x + pan.x).coerceIn(-maxOffsetX, maxOffsetX)
                        val newY = (offset.y + pan.y).coerceIn(-maxOffsetY, maxOffsetY)
                        offset = Offset(newX, newY)
                    } else {
                        offset = Offset.Zero
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = File(page.filePath),
            contentDescription = "Manual Page ${page.pageIndex + 1}",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y
                )
        )
    }
}
