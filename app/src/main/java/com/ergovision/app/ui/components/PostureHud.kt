package com.ergovision.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CenterFocusStrong
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.FlipCameraAndroid
import androidx.compose.material.icons.rounded.Smartphone
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material.icons.rounded.VolumeOff
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ergovision.app.data.model.HazardState
import com.ergovision.app.data.model.Point2D
import com.ergovision.app.data.model.PostureMetrics
import com.ergovision.app.ui.theme.BorderHairline
import com.ergovision.app.ui.theme.BorderSubtle
import com.ergovision.app.ui.theme.BrandCyan
import com.ergovision.app.ui.theme.HazardCooldown
import com.ergovision.app.ui.theme.HazardGreen
import com.ergovision.app.ui.theme.HazardRed
import com.ergovision.app.ui.theme.HazardYellow
import com.ergovision.app.ui.theme.SurfaceSteel
import com.ergovision.app.ui.theme.TextPrimary
import com.ergovision.app.ui.theme.TextSecondary
import com.ergovision.app.ui.theme.TextTertiary

/**
 * Precision Cyber-Industrial HUD overlay for ErgoVision.
 * Responsive across compact smartphones, factory rugged tablets, and foldable displays.
 * Designed according to anti-slop / industrial biometrics instrument principles:
 * - High-density tabular telemetry readouts
 * - Scalable vector iconography (zero emoji slop)
 * - Calibrated risk pips and rate badge indicators
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
        val horizontalPadding = if (isCompact) 10.dp else 14.dp

        val (rawStatusLabel, statusColor) = when (state) {
            HazardState.SAFE -> "SAFE" to HazardGreen
            HazardState.EVALUATING -> "ANALYZING" to HazardYellow
            HazardState.TRIGGERED -> (metrics.detectedHazard?.name?.replace("_", " ") ?: "HAZARD") to HazardRed
            HazardState.COOLDOWN -> "COOLDOWN" to HazardCooldown
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
            // Top Bar: Status Telemetry Pill (Left) & Control Icons Capsule (Right)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sleek Instrument Status Capsule
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(SurfaceSteel.copy(alpha = 0.90f))
                        .border(1.dp, BorderSubtle, RoundedCornerShape(24.dp))
                        .padding(horizontal = horizontalPadding, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Pulsing Status Dot
                    Box(contentAlignment = Alignment.Center) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(statusColor.copy(alpha = 0.25f))
                        )
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(statusColor)
                        )
                    }

                    Spacer(modifier = Modifier.width(7.dp))

                    Text(
                        text = statusLabel,
                        color = TextPrimary,
                        fontSize = if (isCompact) 11.sp else 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Sensor Rate Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0x22FFFFFF))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (isThermalThrottled) "2 FPS" else if (isPocketMode) "IMU" else "5 FPS",
                            color = if (isThermalThrottled) HazardYellow else BrandCyan,
                            fontSize = if (isCompact) 9.sp else 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Vector Action Capsule
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(SurfaceSteel.copy(alpha = 0.90f))
                        .border(1.dp, BorderSubtle, RoundedCornerShape(24.dp))
                        .padding(horizontal = 4.dp, vertical = 3.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Sensor Mode Toggle (Camera vs Pocket IMU)
                    HudIconButton(
                        imageVector = if (isPocketMode) Icons.Rounded.Smartphone else Icons.Rounded.Videocam,
                        contentDescription = if (isPocketMode) "Switch to Camera" else "Switch to Pocket IMU",
                        isCompact = isCompact,
                        isActive = isPocketMode,
                        onClick = onToggleModeClick
                    )

                    // Camera Flip (Front vs Back)
                    if (!isPocketMode) {
                        HudIconButton(
                            imageVector = Icons.Rounded.FlipCameraAndroid,
                            contentDescription = "Flip Camera",
                            isCompact = isCompact,
                            isActive = isFrontCamera,
                            onClick = onToggleCameraClick
                        )
                    }

                    // Calibrate Neutral Posture Baseline
                    HudIconButton(
                        imageVector = Icons.Rounded.CenterFocusStrong,
                        contentDescription = "Calibrate Neutral Posture",
                        isCompact = isCompact,
                        onClick = onCalibrateClick
                    )

                    // Audio Alert Mute/Unmute
                    HudIconButton(
                        imageVector = if (isAudioMuted) Icons.Rounded.VolumeOff else Icons.Rounded.VolumeUp,
                        contentDescription = if (isAudioMuted) "Unmute Audio" else "Mute Audio",
                        isCompact = isCompact,
                        tint = if (isAudioMuted) TextTertiary else TextPrimary,
                        onClick = onToggleAudioClick
                    )

                    // OLED Low Power Dimmer
                    HudIconButton(
                        imageVector = Icons.Rounded.DarkMode,
                        contentDescription = "OLED Low-Power Mode",
                        isCompact = isCompact,
                        onClick = onDimScreenClick
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Floating Precision Telemetry Glass Strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceSteel.copy(alpha = 0.90f))
                    .border(1.dp, BorderHairline, RoundedCornerShape(16.dp))
                    .padding(vertical = 7.dp, horizontal = 10.dp),
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
                Box(modifier = Modifier.width(1.dp).height(18.dp).background(BorderHairline))
                TelemetryItem("NECK", "${metrics.neckAngleDegrees.toInt()}°", neckColor, isCompact)
                Box(modifier = Modifier.width(1.dp).height(18.dp).background(BorderHairline))
                TelemetryItem("ARM", "${metrics.shoulderAngleDegrees.toInt()}°", armColor, isCompact)
                Box(modifier = Modifier.width(1.dp).height(18.dp).background(BorderHairline))
                TelemetryItem("REBA", String.format("%.1f", metrics.rawHazardScore), scoreColor, isCompact)
            }
        }
    }
}

@Composable
private fun HudIconButton(
    imageVector: ImageVector,
    contentDescription: String,
    isCompact: Boolean,
    isActive: Boolean = false,
    tint: Color = TextPrimary,
    onClick: () -> Unit
) {
    val buttonSize = if (isCompact) 30.dp else 34.dp
    val iconSize = if (isCompact) 16.dp else 18.dp
    Box(
        modifier = Modifier
            .size(buttonSize)
            .clip(CircleShape)
            .background(if (isActive) BrandCyan.copy(alpha = 0.22f) else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = contentDescription,
            tint = if (isActive) BrandCyan else tint,
            modifier = Modifier.size(iconSize)
        )
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
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                color = TextSecondary,
                fontSize = if (isCompact) 9.sp else 10.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.6.sp
            )
        }
        Spacer(modifier = Modifier.height(1.dp))
        Text(
            text = value,
            color = TextPrimary,
            fontSize = if (isCompact) 13.sp else 15.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}

/**
 * Medical-Grade Biometric Skeleton Reticle Canvas.
 * Renders anti-aliased luminous bones with dynamic segment risk coloring:
 * - Trunk & Spine: Red (>60°), Yellow (>20°), Green (neutral)
 * - Neck: Yellow (>20°), Green (neutral)
 * - Arms: Red (>90°), Yellow (>60°), Green (neutral)
 * - Precision reticles on key ergonomic joint pivots
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
                // Outer luminous glow line
                drawLine(
                    color = color.copy(alpha = 0.28f),
                    start = pt(i1),
                    end = pt(i2),
                    strokeWidth = 8f,
                    cap = StrokeCap.Round
                )
                // Inner crisp core bone line
                drawLine(
                    color = color,
                    start = pt(i1),
                    end = pt(i2),
                    strokeWidth = 3.5f,
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

        // Ergonomic Central Spine Axis (Shoulder midpoint to hip midpoint)
        if (11 < landmarks.size && 12 < landmarks.size && 23 < landmarks.size && 24 < landmarks.size) {
            val shoulderMid = Offset((pt(11).x + pt(12).x) / 2f, (pt(11).y + pt(12).y) / 2f)
            val hipMid = Offset((pt(23).x + pt(24).x) / 2f, (pt(23).y + pt(24).y) / 2f)
            drawLine(
                color = trunkColor.copy(alpha = 0.40f),
                start = shoulderMid,
                end = hipMid,
                strokeWidth = 4f,
                cap = StrokeCap.Round
            )
        }

        // Draw precision joint reticles (dual halo + core pip)
        val joints = listOf(11, 12, 13, 14, 15, 16, 23, 24, 7, 8)
        for (idx in joints) {
            if (idx < landmarks.size) {
                val center = pt(idx)
                // Outer translucent halo
                drawCircle(
                    color = Color.Black.copy(alpha = 0.45f),
                    radius = 8f,
                    center = center
                )
                // High-contrast ring
                drawCircle(
                    color = Color.White.copy(alpha = 0.85f),
                    radius = 5.5f,
                    center = center,
                    style = Stroke(width = 1.5f)
                )
                // Center target core
                drawCircle(
                    color = Color.White,
                    radius = 2.5f,
                    center = center
                )
            }
        }
    }
}
