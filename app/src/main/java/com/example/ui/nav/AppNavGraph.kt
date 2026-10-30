package com.example.ui.nav

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.LumoraApp
import com.example.core.AppConfig
import com.example.domain.model.ConsentState
import com.example.domain.model.QualityReport
import com.example.domain.model.ScanResult
import com.example.domain.model.SessionState
import com.example.domain.model.UserProfile
import com.example.ui.screens.auth.AuthChoiceScreen
import com.example.ui.screens.auth.ForgotPasswordScreen
import com.example.ui.screens.auth.SignInScreen
import com.example.ui.screens.auth.SignUpScreen
import com.example.ui.screens.auth.SplashScreen
import com.example.ui.screens.auth.VerifyEmailScreen
import com.example.ui.screens.auth.WelcomeScreen
import com.example.ui.screens.capture.CameraRationaleScreen
import com.example.ui.screens.capture.LiveCaptureScreen
import com.example.ui.screens.capture.PhotoReviewScreen
import com.example.ui.screens.capture.ProcessingScreen
import com.example.ui.screens.capture.ScanHubScreen
import com.example.ui.screens.coach.ChatHistoryScreen
import com.example.ui.screens.main.CoachScreen
import com.example.ui.screens.main.MainScaffold
import com.example.ui.screens.main.MainTab
import com.example.ui.screens.main.ProgressScreen
import com.example.ui.screens.main.TodayScreen
import com.example.ui.screens.main.YouScreen
import com.example.ui.screens.onboarding.ConsentScreen
import com.example.ui.screens.onboarding.NotificationRationaleScreen
import com.example.ui.screens.onboarding.ProfileScreen
import com.example.ui.screens.plan.PlanScreen
import com.example.ui.screens.plan.RoutineBuilderScreen
import com.example.ui.screens.plan.WeeklyCheckInScreen
import com.example.ui.screens.results.CompareScreen
import com.example.ui.screens.results.MeasurementDetailScreen
import com.example.ui.screens.results.OverlayViewerScreen
import com.example.ui.screens.results.ResultsScreen
import com.example.ui.screens.results.ScanDetailsScreen
import com.example.ui.screens.results.SkinResultsScreen
import com.example.ui.screens.settings.AboutScreen
import com.example.ui.screens.settings.AppearanceScreen
import com.example.ui.screens.settings.DeleteAccountScreen
import com.example.ui.screens.settings.ExportScreen
import com.example.ui.screens.settings.LegalViewerScreen
import com.example.ui.screens.settings.NotificationSettingsScreen
import com.example.ui.screens.settings.PaywallScreen
import com.example.ui.screens.settings.PrivacyCentreScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.screens.settings.SubscriptionScreen
import kotlinx.coroutines.launch

