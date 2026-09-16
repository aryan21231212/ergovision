package com.ergovision.app.math

import com.ergovision.app.data.model.HazardType
import com.ergovision.app.data.model.Point3D
import com.ergovision.app.data.model.PostureMetrics

/**
 * REBA-inspired biomechanical posture analyzer using 3D world coordinates.
 * Evaluates trunk flexion, neck flexion, and shoulder abduction against clinical thresholds.
 */
class RebaPoseAnalyzer {

    companion object {
        const val NOSE = 0
        const val LEFT_EYE = 2
        const val LEFT_EAR = 7
        const val RIGHT_EAR = 8
        const val LEFT_SHOULDER = 11
        const val RIGHT_SHOULDER = 12
        const val LEFT_ELBOW = 13
        const val RIGHT_ELBOW = 14
        const val LEFT_WRIST = 15
        const val RIGHT_WRIST = 16
        const val LEFT_HIP = 23
        const val RIGHT_HIP = 24
    }

    // Vertical gravity reference vector (Y is down in MediaPipe space, so -Y is up)
    private val verticalVector = Point3D(0f, -1f, 0f)
    private var baselineTrunkOffset: Float = 0f
    
    // EMA Smoothing State
    private var isFirstFrame = true
    private var smoothedTrunk: Float = 0f
    private var smoothedNeck: Float = 0f
    private var smoothedShoulder: Float = 0f
    private val emaAlpha = 0.2f // 20% new value, 80% old value for smooth transition

    fun calibrateBaseline(offset: Float) {
        baselineTrunkOffset = offset
    }

    fun resetCalibration() {
        baselineTrunkOffset = 0f
        isFirstFrame = true
    }

    fun analyze(worldLandmarks: List<Point3D>, timestampMs: Long): PostureMetrics {
        if (worldLandmarks.size < 25) {
            return PostureMetrics(timestampMs, smoothedTrunk, smoothedNeck, smoothedShoulder, 0f, null)
        }

        // Mid-points for robust side-view torso calculation
        val midShoulder = Point3D(
            (worldLandmarks[LEFT_SHOULDER].x + worldLandmarks[RIGHT_SHOULDER].x) / 2f,
            (worldLandmarks[LEFT_SHOULDER].y + worldLandmarks[RIGHT_SHOULDER].y) / 2f,
            (worldLandmarks[LEFT_SHOULDER].z + worldLandmarks[RIGHT_SHOULDER].z) / 2f
        )
        val midHip = Point3D(
            (worldLandmarks[LEFT_HIP].x + worldLandmarks[RIGHT_HIP].x) / 2f,
            (worldLandmarks[LEFT_HIP].y + worldLandmarks[RIGHT_HIP].y) / 2f,
            (worldLandmarks[LEFT_HIP].z + worldLandmarks[RIGHT_HIP].z) / 2f
        )
        val midEar = Point3D(
            (worldLandmarks[LEFT_EAR].x + worldLandmarks[RIGHT_EAR].x) / 2f,
            (worldLandmarks[LEFT_EAR].y + worldLandmarks[RIGHT_EAR].y) / 2f,
            (worldLandmarks[LEFT_EAR].z + worldLandmarks[RIGHT_EAR].z) / 2f
        )

        // 1. Trunk Vector: Hip -> Shoulder vs Vertical
        val trunkVector = VectorMath.subtract(midShoulder, midHip)
        val rawTrunkFlexionAngle = VectorMath.angleBetween(trunkVector, verticalVector)
        val trunkFlexionAngle = (rawTrunkFlexionAngle - baselineTrunkOffset).coerceAtLeast(0f)

        // 2. Neck Vector: Shoulder -> Ear vs Trunk Vector
        val neckVector = VectorMath.subtract(midEar, midShoulder)
        val neckFlexionAngle = VectorMath.angleBetween(neckVector, trunkVector)

        // 3. Upper Arm Abduction: Shoulder -> Elbow vs Trunk 
        // We project to the 2D Coronal Plane (X, Y only) by setting Z=0 to ignore forward reaching.
        // CRITICAL FIX: The trunkVector points UP (shoulder - hip), but the arm vector points DOWN (elbow - shoulder).
        // To get the correct angle (0 when arms hang down), we must compare against a DOWNWARD trunk vector.
        val leftArm2D = Point3D(worldLandmarks[LEFT_ELBOW].x - worldLandmarks[LEFT_SHOULDER].x, worldLandmarks[LEFT_ELBOW].y - worldLandmarks[LEFT_SHOULDER].y, 0f)
        val rightArm2D = Point3D(worldLandmarks[RIGHT_ELBOW].x - worldLandmarks[RIGHT_SHOULDER].x, worldLandmarks[RIGHT_ELBOW].y - worldLandmarks[RIGHT_SHOULDER].y, 0f)
        val trunkVectorDown2D = Point3D(-trunkVector.x, -trunkVector.y, 0f)

        val armAbductionAngle = maxOf(
            VectorMath.angleBetween(leftArm2D, trunkVectorDown2D),
            VectorMath.angleBetween(rightArm2D, trunkVectorDown2D)
        )

        // Apply EMA Smoothing
        if (isFirstFrame) {
            smoothedTrunk = trunkFlexionAngle
            smoothedNeck = neckFlexionAngle
            smoothedShoulder = armAbductionAngle
            isFirstFrame = false
        } else {
            smoothedTrunk = (emaAlpha * trunkFlexionAngle) + ((1f - emaAlpha) * smoothedTrunk)
            smoothedNeck = (emaAlpha * neckFlexionAngle) + ((1f - emaAlpha) * smoothedNeck)
            smoothedShoulder = (emaAlpha * armAbductionAngle) + ((1f - emaAlpha) * smoothedShoulder)
        }

        // Evaluate REBA Risk Score
        var riskScore = 0f
        var detectedHazard: HazardType? = null

        if (smoothedTrunk > 60f) {
            riskScore += 3.0f
            detectedHazard = HazardType.TRUNK_FLEXION_SEVERE
        } else if (smoothedTrunk > 20f) {
            riskScore += 1.5f
            detectedHazard = HazardType.TRUNK_FLEXION_MODERATE
        }

        if (smoothedNeck > 20f) {
            riskScore += 1.0f
            if (detectedHazard == null) detectedHazard = HazardType.NECK_FLEXION
            else detectedHazard = HazardType.COMPOUND_STRAIN
        }

        if (smoothedShoulder > 90f) {
            riskScore += 2.0f
            if (detectedHazard == null) detectedHazard = HazardType.SHOULDER_ABDUCTION
            else detectedHazard = HazardType.COMPOUND_STRAIN
        }

        return PostureMetrics(
            timestampMs = timestampMs,
            trunkAngleDegrees = smoothedTrunk,
            neckAngleDegrees = smoothedNeck,
            shoulderAngleDegrees = smoothedShoulder,
            rawHazardScore = riskScore,
            detectedHazard = detectedHazard
        )
    }
}
