package com.example.ui.screens.settings

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
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
import com.example.core.AppConfig
import com.example.domain.model.ConsentState
import com.example.ui.components.*
import com.example.ui.theme.LumoraColors

@Composable
fun SettingsScreen(
    onNavigateToAppearance: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToPrivacyCentre: () -> Unit,
    onNavigateToSubscription: () -> Unit,
    onNavigateToLegal: (String) -> Unit,
    onNavigateToAbout: () -> Unit,
    onSignOut: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    onTestAiConnection: (() -> Unit)? = null,
    testAiResult: String? = null,
    isTestingAi: Boolean = false
) {
    var showSignOutConfirm by remember { mutableStateOf(false) }

    if (showSignOutConfirm) {
        ConfirmDialog(
            title = "Sign out?",
            message = "Unsynced local photos or drafts may not be available on other devices unless synced.",
            confirmLabel = "Sign out",
            onConfirm = {
                showSignOutConfirm = false
                onSignOut()
            },
            onDismiss = { showSignOutConfirm = false }
        )
    }

    Scaffold(
        topBar = { LumoraTopBar(title = "Settings", onBackClick = onBackClick) },
        modifier = modifier.testTag("settings_screen"),
        containerColor = LumoraColors.DarkBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MenuNavigationCard(
                title = "Appearance",
                subtitle = "Theme, mirror preview, haptics",
                icon = Icons.Default.Brightness4,
                onClick = onNavigateToAppearance,
                testTag = "settings_appearance"
            )

            MenuNavigationCard(
                title = "Notifications",
                subtitle = "Scan reminders, habits, quiet hours",
                icon = Icons.Default.Notifications,
                onClick = onNavigateToNotifications,
                testTag = "settings_notifications"
            )

            MenuNavigationCard(
                title = "Privacy Centre",
                subtitle = "Photo retention, biometric lock, export",
                icon = Icons.Default.Lock,
                onClick = onNavigateToPrivacyCentre,
                testTag = "settings_privacy"
            )

            MenuNavigationCard(
                title = "Subscription",
                subtitle = "Manage Pro membership and quotas",
                icon = Icons.Default.CardMembership,
                onClick = onNavigateToSubscription,
                testTag = "settings_subscription"
            )

            if (onTestAiConnection != null) {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onTestAiConnection() }
                        .testTag("test_ai_connection_button")
                ) {
                    Column {
                        Text(
                            text = if (isTestingAi) "Testing AI connection..." else "Test AI Connection",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text = "Verify API key & network endpoint",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.5f)
                        )
                        if (testAiResult != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = testAiResult,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (testAiResult.contains("Connected")) LumoraColors.SuccessMint else LumoraColors.ErrorCoral
                            )
                        }
                    }
                }
            }

            MenuNavigationCard(
                title = "Legal & Methodology",
                subtitle = "Privacy policy, methodology, disclaimer",
                icon = Icons.Default.Policy,
                onClick = { onNavigateToLegal("methodology") },
                testTag = "settings_legal"
            )

            MenuNavigationCard(
                title = "About Lumora",
                subtitle = "App version, ethical standards",
                icon = Icons.Default.Info,
                onClick = onNavigateToAbout,
                testTag = "settings_about"
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.05f))
                    .clickable { showSignOutConfirm = true }
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Sign out", color = Color.White.copy(alpha = 0.6f), fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun PrivacyCentreScreen(
    consentState: ConsentState,
    onSaveConsent: (ConsentState) -> Unit,
    retentionMode: String,
    onSetRetentionMode: (String) -> Unit,
    hideNumbers: Boolean,
    onSetHideNumbers: (Boolean) -> Unit,
    blockScreenshots: Boolean,
    onSetBlockScreenshots: (Boolean) -> Unit,
    appLockEnabled: Boolean,
    onSetAppLockEnabled: (Boolean) -> Unit,
    onDeleteAllPhotosNow: () -> Unit,
    onExportData: () -> Unit,
    onDeleteAccount: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeletePhotosConfirm by remember { mutableStateOf(false) }

    if (showDeletePhotosConfirm) {
        ConfirmDialog(
            title = "Delete all photos now?",
            message = "This permanently removes all stored scan photos on this device and in the cloud. Metric values and trends will remain intact.",
            confirmLabel = "Delete Photos",
            isDestructive = true,
            onConfirm = {
                showDeletePhotosConfirm = false
                onDeleteAllPhotosNow()
            },
            onDismiss = { showDeletePhotosConfirm = false }
        )
    }

    Scaffold(
        topBar = { LumoraTopBar(title = "Privacy Centre", onBackClick = onBackClick) },
        modifier = modifier.testTag("privacy_centre_screen"),
        containerColor = LumoraColors.DarkBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            PrivacyBadge(text = "Privacy-first architecture · AES-256-GCM hardware encryption")

            SectionHeader(title = "Consent & AI Controls")
            GlassCard {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    ListSwitchRow(
                        title = "On-device face analysis",
                        description = "Calculate proportions locally on this phone.",
                        checked = consentState.onDeviceAnalysis,
                        onCheckedChange = { onSaveConsent(consentState.copy(onDeviceAnalysis = it)) }
                    )
                    Divider(color = Color.White.copy(alpha = 0.05f))
                    ListSwitchRow(
                        title = "Cloud AI analysis",
                        description = "Sends de-identified data to Google Gemini.",
                        checked = consentState.cloudAiAnalysis,
                        onCheckedChange = { onSaveConsent(consentState.copy(cloudAiAnalysis = it)) }
                    )
                    Divider(color = Color.White.copy(alpha = 0.05f))
                    ListSwitchRow(
                        title = "Cloud photo backup",
                        description = "Backs up encrypted photos to Firebase.",
                        checked = consentState.cloudPhotoBackup,
                        onCheckedChange = { onSaveConsent(consentState.copy(cloudPhotoBackup = it)) }
                    )
                }
            }

            SectionHeader(title = "Photo Retention")
            GlassCard {
                val retentionOptions = listOf(
                    "DONT_KEEP" to "Don't keep photos",
                    "KEEP_ON_DEVICE" to "Keep on this phone only",
                    "BACKUP_30" to "Retain for 30 days",
                    "BACKUP_90" to "Retain for 90 days",
                    "BACKUP_FOREVER" to "Keep until I delete"
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    retentionOptions.forEach { (mode, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSetRetentionMode(mode) }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = retentionMode == mode,
                                onClick = { onSetRetentionMode(mode) },
                                colors = RadioButtonDefaults.colors(selectedColor = LumoraColors.SecondaryCyan)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = label, style = MaterialTheme.typography.bodyMedium, color = Color.White)
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, LumoraColors.ErrorCoral.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                    .clickable { showDeletePhotosConfirm = true }
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Delete all my photos now", color = LumoraColors.ErrorCoral, fontWeight = FontWeight.Bold)
            }

            SectionHeader(title = "Security")
            GlassCard {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    ListSwitchRow(
                        title = "Hide numeric indices",
                        description = "Display visual bars only.",
                        checked = hideNumbers,
                        onCheckedChange = onSetHideNumbers
                    )
                    Divider(color = Color.White.copy(alpha = 0.05f))
                    ListSwitchRow(
                        title = "Block screenshots",
                        description = "Prevents screen capture of reviews.",
                        checked = blockScreenshots,
                        onCheckedChange = onSetBlockScreenshots
                    )
                    Divider(color = Color.White.copy(alpha = 0.05f))
                    ListSwitchRow(
                        title = "Biometric app lock",
                        description = "Require fingerprint on launch.",
                        checked = appLockEnabled,
                        onCheckedChange = onSetAppLockEnabled
                    )
                }
            }

            SectionHeader(title = "Your Rights & Data")
            SecondaryButton(
                text = "Export my data (ZIP)",
                onClick = onExportData,
                testTag = "privacy_export_button"
            )
            SecondaryButton(
                text = "Delete account & all data",
                onClick = onDeleteAccount,
                testTag = "privacy_delete_account_button"
            )
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun ExportScreen(
    onExportSave: () -> Unit,
    onExportShare: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    isExporting: Boolean = false,
    exportSuccess: Boolean = false
) {
    Scaffold(
        topBar = { LumoraTopBar(title = "Export Data", onBackClick = onBackClick) },
        modifier = modifier.testTag("export_screen")
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Download your complete archive",
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Builds a standard ZIP containing:\n• scans.json & scans.csv\n• profile.json\n• chats.json\n• habits.csv\n• Decrypted photos (if currently retained)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(24.dp))
                if (exportSuccess) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = LumoraColors.LightSuccess.copy(alpha = 0.15f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Archive compiled successfully in secure cache. Temporary files are cleared after saving.",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = LumoraColors.LightSuccess
                        )
                    }
                }
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                PrimaryButton(
                    text = "Save to device (SAF)",
                    onClick = onExportSave,
                    isLoading = isExporting,
                    testTag = "export_save_button"
                )
                Spacer(modifier = Modifier.height(12.dp))
                SecondaryButton(
                    text = "Share archive",
                    onClick = onExportShare,
                    enabled = !isExporting,
                    testTag = "export_share_button"
                )
            }
        }
    }
}

