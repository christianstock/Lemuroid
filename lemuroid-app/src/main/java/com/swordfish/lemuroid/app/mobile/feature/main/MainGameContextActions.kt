package com.swordfish.lemuroid.app.mobile.feature.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.LemuroidGameImage
import com.swordfish.lemuroid.lib.library.db.entity.Game

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainGameContextActions(
    selectedGameState: MutableState<Game?>,
    shortcutSupported: Boolean,
    onGamePlay: (Game) -> Unit,
    onGameRestart: (Game) -> Unit,
    onFavoriteToggle: (Game, Boolean) -> Unit,
    onCreateShortcut: (Game) -> Unit,
    onResetCheats: (Game) -> Unit,
) {
    val modalSheetState = rememberModalBottomSheetState(true)
    val selectedGame = selectedGameState.value

    LaunchedEffect(selectedGame) {
        if (selectedGame != null) {
            modalSheetState.show()
        } else {
            modalSheetState.hide()
        }
    }

    if (selectedGame != null) {
        ModalBottomSheet(
            sheetState = modalSheetState,
            onDismissRequest = { selectedGameState.value = null },
        ) {
            ContextActionContent(
                selectedGame = selectedGame,
                onGamePlay = onGamePlay,
                selectedGameState = selectedGameState,
                onGameRestart = onGameRestart,
                onResetCheats = onResetCheats,
            )
        }
    }
}

@Composable
private fun ContextActionContent(
    selectedGame: Game,
    onGamePlay: (Game) -> Unit,
    selectedGameState: MutableState<Game?>,
    onGameRestart: (Game) -> Unit,
    onResetCheats: (Game) -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
                .windowInsetsPadding(WindowInsets.safeContent.only(WindowInsetsSides.Bottom)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- LARGE BOX ART ---
        Box(
            modifier = Modifier
                .padding(top = 16.dp)
                .size(200.dp) // Even bigger as requested
                .clip(MaterialTheme.shapes.medium),
            contentAlignment = Alignment.Center
        ) {
            LemuroidGameImage(
                game = selectedGame,
                modifier = Modifier.fillMaxSize(),
                applyAspectRatio = false
            )
        }

        // --- TITLE & PUBLISHER BELOW ---
        Text(
            text = selectedGame.title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 20.dp, start = 24.dp, end = 24.dp)
        )
        
        selectedGame.developer?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )
        }

        Divider(modifier = Modifier.padding(horizontal = 24.dp))

        // --- ACTIONS ---
        ContextActionEntry(
            label = stringResource(id = R.string.game_context_menu_resume),
            icon = Icons.Default.PlayArrow,
            onClick = {
                onGamePlay(selectedGame)
                selectedGameState.value = null
            },
        )
        ContextActionEntry(
            label = stringResource(id = R.string.game_context_menu_restart),
            icon = Icons.Default.RestartAlt,
            onClick = {
                onGameRestart(selectedGame)
                selectedGameState.value = null
            },
        )
        ContextActionEntry(
            label = stringResource(id = R.string.game_context_menu_reset_cheats),
            icon = Icons.Default.Star,
            onClick = {
                onResetCheats(selectedGame)
                selectedGameState.value = null
            },
        )
    }
}

@Composable
private fun ContextActionEntry(
    modifier: Modifier = Modifier,
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .height(64.dp) // Taller row for better touch targets
                .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(28.dp)
        )
        Text(
            modifier = Modifier.padding(start = 16.dp),
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
    }
}