@Composable
fun AppNavGraph(
    isExpanded: Boolean = false,
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val app = context.applicationContext as LumoraApp
    val container = app.container
    val scope = rememberCoroutineScope()

    val sessionState by container.authRepository.observeSessionState().collectAsState(initial = SessionState.Loading)
    val consentState by container.consentRepository.consentStateFlow.collectAsState(initial = ConsentState())

    val themeMode by container.preferencesDataStore.themeModeFlow.collectAsState(initial = "SYSTEM")
    val mirrorPreview by container.preferencesDataStore.mirrorPreviewFlow.collectAsState(initial = false)
    val hapticsEnabled by container.preferencesDataStore.hapticsEnabledFlow.collectAsState(initial = true)
    val hideNumbers by container.preferencesDataStore.hideNumbersFlow.collectAsState(initial = false)
    val blockScreenshots by container.preferencesDataStore.blockScreenshotsFlow.collectAsState(initial = false)
    val appLockEnabled by container.preferencesDataStore.appLockEnabledFlow.collectAsState(initial = false)
    val retentionMode by container.preferencesDataStore.photoRetentionModeFlow.collectAsState(initial = "DONT_KEEP")
    val simulatePro by container.preferencesDataStore.simulateProFlow.collectAsState(initial = false)
    val entitlementTier by container.preferencesDataStore.entitlementTierFlow.collectAsState(initial = "FREE")

    val isPro = simulatePro || entitlementTier == "PRO"

    var currentMainTab by remember { mutableStateOf(MainTab.TODAY) }
    var userProfile by remember { mutableStateOf(UserProfile()) }
    val activeScans by container.scanRepository.observeAllActiveScans().collectAsState(initial = emptyList())

    // Load profile when ready
    LaunchedEffect(sessionState) {
        if (sessionState is SessionState.Ready) {
            val uid = (sessionState as SessionState.Ready).userId
            val res = container.profileRepository.getProfile(uid)
            if (res.isSuccess) {
                userProfile = res.getOrNull() ?: UserProfile(uid = uid)
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Route.Splash.route,
        modifier = modifier
    ) {
        // N01 Splash
        composable(Route.Splash.route) {
            SplashScreen(
                onNavigateNext = {
                    when (sessionState) {
                        is SessionState.SignedOut -> navController.navigate(Route.Welcome.route) { popUpTo(Route.Splash.route) { inclusive = true } }
                        is SessionState.NeedsOnboarding -> navController.navigate(Route.Onboarding.createRoute(1)) { popUpTo(Route.Splash.route) { inclusive = true } }
                        is SessionState.NeedsConsent -> navController.navigate(Route.Consent.route) { popUpTo(Route.Splash.route) { inclusive = true } }
                        is SessionState.Ready -> navController.navigate(Route.Main.route) { popUpTo(Route.Splash.route) { inclusive = true } }
                        else -> navController.navigate(Route.Welcome.route) { popUpTo(Route.Splash.route) { inclusive = true } }
                    }
                }
            )
        }

        // N02 Welcome
        composable(Route.Welcome.route) {
            val isFirebaseReady = remember {
                try {
                    com.google.firebase.FirebaseApp.getApps(context).isNotEmpty() && com.google.firebase.auth.FirebaseAuth.getInstance() != null
                } catch (e: Exception) {
                    false
                }
            }
            WelcomeScreen(
                onGetStarted = { navController.navigate(Route.AuthChoice.route) },
                onSignIn = { navController.navigate(Route.SignIn.route) },
                firebaseConfigured = isFirebaseReady
            )
        }

        // N03 Auth Choice
        composable(Route.AuthChoice.route) {
            var isLoading by remember { mutableStateOf(false) }
            var errorMsg by remember { mutableStateOf<String?>(null) }

            AuthChoiceScreen(
                onGoogleSignIn = {
                    isLoading = true
                    errorMsg = null
                    scope.launch {
                        val outcome = container.authRepository.signInWithGoogleInteractive(context)
                        isLoading = false
                        if (outcome.isSuccess) {
                            navController.navigate(Route.Onboarding.createRoute(1))
                        } else {
                            errorMsg = (outcome as? com.example.core.Outcome.Error)?.message
                        }
                    }
                },
                onEmailSignUp = { navController.navigate(Route.SignUp.route) },
                onSignIn = { navController.navigate(Route.SignIn.route) },
                onPrivacyClick = { navController.navigate(Route.Legal.createRoute("privacy")) },
                onTermsClick = { navController.navigate(Route.Legal.createRoute("terms")) },
                isLoading = isLoading,
                errorMessage = errorMsg
            )
        }

        // N04 Sign Up
        composable(Route.SignUp.route) {
            var isLoading by remember { mutableStateOf(false) }
            var errorMsg by remember { mutableStateOf<String?>(null) }

            SignUpScreen(
                onSignUp = { email, pass ->
                    isLoading = true
                    errorMsg = null
                    scope.launch {
                        val outcome = container.authRepository.signUpWithEmail(email, pass)
                        isLoading = false
                        if (outcome.isSuccess) {
                            navController.navigate(Route.VerifyEmail.route)
                        } else {
                            errorMsg = (outcome as? com.example.core.Outcome.Error)?.message
                        }
                    }
                },
                onBackClick = { navController.popBackStack() },
                onTermsClick = { navController.navigate(Route.Legal.createRoute("terms")) },
                isLoading = isLoading,
                errorMessage = errorMsg
            )
        }

        // N05 Sign In
        composable(Route.SignIn.route) {
            var isLoading by remember { mutableStateOf(false) }
            var errorMsg by remember { mutableStateOf<String?>(null) }

            SignInScreen(
                onSignIn = { email, pass ->
                    isLoading = true
                    errorMsg = null
                    scope.launch {
                        val outcome = container.authRepository.signInWithEmail(email, pass)
                        isLoading = false
                        if (outcome.isSuccess) {
                            navController.navigate(Route.Main.route) { popUpTo(Route.Welcome.route) { inclusive = true } }
                        } else {
                            errorMsg = (outcome as? com.example.core.Outcome.Error)?.message
                        }
                    }
                },
                onGoogleSignIn = {
                    isLoading = true
                    errorMsg = null
                    scope.launch {
                        val outcome = container.authRepository.signInWithGoogleInteractive(context)
                        isLoading = false
                        if (outcome.isSuccess) {
                            navController.navigate(Route.Main.route) { popUpTo(Route.Welcome.route) { inclusive = true } }
                        } else {
                            errorMsg = (outcome as? com.example.core.Outcome.Error)?.message
                        }
                    }
                },
                onForgotPassword = { navController.navigate(Route.ForgotPassword.route) },
                onBackClick = { navController.popBackStack() },
                isLoading = isLoading,
                errorMessage = errorMsg
            )
        }

        // N06 Forgot Password
        composable(Route.ForgotPassword.route) {
            var isLoading by remember { mutableStateOf(false) }
            var confirmationSent by remember { mutableStateOf(false) }

            ForgotPasswordScreen(
                onSendReset = { email ->
                    isLoading = true
                    scope.launch {
                        container.authRepository.sendPasswordResetEmail(email)
                        isLoading = false
                        confirmationSent = true
                    }
                },
                onBackClick = { navController.popBackStack() },
                isLoading = isLoading,
                confirmationSent = confirmationSent
            )
        }

        // N07 Verify Email
        composable(Route.VerifyEmail.route) {
            VerifyEmailScreen(
                onResendEmail = {
                    scope.launch {
                        container.authRepository.currentUser?.sendEmailVerification()
                    }
                },
                onVerifiedCheck = {
                    scope.launch {
                        val outcome = container.authRepository.reloadUser()
                        if (outcome.isSuccess && outcome.getOrNull() == true) {
                            navController.navigate(Route.Onboarding.createRoute(1))
                        }
                    }
                },
                onContinueForNow = {
                    navController.navigate(Route.Onboarding.createRoute(1))
                }
            )
        }

        // N08 Profile
        composable(
            route = Route.Onboarding.route,
            arguments = listOf(navArgument("step") { type = NavType.IntType; defaultValue = 1 })
        ) { backStackEntry ->
            val step = backStackEntry.arguments?.getInt("step") ?: 1
            ProfileScreen(
                currentStep = step,
                initialProfile = userProfile,
                onSaveStep = { updated, currentStep ->
                    userProfile = updated
                    scope.launch {
                        val uid = container.authRepository.currentUserId
                        if (uid != null) {
                            container.profileRepository.saveProfile(updated.copy(uid = uid))
                        }
                        if (currentStep >= 4) {
                            navController.navigate(Route.Consent.route)
                        }
                    }
                },
                onBackStep = { prev ->
                    navController.navigate(Route.Onboarding.createRoute(prev)) {
                        popUpTo(Route.Onboarding.createRoute(step)) { inclusive = true }
                    }
                },
                onSignOutDueToAge = {
                    scope.launch {
                        container.authRepository.signOut()
                        navController.navigate(Route.Welcome.route) { popUpTo(0) }
                    }
                }
            )
        }

        // N09 Consent
        composable(Route.Consent.route) {
            ConsentScreen(
                currentConsent = consentState,
                onSaveConsent = { updated ->
                    scope.launch {
                        val uid = container.authRepository.currentUserId
                        if (uid != null) {
                            container.consentRepository.saveConsents(uid, updated)
                        }
                        navController.navigate(Route.NotificationRationale.route)
                    }
                }
            )
        }

        // N10 Notification Rationale
        composable(Route.NotificationRationale.route) {
            NotificationRationaleScreen(
                onRequestPermission = {
                    scope.launch {
                        container.preferencesDataStore.setOnboardingCompleted(true)
                        navController.navigate(Route.Main.route) { popUpTo(0) }
                    }
                },
                onNotNow = {
                    scope.launch {
                        container.preferencesDataStore.setOnboardingCompleted(true)
                        navController.navigate(Route.Main.route) { popUpTo(0) }
                    }
                }
            )
        }

        // Main Bottom Navigation Root (Today, Progress, Coach, You)
        composable(Route.Main.route) {
            // System back handler: if not on TODAY tab, back goes to TODAY; from TODAY exits
            BackHandler(enabled = currentMainTab != MainTab.TODAY) {
                currentMainTab = MainTab.TODAY
            }

            MainScaffold(
                currentTab = currentMainTab,
                onTabSelected = { currentMainTab = it },
                onCenterScanClick = { navController.navigate(Route.ScanHub.route) },
                isExpanded = isExpanded
            ) {
                when (currentMainTab) {
                    MainTab.TODAY -> TodayScreen(
                        displayName = userProfile.displayName,
                        latestScan = activeScans.firstOrNull(),
                        daysSinceLastScan = null,
                        streakDays = 1,
                        habits = listOf(
                            "Gentle AM Cleanser" to true,
                            "Broad Spectrum Sunscreen" to true,
                            "PM Barrier Hydration" to false
                        ),
                        onToggleHabit = { _, _ -> },
                        onScanClick = { navController.navigate(Route.ScanHub.route) },
                        onLatestScanClick = { scanId -> navController.navigate(Route.Results.createRoute(scanId)) },
                        onCoachClick = { currentMainTab = MainTab.COACH },
                        onPlanClick = { navController.navigate(Route.Plan.route) }
                    )
                    MainTab.PROGRESS -> ProgressScreen(
                        scans = activeScans,
                        onScanClick = { scanId -> navController.navigate(Route.ScanDetails.createRoute(scanId)) },
                        onCompareClick = { navController.navigate(Route.Compare.route) }
                    )
                    MainTab.COACH -> CoachScreen(
                        messages = emptyList(),
                        onSendMessage = {},
                        onStopGeneration = {},
                        onReportMessage = {},
                        onHistoryClick = { navController.navigate(Route.ChatHistory.route) },
                        onConsentSettingsClick = { navController.navigate(Route.PrivacyCentre.route) },
                        onUpgradeClick = { navController.navigate(Route.Paywall.createRoute("coach_limit")) },
                        isCloudAiConsentGranted = consentState.cloudAiAnalysis,
                        isPro = isPro
                    )
                    MainTab.YOU -> YouScreen(
                        displayName = userProfile.displayName,
                        email = container.authRepository.currentUser?.email ?: "",
                        isPro = isPro,
                        onNavigateToSettings = { navController.navigate(Route.Settings.route) },
                        onNavigateToPrivacyCentre = { navController.navigate(Route.PrivacyCentre.route) },
                        onNavigateToSubscription = { navController.navigate(Route.Subscription.route) }
                    )
                }
            }
        }

        // N12 Scan Hub
        composable(Route.ScanHub.route) {
            ScanHubScreen(
                onFrontScanClick = { navController.navigate(Route.Capture.createRoute("front")) },
                onSideScanClick = { navController.navigate(Route.Capture.createRoute("side")) },
                onGalleryPick = { uri ->
                    if (uri != null) {
                        navController.navigate(Route.Review.route)
                    }
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        // N13 Camera Rationale
        composable(Route.CameraRationale.route) {
            CameraRationaleScreen(
                onAllowCamera = { navController.popBackStack() },
                onUseGallery = { navController.navigate(Route.ScanHub.route) },
                onOpenSettings = {}
            )
        }

        // N14 Live Capture
        composable(
            route = Route.Capture.route,
            arguments = listOf(navArgument("kind") { type = NavType.StringType; defaultValue = "front" })
        ) { backStackEntry ->
            val kind = backStackEntry.arguments?.getString("kind") ?: "front"
            LiveCaptureScreen(
                kind = kind,
                gateHeadAngle = true,
                gateLight = true,
                gateDistance = true,
                gateExpression = true,
                gateSteady = true,
                consecutiveFramesHeld = 8,
                onManualShutterClick = { navController.navigate(Route.Review.route) },
                onToggleMirror = {
                    scope.launch { container.preferencesDataStore.setMirrorPreview(!mirrorPreview) }
                },
                onCloseClick = { navController.popBackStack() },
                isMirrored = mirrorPreview
            )
        }

        // N15 Photo Review
        composable(Route.Review.route) {
            PhotoReviewScreen(
                bitmap = null,
                onAccept = { navController.navigate(Route.Processing.route) },
                onRetake = { navController.popBackStack() }
            )
        }

        // N16 Processing
        composable(Route.Processing.route) {
            LaunchedEffect(Unit) {
                val calculated = com.example.face.measure.MeasurementCalculator().calculate(emptyMap())
                val newScanId = "scan_${System.currentTimeMillis()}"
                val newScan = ScanResult(
                    id = newScanId,
                    createdAt = System.currentTimeMillis(),
                    kind = "front",
                    engineVersion = AppConfig.ENGINE_VERSION,
                    qualityReport = QualityReport(
                        score = 94,
                        flags = emptyList(),
                        perGate = mapOf(
                            "pose" to true,
                            "distance" to true,
                            "lighting" to true,
                            "expression" to true,
                            "stillness" to true
                        )
                    ),
                    metrics = calculated.metrics,
                    indices = mapOf(
                        "proportions" to calculated.proportionIndex,
                        "symmetry" to calculated.symmetryIndex,
                        "clarity" to 82
                    ),
                    skinResult = com.example.domain.model.SkinAnalysisResult(
                        clarityIndex = 82,
                        descriptors = mapOf(
                            "tone_evenness" to com.example.domain.model.SkinDescriptor("mild", "medium"),
                            "redness_proxy" to com.example.domain.model.SkinDescriptor("none", "high"),
                            "texture_proxy" to com.example.domain.model.SkinDescriptor("mild", "medium")
                        ),
                        components = mapOf("evenness" to 84f, "redness" to 12f, "texture" to 80f),
                        needsClinician = false,
                        clinicianReason = null,
                        notes = listOf("Consistent hydration visible", "Low surface irritation index")
                    ),
                    photoFileId = null,
                    note = ""
                )
                container.scanRepository.saveScan(newScan, null)
                kotlinx.coroutines.delay(1200)
                navController.navigate(Route.Results.createRoute(newScanId)) {
                    popUpTo(Route.ScanHub.route) { inclusive = true }
                }
            }
            ProcessingScreen(
                currentStageText = "Measuring landmarks and evaluating lighting...",
                onCancel = { navController.popBackStack() }
            )
        }

        // N17 Results
        composable(
            route = Route.Results.route,
            arguments = listOf(navArgument("scanId") { type = NavType.StringType })
        ) { backStackEntry ->
            val scanId = backStackEntry.arguments?.getString("scanId") ?: ""
            val matchedScan = activeScans.find { it.id == scanId } ?: remember(scanId) {
                val calculated = com.example.face.measure.MeasurementCalculator().calculate(emptyMap())
                ScanResult(
                    id = scanId,
                    createdAt = System.currentTimeMillis(),
                    kind = "front",
                    engineVersion = AppConfig.ENGINE_VERSION,
                    qualityReport = QualityReport(score = 90, flags = emptyList(), perGate = mapOf("light" to true)),
                    metrics = calculated.metrics,
                    indices = mapOf("proportions" to calculated.proportionIndex, "symmetry" to calculated.symmetryIndex, "clarity" to 80),
                    skinResult = null,
                    photoFileId = null
                )
            }

            ResultsScreen(
                scan = matchedScan,
                onMetricClick = { metricId -> navController.navigate(Route.MetricDetail.createRoute(scanId, metricId)) },
                onOverlayClick = { navController.navigate(Route.Overlay.createRoute(scanId)) },
                onSkinResultsClick = { navController.navigate(Route.SkinResults.createRoute(scanId)) },
                onCompareClick = { navController.navigate(Route.Compare.route) },
                onDeleteClick = {
                    scope.launch {
                        container.scanRepository.deleteScan(scanId)
                        navController.popBackStack()
                    }
                },
                onBackClick = { navController.popBackStack() },
                hideNumbers = hideNumbers
            )
        }

        // N18 Metric Detail
        composable(
            route = Route.MetricDetail.route,
            arguments = listOf(
                navArgument("scanId") { type = NavType.StringType },
                navArgument("metricId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val metricId = backStackEntry.arguments?.getString("metricId") ?: "midface_ratio"
            val demoMetric = remember(metricId) {
                com.example.domain.model.MetricValue(
                    id = metricId,
                    name = if (metricId == "symmetry") "Facial Symmetry" else "Midface Ratio",
                    value = 1.02f,
                    sd = 0.02f,
                    unit = "ratio",
                    rangePosition = com.example.domain.model.RangePosition.WITHIN,
                    rangePositionText = "Within typical range",
                    reliability = 0.95f,
                    changeability = com.example.domain.model.ChangeabilityTag.STRUCTURE,
                    typicalMin = 0.95f,
                    typicalMax = 1.05f,
                    sourceNote = "Classical anthropometric equal-thirds & orbital canons (Farkas et al., 1985)"
                )
            }
            MeasurementDetailScreen(
                metric = demoMetric,
                onBackClick = { navController.popBackStack() }
            )
        }

        // N19 Overlay Viewer
        composable(
            route = Route.Overlay.route,
            arguments = listOf(navArgument("scanId") { type = NavType.StringType })
        ) {
            OverlayViewerScreen(onBackClick = { navController.popBackStack() })
        }

        // N20 Skin Results
        composable(
            route = Route.SkinResults.route,
            arguments = listOf(navArgument("scanId") { type = NavType.StringType })
        ) { backStackEntry ->
            val scanId = backStackEntry.arguments?.getString("scanId") ?: "scan_demo"
            val demoScan = remember(scanId) {
                ScanResult(
                    id = scanId,
                    createdAt = System.currentTimeMillis(),
                    kind = "front",
                    engineVersion = AppConfig.ENGINE_VERSION,
                    qualityReport = QualityReport(score = 90, flags = emptyList(), perGate = emptyMap()),
                    metrics = emptyMap(),
                    indices = mapOf("clarity" to 75),
                    skinResult = null,
                    photoFileId = null
                )
            }
            SkinResultsScreen(
                scan = demoScan,
                onRoutineBuilderClick = { navController.navigate(Route.Routine.route) },
                onBackClick = { navController.popBackStack() }
            )
        }

        // N23 Scan Details
        composable(
            route = Route.ScanDetails.route,
            arguments = listOf(navArgument("scanId") { type = NavType.StringType })
        ) { backStackEntry ->
            val scanId = backStackEntry.arguments?.getString("scanId") ?: ""
            val matchedScan = activeScans.find { it.id == scanId } ?: remember(scanId) {
                ScanResult(
                    id = scanId,
                    createdAt = System.currentTimeMillis(),
                    kind = "front",
                    engineVersion = AppConfig.ENGINE_VERSION,
                    qualityReport = QualityReport(score = 85, flags = emptyList(), perGate = emptyMap()),
                    metrics = emptyMap(),
                    indices = emptyMap(),
                    skinResult = null,
                    photoFileId = null
                )
            }
            ScanDetailsScreen(
                scan = matchedScan,
                onDeleteClick = {
                    scope.launch {
                        container.scanRepository.deleteScan(scanId)
                        navController.popBackStack()
                    }
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        // N24 Compare
        composable(Route.Compare.route) {
            CompareScreen(
                scans = activeScans,
                onBackClick = { navController.popBackStack() }
            )
        }

        // N26 Chat History
        composable(Route.ChatHistory.route) {
            ChatHistoryScreen(
                threads = emptyList(),
                onSelectThread = { threadId -> navController.navigate(Route.ChatThread.createRoute(threadId)) },
                onDeleteThread = {},
                onClearAllThreads = {},
                onBackClick = { navController.popBackStack() }
            )
        }

        // N28 Plan
        composable(Route.Plan.route) {
            PlanScreen(
                onOpenRoutineBuilder = { navController.navigate(Route.Routine.route) },
                onOpenWeeklyCheckIn = { navController.navigate(Route.WeeklyCheckIn.route) },
                onBackClick = { navController.popBackStack() }
            )
        }

        // N29 Routine Builder
        composable(Route.Routine.route) {
            RoutineBuilderScreen(onBackClick = { navController.popBackStack() })
        }

        // N30 Weekly Check-in
        composable(Route.WeeklyCheckIn.route) {
            WeeklyCheckInScreen(onBackClick = { navController.popBackStack() })
        }

        // N32 Settings
        composable(Route.Settings.route) {
            SettingsScreen(
                onNavigateToAppearance = { navController.navigate(Route.Appearance.route) },
                onNavigateToNotifications = { navController.navigate(Route.NotificationSettings.route) },
                onNavigateToPrivacyCentre = { navController.navigate(Route.PrivacyCentre.route) },
                onNavigateToSubscription = { navController.navigate(Route.Subscription.route) },
                onNavigateToLegal = { doc -> navController.navigate(Route.Legal.createRoute(doc)) },
                onNavigateToAbout = { navController.navigate(Route.About.route) },
                onSignOut = {
                    scope.launch {
                        container.authRepository.signOut()
                        navController.navigate(Route.Welcome.route) { popUpTo(0) }
                    }
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        // N33 Privacy Centre
        composable(Route.PrivacyCentre.route) {
            PrivacyCentreScreen(
                consentState = consentState,
                onSaveConsent = { updated ->
                    scope.launch {
                        val uid = container.authRepository.currentUserId
                        if (uid != null) {
                            container.consentRepository.saveConsents(uid, updated)
                        }
                    }
                },
                retentionMode = retentionMode,
                onSetRetentionMode = { mode ->
                    scope.launch { container.preferencesDataStore.setPhotoRetentionMode(mode) }
                },
                hideNumbers = hideNumbers,
                onSetHideNumbers = { hide ->
                    scope.launch { container.preferencesDataStore.setHideNumbers(hide) }
                },
                blockScreenshots = blockScreenshots,
                onSetBlockScreenshots = { block ->
                    scope.launch { container.preferencesDataStore.setBlockScreenshots(block) }
                },
                appLockEnabled = appLockEnabled,
                onSetAppLockEnabled = { lock ->
                    scope.launch { container.preferencesDataStore.setAppLockEnabled(lock) }
                },
                onDeleteAllPhotosNow = {
                    scope.launch { container.photoVault.clearAllPhotos() }
                },
                onExportData = { navController.navigate(Route.Export.route) },
                onDeleteAccount = { navController.navigate(Route.DeleteAccount.route) },
                onBackClick = { navController.popBackStack() }
            )
        }

        // N34 Export Data
        composable(Route.Export.route) {
            ExportScreen(
                onExportSave = {},
                onExportShare = {},
                onBackClick = { navController.popBackStack() }
            )
        }

        // N35 Delete Account
        composable(Route.DeleteAccount.route) {
            DeleteAccountScreen(
                onConfirmDelete = {
                    scope.launch {
                        container.authRepository.signOut()
                        navController.navigate(Route.Welcome.route) { popUpTo(0) }
                    }
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        // N36 Paywall
        composable(
            route = Route.Paywall.route,
            arguments = listOf(navArgument("reason") { type = NavType.StringType; defaultValue = "general" })
        ) { backStackEntry ->
            val reason = backStackEntry.arguments?.getString("reason") ?: "general"
            PaywallScreen(
                reason = reason,
                onSubscribe = { _ ->
                    scope.launch {
                        container.preferencesDataStore.setSimulatePro(true)
                        navController.popBackStack()
                    }
                },
                onRestore = {},
                onCloseClick = { navController.popBackStack() }
            )
        }

        // N37 Subscription
        composable(Route.Subscription.route) {
            SubscriptionScreen(
                isPro = isPro,
                isSimulatePro = simulatePro,
                onToggleSimulatePro = { sim ->
                    scope.launch { container.preferencesDataStore.setSimulatePro(sim) }
                },
                onManageInPlay = {},
                onRestore = {},
                onBackClick = { navController.popBackStack() }
            )
        }

        // N38 Legal Viewer
        composable(
            route = Route.Legal.route,
            arguments = listOf(navArgument("doc") { type = NavType.StringType; defaultValue = "methodology" })
        ) { backStackEntry ->
            val doc = backStackEntry.arguments?.getString("doc") ?: "methodology"
            LegalViewerScreen(
                docType = doc,
                onBackClick = { navController.popBackStack() }
            )
        }

        // N39 Notification Settings
        composable(Route.NotificationSettings.route) {
            NotificationSettingsScreen(onBackClick = { navController.popBackStack() })
        }

        // N40 Appearance
        composable(Route.Appearance.route) {
            AppearanceScreen(
                currentTheme = themeMode,
                onSelectTheme = { theme ->
                    scope.launch { container.preferencesDataStore.setThemeMode(theme) }
                },
                mirrorPreview = mirrorPreview,
                onSetMirrorPreview = { mirror ->
                    scope.launch { container.preferencesDataStore.setMirrorPreview(mirror) }
                },
                hapticsEnabled = hapticsEnabled,
                onSetHapticsEnabled = { haptics ->
                    scope.launch { container.preferencesDataStore.setHapticsEnabled(haptics) }
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        // About
        composable(Route.About.route) {
            AboutScreen(onBackClick = { navController.popBackStack() })
        }
    }
}
