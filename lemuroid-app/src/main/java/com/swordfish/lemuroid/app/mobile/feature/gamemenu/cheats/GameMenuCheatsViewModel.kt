package com.swordfish.lemuroid.app.mobile.feature.gamemenu.cheats

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.swordfish.lemuroid.app.shared.cheats.CheatManager
import com.swordfish.lemuroid.lib.library.db.entity.GameCheatEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

    private val _cheats = MutableStateFlow<List<GameCheatEntity>>(emptyList())
    val cheats: StateFlow<List<GameCheatEntity>> = _cheats

    var cheatsChanged: Boolean = false
        private set

    init {
        loadCheats()
    }

    private fun loadCheats() {
        viewModelScope.launch {
            _cheats.value = cheatManager.getAllCheats(gameId)
        }
    }

    fun toggleCheat(cheat: GameCheatEntity, enabled: Boolean) {
        viewModelScope.launch {
            cheatManager.updateCheatEnabled(gameId, cheat.cheatIndex, enabled)
            cheatsChanged = true
            loadCheats()
        }
    }

    fun importCheats(uri: Uri) {
        viewModelScope.launch {
            cheatManager.importCheats(appContext, gameId, uri)
            cheatsChanged = true
            loadCheats()
        }
    }
}
