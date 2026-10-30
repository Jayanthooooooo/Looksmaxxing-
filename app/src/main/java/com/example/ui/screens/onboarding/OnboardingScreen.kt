package com.example.ui.screens.onboarding

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.domain.model.ConsentState
import com.example.domain.model.UserProfile
import com.example.ui.components.ListSwitchRow
import com.example.ui.components.LumoraTopBar
import com.example.ui.components.PrimaryButton
import com.example.ui.components.QuietTextButton
import com.example.ui.components.SecondaryButton
import com.example.ui.theme.LumoraColors

@OptIn(ExperimentalLayoutApi::class)
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
    var displayName by remember { mutableStateOf(initialProfile.displayName) }
    var birthYearText by remember { mutableStateOf(initialProfile.birthYear?.toString() ?: "") }
    var skinType by remember { mutableStateOf(initialProfile.skinType) }
    var selectedConcerns by remember { mutableStateOf(initialProfile.concerns.toSet()) }
    var heightCmText by remember { mutableStateOf(initialProfile.heightCm?.toInt()?.toString() ?: "") }
    var refRangePref by remember { mutableStateOf(initialProfile.referenceRangePref) }

    val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
    val parsedYear = birthYearText.toIntOrNull()
    val isUnder13 = parsedYear != null && (currentYear - parsedYear) < 13
    val isTeen = parsedYear != null && (currentYear - parsedYear) in 13..17

    Scaffold(
        topBar = {
            LumoraTopBar(
                title = "Profile (${step}/4)",
                onBackClick = if (step > 1) { { step--; onBackStep(step) } } else null
            )
        },
        modifier = modifier.testTag("profile_screen")
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                LinearProgressIndicator(
                    progress = { step / 4f },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Spacer(modifier = Modifier.height(24.dp))

                when (step) {
                    1 -> {
                        Text(
                            text = "What should we call you?",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Your name stays local and is never shared with external services or AI prompts.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        OutlinedTextField(
                            value = displayName,
                            onValueChange = { displayName = it },
                            label = { Text("Display name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("profile_display_name_input")
                        )
                    }
                    2 -> {
                        Text(
                            text = "Year of birth (Optional)",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Helps determine appropriate guidance. Under 18 enables Teen mode with softer wording and hides strong active ingredients.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        OutlinedTextField(
                            value = birthYearText,
                            onValueChange = { birthYearText = it },
                            label = { Text("Birth year (e.g. 2000)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("profile_birth_year_input")
                        )

                        if (isUnder13) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Card(
                                colors = CardDefaults.cardColors(containerColor = LumoraColors.LightCaution.copy(alpha = 0.15f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "Lumora is designed for individuals age 13 and above. Please explore healthy habits with a parent, guardian, or healthcare provider.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = LumoraColors.LightCaution
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    TextButton(onClick = onSignOutDueToAge) {
                                        Text("Sign out")
                                    }
                                }
                            }
                        } else if (isTeen) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "Teen mode enabled: Numeric indices and harsh active ingredients will be hidden. Always talk to a parent or dermatologist for skin health concerns.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    }
                    3 -> {
                        Text(
                            text = "Skin type & concerns",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Select what best describes your skin. This informs routine building and habit suggestions.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Skin type", style = MaterialTheme.typography.titleSmall)
                        Spacer(modifier = Modifier.height(8.dp))
                        val types = listOf("dry" to "Dry", "oily" to "Oily", "combination" to "Combination", "normal" to "Normal", "sensitive" to "Sensitive", "not_sure" to "Not sure")
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            types.forEach { (key, label) ->
                                val selected = skinType == key
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                        .clickable { skinType = key }
                                        .padding(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                        Text("Concerns (select all that apply)", style = MaterialTheme.typography.titleSmall)
                        Spacer(modifier = Modifier.height(8.dp))
                        val concernsList = listOf(
                            "acne" to "Acne / Breakouts",
                            "marks" to "Post-blemish marks",
                            "redness" to "Redness",
                            "dryness" to "Dryness",
                            "oiliness" to "Oiliness",
                            "texture" to "Texture",
                            "dark_circles" to "Under-eye circles",
                            "fine_lines" to "Fine lines"
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            concernsList.forEach { (key, label) ->
                                val selected = selectedConcerns.contains(key)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                                        .border(1.dp, if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                                        .clickable {
                                            selectedConcerns = if (selected) selectedConcerns - key else selectedConcerns + key
                                        }
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                    4 -> {
                        Text(
                            text = "Reference ranges preference",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Published anthropometric studies report ranges that differ slightly across reference groups. This only affects the typical band shown on charts. You are never scored or ranked.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        val options = listOf(
                            "prefer_not_to_say" to "Prefer not to say (Balanced default)",
                            "female_typical" to "Female-typical reference ranges",
                            "male_typical" to "Male-typical reference ranges",
                            "neutral" to "Neutral combined convention"
                        )
                        options.forEach { (key, label) ->
                            val selected = refRangePref == key
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { refRangePref = key }
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            Column(modifier = Modifier.fillMaxWidth().padding(top = 24.dp)) {
                PrimaryButton(
                    text = if (step < 4) "Continue" else "Complete profile",
                    onClick = {
                        val updated = initialProfile.copy(
                            displayName = displayName.trim(),
                            birthYear = parsedYear,
                            skinType = skinType,
                            concerns = selectedConcerns.toList(),
                            heightCm = heightCmText.toFloatOrNull(),
                            referenceRangePref = refRangePref
                        )
                        if (step < 4) {
                            onSaveStep(updated, step)
                            step++
                        } else {
                            onSaveStep(updated, step)
                        }
                    },
                    enabled = !isUnder13 && (step != 1 || displayName.isNotBlank()),
                    testTag = "profile_continue_button"
                )
            }
        }
    }
}

@Composable
fun ConsentScreen(
    currentConsent: ConsentState,
    onSaveConsent: (ConsentState) -> Unit,
    modifier: Modifier = Modifier
) {
    var onDevice by remember { mutableStateOf(currentConsent.onDeviceAnalysis) }
    var cloudAi by remember { mutableStateOf(currentConsent.cloudAiAnalysis) }
    var photoBackup by remember { mutableStateOf(currentConsent.cloudPhotoBackup) }

    Scaffold(
        topBar = { LumoraTopBar(title = "Privacy & Consent") },
        modifier = modifier.testTag("consent_screen")
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "You decide where your data goes",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Lumora is privacy-first. Photos never leave this phone without your explicit permission. You can change these preferences at any time in the Privacy Centre.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(24.dp))

                ListSwitchRow(
                    title = "On-device face analysis (Required)",
                    description = "Runs ML Kit on your device to align facial landmarks and measure proportions. No images are uploaded.",
                    checked = onDevice,
                    onCheckedChange = { onDevice = it },
                    testTag = "consent_on_device_switch"
                )

                ListSwitchRow(
                    title = "Cloud AI skin & coach analysis",
                    description = "Sends a cropped face photo or conversation text to Google Gemini via Firebase AI Logic. When off, skin analysis is strictly on-device and the AI coach is disabled.",
                    checked = cloudAi,
                    onCheckedChange = { cloudAi = it },
                    testTag = "consent_cloud_ai_switch"
                )

                ListSwitchRow(
                    title = "Cloud photo backup",
                    description = "Securely backs up your encrypted scan photos to Firebase Storage. Off by default.",
                    checked = photoBackup,
                    onCheckedChange = { photoBackup = it },
                    testTag = "consent_photo_backup_switch"
                )

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No third-party analytics or advertising trackers are collected.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(modifier = Modifier.fillMaxWidth().padding(top = 24.dp)) {
                PrimaryButton(
                    text = "Save and continue",
                    onClick = {
                        onSaveConsent(
                            currentConsent.copy(
                                onDeviceAnalysis = onDevice,
                                cloudAiAnalysis = cloudAi,
                                cloudPhotoBackup = photoBackup,
                                analytics = false
                            )
                        )
                    },
                    enabled = onDevice, // Required to scan
                    testTag = "consent_continue_button"
                )
            }
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
        topBar = { LumoraTopBar(title = "Reminders") },
        modifier = modifier.testTag("notification_rationale_screen")
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Gentle weekly progress nudges",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Skin appearance shifts gradually over weeks. A weekly reminder helps you capture consistent lighting and track real trends without daily obsession.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                PrimaryButton(
                    text = "Enable reminders",
                    onClick = onRequestPermission,
                    testTag = "enable_notifications_button"
                )
                Spacer(modifier = Modifier.height(12.dp))
                QuietTextButton(
                    text = "Not now",
                    onClick = onNotNow,
                    testTag = "not_now_notifications_button"
                )
            }
        }
    }
}
