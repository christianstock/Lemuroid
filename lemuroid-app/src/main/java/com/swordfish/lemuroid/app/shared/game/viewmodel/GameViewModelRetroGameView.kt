package com.swordfish.lemuroid.app.shared.game.viewmodel

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.swordfish.lemuroid.BuildConfig
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.mobile.feature.settings.SettingsManager
import com.swordfish.lemuroid.app.shared.cheats.CheatManager
import com.swordfish.lemuroid.app.shared.game.ShaderChooser
import com.swordfish.lemuroid.app.shared.rumble.RumbleManager
import com.swordfish.lemuroid.app.shared.settings.HDModeQuality
import com.swordfish.lemuroid.common.coroutines.MutableStateProperty
import com.swordfish.lemuroid.common.coroutines.launchOnState
import com.swordfish.lemuroid.common.view.disableTouchEvents
import com.swordfish.lemuroid.lib.core.CoreVariable
import com.swordfish.lemuroid.lib.core.CoreVariablesManager
import com.swordfish.lemuroid.lib.game.GameLoader
import com.swordfish.lemuroid.lib.game.GameLoaderError
import com.swordfish.lemuroid.lib.game.GameLoaderException
import com.swordfish.lemuroid.lib.library.CoreID
import com.swordfish.lemuroid.lib.library.GameSystem
import com.swordfish.lemuroid.lib.library.SystemCoreConfig
import com.swordfish.lemuroid.lib.library.db.entity.Game
import com.swordfish.lemuroid.lib.library.db.entity.GameCheatEntity
import com.swordfish.lemuroid.lib.storage.RomFiles
import com.swordfish.libretrodroid.GLRetroView
import com.swordfish.libretrodroid.GLRetroViewData
import com.swordfish.libretrodroid.ImmersiveMode
import com.swordfish.libretrodroid.Variable
import com.swordfish.libretrodroid.VirtualFile
import com.swordfish.touchinput.radial.sensors.HardwareSensorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.time.Duration.Companion.seconds

