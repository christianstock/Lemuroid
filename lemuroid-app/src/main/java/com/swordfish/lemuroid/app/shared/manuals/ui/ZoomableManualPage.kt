package com.swordfish.lemuroid.app.shared.manuals.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.swordfish.lemuroid.app.shared.manuals.ManualPageInfo

@Composable
fun ZoomableManualPage(
    page: ManualPageInfo,
    onZoomChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    // TEMP: Simplified version without any gestures to test if HorizontalPager works
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = page.filePath,
            contentDescription = "Manual Page ${page.pageIndex + 1}",
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )
    }
}
