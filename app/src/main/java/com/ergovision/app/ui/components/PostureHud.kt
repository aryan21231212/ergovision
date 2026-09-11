package com.ergovision.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ergovision.app.data.model.HazardState
import com.ergovision.app.data.model.Point2D
import com.ergovision.app.data.model.PostureMetrics
import com.ergovision.app.ui.theme.HazardGreen
import com.ergovision.app.ui.theme.HazardRed
import com.ergovision.app.ui.theme.HazardYellow

/**
 * Fully responsive, clean industrial HUD overlay for ErgoVision.
 * Adapts dynamically across compact phones, standard displays, and foldables/tablets.
 */
@Composable
fun PostureHud(
    metrics: PostureMetrics,
    state: HazardState,
    isThermalThrottled: Boolean = false,
    isPocketMode: Boolean = false,
    isAudioMuted: Boolean = false,
    isFrontCamera: Boolean = false,
    onCalibrateClick: () -> Unit = {},
    onDimScreenClick: () -> Unit = {},
    onToggleModeClick: () -> Unit = {},
    onToggleAudioClick: () -> Unit = {},
    onToggleCameraClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        val isCompact = maxWidth < 370.dp
        val horizontalPadding = if (isCompact) 8.dp else 14.dp

        val (rawStatusLabel, statusColor) = when (state) {
            HazardState.SAFE -> "SAFE" to HazardGreen
            HazardState.EVALUATING -> "ANALYZING" to HazardYellow
            HazardState.TRIGGERED -> (metrics.detectedHazard?.name?.replace("_", " ") ?: "HAZARD") to HazardRed
            HazardState.COOLDOWN -> "COOLDOWN" to Color(0xFF94A3B8)
        }

        val statusLabel = if (isCompact && rawStatusLabel.length > 10) {
            when {
                rawStatusLabel.contains("TRUNK") -> "TRUNK HAZARD"
                rawStatusLabel.contains("NECK") -> "NECK HAZARD"
                rawStatusLabel.contains("ARM") -> "ARM REACH"
                else -> "HAZARD"
            }
        } else {
            rawStatusLabel
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar: Status Pill (Left) & Control Icons Capsule (Right)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sleek Status Capsule
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xD90F172A))
                        .border(1.dp, Color(0x30FFFFFF), RoundedCornerShape(24.dp))
                        .padding(horizontal = horizontalPadding, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Status dot
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = statusLabel,
                        color = Color.White,
                        fontSize = if (isCompact) 11.sp else 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    // Subtle Sensor / FPS Indicator
                    Text(
                        text = if (isThermalThrottled) "2 FPS" else if (isPocketMode) "IMU" else "5 FPS",
                        color = if (isThermalThrottled) HazardYellow else Color(0xFF94A3B8),
                        fontSize = if (isCompact) 9.sp else 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Minimalist Action Capsule
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xD90F172A))
                        .border(1.dp, Color(0x30FFFFFF), RoundedCornerShape(24.dp))
                        .padding(horizontal = 3.dp, vertical = 3.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Mode Toggle
                    HudIconButton(
                        icon = if (isPocketMode) "📱" else "📷",
                        isCompact = isCompact,
                        isActive = isPocketMode,
                        onClick = onToggleModeClick
                    )

                    // Camera Flip (Front vs Back)
                    if (!isPocketMode) {
                        HudIconButton(
                            icon = "🔄",
                            isCompact = isCompact,
                            onClick = onToggleCameraClick
                        )
                    }

                    // Calibrate Neutral Posture
                    HudIconButton(
                        icon = "🎯",
                        isCompact = isCompact,
                        onClick = onCalibrateClick
                    )

                    // Audio Mute/Unmute
                    HudIconButton(
                        icon = if (isAudioMuted) "🔇" else "🔊",
                        isCompact = isCompact,
                        onClick = onToggleAudioClick
                    )

                    // OLED Low Power Dimmer
                    HudIconButton(
                        icon = "🌙",
                        isCompact = isCompact,
                        onClick = onDimScreenClick
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Floating Telemetry Glass Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xD90F172A))
                    .border(1.dp, Color(0x25FFFFFF), RoundedCornerShape(16.dp))
                    .padding(vertical = 7.dp, horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val trunkColor = when {
                    metrics.trunkAngleDegrees > 60f -> HazardRed
                    metrics.trunkAngleDegrees > 20f -> HazardYellow
                    else -> HazardGreen
                }

                val neckColor = when {
                    metrics.neckAngleDegrees > 20f -> HazardYellow
                    else -> HazardGreen
                }

                val armColor = when {
                    metrics.shoulderAngleDegrees > 90f -> HazardRed
                    metrics.shoulderAngleDegrees > 60f -> HazardYellow
                    else -> HazardGreen
                }

                val scoreColor = when {
                    metrics.rawHazardScore >= 2.0f -> HazardRed
                    metrics.rawHazardScore >= 1.2f -> HazardYellow
                    else -> HazardGreen
                }

                TelemetryItem("TRUNK", "${metrics.trunkAngleDegrees.toInt()}°", trunkColor, isCompact)
                Box(modifier = Modifier.width(1.dp).height(18.dp).background(Color(0x25FFFFFF)))
                TelemetryItem("NECK", "${metrics.neckAngleDegrees.toInt()}°", neckColor, isCompact)
                Box(modifier = Modifier.width(1.dp).height(18.dp).background(Color(0x25FFFFFF)))
                TelemetryItem("ARM", "${metrics.shoulderAngleDegrees.toInt()}°", armColor, isCompact)
                Box(modifier = Modifier.width(1.dp).height(18.dp).background(Color(0x25FFFFFF)))
                TelemetryItem("REBA", String.format("%.1f", metrics.rawHazardScore), scoreColor, isCompact)
            }
        }
    }
}

