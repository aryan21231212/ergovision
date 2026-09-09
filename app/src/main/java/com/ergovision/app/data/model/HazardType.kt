package com.ergovision.app.data.model

/**
 * Supported ergonomic hazard types and their instantaneous spoken template alert sentences.
 */
enum class HazardType(val severity: Int, val defaultSpokenAlert: String) {
    TRUNK_FLEXION_MODERATE(1, "Straighten your back."),
    TRUNK_FLEXION_SEVERE(2, "Warning: Severe back strain. Stand upright."),
    NECK_FLEXION(1, "Lift your chin and relax your neck."),
    SHOULDER_ABDUCTION(2, "Lower your elbows below shoulder level."),
    COMPOUND_STRAIN(3, "Critical posture alert: Re-align back and neck.")
}
