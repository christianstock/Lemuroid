package com.swordfish.touchinput.radial.ui

import androidx.benchmark.traceprocessor.Row
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swordfish.lemuroid.common.compose.textUnit
import com.swordfish.touchinput.radial.LocalLemuroidPadTheme

@Composable
fun LemuroidButtonForeground(
    modifier: Modifier = Modifier,
    pressed: State<Boolean>,
    label: (@Composable BoxWithConstraintsScope.() -> Unit),
    icon: (@Composable BoxWithConstraintsScope.() -> Unit),
) {
    val theme = LocalLemuroidPadTheme.current

    GlassSurface(
        modifier = modifier.fillMaxSize().padding(theme.foregroundPadding),
        fillColor = theme.foregroundFill(pressed.value),
        shadowColor = theme.level3Shadow,
        shadowWidth = theme.level3ShadowWidth,
        content = {
            icon()
            label()
        },
    )
}

@Composable
fun LemuroidButtonForeground(
    modifier: Modifier = Modifier,
    pressed: State<Boolean>,
    label: String? = null,
    icon: Int? = null,
    iconScale: Float = 0.5f,
    labelScale: Float = 1.0f,
) {
    LemuroidButtonForeground(
        modifier = modifier,
        pressed = pressed,
        label = { LemuroidButtonForegroundLabel(label, labelScale, pressed) },
        icon = { LemuroidButtonForegroundIcon(icon, iconScale, pressed) },
    )
}

@Composable
private fun BoxWithConstraintsScope.LemuroidButtonForegroundIcon(
    icon: Int?,
    scale: Float,
    pressedState: State<Boolean>,
) {
    if (icon == null) return

    Icon(
        modifier = Modifier.size(maxWidth * scale, maxHeight * scale),
        painter = painterResource(icon),
        contentDescription = "",
        tint = LocalLemuroidPadTheme.current.icons(pressedState.value),
    )
}

@Composable
private fun BoxWithConstraintsScope.LemuroidButtonForegroundLabel(
    label: String?,
    scale: Float,
    pressedState: State<Boolean>,
) {
    if (label == null) return
    val fontSize = minOf(maxHeight * 0.5f * scale, maxWidth / label.length * scale)
    Text(
        modifier = Modifier.wrapContentSize(),
        textAlign = TextAlign.Center,
        fontWeight = FontWeight.Bold,
        text = label,
        color = LocalLemuroidPadTheme.current.icons(pressedState.value),
        fontSize = fontSize.textUnit(),
    )
}


