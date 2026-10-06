package com.swordfish.lemuroid.app.shared.manuals.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swordfish.lemuroid.app.shared.manuals.ManualPageInfo

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
        val isZoomedIn = remember { mutableStateOf(false) }

        // Automatically reset zoom state when switching pages
        LaunchedEffect(pagerState.currentPage) {
            isZoomedIn.value = false
            android.util.Log.d("ManualViewer-Pager", "Page changed to ${pagerState.currentPage}")
        }

        Surface(
            modifier = modifier.fillMaxSize(),
            color = Color.Black.copy(alpha = 0.92f)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {

                // --- PAGER AREA WITH REALISTIC PAGE FLIP ANIMATION ---
                HorizontalPager(
                    state = pagerState,
                    userScrollEnabled = !isZoomedIn.value,
                    modifier = Modifier.fillMaxSize(),
                    pageSpacing = 0.dp
                ) { pageIdx ->
                    val pageOffset = (pagerState.currentPage - pageIdx) + pagerState.currentPageOffsetFraction
                    
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        // Page content with realistic flip effect
                        PageFlipContainer(
                            pageOffset = pageOffset,
                            page = pages[pageIdx],
                            onZoomChanged = { zoomed -> isZoomedIn.value = zoomed }
                        )
                    }
                }

                // --- TOP LEFT BACK BUTTON ---
                IconButton(
                    onClick = {
                        android.util.Log.d("ManualViewer-Back", "Back button clicked!")
                        onDismiss()
                    },
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

                // --- BOTTOM PAGE COUNTER (MINIMAL) ---
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp)
                        .background(
                            color = Color.Black.copy(alpha = 0.7f),
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

@Composable
private fun PageFlipContainer(
    pageOffset: Float,
    page: ManualPageInfo,
    onZoomChanged: (Boolean) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                // Calculate page curl effect based on scroll progress
                val pageFlipProgress = pageOffset.coerceIn(-1f, 1f).let { 
                    1f - (it * it) 
                }
                
                if (pageFlipProgress > 0f) {
                    drawPageFlipEffect(
                        progress = pageFlipProgress,
                        size = this.size
                    )
                }
            }
    ) {
        // Main page content
        ZoomableManualPage(
            page = page,
            onZoomChanged = onZoomChanged
        )
        
        // Page curl shadow effect overlay
        if (pageOffset.coerceIn(-1f, 1f).let { 1f - (it * it) } > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Color.Black.copy(
                            alpha = 0.15f * (1f - pageOffset.coerceIn(-1f, 1f).let { 1f - (it * it) })
                        )
                    )
            )
        }
    }
}

private fun DrawScope.drawPageFlipEffect(
    progress: Float,
    size: androidx.compose.ui.geometry.Size
) {
    // Page curl corner point - moves from bottom-right to top-right as we flip
    val maxCurlX = size.width * 0.3f
    val curlX = size.width - (maxCurlX * progress)
    val curlY = size.height - (size.height * 0.4f * progress)
    
    // Create shadow/gradient for curl effect
    drawIntoCanvas { canvas ->
        val paint = android.graphics.Paint().apply {
            color = android.graphics.Color.argb(77, 0, 0, 0)  // 0.3f alpha ~ 77/255
            isAntiAlias = true
        }
        
        // Draw curved page fold shadow using bezier curve
        val path = android.graphics.Path().apply {
            moveTo(curlX, 0f)
            // Quadratic bezier for smooth curve
            quadTo(
                size.width * 0.95f, curlY * 0.5f,
                curlX + 15f, curlY
            )
            // Complete the shadow triangle
            lineTo(size.width, size.height)
            lineTo(size.width, 0f)
            close()
        }
        
        canvas.nativeCanvas.drawPath(path, paint)
    }
    
    // Add inner page shadow for depth
    if (progress > 0.1f) {
        drawIntoCanvas { canvas ->
            val innerPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.argb(40, 0, 0, 0)  // Subtle inner shadow
                isAntiAlias = true
            }
            
            val innerPath = android.graphics.Path().apply {
                moveTo(curlX - 5f, 0f)
                quadTo(
                    curlX - 10f, curlY * 0.5f,
                    curlX + 10f, curlY
                )
                lineTo(curlX + 15f, curlY)
                quadTo(
                    size.width * 0.95f, curlY * 0.5f,
                    curlX, 0f
                )
                close()
            }
            
            canvas.nativeCanvas.drawPath(innerPath, innerPaint)
        }
    }
}