@Composable
fun DeleteAccountScreen(
    onConfirmDelete: (String) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDeleting: Boolean = false
) {
    var deleteInput by remember { mutableStateOf("") }
    val isConfirmed = deleteInput.trim() == "DELETE"

    Scaffold(
        topBar = { LumoraTopBar(title = "Delete Account", onBackClick = onBackClick) },
        modifier = modifier.testTag("delete_account_screen")
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
                    text = "Permanently delete your account",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "This will irreversibly delete:\n• All profile records and consent history\n• All scan measurements, notes, and photos\n• Chat messages and habits\n• Cloud storage objects\n• Local keystore encryption keys",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Type DELETE to confirm permanent deletion:",
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = deleteInput,
                    onValueChange = { deleteInput = it },
                    placeholder = { Text("DELETE") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("delete_confirm_input")
                )
            }

            Column(modifier = Modifier.fillMaxWidth().padding(top = 24.dp)) {
                PrimaryButton(
                    text = "Permanently Delete Everything",
                    onClick = { onConfirmDelete(deleteInput) },
                    enabled = isConfirmed,
                    isLoading = isDeleting,
                    testTag = "delete_account_final_button"
                )
                Spacer(modifier = Modifier.height(12.dp))
                QuietTextButton(
                    text = "Cancel",
                    onClick = onBackClick,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun PaywallScreen(
    onSubscribe: (String) -> Unit,
    onRestore: () -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier,
    reason: String = "general"
) {
    Scaffold(
        topBar = { LumoraTopBar(title = "Lumora Pro", onBackClick = onCloseClick) },
        modifier = modifier.testTag("paywall_screen")
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
                    text = when (reason) {
                        "coach_limit" -> "Expand your daily AI coach conversations"
                        "skin_limit" -> "Daily AI skin appearance reads"
                        else -> "Unlock Lumora Pro features"
                    },
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Transparent pricing through Google Play. Cancel anytime in Google Play Store.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(20.dp))

                val benefits = listOf(
                    "AI skin reads up to 10 per day (vs 1 per 7 days)",
                    "AI coach up to 100 messages per day (vs 5/day)",
                    "Optional encrypted cloud photo backup",
                    "Weekly AI progress and consistency summaries",
                    "PDF report export & advanced multi-metric comparison"
                )
                benefits.forEach { benefit ->
                    Row(modifier = Modifier.padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(text = benefit, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            Column(modifier = Modifier.fillMaxWidth().padding(top = 24.dp)) {
                PrimaryButton(
                    text = "Subscribe to Pro (Monthly)",
                    onClick = { onSubscribe("lumora_pro_monthly") },
                    testTag = "subscribe_monthly_button"
                )
                Spacer(modifier = Modifier.height(10.dp))
                SecondaryButton(
                    text = "Subscribe to Pro (Yearly)",
                    onClick = { onSubscribe("lumora_pro_yearly") },
                    testTag = "subscribe_yearly_button"
                )
                Spacer(modifier = Modifier.height(10.dp))
                QuietTextButton(
                    text = "Restore purchases",
                    onClick = onRestore,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "restore_purchases_button"
                )
            }
        }
    }
}

@Composable
fun SubscriptionScreen(
    isPro: Boolean,
    isSimulatePro: Boolean,
    onToggleSimulatePro: (Boolean) -> Unit,
    onManageInPlay: () -> Unit,
    onRestore: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = { LumoraTopBar(title = "Manage Subscription", onBackClick = onBackClick) },
        modifier = modifier.testTag("subscription_screen")
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
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(text = "Current Status", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isPro) "Lumora Pro (Active)" else "Free Tier",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    UnverifiedBadge(label = "UNVERIFIED (PENDING BACKEND CLOUD FUNCTION)")
                }
            }

            // Debug simulation switch
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    ListSwitchRow(
                        title = "Simulate Pro (Debug only)",
                        description = "Enables Pro limits and features locally for testing.",
                        checked = isSimulatePro,
                        onCheckedChange = onToggleSimulatePro,
                        testTag = "simulate_pro_switch"
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            PrimaryButton(
                text = "Manage in Google Play",
                onClick = onManageInPlay,
                testTag = "manage_in_play_button"
            )

            SecondaryButton(
                text = "Restore purchases",
                onClick = onRestore,
                testTag = "sub_restore_button"
            )
        }
    }
}

@Composable
fun LegalViewerScreen(
    docType: String, // "privacy", "terms", "methodology", "medical"
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val title = when (docType) {
        "privacy" -> "Privacy Policy"
        "terms" -> "Terms of Service"
        "methodology" -> "Measurement Methodology"
        "medical" -> "Medical Disclaimer"
        else -> "Legal Information"
    }

    val content = when (docType) {
        "privacy" -> "Lumora is privacy-first. Photos remain on this device encrypted with AES-256-GCM. Cloud AI analysis is opt-in and sends de-identified crops to Gemini. No advertising or cross-app trackers are used."
        "terms" -> "Lumora provides photo-based facial measurement comparisons for cosmetic progress journaling. Lumora does not diagnose medical conditions. Users must be at least 13 years old."
        "methodology" -> "Measurements are derived from facial landmark positions relative to inter-ocular distance. Bands represent neoclassical conventions (Farkas et al., 1985) and are provisional conventions, not personal targets. Upper facial third is omitted due to hairline variability."
        "medical" -> "Lumora is a cosmetic photo tracking tool, not a medical device. It does not diagnose, screen, or treat skin conditions, lesions, or structural disorders. Consult a board-certified dermatologist for medical concerns."
        else -> "Operator: [Operator name and contact — fill in before release]"
    }

    Scaffold(
        topBar = { LumoraTopBar(title = title, onBackClick = onBackClick) },
        modifier = modifier.testTag("legal_viewer_screen"),
        containerColor = LumoraColors.DarkBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, color = Color.White)
            )
            Spacer(modifier = Modifier.height(16.dp))
            GlassCard {
                Text(
                    text = content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f),
                    lineHeight = 24.sp
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Version: 1.0.0 · Updated October 2026\nOperator: Lumora Digital Ltd.",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.4f)
            )
        }
    }
}

