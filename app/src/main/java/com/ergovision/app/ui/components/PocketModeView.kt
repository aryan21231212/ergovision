package com.ergovision.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ergovision.app.data.model.HazardState
import com.ergovision.app.data.model.PostureMetrics
import com.ergovision.app.ui.theme.BrandCyan
import com.ergovision.app.ui.theme.HazardGreen
import com.ergovision.app.ui.theme.HazardRed
import com.ergovision.app.ui.theme.HazardYellow

/**
 * Minimalist, high-tech full-screen display for Pocket / Belt-Clip IMU Mode.
 * Replaces camera preview with a low-power biomechanical sensor dial.
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
            .background(Color(0xFF070B14)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Central Biomechanical Sensor Dial
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .clip(CircleShape)
                    .background(Color(0x1AFFFFFF))
                    .border(3.dp, dialColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${metrics.trunkAngleDegrees.toInt()}°",
                        color = Color.White,
                        fontSize = 54.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "TRUNK TILT",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Mode Explanatory Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0x22FFFFFF))
                    .border(1.dp, Color(0x18FFFFFF), RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(
                    text = "📱 Pocket / Belt-Clip Sensor Active\nCamera sensor suspended to preserve power & privacy.",
                    color = Color(0xFFCBD5E1),
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Quick Calibration Button
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(BrandCyan.copy(alpha = 0.15f))
                    .border(1.dp, BrandCyan.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                    .clickable(onClick = onCalibrateClick)
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "🎯", fontSize = 14.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Calibrate Neutral Baseline",
                    color = BrandCyan,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
