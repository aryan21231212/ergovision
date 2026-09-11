package com.ergovision.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ergovision.app.ui.theme.BrandCyan

/**
 * Responsive, floating glassmorphic control dock.
 * Adapts padding, font sizes, and button spacing for all device form factors.
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
        val buttonSpacing = if (isCompact) 4.dp else 8.dp
        val itemHorizontalPadding = if (isCompact) 9.dp else 13.dp

        Row(
            modifier = Modifier
                .widthIn(max = 540.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(Color(0xD90F172A))
                .border(1.dp, Color(0x30FFFFFF), RoundedCornerShape(32.dp))
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
                Text(text = "✨", fontSize = if (isCompact) 12.sp else 13.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isCompact) "Audit" else "EHS Audit",
                    color = Color.Black,
                    fontSize = if (isCompact) 11.sp else 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Secondary Action: Logs Inspector
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0x28FFFFFF))
                    .clickable(onClick = onLogsClick)
                    .padding(horizontal = itemHorizontalPadding, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "📋", fontSize = if (isCompact) 11.sp else 12.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (eventCount > 0) "Logs ($eventCount)" else "Logs",
                    color = Color.White,
                    fontSize = if (isCompact) 11.sp else 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Action: Export CSV
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0x1AFFFFFF))
                    .clickable(onClick = onExportCsvClick)
                    .padding(horizontal = itemHorizontalPadding, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "📤", fontSize = if (isCompact) 11.sp else 12.sp)
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "CSV",
                    color = Color(0xFFE2E8F0),
                    fontSize = if (isCompact) 11.sp else 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Quick Demo Hazard Simulation Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0x15FFFFFF))
                    .clickable(onClick = onSimulateHazardClick)
                    .padding(horizontal = if (isCompact) 8.dp else 11.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isCompact) "⚡" else "⚡ Test",
                    color = Color(0xFF94A3B8),
                    fontSize = if (isCompact) 11.sp else 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