@Composable
fun GbButtonForeground(
    modifier: Modifier = Modifier,
    pressed: State<Boolean>,
    label: String? = null,
    icon: Int? = null,
    iconScale: Float = 0.6f,
    labelScale: Float = 1.0f,
) {
    Column(
        modifier = modifier
            .fillMaxWidth(0.7f) // Keeps the button itself shrunk down nicely
            .graphicsLayer {
                rotationZ = -30f // Entire structure is rotated up together
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. GBA-Style Thin Pillow Button Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2.6f)
                .background(
                    color = if (pressed.value) Color(0xFF444444) else Color(0xFF666666),
                    shape = RoundedCornerShape(percent = 50)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (icon != null) {
                Icon(
                    modifier = Modifier.fillMaxSize(iconScale),
                    painter = painterResource(icon),
                    contentDescription = null,
                    tint = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        // 2. Text Label that overflows layout bounds horizontally instead of wrapping
        if (label != null) {
            Text(
                modifier = Modifier
                    .layout { measurable, constraints ->
                        // FIXED: Measure the text with completely unconstrained max width
                        // to guarantee it stays strictly on one line.
                        val placeable = measurable.measure(constraints.copy(maxWidth = Int.MAX_VALUE))

                        // Report the real height, but pretend the width matches the
                        // parent column's width constraint to stop it from pushing the column out.
                        layout(constraints.maxWidth, placeable.height) {
                            // Center the overflowing text horizontally over the button axis
                            val xOffset = (constraints.maxWidth - placeable.width) / 2
                            placeable.place(xOffset, 0)
                        }
                    },
                textAlign = TextAlign.Center,
                maxLines = 1, // Enforce single line behavior
                softWrap = false, // Stop internal wrapping engines
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                text = label,
                color = Color(0xFF3639a0),
                fontSize = (12f * labelScale).sp
            )
        }
    }
}

@Composable
private fun BoxWithConstraintsScope.GbButtonForegroundIcon(
    icon: Int?,
    scale: Float,
    pressedState: State<Boolean>,
) {
    if (icon == null) return

    Icon(
        modifier = Modifier.size(maxWidth * scale, maxHeight * scale),
        painter = painterResource(icon),
        contentDescription = "",
        tint = LocalLemuroidPadTheme.current.icons(pressedState.value),
    )
}

@Composable
fun GbcButtonForeground(
    modifier: Modifier = Modifier,
    pressed: State<Boolean>,
    label: (@Composable BoxWithConstraintsScope.() -> Unit),
    icon: (@Composable BoxWithConstraintsScope.() -> Unit),
) {
    val theme = LocalLemuroidPadTheme.current

    // Darken the button body color on press
    val baseFillColor = theme.foregroundFill(pressed.value)
    val finalFillColor = if (pressed.value) {
        Color(
            red = baseFillColor.red * 0.50f,
            green = baseFillColor.green * 0.50f,
            blue = baseFillColor.blue * 0.50f,
            alpha = baseFillColor.alpha
        )
    } else {
        baseFillColor
    }

    Column(
        modifier = modifier.padding(theme.foregroundPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Horizontal Pill Shape
        GlassSurface(
            modifier = Modifier.size(width = 36.dp, height = 12.dp),
            cornerRadius = 6.dp,
            fillColor = finalFillColor,
            shadowColor = theme.level3Shadow,
            shadowWidth = theme.level3ShadowWidth,
            content = {
                this.icon()
            },
        )

        // FIXED: Using unbounded = true allows the label container to break
        // through any forced width limitations from the layout grid.
        Box(
            modifier = Modifier
                .padding(top = 20.dp)
                .wrapContentSize(align = Alignment.Center, unbounded = true),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.foundation.layout.BoxWithConstraints(
                modifier = Modifier.wrapContentSize(unbounded = true),
                contentAlignment = Alignment.Center
            ) {
                this.label()
            }
        }
    }
}

@Composable
fun GbcButtonForeground(
    modifier: Modifier = Modifier,
    pressed: State<Boolean>,
    label: String? = null,
    icon: Int? = null,
    iconScale: Float = 0.6f,
    labelScale: Float = 1.0f,
) {
    GbcButtonForeground(
        modifier = modifier,
        pressed = pressed, // Let the parent handle the actual dark button press state
        label = { GbcButtonForegroundLabelComposable(label, labelScale) },
        icon = { GbcButtonForegroundIcon(icon, iconScale, pressed) },
    )
}

@Composable
private fun BoxWithConstraintsScope.GbcButtonForegroundIcon(
    icon: Int?,
    scale: Float,
    pressedState: State<Boolean>,
) {
    if (icon == null) return

    Icon(
        modifier = Modifier.size(maxWidth * scale, maxHeight * scale),
        painter = painterResource(icon),
        contentDescription = "",
        tint = LocalLemuroidPadTheme.current.icons(pressedState.value),
    )
}

@Composable
private fun GbcButtonForegroundLabelComposable(
    label: String?,
    scale: Float,

) {
    if (label == null) return

    val theme = LocalLemuroidPadTheme.current



// Create a darker shade of the theme color for the label

    val labelColor = theme.icons(false).let { baseColor ->
        Color(
            red = (baseColor.red).coerceAtMost(1f),
            green = (baseColor.green).coerceAtMost(1f),
            blue = (baseColor.blue).coerceAtMost(1f),
            alpha = baseColor.alpha * 0.4f,
            )
        }
    Text(
        modifier = Modifier
            .wrapContentSize()
            .graphicsLayer(
                scaleX = 1.0f, // Adjust this down (e.g., 0.6f) to make it even narrower!
                scaleY = 1.2f  // Keeps vertical height exactly the same
            ),
        textAlign = TextAlign.Center,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.SansSerif,
        text = label,
        color = labelColor,
        // FIXED: Restored clean, legible text scaling independent of the button width bounds
        fontSize = (16.dp * scale).textUnit(),
    )
}

@Composable
fun GbcRoundButton(
    pressed: State<Boolean>,
    label: String
) {
    // Pick your favorite color palette (e.g., deep maroon/dark purple for GBC)
    val theme = LocalLemuroidPadTheme.current
    val baseColor = theme.foregroundFill(pressed.value)
    val buttonColor = if (pressed.value) baseColor.copy(alpha = 0.7f) else baseColor
    val labelColor = buttonColor.copy(alpha = 0.5f)

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // FIXED: ...but this inner Box strictly enforces the exact physical size of your circle!
        Box(
            modifier = Modifier
                .size(60.dp) // Adjust this number up or down to make the circle exactly the size you want
                .background(color = buttonColor, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                modifier = Modifier
                    .wrapContentSize()
                    .graphicsLayer(
                        scaleX = 1.0f, // Adjust this down (e.g., 0.6f) to make it even narrower!
                        scaleY = 1.2f  // Keeps vertical height exactly the same
                    ),
                text = label,
                color = labelColor,
                fontWeight = FontWeight.Bold,
                // Pro-Tip: If you make the button tiny, drop this font size down (e.g., 12.sp) so it fits!
                fontSize = 32.sp
            )
        }
    }
}


@Composable
fun GbaRoundButton(
    pressed: State<Boolean>,
    label: String
) {
    // Pick your favorite color palette (e.g., deep maroon/dark purple for GBC)
    val theme = LocalLemuroidPadTheme.current
    val baseColor = theme.foregroundFill(pressed.value)
    val buttonColor = if (pressed.value) baseColor.copy(alpha = 0.7f) else baseColor
    val labelColor = buttonColor.copy(alpha = 0.5f)

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // FIXED: ...but this inner Box strictly enforces the exact physical size of your circle!
        Box(
            modifier = Modifier
                .size(60.dp) // Adjust this number up or down to make the circle exactly the size you want
                .background(color = buttonColor, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                modifier = Modifier
                    .wrapContentSize()
                    .graphicsLayer(
                        scaleX = 1.0f, // Adjust this down (e.g., 0.6f) to make it even narrower!
                        scaleY = 1.2f  // Keeps vertical height exactly the same
                    ),
                text = label,
                color = labelColor,
                fontWeight = FontWeight.Bold,
                // Pro-Tip: If you make the button tiny, drop this font size down (e.g., 12.sp) so it fits!
                fontSize = 32.sp
            )
        }
    }
}

@Composable
fun GbaButtonForeground(
    modifier: Modifier = Modifier,
    pressed: State<Boolean>,
    label: String? = null,
    icon: Int? = null,
    iconScale: Float = 0.6f,
    labelScale: Float = 1.0f,
) {
    val theme = LocalLemuroidPadTheme.current

    val baseFillColor = theme.foregroundFill(pressed.value)
    val containerBgColor = if (pressed.value) {
        baseFillColor.copy(alpha = baseFillColor.alpha * 0.7f)
    } else {
        baseFillColor.copy(alpha = baseFillColor.alpha * 0.4f)
    }

    Row(
        modifier = modifier
            // FIXED: Prevent the parent ControlButton framework from truncating our oval width and height
            .layout { measurable, constraints ->
                val placeable = measurable.measure(
                    constraints.copy(
                        maxWidth = Int.MAX_VALUE,
                        maxHeight = Int.MAX_VALUE
                    )
                )
                layout(placeable.width, placeable.height) {
                    placeable.place(0, 0)
                }
            }
            .graphicsLayer {
                rotationZ = 10f // 10-degree downward tilt
                translationX = -30f.dp.toPx()
                translationY = 10f.dp.toPx()
            }
            .background(
                color = containerBgColor,
                shape = RoundedCornerShape(percent = 50)
            )
            // Generous padding to make sure the oval background wraps beautifully around everything
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Label on the Left
        if (label != null) {
            val labelColor = theme.icons(false).let { baseColor ->
                Color(
                    red = baseColor.red.coerceAtMost(1f),
                    green = baseColor.green.coerceAtMost(1f),
                    blue = baseColor.blue.coerceAtMost(1f),
                    alpha = baseColor.alpha * 0.8f,
                )
            }

            Text(
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = 0.75f
                    }
                    .layout { measurable, constraints ->
                        val placeable = measurable.measure(constraints.copy(maxWidth = Int.MAX_VALUE))
                        layout((placeable.width * 0.75f).toInt(), placeable.height) {
                            placeable.place(0, 0)
                        }
                    },
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                text = label,
                color = labelColor,
                fontSize = (14f * labelScale).sp,
                maxLines = 1,
                softWrap = false
            )

            Spacer(modifier = Modifier.width(8.dp))
        }

        // Small Round Button on the Right
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(
                    color = theme.foregroundFill(pressed.value),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (icon != null) {
                Icon(
                    modifier = Modifier.size(24.dp * iconScale),
                    painter = painterResource(icon),
                    contentDescription = "",
                    tint = theme.icons(pressed.value),
                )
            }
        }
    }
}

@Composable
fun GbaSideButtonForeground(
    modifier: Modifier = Modifier,
    pressed: State<Boolean>,
    label: String? = null,
    icon: Int? = null,
    iconScale: Float = 0.5f,
    labelScale: Float = 1.0f,
) {
    if (label == null) return

    Box(
        modifier = modifier.fillMaxSize(), // Accept whatever square size the parent forces
        contentAlignment = Alignment.Center
    ) {
        Text(
            modifier = Modifier
                .wrapContentSize()
                .graphicsLayer {
                    alpha = if (pressed.value) 0.5f else 1.0f

                    // THE ULTIMATE HACK:
                    // Force the render layer to stretch 4x wider than the parent container,
                    // completely bypassing the layout engine's square constraints.
                    scaleX = 4.0f
                    scaleY = 1.2f // Slightly bump height if needed, keeping it elongated
                },
            text = label,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp, // Start smaller so stretching doesn't make it pixelated or huge
            color = LocalLemuroidPadTheme.current.icons(pressed.value)
        )
    }
}
