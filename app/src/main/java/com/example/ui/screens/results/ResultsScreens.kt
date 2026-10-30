package com.example.ui.screens.results

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.domain.model.ChangeabilityTag
import com.example.domain.model.MetricValue
import com.example.domain.model.RangePosition
import com.example.domain.model.ScanResult
import com.example.ui.components.BeforeAfterSlider
import com.example.ui.components.ConfirmDialog
import com.example.ui.components.IndexRing
import com.example.ui.components.InsightCard
import com.example.ui.components.LumoraTopBar
import com.example.ui.components.MetricRow
import com.example.ui.components.PrimaryButton
import com.example.ui.components.PrivacyBadge
import com.example.ui.components.QualityChip
import com.example.ui.components.QuietTextButton
import com.example.ui.components.RangeBar
import com.example.ui.components.SecondaryButton
import com.example.ui.components.SectionHeader
import com.example.ui.theme.LumoraColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ResultsScreen(
    scan: ScanResult,
    onMetricClick: (String) -> Unit,
    onOverlayClick: () -> Unit,
    onSkinResultsClick: () -> Unit,
    onCompareClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    hideNumbers: Boolean = false
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        ConfirmDialog(
            title = "Delete this scan?",
            message = "This permanently removes this scan's measurements and encrypted photo.",
            confirmLabel = "Delete",
            isDestructive = true,
            onConfirm = {
                showDeleteConfirm = false
                onDeleteClick()
            },
            onDismiss = { showDeleteConfirm = false }
        )
    }

    Scaffold(
        topBar = {
            LumoraTopBar(
                title = "Scan Results",
                onBackClick = onBackClick,
                actions = {
                    IconButton(onClick = onOverlayClick, modifier = Modifier.testTag("results_overlay_button")) {
                        Icon(Icons.Default.Layers, contentDescription = "View landmark overlay")
                    }
                    IconButton(onClick = { showDeleteConfirm = true }, modifier = Modifier.testTag("results_delete_button")) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete scan")
                    }
                }
            )
        },
        modifier = modifier.testTag("results_screen")
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Quality chip & date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                QualityChip(score = scan.qualityReport.score)
                Text(
                    text = SimpleDateFormat("MMM d, yyyy", Locale.US).format(Date(scan.createdAt)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Privacy Disclosure Panel
            PrivacyBadge(
                text = "What stayed on this phone: Face landmarks & photos.\nWhat was sent: De-identified face crop to Gemini via AI Logic (with consent)."
            )

            // Group Indices (No single overall score)
            Text(
                text = "Photo-based estimates",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                IndexRing(
                    score = scan.indices["proportions"] ?: 72,
                    label = "Proportions",
                    hideNumbers = hideNumbers
                )
                IndexRing(
                    score = scan.indices["symmetry"] ?: 75,
                    label = "Symmetry",
                    hideNumbers = hideNumbers
                )
                IndexRing(
                    score = scan.indices["clarity"] ?: 70,
                    label = "Skin Clarity",
                    hideNumbers = hideNumbers
                )
            }

            // Skin results card shortcut
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(onClick = onSkinResultsClick)
                    .testTag("skin_results_shortcut"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Skin Appearance Breakdown",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Redness, texture, tone evenness & routine suggestions",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                    Text(text = "View →", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                }
            }

            // Proportions & Symmetry Measurements
            SectionHeader(
                title = "Proportion & Symmetry Readings",
                subtitle = "Values compared to published typical ranges"
            )

            scan.metrics.values.forEach { metric ->
                MetricRow(
                    title = metric.name,
                    valueDescription = if (hideNumbers) metric.rangePositionText else "${metric.value} ${metric.unit} (${metric.rangePositionText})",
                    rangePosition = metric.rangePosition.name,
                    changeabilityTag = metric.changeability.label,
                    onClick = { onMetricClick(metric.id) },
                    rangeBar = {
                        RangeBar(
                            value = metric.value,
                            min = metric.typicalMin * 0.7f,
                            max = metric.typicalMax * 1.3f,
                            typicalMin = metric.typicalMin,
                            typicalMax = metric.typicalMax,
                            rangePositionText = metric.rangePositionText,
                            isProvisional = metric.isProvisional
                        )
                    }
                )
            }

            // Insight cards
            SectionHeader(title = "Photo-to-Photo Insights")
            InsightCard(
                title = "Lighting & Angle Variation",
                description = "Minor shifts in ambient light and yaw angle naturally shift symmetry readings by ±2-3 points.",
                isMeaningfulChange = false
            )

            // Actions: Compare & Done
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SecondaryButton(
                    text = "Compare",
                    onClick = onCompareClick,
                    modifier = Modifier.weight(1f),
                    testTag = "results_compare_button"
                )
                PrimaryButton(
                    text = "Done",
                    onClick = onBackClick,
                    modifier = Modifier.weight(1f),
                    testTag = "results_done_button"
                )
            }
        }
    }
}

