package com.swordfish.lemuroid.app.shared.game.skins

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class GbSkinManager private constructor(private val context: Context) {
    companion object {
        private const val PREFS_NAME = "gb_skin_preferences"
        private const val SELECTED_GB_SKIN_KEY = "selected_gb_skin"

        @Volatile
        private var instance: GbSkinManager? = null

        fun getInstance(context: Context): GbSkinManager {
            return instance ?: synchronized(this) {
                instance ?: GbSkinManager(context.applicationContext).also { instance = it }
            }
        }
    }

    private val sharedPrefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val selectedSkinFlow = MutableStateFlow(getDefaultSkin())

    /**
     * Get the currently selected GB skin as a Flow
     */
    fun getSelectedSkinFlow(): Flow<GbSkin> {
        return selectedSkinFlow.asStateFlow()
    }

    /**
     * Get the currently selected skin
     */
    fun getSelectedSkin(): GbSkin {
        val skinId = sharedPrefs.getString(SELECTED_GB_SKIN_KEY, GbSkin.GREY.id)
        return GbSkin.getById(skinId!!) ?: GbSkin.GREY
    }

    /**
     * Set the selected GB skin
     */
    fun setSelectedSkin(skinId: String) {
        sharedPrefs.edit().putString(SELECTED_GB_SKIN_KEY, skinId).apply()
        selectedSkinFlow.value = GbSkin.getById(skinId) ?: GbSkin.GREY
    }

    /**
     * Get all available skins
     */
    fun getAllSkins(): List<GbSkin> = GbSkin.ALL_SKINS

    private fun getDefaultSkin(): GbSkin {
        val skinId = sharedPrefs.getString(SELECTED_GB_SKIN_KEY, GbSkin.GREY.id)
        return GbSkin.getById(skinId!!) ?: GbSkin.GREY
    }
}
