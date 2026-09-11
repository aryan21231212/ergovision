package com.ergovision.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.History
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ergovision.app.ui.theme.BorderHairline
import com.ergovision.app.ui.theme.BorderSubtle
import com.ergovision.app.ui.theme.BrandCyan
import com.ergovision.app.ui.theme.HazardYellow
import com.ergovision.app.ui.theme.SurfaceDark
import com.ergovision.app.ui.theme.SurfaceSteel
import com.ergovision.app.ui.theme.TextPrimary
import com.ergovision.app.ui.theme.TextSecondary

/**
 * Precision Cyber-Industrial Floating Dock for ErgoVision.
 * Features tactile segmented pills, vector iconography, and adaptive layout:
 * - On-Device LiteRT-LM GenAI EHS synthesis trigger
 * - Room SQLite Incident Log audit sheet inspector
 * - Offline CSV telemetry export
 * - Real-time ergonomic hazard simulator
 */
@Composable
fun BottomControlDock(
    eventCount: Int,
    onEhsAuditClick: () -> Unit,
    onLogsClick: () -> Unit,
    onExportCsvClick: () -> Unit,
    onSimulateHazardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        val isCompact = maxWidth < 370.dp
        val buttonSpacing = if (isCompact) 4.dp else 6.dp
        val itemHorizontalPadding = if (isCompact) 9.dp else 13.dp
        val iconSize = if (isCompact) 15.dp else 16.dp

        Row(
            modifier = Modifier
                .widthIn(max = 540.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(SurfaceSteel.copy(alpha = 0.92f))
                .border(1.dp, BorderSubtle, RoundedCornerShape(32.dp))
                .padding(horizontal = 6.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.spacedBy(buttonSpacing),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Primary Action: EHS Audit (LiteRT-LM GenAI Synthesis)
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(BrandCyan)
                    .clickable(onClick = onEhsAuditClick)
                    .padding(horizontal = itemHorizontalPadding, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.AutoAwesome,
                    contentDescription = "EHS Audit Synthesis",
                    tint = Color.Black,
                    modifier = Modifier.size(iconSize)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = if (isCompact) "Audit" else "EHS Audit",
                    color = Color.Black,
                    fontSize = if (isCompact) 11.sp else 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.2.sp
                )
            }

            // Secondary Action: Logs Inspector with Numeric Badge
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(SurfaceDark)
                    .border(1.dp, BorderHairline, RoundedCornerShape(24.dp))
                    .clickable(onClick = onLogsClick)
                    .padding(horizontal = itemHorizontalPadding, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.History,
                    contentDescription = "Incident Logs",
                    tint = TextPrimary,
                    modifier = Modifier.size(iconSize)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "Logs",
                    color = TextPrimary,
                    fontSize = if (isCompact) 11.sp else 12.sp,
                    fontWeight = FontWeight.SemiBold
                )

                if (eventCount > 0) {
                    Spacer(modifier = Modifier.width(5.dp))
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color(0x28FFFFFF))
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "$eventCount",
                            color = BrandCyan,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Action: Export CSV
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(SurfaceDark.copy(alpha = 0.7f))
                    .border(1.dp, BorderHairline, RoundedCornerShape(24.dp))
                    .clickable(onClick = onExportCsvClick)
                    .padding(horizontal = itemHorizontalPadding, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.FileDownload,
                    contentDescription = "Export CSV",
                    tint = TextSecondary,
                    modifier = Modifier.size(iconSize)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "CSV",
                    color = TextSecondary,
                    fontSize = if (isCompact) 11.sp else 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Quick Demo Hazard Simulation Pill
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(HazardYellow.copy(alpha = 0.12f))
                    .border(1.dp, HazardYellow.copy(alpha = 0.35f), RoundedCornerShape(24.dp))
                    .clickable(onClick = onSimulateHazardClick)
                    .padding(horizontal = if (isCompact) 8.dp else 11.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Bolt,
                    contentDescription = "Simulate Hazard",
                    tint = HazardYellow,
                    modifier = Modifier.size(iconSize)
                )
                if (!isCompact) {
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "Test",
                        color = HazardYellow,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