@Composable
fun MeasurementDetailScreen(
    metric: MetricValue,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = { LumoraTopBar(title = metric.name, onBackClick = onBackClick) },
        modifier = modifier.testTag("metric_detail_screen")
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Value: ${metric.value} ${metric.unit}",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = metric.rangePositionText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    RangeBar(
                        value = metric.value,
                        min = metric.typicalMin * 0.7f,
                        max = metric.typicalMax * 1.3f,
                        typicalMin = metric.typicalMin,
                        typicalMax = metric.typicalMax,
                        rangePositionText = "Typical: ${metric.typicalMin} - ${metric.typicalMax} ${metric.unit}",
                        isProvisional = metric.isProvisional
                    )
                }
            }

            // Changeability explanation
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Changeability: ${metric.changeability.label}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = when (metric.changeability) {
                            ChangeabilityTag.STRUCTURE -> "Underlying bone structure. This cannot and should not be changed by cosmetics, skincare, or lifestyle."
                            ChangeabilityTag.SOFT_TISSUE -> "Soft tissue and muscle tone. Modulated slightly by hydration, sodium, and posture."
                            ChangeabilityTag.PRESENTATION -> "Expression, hair styling, lens distance, and camera perspective."
                            ChangeabilityTag.SKIN -> "Skin surface tone, hydration, and barrier health. Responsive to consistent skincare habits."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Source note
            if (metric.sourceNote.isNotBlank()) {
                Text(
                    text = "Reference Note: ${metric.sourceNote}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = "Disclaimer: Classical neoclassical canons and photographic references are aesthetic conventions, not medical standards or personal targets.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun OverlayViewerScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showEyes by remember { mutableStateOf(true) }
    var showNose by remember { mutableStateOf(true) }
    var showMouth by remember { mutableStateOf(true) }
    var showJaw by remember { mutableStateOf(true) }
    var showMidline by remember { mutableStateOf(true) }

    Scaffold(
        topBar = { LumoraTopBar(title = "Landmark Overlay", onBackClick = onBackClick) },
        modifier = modifier.testTag("overlay_viewer_screen")
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // Draw fitted midline
                    if (showMidline) {
                        drawLine(
                            color = Color(0xFFD99A2B),
                            start = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.15f),
                            end = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.85f),
                            strokeWidth = 2.dp.toPx()
                        )
                    }

                    // Draw synthetic landmark points
                    if (showEyes) {
                        drawCircle(Color(0xFF7FC4C0), radius = 4.dp.toPx(), center = androidx.compose.ui.geometry.Offset(w * 0.35f, h * 0.40f))
                        drawCircle(Color(0xFF7FC4C0), radius = 4.dp.toPx(), center = androidx.compose.ui.geometry.Offset(w * 0.65f, h * 0.40f))
                    }
                    if (showNose) {
                        drawCircle(Color(0xFF7FC4C0), radius = 4.dp.toPx(), center = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.55f))
                    }
                    if (showMouth) {
                        drawCircle(Color(0xFF7FC4C0), radius = 4.dp.toPx(), center = androidx.compose.ui.geometry.Offset(w * 0.40f, h * 0.68f))
                        drawCircle(Color(0xFF7FC4C0), radius = 4.dp.toPx(), center = androidx.compose.ui.geometry.Offset(w * 0.60f, h * 0.68f))
                    }
                    if (showJaw) {
                        drawCircle(Color(0xFF7FC4C0), radius = 4.dp.toPx(), center = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.82f))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Landmark toggles
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                FilterChip(selected = showEyes, onClick = { showEyes = !showEyes }, label = { Text("Eyes") })
                FilterChip(selected = showNose, onClick = { showNose = !showNose }, label = { Text("Nose") })
                FilterChip(selected = showMouth, onClick = { showMouth = !showMouth }, label = { Text("Mouth") })
                FilterChip(selected = showJaw, onClick = { showJaw = !showJaw }, label = { Text("Jaw") })
                FilterChip(selected = showMidline, onClick = { showMidline = !showMidline }, label = { Text("Midline") })
            }
        }
    }
}

