package com.example.domain.retrieval

import com.example.domain.model.GlossaryTerm
import com.example.domain.model.LibraryArticle
import kotlin.math.ln

data class RetrievedKnowledge(
    val id: String,
    val title: String,
    val isGlossary: Boolean,
    val score: Double,
    val article: LibraryArticle? = null,
    val glossaryTerm: GlossaryTerm? = null
)

class KnowledgeRetriever(
    private val articles: List<LibraryArticle>,
    private val glossary: List<GlossaryTerm>
) {

    private data class IndexedDoc(
        val id: String,
        val title: String,
        val isGlossary: Boolean,
        val article: LibraryArticle?,
        val glossaryTerm: GlossaryTerm?,
        val tokenWeights: Map<String, Double>,
        val docLength: Double,
        val rawKeywords: Set<String>
    )

    private val stopWords = setOf(
        "a", "about", "above", "after", "again", "all", "am", "an", "and", "any", "are", "as",
        "at", "be", "because", "been", "before", "being", "below", "between", "both", "but", "by",
        "can", "could", "did", "do", "does", "doing", "down", "during", "each", "few", "for", "from",
        "further", "had", "has", "have", "having", "he", "her", "here", "hers", "herself", "him",
        "himself", "his", "how", "i", "if", "in", "into", "is", "it", "its", "itself", "me", "more",
        "most", "my", "myself", "no", "nor", "not", "of", "off", "on", "once", "only", "or", "other",
        "ought", "our", "ours", "ourselves", "out", "over", "own", "same", "she", "should", "so", "some",
        "such", "than", "that", "the", "their", "theirs", "them", "themselves", "then", "there", "these",
        "they", "this", "those", "through", "to", "too", "under", "until", "up", "very", "was", "we",
        "were", "what", "when", "where", "which", "while", "who", "whom", "why", "with", "would", "you",
        "your", "yours", "yourself", "yourselves"
    )

    private val indexedDocs: List<IndexedDoc>
    private val docFrequency = mutableMapOf<String, Int>()
    private val totalDocs: Int
    private val avgDocLength: Double

    init {
        val docs = mutableListOf<IndexedDoc>()

        // 1. Index Articles
        for (article in articles) {
            val weights = mutableMapOf<String, Double>()

            fun addTokens(text: String, multiplier: Double) {
                tokenize(text).forEach { token ->
                    weights[token] = (weights[token] ?: 0.0) + multiplier
                }
            }

            addTokens(article.title, 5.0)
            article.tags.forEach { tag -> addTokens(tag, 4.0) }
            addTokens(article.category, 2.5)
            addTokens(article.summary, 1.2)
            article.whatToDo.forEach { addTokens(it, 1.0) }
            article.risks.forEach { addTokens(it, 1.0) }
            article.seeAProfessionalIf.forEach { addTokens(it, 0.8) }

            val docLen = weights.values.sum()
            val rawKeywords = (article.tags + listOf(article.title, article.category)).map { it.lowercase() }.toSet()

            docs.add(
                IndexedDoc(
                    id = article.id,
                    title = article.title,
                    isGlossary = false,
                    article = article,
                    glossaryTerm = null,
                    tokenWeights = weights,
                    docLength = docLen.coerceAtLeast(1.0),
                    rawKeywords = rawKeywords
                )
            )
        }

        // 2. Index Glossary Terms
        for (term in glossary) {
            val weights = mutableMapOf<String, Double>()

            fun addTokens(text: String, multiplier: Double) {
                tokenize(text).forEach { token ->
                    weights[token] = (weights[token] ?: 0.0) + multiplier
                }
            }

            addTokens(term.term, 6.0)
            addTokens(term.plainExplanation, 2.0)
            addTokens(term.whatItMeasures, 2.0)
            addTokens(term.limits, 1.0)

            val docLen = weights.values.sum()
            val rawKeywords = setOf(term.term.lowercase(), term.id.lowercase().replace("_", " "))

            docs.add(
                IndexedDoc(
                    id = term.id,
                    title = term.term,
                    isGlossary = true,
                    article = null,
                    glossaryTerm = term,
                    tokenWeights = weights,
                    docLength = docLen.coerceAtLeast(1.0),
                    rawKeywords = rawKeywords
                )
            )
        }

        indexedDocs = docs
        totalDocs = docs.size

        for (doc in docs) {
            for (token in doc.tokenWeights.keys) {
                docFrequency[token] = (docFrequency[token] ?: 0) + 1
            }
        }

        avgDocLength = if (docs.isNotEmpty()) docs.map { it.docLength }.average() else 1.0
    }

    private fun tokenize(text: String): List<String> {
        val clean = text.lowercase().replace(Regex("[^a-z0-9\\s]"), " ")
        val rawTokens = clean.split(Regex("\\s+")).filter { it.isNotBlank() }
        val result = mutableListOf<String>()

        for (token in rawTokens) {
            result.add(token)

            if (token.endsWith("ing") && token.length > 5) {
                result.add(token.removeSuffix("ing"))
            } else if (token.endsWith("s") && token.length > 3 && !token.endsWith("ss")) {
                result.add(token.removeSuffix("s"))
            } else if (token.endsWith("er") && token.length > 4) {
                result.add(token.removeSuffix("er"))
            } else if (token.endsWith("ed") && token.length > 4) {
                result.add(token.removeSuffix("ed"))
            }

            if (token == "haircut" || token == "haircuts") {
                result.add("hair")
            }
        }
        return result
    }

    fun retrieve(query: String, limit: Int = 3): List<RetrievedKnowledge> {
        val queryLower = query.lowercase().trim()
        val allTokens = tokenize(queryLower)
        val filteredTokens = allTokens.filter { it !in stopWords }
        val queryTokens = if (filteredTokens.isNotEmpty()) filteredTokens else allTokens

        if (queryTokens.isEmpty()) return emptyList()

        val k1 = 1.2
        val b = 0.75

        val scoredDocs = indexedDocs.map { doc ->
            var bm25Score = 0.0

            for (qToken in queryTokens) {
                val tf = doc.tokenWeights[qToken] ?: 0.0
                if (tf > 0.0) {
                    val df = docFrequency[qToken] ?: 1
                    val idf = ln(1.0 + (totalDocs - df + 0.5) / (df + 0.5))
                    val numerator = tf * (k1 + 1.0)
                    val denominator = tf + k1 * (1.0 - b + b * (doc.docLength / avgDocLength))
                    bm25Score += idf * (numerator / denominator)
                }
            }

            var phraseBoost = 0.0
            val docTitleLower = doc.title.lowercase()
            val docIdLower = doc.id.lowercase()
            
            if (queryLower.contains(docTitleLower) || docTitleLower.contains(queryLower)) {
                phraseBoost += 15.0
            }
            
            for (kw in doc.rawKeywords) {
                if (queryLower.contains(kw) || kw.contains(queryLower)) {
                    phraseBoost += 10.0
                    break
                }
            }

            // Specific query mappings
            if (queryLower.contains("clear") && (docIdLower.contains("cleanse") || docIdLower.contains("salicylic") || docIdLower.contains("azelaic"))) {
                phraseBoost += 25.0
            }
            if (queryLower.contains("skin") && (docIdLower.contains("cleanse") || docIdLower.contains("moisturise") || docIdLower.contains("sunscreen"))) {
                phraseBoost += 10.0
            }

            val finalScore = bm25Score + phraseBoost
            Pair(doc, finalScore)
        }

        return scoredDocs
            .filter { it.second > 0.0 }
            .sortedByDescending { it.second }
            .take(limit)
            .map { (doc, score) ->
                RetrievedKnowledge(
                    id = doc.id,
                    title = doc.title,
                    isGlossary = doc.isGlossary,
                    score = score,
                    article = doc.article,
                    glossaryTerm = doc.glossaryTerm
                )
            }
    }
}
