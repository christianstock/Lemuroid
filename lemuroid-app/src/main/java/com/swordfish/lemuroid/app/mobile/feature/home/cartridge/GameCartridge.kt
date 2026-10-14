package com.swordfish.lemuroid.app.mobile.feature.home.cartridge

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.LemuroidGameImage
import com.swordfish.lemuroid.app.shared.covers.CoverUtils
import com.swordfish.lemuroid.lib.library.db.entity.Game

@Composable
fun GameCartridge(
    game: Game,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.aspectRatio(1.0f),
        contentAlignment = Alignment.Center
    ) {
        when {
            !game.cartridgeUrl.isNullOrEmpty() -> SimpleCartridgeImage(game)
            else -> {
                Box(modifier = Modifier.aspectRatio(0.85f), contentAlignment = Alignment.Center) {
                    when (game.systemId) {
                        "gb", "gbc" -> GameBoyCartridge(game, modifier = Modifier.fillMaxSize())
                        "gba" -> GameBoyCartridge(game, modifier = Modifier.fillMaxSize())
                        else -> GameBoyCartridge(game, modifier = Modifier.fillMaxSize())
                    }
                }
            }
        }
    }
}

@Composable
private fun SimpleCartridgeImage(
    game: Game,
    modifier: Modifier = Modifier,
) {
    val fallbackDrawable = remember(game) {
        CoverUtils.getFallbackDrawable(game)
    }
    val fallbackPainter = rememberDrawablePainter(drawable = fallbackDrawable)

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(game.cartridgeUrl)
                .build(),
            contentDescription = game.title,
            modifier = Modifier.fillMaxSize(),
            fallback = fallbackPainter,
            error = fallbackPainter,
            contentScale = ContentScale.Fit,
        )
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
