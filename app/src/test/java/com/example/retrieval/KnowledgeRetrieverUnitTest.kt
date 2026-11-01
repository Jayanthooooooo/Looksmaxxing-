package com.example.retrieval

import com.example.data.repo.LibraryRepository
import com.example.domain.retrieval.KnowledgeRetriever
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class KnowledgeRetrieverUnitTest {

    private fun loadJsonFromAssets(): String {
        val file = File("src/main/assets/library.json")
        val altFile = File("app/src/main/assets/library.json")
        return if (file.exists()) file.readText() else altFile.readText()
    }

    @Test
    fun testRetrieverRetrievesRelevantArticlesFor6Questions() {
        val json = loadJsonFromAssets()
        val (articles, glossary) = LibraryRepository.parseLibraryJson(json)
        val retriever = KnowledgeRetriever(articles, glossary)

        // 1. "how do I get clearer skin"
        val r1 = retriever.retrieve("how do I get clearer skin", 3)
        println("R1: ${r1.map { it.id }}")
        assertTrue(r1.isNotEmpty())
        assertTrue(r1.any { it.id.contains("cleanse") || it.id.contains("azelaic") || it.id.contains("salicylic") })

        // 2. "does mewing work"
        val r2 = retriever.retrieve("does mewing work", 3)
        println("R2: ${r2.map { it.id }}")
        assertTrue(r2.isNotEmpty())
        assertTrue(r2.any { it.id.contains("mewing") })

        // 3. "what is canthal tilt"
        val r3 = retriever.retrieve("what is canthal tilt", 3)
        println("R3: ${r3.map { it.id }}")
        assertTrue(r3.isNotEmpty())
        assertTrue(r3.any { it.id.contains("canthal_tilt") })

        // 4. "which haircut suits me"
        val r4 = retriever.retrieve("which haircut suits me", 3)
        println("R4: ${r4.map { it.id }}")
        assertTrue(r4.isNotEmpty())
        assertTrue(r4.any { it.id.contains("haircut") })

        // 5. "should I use steroids"
        val r5 = retriever.retrieve("should I use steroids", 3)
        println("R5: ${r5.map { it.id }}")
        assertTrue(r5.isNotEmpty())
        assertTrue(r5.any { it.id.contains("hormone") || it.id.contains("steroid") || it.id.contains("not_rec") || it.title.contains("Steroids", ignoreCase = true) || it.title.contains("Hormones", ignoreCase = true) })

        // 6. "how to fix forward head"
        val r6 = retriever.retrieve("how to fix forward head", 3)
        println("R6: ${r6.map { it.id }}")
        assertTrue(r6.isNotEmpty())
        assertTrue(r6.any { it.id.contains("forward_head") })
    }
}
