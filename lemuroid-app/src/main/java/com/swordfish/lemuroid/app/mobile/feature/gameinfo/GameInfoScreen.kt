package com.swordfish.lemuroid.app.mobile.feature.gameinfo

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.mobile.shared.GameArtBlurBackground
import com.swordfish.lemuroid.app.mobile.shared.RetroBackButton
import com.swordfish.lemuroid.app.mobile.shared.extractAccentColor
import com.swordfish.lemuroid.app.mobile.shared.formatLocalizedDate
import com.swordfish.lemuroid.common.kotlin.cleanGameTitle
import com.swordfish.lemuroid.lib.library.db.entity.Game
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

val PressStart2PFont = FontFamily(
    Font(R.font.press_start_2p)
)

@Composable
fun GameInfoScreen(
    viewModel: GameInfoViewModel,
    onPlay: (Game) -> Unit,
    onRestart: (Game) -> Unit,
    onCheats: (Game) -> Unit = {},
    onManual: (Game) -> Unit = {},
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {}
) {
    val game = viewModel.game.collectAsState().value
    val isRescanning = viewModel.isRescanning.collectAsState().value
    val pendingMetadata = viewModel.pendingMetadata.collectAsState().value

    val fallbackAccentColor = MaterialTheme.colorScheme.primary
    var accentColor by remember { mutableStateOf(fallbackAccentColor) }
    val context = LocalContext.current

    LaunchedEffect(game?.id, game?.coverFrontUrl) {
        val extracted = withContext(Dispatchers.IO) {
            extractAccentColor(context, game?.coverFrontUrl, fallbackAccentColor)
        }
        accentColor = extracted
    }

    val animatedAccentColor by animateColorAsState(
        targetValue = accentColor,
        animationSpec = tween(durationMillis = 500),
        label = "AccentColorCrossfade"
    )

    if (game == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = animatedAccentColor)
        }
        return
    }

    if (pendingMetadata != null) {
        ScrapeResultScreen(
            game = game,
            metadata = pendingMetadata,
            onDismiss = { viewModel.clearPendingMetadata() },
            onAccept = { title, releaseDate, publisher, developer, region, coverFrontUrl, coverBackUrl, cartridgeUrl, manualUrl ->
                viewModel.applyCustomScrapedMetadata(
                    title = title,
                    releaseDate = releaseDate,
                    publisher = publisher,
                    developer = developer,
                    region = region,
                    coverFrontUrl = coverFrontUrl,
                    coverBackUrl = coverBackUrl,
                    cartridgeUrl = cartridgeUrl,
                    manualUrl = manualUrl
                )
            }
        )
    } else {
        Box(modifier = modifier.fillMaxSize()) {
            GameArtBlurBackground(coverFrontUrl = game.coverFrontUrl)

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp)
                        .padding(top = 16.dp, bottom = 64.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 1. Title Block
                    item {
                        val title = game.title.cleanGameTitle()
                        val titleParts = title.split(" - ", limit = 2).map { it.trim() }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text(
                                text = titleParts[0].uppercase(),
                                fontFamily = PressStart2PFont,
                                fontSize = 20.sp,
                                lineHeight = 26.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                            if (titleParts.size > 1) {
                                Text(
                                    text = titleParts[1].uppercase(),
                                    fontFamily = PressStart2PFont,
                                    fontSize = 16.sp,
                                    lineHeight = 20.sp,
                                    color = Color.White.copy(alpha = 0.85f),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(top = 6.dp)
                                )
                            }
                        }
                    }

                    // 2. Metadata Line
                    item {
                        val formattedDate = formatLocalizedDate(game.releaseDate)
                        val publisher = game.publisher ?: ""
                        val developer = game.developer ?: ""

                        val parts = listOfNotNull(
                            formattedDate.takeIf { it.isNotEmpty() },
                            publisher.takeIf { it.isNotEmpty() && it != developer },
                            developer.takeIf { it.isNotEmpty() }
                        )

                        if (parts.isNotEmpty()) {
                            Text(
                                text = parts.joinToString(" | "),
                                style = MaterialTheme.typography.bodyMedium,
                                color = animatedAccentColor,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }

                    // 3. BoxArtSwiper
                    item {
                        BoxArtSwiper(
                            frontImageUrl = game.coverFrontUrl,
                            backImageUrl = game.coverBackUrl,
                            tintColor = animatedAccentColor,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // 4. Region
                    item {
                        val region = game.country ?: ""
                        if (region.isNotEmpty()) {
                            Text(
                                text = region.uppercase(),
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    // 5. Resume / Restart
                    item {
                        val infiniteTransition = rememberInfiniteTransition(label = "resume-pulse")
                        val scale by infiniteTransition.animateFloat(
                            initialValue = 0.95f,
                            targetValue = 1.05f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(durationMillis = 1500),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "pulse"
                        )
                        val alpha by infiniteTransition.animateFloat(
                            initialValue = 0.9f,
                            targetValue = 1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(durationMillis = 1500),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "alpha"
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp, bottom = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                        onClick = { onPlay(game) }
                                    )
                                    .graphicsLayer(
                                        scaleX = scale,
                                        scaleY = scale,
                                        alpha = alpha
                                    )
                                    .padding(vertical = 14.dp, horizontal = 28.dp)
                            ) {
                                Text(
                                    text = "RESUME",
                                    fontFamily = PressStart2PFont,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = animatedAccentColor
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "RESTART",
                                fontFamily = PressStart2PFont,
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.85f),
                                modifier = Modifier
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                        onClick = { onRestart(game) }
                                    )
                                    .padding(vertical = 8.dp, horizontal = 16.dp)
                            )
                        }
                    }

                    // 6. Action Row
                    item {
                        RetroActionBar(
                            hasManual = game.manualUrl != null,
                            isRescanning = isRescanning,
                            onCheats = { onCheats(game) },
                            onManual = { onManual(game) },
                            onRescan = { viewModel.rescan() }
                        )
                    }
                }

                // Fixed Bottom Container for BACK Button
                RetroBackButton(
                    onBack = onBack,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }
}
