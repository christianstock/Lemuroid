package com.swordfish.lemuroid.app.mobile.feature.settings.general

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.fredporciuncula.flow.preferences.FlowSharedPreferences
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.shared.cheats.CheatDownloader
import com.swordfish.lemuroid.app.shared.cheats.CheatManager
import com.swordfish.lemuroid.app.shared.cheats.ui.SystemScanProgress
import com.swordfish.lemuroid.app.shared.library.PendingOperationsMonitor
import com.swordfish.lemuroid.app.shared.settings.SettingsInteractor
import com.swordfish.lemuroid.lib.savesync.SaveSyncManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber

class SettingsViewModel(
    context: Context,
    private val settingsInteractor: SettingsInteractor,
    saveSyncManager: SaveSyncManager,
    sharedPreferences: FlowSharedPreferences,
    private val cheatDownloader: CheatDownloader,
    private val cheatManager: CheatManager,
) : ViewModel() {
    class Factory(
        private val context: Context,
        private val settingsInteractor: SettingsInteractor,
        private val saveSyncManager: SaveSyncManager,
        private val sharedPreferences: FlowSharedPreferences,
        private val cheatDownloader: CheatDownloader,
        private val cheatManager: CheatManager,
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(
                context,
                settingsInteractor,
                saveSyncManager,
                sharedPreferences,
                cheatDownloader,
                cheatManager,
            ) as T
        }
    }

    data class State(
        val currentDirectory: String = "",
        val isSaveSyncSupported: Boolean = false,
        val cheatUpdateInProgress: Boolean = false,
        val cheatUpdateProgress: Float = 0f,
        val cheatsFound: Int = -1,
        val systemScanProgress: List<SystemScanProgress> = emptyList(),
        val scanComplete: Boolean = false,
    )

    private val cheatUpdateInProgress = MutableStateFlow(false)
    private val cheatUpdateProgress = MutableStateFlow(0f)
    private val cheatsFound = MutableStateFlow(-1)

    val indexingInProgress = PendingOperationsMonitor(context).anyLibraryOperationInProgress()

    val directoryScanInProgress = PendingOperationsMonitor(context).isDirectoryScanInProgress()

    val uiState =
        combine(
            sharedPreferences.getString(context.getString(com.swordfish.lemuroid.lib.R.string.pref_key_extenral_folder)).asFlow(),
            cheatUpdateInProgress,
            cheatUpdateProgress,
            cheatsFound
        ) { currentDirectory, inProgress, progress, found ->
            State(
                currentDirectory = currentDirectory,
                isSaveSyncSupported = saveSyncManager.isSupported(),
                cheatUpdateInProgress = inProgress,
                cheatUpdateProgress = progress,
                cheatsFound = found,
                systemScanProgress = emptyList(),
                scanComplete = false
            )
        }
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.Lazily, State())

    // Separate flows for system progress
    val systemScanProgress = cheatManager.systemProgress
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val scanComplete = cheatManager.scanComplete
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    fun changeLocalStorageFolder() {
        settingsInteractor.changeLocalStorageFolder()
    }

    fun downloadAndScanCheats() {
        viewModelScope.launch {
            cheatUpdateInProgress.value = true
            cheatUpdateProgress.value = 0f
            val success = cheatDownloader.downloadAndExtractCheats()
            if (success) {
                cheatsFound.value = cheatManager.scanLibraryForCheats(
                    onProgress = { progress ->
                        cheatUpdateProgress.value = progress
                    },
                    onLog = { message ->
                        Timber.i(message)
                    }
                )
            }
            cheatUpdateInProgress.value = false
        }
    }
}
