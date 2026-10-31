package com.example.ui.screens.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BrightnessLow
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.ConsentState
import com.example.domain.model.UserProfile
import com.example.ui.components.GlassCard
import com.example.ui.components.PrimaryButton
import com.example.ui.components.QuietTextButton
import com.example.ui.components.SecondaryButton
import com.example.ui.theme.Fraunces
import com.example.ui.theme.GoldEyebrow
import com.example.ui.theme.LumoraColors
import com.example.ui.theme.PlusJakartaSans
import com.example.ui.theme.SerifHeadline
import java.util.Calendar

/**
 * Top row: rounded-square back button + 6-segment progress bar (done = white, current = gold, rest = dim)
 */
@Composable
fun OnboardingTopBar(
    currentStepIndex: Int, // 0 to 5
    totalSegments: Int = 6,
    onBackClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (onBackClick != null) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x18FFFFFF))
                    .border(1.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(12.dp))
                    .clickable { onBackClick() }
                    .testTag("top_bar_back_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFFF4F4F6),
                    modifier = Modifier.size(20.dp)
                )
            }
        } else {
            Spacer(modifier = Modifier.size(42.dp))
        }

        // 6-segment progress bar: done = white, current = gold, rest = dim
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            for (i in 0 until totalSegments) {
                val segmentColor = when {
                    i < currentStepIndex -> Color.White
                    i == currentStepIndex -> LumoraColors.ChampagneGold // Gold
                    else -> Color.White.copy(alpha = 0.16f) // Dim
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(segmentColor)
                )
            }
        }
    }
}

/**
 * Intro onboarding / tutorial screen restyled with the luxury aesthetic
 */
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            OnboardingTopBar(
                currentStepIndex = 0,
                totalSegments = 6,
                onBackClick = null
            )
        },
        modifier = modifier.fillMaxSize()
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                GoldEyebrow(text = "WELCOME TO LUMORA")
                Spacer(modifier = Modifier.height(14.dp))
                SerifHeadline(
                    text = "Your private face ritual",
                    italicWord = "ritual",
                    fontSize = 32.sp,
                    lineHeight = 40.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Track subtle changes, skin texture, and natural facial symmetry over time without artificial beauty ratings.",
                    fontFamily = PlusJakartaSans,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    color = Color(0x99F4F4F6),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(32.dp))

                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        FeatureRow("Clinical symmetry & thirds", "Measure proportional balance without value judgments.")
                        FeatureRow("On-device photo vault", "Your scans stay strictly on your phone by default.")
                        FeatureRow("Personal routine tracking", "Build gentle morning and evening skin consistency.")
                    }
                }
            }

            PrimaryButton(
                text = "Continue",
                onClick = onComplete,
                testTag = "onboarding_next_button"
            )
        }
    }
}

@Composable
private fun FeatureRow(title: String, description: String) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(Color(0x22D9C59B)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = LumoraColors.ChampagneGold,
                modifier = Modifier.size(14.dp)
            )
        }
        Column {
            Text(
                text = title,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = Color(0xFFF4F4F6)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                fontFamily = PlusJakartaSans,
                fontSize = 12.sp,
                lineHeight = 17.sp,
                color = Color(0x99F4F4F6)
            )
        }
    }
}

/**
 * Multi-step Profile Onboarding Screens (Steps 1 to 4):
 * 1) "When were you born?" (3-column wheel picker month/day/year, with an "Age N" chip below)
 * 2) "How tall are you?" (ft·in / cm toggle, big serif numbers, draggable ruler)
 * 3) "Which skin type fits you best?" (2x3 selectable cards with icons; Continue disabled until one is chosen)
 * 4) "How to take a good scan" (numbered 4-step vertical timeline card, button "Start my scan")
 */
