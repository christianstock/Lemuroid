package com.swordfish.lemuroid.app.mobile.feature.home

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swordfish.lemuroid.app.mobile.shared.RetroBorderLight
import com.swordfish.lemuroid.app.mobile.shared.RetroMagenta
import com.swordfish.lemuroid.app.mobile.shared.RetroPanel
import com.swordfish.lemuroid.app.mobile.shared.RetroText
import com.swordfish.lemuroid.app.mobile.shared.pressStart2PFontFamily
import com.swordfish.lemuroid.app.shared.cheats.ui.RetroBottomActionButton


@Composable
fun HomeNotification(
    @DrawableRes imageId: Int,
    titleId: Int,
    messageId: Int,
    actionId: Int,
    enabled: Boolean = true,
    onAction: () -> Unit = {}
) {
    val condensedFontFamily = remember {
        FontFamily(android.graphics.Typeface.create("sans-serif-condensed", android.graphics.Typeface.NORMAL))
    }

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
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius, cornerRadius),
                    style = Stroke(width = stroke)
                )
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Top Row: Image + Header + Message
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Image(
                    painter = painterResource(id = imageId),
                    contentDescription = "Notification Icon",
                    modifier = Modifier
                        .size(48.dp)
                        .padding(horizontal = 4.dp)
                        .graphicsLayer(scaleX = 1.2f, scaleY = 1.2f)
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = stringResource(titleId).uppercase(),
                        fontFamily = pressStart2PFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = RetroText
                    )

                    Text(
                        text = stringResource(messageId),
                        fontFamily = condensedFontFamily,
                        fontSize = 12.sp,
                        color = RetroText
                    )
                }
            }

            // Bottom Row: BONUS QUEST on the left, Action Button on the right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "BONUS QUEST",
                    fontFamily = pressStart2PFontFamily,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = RetroMagenta
                )

                RetroBottomActionButton(
                    label = stringResource(id = actionId).uppercase(),
                    onClick = onAction,
                    isLoading = !enabled,
                    modifier = Modifier.width(120.dp)
                )
            }
        }
    }
}
