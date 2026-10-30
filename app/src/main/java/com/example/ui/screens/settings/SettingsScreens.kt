package com.example.ui.screens.settings

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
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.CardMembership
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.core.AppConfig
import com.example.domain.model.ConsentState
import com.example.ui.components.ConfirmDialog
import com.example.ui.components.ListSwitchRow
import com.example.ui.components.LumoraTopBar
import com.example.ui.components.PrimaryButton
import com.example.ui.components.PrivacyBadge
import com.example.ui.components.QuietTextButton
import com.example.ui.components.SecondaryButton
import com.example.ui.components.SectionHeader
import com.example.ui.components.UnverifiedBadge
import com.example.ui.screens.main.MenuNavigationCard
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
    modifier: Modifier = Modifier
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
        modifier = modifier.testTag("settings_screen")
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
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

            MenuNavigationCard(
                title = "Legal & Methodology",
                subtitle = "Privacy policy, measurement methodology, medical disclaimer",
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

            SecondaryButton(
                text = "Sign out",
                onClick = { showSignOutConfirm = true },
                testTag = "settings_sign_out_button"
            )
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
        modifier = modifier.testTag("privacy_centre_screen")
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PrivacyBadge(text = "Privacy-first architecture · AES-256-GCM hardware keystore encryption")

            SectionHeader(title = "Consent Switches")
            ListSwitchRow(
                title = "On-device face analysis",
                description = "Required to calculate landmark proportions locally on this phone.",
                checked = consentState.onDeviceAnalysis,
                onCheckedChange = { onSaveConsent(consentState.copy(onDeviceAnalysis = it)) }
            )
            ListSwitchRow(
                title = "Cloud AI skin & coach analysis",
                description = "Sends cropped face photo / chat queries to Google Gemini via Firebase AI Logic.",
                checked = consentState.cloudAiAnalysis,
                onCheckedChange = { onSaveConsent(consentState.copy(cloudAiAnalysis = it)) }
            )
            ListSwitchRow(
                title = "Cloud photo backup",
                description = "Backs up encrypted scan photos to Firebase Storage. Off by default.",
                checked = consentState.cloudPhotoBackup,
                onCheckedChange = { onSaveConsent(consentState.copy(cloudPhotoBackup = it)) }
            )

            SectionHeader(title = "Photo Retention Policy")
            val retentionOptions = listOf(
                "DONT_KEEP" to "Don't keep photos (Default - measurements only)",
                "KEEP_ON_DEVICE" to "Keep encrypted on this phone only",
                "BACKUP_30" to "Retain for 30 days",
                "BACKUP_90" to "Retain for 90 days",
                "BACKUP_365" to "Retain for 365 days",
                "BACKUP_FOREVER" to "Keep until I delete"
            )
            retentionOptions.forEach { (mode, label) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSetRetentionMode(mode) }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = retentionMode == mode, onClick = { onSetRetentionMode(mode) })
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = label, style = MaterialTheme.typography.bodyMedium)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = { showDeletePhotosConfirm = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Delete all my photos now")
            }

            SectionHeader(title = "Security & View Controls")
            ListSwitchRow(
                title = "Hide numeric indices",
                description = "Display descriptive ranges and visual bars only (default in Teen mode).",
                checked = hideNumbers,
                onCheckedChange = onSetHideNumbers
            )
            ListSwitchRow(
                title = "Block screenshots on photo screens",
                description = "Uses FLAG_SECURE to prevent screen capture of photo reviews and scans.",
                checked = blockScreenshots,
                onCheckedChange = onSetBlockScreenshots
            )
            ListSwitchRow(
                title = "Biometric app lock",
                description = "Require fingerprint, face unlock, or device PIN on launch.",
                checked = appLockEnabled,
                onCheckedChange = onSetAppLockEnabled
            )

            SectionHeader(title = "Your Rights & Data")
            SecondaryButton(
                text = "Export my data (ZIP)",
                onClick = onExportData,
                testTag = "privacy_export_button"
            )
            Spacer(modifier = Modifier.height(4.dp))
            SecondaryButton(
                text = "Delete account & all data",
                onClick = onDeleteAccount,
                testTag = "privacy_delete_account_button"
            )
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
        modifier = modifier.testTag("legal_viewer_screen")
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
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Version: 1.0.0 · Updated October 2026\nOperator: [Operator name and contact — fill in before release]",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
        modifier = modifier.testTag("notification_settings_screen")
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ListSwitchRow(
                title = "Weekly scan reminder",
                description = "Nudges you once a week to take a scan under consistent lighting.",
                checked = weeklyScanReminder,
                onCheckedChange = { weeklyScanReminder = it }
            )

            ListSwitchRow(
                title = "Daily habit nudge",
                description = "Gentle afternoon check on today's routine steps.",
                checked = dailyHabitsReminder,
                onCheckedChange = { dailyHabitsReminder = it }
            )

            ListSwitchRow(
                title = "Weekly progress summary",
                description = "Summary of completed routine streaks and scan trends.",
                checked = weeklyReportReminder,
                onCheckedChange = { weeklyReportReminder = it }
            )
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
        modifier = modifier.testTag("appearance_screen")
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SectionHeader(title = "Theme")
            val themes = listOf("SYSTEM" to "Follow System", "LIGHT" to "Light Mode", "DARK" to "Dark Mode")
            themes.forEach { (mode, label) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectTheme(mode) }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = currentTheme == mode, onClick = { onSelectTheme(mode) })
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = label, style = MaterialTheme.typography.bodyMedium)
                }
            }

            SectionHeader(title = "Camera & Feedback")
            ListSwitchRow(
                title = "Mirror front camera preview",
                description = "Mirrors preview only. Saved photo is always true perspective.",
                checked = mirrorPreview,
                onCheckedChange = onSetMirrorPreview
            )
            ListSwitchRow(
                title = "Haptic feedback",
                description = "Vibrate lightly when quality gates align and on shutter capture.",
                checked = hapticsEnabled,
                onCheckedChange = onSetHapticsEnabled
            )
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
        modifier = modifier.testTag("about_screen")
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = AppConfig.APP_NAME, style = MaterialTheme.typography.headlineMedium)
            Text(text = "Version: ${AppConfig.VERSION_NAME} (${AppConfig.ENGINE_VERSION})", style = MaterialTheme.typography.bodySmall)

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Ethical Product Standards", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• Describes photos, not people.\n• No attractiveness score or tier ranking.\n• No face-warping or cosmetic simulation.\n• No comparison with others or celebrity look-alikes.\n• No face recognition or biometric identification.\n• Zero shame-based wording.\n• Photos stay on this device by default.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
