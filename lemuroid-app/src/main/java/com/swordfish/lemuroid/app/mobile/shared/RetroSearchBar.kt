package com.swordfish.lemuroid.app.mobile.shared

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swordfish.lemuroid.R

val pressStart2PFontFamily = FontFamily(Font(R.font.press_start_2p))

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RetroSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onExitClick: () -> Unit,
    modifier: Modifier = Modifier,
    placeholderText: String = "SEARCH..."
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Black Background Search Input with RetroLime Outline
        Box(
            modifier = Modifier
                .weight(1f)
                .height(42.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(RetroPanel)
                .drawWithContent {
                    drawContent()
                    val stroke = 2.dp.toPx()
                    val cornerRadius = 32.dp.toPx()
                    drawRoundRect(
                        color = RetroLime,
                        topLeft = Offset(stroke / 2f, stroke / 2f),
                        size = Size(size.width - stroke, size.height - stroke),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius, cornerRadius),
                        style = Stroke(width = stroke)
                    )
                }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Bolder Search Icon
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = RetroLime,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    cursorBrush = SolidColor(RetroLime),
                    textStyle = TextStyle(
                        fontFamily = pressStart2PFontFamily,
                        fontSize = 12.sp,
                        color = RetroLime
                    ),
                    modifier = Modifier.weight(1f),
                    decorationBox = { innerTextField ->
                        Box(contentAlignment = Alignment.CenterStart) {
                            if (query.isEmpty()) {
                                Text(
                                    text = placeholderText,
                                    fontFamily = pressStart2PFontFamily,
                                    fontSize = 10.sp,
                                    color = Color.DarkGray
                                )
                            }
                            innerTextField()
                        }
                    }
                )

                if (query.isNotEmpty()) {
                    IconButton(
                        onClick = { onQueryChange("") },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear",
                            tint = RetroLime,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // Magenta Exit Button with RetroMagentaEdge Border
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(RetroMagenta)
                .drawWithContent {
                    drawContent()
                    val stroke = 4.dp.toPx()
                    drawCircle(
                        color = RetroMagentaEdge,
                        radius = (size.minDimension / 2f) - (stroke / 2f),
                        style = Stroke(width = stroke)
                    )
                }
                .combinedClickable(onClick = onExitClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Exit Screen",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