@Composable
fun ProfileScreen(
    currentStep: Int,
    initialProfile: UserProfile,
    onSaveStep: (UserProfile, Int) -> Unit,
    onBackStep: (Int) -> Unit,
    onSignOutDueToAge: () -> Unit,
    modifier: Modifier = Modifier
) {
    var step by remember { mutableIntStateOf(currentStep) }

    // Birth date state for Step 1
    var selectedMonth by remember { mutableIntStateOf(6) } // June
    var selectedDay by remember { mutableIntStateOf(15) }
    var selectedYear by remember { mutableIntStateOf(initialProfile.birthYear ?: 2000) }

    val currentCalendarYear = Calendar.getInstance().get(Calendar.YEAR)
    val calculatedAge = (currentCalendarYear - selectedYear).coerceAtLeast(0)
    val isUnder13 = calculatedAge < 13

    // Height state for Step 2
    var heightCm by remember { mutableFloatStateOf(initialProfile.heightCm ?: 172f) }
    var isCm by remember { mutableStateOf(true) }

    // Skin type state for Step 3
    var selectedSkinType by remember {
        mutableStateOf(if (initialProfile.skinType != "not_sure") initialProfile.skinType else null)
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            OnboardingTopBar(
                currentStepIndex = step, // 1 to 4 in a 6-segment bar
                totalSegments = 6,
                onBackClick = if (step > 1) { { step--; onBackStep(step) } } else null
            )
        },
        modifier = modifier.fillMaxSize().testTag("profile_screen")
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (step) {
                    // SCREEN 1: "When were you born?"
                    1 -> {
                        GoldEyebrow(text = "ABOUT YOU")
                        Spacer(modifier = Modifier.height(10.dp))
                        SerifHeadline(
                            text = "When were you born?",
                            italicWord = "born",
                            fontSize = 32.sp,
                            lineHeight = 40.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "We use your birth date to set age-appropriate reference metrics.",
                            fontFamily = PlusJakartaSans,
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            color = Color(0x99F4F4F6),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(28.dp))

                        // 3-column wheel picker (Month, Day, Year)
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Month column
                                WheelColumn(
                                    items = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"),
                                    selectedIndex = (selectedMonth - 1).coerceIn(0, 11),
                                    onSelect = { selectedMonth = it + 1 },
                                    label = "MONTH"
                                )

                                // Day column
                                val days = (1..31).map { it.toString() }
                                WheelColumn(
                                    items = days,
                                    selectedIndex = (selectedDay - 1).coerceIn(0, 30),
                                    onSelect = { selectedDay = it + 1 },
                                    label = "DAY"
                                )

                                // Year column
                                val years = (1940..2024).map { it.toString() }
                                WheelColumn(
                                    items = years,
                                    selectedIndex = (selectedYear - 1940).coerceIn(0, years.lastIndex),
                                    onSelect = { selectedYear = 1940 + it },
                                    label = "YEAR"
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // "Age N" chip below
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0x22D9C59B),
                            border = BorderStroke(1.dp, Color(0x55D9C59B)),
                            modifier = Modifier.testTag("age_chip")
                        ) {
                            Text(
                                text = "Age $calculatedAge",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = LumoraColors.ChampagneGold,
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                            )
                        }

                        if (isUnder13) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Lumora requires users to be at least 13 years old.",
                                color = LumoraColors.ErrorCoral,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // SCREEN 2: "How tall are you?"
                    2 -> {
                        GoldEyebrow(text = "PROPORTIONS")
                        Spacer(modifier = Modifier.height(10.dp))
                        SerifHeadline(
                            text = "How tall are you?",
                            italicWord = "tall",
                            fontSize = 32.sp,
                            lineHeight = 40.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Assists in contextualizing scan proportions accurately.",
                            fontFamily = PlusJakartaSans,
                            fontSize = 14.sp,
                            color = Color(0x99F4F4F6),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))

                        // ft·in / cm toggle
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0x18FFFFFF))
                                .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(14.dp))
                                .padding(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isCm) LumoraColors.ChampagneGold else Color.Transparent)
                                    .clickable { isCm = true }
                                    .padding(horizontal = 20.dp, vertical = 7.dp)
                            ) {
                                Text(
                                    text = "cm",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = if (isCm) Color(0xFF0B0B0F) else Color(0x99F4F4F6)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (!isCm) LumoraColors.ChampagneGold else Color.Transparent)
                                    .clickable { isCm = false }
                                    .padding(horizontal = 20.dp, vertical = 7.dp)
                            ) {
                                Text(
                                    text = "ft · in",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = if (!isCm) Color(0xFF0B0B0F) else Color(0x99F4F4F6)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(28.dp))

                        // Big serif numbers
                        val totalInches = (heightCm / 2.54f).toInt()
                        val feet = totalInches / 12
                        val inches = totalInches % 12

                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            if (isCm) {
                                Text(
                                    text = "${heightCm.toInt()}",
                                    fontFamily = Fraunces,
                                    fontSize = 58.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = Color(0xFFF4F4F6)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "cm",
                                    fontFamily = PlusJakartaSans,
                                    fontSize = 20.sp,
                                    color = LumoraColors.ChampagneGold,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )
                            } else {
                                Text(
                                    text = "$feet",
                                    fontFamily = Fraunces,
                                    fontSize = 58.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = Color(0xFFF4F4F6)
                                )
                                Text(
                                    text = "ft",
                                    fontFamily = PlusJakartaSans,
                                    fontSize = 18.sp,
                                    color = LumoraColors.ChampagneGold,
                                    modifier = Modifier.padding(bottom = 12.dp, end = 12.dp)
                                )
                                Text(
                                    text = "$inches",
                                    fontFamily = Fraunces,
                                    fontSize = 58.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = Color(0xFFF4F4F6)
                                )
                                Text(
                                    text = "in",
                                    fontFamily = PlusJakartaSans,
                                    fontSize = 18.sp,
                                    color = LumoraColors.ChampagneGold,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Draggable ruler
                        GlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp)
                        ) {
                            DraggableRuler(
                                value = heightCm,
                                onValueChange = { heightCm = it.coerceIn(120f, 230f) },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    // SCREEN 3: "Which skin type fits you best?"
                    3 -> {
                        GoldEyebrow(text = "SKIN PROFILE")
                        Spacer(modifier = Modifier.height(10.dp))
                        SerifHeadline(
                            text = "Which skin type fits you best?",
                            italicWord = "best",
                            fontSize = 30.sp,
                            lineHeight = 38.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Select your primary skin characteristic.",
                            fontFamily = PlusJakartaSans,
                            fontSize = 14.sp,
                            color = Color(0x99F4F4F6),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))

                        // 2x3 selectable cards with icons
                        val skinTypes = listOf(
                            SkinTypeOption("normal", "Balanced", "Comfortable, not tight or greasy", Icons.Default.WbSunny),
                            SkinTypeOption("oily", "Oily / shine", "Excess sebum across face", Icons.Default.WaterDrop),
                            SkinTypeOption("acne", "Acne-prone", "Frequent breakouts or pores", Icons.Default.Grain),
                            SkinTypeOption("dry", "Dry / dehydrated", "Flaky, tight feeling", Icons.Default.Spa),
                            SkinTypeOption("sensitive", "Sensitive / redness", "Reactive, easily flushed", Icons.Default.FavoriteBorder),
                            SkinTypeOption("marks", "Dark spots / scars", "Hyperpigmentation or marks", Icons.Default.BrightnessLow)
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            for (row in skinTypes.chunked(2)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    for (item in row) {
                                        val isSelected = selectedSkinType == item.id
                                        SkinTypeCard(
                                            option = item,
                                            isSelected = isSelected,
                                            onClick = { selectedSkinType = item.id },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // SCREEN 4: "How to take a good scan"
                    4 -> {
                        GoldEyebrow(text = "PREPARATION")
                        Spacer(modifier = Modifier.height(10.dp))
                        SerifHeadline(
                            text = "How to take a good scan",
                            italicWord = "good",
                            fontSize = 32.sp,
                            lineHeight = 40.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "4 simple rules for repeatable, clinical accuracy.",
                            fontFamily = PlusJakartaSans,
                            fontSize = 14.sp,
                            color = Color(0x99F4F4F6),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))

                        // Numbered 4-step vertical timeline card
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.fillMaxWidth().padding(4.dp)) {
                                TimelineStep(
                                    number = 1,
                                    title = "Even, soft lighting",
                                    description = "Face a natural daylight window or soft diffuse lighting. Avoid overhead downlights.",
                                    isLast = false
                                )
                                TimelineStep(
                                    number = 2,
                                    title = "Neutral expression",
                                    description = "Relax forehead and jaw. Mouth gently closed with resting facial posture.",
                                    isLast = false
                                )
                                TimelineStep(
                                    number = 3,
                                    title = "Clean camera lens",
                                    description = "Wipe lens smudges for crystal clear pore and texture detection.",
                                    isLast = false
                                )
                                TimelineStep(
                                    number = 4,
                                    title = "Eye-level distance",
                                    description = "Hold phone roughly 30 cm away directly centered with your eyes.",
                                    isLast = true
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Primary button pinned at the bottom
            val isButtonEnabled = when (step) {
                1 -> !isUnder13
                3 -> selectedSkinType != null // Continue disabled until one is chosen
                else -> true
            }

            val buttonLabel = if (step == 4) "Start my scan" else "Continue"

            PrimaryButton(
                text = buttonLabel,
                onClick = {
                    val updated = initialProfile.copy(
                        birthYear = selectedYear,
                        heightCm = heightCm,
                        skinType = selectedSkinType ?: initialProfile.skinType
                    )
                    if (step < 4) {
                        onSaveStep(updated, step)
                        step++
                    } else {
                        onSaveStep(updated, step)
                    }
                },
                enabled = isButtonEnabled,
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            )
        }
    }
}

private data class SkinTypeOption(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector
)

@Composable
private fun SkinTypeCard(
    option: SkinTypeOption,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) LumoraColors.ChampagneGold else Color.White.copy(alpha = 0.12f)
    val bgColor = if (isSelected) Color(0x24D9C59B) else Color(0x14FFFFFF)

    Box(
        modifier = modifier
            .height(132.dp)
            .clip(RoundedCornerShape(20.dp)) // 20dp radius
            .background(bgColor)
            .border(if (isSelected) 1.5.dp else 1.dp, borderColor, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) LumoraColors.ChampagneGold else Color(0x22FFFFFF)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = option.icon,
                        contentDescription = null,
                        tint = if (isSelected) Color(0xFF0B0B0F) else Color(0xFFF4F4F6),
                        modifier = Modifier.size(18.dp)
                    )
                }

                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = LumoraColors.ChampagneGold,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Column {
                Text(
                    text = option.title,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = Color(0xFFF4F4F6)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = option.description,
                    fontFamily = PlusJakartaSans,
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    color = Color(0x99F4F4F6)
                )
            }
        }
    }
}

@Composable
private fun TimelineStep(
    number: Int,
    title: String,
    description: String,
    isLast: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Gold numbered circle
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(Color(0x24D9C59B))
                    .border(1.dp, LumoraColors.ChampagneGold, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$number",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = LumoraColors.ChampagneGold
                )
            }

            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(42.dp)
                        .background(Color(0x33D9C59B))
                )
            }
        }

        Column(modifier = Modifier.padding(bottom = if (!isLast) 20.dp else 4.dp)) {
            Text(
                text = title,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = Color(0xFFF4F4F6)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                fontFamily = PlusJakartaSans,
                fontSize = 12.sp,
                lineHeight = 17.sp,
                color = Color(0x99F4F4F6)
            )
        }
    }
}

