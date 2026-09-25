package com.swordfish.lemuroid.lib.library.metadata

data class GameMetadata(
    val name: String?,
    val system: String?,
    val romName: String?,
    val developer: String?,
    val publisher: String?,
    val thumbnail: String?,
    val thumbnailBack: String?,
    val cartridgeImage: String?,
    val releaseDate: String?,
    val summary: String?,
    val country: String?,
    val debugInfo: String? = null,
)
