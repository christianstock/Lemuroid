package com.swordfish.lemuroid.app.shared.manuals

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

data class ManualPageInfo(
    val pageIndex: Int,
    val filePath: String,
    val width: Int,
    val height: Int
)

class PdfManualProcessor(private val context: Context) {

    /**
     * Checks if compressed manual pages already exist on disk for the given game.
     */
    fun getExistingPages(gameId: String): List<ManualPageInfo> {
        val outputDir = File(context.filesDir, "manuals/$gameId")
        
        if (!outputDir.exists() || !outputDir.isDirectory) {
            return emptyList()
        }

        val files = outputDir.listFiles()?.filter { it.extension == "webp" }?.sortedBy { it.name } ?: emptyList()
        
        if (files.isEmpty()) {
            return emptyList()
        }

        val pages = files.mapIndexed { index, file ->
            ManualPageInfo(
                pageIndex = index,
                filePath = file.absolutePath,
                width = 800,
                height = 1200
            )
        }
        return pages
    }

    /**
     * Converts a PDF file (via Uri or File) into compressed WebP images stored on internal disk.
     * Splitting double-page spreads if aspect ratio exceeds [aspectRatioThreshold].
     */
    suspend fun processPdfUri(
        pdfUriString: String,
        gameId: String,
        targetHeight: Int = 800,
        aspectRatioThreshold: Float = 1.5f,
        quality: Int = 60
    ): List<ManualPageInfo> = withContext(Dispatchers.IO) {
        
        val existing = getExistingPages(gameId)
        if (existing.isNotEmpty()) {
            return@withContext existing
        }

        val fileDescriptor: ParcelFileDescriptor? = try {
            if (pdfUriString.startsWith("content://") || pdfUriString.startsWith("file://")) {
                context.contentResolver.openFileDescriptor(Uri.parse(pdfUriString), "r")
            } else {
                ParcelFileDescriptor.open(File(pdfUriString), ParcelFileDescriptor.MODE_READ_ONLY)
            }
        } catch (e: Exception) {
            Log.e("PdfManualProcessor", "Error opening PDF: ${e.message}")
            null
        }
        
        if (fileDescriptor == null) {
            Log.e("PdfManualProcessor", "Failed to open PDF file descriptor")
            return@withContext emptyList()
        }

        val result = renderPdfFileDescriptor(
            fileDescriptor = fileDescriptor,
            gameId = gameId,
            targetHeight = targetHeight,
            aspectRatioThreshold = aspectRatioThreshold,
            quality = quality
        )
        return@withContext result
    }

    suspend fun processPdf(
        pdfFile: File,
        gameId: String,
        targetHeight: Int = 1200,
        aspectRatioThreshold: Float = 1.5f,
        quality: Int = 80
    ): List<ManualPageInfo> = withContext(Dispatchers.IO) {
        
        val existing = getExistingPages(gameId)
        if (existing.isNotEmpty()) {
            return@withContext existing
        }

        if (!pdfFile.exists()) {
            Log.e("PdfManualProcessor", "PDF file not found: ${pdfFile.absolutePath}")
            return@withContext emptyList()
        }

        val fileDescriptor = try {
            ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
        } catch (e: Exception) {
            Log.e("PdfManualProcessor", "Error opening PDF: ${e.message}")
            null
        } ?: return@withContext emptyList()

        val result = renderPdfFileDescriptor(
            fileDescriptor = fileDescriptor,
            gameId = gameId,
            targetHeight = targetHeight,
            aspectRatioThreshold = aspectRatioThreshold,
            quality = quality
        )
        return@withContext result
    }

