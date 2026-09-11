package com.swordfish.lemuroid.app.shared.motion

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
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

    private val _debugInfo = MutableStateFlow("Motion: Ready")
    val debugInfo = _debugInfo.asStateFlow()
    
    private var callCount = 0
    private var bridgeStatus = "IDLE"
    private var lastError = ""
    private var pulse = false

    companion object {
        const val TAG = "MotionManager"
        const val RETRO_SENSOR_ACCELEROMETER_X = 0
        const val RETRO_SENSOR_ACCELEROMETER_Y = 1
        const val RETRO_SENSOR_ACCELEROMETER_Z = 2
        const val RETRO_SENSOR_GYROSCOPE_X = 3
        const val RETRO_SENSOR_GYROSCOPE_Y = 4
        const val RETRO_SENSOR_GYROSCOPE_Z = 5
        const val RETRO_SENSOR_ILLUMINANCE = 6
    }

    fun setDebugMessage(msg: String) {
        _debugInfo.value = msg
        Log.d(TAG, "Status Update: $msg")
    }

    suspend fun collectAndProcessMotionEvents(
        systemCoreConfig: SystemCoreConfig,
        retroGameView: GLRetroView,
        enabledSensorsFlow: Flow<Set<Int>>,
        onSensorMissing: (Int) -> Unit = {},
    ) {
        Log.i(TAG, "Motion tracking starting. SensorsSupported: ${systemCoreConfig.sensorsSupported}")
        
        if (!systemCoreConfig.sensorsSupported) {
            _debugInfo.value = "Sensors: NOT SUPPORTED"
            return
        }

        val probeSensors = setOf(0, 1, 2, 3, 4, 5, 6)

        enabledSensorsFlow.onStart { emit(probeSensors) }.collectLatest { coreRequestedSensors ->
            val activeSensors = coreRequestedSensors.ifEmpty { probeSensors }
            Log.d(TAG, "Core requested sensors: ${activeSensors.joinToString(",")}")
            
            val sensorValues = mutableMapOf<Int, Float>()
            val accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            val gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
            val lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

            val callback: Flow<Unit> = callbackFlow {
                val listener = object : SensorEventListener {
                    override fun onSensorChanged(event: SensorEvent) {
                        pulse = !pulse
                        val pulseChar = if (pulse) "*" else " "
                        
                        @Suppress("DEPRECATION")
                        val rotation = windowManager.defaultDisplay.rotation
                        
                        when (event.sensor.type) {
                            Sensor.TYPE_ACCELEROMETER -> {
                                val (x, y, z) = remapSensor(event.values, rotation)
                                
                                // 1. Attempt Native Bridge
                                sendSensor(retroGameView, 0, x)
                                sendSensor(retroGameView, 1, y)
                                sendSensor(retroGameView, 2, z)
                                
                                // 2. Fallback: High-Accuracy Analog Injection (Kirby/Wario)
                                val stickX = (x / 9.81f).coerceIn(-1.0f, 1.0f)
                                val stickY = (y / 9.81f).coerceIn(-1.0f, 1.0f)
                                retroGameView.sendMotionEvent(GLRetroView.MOTION_SOURCE_ANALOG_RIGHT, -stickX, -stickY, 0)
                                
                                sensorValues[0] = x
                                sensorValues[1] = y
                                sensorValues[2] = z
                                
                                if (callCount % 100 == 0) {
                                    Log.v(TAG, "ACC: raw[${event.values[0]}, ${event.values[1]}] -> remapped[$x, $y] -> stick[-$stickX, -$stickY]")
                                }
                            }
                            Sensor.TYPE_GYROSCOPE -> {
                                val (x, y, z) = remapSensor(event.values, rotation)
                                
                                sendSensor(retroGameView, 3, x)
                                sendSensor(retroGameView, 4, y)
                                sendSensor(retroGameView, 5, z)
                                
                                // Fallback: Velocity Injection (WarioWare Twisted)
                                val gyroValue = (z / 5.0f).coerceIn(-1.0f, 1.0f)
                                retroGameView.sendMotionEvent(GLRetroView.MOTION_SOURCE_ANALOG_RIGHT, gyroValue, 0f, 0)
                                
                                sensorValues[3] = x
                                sensorValues[4] = y
                                sensorValues[5] = z

                                if (callCount % 100 == 0) {
                                    Log.v(TAG, "GYRO: raw_z=${event.values[2]} -> remapped_z=$z -> gyroValue=$gyroValue")
                                }
                            }
                            Sensor.TYPE_LIGHT -> {
                                val lux = event.values[0]
                                sendSensor(retroGameView, 6, lux)
                                sensorValues[6] = lux
                                if (callCount % 100 == 0) Log.v(TAG, "LIGHT: $lux lux")
                            }
                        }
                        
                        _debugInfo.value = "BRIDGE: $bridgeStatus $pulseChar | CALLS: $callCount\n" +
                                (if (lastError.isNotEmpty()) "ERR: $lastError\n" else "") +
                                "CORE_REQ: ${coreRequestedSensors.joinToString(",")}\n" +
                                sensorValues.entries.sortedBy { it.key }.joinToString("\n") { 
                                    val name = when(it.key) {
                                        0 -> "ACC_X"
                                        1 -> "ACC_Y"
                                        2 -> "ACC_Z"
                                        3 -> "GYR_X"
                                        4 -> "GYR_Y"
                                        5 -> "GYR_Z"
                                        6 -> "LUX"
                                        else -> "UNK"
                                    }
                                    "$name: %.2f".format(it.value)
                                }
                    }

                    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
                }

                Log.i(TAG, "Registering Sensor Listeners. ACC: ${accSensor != null}, GYRO: ${gyroSensor != null}, LIGHT: ${lightSensor != null}")
                if (accSensor != null) sensorManager.registerListener(listener, accSensor, SensorManager.SENSOR_DELAY_GAME)
                if (gyroSensor != null) sensorManager.registerListener(listener, gyroSensor, SensorManager.SENSOR_DELAY_GAME)
                if (lightSensor != null) sensorManager.registerListener(listener, lightSensor, SensorManager.SENSOR_DELAY_NORMAL)

                awaitClose {
                    Log.i(TAG, "Closing Sensor Listeners")
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
            if (bridgeStatus != "PASSTHROUGH") {
                Log.i(TAG, "Native Passthrough Bridge established")
                bridgeStatus = "PASSTHROUGH"
            }
            callCount++
            lastError = ""
        } catch (e: NoSuchMethodException) {
            if (bridgeStatus != "EMULATED") {
                Log.w(TAG, "Native bridge method NOT found. Using emulated analog injection.")
                bridgeStatus = "EMULATED"
                lastError = "AAR Update Req."
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error sending sensor event", e)
            bridgeStatus = "ERROR"
            lastError = e.javaClass.simpleName
        }
    }
}
