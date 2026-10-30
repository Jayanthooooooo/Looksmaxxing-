package com.example.ui.screens.plan

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.components.LumoraTopBar
import com.example.ui.components.PrimaryButton
import com.example.ui.components.QuietTextButton
import com.example.ui.components.SecondaryButton
import com.example.ui.components.SectionHeader
import com.example.ui.theme.LumoraColors

@Composable
fun PlanScreen(
    onOpenRoutineBuilder: () -> Unit,
    onOpenWeeklyCheckIn: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = { LumoraTopBar(title = "My Plan", onBackClick = onBackClick) },
        modifier = modifier.testTag("plan_screen")
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
                        text = "Intentions, not predictions: Daily habits improve skin hydration and presentation, but underlying bone structure does not change.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            SectionHeader(
                title = "Focus Areas (Up to 3)",
                subtitle = "Active intentions for this month"
            )

            val focusAreas = listOf(
                "Skin Surface Health" to "Gentle cleansing, barrier moisturising, daily mineral sunscreen",
                "Sleep & Posture" to "7-8 hours rest, neck alignment for balanced jaw relaxation",
                "Hydration Habits" to "Regular fluid intake throughout the day"
            )

            focusAreas.forEach { (title, subtitle) ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = title, style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            PrimaryButton(
                text = "Open Routine Builder",
                onClick = onOpenRoutineBuilder,
                testTag = "open_routine_builder_button"
            )

            SecondaryButton(
                text = "Weekly Check-in",
                onClick = onOpenWeeklyCheckIn,
                testTag = "open_weekly_checkin_button"
            )
        }
    }
}

@Composable
fun RoutineBuilderScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = { LumoraTopBar(title = "Routine Builder", onBackClick = onBackClick) },
        modifier = modifier.testTag("routine_builder_screen")
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
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Generic Ingredient Templates Only",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "No brand names or affiliate links. Always patch-test new products on your forearm first. Availability and prescription rules differ by country.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                    )
                }
            }

            SectionHeader(title = "Morning (AM) Routine")
            RoutineStepItem(stepNum = 1, name = "Gentle Cleanser", frequency = "Daily", evidence = "Strong", notes = "Non-stripping hydrating formula")
            RoutineStepItem(stepNum = 2, name = "Light Moisturiser", frequency = "Daily", evidence = "Strong", notes = "Ceramides / Hyaluronic acid")
            RoutineStepItem(stepNum = 3, name = "Broad Spectrum Sunscreen SPF 30+", frequency = "Daily", evidence = "Strong", notes = "Crucial for barrier and mark prevention")

            Spacer(modifier = Modifier.height(8.dp))

            SectionHeader(title = "Evening (PM) Routine")
            RoutineStepItem(stepNum = 1, name = "Gentle Cleanser", frequency = "Daily", evidence = "Strong", notes = "Removes daily pollutants and sunscreen")
            RoutineStepItem(stepNum = 2, name = "Barrier Recovery Cream", frequency = "Daily", evidence = "Moderate", notes = "Niacinamide / Squalane / Glycerin")
        }
    }
}

@Composable
fun RoutineStepItem(
    stepNum: Int,
    name: String,
    frequency: String,
    evidence: String,
    notes: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Step $stepNum: $name", style = MaterialTheme.typography.titleSmall)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(text = "Evidence: $evidence", style = MaterialTheme.typography.labelSmall)
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "$frequency · $notes", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun WeeklyCheckInScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var sleepHours by remember { mutableFloatStateOf(7.5f) }
    var consistencyRating by remember { mutableFloatStateOf(4f) }
    var moodRating by remember { mutableFloatStateOf(4f) }
    var submitted by remember { mutableStateOf(false) }

    val isDistressed = moodRating <= 2f

    Scaffold(
        topBar = { LumoraTopBar(title = "Weekly Check-in", onBackClick = onBackClick) },
        modifier = modifier.testTag("weekly_check_in_screen")
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            if (submitted) {
                if (isDistressed) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = "Take care of yourself",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Appearance fluctuates with stress, sleep, and lighting. If feelings about your appearance cause persistent distress, please consider speaking with a trusted loved one, counselor, or healthcare professional. You are worth far more than any photograph.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                } else {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = LumoraColors.LightSuccess.copy(alpha = 0.15f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = "Check-in logged",
                                style = MaterialTheme.typography.titleLarge,
                                color = LumoraColors.LightSuccess
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Thank you for checking in gently. Rest days keep your streak active.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                PrimaryButton(text = "Return to Plan", onClick = onBackClick)
            } else {
                Text(
                    text = "3 Quick reflections",
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = "No pressure. Check-ins help connect routine consistency with how you actually feel.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // 1. Sleep
                Column {
                    Text(text = "Average sleep: ${sleepHours.toInt()} hours/night", style = MaterialTheme.typography.titleSmall)
                    Slider(
                        value = sleepHours,
                        onValueChange = { sleepHours = it },
                        valueRange = 4f..10f,
                        steps = 5
                    )
                }

                // 2. Consistency
                Column {
                    Text(text = "Routine consistency (1 - 5): ${consistencyRating.toInt()}", style = MaterialTheme.typography.titleSmall)
                    Slider(
                        value = consistencyRating,
                        onValueChange = { consistencyRating = it },
                        valueRange = 1f..5f,
                        steps = 3
                    )
                }

                // 3. Mood about appearance
                Column {
                    Text(text = "Mood regarding your appearance (1 = Low/Distressed, 5 = Content): ${moodRating.toInt()}", style = MaterialTheme.typography.titleSmall)
                    Slider(
                        value = moodRating,
                        onValueChange = { moodRating = it },
                        valueRange = 1f..5f,
                        steps = 3
                    )
                }

                PrimaryButton(
                    text = "Submit check-in",
                    onClick = { submitted = true },
                    testTag = "submit_checkin_button"
                )
            }
        }
    }
}