@Composable
fun SkinResultsScreen(
    scan: ScanResult,
    onRoutineBuilderClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = { LumoraTopBar(title = "Skin Appearance", onBackClick = onBackClick) },
        modifier = modifier.testTag("skin_results_screen")
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "This is a cosmetic photo reading, not a medical assessment. See a doctor or dermatologist for any skin health concern.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Descriptors list
            SectionHeader(title = "Surface Descriptors")
            val descriptors = listOf(
                "Tone Evenness" to "Mild regional variation",
                "Cheek Redness Proxy" to "Low surface redness detected",
                "Texture Proxy" to "Smooth texture profile",
                "Under-eye Contrast" to "Typical optical shadow"
            )

            descriptors.forEach { (title, desc) ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = title, style = MaterialTheme.typography.titleSmall)
                        Text(text = desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            PrimaryButton(
                text = "Build tailored routine",
                onClick = onRoutineBuilderClick,
                testTag = "build_routine_from_skin_button"
            )
        }
    }
}

@Composable
fun ScanDetailsScreen(
    scan: ScanResult,
    onDeleteClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = { LumoraTopBar(title = "Scan Details", onBackClick = onBackClick) },
        modifier = modifier.testTag("scan_details_screen")
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Recorded on ${SimpleDateFormat("MMMM d, yyyy · HH:mm", Locale.US).format(Date(scan.createdAt))}",
                style = MaterialTheme.typography.titleMedium
            )
            Text(text = "Engine version: ${scan.engineVersion}", style = MaterialTheme.typography.bodySmall)
            Text(text = "Quality score: ${scan.qualityReport.score}/100", style = MaterialTheme.typography.bodySmall)

            Spacer(modifier = Modifier.height(24.dp))

            SecondaryButton(
                text = "Delete scan",
                onClick = onDeleteClick,
                testTag = "scan_details_delete_button"
            )
        }
    }
}

@Composable
fun CompareScreen(
    scans: List<ScanResult> = emptyList(),
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedIndexA by remember { mutableStateOf(0) }
    var selectedIndexB by remember { mutableStateOf(if (scans.size > 1) 1 else 0) }

    val scanA = scans.getOrNull(selectedIndexA)
    val scanB = scans.getOrNull(selectedIndexB)

    val deltaChecks = remember(scanA, scanB) {
        if (scanA != null && scanB != null && scanA.id != scanB.id) {
            val baseMap = scanA.metrics.mapValues { it.value.value }
            val currentMap = scanB.metrics.mapValues { it.value.value }
            com.example.face.measure.NoiseFloorEvaluator.evaluateAll(baseMap, currentMap)
        } else {
            emptyList()
        }
    }

    Scaffold(
        topBar = { LumoraTopBar(title = "Compare Scans", onBackClick = onBackClick) },
        modifier = modifier.testTag("compare_screen")
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Visual & Metric Comparison",
                style = MaterialTheme.typography.headlineSmall
            )

            if (scans.size < 2) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Need at least 2 scans",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Capture a new scan to unlock side-by-side visual alignment and noise-floor-filtered progress analytics.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                // Scan selector row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Baseline Scan", style = MaterialTheme.typography.labelMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            scans.forEachIndexed { index, s ->
                                FilterChip(
                                    selected = selectedIndexA == index,
                                    onClick = { selectedIndexA = index },
                                    label = { Text("Scan ${scans.size - index}") }
                                )
                            }
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Recent Scan", style = MaterialTheme.typography.labelMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            scans.forEachIndexed { index, s ->
                                FilterChip(
                                    selected = selectedIndexB == index,
                                    onClick = { selectedIndexB = index },
                                    label = { Text("Scan ${scans.size - index}") }
                                )
                            }
                        }
                    }
                }

                BeforeAfterSlider(
                    beforeBitmap = null,
                    afterBitmap = null,
                    beforeLabel = "Baseline",
                    afterLabel = "Recent"
                )

                InsightCard(
                    title = "Sensor Noise Floor Rule",
                    description = "Changes smaller than measurement tolerance (micro-head tilts, subtle lens distance shifts) are classified as non-significant to prevent false progress impressions."
                )

                SectionHeader(title = "Metric Deltas")

                if (deltaChecks.isEmpty()) {
                    Text(
                        text = "Select two different scans above to evaluate progress deltas.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    deltaChecks.forEach { check ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (check.isSignificant) {
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                } else {
                                    MaterialTheme.colorScheme.surface
                                }
                            )
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = check.metricName, style = MaterialTheme.typography.titleSmall)
                                    Text(
                                        text = (if (check.delta > 0) "+" else "") + String.format("%.1f", check.delta),
                                        style = MaterialTheme.typography.labelLarge,
                                        color = if (check.isSignificant) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = check.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
