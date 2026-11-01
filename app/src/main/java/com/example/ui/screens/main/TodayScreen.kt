package com.example.ui.screens.main

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.HabitLogEntity
import com.example.domain.model.ScanResult
import com.example.ui.theme.GoldEyebrow
import com.example.ui.theme.LumoraColors
import com.example.ui.theme.PlusJakartaSans
import com.example.ui.theme.SerifHeadline
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class QuestItem(
    val id: String,
    val title: String,
    val subtitle: String
)

val DefaultQuests = listOf(
    QuestItem("quest_morning_skincare", "Morning skincare", "Cleanse, moisturise, sunscreen"),
    QuestItem("quest_night_skincare", "Night skincare", "Cleanse and moisturise before bed"),
    QuestItem("quest_sleep", "Sleep 7+ hours", "Last night counts"),
    QuestItem("quest_water", "Drink water through the day", "Drink water through the day"),
    QuestItem("quest_sunlight", "10 minutes of sunlight or a walk", "10 minutes of sunlight or a walk"),
    QuestItem("quest_posture", "Posture check", "Posture check")
)

data class WeekDayChipData(
    val dayName: String,
    val dateNumber: Int,
    val dateStr: String,
    val isToday: Boolean
)

@Composable
fun TodayScreen(
    displayName: String,
    latestScan: ScanResult?,
    daysSinceLastScan: Int?,
    streakDays: Int,
    todayLogs: List<HabitLogEntity>,
    onToggleQuest: (questId: String, dateStr: String, done: Boolean) -> Unit,
    onScanClick: () -> Unit,
    onLatestScanClick: (String) -> Unit,
    onCoachClick: () -> Unit,
    onPlanClick: () -> Unit,
    modifier: Modifier = Modifier,
    isOffline: Boolean = false,
    isEmailUnverified: Boolean = false
) {
    val calendar = remember { Calendar.getInstance() }
    val todayDateStr = remember { SimpleDateFormat("yyyyMMdd", Locale.US).format(calendar.time) }
    var selectedDateStr by remember { mutableStateOf(todayDateStr) }

    // Week strip: 6 day chips (MON to SAT)
    val weekDays = remember {
        val cal = Calendar.getInstance()
        cal.firstDayOfWeek = Calendar.MONDAY
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        val names = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT")
        val days = mutableListOf<WeekDayChipData>()
        for (i in 0 until 6) {
            val dStr = SimpleDateFormat("yyyyMMdd", Locale.US).format(cal.time)
            days.add(
                WeekDayChipData(
                    dayName = names[i],
                    dateNumber = cal.get(Calendar.DAY_OF_MONTH),
                    dateStr = dStr,
                    isToday = dStr == todayDateStr
                )
            )
            cal.add(Calendar.DAY_OF_MONTH, 1)
        }
        days
    }

    // Month and Year top label based on current/selected calendar
    val monthYearLabel = remember(selectedDateStr) {
        val cal = Calendar.getInstance()
        try {
            val parsed = SimpleDateFormat("yyyyMMdd", Locale.US).parse(selectedDateStr)
            if (parsed != null) cal.time = parsed
        } catch (e: Exception) { }
        SimpleDateFormat("MMMM yyyy", Locale.US).format(cal.time).uppercase()
    }

    // Filter logs for selected day
    val selectedLogs = todayLogs.filter { it.date == selectedDateStr }
    val completedQuests = selectedLogs.filter { it.done }.map { it.habitId }.toSet()
    val completedCount = DefaultQuests.count { completedQuests.contains(it.id) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .testTag("today_screen")
    ) {
        // TOP: small caps label with month and year and "Today" pill on the right
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = monthYearLabel,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                letterSpacing = 2.sp,
                color = Color(0x99F4F4F6)
            )

            Surface(
                onClick = { selectedDateStr = todayDateStr },
                shape = RoundedCornerShape(14.dp),
                color = if (selectedDateStr == todayDateStr) Color(0x28D9C59B) else Color(0x18FFFFFF),
                border = BorderStroke(1.dp, if (selectedDateStr == todayDateStr) LumoraColors.ChampagneGold else Color.White.copy(alpha = 0.12f))
            ) {
                Text(
                    text = "Today",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = if (selectedDateStr == todayDateStr) LumoraColors.ChampagneGold else Color(0xFFF4F4F6),
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Week strip: 6 day chips (MON to SAT)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            weekDays.forEach { day ->
                val isSelected = day.dateStr == selectedDateStr
                val isToday = day.isToday

                val chipBg = when {
                    isSelected -> Color(0xFFF0F0F3) // Light / white with dark text
                    else -> Color(0x12FFFFFF)
                }
                val textColor = if (isSelected) Color(0xFF0B0B0F) else Color(0xFFF4F4F6)
                val dayLabelColor = if (isSelected) Color(0xFF0B0B0F) else Color(0x99F4F4F6)
                val dotColor = when {
                    isSelected -> Color(0xFF0B0B0F)
                    isToday -> LumoraColors.ChampagneGold
                    else -> Color.White.copy(alpha = 0.2f)
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(chipBg)
                        .border(1.dp, if (isSelected) Color.Transparent else Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                        .clickable { selectedDateStr = day.dateStr }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = day.dayName,
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.sp,
                            color = dayLabelColor
                        )
                        Text(
                            text = "${day.dateNumber}",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = textColor
                        )
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(dotColor)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // One line below: "Nothing yet today — clear your quests or scan."
        Text(
            text = if (completedCount == 0) "Nothing yet today — clear your quests or scan." else "$completedCount of 6 completed today — keep it up!",
            fontFamily = PlusJakartaSans,
            fontSize = 13.sp,
            color = Color(0x99F4F4F6)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // CARD 1: round gold icon on left. Title "Scan to get your Skin Quality rating". Subtitle "Tap Scan below. Your face is measured on this phone."
        Surface(
            onClick = onScanClick,
            shape = RoundedCornerShape(20.dp),
            color = Color(0x14FFFFFF),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.10f)),
            modifier = Modifier.fillMaxWidth().testTag("home_scan_card")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Round gold icon
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(LumoraColors.ChampagneGold),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Scan",
                        tint = Color(0xFF0B0B0F),
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Scan to get your Skin Quality rating",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = Color(0xFFF4F4F6)
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "Tap Scan below. Your face is measured on this phone.",
                        fontFamily = PlusJakartaSans,
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        color = Color(0x99F4F4F6)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Section "TODAY" (gold eyebrow) with serif title "Daily quests" and a "0/6" pill on the right
        GoldEyebrow(text = "TODAY")
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SerifHeadline(
                text = "Daily quests",
                italicWord = "quests",
                fontSize = 24.sp,
                lineHeight = 32.sp
            )

            // "0/6" pill
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0x18FFFFFF),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
            ) {
                Text(
                    text = "$completedCount/6",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = LumoraColors.ChampagneGold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Progress bar section: "4 clear the day" on left, "+25 XP bonus" on right
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "4 clear the day",
                fontFamily = PlusJakartaSans,
                fontSize = 12.sp,
                color = Color(0x99F4F4F6)
            )
            Text(
                text = "+25 XP bonus",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                color = LumoraColors.ChampagneGold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 6-segment progress bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            for (i in 0 until 6) {
                val isSegmentDone = i < completedCount
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(if (isSegmentDone) LumoraColors.ChampagneGold else Color.White.copy(alpha = 0.12f))
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Quest rows:
        // Morning skincare (Cleanse, moisturise, sunscreen)
        // Night skincare (Cleanse and moisturise before bed)
        // Sleep 7+ hours (Last night counts)
        // Drink water through the day
        // 10 minutes of sunlight or a walk
        // Posture check
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            DefaultQuests.forEach { quest ->
                val isDone = completedQuests.contains(quest.id)
                QuestRowCard(
                    quest = quest,
                    isDone = isDone,
                    onToggle = {
                        onToggleQuest(quest.id, selectedDateStr, !isDone)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun QuestRowCard(
    quest: QuestItem,
    isDone: Boolean,
    onToggle: () -> Unit
) {
    val checkColor by animateColorAsState(
        targetValue = if (isDone) LumoraColors.ChampagneGold else Color.Transparent,
        label = "quest_check_bg"
    )

    Surface(
        onClick = onToggle,
        shape = RoundedCornerShape(18.dp),
        color = Color(0x14FFFFFF),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Round checkbox on the left
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(checkColor)
                    .border(
                        width = 1.5.dp,
                        color = if (isDone) LumoraColors.ChampagneGold else Color.White.copy(alpha = 0.35f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isDone) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Done",
                        tint = Color(0xFF0B0B0F),
                        modifier = Modifier.size(15.dp)
                    )
                }
            }

            // Title and subtitle
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = quest.title,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = Color(0xFFF4F4F6)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = quest.subtitle,
                    fontFamily = PlusJakartaSans,
                    fontSize = 12.sp,
                    color = Color(0x99F4F4F6)
                )
            }

            // "+20 XP" pill on the right
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0x22D9C59B),
                border = BorderStroke(1.dp, Color(0x44D9C59B))
            ) {
                Text(
                    text = "+20 XP",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    color = LumoraColors.ChampagneGold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}
