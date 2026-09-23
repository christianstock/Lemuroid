package com.swordfish.lemuroid.app.shared.game.skins

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class GbaSkinManager private constructor(private val context: Context) {
    companion object {
        private const val PREFS_NAME = "gba_skin_preferences"
        private const val SELECTED_GBA_SKIN_KEY = "selected_gba_skin"

        @Volatile
        private var instance: GbaSkinManager? = null

        fun getInstance(context: Context): GbaSkinManager {
            return instance ?: synchronized(this) {
                instance ?: GbaSkinManager(context.applicationContext).also { instance = it }
            }
        }
    }

    private val sharedPrefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val selectedSkinFlow = MutableStateFlow(getDefaultSkin())

    /**
     * Get the currently selected GBA skin as a Flow
     */
    fun getSelectedSkinFlow(): Flow<GameBoyAdvanceSkin> {
        return selectedSkinFlow.asStateFlow()
    }

    /**
     * Get the currently selected skin
     */
    fun getSelectedSkin(): GameBoyAdvanceSkin {
        val skinId = sharedPrefs.getString(SELECTED_GBA_SKIN_KEY, GameBoyAdvanceSkin.INDIGO.id)
        return GameBoyAdvanceSkin.getById(skinId!!) ?: GameBoyAdvanceSkin.INDIGO
    }

    /**
     * Set the selected GBA skin
     */
    fun setSelectedSkin(skinId: String) {
        sharedPrefs.edit().putString(SELECTED_GBA_SKIN_KEY, skinId).apply()
        selectedSkinFlow.value = GameBoyAdvanceSkin.getById(skinId) ?: GameBoyAdvanceSkin.INDIGO
    }

    /**
     * Get all available skins
     */
    fun getAllSkins(): List<GameBoyAdvanceSkin> = GameBoyAdvanceSkin.ALL_SKINS

    private fun getDefaultSkin(): GameBoyAdvanceSkin {
        val skinId = sharedPrefs.getString(SELECTED_GBA_SKIN_KEY, GameBoyAdvanceSkin.INDIGO.id)
        return GameBoyAdvanceSkin.getById(skinId!!) ?: GameBoyAdvanceSkin.INDIGO
    }
}
