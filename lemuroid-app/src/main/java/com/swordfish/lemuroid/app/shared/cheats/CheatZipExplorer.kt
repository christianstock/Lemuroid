package com.swordfish.lemuroid.app.shared.cheats

import android.content.Context
import android.net.Uri
import java.util.zip.ZipInputStream

data class CheatFile(
    val zipUri: String,
    val entryName: String,
    val displayName: String
)

object CheatZipExplorer {
    fun listCheatFiles(context: Context, zipUris: Set<String>): List<CheatFile> {
        val cheatFiles = mutableListOf<CheatFile>()
        zipUris.forEach { uriString ->
            runCatching {
                val uri = Uri.parse(uriString)
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    ZipInputStream(inputStream).use { zipInputStream ->
                        var entry = zipInputStream.nextEntry
                        while (entry != null) {
                            if (entry.name.endsWith(".cht", ignoreCase = true)) {
                                cheatFiles.add(
                                    CheatFile(
                                        zipUri = uriString,
                                        entryName = entry.name,
                                        displayName = entry.name.substringAfterLast("/").substringBeforeLast(".")
                                    )
                                )
                            }
                            entry = zipInputStream.nextEntry
                        }
                    }
                }
            }
        }
        return cheatFiles.sortedBy { it.displayName }
    }
}