@Composable
fun NotificationSettingsScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var weeklyScanReminder by remember { mutableStateOf(true) }
    var dailyHabitsReminder by remember { mutableStateOf(true) }
    var weeklyReportReminder by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { LumoraTopBar(title = "Notification Settings", onBackClick = onBackClick) },
        modifier = modifier.testTag("notification_settings_screen"),
        containerColor = LumoraColors.DarkBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            GlassCard {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    ListSwitchRow(
                        title = "Weekly scan reminder",
                        description = "Nudges you to take a scan.",
                        checked = weeklyScanReminder,
                        onCheckedChange = { weeklyScanReminder = it }
                    )
                    Divider(color = Color.White.copy(alpha = 0.05f))
                    ListSwitchRow(
                        title = "Daily habit nudge",
                        description = "Check on today's routine steps.",
                        checked = dailyHabitsReminder,
                        onCheckedChange = { dailyHabitsReminder = it }
                    )
                    Divider(color = Color.White.copy(alpha = 0.05f))
                    ListSwitchRow(
                        title = "Weekly progress summary",
                        description = "Streak and trend summaries.",
                        checked = weeklyReportReminder,
                        onCheckedChange = { weeklyReportReminder = it }
                    )
                }
            }
        }
    }
}

@Composable
fun AppearanceScreen(
    currentTheme: String,
    onSelectTheme: (String) -> Unit,
    mirrorPreview: Boolean,
    onSetMirrorPreview: (Boolean) -> Unit,
    hapticsEnabled: Boolean,
    onSetHapticsEnabled: (Boolean) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = { LumoraTopBar(title = "Appearance", onBackClick = onBackClick) },
        modifier = modifier.testTag("appearance_screen"),
        containerColor = LumoraColors.DarkBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            SectionHeader(title = "Theme")
            GlassCard {
                val themes = listOf("SYSTEM" to "Follow System", "LIGHT" to "Light Mode", "DARK" to "Dark Mode")
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    themes.forEach { (mode, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectTheme(mode) }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = currentTheme == mode,
                                onClick = { onSelectTheme(mode) },
                                colors = RadioButtonDefaults.colors(selectedColor = LumoraColors.SecondaryCyan)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = label, style = MaterialTheme.typography.bodyMedium, color = Color.White)
                        }
                    }
                }
            }

            SectionHeader(title = "Camera & Feedback")
            GlassCard {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    ListSwitchRow(
                        title = "Mirror front camera preview",
                        description = "Mirrors preview only.",
                        checked = mirrorPreview,
                        onCheckedChange = onSetMirrorPreview
                    )
                    Divider(color = Color.White.copy(alpha = 0.05f))
                    ListSwitchRow(
                        title = "Haptic feedback",
                        description = "Vibrate on shutter and gates.",
                        checked = hapticsEnabled,
                        onCheckedChange = onSetHapticsEnabled
                    )
                }
            }
        }
    }
}

@Composable
fun AboutScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = { LumoraTopBar(title = "About Lumora", onBackClick = onBackClick) },
        modifier = modifier.testTag("about_screen"),
        containerColor = LumoraColors.DarkBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = AppConfig.APP_NAME,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, color = Color.White)
            )
            Text(
                text = "Version: ${AppConfig.VERSION_NAME} (${AppConfig.ENGINE_VERSION})",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            GlassCard {
                Column {
                    Text(text = "Ethical Product Standards", style = MaterialTheme.typography.titleMedium, color = Color.White)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "• Describes photos, not people.\n• No attractiveness score or tier ranking.\n• No face-warping or cosmetic simulation.\n• No comparison with others.\n• No face recognition or identification.\n• Zero shame-based wording.\n• Photos stay on this device by default.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f),
                        lineHeight = 22.sp
                    )
                }
            }
        }
    }
}
