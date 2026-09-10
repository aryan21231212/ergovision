package com.ergovision.app.math

import com.ergovision.app.data.model.HazardType
import com.ergovision.app.data.model.Point3D
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RebaPoseAnalyzerTest {

    @Test
    fun testVectorMathAngleBetween() {
        val v1 = Point3D(1f, 0f, 0f)
        val v2 = Point3D(0f, 1f, 0f)
        val angle = VectorMath.angleBetween(v1, v2)
        assertEquals(90.0f, angle, 0.01f)
    }

    @Test
    fun testUprightPostureProducesLowRisk() {
        val analyzer = RebaPoseAnalyzer()
        val landmarks = MutableList(33) { Point3D(0f, 0f, 0f) }

        landmarks[RebaPoseAnalyzer.LEFT_HIP] = Point3D(-0.1f, 0f, 0f)
        landmarks[RebaPoseAnalyzer.RIGHT_HIP] = Point3D(0.1f, 0f, 0f)
        landmarks[RebaPoseAnalyzer.LEFT_SHOULDER] = Point3D(-0.15f, -0.5f, 0f)
        landmarks[RebaPoseAnalyzer.RIGHT_SHOULDER] = Point3D(0.15f, -0.5f, 0f)
        landmarks[RebaPoseAnalyzer.LEFT_EAR] = Point3D(-0.08f, -0.7f, 0f)
        landmarks[RebaPoseAnalyzer.RIGHT_EAR] = Point3D(0.08f, -0.7f, 0f)
        landmarks[RebaPoseAnalyzer.LEFT_ELBOW] = Point3D(-0.15f, -0.2f, 0f)
        landmarks[RebaPoseAnalyzer.RIGHT_ELBOW] = Point3D(0.15f, -0.2f, 0f)

        val metrics = analyzer.analyze(landmarks, 1000L)
        assertTrue("Trunk angle should be near 0 for upright posture", metrics.trunkAngleDegrees < 5f)
        assertEquals(0f, metrics.rawHazardScore, 0.1f)
    }

    @Test
    fun testSevereTrunkFlexionTriggersHazard() {
        val analyzer = RebaPoseAnalyzer()
        val landmarks = MutableList(33) { Point3D(0f, 0f, 0f) }

        landmarks[RebaPoseAnalyzer.LEFT_HIP] = Point3D(-0.1f, 0f, 0f)
        landmarks[RebaPoseAnalyzer.RIGHT_HIP] = Point3D(0.1f, 0f, 0f)
        landmarks[RebaPoseAnalyzer.LEFT_SHOULDER] = Point3D(0.45f, -0.15f, 0f)
        landmarks[RebaPoseAnalyzer.RIGHT_SHOULDER] = Point3D(0.45f, -0.15f, 0f)
        landmarks[RebaPoseAnalyzer.LEFT_EAR] = Point3D(0.6f, -0.15f, 0f)
        landmarks[RebaPoseAnalyzer.RIGHT_EAR] = Point3D(0.6f, -0.15f, 0f)

        val metrics = analyzer.analyze(landmarks, 2000L)
        assertTrue("Trunk flexion angle should exceed 60 degrees", metrics.trunkAngleDegrees > 60f)
        assertEquals(HazardType.TRUNK_FLEXION_SEVERE, metrics.detectedHazard)
    }

    @Test
    fun testCalibrationZerosOutCameraTiltAngle() {
        val analyzer = RebaPoseAnalyzer()
        val landmarks = MutableList(33) { Point3D(0f, 0f, 0f) }

        // Upright with a 15-degree camera tilt
        landmarks[RebaPoseAnalyzer.LEFT_HIP] = Point3D(-0.1f, 0f, 0f)
        landmarks[RebaPoseAnalyzer.RIGHT_HIP] = Point3D(0.1f, 0f, 0f)
        landmarks[RebaPoseAnalyzer.LEFT_SHOULDER] = Point3D(0.12f, -0.48f, 0f)
        landmarks[RebaPoseAnalyzer.RIGHT_SHOULDER] = Point3D(0.12f, -0.48f, 0f)

        val uncalibrated = analyzer.analyze(landmarks, 1000L)
        assertTrue("Uncalibrated angle should be positive due to tilt", uncalibrated.trunkAngleDegrees > 10f)

        // Calibrate baseline
        analyzer.calibrateBaseline(uncalibrated.trunkAngleDegrees)

        val calibrated = analyzer.analyze(landmarks, 1001L)
        assertEquals(0f, calibrated.trunkAngleDegrees, 0.01f)
    }
}
