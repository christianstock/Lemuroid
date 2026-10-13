package com.swordfish.lemuroid.lib.library.metadata

data class GameMetadataOptions(
    val names: List<String> = emptyList(),
    val releaseDates: List<String> = emptyList(),
    val publishers: List<String> = emptyList(),
    val developers: List<String> = emptyList(),
    val countries: List<String> = emptyList(),
    val thumbnails: List<String> = emptyList(),
    val thumbnailBacks: List<String> = emptyList(),
    val cartridges: List<String> = emptyList(),
    val manuals: List<String> = emptyList(),
)

fun GameMetadataOptions.toGameMetadata(): GameMetadata {
    return GameMetadata(
        name = names.firstOrNull(),
        releaseDate = releaseDates.firstOrNull(),
        publisher = publishers.firstOrNull(),
        developer = developers.firstOrNull(),
        country = countries.firstOrNull(),
        thumbnail = thumbnails.firstOrNull(),
        thumbnailBack = thumbnailBacks.firstOrNull(),
        cartridgeImage = cartridges.firstOrNull(),
        manualUrl = manuals.firstOrNull(),
        system = null,
        romName = null,
        summary = null,
        debugInfo = null
    )
}
