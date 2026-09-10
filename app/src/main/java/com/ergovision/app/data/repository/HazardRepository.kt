package com.ergovision.app.data.repository

import com.ergovision.app.data.dao.HazardEventDao
import com.ergovision.app.data.entity.HazardEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class HazardRepository(private val dao: HazardEventDao) {

    val allEventsFlow: Flow<List<HazardEvent>> = dao.getAllEventsFlow()

    suspend fun recordHazard(event: HazardEvent): Long = withContext(Dispatchers.IO) {
        dao.insertEvent(event)
    }

    suspend fun updateCoaching(id: Long, advice: String) = withContext(Dispatchers.IO) {
        val events = dao.getEventsSince(0)
        events.find { it.id == id }?.let {
            dao.updateEvent(it.copy(coachingAdvice = advice))
        }
    }

    suspend fun getRecentEvents(windowMs: Long): List<HazardEvent> = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis() - windowMs
        dao.getEventsSince(startTime)
    }

    suspend fun exportToCsvString(): String = withContext(Dispatchers.IO) {
        val events = dao.getEventsSince(0)
        val sb = StringBuilder()
        sb.append("EventId,TimestampMs,DurationSec,HazardType,PeakAngleDeg,PeakScore,CoachingAdvice\n")
        events.forEach { e ->
            sb.append("${e.id},${e.timestampMs},${e.durationSeconds},${e.hazardType.name},${e.peakAngleDegrees},${e.peakScore},\"${e.coachingAdvice.orEmpty()}\"\n")
        }
        sb.toString()
    }

    suspend fun clearLogs() = withContext(Dispatchers.IO) {
        dao.clearAll()
    }
}
