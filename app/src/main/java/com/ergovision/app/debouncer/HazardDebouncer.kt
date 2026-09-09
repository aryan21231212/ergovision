package com.ergovision.app.debouncer

import com.ergovision.app.data.model.HazardState
import com.ergovision.app.data.model.HazardType
import com.ergovision.app.data.model.PostureMetrics
import com.ergovision.app.data.model.TimestampedScore
import java.util.ArrayDeque

/**
 * Sliding time-window debouncer with rolling median filter.
 * Prevents false alerts from single-frame tracking noise and accounts for thermal FPS fluctuation.
 */
class HazardDebouncer(
    private val windowDurationMs: Long = 5000L, // 5.0-second sustained hazard window
    private val cooldownDurationMs: Long = 15000L, // 15-second cooldown
    private val riskThreshold: Float = 1.4f,
    private val onHazardTriggered: (HazardType, Float, Float) -> Unit
) {

    private val rollingWindow = ArrayDeque<TimestampedScore>()
    private var currentState: HazardState = HazardState.SAFE
    private var lastTriggerTimeMs: Long = 0L

    fun processFrame(metrics: PostureMetrics): HazardState {
        val now = metrics.timestampMs

        // Check cooldown state
        if (currentState == HazardState.COOLDOWN) {
            if (now - lastTriggerTimeMs >= cooldownDurationMs) {
                currentState = HazardState.SAFE
                rollingWindow.clear()
            } else {
                return HazardState.COOLDOWN
            }
        }

        // Add current frame to sliding window
        rollingWindow.addLast(
            TimestampedScore(
                timestampMs = now,
                score = metrics.rawHazardScore,
                hazardType = metrics.detectedHazard
            )
        )

        // Evict expired samples older than windowDurationMs
        while (rollingWindow.isNotEmpty() && (now - rollingWindow.first.timestampMs) > windowDurationMs) {
            rollingWindow.removeFirst()
        }

        // Determine if window covers full required duration (at least 80% of window time elapsed)
        val windowSpan = if (rollingWindow.isNotEmpty()) now - rollingWindow.first.timestampMs else 0L
        if (windowSpan < (windowDurationMs * 0.8f)) {
            currentState = HazardState.EVALUATING
            return currentState
        }

        // Calculate median risk score across sliding window
        val scores = rollingWindow.map { it.score }.sorted()
        val medianScore = if (scores.isNotEmpty()) scores[scores.size / 2] else 0f

        if (medianScore >= riskThreshold) {
            val dominantHazard = rollingWindow
                .mapNotNull { it.hazardType }
                .groupBy { it }
                .maxByOrNull { it.value.size }?.key ?: HazardType.TRUNK_FLEXION_MODERATE

            val peakAngle = maxOf(metrics.trunkAngleDegrees, metrics.neckAngleDegrees, metrics.shoulderAngleDegrees)
            val durationSec = windowSpan / 1000f

            currentState = HazardState.TRIGGERED
            lastTriggerTimeMs = now
            onHazardTriggered(dominantHazard, peakAngle, durationSec)

            currentState = HazardState.COOLDOWN
        } else {
            currentState = HazardState.SAFE
        }

        return currentState
    }

    fun reset() {
        rollingWindow.clear()
        currentState = HazardState.SAFE
    }
}
