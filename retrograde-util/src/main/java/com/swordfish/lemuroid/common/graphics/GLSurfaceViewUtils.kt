package com.swordfish.lemuroid.common.graphics

import android.graphics.Bitmap
import android.graphics.Rect
import android.opengl.GLSurfaceView
import android.view.PixelCopy
import com.swordfish.lemuroid.common.kotlin.runCatchingWithRetry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.math.roundToInt

suspend fun GLSurfaceView.takeScreenshot(
    maxResolution: Int,
    retries: Int = 1,
    cropRect: Rect? = null
): Bitmap? =
    withContext(Dispatchers.Main) {
        runCatchingWithRetry(retries) {
            takeScreenshotInternal(maxResolution, cropRect)
        }.getOrNull()
    }

private suspend fun GLSurfaceView.takeScreenshotInternal(
    maxResolution: Int,
    cropRect: Rect? = null
): Bitmap? =
    suspendCoroutine { cont ->
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.N) {
            Timber.w("PixelCopy not supported on this Android version")
            cont.resume(null)
            return@suspendCoroutine
        }

        queueEvent {
            try {
                val currentWidth = width
                val currentHeight = height
                
                if (currentWidth <= 0 || currentHeight <= 0) {
                    Timber.w("TakeScreenshot: Invalid dimensions, aborting")
                    cont.resume(null)
                    return@queueEvent
                }

                // If no cropRect provided, capture the whole surface
                val sourceRect = cropRect ?: Rect(0, 0, currentWidth, currentHeight)
                
                Timber.d("TakeScreenshot: Capturing area ${sourceRect.left},${sourceRect.top} to ${sourceRect.right},${sourceRect.bottom} from ${currentWidth}x${currentHeight} surface")

                // Create a bitmap matching the SOURCE area size
                val inputBitmap = Bitmap.createBitmap(
                    sourceRect.width(),
                    sourceRect.height(),
                    Bitmap.Config.ARGB_8888,
                )

                val onCompleted = { result: Int ->
                    if (result == PixelCopy.SUCCESS) {
                        Timber.d("TakeScreenshot: PixelCopy success")
                        
                        val outputScaling = maxResolution / maxOf(sourceRect.width(), sourceRect.height()).toFloat()
                        val targetWidth = (sourceRect.width() * outputScaling).roundToInt()
                        val targetHeight = (sourceRect.height() * outputScaling).roundToInt()
                        
                        // Rescale to target resolution
                        val outputBitmap = Bitmap.createScaledBitmap(
                            inputBitmap,
                            targetWidth,
                            targetHeight,
                            true,
                        )
                        cont.resume(outputBitmap)
                    } else {
                        Timber.e("TakeScreenshot: PixelCopy failed with error code: $result")
                        cont.resumeWithException(RuntimeException("Cannot take screenshot. Error code: $result"))
                    }
                }
                
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    PixelCopy.request(this, sourceRect, inputBitmap, onCompleted, handler)
                } else {
                    // Fallback for N (API 24/25) which doesn't support source rect in PixelCopy
                    PixelCopy.request(this, inputBitmap, onCompleted, handler)
                }
            } catch (e: Exception) {
                Timber.e(e, "TakeScreenshot: Exception during capture")
                cont.resumeWithException(e)
            }
        }
    }
