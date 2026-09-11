package com.swordfish.lemuroid.lib.library.metadata

import com.swordfish.lemuroid.lib.storage.StorageFile

interface GameMetadataProvider {
    suspend fun retrieveMetadata(
        storageFile: StorageFile,
        onLog: (String) -> Unit = {}
    ): GameMetadata?
}