@Composable
private fun WheelColumn(
    items: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(84.dp)
    ) {
        Text(
            text = label,
            fontFamily = PlusJakartaSans,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.5.sp,
            color = LumoraColors.ChampagneGold
        )
        Spacer(modifier = Modifier.height(8.dp))

        IconButton(
            onClick = { if (selectedIndex > 0) onSelect(selectedIndex - 1) },
            modifier = Modifier.size(28.dp)
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = "Previous",
                tint = Color.White.copy(alpha = 0.5f)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0x22D9C59B))
                .border(1.dp, Color(0x44D9C59B), RoundedCornerShape(10.dp))
                .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = items.getOrElse(selectedIndex) { "" },
                fontFamily = Fraunces,
                fontWeight = FontWeight.Normal,
                fontSize = 20.sp,
                color = Color(0xFFF4F4F6)
            )
        }

        IconButton(
            onClick = { if (selectedIndex < items.lastIndex) onSelect(selectedIndex + 1) },
            modifier = Modifier.size(28.dp)
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Next",
                tint = Color.White.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
private fun DraggableRuler(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectHorizontalDragGestures { change, dragAmount ->
                    change.consume()
                    // Dragging left moves to larger values, dragging right to smaller
                    onValueChange(value - dragAmount / 4f)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val midX = width / 2f
            val pixelsPerUnit = 8f

            val startUnit = (value - (midX / pixelsPerUnit)).toInt() - 2
            val endUnit = (value + (midX / pixelsPerUnit)).toInt() + 2

            for (unit in startUnit..endUnit) {
                val x = midX + (unit - value) * pixelsPerUnit
                if (x in 0f..width) {
                    val isMajor = unit % 10 == 0
                    val isMid = unit % 5 == 0
                    val tickHeight = when {
                        isMajor -> height * 0.55f
                        isMid -> height * 0.38f
                        else -> height * 0.22f
                    }
                    val strokeAlpha = when {
                        isMajor -> 0.7f
                        isMid -> 0.45f
                        else -> 0.2f
                    }
                    drawLine(
                        color = Color.White.copy(alpha = strokeAlpha),
                        start = Offset(x, height / 2f - tickHeight / 2f),
                        end = Offset(x, height / 2f + tickHeight / 2f),
                        strokeWidth = if (isMajor) 2.dp.toPx() else 1.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }

            // Center gold indicator needle
            drawLine(
                color = Color(0xFFD9C59B),
                start = Offset(midX, height * 0.1f),
                end = Offset(midX, height * 0.9f),
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawCircle(
                color = Color(0xFFD9C59B),
                radius = 4.dp.toPx(),
                center = Offset(midX, height * 0.1f)
            )
        }
    }
}

/**
 * Consent Screen styled with near-black and champagne gold
 */
@Composable
fun ConsentScreen(
    currentConsent: ConsentState,
    onSaveConsent: (ConsentState) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            OnboardingTopBar(
                currentStepIndex = 5,
                totalSegments = 6,
                onBackClick = null
            )
        },
        modifier = modifier.fillMaxSize().testTag("consent_screen")
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                GoldEyebrow(text = "DATA PRIVACY")
                Spacer(modifier = Modifier.height(10.dp))
                SerifHeadline(
                    text = "You own your privacy",
                    italicWord = "privacy",
                    fontSize = 32.sp,
                    lineHeight = 40.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Your photos stay strictly on this device by default. Cloud analysis with Google Gemini is only performed with your explicit consent.",
                    fontFamily = PlusJakartaSans,
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    color = Color(0x99F4F4F6),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(28.dp))

                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        FeatureRow("No biometric facial identification", "We analyze symmetry and skin texture, never personal identity.")
                        FeatureRow("Zero advertising data sharing", "Your photos and scores are never sold or shared with advertisers.")
                        FeatureRow("Instant local deletion", "Wipe your journal and all photo data at any moment in Settings.")
                    }
                }
            }

            PrimaryButton(
                text = "Accept & Continue",
                onClick = {
                    onSaveConsent(
                        currentConsent.copy(
                            onDeviceAnalysis = true,
                            cloudAiAnalysis = true
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            )
        }
    }
}

/**
 * Notification Rationale Screen styled consistently
 */
@Composable
fun NotificationRationaleScreen(
    onRequestPermission: () -> Unit,
    onNotNow: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        containerColor = Color.Transparent,
        modifier = modifier.fillMaxSize().testTag("notification_rationale_screen")
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 40.dp)
            ) {
                GoldEyebrow(text = "HABIT TRACKER")
                Spacer(modifier = Modifier.height(12.dp))
                SerifHeadline(
                    text = "Gentle weekly nudges",
                    italicWord = "weekly",
                    fontSize = 32.sp,
                    lineHeight = 40.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Skin changes occur across weeks. Receive gentle reminders to capture consistent progress photos.",
                    fontFamily = PlusJakartaSans,
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    color = Color(0x99F4F4F6),
                    textAlign = TextAlign.Center
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PrimaryButton(
                    text = "Enable Notifications",
                    onClick = onRequestPermission
                )
                Spacer(modifier = Modifier.height(14.dp))
                QuietTextButton(
                    text = "Not now",
                    onClick = onNotNow
                )
            }
        }
    }
}
