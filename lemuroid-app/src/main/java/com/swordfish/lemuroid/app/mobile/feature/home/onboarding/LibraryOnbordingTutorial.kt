package com.swordfish.lemuroid.app.mobile.feature.home.onboarding

import android.graphics.Typeface
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.mobile.shared.DefaultBackground
import com.swordfish.lemuroid.app.mobile.shared.RetroBorderLight
import com.swordfish.lemuroid.app.mobile.shared.RetroGold
import com.swordfish.lemuroid.app.mobile.shared.RetroLime
import com.swordfish.lemuroid.app.mobile.shared.RetroLimeEdge
import com.swordfish.lemuroid.app.mobile.shared.RetroMagenta
import com.swordfish.lemuroid.app.mobile.shared.RetroPanel
import com.swordfish.lemuroid.app.mobile.shared.RetroText
import com.swordfish.lemuroid.app.mobile.shared.pressStart2PFontFamily
import com.swordfish.lemuroid.app.shared.cheats.ui.RetroBottomActionButton
import com.swordfish.lemuroid.lib.achievements.RetroAchievementsApi
import com.swordfish.lemuroid.lib.preferences.SharedPreferencesHelper
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient

@Composable
fun LibraryOnboardingScreen(
    indexInProgress: Boolean,
    onSelectDirectory: () -> Unit,
    modifier: Modifier = Modifier,
    trophiesEnabled: Boolean = true,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val condensedFontFamily = remember {
        FontFamily(Typeface.create("sans-serif-condensed", Typeface.NORMAL))
    }

    val prefs = remember { SharedPreferencesHelper.getSharedPreferences(context) }
    var raUsername by remember { mutableStateOf(prefs.getString("ra_username", "") ?: "") }
    var raPasswordOrToken by remember {
        mutableStateOf(
            prefs.getString("ra_password", "")?.takeIf { it.isNotBlank() }
                ?: prefs.getString("ra_token", "")
                ?: ""
        )
    }
    var isVerifying by remember { mutableStateOf(false) }
    var verifyStatus by remember { mutableStateOf<String?>(null) }

    fun saveCredentials(user: String, pass: String) {
        prefs.edit()
            .putString("ra_username", user)
            .putString("ra_token", pass)
            .putString("ra_password", pass)
            .apply()
    }

    Box(modifier = modifier.fillMaxSize()) {
        DefaultBackground(modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Logo
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "GAME GRL",
                    fontFamily = pressStart2PFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = RetroMagenta,
                    modifier = Modifier.offset(x = 3.dp, y = 3.dp)
                )
                Text(
                    text = "GAME GRL",
                    fontFamily = pressStart2PFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = RetroLime
                )
            }

            // Top Free-Flow Speech Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "PSST! YOUR GAMES NEED ARTWORK.\nLET'S GET IT READY ON YOUR PC.",
                        fontFamily = pressStart2PFontFamily,
                        fontSize = 8.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // STEP 1: Skraper Instructions Box with Tall Game GRL Overlapping
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 28.dp) // Space for sprite top overflow
            ) {
                // Card Box Container
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(RetroPanel)
                        .drawWithContent {
                            drawContent()
                            val stroke = 2.dp.toPx()
                            val cornerRadius = 10.dp.toPx()
                            drawRoundRect(
                                color = RetroBorderLight,
                                topLeft = Offset(stroke / 2f, stroke / 2f),
                                size = Size(size.width - stroke, size.height - stroke),
                                cornerRadius = CornerRadius(cornerRadius, cornerRadius),
                                style = Stroke(width = stroke)
                            )
                        }
                ) {
                    // Vertical column holding both the top content row and the full-width bottom note
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        // Top Section: Leaves 160.dp start padding for Game GRL sprite
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 160.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Step Badge + Header
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                StepBadge(number = "1")
                                Text(
                                    text = "GET SKRAPER.NET",
                                    fontFamily = pressStart2PFontFamily,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            Text(
                                text = "Add your rom folders and enable these:",
                                fontFamily = condensedFontFamily,
                                fontSize = 12.sp,
                                color = RetroText,
                                lineHeight = 14.sp
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            // Non-interactive instruction checklist
                            InstructionCheckItem(
                                title = "EMULATIONSTATION",
                                subtitle = "metadata"
                            )
                            InstructionCheckItem(title = "BOX 2D", subtitle = "front cover")
                            InstructionCheckItem(title = "BOX 2D (BACK)", subtitle = "back cover")
                            InstructionCheckItem(title = "SUPPORT", subtitle = "cartridge art")
                            InstructionCheckItem(title = "MANUAL", subtitle = "game manual")
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Full-width note at the bottom of the Step 1 panel
                        Text(
                            text = "SKIP, IF YOU JUST WANT TO PLAY. I'LL STILL FIND YOUR ROMS AND BOX ART.",
                            fontFamily = pressStart2PFontFamily,
                            fontSize = 8.sp,
                            lineHeight = 16.sp,
                            color = RetroText,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Tall Game GRL Sprite (peeking out top-left)
                Image(
                    painter = painterResource(id = R.drawable.game_grl_casual),
                    contentDescription = "Game GRL",
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .offset(x = 20.dp, y = (-120).dp)
                        .height(235.dp)
                        .graphicsLayer(
                            scaleX = 1.5f,
                            scaleY = 1.5f
                        )
                )
            }

            // STEP 2: Copy Folder Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(RetroPanel)
                    .drawWithContent {
                        drawContent()
                        val stroke = 2.dp.toPx()
                        val cornerRadius = 10.dp.toPx()
                        drawRoundRect(
                            color = RetroBorderLight,
                            topLeft = Offset(stroke / 2f, stroke / 2f),
                            size = Size(size.width - stroke, size.height - stroke),
                            cornerRadius = CornerRadius(cornerRadius, cornerRadius),
                            style = Stroke(width = stroke)
                        )
                    }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StepBadge(number = "2")
                        Text(
                            text = "COPY THE WHOLE FOLDER",
                            fontFamily = pressStart2PFontFamily,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Text(
                        text = "Copy from PC to your device storage.",
                        fontFamily = condensedFontFamily,
                        fontSize = 12.sp,
                        color = RetroText
                    )
                }
            }

            // STEP 3 (Optional RetroAchievements credentials step when trophies are checked)
            if (trophiesEnabled) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(RetroPanel)
                        .drawWithContent {
                            drawContent()
                            val stroke = 2.dp.toPx()
                            val cornerRadius = 10.dp.toPx()
                            drawRoundRect(
                                color = RetroBorderLight,
                                topLeft = Offset(stroke / 2f, stroke / 2f),
                                size = Size(size.width - stroke, size.height - stroke),
                                cornerRadius = CornerRadius(cornerRadius, cornerRadius),
                                style = Stroke(width = stroke)
                            )
                        }
                ) {
                    Box(
                        modifier = Modifier
                            .padding(all = 20.dp)
                            .fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Filled.EmojiEvents,
                            contentDescription = null,
                            tint = Color(0xFFFFB703),
                            modifier = Modifier
                                .size(25.dp)
                                .align(Alignment.CenterEnd)
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StepBadge(number = "3")
                            Text(
                                text = "RETRO ACHIEVEMENTS",
                                fontFamily = pressStart2PFontFamily,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = RetroText
                            )
                        }

                        Text(
                            text = "Your web API key from retroachievements.org:",
                            fontFamily = condensedFontFamily,
                            fontSize = 12.sp,
                            color = RetroText
                        )

                        // Username Input
                        RetroInputField(
                            value = raUsername,
                            onValueChange = {
                                raUsername = it
                                saveCredentials(it, raPasswordOrToken)
                            },
                            placeholder = "USERNAME"
                        )

                        // Password / API Key Input
                        RetroInputField(
                            value = raPasswordOrToken,
                            onValueChange = {
                                raPasswordOrToken = it
                                saveCredentials(raUsername, it)
                            },
                            placeholder = "API KEY",
                            isPassword = true
                        )

                        // Verify & Authenticate Button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RetroBottomActionButton(
                                label = if (isVerifying) "VERIFYING..." else "SAVE",
                                onClick = {
                                    if (raUsername.isNotBlank() && raPasswordOrToken.isNotBlank()) {
                                        scope.launch {
                                            isVerifying = true
                                            verifyStatus = null
                                            val api = RetroAchievementsApi(OkHttpClient())
                                            val token = api.login(raUsername, raPasswordOrToken)
                                            isVerifying = false
                                            if (token != null) {
                                                prefs.edit()
                                                    .putString("ra_username", raUsername)
                                                    .putString("ra_token", token)
                                                    .putString("ra_password", raPasswordOrToken)
                                                    .apply()
                                                verifyStatus = "LOGIN SUCCESSFUL!"
                                            } else {
                                                saveCredentials(raUsername, raPasswordOrToken)
                                                verifyStatus = "CREDENTIALS SAVED"
                                            }
                                        }
                                    } else {
                                        saveCredentials(raUsername, raPasswordOrToken)
                                        verifyStatus = "SAVED (BLANK)"
                                    }
                                },
                                isLoading = isVerifying,
                                modifier = Modifier.weight(1f),
                                backgroundColor = RetroGold,
                                borderColor = RetroText,
                                edgeColor = Color(0xFFC88A00),
                                textColor = RetroPanel
                            )
                        }

                        if (verifyStatus != null) {
                            Text(
                                text = verifyStatus!!,
                                fontFamily = pressStart2PFontFamily,
                                fontSize = 8.sp,
                                color = RetroLime,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Text(
                            text = "LEAVE BLANK TO SKIP.",
                            fontFamily = pressStart2PFontFamily,
                            fontSize = 7.sp,
                            lineHeight = 12.sp,
                            color = RetroText,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // STEP 3 or STEP 4: Select Folder & Scan Action Box
            val selectStepNumber = if (trophiesEnabled) "4" else "3"
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(RetroPanel)
                    .drawWithContent {
                        drawContent()
                        val stroke = 2.dp.toPx()
                        val cornerRadius = 10.dp.toPx()
                        drawRoundRect(
                            color = RetroBorderLight,
                            topLeft = Offset(stroke / 2f, stroke / 2f),
                            size = Size(size.width - stroke, size.height - stroke),
                            cornerRadius = CornerRadius(cornerRadius, cornerRadius),
                            style = Stroke(width = stroke)
                        )
                    }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StepBadge(number = selectStepNumber)
                        Text(
                            text = "SELECT IT HERE",
                            fontFamily = pressStart2PFontFamily,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    // Main Action Button triggers directory selector and scan
                    RetroBottomActionButton(
                        label = if (indexInProgress) "SCANNING..." else "SELECT FOLDER",
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
        }
    }
}

@Composable
private fun RetroInputField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(38.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF1E1022))
            .drawWithContent {
                drawContent()
                val stroke = 2.dp.toPx()
                val cornerRadius = 6.dp.toPx()
                drawRoundRect(
                    color = RetroLime,
                    topLeft = Offset(stroke / 2f, stroke / 2f),
                    size = Size(size.width - stroke, size.height - stroke),
                    cornerRadius = CornerRadius(cornerRadius, cornerRadius),
                    style = Stroke(width = stroke)
                )
            }
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            cursorBrush = SolidColor(RetroLime),
            textStyle = TextStyle(
                fontFamily = pressStart2PFontFamily,
                fontSize = 9.sp,
                color = RetroLime
            ),
            visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { innerTextField ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            fontFamily = pressStart2PFontFamily,
                            fontSize = 8.sp,
                            color = Color.Gray
                        )
                    }
                    innerTextField()
                }
            }
        )
    }
}

@Composable
private fun StepBadge(number: String) {
    Box(
        modifier = Modifier
            .size(22.dp)
            .background(RetroLime, shape = CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = number,
            fontFamily = pressStart2PFontFamily,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = RetroPanel,
            modifier = Modifier.offset(y = (-2).dp)
        )
    }
}

@Composable
private fun InstructionCheckItem(
    title: String,
    subtitle: String
) {
    val condensedFontFamily = remember {
        FontFamily(Typeface.create("sans-serif-condensed", Typeface.NORMAL))
    }

    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.padding(vertical = 1.dp)
    ) {
        // Non-interactive Retro Checkmark Badge
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(RetroLime),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "✓",
                fontFamily = pressStart2PFontFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = RetroPanel,
                modifier = Modifier.offset(y = (-4).dp)
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(
                text = title,
                fontFamily = pressStart2PFontFamily,
                fontSize = 7.sp,
                lineHeight = 14.sp,
                fontWeight = FontWeight.Bold,
                color = RetroLime
            )
            Text(
                text = subtitle,
                fontFamily = condensedFontFamily,
                fontSize = 12.sp,
                lineHeight = 14.sp,
                color = RetroText
            )
        }
    }
}
