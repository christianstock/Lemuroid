package com.swordfish.lemuroid.app.shared.game.viewmodel

import com.swordfish.lemuroid.lib.library.GameSystem
import com.swordfish.lemuroid.lib.library.SystemCoreConfig
import com.swordfish.lemuroid.lib.library.db.entity.Game
import com.swordfish.lemuroid.lib.saves.SavesManager
import com.swordfish.lemuroid.lib.saves.StatesManager
import com.swordfish.lemuroid.lib.saves.StatesPreviewManager
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNull
import org.junit.Test

class GameViewModelSavesTest {
    private val retroGameView: GameViewModelRetroGameView = mockk()
    
    @Test
    fun testCaptureSaveSnapshot_NullWhenViewMissing() = runBlocking {
        // Mock retroGameView.retroGameView to be null
        io.mockk.every { retroGameView.retroGameView } returns null
        
        val saves = GameViewModelSaves(
            appContext = mockk(),
            system = mockk(),
            game = mockk(),
            systemCoreConfig = mockk(),
            retroGameView = retroGameView,
            settingsManager = mockk(),
            savesManager = mockk(),
            statesManager = mockk(),
            statesPreviewManager = mockk(),
            sideEffects = mockk()
        )
        
        val result = saves.captureSaveSnapshot(true)
        assertNull(result)
    }
}
