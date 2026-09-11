# Required changes for LibretroDroid Repo
To enable the native sensor bridge, the following changes must be applied to the `LibretroDroid` repository and a new AAR must be built.

## 1. File: `libretrodroid/src/main/java/com/swordfish/libretrodroid/LibretroDroid.kt`
Add the following JNI declaration:
```kotlin
@JvmStatic
external fun onSensorEvent(id: Int, value: Float)
```

## 2. File: `libretrodroid/src/main/java/com/swordfish/libretrodroid/GLRetroView.kt`
Add the public method to forward events:
```kotlin
fun sendSensorEvent(sensorId: Int, value: Float) {
    queueEvent { LibretroDroid.onSensorEvent(sensorId, value) }
}

fun getEnabledSensors(): Flow<Set<Int>> {
    // This should be populated by the environment callback
    // (RETRO_ENVIRONMENT_GET_SENSOR_INTERFACE -> set_sensor_state)
    return LibretroDroid.enabledSensorsFlow
}
```

## 3. File: `libretrodroid/src/main/cpp/libretrodroidjni.cpp`
Add the JNI implementation:
```cpp
JNIEXPORT void JNICALL Java_com_swordfish_libretrodroid_LibretroDroid_onSensorEvent(
    JNIEnv *env, jclass obj, jint id, jfloat value) {
    // This function must be implemented in environment.cpp to store the sensor state
    set_sensor_input(id, value);
}
```

## 4. File: `libretrodroid/src/main/cpp/environment.cpp`
Implement the Libretro Sensor Interface:
```cpp
// Buffer to store the latest values from Android
static float sensor_values[7] = {0}; 
static bool sensor_enabled[7] = {false};

void set_sensor_input(int id, float value) {
    if (id >= 0 && id < 7) {
        sensor_values[id] = value;
    }
}

// Libretro callback implementation
static bool set_sensor_state(unsigned port, enum retro_sensor_action action, unsigned sensor_id) {
    if (sensor_id < 7) {
        sensor_enabled[sensor_id] = (action == RETRO_SENSOR_ACTION_ON);
        // TODO: Notify Kotlin side that enabled sensors changed
        return true;
    }
    return false;
}

static float get_sensor_input(unsigned port, unsigned id) {
    if (id < 7) return sensor_values[id];
    return 0.0f;
}

// In environment_cb handling:
case RETRO_ENVIRONMENT_GET_SENSOR_INTERFACE: {
    struct retro_sensor_interface *iface = (struct retro_sensor_interface *)data;
    iface->set_sensor_state = set_sensor_state;
    iface->get_sensor_input = get_sensor_input;
    return true;
}
```
