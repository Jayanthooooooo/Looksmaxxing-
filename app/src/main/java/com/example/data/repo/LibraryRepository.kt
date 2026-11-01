package com.example.data.repo

import android.content.Context
import com.example.core.AppDispatchers
import com.example.data.local.dao.BookmarkDao
import com.example.data.local.entity.ArticleBookmarkEntity
import com.example.domain.model.ArticleCost
import com.example.domain.model.ChangeabilityTag
import com.example.domain.model.EvidenceLevel
import com.example.domain.model.GlossaryTerm
import com.example.domain.model.LibraryArticle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONObject

class LibraryRepository(
    private val context: Context,
    private val bookmarkDao: BookmarkDao,
    private val dispatchers: AppDispatchers
) {
    private val _articles = MutableStateFlow<List<LibraryArticle>>(emptyList())
    val articles = _articles.asStateFlow()

    private val _glossary = MutableStateFlow<List<GlossaryTerm>>(emptyList())
    val glossary = _glossary.asStateFlow()

    val bookmarkedArticleIds: Flow<List<String>> = bookmarkDao.observeAllBookmarkedIds()

    fun observeIsBookmarked(articleId: String): Flow<Boolean> =
        bookmarkDao.observeIsBookmarked(articleId)

    suspend fun toggleBookmark(articleId: String) = withContext(dispatchers.io) {
        if (bookmarkDao.isBookmarked(articleId)) {
            bookmarkDao.removeBookmark(articleId)
        } else {
            bookmarkDao.addBookmark(ArticleBookmarkEntity(articleId = articleId))
        }
    }

    suspend fun loadLibrary(): Boolean = withContext(dispatchers.io) {
        if (_articles.value.isNotEmpty() && _glossary.value.isNotEmpty()) {
            return@withContext true
        }
        try {
            val jsonString = context.assets.open("library.json").bufferedReader().use { it.readText() }
            val (articlesList, glossaryList) = parseLibraryJson(jsonString)
            _articles.value = articlesList
            _glossary.value = glossaryList
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun getArticleById(id: String): LibraryArticle? = withContext(dispatchers.io) {
        if (_articles.value.isEmpty()) loadLibrary()
        _articles.value.firstOrNull { it.id == id }
    }

    companion object {
        fun parseLibraryJson(jsonString: String): Pair<List<LibraryArticle>, List<GlossaryTerm>> {
            val root = JSONObject(jsonString)
            val articlesArray = root.getJSONArray("articles")
            val articles = mutableListOf<LibraryArticle>()
            for (i in 0 until articlesArray.length()) {
                val item = articlesArray.getJSONObject(i)
                val whatToDoJson = item.optJSONArray("whatToDo")
                val whatToDo = mutableListOf<String>()
                if (whatToDoJson != null) {
                    for (j in 0 until whatToDoJson.length()) {
                        whatToDo.add(whatToDoJson.getString(j))
                    }
                }

                val risksJson = item.optJSONArray("risks")
                val risks = mutableListOf<String>()
                if (risksJson != null) {
                    for (j in 0 until risksJson.length()) {
                        risks.add(risksJson.getString(j))
                    }
                }

                val proJson = item.optJSONArray("seeAProfessionalIf")
                val seeAProfessionalIf = mutableListOf<String>()
                if (proJson != null) {
                    for (j in 0 until proJson.length()) {
                        seeAProfessionalIf.add(proJson.getString(j))
                    }
                }

                val tagsJson = item.optJSONArray("tags")
                val tags = mutableListOf<String>()
                if (tagsJson != null) {
                    for (j in 0 until tagsJson.length()) {
                        tags.add(tagsJson.getString(j))
                    }
                }

                val evidenceStr = item.getString("evidenceLevel")
                val evidenceLevel = try {
                    EvidenceLevel.valueOf(evidenceStr)
                } catch (e: Exception) {
                    EvidenceLevel.LIMITED
                }

                val costStr = item.optString("cost", "LOW")
                val cost = try {
                    ArticleCost.valueOf(costStr)
                } catch (e: Exception) {
                    ArticleCost.LOW
                }

                val changeabilityStr = item.optString("changeability", "PRESENTATION")
                val changeability = try {
                    ChangeabilityTag.valueOf(changeabilityStr)
                } catch (e: Exception) {
                    ChangeabilityTag.PRESENTATION
                }

                articles.add(
                    LibraryArticle(
                        id = item.getString("id"),
                        category = item.getString("category"),
                        title = item.getString("title"),
                        summary = item.getString("summary"),
                        evidenceLevel = evidenceLevel,
                        whatToDo = whatToDo,
                        timeToSeeResults = item.optString("timeToSeeResults", "Varies"),
                        cost = cost,
                        risks = risks,
                        seeAProfessionalIf = seeAProfessionalIf,
                        changeability = changeability,
                        minAge = item.optInt("minAge", 13),
                        tags = tags,
                        disclaimer = item.optString("disclaimer", "Educational information, not medical advice.")
                    )
                )
            }

            val glossaryArray = root.getJSONArray("glossary")
            val glossary = mutableListOf<GlossaryTerm>()
            for (i in 0 until glossaryArray.length()) {
                val item = glossaryArray.getJSONObject(i)
                glossary.add(
                    GlossaryTerm(
                        id = item.getString("id"),
                        term = item.getString("term"),
                        plainExplanation = item.getString("plainExplanation"),
                        whatItMeasures = item.getString("whatItMeasures"),
                        limits = item.getString("limits"),
                        isCommunityOnly = item.optBoolean("isCommunityOnly", false)
                    )
                )
            }

            return Pair(articles, glossary)
        }
    }
}
