package com.swordfish.lemuroid.app.mobile.feature.home

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.swordfish.lemuroid.app.shared.library.PendingOperationsMonitor
import com.swordfish.lemuroid.app.shared.settings.StorageFrameworkPickerLauncher
import com.swordfish.lemuroid.common.kotlin.cleanGameTitle
import com.swordfish.lemuroid.lib.core.CoresSelection
import com.swordfish.lemuroid.lib.library.CoreID
import com.swordfish.lemuroid.lib.library.SystemID
import com.swordfish.lemuroid.lib.library.db.RetrogradeDatabase
import com.swordfish.lemuroid.lib.library.db.entity.Game
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class HomeViewModel(
    private val appContext: Context,
    private val retrogradeDb: RetrogradeDatabase,
    private val coresSelection: CoresSelection,
) : ViewModel() {
    companion object {
        private const val PREFS_NAME = "home_prefs"
        private const val KEY_SELECTED_SYSTEM = "selected_system_id"
        const val BASE_PAGE_INDEX = Int.MAX_VALUE / 2
    }

    private val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    class Factory(
        private val appContext: Context,
        private val retrogradeDb: RetrogradeDatabase,
        private val coresSelection: CoresSelection,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(appContext, retrogradeDb, coresSelection) as T
        }
    }

    data class SystemLibrary(
        val systemId: String,
        val games: List<Game>,
        val lastResetTime: Long = 0L
    )

    data class UIState(
        val systemLibraries: List<SystemLibrary> = emptyList(),
        val selectedSystemId: String? = null,
        val systemScrollPositions: Map<String, Int> = emptyMap(),
        val indexInProgress: Boolean = true,
        val refreshCount: Int = 0,
        val showNoNotificationPermissionCard: Boolean = false,
        val showNoMicrophonePermissionCard: Boolean = false,
        val showNoGamesCard: Boolean = false,
        val showDesmumeDeprecatedCard: Boolean = false,
    )

    private val microphonePermissionEnabledState = MutableStateFlow(true)
    private val notificationsPermissionEnabledState = MutableStateFlow(true)
    private val refreshCountState = MutableStateFlow(0)
    private val selectedSystemIdState = MutableStateFlow<String?>(prefs.getString(KEY_SELECTED_SYSTEM, null))
    private val systemScrollIndicesState = MutableStateFlow<Map<String, Int>>(emptyMap())
    private val systemResetsState = MutableStateFlow<Map<String, Long>>(emptyMap())
    private val uiStates = MutableStateFlow(UIState())

    fun getViewStates(): Flow<UIState> = uiStates

    fun setSelectedSystem(systemId: String) {
        val normalizedId = systemId.lowercase()
        if (selectedSystemIdState.value == normalizedId) return
        
        selectedSystemIdState.value = normalizedId
        prefs.edit().putString(KEY_SELECTED_SYSTEM, normalizedId).apply()
    }

    fun setSystemScrollPosition(systemId: String, page: Int) {
        val uiState = uiStates.value
        val library = uiState.systemLibraries.find { it.systemId == systemId } ?: return
        if (library.games.isEmpty()) return

        val relativeIndex = page % library.games.size
        val current = systemScrollIndicesState.value.toMutableMap()
        if (current[systemId] == relativeIndex) return
        
        current[systemId] = relativeIndex
        systemScrollIndicesState.value = current
    }

    fun onGameLaunched(game: Game) {
        viewModelScope.launch(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            retrogradeDb.gameDao().update(game.copy(lastPlayedAt = now))
            
            val indices = systemScrollIndicesState.value.toMutableMap()
            indices[game.systemId] = 0
            systemScrollIndicesState.value = indices

            val resets = systemResetsState.value.toMutableMap()
            resets[game.systemId] = now
            systemResetsState.value = resets
        }
    }

    fun changeLocalStorageFolder(context: Context) {
        StorageFrameworkPickerLauncher.pickFolder(context)
    }

    fun refresh() {
        refreshCountState.value++
    }

    fun updatePermissions(context: Context) {
        notificationsPermissionEnabledState.value = isNotificationsPermissionGranted(context)
        microphonePermissionEnabledState.value = isMicrophonePermissionGranted(context)
    }

    private fun isNotificationsPermissionGranted(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    }

    private fun isMicrophonePermissionGranted(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
    }

    init {
        viewModelScope.launch {
            if (selectedSystemIdState.value == null) {
                val lastPlayedGame = retrogradeDb.gameDao().selectLastPlayedGameFlow().first()
                if (lastPlayedGame != null) {
                    setSelectedSystem(lastPlayedGame.systemId)
                } else {
                    val systems = retrogradeDb.gameDao().selectSystems()
                    if (systems.isNotEmpty()) setSelectedSystem(systems.first())
                }
            }
        }

        viewModelScope.launch {
            val uiStatesFlow = combine(
                retrogradeDb.gameDao().selectSystemsFlow(), 
                retrogradeDb.gameDao().selectAllFlow(),
                selectedSystemIdState,
                systemScrollIndicesState,
                systemResetsState,
                refreshCountState,
                notificationsPermissionEnabledState,
                indexingInProgress(appContext).onStart { emit(false) },
                microphoneNotification(retrogradeDb).onStart { emit(false) },
                desmumeWarningNotification().onStart { emit(false) }
            ) { params ->
                val availableSystems = params[0] as List<String>
                val allGames = params[1] as List<Game>
                val selectedSystemId = params[2] as String?
                @Suppress("UNCHECKED_CAST")
                val scrollIndices = params[3] as Map<String, Int>
                @Suppress("UNCHECKED_CAST")
                val systemResets = params[4] as Map<String, Long>
                val refreshCount = params[5] as Int
                val notificationsEnabled = params[6] as Boolean
                val indexInProgress = params[7] as Boolean
                val microphoneEnabled = params[8] as Boolean
                val desmumeWarning = params[9] as Boolean

                val systemLibraries = availableSystems.map { sysId ->
                    val systemGames = allGames
                        .filter { it.systemId.lowercase() == sysId.lowercase() }
                        .sortedWith(compareByDescending<Game> { it.lastPlayedAt ?: 0L }.thenBy { it.title })
                        .map { game ->
                            val cleaned = game.title.cleanGameTitle()
                            if (cleaned != game.title) game.copy(title = cleaned) else game
                        }
                    
                    SystemLibrary(sysId, systemGames, systemResets[sysId] ?: 0L)
                }

                val systemScrollPositions = systemLibraries.associate { lib ->
                    val index = scrollIndices[lib.systemId] ?: 0
                    val gamesSize = lib.games.size.coerceAtLeast(1)
                    val base = (BASE_PAGE_INDEX / gamesSize) * gamesSize
                    val page = base + index
                    lib.systemId to page
                }

                UIState(
                    systemLibraries = systemLibraries,
                    selectedSystemId = selectedSystemId,
                    systemScrollPositions = systemScrollPositions,
                    indexInProgress = indexInProgress,
                    refreshCount = refreshCount,
                    showNoNotificationPermissionCard = !notificationsEnabled,
                    showNoMicrophonePermissionCard = microphoneEnabled,
                    showNoGamesCard = systemLibraries.isEmpty(),
                    showDesmumeDeprecatedCard = desmumeWarning
                )
            }.distinctUntilChanged()

            uiStatesFlow
                .flowOn(Dispatchers.IO)
                .collect { uiStates.value = it }
        }
    }

    private fun indexingInProgress(appContext: Context) =
        PendingOperationsMonitor(appContext).anyLibraryOperationInProgress()

    private fun dsGamesCount(retrogradeDb: RetrogradeDatabase): Flow<Int> {
        return retrogradeDb.gameDao().selectSystemsWithCount()
            .map { systems -> systems.firstOrNull { it.systemId == SystemID.NDS.dbname }?.count ?: 0 }
            .distinctUntilChanged()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun microphoneNotification(db: RetrogradeDatabase): Flow<Boolean> {
        return microphonePermissionEnabledState.flatMapLatest { isEnabled ->
            if (isEnabled) flowOf(false) else combine(coresSelection.getSelectedCores(), dsGamesCount(db)) { cores, dsCount ->
                cores.any { it.coreConfig.supportsMicrophone } && dsCount > 0
            }
        }.distinctUntilChanged()
    }

    private fun desmumeWarningNotification(): Flow<Boolean> {
        return coresSelection.getSelectedCores().map { cores -> cores.any { it.coreConfig.coreID == CoreID.DESMUME } }.distinctUntilChanged()
    }
}
