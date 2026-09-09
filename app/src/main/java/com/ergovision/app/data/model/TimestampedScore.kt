package com.ergovision.app.data.model

data class TimestampedScore(
    val timestampMs: Long,
    val score: Float,
    val hazardType: HazardType?
)
