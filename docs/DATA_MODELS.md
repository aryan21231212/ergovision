# Domain Data Models & Schemas (DATA_MODELS.md)

## 1. Core Enumerations

```kotlin
package com.ergovision.app.data.model

enum class HazardType(val severity: Int, val defaultSpokenAlert: String) {
    TRUNK_FLEXION_MODERATE(1, "Straighten your back."),
    TRUNK_FLEXION_SEVERE(2, "Warning: Severe back strain. Stand upright immediately."),
    NECK_FLEXION(1, "Lift your chin and relax your neck."),
    SHOULDER_ABDUCTION(2, "Lower your elbows below shoulder level."),
    COMPOUND_STRAIN(3, "Critical posture alert: Re-align back and neck.")
}

enum class HazardState {
    SAFE,
    EVALUATING,
    TRIGGERED,
    COOLDOWN
}
```

## 2. Ephemeral In-Memory Models (Processed at 5 FPS, Not Persisted)

```kotlin
package com.ergovision.app.data.model

data class Point3D(
    val x: Float,
    val y: Float,
    val z: Float
)

data class PostureMetrics(
    val timestampMs: Long,
    val trunkAngleDegrees: Float,
    val neckAngleDegrees: Float,
    val shoulderAngleDegrees: Float,
    val rawHazardScore: Float,
    val detectedHazard: HazardType?
)

data class TimestampedScore(
    val timestampMs: Long,
    val score: Float,
    val hazardType: HazardType?
)
```

## 3. Persistent Storage Schemas (Room Entity)

```kotlin
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
```

## 4. Room DAO Specification

```kotlin
package com.ergovision.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.ergovision.app.data.entity.HazardEvent
import kotlinx.coroutines.flow.Flow

@Dao
interface HazardEventDao {
    @Insert
    suspend fun insertEvent(event: HazardEvent): Long

    @Update
    suspend fun updateEvent(event: HazardEvent)

    @Query("SELECT * FROM hazard_events ORDER BY timestampMs DESC")
    fun getAllEventsFlow(): Flow<List<HazardEvent>>

    @Query("SELECT * FROM hazard_events WHERE timestampMs >= :startTimeMs ORDER BY timestampMs ASC")
    suspend fun getEventsSince(startTimeMs: Long): List<HazardEvent>

    @Query("SELECT COUNT(*) FROM hazard_events")
    suspend fun getTotalEventCount(): Int

    @Query("DELETE FROM hazard_events")
    suspend fun clearAll()
}
```

## 5. CSV Export Schema (Office Kit File Transfer)

```csv
EventId,TimestampIso,DurationSec,HazardType,PeakAngleDeg,PeakScore,CoachingAdvice
1,2026-09-09T08:15:30Z,6.2,TRUNK_FLEXION_MODERATE,48.5,2.4,"Keep lower back straight while handling components."
2,2026-09-09T08:24:10Z,8.0,SHOULDER_ABDUCTION,98.2,3.0,"Lower bin position to avoid reaching above shoulder."
```
