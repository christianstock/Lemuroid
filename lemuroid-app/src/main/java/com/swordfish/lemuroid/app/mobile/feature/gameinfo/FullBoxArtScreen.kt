package com.swordfish.lemuroid.app.mobile.feature.gameinfo

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.swordfish.lemuroid.app.mobile.shared.RetroBackButton

@Composable
fun FullBoxArtScreen(
    imageUrl: String,
    onDismiss: () -> Unit
) {
    val zoomScale = remember { mutableFloatStateOf(1f) }
    val panX = remember { mutableFloatStateOf(0f) }
    val panY = remember { mutableFloatStateOf(0f) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.95f))
                .padding(16.dp)
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        zoomScale.floatValue = (zoomScale.floatValue * zoom).coerceIn(1f, 5f)
                        if (zoomScale.floatValue > 1f) {
                            panX.floatValue += pan.x
                            panY.floatValue += pan.y
                        }
                    }
                }
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = "Full screen box art",
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = zoomScale.floatValue,
                        scaleY = zoomScale.floatValue,
                        translationX = panX.floatValue,
                        translationY = panY.floatValue
                    ),
                contentScale = ContentScale.Fit
            )

            RetroBackButton(
                onBack = onDismiss,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}
