package com.swordfish.lemuroid.app.mobile.feature.gamemenu.states

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.mobile.feature.gamemenu.GameMenuActivity
import com.swordfish.lemuroid.app.shared.gamemenu.GameMenuHelper
import com.swordfish.lemuroid.app.shared.game.viewmodel.GameViewModelSaves
import com.swordfish.lemuroid.lib.saves.StatesManager
import com.swordfish.lemuroid.lib.saves.StatesPreviewManager
import kotlinx.coroutines.flow.flow
import java.text.SimpleDateFormat
import java.util.Date

class GameMenuStatesViewModel(
    private val application: Application,
    private val gameMenuRequest: GameMenuActivity.GameMenuRequest,
    private val statesManager: StatesManager,
    private val disableMissingEntries: Boolean,
    private val statesPreviewManager: StatesPreviewManager,
) : ViewModel() {
    class Factory(
        private val application: Application,
        private val gameMenuRequest: GameMenuActivity.GameMenuRequest,
        private val statesManager: StatesManager,
        private val disableMissingEntries: Boolean,
        private val statesPreviewManager: StatesPreviewManager,
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return GameMenuStatesViewModel(
                application,
                gameMenuRequest,
                statesManager,
                disableMissingEntries,
                statesPreviewManager,
            ) as T
        }
    }

    data class StateEntry(
        val title: String,
        val description: String,
        val enabled: Boolean,
        val preview: Bitmap?,
        val isQuickSave: Boolean = false,
    )

    data class State(val entries: List<StateEntry> = emptyList()) {
        val hasQuickSave: Boolean get() = entries.any { it.isQuickSave }
    }

    val uiStates =
        flow {
            val slotsInfo = statesManager.getSavedSlotsInfo(gameMenuRequest.game, gameMenuRequest.coreConfig.coreID)

            val entries = mutableListOf<StateEntry>()

            // Add quick save entry at the top if it exists
            val quickSaveTimestamp = GameViewModelSaves.getQuickSaveTimestampForGame(application, gameMenuRequest.game.id.toLong())
            if (quickSaveTimestamp > 0) {
                try {
                    val preview = statesPreviewManager.getQuickSavePreview(
                        gameMenuRequest.game,
                        gameMenuRequest.coreConfig.coreID,
                        (96 * 2)  // size in pixels
                    )
                    val formatter = SimpleDateFormat.getDateTimeInstance()
                    val description = formatter.format(Date(quickSaveTimestamp))
                    entries.add(
                        StateEntry(
                            title = application.getString(R.string.game_menu_quick_save),
                            description = description,
                            enabled = true,
                            preview = preview,
                            isQuickSave = true
                        )
                    )
                } catch (e: Exception) {
                    // Quick save might not have a preview, show without image
                    val formatter = SimpleDateFormat.getDateTimeInstance()
                    val description = formatter.format(Date(quickSaveTimestamp))
                    entries.add(
                        StateEntry(
                            title = application.getString(R.string.game_menu_quick_save),
                            description = description,
                            enabled = true,
                            preview = null,
                            isQuickSave = true
                        )
                    )
                }
            }

            // Add regular slot saves
            slotsInfo.forEachIndexed { index, slotInfo ->
                val title =
                    application.applicationContext.getString(
                        R.string.game_menu_state,
                        (index + 1).toString(),
                    )
                val description = GameMenuHelper.getSaveStateDescription(slotInfo)
                val isEnabled = !disableMissingEntries || slotInfo.exists
                val preview =
                    GameMenuHelper.getSaveStateBitmap(
                        application.applicationContext,
                        statesPreviewManager,
                        slotInfo,
                        gameMenuRequest.game,
                        gameMenuRequest.coreConfig.coreID,
                        index,
                    )

                entries.add(StateEntry(title, description, isEnabled, preview))
            }

            emit(State(entries))
        }
}
