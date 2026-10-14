package com.example.util

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import kotlin.math.sqrt

/**
 * Helper to detect deliberate device shakes for counting while avoiding false positives
 * from walking or regular hand movements.
 */
class ShakeDetector(
    context: Context,
    private val onShake: () -> Unit
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    // Try LINEAR_ACCELERATION first (gravity already isolated), fallback to ACCELEROMETER
    private val linearAcceleration = sensorManager?.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
    private val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val targetSensor: Sensor? = linearAcceleration ?: accelerometer

    private var lastShakeTimestamp: Long = 0L

    // Debounce duration in milliseconds (450ms allows comfortable sequential shakes)
    private val debounceIntervalMs: Long = 450L

    // Moderate threshold: 11.0f gives an easy, reliable shake detection without extreme force
    private val shakeThreshold: Float = 11.0f

    // Gravity filter for TYPE_ACCELEROMETER
    private var gravity = floatArrayOf(0f, 0f, 0f)
    private val alpha = 0.8f

    fun start() {
        if (targetSensor != null) {
            val registered = sensorManager?.registerListener(
                this,
                targetSensor,
                SensorManager.SENSOR_DELAY_UI
            ) ?: false
            Log.d("ShakeDetector", "ShakeDetector started with sensor: ${targetSensor.name}, registered=$registered")
        } else {
            Log.w("ShakeDetector", "No accelerometer or linear acceleration sensor found on this device!")
        }
    }

    fun stop() {
        Log.d("ShakeDetector", "ShakeDetector stopped")
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        val deltaX: Float
        val deltaY: Float
        val deltaZ: Float

        if (event.sensor.type == Sensor.TYPE_LINEAR_ACCELERATION) {
            // Gravity is already removed by the hardware/OS
            deltaX = event.values[0]
            deltaY = event.values[1]
            deltaZ = event.values[2]
        } else if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            // Isolate gravity with low-pass filter
            gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0]
            gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1]
            gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2]

            // Remove the gravity contribution with high-pass filter
            deltaX = event.values[0] - gravity[0]
            deltaY = event.values[1] - gravity[1]
            deltaZ = event.values[2] - gravity[2]
        } else {
            return
        }

        // Net user-applied acceleration vector length
        val netAcceleration = sqrt((deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ).toDouble()).toFloat()

        if (netAcceleration > shakeThreshold) {
            val now = System.currentTimeMillis()
            if (now - lastShakeTimestamp >= debounceIntervalMs) {
                lastShakeTimestamp = now
                Log.d("ShakeDetector", "Shake detected successfully! netAcceleration=$netAcceleration, threshold=$shakeThreshold")
                onShake()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No-op
    }
}
