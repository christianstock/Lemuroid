package com.swordfish.lemuroid.app.mobile.feature.gameinfo

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.LemuroidGameImage
import com.swordfish.lemuroid.common.kotlin.cleanGameTitle
import com.swordfish.lemuroid.lib.library.db.entity.Game
import java.text.DateFormat
import java.util.Calendar
import java.util.Locale

val PressStart2PFont = FontFamily(
    Font(R.font.press_start_2p)
)

@Composable
fun GameInfoScreen(
    viewModel: GameInfoViewModel,
    onPlay: (Game) -> Unit,
    onRestart: (Game) -> Unit,
    onCheats: (Game) -> Unit = {},
    onManual: (Game) -> Unit = {},
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {}
) {
    val game = viewModel.game.collectAsState().value
    val isRescanning = viewModel.isRescanning.collectAsState().value
    val pendingMetadata = viewModel.pendingMetadata.collectAsState().value
    var showEditScreen by remember { mutableStateOf(false) }

    if (game == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFFFF6D00))
        }
        return
    }

    if (showEditScreen) {
        EditDetailsScreen(
            game = game,
            onDismiss = { showEditScreen = false },
            onSave = { title, date, pub, dev, reg, ver ->
                viewModel.updateGameDetails(title, date, pub, dev, reg, ver)
                showEditScreen = false
            },
            onThumbnailSelected = { uri, deleteSource ->
                viewModel.saveLocalThumbnail(uri, deleteSource)
            }
        )
    } else if (pendingMetadata != null) {
        ScrapeResultScreen(
            game = game,
            metadata = pendingMetadata,
            onDismiss = { viewModel.clearPendingMetadata() },
            onAccept = { title, releaseDate, publisher, developer, region, coverFrontUrl, coverBackUrl, cartridgeUrl, manualUrl ->
                viewModel.applyCustomScrapedMetadata(
                    title = title,
                    releaseDate = releaseDate,
                    publisher = publisher,
                    developer = developer,
                    region = region,
                    coverFrontUrl = coverFrontUrl,
                    coverBackUrl = coverBackUrl,
                    cartridgeUrl = cartridgeUrl,
                    manualUrl = manualUrl
                )
            }
        )
    } else {
        Box(modifier = modifier.fillMaxSize()) {
            // --- FULLSCREEN BLURRED BACKGROUND WITH VIGNETTE GRADIENT ---
            Crossfade(
                targetState = game.coverFrontUrl,
                animationSpec = tween(durationMillis = 600),
                label = "BackgroundBlurCrossfade",
                modifier = Modifier.fillMaxSize()
            ) { coverUrl ->
                if (!coverUrl.isNullOrEmpty()) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(coverUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .blur(radius = 1000.dp)
                                .graphicsLayer {
                                    scaleX = 1.2f
                                    scaleY = 1.2f
                                }
                        )
                        // Radial Vignette Gradient: Semi-transparent in center, deep black towards edges
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .drawWithContent {
                                    drawContent()
                                    drawRect(Color.Black.copy(alpha = 0.25f))
                                    drawRect(
                                        brush = Brush.radialGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                Color.Black.copy(alpha = 0.5f),
                                                Color.Black.copy(alpha = 0.95f)
                                            ),
                                            center = center,
                                            radius = size.maxDimension * 0.7f
                                        )
                                    )
                                }
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .drawWithContent {
                                drawRect(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            Color(0xFF1E1E24),
                                            Color.Black
                                        ),
                                        center = center,
                                        radius = size.maxDimension * 0.75f
                                    )
                                )
                            }
                    )
                }
            }

            // --- MAIN CONTENT ---
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Title Block
                item {
                    val title = game.title.cleanGameTitle()
                    val titleParts = title.split(" - ", limit = 2).map { it.trim() }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(top = 12.dp)
                    ) {
                        Text(
                            text = titleParts[0].uppercase(),
                            fontFamily = PressStart2PFont,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            lineHeight = 24.sp
                        )
                        if (titleParts.size > 1) {
                            Text(
                                text = titleParts[1].uppercase(),
                                fontFamily = PressStart2PFont,
                                fontSize = 11.sp,
                                color = Color(0xFFFF6D00),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                        }
                    }
                }

                // 2. Developer / Publisher / Date Metadata
                item {
                    val formattedDate = formatLocalizedDate(game.releaseDate)
                    val publisher = game.publisher ?: ""
                    val developer = game.developer ?: ""

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        if (developer.isNotEmpty()) {
                            Text(
                                text = "Developer: $developer",
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 13.sp,
                                color = Color(0xFFFF6D00),
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center
                            )
                        }
                        if (publisher.isNotEmpty() && publisher != developer) {
                            Text(
                                text = "Publisher: $publisher",
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 13.sp,
                                color = Color(0xFFFF6D00),
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center
                            )
                        }
                        if (formattedDate.isNotEmpty()) {
                            Text(
                                text = "Release Date: $formattedDate",
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 13.sp,
                                color = Color(0xFFFF6D00),
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // 3. Retro Box Art Frame
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.72f)
                            .padding(vertical = 8.dp)
                            .shadow(16.dp, shape = RectangleShape)
                            .background(Color.White)
                            .padding(6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        BoxArtSwiper(
                            frontImageUrl = game.coverFrontUrl,
                            backImageUrl = game.coverBackUrl,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // 4. Region & Version Details
                item {
                    val region = game.country ?: ""
                    val version = game.summary ?: ""
                    val regionText = listOfNotNull(
                        region.takeIf { it.isNotEmpty() },
                        version.takeIf { it.isNotEmpty() }
                    ).joinToString(", ").uppercase()

                    if (regionText.isNotEmpty()) {
                        Text(
                            text = regionText,
                            fontFamily = PressStart2PFont,
                            fontSize = 9.sp,
                            color = Color.White.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            letterSpacing = 1.sp
                        )
                    }
                }

                // 5. RESUME & RESTART
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        DottedDivider()

                        // RESUME
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = { onPlay(game) }
                                )
                                .padding(vertical = 14.dp)
                        ) {
                            Text(
                                text = "▶",
                                fontSize = 18.sp,
                                color = Color(0xFFFF6D00),
                                modifier = Modifier.padding(end = 12.dp)
                            )
                            Text(
                                text = "RESUME",
                                fontFamily = PressStart2PFont,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF6D00)
                            )
                        }

                        DottedDivider()

                        // RESTART
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = { onRestart(game) }
                                )
                                .padding(vertical = 12.dp)
                        ) {
                            Text(
                                text = "↺",
                                fontSize = 15.sp,
                                color = Color(0xFFFF80AB),
                                modifier = Modifier.padding(end = 10.dp)
                            )
                            Text(
                                text = "RESTART",
                                fontFamily = PressStart2PFont,
                                fontSize = 12.sp,
                                color = Color.White
                            )
                        }

                        DottedDivider()
                    }
                }

                // 6. Action Row
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RetroActionTextButton(
                            icon = Icons.Default.Star,
                            iconColor = Color(0xFFFFD600),
                            label = "CHEATS",
                            onClick = { onCheats(game) }
                        )

                        if (game.manualUrl != null) {
                            RetroActionTextButton(
                                icon = Icons.Default.Description,
                                iconColor = Color(0xFF29B6F6),
                                label = "MANUAL",
                                onClick = { onManual(game) }
                            )
                        }

                        RetroActionTextButton(
                            icon = Icons.Default.Edit,
                            iconColor = Color(0xFFFF8A80),
                            label = "EDIT",
                            onClick = { showEditScreen = true }
                        )

                        RetroActionTextButton(
                            icon = Icons.Default.Refresh,
                            iconColor = Color(0xFF69F0AE),
                            label = "RESCAN",
                            isLoading = isRescanning,
                            onClick = { viewModel.rescan() }
                        )
                    }
                }

                // 7. Bottom Back Button
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "< BACK",
                        fontFamily = PressStart2PFont,
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onBack
                            )
                            .padding(16.dp)
                    )
                }
            }
        }
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
                modifier = Modifier.size(32.dp)
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

