package com.example.ui.screens.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.GlassCard
import com.example.ui.components.GradientButton
import com.example.ui.theme.LumoraColors
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(pageCount = { 4 })
    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = LumoraColors.DarkBackground
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Background glow
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(LumoraColors.GlowGradient)
            )

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.weight(1f)
                ) { page ->
                    when (page) {
                        0 -> OnboardingPage(
                            title = "AI Face Analysis",
                            description = "High-precision biometric scanning to track your skin's health and proportions over time.",
                            imageRes = R.drawable.img_onboarding_scan
                        )
                        1 -> OnboardingPage(
                            title = "Progress Tracking",
                            description = "Visualize your results with premium data insights and side-by-side comparisons.",
                            imageRes = R.drawable.img_onboarding_results
                        )
                        2 -> OnboardingPage(
                            title = "Personal Coach",
                            description = "An intelligent AI assistant that provides personalized beauty-tech advice and routines.",
                            imageRes = R.drawable.img_onboarding_coach
                        )
                        3 -> AgeConfirmationPage(
                            onConfirm = { onComplete() }
                        )
                    }
                }

                if (pagerState.currentPage < 3) {
                    Row(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Page Indicators
                        Row {
                            repeat(4) { index ->
                                val color = if (pagerState.currentPage == index) LumoraColors.SecondaryCyan else Color.White.copy(alpha = 0.2f)
                                Box(
                                    modifier = Modifier
                                        .padding(4.dp)
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                )
                            }
                        }

                        GradientButton(
                            text = if (pagerState.currentPage == 2) "Get Started" else "Next",
                            onClick = {
                                scope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            },
                            modifier = Modifier.testTag("onboarding_next_button")
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OnboardingPage(
    title: String,
    description: String,
    imageRes: Int
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
        ) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(MaterialTheme.shapes.large),
                contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.height(40.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.displayMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
    }
}

@Composable
fun AgeConfirmationPage(
    onConfirm: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(id = android.R.drawable.ic_dialog_info),
            contentDescription = null,
            tint = LumoraColors.WarningAmber,
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "One last thing",
            style = MaterialTheme.typography.displaySmall,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Lumora is designed for individuals age 18 and above. By continuing, you confirm that you meet the age requirement.",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(48.dp))
        GradientButton(
            text = "I am 18 or older",
            onClick = onConfirm,
            modifier = Modifier.fillMaxWidth().testTag("confirm_age_button")
        )
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class, androidx.compose.foundation.layout.LayoutScopeMarker::class)
@Composable
fun ProfileScreen(
    currentStep: Int,
    initialProfile: com.example.domain.model.UserProfile,
    onSaveStep: (com.example.domain.model.UserProfile, Int) -> Unit,
    onBackStep: (Int) -> Unit,
    onSignOutDueToAge: () -> Unit,
    modifier: Modifier = Modifier
) {
    var step by remember { mutableIntStateOf(currentStep) }
    var displayName by remember { mutableStateOf(initialProfile.displayName) }
    var birthYearText by remember { mutableStateOf(initialProfile.birthYear?.toString() ?: "") }
    var skinType by remember { mutableStateOf(initialProfile.skinType) }
    var selectedConcerns by remember { mutableStateOf(initialProfile.concerns.toSet()) }
    var refRangePref by remember { mutableStateOf(initialProfile.referenceRangePref) }

    val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
    val parsedYear = birthYearText.toIntOrNull()
    val isUnder13 = parsedYear != null && (currentYear - parsedYear) < 13

    Scaffold(
        topBar = {
            com.example.ui.components.LumoraTopBar(
                title = "Profile (${step}/4)",
                onBackClick = if (step > 1) { { step--; onBackStep(step) } } else null
            )
        },
        containerColor = LumoraColors.DarkBackground,
        modifier = modifier.testTag("profile_screen")
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
        ) {
            LinearProgressIndicator(
                progress = { step / 4f },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = LumoraColors.SecondaryCyan,
                trackColor = Color.White.copy(alpha = 0.1f)
            )
            Spacer(modifier = Modifier.height(24.dp))

            GlassCard(modifier = Modifier.fillMaxWidth().weight(1f)) {
                Column(modifier = Modifier.verticalScroll(androidx.compose.foundation.rememberScrollState())) {
                    when (step) {
                        1 -> {
                            Text("What should we call you?", style = MaterialTheme.typography.headlineSmall, color = Color.White)
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedTextField(
                                value = displayName,
                                onValueChange = { displayName = it },
                                label = { Text("Display name") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        2 -> {
                            Text("Year of birth", style = MaterialTheme.typography.headlineSmall, color = Color.White)
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedTextField(
                                value = birthYearText,
                                onValueChange = { birthYearText = it },
                                label = { Text("Birth year (e.g. 2000)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        3 -> {
                            Text("Skin type", style = MaterialTheme.typography.headlineSmall, color = Color.White)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Select your skin type in settings later.", color = Color.White.copy(alpha = 0.6f))
                        }
                        4 -> {
                            Text("Reference ranges", style = MaterialTheme.typography.headlineSmall, color = Color.White)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Neutral combined convention enabled.", color = Color.White.copy(alpha = 0.6f))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            GradientButton(
                text = if (step < 4) "Continue" else "Complete",
                onClick = {
                    val updated = initialProfile.copy(
                        displayName = displayName.trim(),
                        birthYear = parsedYear,
                        skinType = skinType,
                        concerns = selectedConcerns.toList(),
                        referenceRangePref = refRangePref
                    )
                    if (step < 4) {
                        onSaveStep(updated, step)
                        step++
                    } else {
                        onSaveStep(updated, step)
                    }
                },
                enabled = !isUnder13,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun ConsentScreen(
    currentConsent: com.example.domain.model.ConsentState,
    onSaveConsent: (com.example.domain.model.ConsentState) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = { com.example.ui.components.LumoraTopBar(title = "Privacy") },
        containerColor = LumoraColors.DarkBackground,
        modifier = modifier.testTag("consent_screen")
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp)) {
            Text("You decide where your data goes", style = MaterialTheme.typography.headlineSmall, color = Color.White)
            Spacer(modifier = Modifier.height(16.dp))
            
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text("Your photos stay on this phone by default. We use Google Gemini for optional AI skin analysis only with your consent.", color = Color.White.copy(alpha = 0.7f))
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            GradientButton(
                text = "Accept & Continue",
                onClick = { onSaveConsent(currentConsent.copy(onDeviceAnalysis = true, cloudAiAnalysis = true)) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun NotificationRationaleScreen(
    onRequestPermission: () -> Unit,
    onNotNow: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = { com.example.ui.components.LumoraTopBar(title = "Reminders") },
        containerColor = LumoraColors.DarkBackground,
        modifier = modifier.testTag("notification_rationale_screen")
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Gentle weekly nudges", style = MaterialTheme.typography.headlineMedium, color = Color.White)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Stay consistent with your progress.", color = Color.White.copy(alpha = 0.7f), textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(48.dp))
            GradientButton(text = "Enable Notifications", onClick = onRequestPermission, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(12.dp))
            androidx.compose.material3.TextButton(onClick = onNotNow) {
                Text("Not now", color = Color.White.copy(alpha = 0.5f))
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFF0B0D1A)
@Composable
fun OnboardingPreview() {
    com.example.ui.theme.LumoraTheme {
        OnboardingScreen(onComplete = {})
    }
}
