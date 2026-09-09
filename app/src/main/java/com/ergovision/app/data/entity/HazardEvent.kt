package com.ergovision.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ergovision.app.data.model.HazardType

@Entity(tableName = "hazard_events")
data class HazardEvent(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestampMs: Long,
    val durationSeconds: Float,
    val hazardType: HazardType,
    val peakAngleDegrees: Float,
    val peakScore: Float,
    val coachingAdvice: String? = null
)
