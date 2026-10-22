package com.swordfish.lemuroid.app.mobile.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RetroCheckbox(
    checked: Boolean,
    modifier: Modifier = Modifier,
    color: Color
) {
    Box(
        modifier = modifier
            .size(20.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(if (checked) color else RetroPanel)
            .drawWithContent {
                drawContent()
                val stroke = 2.dp.toPx()
                drawRect(
                    color = color,
                    topLeft = Offset(stroke / 2f, stroke / 2f),
                    size = Size(size.width - stroke, size.height - stroke),
                    style = Stroke(width = stroke)
                )
            },
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            Text(
                text = "✓",
                fontFamily = pressStart2PFontFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = RetroPanel,
                modifier = Modifier.offset(y = (-4).dp)
            )
        }
    }
}
