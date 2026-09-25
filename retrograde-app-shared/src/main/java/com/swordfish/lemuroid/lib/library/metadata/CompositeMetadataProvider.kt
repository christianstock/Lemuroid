package com.swordfish.lemuroid.lib.library.metadata

import com.swordfish.lemuroid.lib.storage.StorageFile

class CompositeMetadataProvider(
    private val skraperMetadataProvider: SkraperMetadataProvider,
    private val fallbackMetadataProvider: GameMetadataProvider
) : GameMetadataProvider {

    override suspend fun retrieveMetadata(
        storageFile: StorageFile,
        onLog: (String) -> Unit
    ): GameMetadata? {
        val skraperMetadata = runCatching {
            skraperMetadataProvider.retrieveMetadata(storageFile, onLog)
        }.getOrNull()

        val libretroMetadata = runCatching {
            fallbackMetadataProvider.retrieveMetadata(storageFile, onLog)
        }.getOrNull()

        if (skraperMetadata == null) {
            return libretroMetadata
        }

        if (libretroMetadata == null) {
            return skraperMetadata
        }

        // Amend/combine both: Skraper metadata takes precedence for non-null fields,
        // while LibretroDB fills in any missing/null fields (e.g., thumbnail art, system info, etc.).
        return skraperMetadata.copy(
            name = skraperMetadata.name ?: libretroMetadata.name,
            system = skraperMetadata.system ?: libretroMetadata.system,
            romName = skraperMetadata.romName ?: libretroMetadata.romName,
            developer = skraperMetadata.developer ?: libretroMetadata.developer,
            publisher = skraperMetadata.publisher ?: libretroMetadata.publisher,
            thumbnail = skraperMetadata.thumbnail ?: libretroMetadata.thumbnail,
            thumbnailBack = skraperMetadata.thumbnailBack ?: libretroMetadata.thumbnailBack,
            releaseDate = skraperMetadata.releaseDate ?: libretroMetadata.releaseDate,
            summary = skraperMetadata.summary ?: libretroMetadata.summary,
            country = skraperMetadata.country ?: libretroMetadata.country,
            debugInfo = "Skraper + LibretroDB"
        )
    }
}