@Composable
private fun DottedDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .drawWithContent {
                drawPath(
                    path = androidx.compose.ui.graphics.Path().apply {
                        moveTo(0f, 0f)
                        lineTo(size.width, 0f)
                    },
                    color = Color.White.copy(alpha = 0.25f),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
                    )
                )
            }
    )
}

private fun formatLocalizedDate(dateString: String?): String {
    if (dateString.isNullOrBlank()) return ""
    return runCatching {
        val cleanDate = dateString.replace("-", "").trim()
        if (cleanDate.length >= 8) {
            val year = cleanDate.substring(0, 4).toInt()
            val month = cleanDate.substring(4, 6).toInt() - 1
            val day = cleanDate.substring(6, 8).toInt()
            val cal = Calendar.getInstance().apply {
                set(year, month, day)
            }
            DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault()).format(cal.time)
        } else {
            dateString
        }
    }.getOrDefault(dateString)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditDetailsScreen(
    game: Game,
    onDismiss: () -> Unit,
    onSave: (String, String?, String?, String?, String?, String?) -> Unit,
    onThumbnailSelected: (Uri, Boolean) -> Unit
) {
    var title by remember { mutableStateOf(game.title) }
    var date by remember { mutableStateOf(game.releaseDate ?: "") }
    var publisher by remember { mutableStateOf(game.publisher ?: "") }
    var developer by remember { mutableStateOf(game.developer ?: "") }
    var region by remember { mutableStateOf(game.country ?: "") }
    var version by remember { mutableStateOf(game.summary ?: "") }
    var deleteSource by remember { mutableStateOf(false) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onThumbnailSelected(it, deleteSource) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Game Details") },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = { onSave(title, date, publisher, developer, region, version) }) {
                        Text("Save", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { imagePicker.launch("image/*") }
                        .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.medium)
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(80.dp)) {
                        LemuroidGameImage(game = game, applyAspectRatio = true)
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = Color.White)
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Change Box Art", style = MaterialTheme.typography.titleMedium)
                        Text("Tap to select image", style = MaterialTheme.typography.bodySmall)

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = deleteSource, onCheckedChange = { deleteSource = it })
                            Text("Delete original after copy", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = region,
                        onValueChange = { region = it },
                        label = { Text("Region") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = version,
                        onValueChange = { version = it },
                        label = { Text("Version") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Release Year/Date") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                OutlinedTextField(
                    value = publisher,
                    onValueChange = { publisher = it },
                    label = { Text("Publisher") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                OutlinedTextField(
                    value = developer,
                    onValueChange = { developer = it },
                    label = { Text("Developer") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = { onSave(title, date, publisher, developer, region, version) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Save Changes")
                }
            }
        }
    }
}
