package com.swordfish.lemuroid.app.mobile.feature.games

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.swordfish.lemuroid.lib.library.MetaSystemID
import com.swordfish.lemuroid.lib.library.db.RetrogradeDatabase
import com.swordfish.lemuroid.lib.library.db.entity.Game
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class GamesViewModel(
    private val retrogradeDb: RetrogradeDatabase,
    private val initialMetaSystem: MetaSystemID,
) : ViewModel() {
    enum class SortMode {
        RECENTS, ALPHABETICAL, RELEASE
    }

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _showAllSystems = MutableStateFlow(false)
    val showAllSystems: StateFlow<Boolean> = _showAllSystems

    private val _sortMode = MutableStateFlow(SortMode.RECENTS)
    val sortMode: StateFlow<SortMode> = _sortMode

    val currentMetaSystem = initialMetaSystem

    val games: StateFlow<List<Game>> = combine(
        retrogradeDb.gameDao().selectAllFlow(),
        _searchQuery,
        _showAllSystems,
        _sortMode
    ) { allGames, query, showAll, sort ->
        var filtered = allGames
        
        // 1. System Filter
        if (!showAll) {
            val dbNames = initialMetaSystem.systemIDs.map { it.dbname }
            filtered = filtered.filter { dbNames.contains(it.systemId) }
        }

        // 2. Search Filter
        if (query.isNotEmpty()) {
            filtered = filtered.filter { 
                it.title.contains(query, ignoreCase = true) || 
                it.country?.contains(query, ignoreCase = true) == true ||
                it.summary?.contains(query, ignoreCase = true) == true
            }
        }

        // 3. Sorting
        when (sort) {
            SortMode.RECENTS -> filtered.sortedByDescending { it.lastPlayedAt ?: 0L }
            SortMode.ALPHABETICAL -> filtered.sortedBy { it.title.lowercase() }
            SortMode.RELEASE -> filtered.sortedByDescending { it.releaseDate ?: "" }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleShowAllSystems(showAll: Boolean) {
        _showAllSystems.value = showAll
    }

    fun updateSortMode(mode: SortMode) {
        _sortMode.value = mode
    }

    class Factory(
        private val retrogradeDb: RetrogradeDatabase,
        private val initialMetaSystem: MetaSystemID,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return GamesViewModel(retrogradeDb, initialMetaSystem) as T
        }
    }
}
