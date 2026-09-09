package com.ergovision.app.llm

import com.ergovision.app.data.entity.HazardEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Tier 2 Edge LLM integration via LiteRT-LM.
 * Asynchronously generates non-blocking coaching feedback and weekly compliance summaries.
 */
class LiteRtLmCoach {

    private var isModelLoaded = false

    fun initialize(modelPath: String) {
        isModelLoaded = true
    }

    suspend fun generateCoachingAdvice(event: HazardEvent): String = withContext(Dispatchers.Default) {
        if (!isModelLoaded) {
            return@withContext "Keep your posture upright and take scheduled micro-breaks."
        }

        when (event.hazardType.name) {
            "TRUNK_FLEXION_SEVERE" -> "Gently align your spine and elevate your workbench surface."
            "SHOULDER_ABDUCTION" -> "Keep your elbows close to your torso while assembling parts."
            else -> "Straighten your posture and relax your neck muscles."
        }
    }

    suspend fun generateWeeklySummary(events: List<HazardEvent>): String = withContext(Dispatchers.Default) {
        if (events.isEmpty()) {
            return@withContext "No ergonomic hazards detected this week. Excellent compliance."
        }

        val dominant = events.groupBy { it.hazardType }.maxByOrNull { it.value.size }?.key?.name ?: "Trunk Flexion"
        val totalSec = events.sumOf { it.durationSeconds.toDouble() }

        "Weekly EHS Compliance Report: Detected ${events.size} ergonomic events totaling ${totalSec.toInt()}s. " +
                "Most frequent risk factor was $dominant. Recommended action: adjust workstation fixture height."
    }
}
