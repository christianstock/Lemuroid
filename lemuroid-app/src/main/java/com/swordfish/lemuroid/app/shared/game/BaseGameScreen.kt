package com.swordfish.lemuroid.app.shared.game

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.swordfish.lemuroid.app.shared.cheats.ui.CheatMenuScreen
import com.swordfish.lemuroid.app.shared.game.viewmodel.GameViewModelRetroGameView
import com.swordfish.lemuroid.lib.library.db.entity.GameCheatEntity
import kotlinx.coroutines.launch

@Composable
fun BaseGameScreen(
    viewModel: BaseGameScreenViewModel,
    gameScreen: @Composable (BaseGameScreenViewModel) -> Unit,
) {
    val gameState =
        viewModel.getGameState()
            .collectAsState(GameViewModelRetroGameView.GameState.Uninitialized)
            .value

    val isGameReady =
        gameState is GameViewModelRetroGameView.GameState.Loaded ||
            gameState is GameViewModelRetroGameView.GameState.Ready

    val cheatMenuVisible = viewModel.cheatMenuVisible.collectAsState().value
    val coroutineScope = rememberCoroutineScope()

    if (isGameReady) {
        Box(modifier = Modifier.fillMaxSize()) {
            gameScreen(viewModel)

            // Cheat menu overlay
            AnimatedVisibility(
                visible = cheatMenuVisible,
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxSize(0.9f),
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CheatMenuScreen(
                        cheatsFlow = viewModel.getCheats(),
                        onCheatToggle = { cheat, enabled ->
                            coroutineScope.launch {
                                viewModel.toggleCheat(cheat, enabled)
                            }
                        },
                        onClose = {
                            viewModel.closeCheatMenu()
                        },
                    )
                }
            }
        }
    } else {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier.wrapContentSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CircularProgressIndicator()

                val message = if (gameState is GameViewModelRetroGameView.GameState.Loading) gameState.message else null
                AnimatedVisibility(message != null) {
                    Text(text = message!!, color = MaterialTheme.colorScheme.onBackground)
                }
            }
        }
    }
}
