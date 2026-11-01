package com.example.ui.screens.skintools

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.domain.skin.ConflictStatus
import com.example.ui.components.GlassCard
import com.example.ui.components.LumoraTopBar
import com.example.ui.components.PrimaryButton
import com.example.ui.theme.Fraunces
import com.example.ui.theme.LumoraColors
import com.example.ui.theme.PlusJakartaSans

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SkinToolsScreen(
    onBackClick: () -> Unit,
    viewModel: SkinToolsViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            LumoraTopBar(
                title = "Skin Tools",
                onBackClick = onBackClick
            )
        },
        modifier = modifier.testTag("skin_tools_screen")
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Tab Row
            TabRow(
                selectedTabIndex = uiState.selectedTab,
                containerColor = Color(0xFF141419),
                contentColor = LumoraColors.ChampagneGold,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[uiState.selectedTab]),
                        color = LumoraColors.ChampagneGold
                    )
                }
            ) {
                Tab(
                    selected = uiState.selectedTab == 0,
                    onClick = { viewModel.setSelectedTab(0) },
                    text = { Text("Conflicts", fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold) },
                    modifier = Modifier.testTag("tab_conflicts")
                )
                Tab(
                    selected = uiState.selectedTab == 1,
                    onClick = { viewModel.setSelectedTab(1) },
                    text = { Text("Routine", fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold) },
                    modifier = Modifier.testTag("tab_routine")
                )
                Tab(
                    selected = uiState.selectedTab == 2,
                    onClick = { viewModel.setSelectedTab(2) },
                    text = { Text("Sun & UV", fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold) },
                    modifier = Modifier.testTag("tab_sun")
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Evidence & Mandatory Disclaimer Header
                EvidenceDisclaimerHeader()

                Spacer(modifier = Modifier.height(16.dp))

                when (uiState.selectedTab) {
                    0 -> ConflictsTabContent(uiState, viewModel)
                    1 -> RoutineTabContent(uiState, viewModel)
                    2 -> SunTabContent(uiState, viewModel)
                }
            }
        }
    }
}

