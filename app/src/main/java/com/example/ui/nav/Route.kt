package com.example.ui.nav

sealed class Route(val route: String) {
    // Stage 1 & 2: Auth and Onboarding
    data object Splash : Route("splash")
    data object Welcome : Route("welcome")
    data object AuthChoice : Route("auth_choice")
    data object SignUp : Route("sign_up")
    data object SignIn : Route("sign_in")
    data object ForgotPassword : Route("forgot_password")
    data object VerifyEmail : Route("verify_email")
    data object Onboarding : Route("onboarding/{step}") {
        fun createRoute(step: Int) = "onboarding/$step"
    }
    data object Consent : Route("consent")
    data object NotificationRationale : Route("notification_rationale")

    // Main bottom-bar root & tabs
    data object Main : Route("main")
    data object Today : Route("today")
    data object Progress : Route("progress")
    data object Coach : Route("coach")
    data object You : Route("you")

    // Stage 3: Capture flow
    data object ScanHub : Route("scan_hub")
    data object CameraRationale : Route("camera_rationale")
    data object Capture : Route("capture/{kind}") {
        fun createRoute(kind: String) = "capture/$kind" // "front" or "side"
    }
    data object Review : Route("review")
    data object Processing : Route("processing")

    // Stage 4: Results & Metrics
    data object Results : Route("results/{scanId}") {
        fun createRoute(scanId: String) = "results/$scanId"
    }
    data object MetricDetail : Route("metric/{scanId}/{metricId}") {
        fun createRoute(scanId: String, metricId: String) = "metric/$scanId/$metricId"
    }
    data object Overlay : Route("overlay/{scanId}") {
        fun createRoute(scanId: String) = "overlay/$scanId"
    }
    data object ScanDetails : Route("scan_details/{scanId}") {
        fun createRoute(scanId: String) = "scan_details/$scanId"
    }
    data object Compare : Route("compare")

    // Stage 5: Skin results
    data object SkinResults : Route("skin/{scanId}") {
        fun createRoute(scanId: String) = "skin/$scanId"
    }

    // Stage 6: Coach, Chat, Plan, Routines
    data object ChatThread : Route("chat/{threadId}") {
        fun createRoute(threadId: String) = "chat/$threadId"
    }
    data object ChatHistory : Route("chat_history")
    data object Plan : Route("plan")
    data object Routine : Route("routine")
    data object WeeklyCheckIn : Route("weekly_check_in")

    // Stage 7 & 8: Settings, Privacy, Entitlements
    data object Settings : Route("settings")
    data object PrivacyCentre : Route("privacy_centre")
    data object Export : Route("export")
    data object DeleteAccount : Route("delete_account")
    data object Paywall : Route("paywall/{reason}") {
        fun createRoute(reason: String) = "paywall/$reason"
    }
    data object Subscription : Route("subscription")
    data object Legal : Route("legal/{doc}") {
        fun createRoute(doc: String) = "legal/$doc" // "privacy", "terms", "methodology", "medical"
    }
    data object NotificationSettings : Route("notification_settings")
    data object Appearance : Route("appearance")
    data object About : Route("about")
}
