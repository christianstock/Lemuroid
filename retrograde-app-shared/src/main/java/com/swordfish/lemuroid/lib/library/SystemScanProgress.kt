package com.swordfish.lemuroid.lib.library

data class SystemScanProgress(
    val systemName: String,
    val gamesFound: Int,
    val isCurrentlyScanning: Boolean = false,
    val isComplete: Boolean = false
)
