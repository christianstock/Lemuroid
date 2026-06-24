package com.swordfish.lemuroid.app.shared.cheats

import android.content.Context
import com.swordfish.lemuroid.common.kotlin.writeToFile
import com.swordfish.lemuroid.lib.storage.DirectoriesManager
import okhttp3.OkHttpClient
import okhttp3.Request
import timber.log.Timber
import java.io.File
import java.util.zip.ZipInputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CheatDownloader(
    private val context: Context,
    private val directoriesManager: DirectoriesManager,
    private val okHttpClient: OkHttpClient
) {
    companion object {
        private const val CHEATS_URL = "https://buildbot.libretro.com/assets/frontend/cheats.zip"
    }

    suspend fun downloadAndExtractCheats(): Boolean = withContext(Dispatchers.IO) {
        val cheatsDir = directoriesManager.getCheatsDirectory()
        val tempZip = File(context.cacheDir, "cheats.zip")

        try {
            Timber.i("Downloading cheats from $CHEATS_URL")
            val request = Request.Builder().url(CHEATS_URL).build()
            val response = okHttpClient.newCall(request).execute()

            if (!response.isSuccessful) {
                Timber.e("Failed to download cheats: ${response.code}")
                return@withContext false
            }

            response.body?.byteStream()?.use { inputStream ->
                ZipInputStream(inputStream).use { zipInputStream ->
                    var entry = zipInputStream.nextEntry
                    while (entry != null) {
                        var entryName = entry.name
                        // Strip leading "cheats/" if present in the ZIP
                        if (entryName.startsWith("cheats/")) {
                            entryName = entryName.substring(7)
                        }
                        
                        if (entryName.isNotEmpty()) {
                            val outFile = File(cheatsDir, entryName)
                            if (entry.isDirectory) {
                                outFile.mkdirs()
                            } else {
                                outFile.parentFile?.mkdirs()
                                outFile.outputStream().use { outputStream ->
                                    zipInputStream.copyTo(outputStream)
                                }
                            }
                        }
                        zipInputStream.closeEntry()
                        entry = zipInputStream.nextEntry
                    }
                }
            }
            Timber.i("Cheats downloaded and extracted to ${cheatsDir.absolutePath}")
            true
        } catch (e: Exception) {
            Timber.e(e, "Error downloading cheats")
            false
        } finally {
            if (tempZip.exists()) tempZip.delete()
        }
    }
}