@Composable
private fun EvidenceDisclaimerHeader() {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                tint = LumoraColors.ChampagneGold,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Evidence level: Dermatological consensus & cosmetic science",
                fontFamily = PlusJakartaSans,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = LumoraColors.ChampagneGold
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Educational information, not medical advice.",
            fontFamily = PlusJakartaSans,
            fontSize = 11.sp,
            color = Color.White.copy(alpha = 0.6f)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ConflictsTabContent(
    uiState: SkinToolsUiState,
    viewModel: SkinToolsViewModel
) {
    Text(
        text = "Ingredient Conflict Checker",
        fontFamily = Fraunces,
        fontSize = 22.sp,
        color = Color.White,
        fontWeight = FontWeight.Bold
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        text = "Select active ingredients in your products to check for interactions, peeling risks, and safer separation schedules.",
        fontFamily = PlusJakartaSans,
        fontSize = 13.sp,
        color = Color.White.copy(alpha = 0.7f)
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Safety Toggles (Teen Mode & Pregnancy)
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Safety Filters",
            fontFamily = PlusJakartaSans,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = LumoraColors.ChampagneGold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Teen Mode (Under 18)",
                    fontFamily = PlusJakartaSans,
                    fontSize = 13.sp,
                    color = Color.White
                )
                Text(
                    text = "Hides retinoids & strong acid combinations; suggests consulting a parent/dermatologist.",
                    fontFamily = PlusJakartaSans,
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }
            Switch(
                checked = uiState.isTeen,
                onCheckedChange = { viewModel.setIsTeen(it) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color(0xFF0B0B0F),
                    checkedTrackColor = LumoraColors.ChampagneGold
                ),
                modifier = Modifier.testTag("teen_mode_switch")
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Pregnancy / Nursing Note",
                    fontFamily = PlusJakartaSans,
                    fontSize = 13.sp,
                    color = Color.White
                )
                Text(
                    text = "Flags retinoids and hydroquinone. Always check with your OB-GYN or doctor.",
                    fontFamily = PlusJakartaSans,
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }
            Switch(
                checked = uiState.isPregnant,
                onCheckedChange = { viewModel.setIsPregnant(it) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color(0xFF0B0B0F),
                    checkedTrackColor = LumoraColors.ChampagneGold
                ),
                modifier = Modifier.testTag("pregnancy_mode_switch")
            )
        }
    }

    if (uiState.isTeen) {
        Spacer(modifier = Modifier.height(12.dp))
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0x33FBBF24)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = LumoraColors.WarningAmber,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Teen Safety Active: Potent retinoids and heavy peeling acid combinations are hidden. Ask a parent or dermatologist before starting strong acne treatments.",
                    fontFamily = PlusJakartaSans,
                    fontSize = 12.sp,
                    color = Color.White
                )
            }
        }
    }

    if (uiState.isPregnant) {
        Spacer(modifier = Modifier.height(12.dp))
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0x33F87171)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = LumoraColors.ErrorCoral,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Pregnancy Warning: Topical retinoids (Retinol, Tretinoin, Retinal) and Hydroquinone should be avoided during pregnancy or nursing. Always consult your physician.",
                    fontFamily = PlusJakartaSans,
                    fontSize = 12.sp,
                    color = Color.White
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Search input
    OutlinedTextField(
        value = uiState.searchQuery,
        onValueChange = { viewModel.setSearchQuery(it) },
        placeholder = { Text("Search ingredients (e.g. Retinol, AHA, BHA, Vitamin C...)", color = Color.White.copy(0.4f)) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = LumoraColors.ChampagneGold) },
        modifier = Modifier
            .fillMaxWidth()
            .testTag("ingredient_search_input"),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = LumoraColors.ChampagneGold,
            unfocusedBorderColor = Color.White.copy(0.2f),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp)
    )

    Spacer(modifier = Modifier.height(12.dp))

    // Selectable Ingredient Chips
    val filteredIngredients = uiState.allIngredients.filter { ing ->
        if (uiState.isTeen && ing.isTeenRestricted) return@filter false
        if (uiState.searchQuery.isBlank()) true
        else ing.name.contains(uiState.searchQuery, ignoreCase = true) || ing.category.contains(uiState.searchQuery, ignoreCase = true)
    }

    Text(
        text = "Select Active Ingredients in Your Products:",
        fontFamily = PlusJakartaSans,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = Color.White.copy(0.8f)
    )
    Spacer(modifier = Modifier.height(8.dp))

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        filteredIngredients.forEach { ing ->
            val isSelected = uiState.selectedIngredientIds.contains(ing.id)
            FilterChip(
                selected = isSelected,
                onClick = { viewModel.toggleIngredient(ing.id) },
                label = { Text(ing.name, fontSize = 12.sp, fontFamily = PlusJakartaSans) },
                leadingIcon = if (isSelected) {
                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                } else null,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = LumoraColors.ChampagneGold,
                    selectedLabelColor = Color(0xFF0B0B0F),
                    containerColor = Color(0xFF1B1B22),
                    labelColor = Color.White
                ),
                shape = CircleShape
            )
        }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // Dermatologist Warning Card (Painful/scarring acne, changing moles)
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1724)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x44D9C59B)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Icon(
                imageVector = Icons.Default.MedicalServices,
                contentDescription = null,
                tint = LumoraColors.ChampagneGold,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "When to See a Dermatologist",
                    fontFamily = Fraunces,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "If you have painful nodular acne, deep cyst formation, scarring, changing mole borders, or bleeding skin lesions, stop over-the-counter active stacking and seek professional evaluation.",
                    fontFamily = PlusJakartaSans,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // Results Header
    Text(
        text = "Pair Interaction Results (${uiState.conflicts.size} pairs analyzed)",
        fontFamily = Fraunces,
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold,
        color = Color.White
    )
    Spacer(modifier = Modifier.height(12.dp))

    if (uiState.conflicts.isEmpty()) {
        Text(
            text = "Select at least 2 ingredients above to analyze interactions.",
            fontFamily = PlusJakartaSans,
            fontSize = 13.sp,
            color = Color.White.copy(alpha = 0.5f)
        )
    } else {
        uiState.conflicts.forEach { item ->
            ConflictResultCard(item)
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun ConflictResultCard(result: com.example.domain.skin.ConflictCheckResult) {
    val chipColor = when (result.status) {
        ConflictStatus.FINE_TOGETHER -> LumoraColors.SuccessMint
        ConflictStatus.USE_WITH_CARE -> LumoraColors.WarningAmber
        ConflictStatus.BETTER_SEPARATED -> LumoraColors.ErrorCoral
    }

    val chipBg = when (result.status) {
        ConflictStatus.FINE_TOGETHER -> Color(0x224ADE80)
        ConflictStatus.USE_WITH_CARE -> Color(0x22FBBF24)
        ConflictStatus.BETTER_SEPARATED -> Color(0x22F87171)
    }

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${result.ingredient1.name}  +  ${result.ingredient2.name}",
                fontFamily = PlusJakartaSans,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.weight(1f)
            )

            Surface(
                color = chipBg,
                shape = CircleShape,
                border = androidx.compose.foundation.BorderStroke(1.dp, chipColor.copy(alpha = 0.4f))
            ) {
                Text(
                    text = result.status.displayName,
                    fontFamily = PlusJakartaSans,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = chipColor,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = result.reason,
            fontFamily = PlusJakartaSans,
            fontSize = 13.sp,
            color = Color.White.copy(alpha = 0.8f)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = LumoraColors.ChampagneGold,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Safer Protocol: ${result.saferWay}",
                fontFamily = PlusJakartaSans,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = LumoraColors.ChampagneGold
            )
        }
    }
}

@Composable
private fun RoutineTabContent(
    uiState: SkinToolsUiState,
    viewModel: SkinToolsViewModel
) {
    val routine = uiState.routine

    Text(
        text = "Personalized Routine Builder",
        fontFamily = Fraunces,
        fontSize = 22.sp,
        color = Color.White,
        fontWeight = FontWeight.Bold
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        text = "Correct step ordering (Cleanse -> Treat -> Moisturise -> Sunscreen AM) based on your selected active ingredients.",
        fontFamily = PlusJakartaSans,
        fontSize = 13.sp,
        color = Color.White.copy(alpha = 0.7f)
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Patch Test Banner
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF17202A)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = LumoraColors.ChampagneGold,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = routine?.patchTestReminder ?: "Apply a small amount behind ear or on inner forearm for 24h before full face application.",
                fontFamily = PlusJakartaSans,
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.9f)
            )
        }
    }

    if (routine?.activeWarning != null) {
        Spacer(modifier = Modifier.height(12.dp))
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0x44F87171)),
            border = androidx.compose.foundation.BorderStroke(1.dp, LumoraColors.ErrorCoral),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = LumoraColors.ErrorCoral,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = routine.activeWarning,
                    fontFamily = PlusJakartaSans,
                    fontSize = 12.sp,
                    color = Color.White
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // AM Routine Section
    Text(
        text = "Morning Routine (AM)",
        fontFamily = Fraunces,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = LumoraColors.ChampagneGold
    )
    Spacer(modifier = Modifier.height(8.dp))

    routine?.amSteps?.forEach { step ->
        RoutineStepCard(step)
        Spacer(modifier = Modifier.height(8.dp))
    }

    Spacer(modifier = Modifier.height(20.dp))

    // PM Routine Section
    Text(
        text = "Evening Routine (PM)",
        fontFamily = Fraunces,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = LumoraColors.ChampagneGold
    )
    Spacer(modifier = Modifier.height(8.dp))

    routine?.pmSteps?.forEach { step ->
        RoutineStepCard(step)
        Spacer(modifier = Modifier.height(8.dp))
    }

    Spacer(modifier = Modifier.height(24.dp))

    // Start Slow Schedule
    Text(
        text = "Start Slow Schedule (1 Active Every 2 Weeks)",
        fontFamily = Fraunces,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White
    )
    Spacer(modifier = Modifier.height(8.dp))

    routine?.startSlowPhases?.forEach { phase ->
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = phase.weeks,
                    fontFamily = PlusJakartaSans,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = LumoraColors.ChampagneGold
                )
                Text(
                    text = phase.frequency,
                    fontFamily = PlusJakartaSans,
                    fontSize = 11.sp,
                    color = Color.White.copy(0.6f)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Introduce: ${phase.activeName}",
                fontFamily = PlusJakartaSans,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = phase.tip,
                fontFamily = PlusJakartaSans,
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
    }

    Spacer(modifier = Modifier.height(20.dp))

    PrimaryButton(
        text = if (uiState.habitSyncSuccess) "Routine Added to Daily Habits!" else "Add Routine to My Daily Habits",
        onClick = { viewModel.addRoutineToDailyHabits() },
        testTag = "add_routine_habits_button"
    )
}

