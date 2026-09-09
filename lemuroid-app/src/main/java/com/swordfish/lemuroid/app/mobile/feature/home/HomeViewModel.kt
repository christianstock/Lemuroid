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
import kotlinx.coroutines.flow.catch
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

    fun getViewStates(): Flow<UIState> = uiStates

    fun setSelectedSystem(systemId: String) {
        val normalizedId = systemId.lowercase()
        selectedSystemIdState.value = normalizedId
        prefs.edit().putString(KEY_SELECTED_SYSTEM, normalizedId).apply()
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
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    }

    private fun isMicrophonePermissionGranted(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
    }

    private fun buildViewState(
        games: List<Game>,
        availableSystems: List<String>,
        selectedSystemId: String?,
        refreshCount: Int,
        indexInProgress: Boolean,
        notificationsPermissionEnabled: Boolean,
        showMicrophoneCard: Boolean,
        showDesmumeWarning: Boolean,
    ): UIState {
        val cleanedGames = games.map { game ->
            var cleanedTitle = game.title
                .replace(Regex("\\s*\\([^)]*\\)"), "") 
                .replace(Regex("\\s*\\[[^]]*\\]"), "")
                .trim()
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
            showNoGamesCard = games.isEmpty() && availableSystems.isEmpty(),
            showDesmumeDeprecatedCard = showDesmumeWarning,
        )
    }

    init {
        // Handle initial selection
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

        // --- UNIFIED STATE PIPELINE ---
        viewModelScope.launch {
            @OptIn(ExperimentalCoroutinesApi::class)
            val pipeline = combine(
                selectedSystemIdState,
                refreshCountState,
                retrogradeDb.gameDao().selectSystemsOrderedByRecentsFlow().onStart { emit(emptyList()) },
                notificationsPermissionEnabledState,
                indexingInProgress(appContext).onStart { emit(false) },
                microphoneNotification(retrogradeDb).onStart { emit(false) },
                desmumeWarningNotification().onStart { emit(false) }
            ) { params ->
                val sysId = params[0] as String?
                val ref = params[1] as Int
                val systems = params[2] as List<String>
                val notifications = params[3] as Boolean
                val indexing = params[4] as Boolean
                val micro = params[5] as Boolean
                val desmume = params[6] as Boolean
                
                Metadata(sysId, ref, systems, notifications, indexing, micro, desmume)
            }.flatMapLatest { meta ->
                if (meta.sysId == null) {
                    flowOf(buildViewState(emptyList(), meta.systems, null, meta.ref, meta.indexing, meta.notifications, meta.micro, meta.desmume))
                } else {
                    val sysIdLower = meta.sysId.lowercase()
                    val isConcrete = SystemID.entries.any { it.dbname.equals(sysIdLower, ignoreCase = true) }
                    val gamesSource = if (isConcrete) {
                        retrogradeDb.gameDao().selectBySystemOrderedByRecentsFlow(sysIdLower)
                    } else {
                        val metaSys = MetaSystemID.entries.find { it.name.equals(meta.sysId, ignoreCase = true) }
                        if (metaSys != null) {
                            retrogradeDb.gameDao().selectBySystemsOrderedByRecentsFlow(metaSys.systemIDs.map { it.dbname.lowercase() })
                        } else {
                            retrogradeDb.gameDao().selectBySystemOrderedByRecentsFlow(sysIdLower)
                        }
                    }

                    gamesSource.map { games ->
                        buildViewState(games, meta.systems, meta.sysId, meta.ref, meta.indexing, meta.notifications, meta.micro, meta.desmume)
                    }.onStart {
                        emit(buildViewState(emptyList(), meta.systems, meta.sysId, meta.ref, meta.indexing, meta.notifications, meta.micro, meta.desmume))
                    }.catch {
                        emit(buildViewState(emptyList(), meta.systems, meta.sysId, meta.ref, meta.indexing, meta.notifications, meta.micro, meta.desmume))
                    }
                }
            }

            pipeline
                .flowOn(Dispatchers.Main.immediate)
                .collect { uiStates.value = it }
        }
    }

    private data class Metadata(
        val sysId: String?,
        val ref: Int,
        val systems: List<String>,
        val notifications: Boolean,
        val indexing: Boolean,
        val micro: Boolean,
        val desmume: Boolean
    )

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
