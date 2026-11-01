package com.example.ui.screens.main

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.HabitLogEntity
import com.example.domain.model.ScanResult
import com.example.ui.theme.Fraunces
import com.example.ui.theme.GoldEyebrow
import com.example.ui.theme.LumoraColors
import com.example.ui.theme.PlusJakartaSans
import com.example.ui.theme.SerifHeadline
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
val ScanResult.overallScore: Int
    get() = indices["overall"] ?: (
        ((indices["proportions"] ?: 70) * 0.4f +
         (indices["symmetry"] ?: 72) * 0.4f +
         (indices["clarity"] ?: 68) * 0.2f).toInt()
    )

@Composable
fun ProgressScreen(
    scans: List<ScanResult>,
    completedLogs: List<HabitLogEntity> = emptyList(),
    onScanClick: () -> Unit,
    onViewScanClick: (String) -> Unit = {},
    onCompareClick: () -> Unit = {},
    onMethodologyClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val todayCal = remember { Calendar.getInstance() }
    val fullDateEyebrow = remember {
        SimpleDateFormat("EEEE d MMMM", Locale.US).format(todayCal.time).uppercase()
    }
    val todayDateStr = remember {
        SimpleDateFormat("yyyyMMdd", Locale.US).format(todayCal.time)
    }

    // XP calculation: 20 XP per completed quest + 25 XP bonus for days with >= 4 completed quests
    val totalXp = remember(completedLogs) {
        val questXp = completedLogs.count { it.done } * 20
        val bonusDays = completedLogs.filter { it.done }
            .groupBy { it.date }
            .count { it.value.size >= 4 }
        questXp + (bonusDays * 25)
    }
    val xpToNextRank = (500 - totalXp).coerceAtLeast(0)

    // Calendar month days calculation
    val monthCalendarData = remember {
        val cal = Calendar.getInstance()
        val currentYear = cal.get(Calendar.YEAR)
        val currentMonth = cal.get(Calendar.MONTH)
        cal.set(Calendar.DAY_OF_MONTH, 1)

        // 1 = Sunday, 2 = Monday, ...
        // We want Monday = 0, Tuesday = 1, ... Sunday = 6
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        val offset = (dayOfWeek + 5) % 7 // Monday-based offset
        val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

        val scanDateStrings = scans.map {
            SimpleDateFormat("yyyyMMdd", Locale.US).format(it.createdAt)
        }.toSet()

        val monthName = SimpleDateFormat("MMMM yyyy", Locale.US).format(cal.time)

        MonthCalendarInfo(
            monthName = monthName,
            offset = offset,
            totalDays = maxDays,
            currentYear = currentYear,
            currentMonth = currentMonth,
            scanDates = scanDateStrings
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .testTag("progress_screen")
    ) {
        // Eyebrow with full date (e.g. "SATURDAY 31 OCTOBER")
        GoldEyebrow(text = fullDateEyebrow)
        Spacer(modifier = Modifier.height(8.dp))

        // Serif title "Progress"
        SerifHeadline(
            text = "Progress",
            italicWord = "Progress",
            fontSize = 32.sp,
            lineHeight = 40.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        // CARD "FACE SCORE"
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0x14FFFFFF),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.10f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GoldEyebrow(text = "FACE SCORE")
                    if (scans.isNotEmpty()) {
                        Text(
                            text = "${scans.first().overallScore} / 100",
                            fontFamily = Fraunces,
                            fontSize = 18.sp,
                            color = LumoraColors.ChampagneGold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (scans.isEmpty()) {
                    // Empty state: Gold orb, serif text "Your trend starts here", subtitle "Your first saved scan starts this chart.", gold button "Scan your face"
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Gold orb
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            Color(0xFFFFF0D4),
                                            LumoraColors.ChampagneGold,
                                            Color(0xFF8C7345)
                                        )
                                    )
                                )
                                .border(2.dp, Color(0xFFF7EBD6), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color(0xFF0B0B0F),
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        SerifHeadline(
                            text = "Your trend starts here",
                            italicWord = "trend",
                            fontSize = 20.sp,
                            lineHeight = 26.sp,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Your first saved scan starts this chart.",
                            fontFamily = PlusJakartaSans,
                            fontSize = 13.sp,
                            color = Color(0x99F4F4F6),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = onScanClick,
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = LumoraColors.ChampagneGold,
                                contentColor = Color(0xFF0B0B0F)
                            ),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Text(
                                text = "Scan your face",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                        }
                    }
                } else {
                    // Line chart after scans exist
                    FaceScoreLineChart(scans = scans)
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // SECTION "YOUR EFFORT", serif "Consistency"
        GoldEyebrow(text = "YOUR EFFORT")
        Spacer(modifier = Modifier.height(8.dp))
        SerifHeadline(
            text = "Consistency",
            italicWord = "Consistency",
            fontSize = 24.sp,
            lineHeight = 32.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Consistency card: rank badge (RANK: NEWBIE), XP counter, and "500 XP to next rank"
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0x14FFFFFF),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.10f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Rank badge
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0x28D9C59B),
                        border = BorderStroke(1.dp, LumoraColors.ChampagneGold)
                    ) {
                        Text(
                            text = if (totalXp >= 500) "RANK: CONSISTENT" else "RANK: NEWBIE",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp,
                            color = LumoraColors.ChampagneGold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }

                    // "500 XP to next rank"
                    Text(
                        text = "$xpToNextRank XP to next rank",
                        fontFamily = PlusJakartaSans,
                        fontSize = 12.sp,
                        color = Color(0x99F4F4F6)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // XP counter
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "$totalXp",
                        fontFamily = Fraunces,
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFFF4F4F6)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "XP",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = LumoraColors.ChampagneGold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Progress towards next rank
                val rankProgress = (totalXp / 500f).coerceIn(0f, 1f)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color.White.copy(alpha = 0.12f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(rankProgress)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(LumoraColors.ChampagneGold)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Month calendar card (7 columns, today circled, dots on scan days)
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0x14FFFFFF),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.10f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = monthCalendarData.monthName.uppercase(),
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    letterSpacing = 1.5.sp,
                    color = LumoraColors.ChampagneGold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 7 column headers: MON TUE WED THU FRI SAT SUN
                val dayHeaders = listOf("M", "T", "W", "T", "F", "S", "S")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    dayHeaders.forEach { h ->
                        Text(
                            text = h,
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = Color(0x99F4F4F6),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.width(36.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Days grid
                val totalCells = monthCalendarData.offset + monthCalendarData.totalDays
                val rows = (totalCells + 6) / 7

                for (row in 0 until rows) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        for (col in 0 until 7) {
                            val cellIndex = row * 7 + col
                            val dayNumber = cellIndex - monthCalendarData.offset + 1

                            if (dayNumber in 1..monthCalendarData.totalDays) {
                                val cal = Calendar.getInstance().apply {
                                    set(Calendar.YEAR, monthCalendarData.currentYear)
                                    set(Calendar.MONTH, monthCalendarData.currentMonth)
                                    set(Calendar.DAY_OF_MONTH, dayNumber)
                                }
                                val dateStr = SimpleDateFormat("yyyyMMdd", Locale.US).format(cal.time)
                                val isToday = dateStr == todayDateStr
                                val hasScan = monthCalendarData.scanDates.contains(dateStr)

                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .then(
                                            if (isToday) {
                                                Modifier.border(1.5.dp, LumoraColors.ChampagneGold, CircleShape)
                                            } else {
                                                Modifier
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = "$dayNumber",
                                            fontFamily = PlusJakartaSans,
                                            fontSize = 12.sp,
                                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isToday) LumoraColors.ChampagneGold else Color(0xFFF4F4F6)
                                        )
                                        if (hasScan) {
                                            Box(
                                                modifier = Modifier
                                                    .size(4.dp)
                                                    .clip(CircleShape)
                                                    .background(LumoraColors.ChampagneGold)
                                            )
                                        }
                                    }
                                }
                            } else {
                                Spacer(modifier = Modifier.size(36.dp))
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // CARD "SKIN"
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0x14FFFFFF),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.10f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                GoldEyebrow(text = "SKIN")
                Spacer(modifier = Modifier.height(8.dp))
                SerifHeadline(
                    text = "Skin trend summary",
                    italicWord = "trend",
                    fontSize = 20.sp,
                    lineHeight = 26.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Empty state text
                Text(
                    text = "Nothing to compare yet",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = Color(0xFFF4F4F6)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Capture your first scan to start tracking changes here.",
                    fontFamily = PlusJakartaSans,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = Color(0x99F4F4F6)
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        val latest = scans.firstOrNull()
                        if (latest != null) onViewScanClick(latest.id) else onScanClick()
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0x22D9C59B),
                        contentColor = LumoraColors.ChampagneGold
                    ),
                    border = BorderStroke(1.dp, LumoraColors.ChampagneGold),
                    modifier = Modifier.height(44.dp)
                ) {
                    Text(
                        text = "View skin",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Not medical advice.",
                    fontFamily = PlusJakartaSans,
                    fontSize = 11.sp,
                    color = Color(0x99F4F4F6)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Row card "How these numbers are calculated" with info icon and chevron opening Methodology page
        Surface(
            onClick = onMethodologyClick,
            shape = RoundedCornerShape(18.dp),
            color = Color(0x14FFFFFF),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
            modifier = Modifier.fillMaxWidth().testTag("row_methodology")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = LumoraColors.ChampagneGold,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "How these numbers are calculated",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = Color(0xFFF4F4F6),
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = "Open Methodology",
                    tint = Color(0x99F4F4F6),
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

private data class MonthCalendarInfo(
    val monthName: String,
    val offset: Int,
    val totalDays: Int,
    val currentYear: Int,
    val currentMonth: Int,
    val scanDates: Set<String>
)

@Composable
private fun FaceScoreLineChart(scans: List<ScanResult>) {
    val reversedScans = remember(scans) { scans.take(10).reversed() }
    val scores = remember(reversedScans) { reversedScans.map { it.overallScore.toFloat() } }
    val minScore = remember(scores) { (scores.minOrNull() ?: 50f) - 5f }
    val maxScore = remember(scores) { (scores.maxOrNull() ?: 100f) + 5f }

    Column(modifier = Modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
        ) {
            val width = size.width
            val height = size.height
            val scoreRange = (maxScore - minScore).coerceAtLeast(10f)

            if (scores.size < 2) {
                // Single point
                val y = height - ((scores.first() - minScore) / scoreRange * height)
                drawCircle(
                    color = Color(0xFFD9C59B),
                    radius = 6.dp.toPx(),
                    center = Offset(width / 2f, y)
                )
                return@Canvas
            }

            val stepX = width / (scores.size - 1)
            val points = scores.mapIndexed { index, score ->
                val x = index * stepX
                val y = height - ((score - minScore) / scoreRange * height)
                Offset(x, y)
            }

            // Draw line
            val path = Path().apply {
                moveTo(points.first().x, points.first().y)
                for (i in 1 until points.size) {
                    lineTo(points[i].x, points[i].y)
                }
            }

            drawPath(
                path = path,
                color = Color(0xFFD9C59B),
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )

            // Draw points
            points.forEach { pt ->
                drawCircle(
                    color = Color(0xFF0B0B0F),
                    radius = 5.dp.toPx(),
                    center = pt
                )
                drawCircle(
                    color = Color(0xFFD9C59B),
                    radius = 3.5.dp.toPx(),
                    center = pt
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val firstDate = SimpleDateFormat("MMM d", Locale.US).format(reversedScans.first().createdAt)
            val lastDate = SimpleDateFormat("MMM d", Locale.US).format(reversedScans.last().createdAt)
            Text(firstDate, fontFamily = PlusJakartaSans, fontSize = 11.sp, color = Color(0x99F4F4F6))
            Text(lastDate, fontFamily = PlusJakartaSans, fontSize = 11.sp, color = Color(0x99F4F4F6))
        }
    }
}
