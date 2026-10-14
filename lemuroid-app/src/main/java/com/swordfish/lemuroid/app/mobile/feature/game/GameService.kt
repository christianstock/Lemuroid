package com.swordfish.lemuroid.app.mobile.feature.game

import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import androidx.core.graphics.drawable.toBitmap
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.swordfish.lemuroid.app.mobile.shared.NotificationsManager
import dagger.android.DaggerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import kotlin.system.exitProcess

class GameService : DaggerService() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onBind(intent: Intent?) = null

    private suspend fun fetchBoxArtBitmap(context: Context, coverUrl: String?): Bitmap? {
        if (coverUrl.isNullOrEmpty()) return null
        return runCatching {
            val loader = ImageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(coverUrl)
                .allowHardware(false) // Required for RemoteViews bitmap rendering
                .build()

            val result = loader.execute(request)
            (result as? SuccessResult)?.drawable?.toBitmap()
        }.getOrNull()
    }

    override fun onCreate() {
        super.onCreate()
        serviceScope.launch {
            awaitTermination()
            withContext(Dispatchers.Main) {
                ServiceCompat.stopForeground(this@GameService, ServiceCompat.STOP_FOREGROUND_REMOVE)
                stopSelf()
                exitProcess(0)
            }
        }
    }

    override fun onStartCommand(
        intent: Intent,
        flags: Int,
        startId: Int,
    ): Int {
        displayNotification(intent)
        return START_NOT_STICKY
    }

    private fun displayNotification(intent: Intent) {
        val gameIntent = intent.getParcelableExtra<Intent>(EXTRA_GAME_ACTIVITY_INTENT) ?: return
        val gameTitle = intent.getStringExtra(EXTRA_GAME_TITLE)
        val coverUrl = intent.getStringExtra(EXTRA_GAME_COVER_URL)

        val notificationsManager = NotificationsManager(applicationContext)

        // 1. Instantly post fallback notification to satisfy Android Foreground Service SLA
        val initialNotification = notificationsManager.gameRunningNotification(
            gameIntent = gameIntent,
            gameTitle = gameTitle,
            coverBitmap = null
        )

        ServiceCompat.startForeground(
            this,
            NotificationsManager.GAME_RUNNING_NOTIFICATION_ID,
            initialNotification,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC,
        )

        // 2. Load cover art asynchronously and update notification when ready
        if (!coverUrl.isNullOrEmpty()) {
            serviceScope.launch {
                val coverBitmap = fetchBoxArtBitmap(applicationContext, coverUrl)
                if (coverBitmap != null) {
                    val updatedNotification = notificationsManager.gameRunningNotification(
                        gameIntent = gameIntent,
                        gameTitle = gameTitle,
                        coverBitmap = coverBitmap
                    )
                    NotificationManagerCompat.from(this@GameService)
                        .notify(NotificationsManager.GAME_RUNNING_NOTIFICATION_ID, updatedNotification)
                }
            }
        }
    }

    private fun hideNotification() {
        NotificationManagerCompat.from(this).cancel(NotificationsManager.GAME_RUNNING_NOTIFICATION_ID)
    }

    override fun onDestroy() {
        serviceScope.cancel()
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        hideNotification()
    }

    companion object {
        private const val EXTRA_GAME_ACTIVITY_INTENT = "EXTRA_GAME_ACTIVITY_INTENT"
        private const val EXTRA_GAME_TITLE = "EXTRA_GAME_TITLE"
        private const val EXTRA_GAME_COVER_URL = "EXTRA_GAME_COVER_URL"

        private data class GameProcessTask(
            val task: suspend () -> Unit = {},
            val terminate: Boolean = false,
        )

        private val tasks = Channel<GameProcessTask>(capacity = Channel.BUFFERED)

        fun startService(
            context: Context,
            gameActivityIntent: Intent,
            gameTitle: String? = null,
            coverUrl: String? = null,
        ) {
            context.startService(
                Intent(context, GameService::class.java).apply {
                    putExtra(EXTRA_GAME_ACTIVITY_INTENT, gameActivityIntent)
                    putExtra(EXTRA_GAME_TITLE, gameTitle)
                    putExtra(EXTRA_GAME_COVER_URL, coverUrl)
                },
            )
        }

        fun schedule(task: suspend () -> Unit) {
            val result = tasks.trySend(GameProcessTask(task = task))
            Timber.i("GameService.schedule sent=%s", result.isSuccess)
        }

        fun requestTermination() {
            val result = tasks.trySend(GameProcessTask(terminate = true))
            Timber.i("GameService.requestTermination sent=%s", result.isSuccess)
        }

        private suspend fun awaitTermination() {
            for (task in tasks) {
                runCatching { task.task() }
                    .onFailure { Timber.e(it, "GameService task failed") }

                if (task.terminate) {
                    return
                }
            }
        }
    }
}
