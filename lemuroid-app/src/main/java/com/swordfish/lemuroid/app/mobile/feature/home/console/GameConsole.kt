package com.swordfish.lemuroid.app.mobile.feature.home.console

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

/**
 * Orchestrator composable that renders the interactive lower console overlay
 * matching the currently selected system ID.
 */
@Composable
fun GameConsole(
    systemId: String?,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val systemIdNorm = remember(systemId) { systemId?.lowercase().orEmpty() }

    when (systemIdNorm) {
        "gb" -> GameBoyConsole(
            onOpenSettings = onOpenSettings,
            modifier = modifier
        )
        "gbc" -> GameBoyColorConsole(
            onOpenSettings = onOpenSettings,
            modifier = modifier
        )
        "gba" -> GameBoyAdvanceConsole(
            onOpenSettings = onOpenSettings,
            modifier = modifier
        )
        else -> GameBoyConsole(
            onOpenSettings = onOpenSettings,
            modifier = modifier
        )
    }
}
