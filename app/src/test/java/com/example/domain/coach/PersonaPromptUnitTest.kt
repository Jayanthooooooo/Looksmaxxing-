package com.example.domain.coach

import com.example.core.AppConfig
import org.junit.Assert.assertTrue
import org.junit.Test

class PersonaPromptUnitTest {

    @Test
    fun testSystemInstructionContainsAllPersonaRules() {
        val prompt = AppConfig.SYSTEM_INSTRUCTION
        val bio = AppConfig.CREATOR_BIO

        assertTrue("Must declare coach name as Lumora Coach", prompt.contains("Lumora Coach"))
        assertTrue("Must state built by Jayant", prompt.contains("built by Jayant"))
        assertTrue("Must reference creator bio for questions about Jayant", prompt.contains("creator bio provided to you"))
        assertTrue("Must instruct not to discuss underlying technology", prompt.contains("do not discuss the underlying technology"))
        assertTrue("Must forbid claiming to be human", prompt.contains("Never claim to be a human"))
        assertTrue("Must forbid naming or claiming to be a different model or company", prompt.contains("Never name or claim to be a different model or company"))

        assertTrue("CREATOR_BIO constant must not be empty", bio.isNotBlank())
        assertTrue("CREATOR_BIO must mention Jayant", bio.contains("Jayant"))
    }
}
