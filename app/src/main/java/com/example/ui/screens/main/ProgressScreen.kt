package com.example.ui.screens.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.domain.model.ScanResult
import com.example.ui.components.EmptyState
import com.example.ui.components.LumoraTopBar
import com.example.ui.components.SecondaryButton
import com.example.ui.components.SectionHeader
import com.example.ui.components.TrendChart
import com.example.ui.components.TrendPoint
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
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            if (scans.size >= 2) {
                SecondaryButton(
                    text = "Compare",
                    onClick = onCompareClick,
                    modifier = Modifier.height(40.dp),
                    testTag = "progress_compare_button"
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Period filter chips
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("4wk" to "4 Weeks", "12wk" to "12 Weeks", "all" to "All time").forEach { (key, label) ->
                FilterChip(
                    selected = selectedPeriod == key,
                    onClick = { selectedPeriod = key },
                    label = { Text(label) }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

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
            TrendChart(title = "Skin Clarity Index", points = clarityPoints)

            Spacer(modifier = Modifier.height(24.dp))

            SectionHeader(title = "Scan History (${scans.size})")
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                scans.forEach { scan ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onScanClick(scan.id) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = SimpleDateFormat("EEEE, MMM d, yyyy", Locale.US).format(Date(scan.createdAt)),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${scan.kind.replaceFirstChar { it.uppercase() }} scan · Quality ${scan.qualityReport.score}/100",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "View →",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}
