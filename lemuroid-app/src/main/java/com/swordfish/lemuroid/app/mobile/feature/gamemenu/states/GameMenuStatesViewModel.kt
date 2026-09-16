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
        val customName: String? = null,
        val slotIndex: Int = -1,
    )

    data class State(val entries: List<StateEntry> = emptyList()) {
        val hasQuickSave: Boolean get() = entries.any { it.isQuickSave }
    }

    fun saveSlotName(slotIndex: Int, name: String) {
        val prefs = application.getSharedPreferences("slot_names", android.content.Context.MODE_PRIVATE)
        prefs.edit().putString("${gameMenuRequest.game.id}_slot_$slotIndex", name).apply()
    }

    val uiStates =
        flow {
            val slotsInfo = statesManager.getSavedSlotsInfo(gameMenuRequest.game, gameMenuRequest.coreConfig.coreID)

            val entries = mutableListOf<StateEntry>()
            val slotNamesPrefs = application.getSharedPreferences("slot_names", android.content.Context.MODE_PRIVATE)

            // Add quick save entry at the top if it exists
            val quickSaveTimestamp = GameViewModelSaves.getQuickSaveTimestampForGame(application, gameMenuRequest.game.id.toLong())
            if (quickSaveTimestamp > 0) {
                try {
                    val preview = statesPreviewManager.getQuickSavePreview(
                        gameMenuRequest.game,
                        gameMenuRequest.coreConfig.coreID,
                        (StatesPreviewManager.PREVIEW_SIZE_DP * 2).toInt() // Ensure enough resolution for the grid
                    )
                    val formatter = SimpleDateFormat.getDateTimeInstance()
                    val description = formatter.format(Date(quickSaveTimestamp))
                    entries.add(
                        StateEntry(
                            title = application.getString(R.string.game_menu_quick_save),
                            description = description,
                            enabled = true,
                            preview = preview,
                            isQuickSave = true,
                            slotIndex = -1
                        )
                    )
                } catch (e: Exception) {
                    val formatter = SimpleDateFormat.getDateTimeInstance()
                    val description = formatter.format(Date(quickSaveTimestamp))
                    entries.add(
                        StateEntry(
                            title = application.getString(R.string.game_menu_quick_save),
                            description = description,
                            enabled = true,
                            preview = null,
                            isQuickSave = true,
                            slotIndex = -1
                        )
                    )
                }
            }

            // Add regular slot saves
            slotsInfo.forEachIndexed { index, slotInfo ->
                val title = "" // Hiding "State X" label as requested
                val description = GameMenuHelper.getSaveStateDescription(slotInfo)
                val isEnabled = !disableMissingEntries || slotInfo.exists
                
                val previewSize = (StatesPreviewManager.PREVIEW_SIZE_DP * 2).toInt()
                val preview = if (slotInfo.exists) {
                    statesPreviewManager.getPreviewForSlot(
                        gameMenuRequest.game,
                        gameMenuRequest.coreConfig.coreID,
                        index,
                        previewSize
                    )
                } else null

                val customName = slotNamesPrefs.getString("${gameMenuRequest.game.id}_slot_$index", null)

                entries.add(StateEntry(title, description, isEnabled, preview, isQuickSave = false, customName = customName, slotIndex = index))
            }

            emit(State(entries))
        }
}
