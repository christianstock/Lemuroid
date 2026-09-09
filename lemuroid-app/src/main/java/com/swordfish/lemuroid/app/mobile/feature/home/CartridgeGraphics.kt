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
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.LemuroidGameImage
import com.swordfish.lemuroid.lib.library.db.entity.Game

@Composable
fun GameCartridge(
    game: Game,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.aspectRatio(0.85f), // Slightly taller for notch
        contentAlignment = Alignment.Center
    ) {
        when (game.systemId) {
            "gb", "gbc" -> GbGbcCartridgeShape(game)
            "gba" -> GbaCartridgeShape(game)
            else -> DefaultCartridgeShape(game)
        }
    }
}

@Composable
private fun GbGbcCartridgeShape(game: Game) {
    val cartridgeColor = Color(0xFF3A3A3A) // Dark grey
    val detailColor = Color(0xFF4A4A4A)    // shade lighter
    val lineInsetColor = Color(0xFF2A2A2A) // shade darker
    
    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            
            // 1. Body Path with top-right notch and sharpish corners
            val shellPath = Path().apply {
                val r = 1.5.dp.toPx() // Less rounded corners
                val notchW = 16.dp.toPx()
                val notchH = 12.dp.toPx()
                
                moveTo(r, 0f)
                lineTo(w - notchW, 0f)
                lineTo(w - notchW, notchH)
                lineTo(w, notchH)
                lineTo(w, h - r)
                quadraticTo(w, h, w - r, h)
                lineTo(r, h)
                quadraticTo(0f, h, 0f, h - r)
                lineTo(0f, r)
                quadraticTo(0f, 0f, r, 0f)
                close()
            }
            drawPath(shellPath, cartridgeColor)
            
            // 2. Side Grooves (60% height from bottom)
            val grooveW = 4.dp.toPx()
            val grooveH = h * 0.75f
            val grooveY = h - grooveH - 0.dp.toPx()
            
            // Two thin darker stripes on each side
            drawRect(lineInsetColor, Offset(0.dp.toPx(), grooveY), Size(grooveW, grooveH))

            drawRect(lineInsetColor, Offset(w - 4.dp.toPx(), grooveY), Size(grooveW, grooveH))

            // 3. Top Area details
            val firstLineY = 26.dp.toPx()
            val lineSpacing = 6.dp.toPx()
            val lastLineY = firstLineY + 4 * lineSpacing // 5 lines
            
            // 5 thin lines crossing the entire width
            repeat(5) { i ->
                val lineY = firstLineY + i * lineSpacing
                drawRect(
                    color = lineInsetColor,
                    topLeft = Offset(0f, lineY),
                    size = Size(w, 3.dp.toPx())
                )
            }
            
            // Grip Rounded Box - Lowered to cover 5th line, stretched
            val labelPadding = 16.dp.toPx()
            val ovalW = w - labelPadding * 2 - 16.dp.toPx()
            val ovalH = (lastLineY - firstLineY) + 16.dp.toPx()
            drawRoundRect(
                color = detailColor,
                topLeft = Offset(labelPadding + 8.dp.toPx(), firstLineY - 8.dp.toPx()),
                size = Size(ovalW, ovalH),
                cornerRadius = CornerRadius(ovalH / 2)
            )

            // Nintendo GAME BOY Text
            drawContext.canvas.nativeCanvas.apply {
                val paint = android.graphics.Paint().apply {
                    color = cartridgeColor.toArgb()
                    textSize = 12.sp.toPx()
                    typeface = android.graphics.Typeface.create("sans-serif-condensed", android.graphics.Typeface.NORMAL)
                    textAlign = android.graphics.Paint.Align.CENTER
                }
                drawText("Nintendo", w / 2 - 38.dp.toPx(), firstLineY + ovalH / 2 - 0.dp.toPx(), paint)
            }

            drawContext.canvas.nativeCanvas.apply {
                val paint = android.graphics.Paint().apply {
                    color = cartridgeColor.toArgb()
                    textSize = 18.sp.toPx()
                    typeface = android.graphics.Typeface.create("sans-serif-condensed", android.graphics.Typeface.BOLD_ITALIC)
                    textAlign = android.graphics.Paint.Align.CENTER
                }
                drawText("GAME BOY", w / 2 + 32.dp.toPx(), firstLineY + ovalH / 2 - 0.dp.toPx(), paint)
            }
            
            // 4. Down triangle (Darker color, Lower)
            val triW = 32.dp.toPx()
            val triH = 20.dp.toPx()
            val triBottomOffset = 4.dp.toPx() // Lowered
            val triPath = Path().apply {
                moveTo(w / 2 - triW / 2, h - triH - triBottomOffset)
                lineTo(w / 2 + triW / 2, h - triH - triBottomOffset)
                lineTo(w / 2, h - triBottomOffset)
                close()
            }
            drawPath(triPath, lineInsetColor)
        }
        
        // 5. Box art with more rounded corners (10.dp)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 24.dp, end = 24.dp, top = 68.dp, bottom = 32.dp)
                .clip(androidx.compose.foundation.shape.RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            LemuroidGameImage(
                game = game,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds,
                applyAspectRatio = false
            )
        }
    }
}

