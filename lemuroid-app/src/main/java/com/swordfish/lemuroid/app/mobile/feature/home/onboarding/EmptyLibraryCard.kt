package com.swordfish.lemuroid.app.mobile.feature.home.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.mobile.shared.RetroBorderLight
import com.swordfish.lemuroid.app.mobile.shared.RetroCheckbox
import com.swordfish.lemuroid.app.mobile.shared.RetroGold
import com.swordfish.lemuroid.app.mobile.shared.RetroLime
import com.swordfish.lemuroid.app.mobile.shared.RetroLimeEdge
import com.swordfish.lemuroid.app.mobile.shared.RetroPanel
import com.swordfish.lemuroid.app.mobile.shared.RetroText
import com.swordfish.lemuroid.app.mobile.shared.RetroViolet
import com.swordfish.lemuroid.app.mobile.shared.pressStart2PFontFamily
import com.swordfish.lemuroid.app.shared.cheats.ui.RetroBottomActionButton

@Composable
fun EmptyLibraryCard(
    indexInProgress: Boolean,
    onSelectDirectory: () -> Unit,
    modifier: Modifier = Modifier,
    cheatsEnabled: Boolean = true,
    onCheatsToggled: (Boolean) -> Unit = {},
    trophiesEnabled: Boolean = true,
    onTrophiesToggled: (Boolean) -> Unit = {}
) {
    val condensedFontFamily = remember {
        FontFamily(android.graphics.Typeface.create("sans-serif-condensed", android.graphics.Typeface.NORMAL))
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(RetroPanel)
                .drawWithContent {
                    drawContent()
                    val stroke = 2.dp.toPx()
                    val cornerRadius = 12.dp.toPx()
                    drawRoundRect(
                        color = RetroBorderLight,
                        topLeft = Offset(stroke / 2f, stroke / 2f),
                        size = Size(size.width - stroke, size.height - stroke),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius, cornerRadius),
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
                Text(
                    text = "MAIN QUEST",
                    fontFamily = pressStart2PFontFamily,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = RetroLime
                )

                Text(
                    text = stringResource(R.string.home_empty_title).uppercase(),
                    fontFamily = pressStart2PFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = stringResource(R.string.home_empty_message),
                    fontFamily = condensedFontFamily,
                    fontSize = 14.sp,
                    color = RetroText
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "EXTRAS",
                    fontFamily = pressStart2PFontFamily,
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Bold,
                    color = RetroText
                )

                // Checkbox Row 1: CHEATS (Entire row is clickable for easy tapping)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { onCheatsToggled(!cheatsEnabled) }
                        .padding(vertical = 2.dp)
                ) {
                    RetroCheckbox(
                        checked = cheatsEnabled,
                        color = RetroViolet
                    )
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = RetroViolet,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "CHEATS",
                        fontFamily = pressStart2PFontFamily,
                        fontSize = 9.sp,
                        color = RetroViolet
                    )
                }

                // Checkbox Row 2: TROPHIES (Entire row is clickable)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { onTrophiesToggled(!trophiesEnabled) }
                        .padding(vertical = 2.dp)
                ) {
                    RetroCheckbox(
                        checked = trophiesEnabled,
                        color = RetroGold
                    )
                    Icon(
                        imageVector = Icons.Filled.EmojiEvents,
                        contentDescription = null,
                        tint = Color(0xFFFFB703),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "TROPHIES",
                        fontFamily = pressStart2PFontFamily,
                        fontSize = 9.sp,
                        color = Color(0xFFFFB703)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                RetroBottomActionButton(
                    label = stringResource(R.string.home_empty_action).uppercase(),
                    onClick = onSelectDirectory,
                    isLoading = indexInProgress,
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = RetroLime,
                    borderColor = RetroText,
                    edgeColor = RetroLimeEdge,
                    textColor = RetroPanel
                )
            }
        }

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
}