@Composable
private fun HudIconButton(
    icon: String,
    isCompact: Boolean,
    isActive: Boolean = false,
    onClick: () -> Unit
) {
    val buttonSize = if (isCompact) 30.dp else 34.dp
    Box(
        modifier = Modifier
            .size(buttonSize)
            .clip(CircleShape)
            .background(if (isActive) Color(0xFF4F46E5) else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text = icon, fontSize = if (isCompact) 12.sp else 14.sp)
    }
}

@Composable
private fun TelemetryItem(
    label: String,
    value: String,
    statusColor: Color,
    isCompact: Boolean
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(if (isCompact) 4.dp else 5.dp)
                    .clip(CircleShape)
                    .background(statusColor)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = label,
                color = Color(0xFF94A3B8),
                fontSize = if (isCompact) 9.sp else 10.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.5.sp
            )
        }
        Spacer(modifier = Modifier.height(1.dp))
        Text(
            text = value,
            color = Color.White,
            fontSize = if (isCompact) 13.sp else 15.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}

/**
 * High-precision Segment-Level Ergonomic Skeleton Canvas.
 * Draws subtle anti-aliased bones with dynamic per-joint risk coloring:
 * - Trunk: Red (>60°), Yellow (>20°), Green (neutral)
 * - Neck: Yellow (>20°), Green (neutral)
 * - Arms: Red (>90°), Yellow (>60°), Green (neutral)
 */
@Composable
fun SkeletonOverlay(
    landmarks: List<Point2D>,
    metrics: PostureMetrics,
    modifier: Modifier = Modifier
) {
    if (landmarks.size < 25) return

    val trunkColor = when {
        metrics.trunkAngleDegrees > 60f -> HazardRed
        metrics.trunkAngleDegrees > 20f -> HazardYellow
        else -> HazardGreen
    }

    val neckColor = when {
        metrics.neckAngleDegrees > 20f -> HazardYellow
        else -> HazardGreen
    }

    val armColor = when {
        metrics.shoulderAngleDegrees > 90f -> HazardRed
        metrics.shoulderAngleDegrees > 60f -> HazardYellow
        else -> HazardGreen
    }

    val neutralColor = HazardGreen

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        fun pt(index: Int): Offset = Offset(landmarks[index].x * w, landmarks[index].y * h)

        fun drawBone(i1: Int, i2: Int, color: Color) {
            if (i1 < landmarks.size && i2 < landmarks.size) {
                // Semi-transparent halo for smooth, anti-aliased visual presentation
                drawLine(
                    color = color.copy(alpha = 0.35f),
                    start = pt(i1),
                    end = pt(i2),
                    strokeWidth = 9f,
                    cap = StrokeCap.Round
                )
                // Crisp inner bone line
                drawLine(
                    color = color,
                    start = pt(i1),
                    end = pt(i2),
                    strokeWidth = 5f,
                    cap = StrokeCap.Round
                )
            }
        }

        // Draw segmented bones
        drawBone(11, 12, neutralColor) // Shoulders
        drawBone(11, 23, trunkColor)   // Left torso
        drawBone(12, 24, trunkColor)   // Right torso
        drawBone(23, 24, neutralColor) // Hips
        drawBone(11, 13, armColor)     // Left upper arm
        drawBone(13, 15, armColor)     // Left forearm
        drawBone(12, 14, armColor)     // Right upper arm
        drawBone(14, 16, armColor)     // Right forearm
        drawBone(11, 7, neckColor)     // Left neck
        drawBone(12, 8, neckColor)     // Right neck

        // Draw precise joint nodes with dual-ring halo
        val joints = listOf(11, 12, 13, 14, 15, 16, 23, 24, 7, 8)
        for (idx in joints) {
            if (idx < landmarks.size) {
                val center = pt(idx)
                // Outer halo
                drawCircle(
                    color = Color.Black.copy(alpha = 0.5f),
                    radius = 7f,
                    center = center
                )
                // Inner bright point
                drawCircle(
                    color = Color.White,
                    radius = 4f,
                    center = center
                )
            }
        }
    }
}
