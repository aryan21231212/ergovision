package com.ergovision.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CenterFocusStrong
import androidx.compose.material.icons.rounded.Sensors
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ergovision.app.data.model.HazardState
import com.ergovision.app.data.model.PostureMetrics
import com.ergovision.app.ui.theme.BackgroundDark
import com.ergovision.app.ui.theme.BorderHairline
import com.ergovision.app.ui.theme.BorderSubtle
import com.ergovision.app.ui.theme.BrandCyan
import com.ergovision.app.ui.theme.HazardGreen
import com.ergovision.app.ui.theme.HazardRed
import com.ergovision.app.ui.theme.HazardYellow
import com.ergovision.app.ui.theme.SurfaceDark
import com.ergovision.app.ui.theme.SurfaceSteel
import com.ergovision.app.ui.theme.TextPrimary
import com.ergovision.app.ui.theme.TextSecondary
import com.ergovision.app.ui.theme.TextTertiary
import kotlin.math.cos
import kotlin.math.sin

/**
 * High-Precision Biomechanical Inclinometer Dial for Pocket / Belt-Clip IMU Mode.
 * Replaces the power-hungry camera pipeline with a 6-axis hardware attitude instrument.
 * Calibrated in degrees against factory ergonomic thresholds:
 * - Green (0° - 20°): Neutral trunk alignment
 * - Yellow (20° - 60°): Mild trunk flexion warning
 * - Red (> 60°): Severe spinal flexion hazard
 */
@Composable
fun PocketModeView(
    metrics: PostureMetrics,
    state: HazardState,
    onCalibrateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dialColor = when {
        metrics.trunkAngleDegrees > 60f -> HazardRed
        metrics.trunkAngleDegrees > 20f -> HazardYellow
        else -> HazardGreen
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Sensor Status Pill
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(SurfaceSteel)
                    .border(1.dp, BorderHairline, RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Sensors,
                    contentDescription = "IMU Sensor",
                    tint = BrandCyan,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "6-AXIS IMU STREAMING",
                    color = BrandCyan,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Aircraft Attitude Inclinometer Dial
            Box(
                modifier = Modifier
                    .size(240.dp),
                contentAlignment = Alignment.Center
            ) {
                // Calibrated Inclinometer Gauge Canvas
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val radius = size.width / 2f - 16.dp.toPx()

                    // Gauge background disc
                    drawCircle(
                        color = SurfaceSteel.copy(alpha = 0.85f),
                        radius = radius,
                        center = center
                    )

                    // Outer calibration ring
                    drawCircle(
                        color = Color(0x30FFFFFF),
                        radius = radius,
                        center = center,
                        style = Stroke(width = 2.dp.toPx())
                    )

                    // Degree tick marks (every 15 degrees from -90° to 90°)
                    for (deg in -90..90 step 15) {
                        val angleRad = Math.toRadians((deg - 90).toDouble())
                        val isMajor = deg % 30 == 0
                        val tickLength = if (isMajor) 12.dp.toPx() else 6.dp.toPx()
                        val tickColor = if (isMajor) Color.White.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.25f)

                        val start = Offset(
                            (center.x + (radius - tickLength) * cos(angleRad)).toFloat(),
                            (center.y + (radius - tickLength) * sin(angleRad)).toFloat()
                        )
                        val end = Offset(
                            (center.x + radius * cos(angleRad)).toFloat(),
                            (center.y + radius * sin(angleRad)).toFloat()
                        )
                        drawLine(
                            color = tickColor,
                            start = start,
                            end = end,
                            strokeWidth = if (isMajor) 2.dp.toPx() else 1.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }

                    // Dynamic live flexion arc
                    val sweepAngle = metrics.trunkAngleDegrees.coerceIn(-90f, 90f)
                    drawArc(
                        color = dialColor,
                        startAngle = -90f,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        topLeft = Offset(center.x - radius + 8.dp.toPx(), center.y - radius + 8.dp.toPx()),
                        size = Size((radius - 8.dp.toPx()) * 2, (radius - 8.dp.toPx()) * 2),
                        style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Artificial horizon reference crossbars
                    val horizonY = (center.y + (sweepAngle * 0.8f)).coerceIn(center.y - radius * 0.6f, center.y + radius * 0.6f)
                    drawLine(
                        color = dialColor.copy(alpha = 0.5f),
                        start = Offset(center.x - radius * 0.5f, horizonY),
                        end = Offset(center.x + radius * 0.5f, horizonY),
                        strokeWidth = 2.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }

                // Dial Monospace Readout
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${metrics.trunkAngleDegrees.toInt()}°",
                        color = TextPrimary,
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "TRUNK TILT",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Precision Sensor Info Capsule
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceSteel)
                    .border(1.dp, BorderHairline, RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Shield,
                    contentDescription = "Privacy Shield",
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Zero camera power & full worker privacy.\nPocket / belt-clip posture tracking active.",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Tactile Baseline Calibration Button
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(BrandCyan.copy(alpha = 0.12f))
                    .border(1.dp, BrandCyan.copy(alpha = 0.40f), RoundedCornerShape(20.dp))
                    .clickable(onClick = onCalibrateClick)
                    .padding(horizontal = 20.dp, vertical = 11.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.CenterFocusStrong,
                    contentDescription = "Calibrate Zero Baseline",
                    tint = BrandCyan,
                    modifier = Modifier.size(17.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Calibrate Neutral Baseline",
                    color = BrandCyan,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.3.sp
                )
            }
        }
    }
}
