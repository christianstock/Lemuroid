package com.swordfish.lemuroid.app.shared.game

import android.app.ActivityManager
import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.ArrayDeque
import kotlin.math.max
import kotlin.math.min

data class RewindState(
    val stateData: ByteArray,
    val timestamp: Long,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RewindState

        if (!stateData.contentEquals(other.stateData)) return false
        if (timestamp != other.timestamp) return false

        return true
    }

    override fun hashCode(): Int {
        var result = stateData.contentHashCode()
        result = 31 * result + timestamp.hashCode()
        return result
    }
}

class RewindManager(private val context: Context) {
    private val rewindBuffer = ArrayDeque<RewindState>()
    private var maxBufferSizeBytes = 0L
    private var currentBufferSizeBytes = 0L
    private var isRewindActive = false
    private var rewindIndex = 0

    enum class Strategy {
        DISABLED,      // Rewind disabled
        LOW_RAM,       // 5s intervals, 30s buffer
        MEDIUM_RAM,    // 5s intervals, 1min buffer
        HIGH_RAM,      // 1s intervals, 1min buffer
    }

    companion object {
        // Snapshot interval: each snapshot represents 0.1 seconds of gameplay
        const val SNAPSHOT_INTERVAL_MS = 100
    }

    init {
        updateStrategy()
    }

    fun updateStrategy() {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memInfo)

        val availableMemMB = memInfo.availMem / (1024 * 1024)
        
        // Reverting to available RAM strategy to avoid crashes
        val strategy = when {
            availableMemMB < 512 -> Strategy.DISABLED
            availableMemMB < 1024 -> Strategy.LOW_RAM
            availableMemMB < 2048 -> Strategy.MEDIUM_RAM
            else -> Strategy.HIGH_RAM
        }

        Timber.i("Rewind strategy selected based on AVAILABLE RAM: $strategy (Available: ${availableMemMB}MB)")

        when (strategy) {
            Strategy.DISABLED -> {
                maxBufferSizeBytes = 0
            }
            Strategy.LOW_RAM -> {
                maxBufferSizeBytes = 50 * 1024 * 1024 // 50MB
            }
            Strategy.MEDIUM_RAM -> {
                maxBufferSizeBytes = 100 * 1024 * 1024 // 100MB
            }
            Strategy.HIGH_RAM -> {
                maxBufferSizeBytes = 200 * 1024 * 1024 // 200MB
            }
        }

        currentBufferSizeBytes = 0
        rewindBuffer.clear()
    }

    suspend fun captureState(stateData: ByteArray) = withContext(Dispatchers.IO) {
        if (maxBufferSizeBytes == 0L) return@withContext // Rewind disabled

        val newStateSize = stateData.size

        // Remove old states if buffer is full
        while (currentBufferSizeBytes + newStateSize > maxBufferSizeBytes && rewindBuffer.isNotEmpty()) {
            val removedState = rewindBuffer.removeFirst()
            currentBufferSizeBytes -= removedState.stateData.size
        }

        // Add new state
        rewindBuffer.addLast(RewindState(stateData.copyOf(), System.currentTimeMillis()))
        currentBufferSizeBytes += newStateSize
    }

    fun getRewindStates(): List<RewindState> {
        return rewindBuffer.toList()
    }

    fun getBufferStats(): RewindBufferStats {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memInfo)

        return RewindBufferStats(
            stateCount = rewindBuffer.size,
            bufferSizeBytes = currentBufferSizeBytes,
            maxBufferSizeBytes = maxBufferSizeBytes,
            isEnabled = maxBufferSizeBytes > 0,
            availableMemMB = memInfo.availMem / (1024 * 1024),
            totalMemMB = memInfo.totalMem / (1024 * 1024)
        )
    }

    fun isRewindAvailable(): Boolean {
        return rewindBuffer.size > 1
    }

    fun getLatestRewindState(): RewindState? {
        return if (isRewindActive && rewindIndex < rewindBuffer.size) {
            rewindBuffer.toList().getOrNull(rewindBuffer.size - 1 - rewindIndex)
        } else {
            rewindBuffer.lastOrNull()
        }
    }

    fun rewindBackward() {
        if (!isRewindActive) {
            isRewindActive = true
            rewindIndex = 0
        }
        rewindIndex = min(rewindIndex + 1, rewindBuffer.size - 1)
        Timber.d("Rewind backward: index=$rewindIndex")
    }

    fun rewindForward() {
        if (isRewindActive) {
            rewindIndex = max(rewindIndex - 1, 0)
            if (rewindIndex == 0) {
                isRewindActive = false
            }
        }
        Timber.d("Rewind forward: index=$rewindIndex, active=$isRewindActive")
    }

    fun getRewindStateAt(index: Int): RewindState? {
        return rewindBuffer.toList().getOrNull(index)
    }

    fun stopRewind() {
        isRewindActive = false
        rewindIndex = 0
    }

    fun isRewindActive(): Boolean = isRewindActive

    fun getRewindProgress(): Float {
        if (rewindBuffer.isEmpty()) return 0f
        return if (isRewindActive) {
            // Calculate actual seconds rewound based on snapshot index
            // rewindIndex represents how many snapshots we've gone back
            val secondsRewound = rewindIndex * (SNAPSHOT_INTERVAL_MS / 1000f)
            secondsRewound
        } else {
            0f
        }
    }

    fun getMaxRewindSeconds(): Float {
        return if (rewindBuffer.isEmpty()) {
            0f
        } else {
            (rewindBuffer.size - 1) * (SNAPSHOT_INTERVAL_MS / 1000f)
        }
    }
}

data class RewindBufferStats(
    val stateCount: Int,
    val bufferSizeBytes: Long,
    val maxBufferSizeBytes: Long,
    val isEnabled: Boolean,
    val availableMemMB: Long,
    val totalMemMB: Long
)
