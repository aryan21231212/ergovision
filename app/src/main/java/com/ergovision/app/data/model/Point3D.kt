package com.ergovision.app.data.model

/**
 * Metric 3D coordinate point output by MediaPipe PoseLandmarker (world landmarks, hip origin in meters).
 */
data class Point3D(
    val x: Float,
    val y: Float,
    val z: Float,
    val visibility: Float = 1.0f
)
