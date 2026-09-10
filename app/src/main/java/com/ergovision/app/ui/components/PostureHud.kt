package com.ergovision.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ergovision.app.data.model.HazardState
import com.ergovision.app.data.model.Point2D
import com.ergovision.app.data.model.PostureMetrics
import com.ergovision.app.ui.theme.BrandCyan
import com.ergovision.app.ui.theme.HazardGreen
import com.ergovision.app.ui.theme.HazardRed
import com.ergovision.app.ui.theme.HazardYellow

@Composable
fun PostureHud(
    metrics: PostureMetrics,
    state: HazardState,
    isThermalThrottled: Boolean = false,
    isPocketMode: Boolean = false,
    eventCount: Int = 0,
    onCalibrateClick: () -> Unit = {},
    onLogsClick: () -> Unit = {},
    onDimScreenClick: () -> Unit = {},
    onToggleModeClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val stateColor = when (state) {
        HazardState.SAFE -> HazardGreen
        HazardState.EVALUATING -> HazardYellow
        HazardState.TRIGGERED -> HazardRed
        HazardState.COOLDOWN -> Color.Gray
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Status Banner
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(stateColor.copy(alpha = 0.88f), RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "STATUS: ${state.name}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                if (metrics.detectedHazard != null) {
                    Text(
                        text = metrics.detectedHazard.name,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Thermal / FPS Indicator
                Box(
                    modifier = Modifier
                        .background(
                            if (isThermalThrottled) HazardYellow.copy(alpha = 0.3f) else Color.Black.copy(alpha = 0.25f),
                            RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = if (isThermalThrottled) "⚡ 2 FPS" else if (isPocketMode) "📱 IMU" else "📷 5 FPS",
                        color = if (isThermalThrottled) HazardYellow else Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Mode Toggle Button (Camera Mount vs Pocket/IMU)
                Box(
                    modifier = Modifier
                        .background(if (isPocketMode) Color(0xFF6366F1) else Color(0xFF334155), RoundedCornerShape(4.dp))
                        .clickable { onToggleModeClick() }
                        .padding(horizontal = 7.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isPocketMode) "Pocket" else "Mount",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Calibrate Button
                Box(
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.35f), RoundedCornerShape(4.dp))
                        .clickable { onCalibrateClick() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Calibrate",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Logs Counter Button
                Box(
                    modifier = Modifier
                        .background(BrandCyan.copy(alpha = 0.9f), RoundedCornerShape(4.dp))
                        .clickable { onLogsClick() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Logs ($eventCount)",
                        color = Color.Black,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Dim Button (OLED Battery Saver)
                Box(
                    modifier = Modifier
                        .background(Color.DarkGray.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                        .clickable { onDimScreenClick() }
                        .padding(horizontal = 7.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Dim",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Angular Metrics Card
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xBB1E293B), RoundedCornerShape(8.dp))
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            MetricItem("TRUNK", "${metrics.trunkAngleDegrees.toInt()}°")
            MetricItem("NECK", "${metrics.neckAngleDegrees.toInt()}°")
            MetricItem("ARM", "${metrics.shoulderAngleDegrees.toInt()}°")
            MetricItem("SCORE", String.format("%.1f", metrics.rawHazardScore))
        }
    }
}

@Composable
fun SkeletonOverlay(
    landmarks: List<Point2D>,
    hazardActive: Boolean,
    modifier: Modifier = Modifier
) {
    if (landmarks.size < 25) return

    val boneColor = if (hazardActive) HazardRed else HazardGreen

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        fun pt(index: Int): Offset = Offset(landmarks[index].x * w, landmarks[index].y * h)

        fun drawBone(i1: Int, i2: Int) {
            if (i1 < landmarks.size && i2 < landmarks.size) {
                drawLine(
                    color = boneColor,
                    start = pt(i1),
                    end = pt(i2),
                    strokeWidth = 6f
                )
            }
        }

        // Connect key posture landmarks
        drawBone(11, 12) // Shoulders
        drawBone(11, 23) // Left torso
        drawBone(12, 24) // Right torso
        drawBone(23, 24) // Hips
        drawBone(11, 13) // Left upper arm
        drawBone(13, 15) // Left forearm
        drawBone(12, 14) // Right upper arm
        drawBone(14, 16) // Right forearm
        drawBone(11, 7)  // Left neck
        drawBone(12, 8)  // Right neck

        // Draw joint points
        val joints = listOf(11, 12, 13, 14, 15, 16, 23, 24, 7, 8)
        for (idx in joints) {
            if (idx < landmarks.size) {
                drawCircle(
                    color = Color.White,
                    radius = 8f,
                    center = pt(idx)
                )
            }
        }
    }
}

@Composable
private fun MetricItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, color = Color.LightGray, fontSize = 11.sp)
        Text(text = value, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}
