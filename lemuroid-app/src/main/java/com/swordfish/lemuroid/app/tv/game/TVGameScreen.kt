package com.swordfish.lemuroid.app.tv.game

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.swordfish.lemuroid.app.shared.game.BaseGameScreenViewModel
import com.swordfish.lemuroid.app.shared.game.viewmodel.GameViewModelRetroGameView
import androidx.compose.runtime.getValue

@Composable
fun TVGameScreen(viewModel: BaseGameScreenViewModel) {
    val lifecycle = LocalLifecycleOwner.current

    val gameState by viewModel.getGameState().collectAsState(GameViewModelRetroGameView.GameState.Uninitialized)
    val isLoaded = gameState is GameViewModelRetroGameView.GameState.Loaded || gameState is GameViewModelRetroGameView.GameState.Ready

    if (isLoaded) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                viewModel.createRetroView(ctx, lifecycle)!!
            },
        )
    }

    val isLoading =
        viewModel.loadingState
            .collectAsState(true)
            .value

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
    }
}
