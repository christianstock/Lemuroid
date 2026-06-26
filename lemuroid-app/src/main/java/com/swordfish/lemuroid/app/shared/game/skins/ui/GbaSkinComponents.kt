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
 * GBA D-Pad button component - a plus-shaped control for Game Boy Advance
 */
@Composable
fun GbaDPad(
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
 * GBA Action Buttons group - circular button
 */
@Composable
fun GbaActionButtons(
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
 * GBA L Button (shoulder button)
 */
@Composable
fun GbaLButton(
    color: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(48.dp, 16.dp)
            .background(color, RoundedCornerShape(8.dp))
            .border(1.dp, Color.Black, RoundedCornerShape(8.dp))
    )
}

/**
 * GBA R Button (shoulder button)
 */
@Composable
fun GbaRButton(
    color: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(48.dp, 16.dp)
            .background(color, RoundedCornerShape(8.dp))
            .border(1.dp, Color.Black, RoundedCornerShape(8.dp))
    )
}

/**
 * GBA Select button - horizontal bar
 */
@Composable
fun GbaSelectButton(
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
 * GBA Start button - horizontal bar
 */
@Composable
fun GbaStartButton(
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

