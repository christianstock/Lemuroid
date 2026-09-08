package com.swordfish.lemuroid.app.mobile.feature.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp

@Composable
fun GameCartridge(
    systemId: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier.aspectRatio(0.9f),
        contentAlignment = Alignment.Center
    ) {
        when (systemId) {
            "gb", "gbc" -> GbGbcCartridgeShape(content)
            "gba" -> GbaCartridgeShape(content)
            else -> DefaultCartridgeShape(content)
        }
    }
}

@Composable
private fun GbGbcCartridgeShape(content: @Composable () -> Unit) {
    val cartridgeColor = Color(0xFF3A3A3A) // Dark grey
    val detailColor = Color(0xFF4A4A4A)    // shade lighter
    val lineInsetColor = Color(0xFF2A2A2A) // shade darker
    
    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            
            // 1. Slightly rounded corners for the cartridge body
            drawRoundRect(
                color = cartridgeColor,
                size = size,
                cornerRadius = CornerRadius(8.dp.toPx())
            )
            
            // 2. Top Area details (Moved down slightly)
            val firstLineY = 16.dp.toPx()
            val lineSpacing = 6.dp.toPx()
            val lastLineY = firstLineY + 3 * lineSpacing
            
            // 4 thin lines crossing the entire width
            repeat(4) { i ->
                val lineY = firstLineY + i * lineSpacing
                drawRect(
                    color = lineInsetColor,
                    topLeft = Offset(0f, lineY),
                    size = Size(w, 1.dp.toPx())
                )
            }
            
            // "Oval" (Rounded Box) - Wider
            val labelPadding = 16.dp.toPx() // Wider than box art
            val ovalW = w - labelPadding * 2
            val ovalH = (lastLineY - firstLineY) + 2.dp.toPx()
            drawRoundRect(
                color = detailColor,
                topLeft = Offset(labelPadding, firstLineY - 1.dp.toPx()),
                size = Size(ovalW, ovalH),
                cornerRadius = CornerRadius(ovalH / 2)
            )
            
            // 3. Down triangle (Moved down a bit)
            val triW = 32.dp.toPx()
            val triH = 20.dp.toPx()
            val triBottomOffset = 8.dp.toPx() // Lower down
            val triPath = Path().apply {
                moveTo(w / 2 - triW / 2, h - triH - triBottomOffset)
                lineTo(w / 2 + triW / 2, h - triH - triBottomOffset)
                lineTo(w / 2, h - triBottomOffset)
                close()
            }
            drawPath(triPath, detailColor)
        }
        
        // 4. Box art with less rounded corners (4.dp)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 24.dp, end = 24.dp, top = 60.dp, bottom = 32.dp)
                .background(
                    Color.Black.copy(alpha = 0.05f),
                    androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                )
                .clip(androidx.compose.foundation.shape.RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}

@Composable
private fun GbaCartridgeShape(content: @Composable () -> Unit) {
    val cartridgeColor = Color(0xFF333333) // Dark grey
    val detailColor = Color(0xFF4A4A4A)    // shade lighter
    
    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val overhangH = 24.dp.toPx()
            val sideIndent = 12.dp.toPx()
            
            // 1. T-Shaped body with slight overhang on top (Slightly Rounded Corners)
            val path = Path().apply {
                // Top overhang
                moveTo(4.dp.toPx(), 0f)
                lineTo(w - 4.dp.toPx(), 0f)
                quadraticTo(w, 0f, w, 4.dp.toPx())
                lineTo(w, overhangH)
                
                // Indent to main body
                lineTo(w - sideIndent, overhangH)
                lineTo(w - sideIndent, h - 4.dp.toPx())
                quadraticTo(w - sideIndent, h, w - sideIndent - 4.dp.toPx(), h)
                lineTo(sideIndent + 4.dp.toPx(), h)
                quadraticTo(sideIndent, h, sideIndent, h - 4.dp.toPx())
                lineTo(sideIndent, overhangH)
                
                // Back to overhang
                lineTo(0f, overhangH)
                lineTo(0f, 4.dp.toPx())
                quadraticTo(0f, 0f, 4.dp.toPx(), 0f)
                close()
            }
            drawPath(path, cartridgeColor)
            
            // 2. Half-oval on top
            val ovalW = w * 0.6f
            val ovalH = 16.dp.toPx()
            drawArc(
                color = detailColor,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset((w - ovalW) / 2, -ovalH / 2),
                size = Size(ovalW, ovalH)
            )
            
            // 3. Down triangle at bottom center
            val triW = 20.dp.toPx()
            val triH = 10.dp.toPx()
            val triBottomOffset = 10.dp.toPx()
            val triPath = Path().apply {
                moveTo(w / 2 - triW / 2, h - triH - triBottomOffset)
                lineTo(w / 2 + triW / 2, h - triH - triBottomOffset)
                lineTo(w / 2, h - triBottomOffset)
                close()
            }
            drawPath(triPath, detailColor)
        }
        
        // 4. Content (Box Art) - matching the indent and overhang
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 24.dp, end = 24.dp, top = 36.dp, bottom = 32.dp)
                .background(
                    Color.Black.copy(alpha = 0.2f),
                    androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                )
                .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}

@Composable
private fun DefaultCartridgeShape(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Gray.copy(alpha = 0.5f), shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
