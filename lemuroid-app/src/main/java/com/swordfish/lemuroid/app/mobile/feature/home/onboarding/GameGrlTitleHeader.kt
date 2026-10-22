package com.swordfish.lemuroid.app.mobile.feature.home.onboarding

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swordfish.lemuroid.app.mobile.shared.RetroLime
import com.swordfish.lemuroid.app.mobile.shared.RetroMagenta
import com.swordfish.lemuroid.app.mobile.shared.pressStart2PFontFamily


/**
 * Modular Header Title for GAME GRL with pixel shadow offset
 */
@Composable
fun GameGrlTitleHeader(
    modifier: Modifier = Modifier,
    fontSize: Int = 18
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        // Magenta Shadow Layer (Offset)
        Text(
            text = "GAME GRL",
            fontFamily = pressStart2PFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = fontSize.sp,
            color = RetroMagenta,
            textAlign = TextAlign.Center,
            modifier = Modifier.offset(x = 3.dp, y = 3.dp)
        )
        // Lime Foreground Layer
        Text(
            text = "GAME GRL",
            fontFamily = pressStart2PFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = fontSize.sp,
            color = RetroLime,
            textAlign = TextAlign.Center
        )
    }
}
