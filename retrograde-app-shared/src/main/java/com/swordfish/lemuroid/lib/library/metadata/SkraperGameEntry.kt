package com.swordfish.lemuroid.lib.library.metadata

data class SkraperGameEntry(
    val title: String,
    val romFileName: String?,
    val developer: String? = null,
    val publisher: String? = null,
    val description: String? = null,
    val releaseDate: String? = null,
    val coverFrontPath: String? = null,
    val coverBackPath: String? = null,
    val cartridgeImagePath: String? = null,
    val manualPath: String? = null,
    val genre: String? = null
)
