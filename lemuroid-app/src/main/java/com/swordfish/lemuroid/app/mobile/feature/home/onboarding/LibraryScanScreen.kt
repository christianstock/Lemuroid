package com.swordfish.lemuroid.app.mobile.feature.home.onboarding

import android.graphics.Typeface
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.mobile.shared.DefaultBackground
import com.swordfish.lemuroid.app.mobile.shared.RetroBorderLight
import com.swordfish.lemuroid.app.mobile.shared.RetroLime
import com.swordfish.lemuroid.app.mobile.shared.RetroLimeEdge
import com.swordfish.lemuroid.app.mobile.shared.RetroMagenta
import com.swordfish.lemuroid.app.mobile.shared.RetroPanel
import com.swordfish.lemuroid.app.mobile.shared.RetroText
import com.swordfish.lemuroid.app.mobile.shared.pressStart2PFontFamily
import com.swordfish.lemuroid.app.shared.cheats.ui.RetroBottomActionButton
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

data class ScanProgressState(
    val isComplete: Boolean = false,
    val gamesFound: Int = 0,
    val boxArtFound: Int = 0,
    val cartridgesFound: Int = 0,
    val manualsFound: Int = 0,
    val recentArtworks: List<String> = emptyList() // Holds up to 9 active image paths/URLs
)

@Composable
fun LibraryScanScreen(
    state: ScanProgressState,
    onStartPlaying: () -> Unit,
    modifier: Modifier = Modifier
) {
    val condensedFontFamily = remember {
        FontFamily(Typeface.create("sans-serif-condensed", Typeface.NORMAL))
    }

    Box(modifier = modifier.fillMaxSize()) {
        DefaultBackground(modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Logo
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "GAME GRL",
                    fontFamily = pressStart2PFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = RetroMagenta,
                    modifier = Modifier.offset(x = 3.dp, y = 3.dp)
                )
                Text(
                    text = "GAME GRL",
                    fontFamily = pressStart2PFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = RetroLime
                )
            }

            // MIDDLE SECTION: 2D Dual-Origin Orbital Rotating BoxArt Wheel
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
                contentAlignment = Alignment.Center
            ) {
                RotatingBoxArtWheel(
                    gamesCount = state.gamesFound,
                    artworks = state.recentArtworks.takeLast(9),
                    isComplete = state.isComplete
                )
            }

            // TRACKING BOX WITH OVERLAPPING GAME GRL & RIGHT-ALIGNED STATS
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 28.dp) // Space for Game GRL hat overflow
            ) {
                // Tracking Card Container
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(RetroPanel)
                        .drawWithContent {
                            drawContent()
                            val stroke = 2.dp.toPx()
                            val cornerRadius = 10.dp.toPx()
                            drawRoundRect(
                                color = RetroBorderLight,
                                topLeft = Offset(stroke / 2f, stroke / 2f),
                                size = Size(size.width - stroke, size.height - stroke),
                                cornerRadius = CornerRadius(cornerRadius, cornerRadius),
                                style = Stroke(width = stroke)
                            )
                        }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 180.dp, top = 16.dp, end = 16.dp, bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatRow(
                            label = "GAMES",
                            value = if (state.isComplete) "${state.gamesFound}" else "${state.gamesFound}",
                            color = RetroLime
                        )
                        StatRow(
                            label = "BOX ART",
                            value = if (state.isComplete) "${state.boxArtFound} OF ${state.gamesFound}" else "${state.boxArtFound}",
                            color = Color.White
                        )
                        StatRow(
                            label = "CARTRIDGES",
                            value = if (state.isComplete) "${state.cartridgesFound} OF ${state.gamesFound}" else "${state.cartridgesFound}",
                            color = Color.White
                        )
                        StatRow(
                            label = "MANUALS",
                            value = if (state.isComplete) "${state.manualsFound} OF ${state.gamesFound}" else "${state.manualsFound}",
                            color = Color.White
                        )
                    }
                }

                // Tall Game GRL Sprite (Overlapping Top-Left)
                Image(
                    painter = painterResource(id = R.drawable.game_grl_casual),
                    contentDescription = "Game GRL",
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .offset(x = 20.dp, y = (-80).dp)
                        .height(235.dp)
                        .graphicsLayer(
                            scaleX = 1.5f,
                            scaleY = 1.5f
                        )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // BOTTOM PHASE: SCANNING VS SCAN DONE
            if (!state.isComplete) {
                // SCANNING PHASE: Notification Panel
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(RetroPanel)
                        .drawWithContent {
                            drawContent()
                            val stroke = 2.dp.toPx()
                            val cornerRadius = 8.dp.toPx()
                            drawRoundRect(
                                color = RetroBorderLight,
                                topLeft = Offset(stroke / 2f, stroke / 2f),
                                size = Size(size.width - stroke, size.height - stroke),
                                cornerRadius = CornerRadius(cornerRadius, cornerRadius),
                                style = Stroke(width = stroke)
                            )
                        }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "HOLD TIGHT! THIS CAN TAKE A FEW MINUTES.",
                            fontFamily = pressStart2PFontFamily,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = RetroLime,
                            lineHeight = 14.sp
                        )

                        Text(
                            text = "You can leave. We will ping you when it is done.",
                            fontFamily = condensedFontFamily,
                            fontSize = 13.sp,
                            color = RetroText
                        )
                    }
                }
            } else {
                // SCAN COMPLETE PHASE: Free-form speech + Start Playing button
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "ALL SET! LET'S PLAY.",
                        fontFamily = pressStart2PFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    val unartworkedGames = state.gamesFound - state.boxArtFound
                    if (unartworkedGames > 0) {
                        Text(
                            text = "$unartworkedGames games have no art. You can scrape them again on the PC and rescan any time.",
                            fontFamily = condensedFontFamily,
                            fontSize = 12.sp,
                            color = RetroText,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    RetroBottomActionButton(
                        label = "START PLAYING",
                        onClick = onStartPlaying,
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = RetroLime,
                        borderColor = RetroText,
                        edgeColor = RetroLimeEdge,
                        textColor = RetroPanel
                    )
                }
            }
        }
    }
}