    private fun renderPdfFileDescriptor(
        fileDescriptor: ParcelFileDescriptor,
        gameId: String,
        targetHeight: Int,
        aspectRatioThreshold: Float,
        quality: Int
    ): List<ManualPageInfo> {
        val outputDir = File(context.filesDir, "manuals/$gameId").apply {
            if (!exists()) mkdirs()
        }

        val pdfRenderer = try {
            PdfRenderer(fileDescriptor)
        } catch (e: Exception) {
            Log.e("PdfManualProcessor", "Error creating PdfRenderer: ${e.message}")
            fileDescriptor.close()
            return emptyList()
        }

        val pagesInfo = mutableListOf<ManualPageInfo>()
        var pageIndexCounter = 0

        try {
            for (i in 0 until pdfRenderer.pageCount) {
                val page = pdfRenderer.openPage(i)

                val originalWidth = page.width
                val originalHeight = page.height
                val aspectRatio = originalWidth.toFloat() / originalHeight.toFloat()

                if (aspectRatio > aspectRatioThreshold) {
                    // --- DOUBLE-PAGE SPREAD DETECTED: SPLIT IN HALF ---
                    val singlePageTargetWidth = ((targetHeight * (aspectRatio / 2f))).toInt()
                    val renderWidth = singlePageTargetWidth * 2

                    val fullBitmap = Bitmap.createBitmap(
                        renderWidth,
                        targetHeight,
                        Bitmap.Config.ARGB_8888
                    )

                    page.render(fullBitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

                    // Left Page (Page N)
                    val leftBitmap = cropBitmap(fullBitmap, 0, 0, singlePageTargetWidth, targetHeight)
                    val leftFile = File(outputDir, "page_${pageIndexCounter.toString().padStart(3, '0')}.webp")
                    saveCompressedBitmap(leftBitmap, leftFile, quality)
                    pagesInfo.add(ManualPageInfo(pageIndexCounter++, leftFile.absolutePath, singlePageTargetWidth, targetHeight))
                    leftBitmap.recycle()

                    // Right Page (Page N+1)
                    val rightBitmap = cropBitmap(fullBitmap, singlePageTargetWidth, 0, singlePageTargetWidth, targetHeight)
                    val rightFile = File(outputDir, "page_${pageIndexCounter.toString().padStart(3, '0')}.webp")
                    saveCompressedBitmap(rightBitmap, rightFile, quality)
                    pagesInfo.add(ManualPageInfo(pageIndexCounter++, rightFile.absolutePath, singlePageTargetWidth, targetHeight))
                    rightBitmap.recycle()

                    fullBitmap.recycle()
                } else {
                    // --- SINGLE PAGE ---
                    val targetWidth = (targetHeight * aspectRatio).toInt()

                    val bitmap = Bitmap.createBitmap(
                        targetWidth,
                        targetHeight,
                        Bitmap.Config.ARGB_8888
                    )

                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

                    val pageFile = File(outputDir, "page_${pageIndexCounter.toString().padStart(3, '0')}.webp")
                    saveCompressedBitmap(bitmap, pageFile, quality)
                    pagesInfo.add(ManualPageInfo(pageIndexCounter++, pageFile.absolutePath, targetWidth, targetHeight))
                    bitmap.recycle()
                }

                page.close()
            }
        } finally {
            pdfRenderer.close()
            fileDescriptor.close()
        }

        return pagesInfo
    }

    private fun cropBitmap(source: Bitmap, x: Int, y: Int, width: Int, height: Int): Bitmap {
        val cropped = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(cropped)
        val srcRect = Rect(x, y, x + width, y + height)
        val destRect = Rect(0, 0, width, height)
        canvas.drawBitmap(source, srcRect, destRect, null)
        return cropped
    }

    private fun saveCompressedBitmap(bitmap: Bitmap, outputFile: File, quality: Int) {
        FileOutputStream(outputFile).use { out ->
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                bitmap.compress(Bitmap.CompressFormat.WEBP_LOSSY, quality, out)
            } else {
                @Suppress("DEPRECATION")
                bitmap.compress(Bitmap.CompressFormat.WEBP, quality, out)
            }
        }
    }

    private fun Int.padStart(length: Int): String {
        return this.toString().padStart(length, '0')
    }
}
