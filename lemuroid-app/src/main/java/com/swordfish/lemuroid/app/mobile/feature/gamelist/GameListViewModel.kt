package com.swordfish.lemuroid.app.mobile.feature.gamelist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.swordfish.lemuroid.lib.library.MetaSystemID
import com.swordfish.lemuroid.lib.library.db.RetrogradeDatabase
import com.swordfish.lemuroid.lib.library.db.entity.Game
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GameListViewModel(
    private val retrogradeDb: RetrogradeDatabase,
    private val initialMetaSystem: MetaSystemID,
) : ViewModel() {

    enum class SortMode {
        RECENTS, ALPHABETICAL, RELEASE
    }

    companion object {
        // Enforce strict chronological handheld order: GB -> GBC -> GBA
        val ALLOWED_META_SYSTEMS = listOf(
            MetaSystemID.GB,
            MetaSystemID.GBC,
            MetaSystemID.GBA
        )

        private val ALLOWED_DB_NAMES = ALLOWED_META_SYSTEMS
            .flatMap { it.systemIDs }
            .map { it.dbname }
            .toSet()
    }

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _showAllSystems = MutableStateFlow(false)
    val showAllSystems: StateFlow<Boolean> = _showAllSystems

    private val _sortMode = MutableStateFlow(SortMode.RECENTS)
    val sortMode: StateFlow<SortMode> = _sortMode

    private val _currentMetaSystem = MutableStateFlow(
        if (initialMetaSystem in ALLOWED_META_SYSTEMS) initialMetaSystem else ALLOWED_META_SYSTEMS.first()
    )
    val currentMetaSystem: StateFlow<MetaSystemID> = _currentMetaSystem

    val games: StateFlow<List<Game>> = combine(
        retrogradeDb.gameDao().selectAllFlow(),
        _searchQuery,
        _showAllSystems,
        _sortMode,
        _currentMetaSystem,
    ) { allGames, query, showAll, sort, metaSystem ->

        // 1. System Whitelist & MetaSystem Filter
        var filtered = if (showAll) {
            allGames.filter { it.systemId in ALLOWED_DB_NAMES }
        } else {
            val activeDbNames = metaSystem.systemIDs.map { it.dbname }.toSet()
            allGames.filter { it.systemId in activeDbNames }
        }

        // 2. Search Query Filter
        if (query.isNotBlank()) {
            val trimmedQuery = query.trim()
            filtered = filtered.filter { game ->
                game.title.contains(trimmedQuery, ignoreCase = true) ||
                        game.country?.contains(trimmedQuery, ignoreCase = true) == true ||
                        game.summary?.contains(trimmedQuery, ignoreCase = true) == true
            }
        }

        // 3. Sorting
        when (sort) {
            SortMode.RECENTS -> filtered.sortedByDescending { it.lastPlayedAt ?: 0L }
            SortMode.ALPHABETICAL -> filtered.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.title })
            SortMode.RELEASE -> filtered.sortedByDescending { it.releaseDate ?: "" }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun switchToPreviousSystem() {
        val currentIndex = ALLOWED_META_SYSTEMS.indexOf(_currentMetaSystem.value)
        if (currentIndex != -1) {
            val prevIndex = (currentIndex - 1 + ALLOWED_META_SYSTEMS.size) % ALLOWED_META_SYSTEMS.size
            _currentMetaSystem.value = ALLOWED_META_SYSTEMS[prevIndex]
        } else {
            _currentMetaSystem.value = ALLOWED_META_SYSTEMS.last()
        }
    }

    fun switchToNextSystem() {
        val currentIndex = ALLOWED_META_SYSTEMS.indexOf(_currentMetaSystem.value)
        if (currentIndex != -1) {
            val nextIndex = (currentIndex + 1) % ALLOWED_META_SYSTEMS.size
            _currentMetaSystem.value = ALLOWED_META_SYSTEMS[nextIndex]
        } else {
            _currentMetaSystem.value = ALLOWED_META_SYSTEMS.first()
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleShowAllSystems(showAll: Boolean) {
        _showAllSystems.value = showAll
    }

    fun updateSortMode(mode: SortMode) {
        _sortMode.value = mode
    }

    fun onGameLaunched(game: Game) {
        viewModelScope.launch(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            retrogradeDb.gameDao().update(game.copy(lastPlayedAt = now))
        }
    }

    // Emits only the meta-systems from ALLOWED_META_SYSTEMS that actually have games in the DB
    val availableMetaSystems: StateFlow<List<MetaSystemID>> = retrogradeDb.gameDao().selectAllFlow()
        .map { allGames ->
            ALLOWED_META_SYSTEMS.filter { metaSystem ->
                val dbNames = metaSystem.systemIDs.map { it.dbname }.toSet()
                allGames.any { it.systemId in dbNames }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    class Factory(
        private val retrogradeDb: RetrogradeDatabase,
        private val initialMetaSystem: MetaSystemID,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return GameListViewModel(retrogradeDb, initialMetaSystem) as T
        }
    }
}
