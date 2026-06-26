package com.swordfish.lemuroid.app.shared.game.skins.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * GBC D-Pad button component - a plus-shaped control
 */
@Composable
fun GbcDPad(
    color: Color,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.size(40.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.size(8.dp, 4.dp).background(color))
            Row {
                Box(modifier = Modifier.size(4.dp, 8.dp).background(color))
                Box(modifier = Modifier.size(8.dp, 8.dp))
                Box(modifier = Modifier.size(4.dp, 8.dp).background(color))
            }
            Box(modifier = Modifier.size(8.dp, 4.dp).background(color))
        }
    }
}

/**
 * GBC Action Buttons group - circular button
 */
@Composable
fun GbcActionButtons(
    color: Color,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.size(50.dp), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, CircleShape)
                .border(1.dp, Color.Black, CircleShape)
        )
    }
}

/**
 * GBC Select button - horizontal bar
 */
@Composable
fun GbcSelectButton(
    color: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(28.dp, 10.dp)
            .background(Color(0xFF37393A), RoundedCornerShape(5.dp))
            .border(1.dp, Color.Black, RoundedCornerShape(5.dp))
    )
}

/**
 * GBC Start button - horizontal bar
 */
@Composable
fun GbcStartButton(
    color: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(28.dp, 10.dp)
            .background(Color(0xFF37393A), RoundedCornerShape(5.dp))
            .border(1.dp, Color.Black, RoundedCornerShape(5.dp))
    )
}