@Composable
private fun GbaCartridgeShape(game: Game) {
    val cartridgeColor = Color(0xFF3A3A3A) // Dark grey
    val detailColor = Color(0xFF4A4A4A)    // shade lighter
    val lineInsetColor = Color(0xFF2A2A2A) // shade darker
    
    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val overhangH = 36.dp.toPx()
            val sideIndent = 6.dp.toPx()
            
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
            val ovalW = w * 0.8f
            val ovalH = 56.dp.toPx()
            drawArc(
                color = detailColor,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset((w - ovalW) / 2, 4.dp.toPx()),
                size = Size(ovalW, ovalH)
            )

            val firstLineY = 4.dp.toPx()
            // Nintendo GAME BOY Text
            drawContext.canvas.nativeCanvas.apply {
                val paint = android.graphics.Paint().apply {
                    color = cartridgeColor.toArgb()
                    textSize = 14.sp.toPx()
                    typeface = android.graphics.Typeface.create("sans-serif-condensed", android.graphics.Typeface.BOLD_ITALIC)
                    textAlign = android.graphics.Paint.Align.CENTER
                }
                drawText("GAME BOY", w / 2 - 32.dp.toPx(), firstLineY + ovalH / 2 - 4.dp.toPx(), paint)
            }

            drawContext.canvas.nativeCanvas.apply {
                val paint = android.graphics.Paint().apply {
                    color = cartridgeColor.toArgb()
                    textSize = 12.sp.toPx()
                    typeface = android.graphics.Typeface.create("sans-serif-condensed", android.graphics.Typeface.BOLD_ITALIC)
                    textAlign = android.graphics.Paint.Align.CENTER
                }
                drawText("ADVANCE", w / 2 + 32.dp.toPx(), firstLineY + ovalH / 2 - 6.dp.toPx(), paint)
            }
            
            // 3. Down triangle at bottom center
            val triW = 32.dp.toPx()
            val triH = 20.dp.toPx()
            val triBottomOffset = 6.dp.toPx()
            val triPath = Path().apply {
                moveTo(w / 2 - triW / 2, h - triH - triBottomOffset)
                lineTo(w / 2 + triW / 2, h - triH - triBottomOffset)
                lineTo(w / 2, h - triBottomOffset)
                close()
            }
            drawPath(triPath, lineInsetColor)
        }
        
        // 4. Content (Box Art) - matching the indent and overhang
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 24.dp, end = 24.dp, top = 44.dp, bottom = 32.dp)
                .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            LemuroidGameImage(
                game = game,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds,
                applyAspectRatio = false
            )
        }
    }
}

@Composable
private fun DefaultCartridgeShape(game: Game) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Gray.copy(alpha = 0.5f), shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        LemuroidGameImage(
            game = game,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
    }
}
