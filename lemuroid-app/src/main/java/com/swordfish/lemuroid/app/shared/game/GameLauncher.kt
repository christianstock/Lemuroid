package com.swordfish.lemuroid.app.shared.game

import android.app.Activity
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.shared.main.GameLaunchTaskHandler
import com.swordfish.lemuroid.common.displayToast
import com.swordfish.lemuroid.lib.core.CoresSelection
import com.swordfish.lemuroid.lib.library.GameSystem
import com.swordfish.lemuroid.lib.library.db.entity.Game
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GameLauncher(
    private val coresSelection: CoresSelection,
    private val gameLaunchTaskHandler: GameLaunchTaskHandler,
) {
    @OptIn(DelicateCoroutinesApi::class)
    fun launchGameAsync(
        activity: Activity,
        game: Game,
        loadSave: Boolean,
        leanback: Boolean,
    ): Boolean {
        // Bug Fix: Check if a game process is already held by another process.
        // Some cores take a few seconds to fully release the lock file on exit.
        // Added a short retry loop to improve reliability when restarting games.
        GlobalScope.launch {
            var retries = 5
            var isLocked = true
            while (retries > 0 && isLocked) {
                isLocked = GameProcessLock.isHeldByAnotherProcess(activity.applicationContext)
                if (isLocked) {
                    delay(500)
                    retries--
                }
            }

            if (isLocked) {
                activity.runOnUiThread {
                    activity.displayToast(R.string.game_process_another_game_running)
                }
                return@launch
            }

            val system = GameSystem.findById(game.systemId)
            val coreConfig = coresSelection.getCoreConfigForSystem(system)
            gameLaunchTaskHandler.handleGameStart(activity.applicationContext)
            BaseGameActivity.launchGame(activity, coreConfig, game, loadSave, leanback)
        }

        return true
    }
}
