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

    fun analyze(worldLandmarks: List<Point3D>, timestampMs: Long): PostureMetrics {
        if (worldLandmarks.size < 25) {
            return PostureMetrics(timestampMs, 0f, 0f, 0f, 0f, null)
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
        val trunkFlexionAngle = VectorMath.angleBetween(trunkVector, verticalVector)

        // 2. Neck Vector: Shoulder -> Ear vs Trunk Vector
        val neckVector = VectorMath.subtract(midEar, midShoulder)
        val neckFlexionAngle = VectorMath.angleBetween(neckVector, trunkVector)

        // 3. Upper Arm Abduction: Shoulder -> Elbow vs Trunk
        val leftArm = VectorMath.subtract(worldLandmarks[LEFT_ELBOW], worldLandmarks[LEFT_SHOULDER])
        val rightArm = VectorMath.subtract(worldLandmarks[RIGHT_ELBOW], worldLandmarks[RIGHT_SHOULDER])
        val armAbductionAngle = maxOf(
            VectorMath.angleBetween(leftArm, trunkVector),
            VectorMath.angleBetween(rightArm, trunkVector)
        )

        // Evaluate REBA Risk Score
        var riskScore = 0f
        var detectedHazard: HazardType? = null

        if (trunkFlexionAngle > 60f) {
            riskScore += 3.0f
            detectedHazard = HazardType.TRUNK_FLEXION_SEVERE
        } else if (trunkFlexionAngle > 20f) {
            riskScore += 1.5f
            detectedHazard = HazardType.TRUNK_FLEXION_MODERATE
        }

        if (neckFlexionAngle > 20f) {
            riskScore += 1.0f
            if (detectedHazard == null) detectedHazard = HazardType.NECK_FLEXION
            else detectedHazard = HazardType.COMPOUND_STRAIN
        }

        if (armAbductionAngle > 90f) {
            riskScore += 2.0f
            if (detectedHazard == null) detectedHazard = HazardType.SHOULDER_ABDUCTION
            else detectedHazard = HazardType.COMPOUND_STRAIN
        }

        return PostureMetrics(
            timestampMs = timestampMs,
            trunkAngleDegrees = trunkFlexionAngle,
            neckAngleDegrees = neckFlexionAngle,
            shoulderAngleDegrees = armAbductionAngle,
            rawHazardScore = riskScore,
            detectedHazard = detectedHazard
        )
    }
}
