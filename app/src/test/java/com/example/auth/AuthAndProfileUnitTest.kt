package com.example.auth

import com.example.domain.model.ConsentState
import com.example.domain.model.SessionState
import com.example.domain.model.UserProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class AuthAndProfileUnitTest {

    @Test
    fun testUserProfileDefaultsAndTeenMode() {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)

        val defaultProfile = UserProfile(uid = "test_user_123")
        assertEquals("test_user_123", defaultProfile.uid)
        assertEquals("not_sure", defaultProfile.skinType)
        assertTrue(defaultProfile.concerns.isEmpty())
        assertFalse(defaultProfile.isTeen)
        assertFalse(defaultProfile.isUnder13)

        // 15-year old user should have teen mode enabled
        val teenProfile = defaultProfile.copy(birthYear = currentYear - 15)
        assertTrue(teenProfile.isTeen)
        assertFalse(teenProfile.isUnder13)

        // 12-year old user should be flagged under 13
        val under13Profile = defaultProfile.copy(birthYear = currentYear - 12)
        assertTrue(under13Profile.isUnder13)
    }

    @Test
    fun testConsentStateDefaultValues() {
        val defaultConsent = ConsentState()
        assertEquals(1, defaultConsent.version)
        assertFalse(defaultConsent.onDeviceAnalysis)
        assertFalse(defaultConsent.cloudAiAnalysis)
        assertFalse(defaultConsent.cloudPhotoBackup)
        assertFalse(defaultConsent.analytics)
    }

    @Test
    fun testSessionStateHierarchy() {
        val loading: SessionState = SessionState.Loading
        val signedOut: SessionState = SessionState.SignedOut
        val onboarding: SessionState = SessionState.NeedsOnboarding(step = 2)
        val consent: SessionState = SessionState.NeedsConsent
        val ready: SessionState = SessionState.Ready(userId = "user_456")

        assertTrue(loading is SessionState.Loading)
        assertTrue(signedOut is SessionState.SignedOut)
        assertEquals(2, (onboarding as SessionState.NeedsOnboarding).step)
        assertTrue(consent is SessionState.NeedsConsent)
        assertEquals("user_456", (ready as SessionState.Ready).userId)
    }
}
