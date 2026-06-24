package com.swordfish.lemuroid.app.shared.game

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.view.KeyEvent
import android.view.MotionEvent
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.ui.unit.Density
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.swordfish.lemuroid.app.mobile.feature.game.GameService
import com.swordfish.lemuroid.app.mobile.feature.settings.SettingsManager
import com.swordfish.lemuroid.app.shared.cheats.CheatManager
import com.swordfish.lemuroid.app.shared.game.viewmodel.GameViewModelInput
import com.swordfish.lemuroid.app.shared.game.viewmodel.GameViewModelRetroGameView
import com.swordfish.lemuroid.app.shared.game.viewmodel.GameViewModelSaves
import com.swordfish.lemuroid.app.shared.game.viewmodel.GameViewModelSideEffects
import com.swordfish.lemuroid.app.shared.game.viewmodel.GameViewModelTilt
import com.swordfish.lemuroid.app.shared.game.viewmodel.GameViewModelTouchControls
import com.swordfish.lemuroid.app.shared.input.InputDeviceManager
import com.swordfish.lemuroid.app.shared.rumble.RumbleManager
import com.swordfish.lemuroid.app.shared.settings.ControllerConfigsManager
import com.swordfish.lemuroid.app.shared.settings.HapticFeedbackMode
import com.swordfish.lemuroid.common.coroutines.launchOnState
import com.swordfish.lemuroid.common.graphics.takeScreenshot
import com.swordfish.lemuroid.common.longAnimationDuration
import com.swordfish.lemuroid.lib.controller.ControllerConfig
import com.swordfish.lemuroid.lib.core.CoreVariablesManager
import com.swordfish.lemuroid.lib.game.GameLoader
import com.swordfish.lemuroid.lib.library.GameSystem
import com.swordfish.lemuroid.lib.library.SystemCoreConfig
import com.swordfish.lemuroid.lib.library.db.entity.Game
import com.swordfish.lemuroid.lib.library.db.entity.GameCheatEntity
import com.swordfish.lemuroid.lib.saves.SavesManager
import com.swordfish.lemuroid.lib.saves.StatesManager
import com.swordfish.lemuroid.lib.saves.StatesPreviewManager
import com.swordfish.libretrodroid.GLRetroView
import com.swordfish.touchinput.radial.sensors.TiltConfiguration
import com.swordfish.touchinput.radial.settings.TouchControllerSettingsManager
import gg.padkit.inputevents.InputEvent
import gg.padkit.inputstate.InputState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class BaseGameScreenViewModel(
    private val appContext: Context,
    val game: Game,
    settingsManager: SettingsManager,
    inputDeviceManager: InputDeviceManager,
    controllerConfigsManager: ControllerConfigsManager,
    system: GameSystem,
    private val systemCoreConfig: SystemCoreConfig,
    sharedPreferences: SharedPreferences,
    savesManager: SavesManager,
    statesManager: StatesManager,
    private val statesPreviewManager: StatesPreviewManager,
    coreVariablesManager: CoreVariablesManager,
    rumbleManager: RumbleManager,
    private val cheatManager: CheatManager,
) : ViewModel(), DefaultLifecycleObserver {
    class Factory(
        private val appContext: Context,
        private val game: Game,
        private val settingsManager: SettingsManager,
        private val inputDeviceManager: InputDeviceManager,
        private val controllerConfigsManager: ControllerConfigsManager,
        private val system: GameSystem,
        private val systemCoreConfig: SystemCoreConfig,
        private val sharedPreferences: SharedPreferences,
        private val savesManager: SavesManager,
        private val statesManager: StatesManager,
        private val statesPreviewManager: StatesPreviewManager,
        private val coreVariablesManager: CoreVariablesManager,
        private val rumbleManager: RumbleManager,
        private val cheatManager: CheatManager,
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return BaseGameScreenViewModel(
                appContext,
                game,
                settingsManager,
                inputDeviceManager,
                controllerConfigsManager,
                system,
                systemCoreConfig,
                sharedPreferences,
                savesManager,
                statesManager,
                statesPreviewManager,
                coreVariablesManager,
                rumbleManager,
                cheatManager,
            ) as T
        }
    }

    private val sideEffects = GameViewModelSideEffects(viewModelScope)
    val retroGameView =
        GameViewModelRetroGameView(
            appContext,
            system,
            systemCoreConfig,
            settingsManager,
            cheatManager,
            coreVariablesManager,
            sideEffects,
            rumbleManager,
            viewModelScope,
        )
    private val tilt = GameViewModelTilt(appContext, settingsManager)
    private val inputs =
        GameViewModelInput(
            appContext,
            system,
            systemCoreConfig,
            inputDeviceManager,
            controllerConfigsManager,
            retroGameView,
            tilt,
            sideEffects,
            viewModelScope,
        )
    private val touchControls =
        GameViewModelTouchControls(
            settingsManager,
            TouchControllerSettingsManager(sharedPreferences),
            retroGameView,
            inputs,
            tilt,
            sideEffects,
            viewModelScope,
        )
    private val saves =
        GameViewModelSaves(
            appContext,
            system,
            game,
            systemCoreConfig,
            retroGameView,
            settingsManager,
            savesManager,
            statesManager,
            statesPreviewManager,
            sideEffects,
        )
    private val rewindManager = RewindManager(appContext)

    val loadingState = MutableStateFlow(false)
    val cheatMenuVisible = MutableStateFlow(false)
    private val rewindAvailable = MutableStateFlow(false)
    private val rewindProgress = MutableStateFlow(0f)
    private val isPlaying = MutableStateFlow(true)
    private val rewindBufferStats = MutableStateFlow<RewindBufferStats?>(null)

    private inline fun withLoading(block: () -> Unit) {
        loadingState.value = true
        block()
        loadingState.value = false
    }

    fun getGameState(): Flow<GameViewModelRetroGameView.GameState> {
        return retroGameView.getGameState()
    }

    fun getSideEffects(): Flow<GameViewModelSideEffects.UiEffect> {
        return sideEffects.getUiEffects()
    }

    fun getTiltConfiguration(): Flow<TiltConfiguration> {
        return tilt.getTiltConfiguration()
    }

    fun getSimulatedTiltEvents(): Flow<InputState> {
        return tilt.getSimulatedTiltEvents()
    }

    fun getTouchControlsSettings(
        density: Density,
        insets: WindowInsets,
    ): Flow<TouchControllerSettingsManager.Settings?> {
        return touchControls.getTouchControlsSettings(density, insets)
    }

    fun getTouchHapticFeedbackMode(): Flow<HapticFeedbackMode> {
        return touchControls.getTouchHapticFeedbackMode()
    }

    fun createRetroView(
        context: Context,
        lifecycle: LifecycleOwner,
    ): GLRetroView {
        val (gameData, result) = retroGameView.createRetroView(context, lifecycle)
        viewModelScope.launch {
            gameData.quickSaveData?.let {
                saves.restoreAutoSaveAsync(it)
            }
        }
        return result
    }

    suspend fun loadGame(
        applicationContext: Context,
        game: Game,
        systemCoreConfig: SystemCoreConfig,
        gameLoader: GameLoader,
        requestLoadSave: Boolean,
    ) {
        Timber.i("Calling load game: $game")
        retroGameView.initialize(applicationContext, game, systemCoreConfig, gameLoader, requestLoadSave)
        // Load enabled cheats after game is initialized
        retroGameView.initializeCheats(game)
    }

    fun showSaveMenu() {
        sideEffects.showSaveMenu(tilt, inputs)
    }

    fun showLoadMenu() {
        sideEffects.showLoadMenu(tilt, inputs)
    }

    fun showEditControls(show: Boolean) {
        touchControls.showEditControls(show)
    }

    fun isEditControlShown(): Flow<Boolean> {
        return touchControls.isEditControlsShown()
    }

    fun updateTouchControllerSettings(touchControllerSettings: TouchControllerSettingsManager.Settings) {
        touchControls.updateTouchControllerSettings(touchControllerSettings)
    }

    fun resetTouchControls() {
        touchControls.resetTouchControls()
    }

    fun onScreenOrientationChanged(orientation: TouchControllerSettingsManager.Orientation) {
        touchControls.updateScreenOrientation(orientation)
    }

    fun isTouchControllerVisible(): Flow<Boolean> {
        return touchControls.isTouchControllerVisible()
    }

    fun getTouchControllerConfig(): Flow<ControllerConfig> {
        return touchControls.getTouchControllerConfig()
    }

    fun changeTiltConfiguration(tiltConfig: TiltConfiguration) {
        tilt.changeTiltConfiguration(tiltConfig)
    }

    fun isMenuPressed(): Flow<Boolean> {
        return touchControls.isMenuPressed()
    }

    suspend fun saveSlot(index: Int) {
        if (loadingState.value) return
        withLoading {
            saves.saveSlot(index)
        }
    }

    suspend fun loadSlot(index: Int) {
        if (loadingState.value) return
        withLoading {
            saves.loadSlot(index)
        }
    }

    fun saveQuickSave() {
        Timber.d("Saving quick save")
        if (loadingState.value) return
        withLoading {
            saves.saveQuickSave()
            // Capture screenshot for quick save preview asynchronously
            viewModelScope.launch {
                try {
                    val previewSizePixels = 192  // 96dp * 2
                    val preview = retroGameView.retroGameView?.takeScreenshot(previewSizePixels, 3)
                    if (preview != null) {
                        statesPreviewManager.setQuickSavePreview(game, preview, systemCoreConfig.coreID)
                        Timber.i("✓ Quick save screenshot saved: ${preview.width}x${preview.height}")
                    } else {
                        Timber.w("Failed to capture quick save screenshot")
                    }
                } catch (e: Exception) {
                    Timber.e(e, "❌ Error capturing quick save screenshot")
                }
            }
        }
    }

    fun loadQuickSave() {
        Timber.d("Loading quick save")
        if (loadingState.value) return
        withLoading {
            saves.loadQuickSave()
        }
    }

    fun toggleFastForward() {
        Timber.d("Loading quick save")
        retroGameView.retroGameView?.apply {
            frameSpeed = if (frameSpeed == 1) 2 else 1
        }
    }

    suspend fun reset() =
        withLoading {
            try {
                delay(appContext.longAnimationDuration().toLong())
                retroGameView.retroGameViewFlow().reset()
            } catch (e: Throwable) {
                Timber.e(e, "Error in reset")
            }
        }

    fun requestFinish() {
        if (loadingState.value) return
        viewModelScope.launch {
            withLoading {
                val snapshot = saves.captureSaveSnapshot(true) ?: return@launch
                saves.writeSaveSnapshot(snapshot)
                sideEffects.requestSuccessfulFinish()
            }
        }
    }

    fun requestBackgroundSave() {
        if (loadingState.value) return
        GameService.schedule {
            val snapshot = saves.captureSaveSnapshot(false)
            saves.writeSaveSnapshot(snapshot)
        }
    }

    fun handleVirtualInputEvent(events: List<InputEvent>) {
        touchControls.handleVirtualInputEvent(events)
    }

    fun toggleCheatMenu() {
        cheatMenuVisible.value = !cheatMenuVisible.value
    }

    fun closeCheatMenu() {
        cheatMenuVisible.value = false
    }

    suspend fun toggleCheat(cheat: GameCheatEntity, enabled: Boolean) {
        retroGameView.toggleCheat(cheat, enabled)
    }

    fun importCheats(uri: Uri) {
        viewModelScope.launch {
            retroGameView.importCheats(uri)
        }
    }

    fun getCheats(): Flow<List<GameCheatEntity>> {
        return retroGameView.getCheats()
    }

    // Rewind System Methods
    fun getRewindAvailable(): Flow<Boolean> = rewindAvailable

    fun getRewindProgress(): Flow<Float> = rewindProgress

    fun getIsPlaying(): Flow<Boolean> = isPlaying

    fun getRewindBufferStats(): StateFlow<RewindBufferStats?> = rewindBufferStats

    suspend fun captureRewindState() {
        val retroGameView = retroGameView.retroGameView ?: return
        try {
            val stateData = retroGameView.serializeState()
            rewindManager.captureState(stateData)
            rewindAvailable.value = rewindManager.isRewindAvailable()
            rewindBufferStats.value = rewindManager.getBufferStats()
        } catch (e: Throwable) {
            Timber.e(e, "Error capturing rewind state")
        }
    }

    suspend fun startRewind() {
        if (rewindManager.isRewindActive()) return
        isPlaying.value = false
        rewindManager.rewindBackward() // Set active
        viewModelScope.launch {
            var stepCount = 0
            while (rewindManager.isRewindActive()) {
                rewindManager.rewindBackward()
                rewindProgress.value = rewindManager.getRewindProgress()
                applyRewindState()
                
                // 3:1 (33ms delay) for first 15 seconds (150 steps at 0.1s/step)
                // 5:1 (20ms delay) thereafter
                val delayTime = if (stepCount < 150) 33L else 20L
                delay(delayTime)
                stepCount++
            }
        }
    }

    suspend fun continueRewind() {
        // Handled by the loop in startRewind
    }

    suspend fun stopRewind() {
        isPlaying.value = true
        rewindManager.stopRewind()
        rewindProgress.value = 0f
    }

    private suspend fun applyRewindState() {
        val retroGameView = retroGameView.retroGameView ?: return
        val rewindState = rewindManager.getLatestRewindState() ?: return
        try {
            retroGameView.unserializeState(rewindState.stateData)
        } catch (e: Throwable) {
            Timber.e(e, "Error applying rewind state")
        }
    }

    fun togglePause() {
        isPlaying.value = !isPlaying.value
        retroGameView.retroGameView?.apply {
            frameSpeed = if (isPlaying.value) 1 else 0
        }
    }

    override fun onCleared() {
        super.onCleared()
        rewindManager.stopRewind()
        // No explicit clear call needed if we trust JVM GC, 
        // but we can add one to RewindManager if we use DirectByteBuffers later.
    }

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)

        owner.lifecycle.addObserver(tilt)
        owner.lifecycle.addObserver(inputs)
        owner.lifecycle.addObserver(retroGameView)
        owner.lifecycle.addObserver(touchControls)

        owner.launchOnState(androidx.lifecycle.Lifecycle.State.RESUMED) {
            retroGameView.initializeCheats(game)
        }

        // Periodic rewind capture loop
        owner.launchOnState(androidx.lifecycle.Lifecycle.State.RESUMED) {
            while (true) {
                if (isPlaying.value) {
                    captureRewindState()
                }
                delay(100) // Capture every 0.1 second
            }
        }
    }

    fun sendKeyEvent(
        keyCode: Int,
        event: KeyEvent,
    ): Boolean {
        return inputs.sendKeyEvent(keyCode, event)
    }

    fun sendMotionEvent(event: MotionEvent): Boolean {
        return inputs.sendMotionEvent(event)
    }
}