@Composable
private fun StatRow(
    label: String,
    value: String,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontFamily = pressStart2PFontFamily,
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = value,
            fontFamily = pressStart2PFontFamily,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun RotatingBoxArtWheel(
    gamesCount: Int,
    artworks: List<String>,
    isComplete: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "WheelAnimation")

    val orbitAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -(2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(14000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "OrbitAngle"
    )

    val spinAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(7000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "SpinAngle"
    )

    val density = LocalDensity.current
    val radiusPx = with(density) { 105.dp.toPx() }

    val activeArtworks = remember(artworks) {
        artworks.filter { it.isNotBlank() }.distinct().takeLast(9)
    }
    val count = activeArtworks.size

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // 1. CENTER BIG COUNTER
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "$gamesCount",
                fontFamily = pressStart2PFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 36.sp,
                color = RetroLime,
                textAlign = TextAlign.Center,
                modifier = Modifier.offset(x = 2.dp, y = 2.dp)
            )
            Text(
                text = if (isComplete) "GAMES READY" else "GAMES FOUND",
                fontFamily = pressStart2PFontFamily,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }

        // 2. ORBITING BOX ART (NO BORDER, CORRECT ASPECT RATIO)
        activeArtworks.forEachIndexed { index, artworkUrl ->
            val stepAngle = (2 * PI / count.coerceAtLeast(1)).toFloat()
            val slotAngle = orbitAngle + (index * stepAngle)

            val offsetX = (cos(slotAngle) * radiusPx).roundToInt()
            val offsetY = (sin(slotAngle) * radiusPx).roundToInt()

            Box(
                modifier = Modifier
                    .offset { IntOffset(offsetX, offsetY) }
                    .graphicsLayer {
                        rotationZ = spinAngle
                    }
                    // Standard 3:4 retro box art aspect ratio with rounded corners and NO stroke border
                    .size(54.dp, 72.dp)
                    .clip(RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(artworkUrl)
                        .crossfade(false)
                        .build(),
                    contentDescription = "Scanned Cover",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
