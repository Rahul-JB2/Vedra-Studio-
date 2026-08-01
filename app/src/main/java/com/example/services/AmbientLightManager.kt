package com.example.services

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.provider.Settings
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf

class AmbientLightManager(private val context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val lightSensor: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_LIGHT)

    private val _ambientLux = mutableFloatStateOf(150f)
    val ambientLux: State<Float> = _ambientLux

    private val _systemBrightness = mutableIntStateOf(128)
    val systemBrightness: State<Int> = _systemBrightness

    private val _tintIntensity = mutableFloatStateOf(1.0f)
    val tintIntensity: State<Float> = _tintIntensity

    private val _bgAlpha = mutableFloatStateOf(0.16f)
    val bgAlpha: State<Float> = _bgAlpha

    private val _borderAlpha = mutableFloatStateOf(0.28f)
    val borderAlpha: State<Float> = _borderAlpha

    private var isListening = false

    fun startListening() {
        if (!isListening) {
            lightSensor?.let { sensor ->
                sensorManager?.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
                isListening = true
            }
        }
        updateSystemBrightness()
    }

    fun stopListening() {
        if (isListening) {
            sensorManager?.unregisterListener(this)
            isListening = false
        }
    }

    fun updateSystemBrightness() {
        try {
            val brightness = Settings.System.getInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS
            )
            _systemBrightness.intValue = brightness
            if (lightSensor == null) {
                // Fallback to system brightness for ambient factor if hardware light sensor isn't available
                val factor = brightness / 255f
                calculateTintFromFactor(factor * 300f)
            }
        } catch (e: Exception) {
            _systemBrightness.intValue = 128
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_LIGHT) {
            val lux = event.values[0]
            _ambientLux.floatValue = lux
            calculateTintFromFactor(lux)
        }
    }

    private fun calculateTintFromFactor(lux: Float) {
        // Lux ranges: 0..25 (dark room), 25..350 (indoor), 350+ (bright)
        when {
            lux < 25f -> {
                // Dim ambient light -> sleek deep dark glass, glowing crisp border
                _tintIntensity.floatValue = 0.75f
                _bgAlpha.floatValue = 0.10f
                _borderAlpha.floatValue = 0.22f
            }
            lux > 350f -> {
                // High ambient light -> increased glass contrast & brighter tint highlights for sunlight readability
                _tintIntensity.floatValue = 1.35f
                _bgAlpha.floatValue = 0.25f
                _borderAlpha.floatValue = 0.40f
            }
            else -> {
                // Balanced indoor ambient light
                val normalized = (lux - 25f) / (350f - 25f)
                _tintIntensity.floatValue = 0.75f + (0.60f * normalized)
                _bgAlpha.floatValue = 0.10f + (0.15f * normalized)
                _borderAlpha.floatValue = 0.22f + (0.18f * normalized)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
