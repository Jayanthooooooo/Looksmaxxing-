package com.example.ui.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.ScanResult
import com.example.ui.components.*
import com.example.ui.theme.LumoraColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProgressScreen(
    scans: List<ScanResult>,
    onScanClick: (String) -> Unit,
    onCompareClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedPeriod by remember { mutableStateOf("12wk") }
    val dateFormat = remember { SimpleDateFormat("MMM d", Locale.US) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
            .testTag("progress_screen")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Progress & Trends",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
            if (scans.size >= 2) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(LumoraColors.GlassBackground)
                        .clickable { onCompareClick() }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Compare",
                        style = MaterialTheme.typography.labelMedium,
                        color = LumoraColors.SecondaryCyan
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Period filter chips
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("4wk" to "4 Weeks", "12wk" to "12 Weeks", "all" to "All time").forEach { (key, label) ->
                val isSelected = selectedPeriod == key
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (isSelected) LumoraColors.PrimaryGradient else Brush.linearGradient(listOf(Color.White.copy(alpha = 0.05f), Color.White.copy(alpha = 0.05f))))
                        .clickable { selectedPeriod = key }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (scans.isEmpty()) {
            EmptyState(
                title = "No scans yet",
                message = "Your progress over time will appear here once you take your first scan."
            )
        } else {
            // Symmetry trend
            val symmetryPoints = scans.reversed().map {
                TrendPoint(dateFormat.format(Date(it.createdAt)), (it.indices["symmetry"] ?: 70).toFloat())
            }
            TrendChart(title = "Symmetry Index", points = symmetryPoints)

            Spacer(modifier = Modifier.height(16.dp))

            // Proportions trend
            val proportionPoints = scans.reversed().map {
                TrendPoint(dateFormat.format(Date(it.createdAt)), (it.indices["proportions"] ?: 70).toFloat())
            }
            TrendChart(title = "Proportions Index", points = proportionPoints)

            Spacer(modifier = Modifier.height(16.dp))

            // Clarity trend
            val clarityPoints = scans.reversed().map {
                TrendPoint(dateFormat.format(Date(it.createdAt)), (it.indices["clarity"] ?: 68).toFloat())
            }
            TrendChart(title = "Skin Clarity Index", points = clarityPoints, lineColor = LumoraColors.MagentaPrimary)

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Scan History (${scans.size})",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = 0.7f),
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                scans.forEach { scan ->
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onScanClick(scan.id) },
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = SimpleDateFormat("EEEE, MMM d", Locale.US).format(Date(scan.createdAt)),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${scan.kind.replaceFirstChar { it.uppercase() }} · Quality ${scan.qualityReport.score}%",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.5f)
                                )
                            }
                            Text(
                                text = "View →",
                                style = MaterialTheme.typography.labelMedium,
                                color = LumoraColors.SecondaryCyan
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(40.dp))
    }
}
