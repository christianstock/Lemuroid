package com.swordfish.lemuroid.app.shared.game.skins

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class GbcSkinManager private constructor(private val context: Context) {
    companion object {
        private const val PREFS_NAME = "gbc_skin_preferences"
        private const val SELECTED_GBC_SKIN_KEY = "selected_gbc_skin"

        @Volatile
        private var instance: GbcSkinManager? = null

        fun getInstance(context: Context): GbcSkinManager {
            return instance ?: synchronized(this) {
                instance ?: GbcSkinManager(context.applicationContext).also { instance = it }
            }
        }
    }

    private val sharedPrefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val selectedSkinFlow = MutableStateFlow(getDefaultSkin())

    private fun getPrefs(): SharedPreferences = sharedPrefs

    /**
     * Get the currently selected GBC skin as a Flow
     */
    fun getSelectedSkinFlow(): Flow<GameBoyColorSkin> {
        return selectedSkinFlow.asStateFlow()
    }

    /**
     * Get the currently selected skin
     */
    fun getSelectedSkin(): GameBoyColorSkin {
        val skinId = sharedPrefs.getString(SELECTED_GBC_SKIN_KEY, GameBoyColorSkin.BERRY.id)
        return GameBoyColorSkin.getById(skinId!!) ?: GameBoyColorSkin.BERRY
    }

    /**
     * Set the selected GBC skin
     */
    fun setSelectedSkin(skinId: String) {
        sharedPrefs.edit().putString(SELECTED_GBC_SKIN_KEY, skinId).apply()
        selectedSkinFlow.value = GameBoyColorSkin.getById(skinId) ?: GameBoyColorSkin.BERRY
    }

    /**
     * Get all available skins
     */
    fun getAllSkins(): List<GameBoyColorSkin> = GameBoyColorSkin.ALL_SKINS

    private fun getDefaultSkin(): GameBoyColorSkin {
        val skinId = sharedPrefs.getString(SELECTED_GBC_SKIN_KEY, GameBoyColorSkin.BERRY.id)
        return GameBoyColorSkin.getById(skinId!!) ?: GameBoyColorSkin.BERRY
    }
}