@OptIn(FlowPreview::class)
class GameViewModelRetroGameView(
    private val appContext: Context,
    private val system: GameSystem,
    private val systemCoreConfig: SystemCoreConfig,
    private val settingsManager: SettingsManager,
    private val cheatManager: CheatManager,
    private val coreVariablesManager: CoreVariablesManager,
    private val sideEffects: GameViewModelSideEffects,
    private val rumbleManager: RumbleManager,
    private val motionManager: com.swordfish.lemuroid.app.shared.motion.MotionManager,
    private val scope: CoroutineScope,
) : DefaultLifecycleObserver {
    private val hardwareSensorManager = HardwareSensorManager(appContext)

    sealed interface GameState {
        data object Uninitialized : GameState

        data class Loading(val message: String) : GameState

        data class Loaded(
            val gameData: GameLoader.GameData,
            val retroViewData: GLRetroViewData,
        ) : GameState

        data object Ready : GameState
    }

    private val gameState: MutableStateFlow<GameState> = MutableStateFlow(GameState.Uninitialized)

    private val retroGameViewFlow = MutableStateFlow<GLRetroView?>(null)
    var retroGameView: GLRetroView? by MutableStateProperty(retroGameViewFlow)

    var currentViewport: android.graphics.RectF? = null

    private var currentGameId: Int = -1
    private val cheatsFlow = MutableStateFlow<List<GameCheatEntity>>(emptyList())

    fun getGameState(): Flow<GameState> {
        return gameState.debounce(200)
    }

    fun getCheats(): Flow<List<GameCheatEntity>> {
        return cheatsFlow
    }

    fun getSensorDebugInfo(): Flow<String> = motionManager.debugInfo

    suspend fun toggleCheat(cheat: GameCheatEntity, enabled: Boolean) {
        try {
            cheatManager.updateCheatEnabled(cheat.gameId, cheat.cheatIndex, enabled)
            
            val updatedCheats = cheatsFlow.value.map {
                if (it.cheatIndex == cheat.cheatIndex) it.copy(enabled = enabled) else it
            }
            cheatsFlow.value = updatedCheats
            
            applyCheats(updatedCheats)

            if (enabled) {
                detectAndRevertFrozenCheat(cheat, updatedCheats)
            }
        } catch (e: Exception) {
            // Ignored
        }
    }

    private fun detectAndRevertFrozenCheat(cheat: GameCheatEntity, appliedCheats: List<GameCheatEntity>) {
        scope.launch {
            try {
                delay(3.seconds)
                val gameView = retroGameView
                if (gameView == null) {
                    val revertedCheats = appliedCheats.map {
                        if (it.cheatIndex == cheat.cheatIndex) it.copy(enabled = false) else it
                    }
                    cheatManager.updateCheatEnabled(cheat.gameId, cheat.cheatIndex, false)
                    cheatsFlow.value = revertedCheats
                    applyCheats(revertedCheats)
                    
                    val message = appContext.getString(
                        R.string.cheat_frozen_warning,
                        cheat.description
                    )
                    sideEffects.showToast(message)
                }
            } catch (e: Exception) {
                // Ignored
            }
        }
    }

    private fun applyCheats(cheats: List<GameCheatEntity>) {
        retroGameView?.queueEvent {
            com.swordfish.libretrodroid.LibretroDroid.resetCheat()
            cheats.forEach { cheat ->
                if (cheat.enabled) {
                    com.swordfish.libretrodroid.LibretroDroid.setCheat(cheat.cheatIndex, true, cheat.code)
                }
            }
        }
    }

    suspend fun initialize(
        applicationContext: Context,
        game: Game,
        systemCoreConfig: SystemCoreConfig,
        gameLoader: GameLoader,
        requestLoadSave: Boolean,
    ) {
        val currentState = gameState.value
        if (currentState != GameState.Uninitialized) return

        val autoSaveEnabled = settingsManager.autoSave()
        val filter = settingsManager.screenFilter()
        val hdMode = settingsManager.hdMode()
        val hdModeQuality = settingsManager.hdModeQuality()
        val lowLatencyAudio = settingsManager.lowLatencyAudio()
        val enableRumble = true // Force enabled
        val directLoad = settingsManager.allowDirectGameLoad()
        val enableImmersiveMode = settingsManager.enableImmersiveMode()

        val hasMicrophonePermission =
            ContextCompat.checkSelfPermission(
                applicationContext,
                android.Manifest.permission.RECORD_AUDIO,
            ) == PackageManager.PERMISSION_GRANTED

        val enableMicrophone = systemCoreConfig.supportsMicrophone && hasMicrophonePermission

        val loadingStatesFlow =
            gameLoader.load(
                applicationContext,
                game,
                requestLoadSave && autoSaveEnabled,
                systemCoreConfig,
                directLoad,
            )

        loadingStatesFlow
            .flowOn(Dispatchers.IO)
            .catch {
                val message =
                    if (it is GameLoaderException) {
                        getErrorMessage(it.error)
                    } else {
                        ""
                    }
                sideEffects.requestFailureFinish(message)
            }
            .debounce(200)
            .collect { loadingState ->
                gameState.value =
                    if (loadingState is GameLoader.LoadingState.Ready) {
                        val retroViewData =
                            buildRetroViewData(
                                applicationContext,
                                systemCoreConfig,
                                loadingState.gameData,
                                hdMode,
                                hdModeQuality,
                                filter,
                                lowLatencyAudio,
                                enableMicrophone,
                                enableImmersiveMode,
                            )
                        GameState.Loaded(
                            gameData = loadingState.gameData,
                            retroViewData = retroViewData,
                        )
                    } else {
                        GameState.Loading(getLoadingMessage(loadingState))
                    }
            }
    }

    fun createRetroViewSafe(
        context: Context,
        lifecycle: LifecycleOwner,
    ): Pair<GameLoader.GameData, GLRetroView>? {
        val currentState = gameState.value
        if (currentState !is GameState.Loaded) {
            // Log as error instead of throwing to prevent crash
            return null
        }

        val result =
            GLRetroView(context, currentState.retroViewData)
                .apply {
                    isFocusable = false
                    isFocusableInTouchMode = false
                }

        if (!system.hasTouchScreen) {
            result.disableTouchEvents()
        }

        lifecycle.lifecycle.addObserver(result)

        if (BuildConfig.DEBUG) {
            runCatching {
                printRetroVariables(result)
            }
        }

        retroGameViewFlow.value = result
        gameState.value = GameState.Ready

        return currentState.gameData to result
    }

    fun resetToUninitialized() {
        gameState.value = GameState.Uninitialized
        retroGameViewFlow.value = null
    }

    suspend fun retroGameViewFlow() =
        retroGameViewFlow
            .filterNotNull()
            .first()

    suspend fun waitRetroGameViewInitialized() {
        retroGameViewFlow()
    }

    suspend inline fun <reified T> waitGLEvent() {
        val retroView = retroGameViewFlow()
        retroView.getGLRetroEvents()
            .filterIsInstance<T>()
            .first()
    }

    private fun buildRetroViewData(
        appContext: Context,
        systemCoreConfig: SystemCoreConfig,
        gameData: GameLoader.GameData,
        hdMode: Boolean,
        hdModeQuality: HDModeQuality,
        screenFilter: String,
        lowLatencyAudio: Boolean,
        requestMicrophone: Boolean,
        enableImmersiveMode: Boolean,
    ): GLRetroViewData {
        return GLRetroViewData(appContext).apply {
            coreFilePath = gameData.coreLibrary

            when (val gameFiles = gameData.gameFiles) {
                is RomFiles.Standard -> {
                    gameFilePath = gameFiles.files.first().absolutePath
                }

                is RomFiles.Virtual -> {
                    gameVirtualFiles = gameFiles.files.map { VirtualFile(it.filePath, it.fd) }
                }
            }

            systemDirectory = gameData.systemDirectory.absolutePath
            savesDirectory = gameData.savesDirectory.absolutePath
            variables = gameData.coreVariables.map { Variable(it.key, it.value) }.toTypedArray()
            saveRAMState = gameData.saveRAMData
            shader =
                ShaderChooser.getShaderForSystem(
                    appContext,
                    hdMode,
                    hdModeQuality,
                    screenFilter,
                    GameSystem.findById(gameData.game.systemId),
                )
            preferLowLatencyAudio = lowLatencyAudio
            rumbleEventsEnabled = true // Force enabled
            skipDuplicateFrames = systemCoreConfig.skipDuplicateFrames
            enableMicrophone = requestMicrophone
            immersiveMode = buildImmersiveModeConfiguration(enableImmersiveMode)
        }
    }

    private fun buildImmersiveModeConfiguration(enableImmersiveMode: Boolean): ImmersiveMode? {
        return if (enableImmersiveMode) {
            ImmersiveMode(blendFactor = 0.05f)
        } else {
            null
        }
    }

    private fun getLoadingMessage(loadingState: GameLoader.LoadingState): String {
        return when (loadingState) {
            is GameLoader.LoadingState.LoadingCore -> {
                appContext.getString(com.swordfish.lemuroid.ext.R.string.game_loading_download_core)
            }

            is GameLoader.LoadingState.LoadingGame -> {
                appContext.getString(R.string.game_loading_preparing_game)
            }

            else -> ""
        }
    }

    private fun printRetroVariables(retroGameView: GLRetroView) {
        scope.launch {
            // Some cores do not immediately call SET_VARIABLES so we might need to wait a little bit
            delay(1.seconds)
            retroGameView.getVariables().forEach {
                Timber.i("Libretro variable: $it")
            }
        }
    }

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)

        owner.launchOnState(Lifecycle.State.STARTED) {
            initializeRetroGameViewErrorsFlow()
        }

        owner.launchOnState(Lifecycle.State.RESUMED) {
            initializeCoreVariablesFlow()
        }

        owner.launchOnState(Lifecycle.State.RESUMED) {
            initializeRumbleFlow()
        }

        owner.launchOnState(Lifecycle.State.RESUMED) {
            initializeMotionFlow()
        }
    }

    private suspend fun initializeMotionFlow() {
        val retroView = retroGameViewFlow()
        val enabledSensors: Flow<Set<Int>> = try {
            val method = retroView.javaClass.getMethod("getEnabledSensors")
            @Suppress("UNCHECKED_CAST")
            method.invoke(retroView) as Flow<Set<Int>>
        } catch (e: Exception) {
            emptyFlow()
        }
        motionManager.collectAndProcessMotionEvents(systemCoreConfig, retroView, enabledSensors) { missingType ->
            val sensorName = when (missingType) {
                Sensor.TYPE_LIGHT -> "Light Sensor"
                Sensor.TYPE_ACCELEROMETER -> "Accelerometer"
                Sensor.TYPE_GYROSCOPE -> "Gyroscope"
                else -> "Unknown"
            }
            sideEffects.showToast("Hardware $sensorName missing. Using manual settings.")
        }
    }

    private suspend fun initializeCoreVariablesFlow() {
        try {
            waitRetroGameViewInitialized()
            val options = coreVariablesManager.getOptionsForCore(system.id, systemCoreConfig)
            updateCoreVariables(options)
        } catch (e: Exception) {
            // Ignored
        }
    }

    private suspend fun initializeRumbleFlow() {
        val retroGameView = retroGameViewFlow()
        val rumbleEvents = retroGameView.getRumbleEvents()
        rumbleManager.collectAndProcessRumbleEvents(systemCoreConfig, rumbleEvents)
    }

    suspend fun initializeCheats(gameEntity: Game) {
        try {
            currentGameId = gameEntity.id
            waitRetroGameViewInitialized()
            val cheats = cheatManager.getAllCheats(gameEntity.id)
            cheatsFlow.value = cheats
            applyCheats(cheats)
        } catch (e: Exception) {
            // Ignored
        }
    }

    suspend fun importCheats(uri: Uri) {
        try {
            cheatManager.importCheats(appContext, currentGameId, uri)
            val updatedCheats = cheatManager.getAllCheats(currentGameId)
            cheatsFlow.value = updatedCheats
        } catch (e: Exception) {
            // Ignored
        }
    }

    private suspend fun initializeRetroGameViewErrorsFlow() {
        retroGameViewFlow().getGLRetroErrors()
            .catch { /* Silent */ }
            .collect { handleRetroViewError(it) }
    }

    private fun updateCoreVariables(options: List<CoreVariable>) {
        val updatedVariables =
            options.map { Variable(it.key, it.value) }
                .toTypedArray()

        retroGameView?.updateVariables(*updatedVariables)
    }

    private fun handleRetroViewError(errorCode: Int) {
        val gameLoaderError =
            when (errorCode) {
                GLRetroView.ERROR_GL_NOT_COMPATIBLE -> GameLoaderError.GLIncompatible
                GLRetroView.ERROR_LOAD_GAME -> GameLoaderError.LoadGame
                GLRetroView.ERROR_LOAD_LIBRARY -> GameLoaderError.LoadCore
                GLRetroView.ERROR_SERIALIZATION -> GameLoaderError.Saves
                else -> GameLoaderError.Generic
            }

        sideEffects.requestFailureFinish(getErrorMessage(gameLoaderError))
    }

    private fun getErrorMessage(gameError: GameLoaderError): String {
        val message =
            when (gameError) {
                is GameLoaderError.GLIncompatible -> {
                    appContext.getString(R.string.game_loader_error_gl_incompatible)
                }
                is GameLoaderError.Generic -> {
                    appContext.getString(R.string.game_loader_error_generic)
                }
                is GameLoaderError.LoadCore -> {
                    appContext.getString(com.swordfish.lemuroid.ext.R.string.game_loader_error_load_core)
                }
                is GameLoaderError.LoadGame -> {
                    appContext.getString(R.string.game_loader_error_load_game)
                }
                is GameLoaderError.Saves -> {
                    appContext.getString(R.string.game_loader_error_save)
                }
                is GameLoaderError.UnsupportedArchitecture -> {
                    appContext.getString(R.string.game_loader_error_unsupported_architecture)
                }
                is GameLoaderError.MissingBiosFiles -> {
                    appContext.getString(R.string.game_loader_error_missing_bios, gameError.missingFiles)
                }
            }

        return message
    }
}
