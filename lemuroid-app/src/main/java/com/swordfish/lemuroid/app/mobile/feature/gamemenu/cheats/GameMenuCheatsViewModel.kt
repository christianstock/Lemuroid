package com.swordfish.lemuroid.app.mobile.feature.gamemenu.cheats

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.swordfish.lemuroid.app.shared.cheats.CheatManager
import com.swordfish.lemuroid.lib.library.db.entity.GameCheatEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class GameMenuCheatsViewModel(
    private val appContext: Context,
    private val gameId: Int,
    private val cheatManager: CheatManager
) : ViewModel() {
    class Factory(
        private val appContext: Context,
        private val gameId: Int,
        private val cheatManager: CheatManager
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return GameMenuCheatsViewModel(appContext, gameId, cheatManager) as T
        }
    }

    val cheats: Flow<List<GameCheatEntity>> = cheatManager.getAllCheatsFlow(gameId)

    var cheatsChanged: Boolean = false
        private set

    fun toggleCheat(cheat: GameCheatEntity, enabled: Boolean) {
        viewModelScope.launch {
            cheatManager.updateCheatEnabled(gameId, cheat.cheatIndex, enabled)
            cheatsChanged = true
        }
    }

    fun importCheats(uri: Uri) {
        viewModelScope.launch {
            cheatManager.importCheats(appContext, gameId, uri)
            cheatsChanged = true
        }
    }
}
