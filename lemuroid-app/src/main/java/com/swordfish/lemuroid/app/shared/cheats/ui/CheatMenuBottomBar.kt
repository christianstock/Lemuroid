package com.swordfish.lemuroid.app.shared.cheats.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swordfish.lemuroid.app.mobile.shared.RetroBorder
import com.swordfish.lemuroid.app.mobile.shared.RetroBorderLight
import com.swordfish.lemuroid.app.mobile.shared.RetroPanel
import com.swordfish.lemuroid.app.mobile.shared.RetroPanelLight
import com.swordfish.lemuroid.app.mobile.shared.RetroText
import com.swordfish.lemuroid.app.mobile.shared.pressStart2PFontFamily

@Composable
fun CheatMenuBottomBar(
    isRescanning: Boolean,
    onRescanCheats: () -> Unit,
    onDisableAllCheats: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(RetroPanelLight)
            .drawWithContent {
                drawContent()
                drawLine(
                    color = RetroBorder,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 4.dp.toPx()
                )
            }
            .padding(top = 12.dp)
            .padding(horizontal = 16.dp)
            .navigationBarsPadding(), // Pushes the entire bottom row container cleanly above the nav bar!
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        RetroBottomActionButton(
            label = "RESCAN",
            isLoading = isRescanning,
            onClick = onRescanCheats,
            modifier = Modifier.weight(1f)
        )

        RetroBottomActionButton(
            label = "UNSELECT ALL",
            onClick = onDisableAllCheats,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun RetroBottomActionButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
) {
    val outerShape = RoundedCornerShape(8.dp)
    val faceShape = RoundedCornerShape(
        topStart = 6.dp, topEnd = 6.dp,
        bottomStart = 2.dp, bottomEnd = 2.dp,
    )

    Box(
        modifier = modifier
            .height(60.dp)
            .alpha(if (isLoading) 0.5f else 1f)
            .clip(outerShape)
            .background(RetroBorderLight)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = !isLoading,
                role = Role.Button,
                onClick = onClick,
            ),
    ) {
        // Bottom band, drawn over the outline so it has no border of its own
        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(8.dp)
                .background(RetroPanel)
        )

        // Face: 2 dp inside on top and sides, 6 dp on the bottom
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 2.dp, top = 2.dp, end = 2.dp, bottom = 8.dp)
                .clip(faceShape)
                .background(RetroPanelLight),
            contentAlignment = Alignment.Center,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = label,
                    fontFamily = com.swordfish.lemuroid.app.shared.cheats.ui.pressStart2PFontFamily,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = RetroText,
                )
            }
        }
    }
}
