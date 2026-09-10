package com.ergovision.app.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.SystemClock
import com.ergovision.app.data.model.HazardType
import com.ergovision.app.data.model.PostureMetrics
import kotlin.math.abs

/**
 * Low-power IMU Posture Tracker (Stretch H1).
 * Uses Android fused TYPE_ROTATION_VECTOR for pocket/belt-worn worker scenarios
 * when workstation camera mounting is not possible or during high thermal throttling.
 */
class ImuPostureTracker(
    context: Context,
    private val onMetricsUpdated: (PostureMetrics) -> Unit
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val rotationSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

    private var isTracking = false
    private var baselinePitchDegrees = 0f
    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    fun start() {
        if (isTracking || rotationSensor == null) return
        sensorManager?.registerListener(this, rotationSensor, SensorManager.SENSOR_DELAY_UI)
        isTracking = true
    }

    fun stop() {
        if (!isTracking) return
        sensorManager?.unregisterListener(this)
        isTracking = false
    }

    fun calibrateBaseline() {
        // Sets current pitch orientation as neutral standing posture
        baselinePitchDegrees = Math.toDegrees(orientationAngles[1].toDouble()).toFloat()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor.type != Sensor.TYPE_ROTATION_VECTOR) return

        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
        SensorManager.getOrientation(rotationMatrix, orientationAngles)

        // orientationAngles[1] = Pitch (-pi/2 to pi/2)
        val currentPitchDegrees = Math.toDegrees(orientationAngles[1].toDouble()).toFloat()
        val forwardTilt = abs(currentPitchDegrees - baselinePitchDegrees)

        val timestamp = SystemClock.elapsedRealtime()
        var detectedHazard: HazardType? = null
        var riskScore = 0f

        if (forwardTilt > 50f) {
            riskScore = 3.0f
            detectedHazard = HazardType.TRUNK_FLEXION_SEVERE
        } else if (forwardTilt > 20f) {
            riskScore = 1.5f
            detectedHazard = HazardType.TRUNK_FLEXION_MODERATE
        }

        val metrics = PostureMetrics(
            timestampMs = timestamp,
            trunkAngleDegrees = forwardTilt,
            neckAngleDegrees = 0f, // IMU on chest cannot measure neck independently
            shoulderAngleDegrees = 0f, // IMU on chest cannot measure arm abduction
            rawHazardScore = riskScore,
            detectedHazard = detectedHazard
        )

        onMetricsUpdated(metrics)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No-op
    }
}
