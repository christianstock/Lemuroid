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
import com.swordfish.lemuroid.common.coroutines.combine
import com.swordfish.lemuroid.lib.core.CoresSelection
import com.swordfish.lemuroid.lib.library.CoreID
import com.swordfish.lemuroid.lib.library.MetaSystemID
import com.swordfish.lemuroid.lib.library.SystemID
import com.swordfish.lemuroid.lib.library.db.RetrogradeDatabase
import com.swordfish.lemuroid.lib.library.db.entity.Game
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class HomeViewModel(
    private val appContext: Context,
    private val retrogradeDb: RetrogradeDatabase,
    private val coresSelection: CoresSelection,
) : ViewModel() {
    companion object {
        const val DEBOUNCE_TIME = 100L
        private const val PREFS_NAME = "home_prefs"
        private const val KEY_SELECTED_SYSTEM = "selected_system_id"
    }

    private val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    class Factory(
        val appContext: Context,
        val retrogradeDb: RetrogradeDatabase,
        val coresSelection: CoresSelection,
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(appContext, retrogradeDb, coresSelection) as T
        }
    }

    data class UIState(
        val games: List<Game> = emptyList(),
        val availableSystems: List<String> = emptyList(),
        val selectedSystemId: String? = null,
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
    private val uiStates = MutableStateFlow(UIState())

    fun getViewStates(): Flow<UIState> {
        return uiStates
    }

    fun setSelectedSystem(systemId: String) {
        selectedSystemIdState.value = systemId
        prefs.edit().putString(KEY_SELECTED_SYSTEM, systemId).apply()
        refreshCountState.value++
    }

    fun changeLocalStorageFolder(context: Context) {
        StorageFrameworkPickerLauncher.pickFolder(context)
    }

    fun updatePermissions(context: Context) {
        notificationsPermissionEnabledState.value = isNotificationsPermissionGranted(context)
        microphonePermissionEnabledState.value = isMicrophonePermissionGranted(context)
        refreshCountState.value++
    }

    private fun isNotificationsPermissionGranted(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return true
        }

        val permissionResult =
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            )

        return permissionResult == PackageManager.PERMISSION_GRANTED
    }

    private fun isMicrophonePermissionGranted(context: Context): Boolean {
        val permissionResult =
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO,
            )

        return permissionResult == PackageManager.PERMISSION_GRANTED
    }

    private fun buildViewState(
        games: List<Game>,
        availableSystems: List<String>,
        selectedSystemId: String?,
        indexInProgress: Boolean,
        refreshCount: Int,
        notificationsPermissionEnabled: Boolean,
        showMicrophoneCard: Boolean,
        showDesmumeWarning: Boolean,
    ): UIState {
        val noGames = games.isEmpty() && availableSystems.isEmpty()

        // Clean up game titles by removing info in brackets only
        val cleanedGames = games.map { game ->
            var cleanedTitle = game.title
                .replace(Regex("\\s*\\([^)]*\\)"), "") // Remove (...)
                .replace(Regex("\\s*\\[[^]]*\\]"), "") // Remove [...]
                .trim()

            // Move ", The" to the front (e.g. "Lord of the Rings, The" -> "The Lord of the Rings")
            if (cleanedTitle.contains(", The", ignoreCase = true)) {
                cleanedTitle = cleanedTitle.replace(Regex("^(.*),\\s*[Tt]he\\b(.*)$"), "The $1$2").trim()
            }

            if (cleanedTitle.isEmpty()) game else game.copy(title = cleanedTitle)
        }

        return UIState(
            games = cleanedGames,
            availableSystems = availableSystems,
            selectedSystemId = selectedSystemId,
            indexInProgress = indexInProgress,
            refreshCount = refreshCount,
            showNoNotificationPermissionCard = !notificationsPermissionEnabled,
            showNoMicrophonePermissionCard = showMicrophoneCard,
            showNoGamesCard = noGames,
            showDesmumeDeprecatedCard = showDesmumeWarning,
        )
    }

    init {
        // If no system is selected, try to pick the last played one
        if (selectedSystemIdState.value == null) {
            viewModelScope.launch {
                val lastPlayedGame = retrogradeDb.gameDao().selectLastPlayedGameFlow().first()
                if (lastPlayedGame != null) {
                    setSelectedSystem(lastPlayedGame.systemId)
                } else {
                    val systems = retrogradeDb.gameDao().selectSystems()
                    if (systems.isNotEmpty()) {
                        setSelectedSystem(systems.first())
                    }
                }
            }
        }

        viewModelScope.launch {
            @OptIn(ExperimentalCoroutinesApi::class)
            val gamesFlow = selectedSystemIdState.flatMapLatest { systemId ->
                if (systemId != null) {
                    // systemId might be a MetaSystem name (e.g. "GBA") or a dbname (e.g. "gba")
                    // Let's check if it matches a MetaSystemID
                    val metaSystem = try { MetaSystemID.valueOf(systemId) } catch (_: Exception) { null }
                    if (metaSystem != null) {
                        val systemIds = metaSystem.systemIDs.map { it.dbname }
                        retrogradeDb.gameDao().selectBySystemsOrderedByRecentsFlow(systemIds)
                    } else {
                        retrogradeDb.gameDao().selectBySystemOrderedByRecentsFlow(systemId)
                    }
                } else {
                    flowOf(emptyList())
                }
            }

            val uiStatesFlow =
                kotlinx.coroutines.flow.combine(
                    gamesFlow,
                    retrogradeDb.gameDao().selectSystemsFlow(),
                    selectedSystemIdState,
                    indexingInProgress(appContext),
                    refreshCountState,
                    notificationsPermissionEnabledState,
                    microphoneNotification(retrogradeDb),
                    desmumeWarningNotification()
                ) { params ->
                    buildViewState(
                        games = params[0] as List<Game>,
                        availableSystems = params[1] as List<String>,
                        selectedSystemId = params[2] as String?,
                        indexInProgress = params[3] as Boolean,
                        refreshCount = params[4] as Int,
                        notificationsPermissionEnabled = params[5] as Boolean,
                        showMicrophoneCard = params[6] as Boolean,
                        showDesmumeWarning = params[7] as Boolean
                    )
                }

            uiStatesFlow
                .debounce(DEBOUNCE_TIME)
                .flowOn(Dispatchers.IO)
                .collect { uiStates.value = it }
        }
    }

    private fun indexingInProgress(appContext: Context) =
        PendingOperationsMonitor(appContext).anyLibraryOperationInProgress()

    private fun dsGamesCount(retrogradeDb: RetrogradeDatabase): Flow<Int> {
        return retrogradeDb.gameDao().selectSystemsWithCount()
            .map { systems ->
                systems
                    .firstOrNull { it.systemId == SystemID.NDS.dbname }
                    ?.count
                    ?: 0
            }
            .distinctUntilChanged()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun microphoneNotification(db: RetrogradeDatabase): Flow<Boolean> {
        return microphonePermissionEnabledState
            .flatMapLatest { isMicrophoneEnabled ->
                if (isMicrophoneEnabled) {
                    flowOf(false)
                } else {
                    combine(
                        coresSelection.getSelectedCores(),
                        dsGamesCount(db),
                    ) { cores, dsCount ->
                        cores.any { it.coreConfig.supportsMicrophone } &&
                            dsCount > 0
                    }
                }
                    .distinctUntilChanged()
            }
    }

    private fun desmumeWarningNotification(): Flow<Boolean> {
        return coresSelection.getSelectedCores()
            .map { cores -> cores.any { it.coreConfig.coreID == CoreID.DESMUME } }
            .distinctUntilChanged()
    }
}
