package com.swordfish.lemuroid.app.shared.manuals.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
        var isZoomedIn by remember { mutableStateOf(false) }
        
        // Log page changes for debugging
        androidx.compose.runtime.LaunchedEffect(pagerState.currentPage) {
            android.util.Log.d("ManualViewer-Pager", "Page changed to ${pagerState.currentPage}")
        }

        Surface(
            modifier = modifier.fillMaxSize(),
            color = Color.Black.copy(alpha = 0.92f)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {

                // --- PAGER AREA ---
                HorizontalPager(
                    state = pagerState,
                    userScrollEnabled = true, // TEMP: Always enabled for testing
                    modifier = Modifier.fillMaxSize()
                ) { pageIdx ->
                    ZoomableManualPage(
                        page = pages[pageIdx],
                        onZoomChanged = { zoomed ->
                            isZoomedIn = zoomed
                        }
                    )
                }

                // --- TOP TOOLBAR ---
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.4f))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Game Manual",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(
                        onClick = {
                            android.util.Log.d("ManualViewer-Close", "Close button clicked!")
                            onDismiss()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Manual",
                            tint = Color.White
                        )
                    }
                }

                // --- BOTTOM PAGE COUNTER ---
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
