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
