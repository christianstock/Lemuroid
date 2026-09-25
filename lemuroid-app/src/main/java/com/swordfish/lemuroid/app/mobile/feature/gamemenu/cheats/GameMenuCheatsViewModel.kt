package com.swordfish.lemuroid.app.mobile.feature.gamemenu.cheats

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.swordfish.lemuroid.app.shared.cheats.CheatManager
import com.swordfish.lemuroid.lib.library.db.entity.GameCheatEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
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

    private val allCheats: Flow<List<GameCheatEntity>> = cheatManager.getAllCheatsFlow(gameId)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedSources = MutableStateFlow(setOf<String>())
    val selectedSources: StateFlow<Set<String>> = _selectedSources.asStateFlow()

    private val _isRescanning = MutableStateFlow(false)
    val isRescanning: StateFlow<Boolean> = _isRescanning.asStateFlow()

    private val _deletedCheats = MutableStateFlow<Map<Int, GameCheatEntity>>(emptyMap())
    val deletedCheats: StateFlow<Map<Int, GameCheatEntity>> = _deletedCheats.asStateFlow()

    var cheatsChanged: Boolean = false
        private set

    val cheats: Flow<List<GameCheatEntity>> = allCheats.combine(
        _searchQuery
    ) { allCheats, query ->
        val filtered = if (query.isBlank()) {
            allCheats
        } else {
            allCheats.filter { cheat ->
                val queryLower = query.lowercase()
                cheat.description.lowercase().contains(queryLower) ||
                cheat.code.lowercase().contains(queryLower)
            }
        }

        val selected = _selectedSources.value
        if (selected.isEmpty()) {
            filtered
        } else {
            filtered.filter { it.source in selected }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleSourceFilter(source: String) {
        val current = _selectedSources.value.toMutableSet()
        if (current.contains(source)) {
            current.remove(source)
        } else {
            current.add(source)
        }
        _selectedSources.value = current
    }

    fun clearSourceFilter() {
        _selectedSources.value = emptySet()
    }

    fun toggleCheat(cheat: GameCheatEntity, enabled: Boolean) {
        viewModelScope.launch {
            cheatManager.updateCheatEnabled(gameId, cheat.cheatIndex, enabled)
            cheatsChanged = true
        }
    }

    fun deleteCheat(cheat: GameCheatEntity) {
        viewModelScope.launch {
            cheatManager.deleteCheat(cheat.id)
            _deletedCheats.value = _deletedCheats.value + (cheat.id to cheat)
            cheatsChanged = true
        }
    }

    fun undoDeleteCheat(cheatId: Int) {
        val cheat = _deletedCheats.value[cheatId] ?: return
        viewModelScope.launch {
            cheatManager.insertCheat(cheat)
            _deletedCheats.value = _deletedCheats.value - cheatId
            cheatsChanged = true
        }
    }

    fun updateCheatDisplayOrder(cheatId: Int, newOrder: Int) {
        viewModelScope.launch {
            cheatManager.updateCheatDisplayOrder(cheatId, newOrder)
            cheatsChanged = true
        }
    }

    fun rescandCheatsForCurrentGame() {
        viewModelScope.launch {
            _isRescanning.value = true
            try {
                cheatManager.rescandGameForCheats(gameId)
                cheatsChanged = true
            } finally {
                _isRescanning.value = false
            }
        }
    }

    fun disableAllCheats() {
        viewModelScope.launch {
            cheatManager.disableAllCheats(gameId)
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