@Composable
private fun RoutineStepCard(step: com.example.domain.skin.RoutineStep) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(LumoraColors.ChampagneGold),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${step.order}",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF0B0B0F)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = step.stepType.uppercase(),
                        fontFamily = PlusJakartaSans,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = LumoraColors.ChampagneGold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = step.title,
                        fontFamily = PlusJakartaSans,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = step.description,
                    fontFamily = PlusJakartaSans,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun SunTabContent(
    uiState: SkinToolsUiState,
    viewModel: SkinToolsViewModel
) {
    val guidance = uiState.sunGuidance

    Text(
        text = "UV & Weather Aware Sun Shield",
        fontFamily = Fraunces,
        fontSize = 22.sp,
        color = Color.White,
        fontWeight = FontWeight.Bold
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        text = "No location permission or network call needed. Select your daily plans and UV index to receive customized sunscreen reapplication advice.",
        fontFamily = PlusJakartaSans,
        fontSize = 13.sp,
        color = Color.White.copy(alpha = 0.7f)
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Indoor / Outdoor Toggle
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Today I will be...",
            fontFamily = PlusJakartaSans,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = LumoraColors.ChampagneGold
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { viewModel.setIsOutdoors(true) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (uiState.isOutdoors) LumoraColors.ChampagneGold else Color(0xFF1B1B22),
                    contentColor = if (uiState.isOutdoors) Color(0xFF0B0B0F) else Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("outdoors_button")
            ) {
                Text("Outdoors", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { viewModel.setIsOutdoors(false) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (!uiState.isOutdoors) LumoraColors.ChampagneGold else Color(0xFF1B1B22),
                    contentColor = if (!uiState.isOutdoors) Color(0xFF0B0B0F) else Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("indoors_button")
            ) {
                Text("Mostly Indoors", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold)
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // UV Index Selector
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Expected UV Level Today:",
            fontFamily = PlusJakartaSans,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = LumoraColors.ChampagneGold
        )
        Spacer(modifier = Modifier.height(10.dp))

        val levels = listOf("Low", "Moderate", "High", "Very high")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            levels.forEach { level ->
                val isSelected = uiState.uvLevel.equals(level, ignoreCase = true)
                Surface(
                    color = if (isSelected) LumoraColors.ChampagneGold else Color(0xFF1B1B22),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.setUvLevel(level) }
                ) {
                    Text(
                        text = level,
                        fontFamily = PlusJakartaSans,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color(0xFF0B0B0F) else Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 10.dp)
                    )
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // Guidance Card
    if (guidance != null) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A22)),
            border = androidx.compose.foundation.BorderStroke(1.dp, LumoraColors.ChampagneGold.copy(0.4f)),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.WbSunny,
                        contentDescription = null,
                        tint = LumoraColors.ChampagneGold,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = guidance.title,
                            fontFamily = Fraunces,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "UV Index: ${guidance.uvLevel}",
                            fontFamily = PlusJakartaSans,
                            fontSize = 12.sp,
                            color = LumoraColors.ChampagneGold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Recommended SPF",
                        fontFamily = PlusJakartaSans,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(0.6f)
                    )
                    Text(
                        text = guidance.recommendedSpf,
                        fontFamily = PlusJakartaSans,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Reapplication Schedule",
                        fontFamily = PlusJakartaSans,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(0.6f)
                    )
                    Text(
                        text = guidance.reapplyInterval,
                        fontFamily = PlusJakartaSans,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = LumoraColors.ChampagneGold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = guidance.mainTip,
                    fontFamily = PlusJakartaSans,
                    fontSize = 13.sp,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = guidance.details,
                    fontFamily = PlusJakartaSans,
                    fontSize = 12.sp,
                    color = Color.White.copy(0.6f)
                )
            }
        }
    }
}
