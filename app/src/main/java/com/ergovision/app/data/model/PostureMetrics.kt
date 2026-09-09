package com.ergovision.app.data.model

data class PostureMetrics(
    val timestampMs: Long,
    val trunkAngleDegrees: Float,
    val neckAngleDegrees: Float,
    val shoulderAngleDegrees: Float,
    val rawHazardScore: Float,
    val detectedHazard: HazardType?
)
