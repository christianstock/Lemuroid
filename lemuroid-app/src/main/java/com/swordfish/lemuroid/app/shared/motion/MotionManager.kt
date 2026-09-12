package com.swordfish.lemuroid.app.shared.motion

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.Surface
import android.view.WindowManager
import com.swordfish.lemuroid.lib.library.SystemCoreConfig
import com.swordfish.libretrodroid.GLRetroView
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onStart

class MotionManager(context: Context) {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    private val _debugInfo = MutableStateFlow("")
    val debugInfo = _debugInfo.asStateFlow()

    companion object {
        const val RETRO_SENSOR_ACCELEROMETER_X = 0
        const val RETRO_SENSOR_ACCELEROMETER_Y = 1
        const val RETRO_SENSOR_ACCELEROMETER_Z = 2
        const val RETRO_SENSOR_GYROSCOPE_X = 3
        const val RETRO_SENSOR_GYROSCOPE_Y = 4
        const val RETRO_SENSOR_GYROSCOPE_Z = 5
        const val RETRO_SENSOR_ILLUMINANCE = 6
    }

    suspend fun collectAndProcessMotionEvents(
        systemCoreConfig: SystemCoreConfig,
        retroGameView: GLRetroView,
        enabledSensorsFlow: Flow<Set<Int>>,
        onSensorMissing: (Int) -> Unit = {},
    ) {
        if (!systemCoreConfig.sensorsSupported) return

        val probeSensors = setOf(0, 1, 2, 3, 4, 5, 6)

        enabledSensorsFlow.onStart { emit(probeSensors) }.collectLatest { _ ->
            val accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            val gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
            val lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

            val callback: Flow<Unit> = callbackFlow {
                val listener = object : SensorEventListener {
                    override fun onSensorChanged(event: SensorEvent) {
                        @Suppress("DEPRECATION")
                        val rotation = windowManager.defaultDisplay.rotation
                        
                        when (event.sensor.type) {
                            Sensor.TYPE_ACCELEROMETER -> {
                                val (x, y, z) = remapSensor(event.values, rotation)
                                sendSensor(retroGameView, 0, x)
                                sendSensor(retroGameView, 1, y)
                                sendSensor(retroGameView, 2, z)
                                val stickX = (x / 9.81f).coerceIn(-1.0f, 1.0f)
                                val stickY = (y / 9.81f).coerceIn(-1.0f, 1.0f)
                                retroGameView.sendMotionEvent(GLRetroView.MOTION_SOURCE_ANALOG_RIGHT, -stickX, -stickY, 0)
                            }
                            Sensor.TYPE_GYROSCOPE -> {
                                val (x, y, z) = remapSensor(event.values, rotation)
                                sendSensor(retroGameView, 3, x)
                                sendSensor(retroGameView, 4, y)
                                sendSensor(retroGameView, 5, z)
                                val gyroValue = (z / 5.0f).coerceIn(-1.0f, 1.0f)
                                retroGameView.sendMotionEvent(GLRetroView.MOTION_SOURCE_ANALOG_RIGHT, gyroValue, 0f, 0)
                            }
                            Sensor.TYPE_LIGHT -> {
                                sendSensor(retroGameView, 6, event.values[0])
                            }
                        }
                    }

                    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
                }

                if (accSensor != null) sensorManager.registerListener(listener, accSensor, SensorManager.SENSOR_DELAY_GAME)
                if (gyroSensor != null) sensorManager.registerListener(listener, gyroSensor, SensorManager.SENSOR_DELAY_GAME)
                if (lightSensor != null) sensorManager.registerListener(listener, lightSensor, SensorManager.SENSOR_DELAY_NORMAL)

                awaitClose {
                    sensorManager.unregisterListener(listener)
                }
            }

            callback.collect {}
        }
    }

    private fun remapSensor(values: FloatArray, rotation: Int): Triple<Float, Float, Float> {
        return when (rotation) {
            Surface.ROTATION_90 -> Triple(-values[1], values[0], values[2])
            Surface.ROTATION_270 -> Triple(values[1], -values[0], values[2])
            Surface.ROTATION_180 -> Triple(-values[0], -values[1], values[2])
            else -> Triple(values[0], values[1], values[2])
        }
    }

    private fun sendSensor(view: GLRetroView, id: Int, value: Float) {
        try {
            val method = view.javaClass.getMethod("sendSensorEvent", Int::class.javaPrimitiveType, Float::class.javaPrimitiveType)
            method.invoke(view, id, value)
        } catch (e: Exception) {
            // Silently fail
        }
    }
}
