@file:Suppress("UNUSED", "INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package com.swordfish.lemuroid.app.mobile.feature.gamemenu

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.mobile.feature.gamemenu.coreoptions.GameMenuCoreOptionsScreen
import com.swordfish.lemuroid.app.mobile.feature.gamemenu.coreoptions.GameMenuCoreOptionsViewModel
import com.swordfish.lemuroid.app.mobile.feature.gamemenu.states.GameMenuStatesScreen
import com.swordfish.lemuroid.app.mobile.feature.gamemenu.states.GameMenuStatesViewModel
import com.swordfish.lemuroid.app.mobile.feature.gamemenu.cheats.GameMenuCheatsViewModel
import com.swordfish.lemuroid.app.shared.game.skins.GbSkinManager
import com.swordfish.lemuroid.app.shared.game.skins.GbaSkinManager
import com.swordfish.lemuroid.app.shared.game.skins.GbcSkinManager
import com.swordfish.lemuroid.app.shared.game.skins.ui.GbSkinSelectionScreen
import com.swordfish.lemuroid.app.shared.game.skins.ui.GbaSkinSelectionScreen
import com.swordfish.lemuroid.app.shared.game.skins.ui.GbcSkinSelectionScreen
import com.swordfish.lemuroid.app.shared.cheats.ui.CheatMenuScreen
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.AppTheme
import com.swordfish.lemuroid.app.shared.GameMenuContract
import com.swordfish.lemuroid.app.shared.cheats.CheatManager
import com.swordfish.lemuroid.app.shared.coreoptions.LemuroidCoreOption
import com.swordfish.lemuroid.app.shared.coreoptions.CoreOptionsWrapper
import com.swordfish.lemuroid.app.shared.input.InputDeviceManager
import com.swordfish.lemuroid.common.kotlin.serializable
import com.swordfish.lemuroid.lib.android.RetrogradeComponentActivity
import com.swordfish.lemuroid.lib.library.SystemCoreConfig
import com.swordfish.lemuroid.lib.library.db.entity.Game
import com.swordfish.lemuroid.lib.saves.StatesManager
import com.swordfish.lemuroid.lib.saves.StatesPreviewManager
import com.swordfish.touchinput.radial.sensors.TiltConfiguration
import java.security.InvalidParameterException
import javax.inject.Inject

class GameMenuActivity : RetrogradeComponentActivity() {
    @Inject
    lateinit var inputDeviceManager: InputDeviceManager

    @Inject
    lateinit var statesManager: StatesManager

    @Inject
    lateinit var statesPreviewManager: StatesPreviewManager

    @Inject
    lateinit var cheatManager: CheatManager

    private var cheatsChanged = false

    data class GameMenuRequest(
        val coreOptions: List<LemuroidCoreOption>,
        val advancedCoreOptions: List<LemuroidCoreOption>,
        val game: Game,
        val coreConfig: SystemCoreConfig,
        val audioEnabled: Boolean,
        val fastForwardSupported: Boolean,
        val fastForwardEnabled: Boolean,
        val numDisks: Int,
        val currentDisk: Int,
        val currentTiltConfiguration: TiltConfiguration,
        val allTiltConfigurations: List<TiltConfiguration>,
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge(
            SystemBarStyle.dark(Color.TRANSPARENT),
            SystemBarStyle.dark(Color.TRANSPARENT),
        )

        val extras = intent.extras

        val gameMenuRequest =
            GameMenuRequest(
                coreOptions =
                    intent.serializable<com.swordfish.lemuroid.app.shared.coreoptions.CoreOptionsWrapper>(GameMenuContract.EXTRA_CORE_OPTIONS)
                        ?.options
                        ?: throw InvalidParameterException("Missing EXTRA_CORE_OPTIONS"),
                advancedCoreOptions =
                    intent.serializable<com.swordfish.lemuroid.app.shared.coreoptions.CoreOptionsWrapper>(GameMenuContract.EXTRA_ADVANCED_CORE_OPTIONS)
                        ?.options
                        ?: throw InvalidParameterException("Missing EXTRA_ADVANCED_CORE_OPTIONS"),
                game =
                    intent.serializable<Game>(GameMenuContract.EXTRA_GAME)
                        ?: throw InvalidParameterException("Missing EXTRA_GAME"),
                coreConfig =
                    intent.serializable<SystemCoreConfig>(GameMenuContract.EXTRA_SYSTEM_CORE_CONFIG)
                        ?: throw InvalidParameterException("Missing EXTRA_SYSTEM_CORE_CONFIG"),
                audioEnabled =
                    extras?.getBoolean(GameMenuContract.EXTRA_AUDIO_ENABLED, false) ?: false,
                fastForwardSupported =
                    extras?.getBoolean(GameMenuContract.EXTRA_FAST_FORWARD_SUPPORTED, false) ?: false,
                fastForwardEnabled =
                    extras?.getBoolean(GameMenuContract.EXTRA_FAST_FORWARD, false) ?: false,
                numDisks =
                    extras?.getInt(GameMenuContract.EXTRA_DISKS, 0) ?: 0,
                currentDisk =
                    extras?.getInt(GameMenuContract.EXTRA_CURRENT_DISK, 0) ?: 0,
                currentTiltConfiguration =
                    intent.serializable<TiltConfiguration>(GameMenuContract.EXTRA_CURRENT_TILT_CONFIG)
                        ?: TiltConfiguration.Disabled,
                allTiltConfigurations =
                    intent.serializable<Array<TiltConfiguration>>(GameMenuContract.EXTRA_TILT_ALL_CONFIGS)
                        ?.toList()
                        ?: emptyList(),
            )

        val initialRoute = intent.getStringExtra("INITIAL_ROUTE") ?: GameMenuRoute.HOME.route
        val isDirectAccess = intent.getBooleanExtra("IS_DIRECT_ACCESS", false)

        setContent {
            GameMenuScreen(gameMenuRequest, initialRoute, isDirectAccess)
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun GameMenuScreen(gameMenuRequest: GameMenuRequest, initialRoute: String, isDirectAccess: Boolean) {
        AppTheme {
            val navController = rememberNavController()
            val navBackStackEntry = navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry.value?.destination

            val currentRoute =
                currentDestination?.route
                    ?.let { GameMenuRoute.findByRoute(it) }
                    ?: GameMenuRoute.findByRoute(initialRoute)

            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.displayCutout)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    TopAppBar(
                        title = { Text(stringResource(currentRoute.titleId)) },
                        windowInsets = WindowInsets(0.dp),
                        navigationIcon = {
                            AnimatedContent(targetState = currentRoute.canGoBack() && !isDirectAccess, label = "Back") { canGoBack ->
                                if (canGoBack) {
                                    IconButton(onClick = { navController.popBackStack() }) {
                                        Icon(
                                            Icons.AutoMirrored.Filled.ArrowBack,
                                            stringResource(R.string.back),
                                        )
                                    }
                                } else {
                                    IconButton(onClick = { onResult { } }) {
                                        Icon(
                                            Icons.Filled.Close,
                                            stringResource(R.string.close),
                                        )
                                    }
                                }
                            }
                        },
                    )
                    Divider(modifier = Modifier.fillMaxWidth())
                    NavHost(
                        modifier = Modifier.fillMaxSize(),
                        navController = navController,
                        startDestination = initialRoute,
                        enterTransition = { fadeIn() },
                        exitTransition = { fadeOut() },
                    ) {
                        composable(GameMenuRoute.HOME) {
                            GameMenuHomeScreen(navController, gameMenuRequest, ::onResult)
                        }
                        composable(GameMenuRoute.SAVE) {
                            GameMenuStatesScreen(
                                viewModel = viewModel(
                                    factory =
                                        GameMenuStatesViewModel.Factory(
                                            application,
                                            gameMenuRequest,
                                            statesManager,
                                            false,
                                            statesPreviewManager,
                                        ),
                                ),
                                isSaveRoute = true,
                                onStateClicked = {
                                    onResult { putExtra(GameMenuContract.RESULT_SAVE, it) }
                                },
                                onCancel = { onResult { } }
                            )
                        }
                        composable(GameMenuRoute.LOAD) {
                            GameMenuStatesScreen(
                                viewModel = viewModel(
                                    factory =
                                        GameMenuStatesViewModel.Factory(
                                            application,
                                            gameMenuRequest,
                                            statesManager,
                                            true,
                                            statesPreviewManager,
                                        ),
                                ),
                                isSaveRoute = false,
                                onStateClicked = {
                                    onResult { putExtra(GameMenuContract.RESULT_LOAD, it) }
                                },
                                onCancel = { onResult { } }
                            )
                        }
                        composable(GameMenuRoute.OPTIONS) {
                            GameMenuCoreOptionsScreen(
                                viewModel(
                                    factory = GameMenuCoreOptionsViewModel.Factory(inputDeviceManager),
                                ),
                                gameMenuRequest,
                            )
                        }
                        composable(GameMenuRoute.CHEATS) {
                            val viewModel: GameMenuCheatsViewModel = viewModel(
                                factory = GameMenuCheatsViewModel.Factory(
                                    applicationContext,
                                    gameMenuRequest.game.id,
                                    cheatManager
                                )
                            )
                            CheatMenuScreen(
                                modifier = Modifier.fillMaxSize(),
                                cheatsFlow = viewModel.cheats,
                                searchQueryFlow = viewModel.searchQuery,
                                selectedSourcesFlow = viewModel.selectedSources,
                                isRescanningFlow = viewModel.isRescanning,
                                deletedCheatsFlow = viewModel.deletedCheats,
                                onCheatToggle = { cheat, enabled ->
                                    viewModel.toggleCheat(cheat, enabled)
                                    cheatsChanged = true
                                },
                                onDeleteCheat = { cheat ->
                                    viewModel.deleteCheat(cheat)
                                    cheatsChanged = true
                                },
                                onUndoDeleteCheat = { cheatId ->
                                    viewModel.undoDeleteCheat(cheatId)
                                    cheatsChanged = true
                                },
                                onUpdateCheatOrder = { cheatId, newOrder ->
                                    viewModel.updateCheatDisplayOrder(cheatId, newOrder)
                                    cheatsChanged = true
                                },
                                onImportCheats = { uri ->
                                    viewModel.importCheats(uri)
                                    cheatsChanged = true
                                },
                                onSetSearchQuery = { query ->
                                    viewModel.setSearchQuery(query)
                                },
                                onToggleSourceFilter = { source ->
                                    viewModel.toggleSourceFilter(source)
                                },
                                onClearSourceFilter = {
                                    viewModel.clearSourceFilter()
                                },
                                onRescanCheats = {
                                    viewModel.rescandCheatsForCurrentGame()
                                    cheatsChanged = true
                                },
                                onDisableAllCheats = {
                                    viewModel.disableAllCheats()
                                    cheatsChanged = true
                                },
                                onClose = {
                                    navController.popBackStack()
                                }
                            )
                        }
                        composable(GameMenuRoute.SKINS) {
                            when (gameMenuRequest.game.systemId) {
                                "gb" -> {
                                    val gbSkinManager = remember { GbSkinManager.getInstance(applicationContext) }
                                    GbSkinSelectionScreen(
                                        skinManager = gbSkinManager,
                                        onSkinSelected = { skinId ->
                                            gbSkinManager.setSelectedSkin(skinId)
                                        },
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                "gbc" -> {
                                    val gbcSkinManager = remember { GbcSkinManager.getInstance(applicationContext) }
                                    GbcSkinSelectionScreen(
                                        skinManager = gbcSkinManager,
                                        onSkinSelected = { skinId ->
                                            gbcSkinManager.setSelectedSkin(skinId)
                                        },
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                "gba" -> {
                                    val gbaSkinManager = remember { GbaSkinManager.getInstance(applicationContext) }
                                    GbaSkinSelectionScreen(
                                        skinManager = gbaSkinManager,
                                        onSkinSelected = { skinId ->
                                            gbaSkinManager.setSelectedSkin(skinId)
                                        },
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                else -> {
                                    // Default to GBC for unknown systems
                                    val gbcSkinManager = remember { GbcSkinManager.getInstance(applicationContext) }
                                    GbcSkinSelectionScreen(
                                        skinManager = gbcSkinManager,
                                        onSkinSelected = { skinId ->
                                            gbcSkinManager.setSelectedSkin(skinId)
                                        },
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun onResult(block: Intent.() -> Unit) {
        val resultIntent = Intent()
        resultIntent.block()
        if (cheatsChanged) {
            resultIntent.putExtra(GameMenuContract.RESULT_CHEATS_CHANGED, true)
        }
        setResult(RESULT_OK, resultIntent)
        finish()
    }

    @dagger.Module
    abstract class Module
}
