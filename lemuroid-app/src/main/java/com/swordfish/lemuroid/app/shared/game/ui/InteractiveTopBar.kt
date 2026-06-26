package com.swordfish.lemuroid.app.shared.game.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swordfish.touchinput.radial.LocalLemuroidPadTheme

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun InteractiveTopBar(
    isPlaying: Boolean,
    isRewindAvailable: Boolean,
    onSaveClick: () -> Unit,
    onSaveLongClick: () -> Unit,
    onLoadClick: () -> Unit,
    onLoadLongClick: () -> Unit,
    onRewindClick: (pressed: Boolean) -> Unit,
    onPauseToggle: () -> Unit,
    onMenuClick: (pressed: Boolean) -> Unit,
    modifier: Modifier = Modifier,
    isMenuPressed: Boolean = false,
) {
    val rewindPressed = remember { mutableStateOf(false) }
    val theme = LocalLemuroidPadTheme.current
    val buttonLabelColor = theme.level0Fill.copy(alpha = 0.7f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(84.dp)
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Save Button
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .weight(1f)
                    .combinedClickable(
                        onClick = onSaveClick,
                        onLongClick = onSaveLongClick
                    )
            ) {
                Text(
                    modifier = Modifier
                        .wrapContentSize()
                        .graphicsLayer(
                            scaleX = 1.0f, // Adjust this down (e.g., 0.6f) to make it even narrower!
                            scaleY = 1.2f  // Keeps vertical height exactly the same
                        ),
                    text = "SAVE",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = buttonLabelColor
                )
            }

            // Load Button
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .weight(1f)
                    .combinedClickable(
                        onClick = onLoadClick,
                        onLongClick = onLoadLongClick
                    )
            ) {
                Text(
                    modifier = Modifier
                        .wrapContentSize()
                        .graphicsLayer(
                            scaleX = 1.0f, // Adjust this down (e.g., 0.6f) to make it even narrower!
                            scaleY = 1.2f  // Keeps vertical height exactly the same
                        ),
                    text = "LOAD",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = buttonLabelColor
                )
            }

            // Rewind Button
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .weight(1f)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                if (isRewindAvailable) {
                                    rewindPressed.value = true
                                    onRewindClick(true)
                                    tryAwaitRelease()
                                    rewindPressed.value = false
                                    onRewindClick(false)
                                }
                            },
                        )
                    }
            ) {
                Text(
                    modifier = Modifier
                        .wrapContentSize()
                        .graphicsLayer(
                            scaleX = 1.0f, // Adjust this down (e.g., 0.6f) to make it even narrower!
                            scaleY = 1.2f  // Keeps vertical height exactly the same
                        ),
                    text = "REWIND",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = buttonLabelColor
                )
            }

            // Pause Button
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .combinedClickable(onClick = onPauseToggle)
                ) {
                    Text(
                        modifier = Modifier
                            .wrapContentSize()
                            .graphicsLayer(
                                scaleX = 1.0f, // Adjust this down (e.g., 0.6f) to make it even narrower!
                                scaleY = 1.2f  // Keeps vertical height exactly the same
                            ),
                        text = "PAUSE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = buttonLabelColor
                    )
                }
            }

            // Menu Button
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .weight(1f)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                onMenuClick(true)
                                tryAwaitRelease()
                                onMenuClick(false)
                            }
                        )
                    }
            ) {
                Text(
                    modifier = Modifier
                        .wrapContentSize()
                        .graphicsLayer(
                            scaleX = 1.0f, // Adjust this down (e.g., 0.6f) to make it even narrower!
                            scaleY = 1.2f  // Keeps vertical height exactly the same
                        ),
                    text = "MENU",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = buttonLabelColor
                )
            }
        }
    }
}



