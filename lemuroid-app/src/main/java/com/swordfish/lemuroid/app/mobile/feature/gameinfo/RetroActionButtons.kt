package com.swordfish.lemuroid.app.mobile.feature.gameinfo

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swordfish.lemuroid.app.mobile.feature.gameinfo.PressStart2PFont

@Composable
fun RetroActionBar(
    hasManual: Boolean,
    isRescanning: Boolean,
    onCheats: () -> Unit,
    onManual: () -> Unit,
    onRescan: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        RetroActionTextButton(
            icon = Icons.Default.Star,
            iconColor = Color(0xFFFFD600),
            label = "CHEATS",
            onClick = onCheats
        )

        if (hasManual) {
            RetroActionTextButton(
                icon = Icons.Default.Description,
                iconColor = Color(0xFF29B6F6),
                label = "MANUAL",
                onClick = onManual
            )
        }

        RetroActionTextButton(
            icon = Icons.Default.Refresh,
            iconColor = Color(0xFF69F0AE),
            label = "RESCAN",
            isLoading = isRescanning,
            onClick = onRescan
        )
    }
}

@Composable
private fun RetroActionTextButton(
    icon: ImageVector,
    iconColor: Color,
    label: String,
    onClick: () -> Unit,
    isLoading: Boolean = false
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(8.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(32.dp),
                color = iconColor,
                strokeWidth = 2.5.dp
            )
        } else {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(48.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontFamily = PressStart2PFont,
            fontSize = 8.sp,
            color = Color.White.copy(alpha = 0.9f)
        )
    }
}
