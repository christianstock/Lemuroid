package com.swordfish.lemuroid.app.mobile.feature.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swordfish.lemuroid.app.mobile.shared.ScrubberTrack
import com.swordfish.lemuroid.lib.library.db.entity.Game

/**
 * Top title and scrubber track header component.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GameTitle(
    game: Game,
    accentColor: Color,
    pagerState: PagerState,
    itemCount: Int,
    modifier: Modifier = Modifier
) {
    val titleParts = remember(game.title) {
        game.title.split(" - ", limit = 2).map { it.trim() }
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ScrubberTrack(
            pagerState = pagerState,
            itemCount = itemCount,
            accentColor = accentColor,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 48.dp, vertical = 4.dp)
        )

        Text(
            text = titleParts[0].uppercase(),
            fontFamily = PressStart2PFontFamily,
            fontSize = 20.sp,
            lineHeight = 26.sp,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )

        titleParts.getOrNull(1)?.let { subtitle ->
            Text(
                text = subtitle.uppercase(),
                fontFamily = PressStart2PFontFamily,
                fontSize = 16.sp,
                lineHeight = 20.sp,
                color = Color.White.copy(alpha = 0.85f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(start = 32.dp, top = 8.dp, end = 32.dp)
            )
        }

        val year = game.releaseDate?.take(4) ?: ""
        val publisher = game.publisher ?: ""

        if (publisher.isNotEmpty() || year.isNotEmpty()) {
            Text(
                text = listOfNotNull(
                    publisher.takeIf { it.isNotEmpty() },
                    year.takeIf { it.isNotEmpty() }
                ).joinToString(" | "),
                style = MaterialTheme.typography.bodyMedium,
                color = accentColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
