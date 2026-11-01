package com.example.repo

import com.example.core.AppDispatchers
import com.example.data.local.dao.BookmarkDao
import com.example.data.local.entity.ArticleBookmarkEntity
import com.example.data.repo.LibraryRepository
import com.example.domain.model.EvidenceLevel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

class FakeBookmarkDao : BookmarkDao {
    private val bookmarkedIds = MutableStateFlow<Set<String>>(emptySet())

    override fun observeAllBookmarkedIds(): Flow<List<String>> {
        return bookmarkedIds.map { it.toList() }
    }

    override fun observeIsBookmarked(articleId: String): Flow<Boolean> {
        return bookmarkedIds.map { it.contains(articleId) }
    }

    override suspend fun isBookmarked(articleId: String): Boolean {
        return bookmarkedIds.value.contains(articleId)
    }

    override suspend fun addBookmark(entity: ArticleBookmarkEntity) {
        bookmarkedIds.value = bookmarkedIds.value + entity.articleId
    }

    override suspend fun removeBookmark(articleId: String) {
        bookmarkedIds.value = bookmarkedIds.value - articleId
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class LibraryRepositoryTest {

    private fun loadJsonFromAssets(): String {
        // In local unit test, load from src/main/assets/library.json
        val file = File("src/main/assets/library.json")
        val altFile = File("app/src/main/assets/library.json")
        return if (file.exists()) file.readText() else altFile.readText()
    }

    @Test
    fun testLibraryJsonLoadsAndParsesCorrectly() {
        val json = loadJsonFromAssets()
        val (articles, glossary) = LibraryRepository.parseLibraryJson(json)

        // At least 30 articles and 30 glossary terms
        assertTrue("Articles count must be at least 30, got ${articles.size}", articles.size >= 30)
        assertEquals(30, glossary.size)

        // Validate all articles
        val requiredCategories = listOf(
            "Skin basics",
            "Acne and marks",
            "Sun protection",
            "Hair and haircuts by face shape",
            "Hair loss basics",
            "Facial hair and brows",
            "Teeth and smile",
            "Posture and neck",
            "Body fat and the face",
            "Sleep, hydration and diet",
            "Style, colours and glasses",
            "Photography",
            "Myths",
            "NOT_RECOMMENDED"
        )

        val presentCategories = articles.map { it.category }.toSet()
        requiredCategories.forEach { reqCat ->
            assertTrue("Category '$reqCat' must be present in library", presentCategories.contains(reqCat))
        }

        articles.forEach { article ->
            assertNotNull(article.id)
            assertTrue("Title must not be empty", article.title.isNotBlank())
            assertTrue("Summary must not be empty", article.summary.isNotBlank())
            assertTrue("whatToDo must not be empty", article.whatToDo.isNotEmpty())
            assertTrue("Min age must be at least 13", article.minAge >= 13)
            assertEquals("Educational information, not medical advice.", article.disclaimer)
        }
    }

    @Test
    fun testNotRecommendedArticlesAreProperlyFlagged() {
        val json = loadJsonFromAssets()
        val (articles, _) = LibraryRepository.parseLibraryJson(json)

        val notRec = articles.filter { it.category == "NOT_RECOMMENDED" || it.evidenceLevel == EvidenceLevel.NOT_RECOMMENDED }
        assertTrue("Must have NOT_RECOMMENDED articles", notRec.isNotEmpty())

        val ids = notRec.map { it.id }
        assertTrue("Must contain bone smashing", ids.any { it.contains("bone_smash") })
        assertTrue("Must contain DIY fillers", ids.any { it.contains("diy_filler") })
        assertTrue("Must contain unregulated hormones", ids.any { it.contains("hormone") })
        assertTrue("Must contain extreme dieting", ids.any { it.contains("diet") })
        assertTrue("Must contain harsh acid peels", ids.any { it.contains("acid_peel") })

        notRec.forEach { article ->
            assertTrue("Must contain warning risks", article.risks.isNotEmpty())
            assertTrue("Must contain whatToDo / safer alternatives", article.whatToDo.isNotEmpty())
        }
    }

    @Test
    fun testTeenModeArticleFiltering() {
        val json = loadJsonFromAssets()
        val (articles, _) = LibraryRepository.parseLibraryJson(json)

        val adultArticles = articles.filter { it.minAge >= 18 }
        val teenSafeArticles = articles.filter { it.isTeenSafe }

        assertTrue("Must have some articles marked age 18 for active ingredients", adultArticles.isNotEmpty())
        assertEquals(articles.size, adultArticles.size + teenSafeArticles.size)

        // Ensure retinoids is marked 18
        val retinoid = articles.firstOrNull { it.id == "topical_retinoids" }
        assertNotNull(retinoid)
        assertEquals(18, retinoid?.minAge)
    }

    @Test
    fun testGlossaryTermsAndCommunityConventionDefinitions() {
        val json = loadJsonFromAssets()
        val (_, glossary) = LibraryRepository.parseLibraryJson(json)

        assertEquals(30, glossary.size)
        val termIds = glossary.map { it.id }

        // Must include canthal tilt, FWHR, symmetry, midface, gonial angle, E-line, nasolabial angle, facial thirds
        assertTrue(termIds.contains("canthal_tilt"))
        assertTrue(termIds.contains("fwhr"))
        assertTrue(termIds.contains("symmetry"))
        assertTrue(termIds.contains("midface_ratio"))
        assertTrue(termIds.contains("gonial_angle"))
        assertTrue(termIds.contains("eline"))
        assertTrue(termIds.contains("nasolabial_angle"))
        assertTrue(termIds.contains("facial_thirds"))

        // Must include PSL, tiers, ratings explaining no scientific validity
        val psl = glossary.first { it.id == "psl" }
        val tiers = glossary.first { it.id == "tiers" }
        val ratings = glossary.first { it.id == "ratings" }

        assertTrue(psl.isCommunityOnly)
        assertTrue(tiers.isCommunityOnly)
        assertTrue(ratings.isCommunityOnly)

        assertTrue("PSL must explain community convention / no scientific validity",
            psl.whatItMeasures.contains("community convention", ignoreCase = true) ||
            psl.plainExplanation.contains("community convention", ignoreCase = true) ||
            psl.limits.contains("no scientific", ignoreCase = true)
        )
        assertTrue("Tiers must explain community convention / no scientific validity",
            tiers.whatItMeasures.contains("community convention", ignoreCase = true) ||
            tiers.limits.contains("unscientific", ignoreCase = true)
        )
        assertTrue("Ratings must explain community convention / no scientific validity",
            ratings.whatItMeasures.contains("community convention", ignoreCase = true) ||
            ratings.limits.contains("arbitrary", ignoreCase = true)
        )
    }

    @Test
    fun testBookmarkDaoToggling() = runBlocking {
        val fakeDao = FakeBookmarkDao()
        assertFalse(fakeDao.isBookmarked("cleanse_basics"))

        fakeDao.addBookmark(ArticleBookmarkEntity("cleanse_basics"))
        assertTrue(fakeDao.isBookmarked("cleanse_basics"))

        fakeDao.removeBookmark("cleanse_basics")
        assertFalse(fakeDao.isBookmarked("cleanse_basics"))
    }
}
