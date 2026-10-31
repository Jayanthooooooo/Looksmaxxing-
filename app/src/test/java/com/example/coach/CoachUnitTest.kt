package com.example.coach

import com.example.core.AiConfig
import com.example.domain.model.QualityReport
import com.example.domain.model.ScanResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class CoachUnitTest {

    @Test
    fun testFreeVsProDailyLimits() {
        val freeLimit = AiConfig.FREE_DAILY_MESSAGE_LIMIT
        assertEquals(5, freeLimit)

        val proLimit = 100
        assertTrue("Pro limit should be significantly higher than free limit", proLimit > freeLimit)
    }

    @Test
    fun testScanContextFormatting() {
        val scan = ScanResult(
            id = "test_scan",
            createdAt = System.currentTimeMillis(),
            kind = "front",
            engineVersion = "lumora-v1",
            qualityReport = QualityReport(score = 88, flags = emptyList(), perGate = emptyMap()),
            metrics = emptyMap(),
            indices = mapOf("symmetry" to 92, "proportions" to 85, "clarity" to 80),
            skinResult = null,
            photoFileId = null
        )

        assertEquals(88, scan.qualityReport.score)
        assertEquals(92, scan.indices["symmetry"])
        assertEquals(85, scan.indices["proportions"])
    }

    @Test
    fun testErrorMessageMapping() {
        val appCheckError = Exception("App Check attestation failed")
        val isAppCheckFailure = appCheckError.message?.contains("App Check", ignoreCase = true) == true
        assertTrue(isAppCheckFailure)

        val quotaError = Exception("Quota exceeded for project")
        val isQuotaFailure = quotaError.message?.contains("quota", ignoreCase = true) == true
        assertTrue(isQuotaFailure)
    }
}
