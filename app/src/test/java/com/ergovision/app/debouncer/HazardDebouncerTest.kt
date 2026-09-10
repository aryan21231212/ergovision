package com.ergovision.app.debouncer

import com.ergovision.app.data.model.HazardState
import com.ergovision.app.data.model.HazardType
import com.ergovision.app.data.model.PostureMetrics
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HazardDebouncerTest {

    @Test
    fun testSafeFramesDoNotTriggerHazard() {
        var triggeredCount = 0
        val debouncer = HazardDebouncer(
            windowDurationMs = 5000L,
            cooldownDurationMs = 15000L,
            riskThreshold = 1.4f
        ) { _, _, _ -> triggeredCount++ }

        // Send 6 seconds of safe frames (1 frame every 200ms)
        for (t in 0..6000 step 200) {
            val metrics = PostureMetrics(
                timestampMs = t.toLong(),
                trunkAngleDegrees = 10f,
                neckAngleDegrees = 5f,
                shoulderAngleDegrees = 15f,
                rawHazardScore = 0.5f,
                detectedHazard = null
            )
            val state = debouncer.processFrame(metrics)
            if (t < 4000) {
                assertEquals(HazardState.EVALUATING, state)
            } else {
                assertEquals(HazardState.SAFE, state)
            }
        }

        assertEquals(0, triggeredCount)
    }

    @Test
    fun testSustainedHazardTriggersAlertAfterWindow() {
        var triggeredHazard: HazardType? = null
        var triggeredPeakAngle = 0f
        var triggeredDuration = 0f

        val debouncer = HazardDebouncer(
            windowDurationMs = 5000L,
            cooldownDurationMs = 15000L,
            riskThreshold = 1.4f
        ) { hazard, peakAngle, duration ->
            triggeredHazard = hazard
            triggeredPeakAngle = peakAngle
            triggeredDuration = duration
        }

        // Send 4.2 seconds of severe hazard frames (1 frame every 200ms)
        var lastState: HazardState = HazardState.EVALUATING
        for (t in 0..4200 step 200) {
            val metrics = PostureMetrics(
                timestampMs = t.toLong(),
                trunkAngleDegrees = 65f,
                neckAngleDegrees = 10f,
                shoulderAngleDegrees = 20f,
                rawHazardScore = 2.0f,
                detectedHazard = HazardType.TRUNK_FLEXION_SEVERE
            )
            lastState = debouncer.processFrame(metrics)
        }

        assertEquals(HazardState.COOLDOWN, lastState)
        assertEquals(HazardType.TRUNK_FLEXION_SEVERE, triggeredHazard)
        assertTrue(triggeredPeakAngle >= 65f)
        assertTrue(triggeredDuration >= 4.0f)
    }

    @Test
    fun testCooldownPreventsImmediateRetrigger() {
        var triggerCount = 0
        val debouncer = HazardDebouncer(
            windowDurationMs = 5000L,
            cooldownDurationMs = 15000L,
            riskThreshold = 1.4f
        ) { _, _, _ -> triggerCount++ }

        // Trigger first hazard
        for (t in 0..4200 step 200) {
            debouncer.processFrame(
                PostureMetrics(
                    timestampMs = t.toLong(),
                    trunkAngleDegrees = 65f,
                    neckAngleDegrees = 10f,
                    shoulderAngleDegrees = 20f,
                    rawHazardScore = 2.0f,
                    detectedHazard = HazardType.TRUNK_FLEXION_SEVERE
                )
            )
        }
        assertEquals(1, triggerCount)

        // Continue sending hazard frames during cooldown (within 15 seconds from trigger at 4200ms)
        for (t in 4400..18000 step 200) {
            val state = debouncer.processFrame(
                PostureMetrics(
                    timestampMs = t.toLong(),
                    trunkAngleDegrees = 70f,
                    neckAngleDegrees = 10f,
                    shoulderAngleDegrees = 20f,
                    rawHazardScore = 2.0f,
                    detectedHazard = HazardType.TRUNK_FLEXION_SEVERE
                )
            )
            assertEquals(HazardState.COOLDOWN, state)
        }
        assertEquals(1, triggerCount)
    }

    @Test
    fun testTransientNoiseSpikesDoNotTriggerHazard() {
        var triggeredCount = 0
        val debouncer = HazardDebouncer(
            windowDurationMs = 5000L,
            cooldownDurationMs = 15000L,
            riskThreshold = 1.4f
        ) { _, _, _ -> triggeredCount++ }

        // 5 seconds of mostly safe frames with isolated random spikes
        for (t in 0..5000 step 200) {
            val isSpike = (t == 1000 || t == 3000)
            val metrics = PostureMetrics(
                timestampMs = t.toLong(),
                trunkAngleDegrees = if (isSpike) 75f else 12f,
                neckAngleDegrees = 5f,
                shoulderAngleDegrees = 10f,
                rawHazardScore = if (isSpike) 2.5f else 0.4f,
                detectedHazard = if (isSpike) HazardType.TRUNK_FLEXION_SEVERE else null
            )
            debouncer.processFrame(metrics)
        }

        // Median score should remain well below 1.4 threshold
        assertEquals(0, triggeredCount)
    }
}
