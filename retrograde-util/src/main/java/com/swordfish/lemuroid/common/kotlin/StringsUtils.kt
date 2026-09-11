package com.swordfish.lemuroid.common.kotlin

fun String.startsWithAny(strings: Collection<String>) = strings.any { this.startsWith(it) }

fun String.cleanGameTitle(): String {
    var cleaned = this
        .replace(Regex("\\s*\\([^)]*\\)"), "") 
        .replace(Regex("\\s*\\[[^]]*\\]"), "")
        .trim()
    
    // Correct grammar pattern like "Lord of the Rings, The" -> "The Lord of the Rings"
    if (cleaned.contains(", The", ignoreCase = true)) {
        cleaned = cleaned.replace(Regex("^(.*),\\s*[Tt]he\\b(.*)$"), "The $1$2").trim()
    }
    
    // Remove technical suffixes but keep subtitles
    val technicalSuffixes = listOf("Force GBA", "Enhanced Mode", "Hack", "Translation", "v1.0", "v1.1", "Colorized")
    if (cleaned.contains(" - ")) {
        val parts = cleaned.split(" - ")
        if (technicalSuffixes.any { parts.last().contains(it, ignoreCase = true) }) {
            cleaned = cleaned.substringBeforeLast(" - ").trim()
        }
    }
    
    return cleaned.ifEmpty { this }
}

/**
 * Technical cleaning for Art URLs. 
 * Strips technical tags [Hack] etc but preserves region tags (USA).
 */
fun String.cleanForArtUrl(): String {
    val technicalSuffixes = listOf("Force GBA", "Enhanced Mode", "Hack", "Translation", "v1.0", "v1.1", "Colorized", "Polar-Star")
    
    var result = this
    // 1. Remove anything in square brackets (technical tags)
    result = result.replace(Regex("\\s*\\[[^\\]]*\\]"), "")
    
    // 2. Remove technical hyphenated suffixes ONLY if they match technical strings
    if (result.contains(" - ")) {
        val parts = result.split(" - ")
        if (technicalSuffixes.any { parts.last().contains(it, ignoreCase = true) }) {
            result = result.substringBeforeLast(" - ").trim()
        }
    }
    
    return result.trim()
}
