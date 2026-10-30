package com.example.core

import com.example.domain.model.UserProfile
import com.example.ui.nav.Route
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class FoundationsUnitTest {

    @Test
    fun testAppConfigConstants() {
        assertEquals("Lumora", AppConfig.APP_NAME)
        assertEquals("1.0.0", AppConfig.VERSION_NAME)
        assertEquals("lumora-v1", AppConfig.ENGINE_VERSION)
        assertEquals(8, AppConfig.BURST_FRAME_COUNT)
        assertEquals(8, AppConfig.HOLD_FRAMES_REQUIRED)
    }

    @Test
    fun testOutcomeMappingAndAccessors() {
        val success: Outcome<Int> = Outcome.Success(42)
        assertTrue(success.isSuccess)
        assertFalse(success.isError)
        assertEquals(42, success.getOrNull())

        val mapped = success.map { "Value: $it" }
        assertEquals("Value: 42", mapped.getOrNull())

        val error: Outcome<Int> = Outcome.Error("Network failure")
        assertTrue(error.isError)
        assertFalse(error.isSuccess)
        assertNull(error.getOrNull())
        assertEquals("Network failure", (error as Outcome.Error).message)
    }

    @Test
    fun testEntitlementPolicyFreeVsPro() {
        val policy = EntitlementPolicy()

        // Free tier limits
        assertEquals(1, policy.maxAiSkinReads(EntitlementTier.FREE))
        assertEquals(5, policy.maxCoachMessagesPerDay(EntitlementTier.FREE))
        assertTrue(policy.canUseFeature(EntitlementTier.FREE, EntitlementFeature.UNLIMITED_LOCAL_SCANS))
        assertFalse(policy.canUseFeature(EntitlementTier.FREE, EntitlementFeature.CLOUD_PHOTO_BACKUP))
        assertFalse(policy.canUseFeature(EntitlementTier.FREE, EntitlementFeature.WEEKLY_AI_REPORT))
        assertFalse(policy.canUseFeature(EntitlementTier.FREE, EntitlementFeature.PDF_EXPORT))
        assertFalse(policy.canUseFeature(EntitlementTier.FREE, EntitlementFeature.ADVANCED_COMPARE))

        // Pro tier limits
        assertEquals(10, policy.maxAiSkinReads(EntitlementTier.PRO))
        assertEquals(100, policy.maxCoachMessagesPerDay(EntitlementTier.PRO))
        assertTrue(policy.canUseFeature(EntitlementTier.PRO, EntitlementFeature.CLOUD_PHOTO_BACKUP))
        assertTrue(policy.canUseFeature(EntitlementTier.PRO, EntitlementFeature.WEEKLY_AI_REPORT))
        assertTrue(policy.canUseFeature(EntitlementTier.PRO, EntitlementFeature.PDF_EXPORT))
        assertTrue(policy.canUseFeature(EntitlementTier.PRO, EntitlementFeature.ADVANCED_COMPARE))
    }

    @Test
    fun testUserProfileAgeChecks() {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)

        // Adult user (age 25)
        val adult = UserProfile(birthYear = currentYear - 25)
        assertFalse(adult.isTeen)
        assertFalse(adult.isUnder13)

        // Teen user (age 16)
        val teen = UserProfile(birthYear = currentYear - 16)
        assertTrue(teen.isTeen)
        assertFalse(teen.isUnder13)

        // Under 13 child (age 11)
        val child = UserProfile(birthYear = currentYear - 11)
        assertTrue(child.isUnder13)
    }

    @Test
    fun testNavigationRoutesCompleteness() {
        val routes = listOf(
            Route.Splash.route,
            Route.Welcome.route,
            Route.AuthChoice.route,
            Route.SignUp.route,
            Route.SignIn.route,
            Route.ForgotPassword.route,
            Route.VerifyEmail.route,
            Route.Onboarding.route,
            Route.Consent.route,
            Route.NotificationRationale.route,
            Route.Main.route,
            Route.Today.route,
            Route.Progress.route,
            Route.Coach.route,
            Route.You.route,
            Route.ScanHub.route,
            Route.CameraRationale.route,
            Route.Capture.route,
            Route.Review.route,
            Route.Processing.route,
            Route.Results.route,
            Route.MetricDetail.route,
            Route.Overlay.route,
            Route.ScanDetails.route,
            Route.Compare.route,
            Route.SkinResults.route,
            Route.ChatThread.route,
            Route.ChatHistory.route,
            Route.Plan.route,
            Route.Routine.route,
            Route.WeeklyCheckIn.route,
            Route.Settings.route,
            Route.PrivacyCentre.route,
            Route.Export.route,
            Route.DeleteAccount.route,
            Route.Paywall.route,
            Route.Subscription.route,
            Route.Legal.route,
            Route.NotificationSettings.route,
            Route.Appearance.route,
            Route.About.route
        )

        assertEquals(41, routes.size)
        routes.forEach { route ->
            assertTrue(route.isNotBlank())
        }
    }
}
