package com.swordfish.lemuroid.app.mobile.feature.gameinfo

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.swordfish.lemuroid.lib.library.db.entity.Game
import com.swordfish.lemuroid.lib.library.metadata.GameMetadata

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScrapeResultScreen(
    game: Game,
    metadata: GameMetadata,
    onDismiss: () -> Unit,
    onAccept: (title: String, releaseDate: String?, publisher: String?, developer: String?, region: String?, coverFrontUrl: String?, coverBackUrl: String?, cartridgeUrl: String?, manualUrl: String?) -> Unit
) {
    var title by remember { mutableStateOf(metadata.name ?: game.title) }
    var releaseDate by remember { mutableStateOf(metadata.releaseDate ?: game.releaseDate ?: "") }
    var publisher by remember { mutableStateOf(metadata.publisher ?: game.publisher ?: "") }
    var developer by remember { mutableStateOf(metadata.developer ?: game.developer ?: "") }
    var region by remember { mutableStateOf(metadata.country ?: game.country ?: "") }
    var selectedCoverFrontUrl by remember { mutableStateOf(metadata.thumbnail ?: game.coverFrontUrl) }
    var selectedCoverBackUrl by remember { mutableStateOf(metadata.thumbnailBack ?: game.coverBackUrl) }
    var selectedCartridgeUrl by remember { mutableStateOf(metadata.cartridgeImage ?: game.cartridgeUrl) }
    var selectedManualUrl by remember { mutableStateOf(metadata.manualUrl ?: game.manualUrl) }

    // Parse metadata options from debugInfo
    val releaseDateOptions = remember { parseMetadataOptions(metadata.debugInfo, "dates") }
    val coverArtOptions = remember { parseMetadataOptions(metadata.debugInfo, "arts") }
    val coverBackOptions = remember { parseMetadataOptions(metadata.debugInfo, "backs") }
    val cartridgeOptions = remember { parseMetadataOptions(metadata.debugInfo, "carts") }
    val manualOptions = remember { parseMetadataOptions(metadata.debugInfo, "manuals") }
    val developerOptions = remember { parseMetadataOptions(metadata.debugInfo, "devs") }
    val publisherOptions = remember { parseMetadataOptions(metadata.debugInfo, "pubs") }
    
    LaunchedEffect(Unit) {
        android.util.Log.d("ScrapeResult", "debugInfo: ${metadata.debugInfo}")
        android.util.Log.d("ScrapeResult", "coverArtOptions parsed: $coverArtOptions")
        android.util.Log.d("ScrapeResult", "coverBackOptions parsed: $coverBackOptions")
        android.util.Log.d("ScrapeResult", "cartridgeOptions parsed: $cartridgeOptions")
        android.util.Log.d("ScrapeResult", "manualOptions parsed: $manualOptions")
        android.util.Log.d("ScrapeResult", "releaseDateOptions parsed: $releaseDateOptions")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scraped Metadata Results") },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Cancel")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            onAccept(
                                title,
                                releaseDate.ifBlank { null },
                                publisher.ifBlank { null },
                                developer.ifBlank { null },
                                region.ifBlank { null },
                                selectedCoverFrontUrl,
                                selectedCoverBackUrl,
                                selectedCartridgeUrl,
                                selectedManualUrl
                            )
                        }
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Accept", fontWeight = FontWeight.Bold)
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
                Text(
                    text = "Review and edit the scraped metadata below. You can pick values or freely edit any field.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // --- FRONT COVER ART ---
            if (metadata.thumbnail != null || game.coverFrontUrl != null || coverArtOptions.isNotEmpty()) {
                item {
                    ImageGalleryCard(
                        title = "Front Cover",
                        existingImage = game.coverFrontUrl,
                        selectedImage = selectedCoverFrontUrl,
                        onImageSelected = { selectedCoverFrontUrl = it },
                        options = coverArtOptions
                    )
                }
            }

            // --- BACK COVER ART ---
            if (metadata.thumbnailBack != null || game.coverBackUrl != null || coverBackOptions.isNotEmpty()) {
                item {
                    ImageGalleryCard(
                        title = "Back Cover",
                        existingImage = game.coverBackUrl,
                        selectedImage = selectedCoverBackUrl,
                        onImageSelected = { selectedCoverBackUrl = it },
                        options = coverBackOptions
                    )
                }
            }

            // --- CARTRIDGE ART ---
            if (metadata.cartridgeImage != null || game.cartridgeUrl != null || cartridgeOptions.isNotEmpty()) {
                item {
                    ImageGalleryCard(
                        title = "Cartridge",
                        existingImage = game.cartridgeUrl,
                        selectedImage = selectedCartridgeUrl,
                        onImageSelected = { selectedCartridgeUrl = it },
                        options = cartridgeOptions
                    )
                }
            }

            // --- MANUAL ---
            if (manualOptions.isNotEmpty() || metadata.manualUrl != null || game.manualUrl != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Manual PDF", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            
                            val existingManual = game.manualUrl != null
                            val scrapedManual = metadata.manualUrl != null || manualOptions.isNotEmpty()
                            
                            if (existingManual || scrapedManual) {
                                val manualUrl = selectedManualUrl ?: metadata.manualUrl ?: game.manualUrl
                                android.util.Log.d("ScrapeResult", "Counting PDF pages for: $manualUrl")
                                val pageCount = try {
                                    countPdfPages(manualUrl)
                                } catch (e: Exception) {
                                    android.util.Log.e("ScrapeResult", "Error counting pages: ${e.message}")
                                    0
                                }
                                android.util.Log.d("ScrapeResult", "Page count: $pageCount")
                                
                                if (pageCount > 0) {
                                    Text(
                                        "✓ $pageCount pages",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                } else {
                                    Text(
                                        "✓ Detected",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            } else {
                                Text(
                                    "No manual found",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // --- TITLE ---
            item {
                ScrapeFieldEditor(
                    label = "Title",
                    existingValue = game.title,
                    scrapedValue = metadata.name,
                    currentValue = title,
                    onValueChange = { title = it }
                )
            }

            // --- RELEASE DATE ---
            item {
                ScrapeFieldEditor(
                    label = "Release Date",
                    existingValue = game.releaseDate,
                    scrapedValue = metadata.releaseDate,
                    scrapedOptions = releaseDateOptions,
                    currentValue = releaseDate,
                    onValueChange = { releaseDate = it }
                )
            }

            // --- PUBLISHER ---
            item {
                ScrapeFieldEditor(
                    label = "Publisher",
                    existingValue = game.publisher,
                    scrapedValue = metadata.publisher,
                    currentValue = publisher,
                    onValueChange = { publisher = it }
                )
            }

            // --- DEVELOPER ---
            item {
                ScrapeFieldEditor(
                    label = "Developer",
                    existingValue = game.developer,
                    scrapedValue = metadata.developer,
                    currentValue = developer,
                    onValueChange = { developer = it }
                )
            }

            // --- REGION / COUNTRY ---
            item {
                ScrapeFieldEditor(
                    label = "Region",
                    existingValue = game.country,
                    scrapedValue = metadata.country,
                    currentValue = region,
                    onValueChange = { region = it }
                )
            }

            // --- BOTTOM ACTIONS ---
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(50.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            onAccept(
                                title,
                                releaseDate.ifBlank { null },
                                publisher.ifBlank { null },
                                developer.ifBlank { null },
                                region.ifBlank { null },
                                selectedCoverFrontUrl,
                                selectedCoverBackUrl,
                                selectedCartridgeUrl,
                                selectedManualUrl
                            )
                        },
                        modifier = Modifier.weight(1f).height(50.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Accept")
                    }
                }
            }
        }
    }
}

@Composable
private fun ImageGalleryCard(
    title: String,
    existingImage: String?,
    selectedImage: String?,
    onImageSelected: (String?) -> Unit,
    options: List<String>
) {
    val validImages = options.filter { it.isNotBlank() && isValidImageUri(it) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                existingImage?.let { img ->
                    if (isValidImageUri(img)) {
                        Box(
                            modifier = Modifier
                                .width(140.dp)
                                .aspectRatio(1f)
                                .clip(MaterialTheme.shapes.medium)
                                .background(
                                    if (selectedImage == img) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surface
                                )
                                .clickable { onImageSelected(img) }
                                .padding(4.dp)
                        ) {
                            AsyncImage(
                                model = img,
                                contentDescription = "Existing $title",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                            Text(
                                "Existing",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.6f))
                                    .padding(horizontal = 4.dp),
                                color = androidx.compose.ui.graphics.Color.White
                            )
                        }
                    }
                }

                validImages.forEach { scrapedImage ->
                    Box(
                        modifier = Modifier
                            .width(140.dp)
                            .aspectRatio(1f)
                            .clip(MaterialTheme.shapes.medium)
                            .background(
                                if (selectedImage == scrapedImage) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surface
                            )
                            .clickable { onImageSelected(scrapedImage) }
                            .padding(4.dp)
                    ) {
                        AsyncImage(
                            model = scrapedImage,
                            contentDescription = "Scraped $title",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                        Text(
                            "Scraped",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.6f))
                                .padding(horizontal = 4.dp),
                            color = androidx.compose.ui.graphics.Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ScrapeFieldEditor(
    label: String,
    existingValue: String?,
    scrapedValue: String?,
    currentValue: String,
    onValueChange: (String) -> Unit,
    scrapedOptions: List<String> = emptyList()
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(label, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

        val existing = existingValue?.takeIf { it.isNotBlank() }
        val scraped = scrapedValue?.takeIf { it.isNotBlank() }
        val allScrapedOptions = (listOfNotNull(scraped) + scrapedOptions).distinct()

        if (existing != null || allScrapedOptions.isNotEmpty()) {
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (existing != null) {
                    FilterChip(
                        selected = currentValue == existing,
                        onClick = { onValueChange(existing) },
                        label = { Text("Existing: $existing") }
                    )
                }
                allScrapedOptions.forEach { option ->
                    FilterChip(
                        selected = currentValue == option,
                        onClick = { onValueChange(option) },
                        label = { Text("Scraped: $option") }
                    )
                }
            }
        }

        OutlinedTextField(
            value = currentValue,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
    }
}

private fun parseMetadataOptions(debugInfo: String?, key: String): List<String> {
    if (debugInfo == null || !debugInfo.startsWith("OPTIONS|")) return emptyList()
    // Match "key:" followed by anything that's not a single pipe (but allows ||)
    val pattern = Regex("$key:([^|]*(?:\\|\\|[^|]*)*)")
    val match = pattern.find(debugInfo) ?: return emptyList()
    val values = match.groupValues[1].split("||").filter { it.isNotEmpty() }
    return values
}

private fun isValidImageUri(uri: String?): Boolean {
    if (uri == null || uri.isBlank()) return false
    // Check for valid URI scheme - must be one of these
    val validSchemes = listOf("http://", "https://", "file://", "content://")
    if (!validSchemes.any { uri.startsWith(it) }) return false
    // Valid image URIs should not contain multiple URLs separated by comma
    // (commas in the URL itself are OK, but not as separators)
    return !uri.contains(",http") && !uri.contains(",content") && !uri.contains(",file")
}

private fun String?.isNull_or_Empty(): Boolean = this.isNullOrEmpty()

private fun countPdfPages(uri: String?): Int {
    android.util.Log.d("ScrapeResult-MANUAL-COUNT", "countPdfPages START: uri=$uri")
    if (uri == null || uri.isBlank()) {
        android.util.Log.d("ScrapeResult-MANUAL-COUNT", "countPdfPages: uri is null or blank, returning 0")
        return 0
    }
    
    return try {
        when {
            uri.startsWith("file://") -> {
                android.util.Log.d("ScrapeResult-MANUAL-COUNT", "countPdfPages: Processing file:// URI")
                val filePath = uri.removePrefix("file://")
                android.util.Log.d("ScrapeResult-MANUAL-COUNT", "countPdfPages: filePath=$filePath")
                val file = java.io.File(filePath)
                android.util.Log.d("ScrapeResult-MANUAL-COUNT", "countPdfPages: file.exists=${file.exists()}, file.isFile=${file.isFile()}")
                if (!file.exists()) {
                    android.util.Log.d("ScrapeResult-MANUAL-COUNT", "countPdfPages: File does not exist")
                    return 0
                }
                val count = countPdfPagesFromFile(file)
                android.util.Log.d("ScrapeResult-MANUAL-COUNT", "countPdfPages: Counted $count pages from file:// URI")
                count
            }
            uri.startsWith("content://") -> {
                // For content URIs, we can't easily count pages without context
                // Return 0 to show "Detected" instead
                android.util.Log.d("ScrapeResult-MANUAL-COUNT", "countPdfPages: content:// URI detected, returning 0 (will show 'Detected')")
                0
            }
            uri.startsWith("/") -> {
                // Direct file path
                android.util.Log.d("ScrapeResult-MANUAL-COUNT", "countPdfPages: Processing direct file path")
                val file = java.io.File(uri)
                android.util.Log.d("ScrapeResult-MANUAL-COUNT", "countPdfPages: file.exists=${file.exists()}, file.isFile=${file.isFile()}")
                if (!file.exists()) {
                    android.util.Log.d("ScrapeResult-MANUAL-COUNT", "countPdfPages: File does not exist")
                    return 0
                }
                val count = countPdfPagesFromFile(file)
                android.util.Log.d("ScrapeResult-MANUAL-COUNT", "countPdfPages: Counted $count pages from direct path")
                count
            }
            else -> {
                android.util.Log.d("ScrapeResult-MANUAL-COUNT", "countPdfPages: Unknown URI scheme, returning 0")
                0
            }
        }
    } catch (e: Exception) {
        android.util.Log.e("ScrapeResult-MANUAL-COUNT", "Error counting PDF pages: ${e.message}", e)
        0
    }
}

private fun countPdfPagesFromFile(file: java.io.File): Int {
    android.util.Log.d("ScrapeResult-MANUAL-COUNT", "countPdfPagesFromFile: Attempting to read ${file.absolutePath}")
    return try {
        val fileDescriptor = android.os.ParcelFileDescriptor.open(file, android.os.ParcelFileDescriptor.MODE_READ_ONLY)
        android.util.Log.d("ScrapeResult-MANUAL-COUNT", "countPdfPagesFromFile: ParcelFileDescriptor opened successfully")
        val pdfRenderer = android.graphics.pdf.PdfRenderer(fileDescriptor)
        val pageCount = pdfRenderer.pageCount
        android.util.Log.d("ScrapeResult-MANUAL-COUNT", "countPdfPagesFromFile: PDF has $pageCount pages")
        pdfRenderer.close()
        fileDescriptor.close()
        pageCount
    } catch (e: Exception) {
        android.util.Log.e("ScrapeResult-MANUAL-COUNT", "Error reading PDF file: ${e.message}", e)
        0
    }
}
