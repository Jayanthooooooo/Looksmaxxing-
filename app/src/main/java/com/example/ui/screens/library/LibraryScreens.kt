package com.example.ui.screens.library

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.ArticleCost
import com.example.domain.model.ChangeabilityTag
import com.example.domain.model.EvidenceLevel
import com.example.domain.model.GlossaryTerm
import com.example.domain.model.LibraryArticle
import com.example.ui.theme.Fraunces
import com.example.ui.theme.GoldEyebrow
import com.example.ui.theme.LumoraColors
import com.example.ui.theme.PlusJakartaSans
import com.example.ui.theme.SerifHeadline

@Composable
fun LibraryScreen(
    articles: List<LibraryArticle>,
    bookmarkedIds: Set<String>,
    isTeenMode: Boolean,
    onArticleClick: (String) -> Unit,
    onGlossaryClick: () -> Unit,
    onToggleBookmark: (String) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    val categories = remember(articles) {
        listOf("All", "Saved") + articles.map { it.category }.distinct()
    }

    // Filter by teen mode, category, and search query
    val visibleArticles = remember(articles, isTeenMode, selectedCategory, searchQuery, bookmarkedIds) {
        articles.filter { article ->
            // Teen mode rule: hide articles requiring age 18
            if (isTeenMode && article.minAge >= 18) return@filter false

            // Category filter
            val matchesCategory = when (selectedCategory) {
                "All" -> true
                "Saved" -> bookmarkedIds.contains(article.id)
                else -> article.category.equals(selectedCategory, ignoreCase = true)
            }
            if (!matchesCategory) return@filter false

            // Search query filter
            if (searchQuery.isNotBlank()) {
                val q = searchQuery.trim().lowercase()
                val matchTitle = article.title.lowercase().contains(q)
                val matchSummary = article.summary.lowercase().contains(q)
                val matchCategory = article.category.lowercase().contains(q)
                val matchTags = article.tags.any { it.lowercase().contains(q) }
                matchTitle || matchSummary || matchCategory || matchTags
            } else {
                true
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LumoraColors.DarkBackground)
    ) {
        // Soft warm-brown radial glow
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(LumoraColors.TopRightWarmGlow)
        )

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp, start = 20.dp, end = 20.dp, bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onBackClick,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0x22FFFFFF))
                                .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)), CircleShape)
                                .testTag("library_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        GoldEyebrow(text = "EVIDENCE LIBRARY")

                        Spacer(modifier = Modifier.size(40.dp))
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    SerifHeadline(
                        text = "Knowledge Base",
                        italicWord = "Base",
                        fontSize = 28.sp,
                        lineHeight = 34.sp
                    )
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                // Glossary banner card
                Surface(
                    onClick = onGlossaryClick,
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0x18D9C59B),
                    border = BorderStroke(1.dp, Color(0x44D9C59B)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("library_glossary_entry_card")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x33D9C59B)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MenuBook,
                                    contentDescription = null,
                                    tint = LumoraColors.ChampagneGold,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "FACIAL METRICS & ANATOMY",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp,
                                    letterSpacing = 1.2.sp,
                                    color = LumoraColors.ChampagneGold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "30-Term Glossary",
                                    fontFamily = Fraunces,
                                    fontSize = 18.sp,
                                    color = Color(0xFFF4F4F6)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Canthal tilt, FWHR, symmetry & community myths.",
                                    fontFamily = PlusJakartaSans,
                                    fontSize = 12.sp,
                                    color = LumoraColors.DarkOnSurfaceMuted
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = "Open Glossary",
                            tint = LumoraColors.ChampagneGold,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Search input
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            text = "Search skin, hair, posture, myths...",
                            fontFamily = PlusJakartaSans,
                            fontSize = 14.sp,
                            color = Color(0x77FFFFFF)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = LumoraColors.ChampagneGold,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear",
                                    tint = Color.White.copy(alpha = 0.6f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0x18FFFFFF),
                        unfocusedContainerColor = Color(0x10FFFFFF),
                        focusedBorderColor = LumoraColors.ChampagneGold,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                        focusedTextColor = Color(0xFFF4F4F6),
                        unfocusedTextColor = Color(0xFFF4F4F6)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("library_search_input")
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Category chips scroll
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { category ->
                        val isSelected = selectedCategory.equals(category, ignoreCase = true)
                        Surface(
                            onClick = { selectedCategory = category },
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) LumoraColors.ChampagneGold else Color(0x14FFFFFF),
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) LumoraColors.ChampagneGold else Color.White.copy(alpha = 0.10f)
                            ),
                            modifier = Modifier.testTag("category_chip_$category")
                        ) {
                            Text(
                                text = category,
                                fontFamily = PlusJakartaSans,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                fontSize = 12.sp,
                                color = if (isSelected) Color(0xFF0B0B0F) else Color(0xFFF4F4F6),
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Teen Mode alert banner if teen mode active
                if (isTeenMode) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0x1ADB8B28),
                        border = BorderStroke(1.dp, Color(0x44DB8B28)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = LumoraColors.WarningAmber,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Teen mode active: Articles with active ingredients (like retinoids or chemical peels) are hidden. Ask a parent or dermatologist before trying active skincare ingredients.",
                                fontFamily = PlusJakartaSans,
                                fontSize = 12.sp,
                                lineHeight = 17.sp,
                                color = Color(0xFFF7EBD6)
                            )
                        }
                    }
                }

                // Article list
                if (visibleArticles.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.HelpOutline,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.4f),
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "No matching articles found",
                                fontFamily = Fraunces,
                                fontSize = 18.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Try adjusting your search terms or filters.",
                                fontFamily = PlusJakartaSans,
                                fontSize = 13.sp,
                                color = LumoraColors.DarkOnSurfaceMuted
                            )
                        }
                    }
                } else {
                    visibleArticles.forEach { article ->
                        val isBookmarked = bookmarkedIds.contains(article.id)
                        ArticleCard(
                            article = article,
                            isBookmarked = isBookmarked,
                            onClick = { onArticleClick(article.id) },
                            onToggleBookmark = { onToggleBookmark(article.id) }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Educational information, not medical advice.",
                    fontFamily = PlusJakartaSans,
                    fontSize = 11.sp,
                    color = Color(0x66FFFFFF),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                )
            }
        }
    }
}

@Composable
fun ArticleCard(
    article: LibraryArticle,
    isBookmarked: Boolean,
    onClick: () -> Unit,
    onToggleBookmark: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isNotRecommended = article.evidenceLevel == EvidenceLevel.NOT_RECOMMENDED
    val cardBg = if (isNotRecommended) Color(0x223A1215) else Color(0x14FFFFFF)
    val cardBorder = if (isNotRecommended) Color(0x55F87171) else Color.White.copy(alpha = 0.10f)

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = cardBg,
        border = BorderStroke(1.dp, cardBorder),
        modifier = modifier
            .fillMaxWidth()
            .testTag("article_card_${article.id}")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = article.category.uppercase(),
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 10.sp,
                    letterSpacing = 1.5.sp,
                    color = if (isNotRecommended) LumoraColors.ErrorCoral else LumoraColors.ChampagneGold
                )

                IconButton(
                    onClick = onToggleBookmark,
                    modifier = Modifier.size(28.dp).testTag("bookmark_btn_${article.id}")
                ) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = if (isBookmarked) "Bookmarked" else "Bookmark",
                        tint = if (isBookmarked) LumoraColors.ChampagneGold else Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = article.title,
                fontFamily = Fraunces,
                fontWeight = FontWeight.Normal,
                fontSize = 18.sp,
                lineHeight = 24.sp,
                color = Color(0xFFF4F4F6)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = article.summary,
                fontFamily = PlusJakartaSans,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                color = LumoraColors.DarkOnSurfaceMuted,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                EvidenceBadge(level = article.evidenceLevel)

                Text(
                    text = article.timeToSeeResults,
                    fontFamily = PlusJakartaSans,
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
fun EvidenceBadge(level: EvidenceLevel, modifier: Modifier = Modifier) {
    val (bgColor, borderColor, textColor, icon) = when (level) {
        EvidenceLevel.STRONG -> Quadruple(
            Color(0x224ADE80),
            Color(0x554ADE80),
            Color(0xFF86EFAC),
            Icons.Default.CheckCircle
        )
        EvidenceLevel.MODERATE -> Quadruple(
            Color(0x22D9C59B),
            Color(0x55D9C59B),
            LumoraColors.ChampagneGold,
            Icons.Default.CheckCircle
        )
        EvidenceLevel.LIMITED -> Quadruple(
            Color(0x22FBBF24),
            Color(0x44FBBF24),
            Color(0xFFFDE68A),
            Icons.Default.HelpOutline
        )
        EvidenceLevel.MYTH -> Quadruple(
            Color(0x22FB923C),
            Color(0x55FB923C),
            Color(0xFFFDBA74),
            Icons.Default.ErrorOutline
        )
        EvidenceLevel.NOT_RECOMMENDED -> Quadruple(
            Color(0x33F87171),
            Color(0x77F87171),
            Color(0xFFFCA5A5),
            Icons.Default.Warning
        )
    }

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = bgColor,
        border = BorderStroke(1.dp, borderColor),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = level.badgeText,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
                color = textColor
            )
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
fun ArticleDetailScreen(
    article: LibraryArticle,
    isBookmarked: Boolean,
    onToggleBookmark: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isNotRecommended = article.evidenceLevel == EvidenceLevel.NOT_RECOMMENDED

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LumoraColors.DarkBackground)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(LumoraColors.TopRightWarmGlow)
        )

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp, start = 20.dp, end = 20.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0x22FFFFFF))
                            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)), CircleShape)
                            .testTag("article_detail_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    GoldEyebrow(text = article.category.uppercase())

                    IconButton(
                        onClick = onToggleBookmark,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0x22FFFFFF))
                            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)), CircleShape)
                            .testTag("article_detail_bookmark_toggle")
                    ) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = if (isBookmarked) "Bookmarked" else "Bookmark",
                            tint = if (isBookmarked) LumoraColors.ChampagneGold else Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                // Title
                Text(
                    text = article.title,
                    fontFamily = Fraunces,
                    fontSize = 26.sp,
                    lineHeight = 32.sp,
                    color = Color(0xFFF4F4F6)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Badges row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    EvidenceBadge(level = article.evidenceLevel)

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0x18FFFFFF),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.10f))
                    ) {
                        Text(
                            text = article.changeability.label,
                            fontFamily = PlusJakartaSans,
                            fontSize = 11.sp,
                            color = Color(0xCCFFFFFF),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    if (article.minAge >= 18) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0x22FBBF24),
                            border = BorderStroke(1.dp, Color(0x55FBBF24))
                        ) {
                            Text(
                                text = "Age 18+ Actives",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp,
                                color = Color(0xFFFDE68A),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // NOT RECOMMENDED WARNING BANNER
                if (isNotRecommended) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0x33F87171),
                        border = BorderStroke(1.5.dp, LumoraColors.ErrorCoral),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = LumoraColors.ErrorCoral,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "NOT RECOMMENDED · DANGER",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    letterSpacing = 1.sp,
                                    color = LumoraColors.ErrorCoral
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "This practice carries severe risks of irreversible injury or systemic health harm. Safer, evidence-based alternatives are provided below.",
                                fontFamily = PlusJakartaSans,
                                fontSize = 13.sp,
                                lineHeight = 18.sp,
                                color = Color(0xFFFEE2E2)
                            )
                        }
                    }
                }

                // Summary Card
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0x14FFFFFF),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.10f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "OVERVIEW",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 10.sp,
                            letterSpacing = 1.5.sp,
                            color = LumoraColors.ChampagneGold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = article.summary,
                            fontFamily = PlusJakartaSans,
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            color = Color(0xFFF4F4F6)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // WHAT TO DO / SAFER ALTERNATIVES
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0x14FFFFFF),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.10f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = if (isNotRecommended) "SAFER ALTERNATIVES & GUIDANCE" else "WHAT TO DO",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 10.sp,
                            letterSpacing = 1.5.sp,
                            color = LumoraColors.ChampagneGold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        article.whatToDo.forEachIndexed { idx, step ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(Color(0x22D9C59B)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${idx + 1}",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = LumoraColors.ChampagneGold
                                    )
                                }
                                Text(
                                    text = step,
                                    fontFamily = PlusJakartaSans,
                                    fontSize = 13.sp,
                                    lineHeight = 19.sp,
                                    color = Color(0xFFF4F4F6),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Timeline & Cost Card
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0x14FFFFFF),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.10f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "TIMELINE",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 10.sp,
                                letterSpacing = 1.2.sp,
                                color = LumoraColors.ChampagneGold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = article.timeToSeeResults,
                                fontFamily = Fraunces,
                                fontSize = 15.sp,
                                color = Color(0xFFF4F4F6)
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "COST PROFILE",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 10.sp,
                                letterSpacing = 1.2.sp,
                                color = LumoraColors.ChampagneGold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = article.cost.label,
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = Color(0xFFF4F4F6)
                            )
                        }
                    }
                }

                // Risks section
                if (article.risks.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0x18F87171),
                        border = BorderStroke(1.dp, Color(0x44F87171)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = LumoraColors.ErrorCoral,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "RISKS & CAUTIONS",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 10.sp,
                                    letterSpacing = 1.5.sp,
                                    color = LumoraColors.ErrorCoral
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            article.risks.forEach { risk ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "•",
                                        color = LumoraColors.ErrorCoral,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = risk,
                                        fontFamily = PlusJakartaSans,
                                        fontSize = 13.sp,
                                        lineHeight = 18.sp,
                                        color = Color(0xFFFEE2E2),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }

                // When to see a professional
                if (article.seeAProfessionalIf.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0x14FFFFFF),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.10f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = LumoraColors.ChampagneGold,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "WHEN TO CONSULT A DOCTOR",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 10.sp,
                                    letterSpacing = 1.5.sp,
                                    color = LumoraColors.ChampagneGold
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            article.seeAProfessionalIf.forEach { pro ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "•",
                                        color = LumoraColors.ChampagneGold,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = pro,
                                        fontFamily = PlusJakartaSans,
                                        fontSize = 13.sp,
                                        lineHeight = 18.sp,
                                        color = Color(0xFFF4F4F6),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Mandatory disclaimer
                Text(
                    text = article.disclaimer,
                    fontFamily = PlusJakartaSans,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
                )
            }
        }
    }
}

@Composable
fun GlossaryScreen(
    terms: List<GlossaryTerm>,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredTerms = remember(terms, searchQuery) {
        if (searchQuery.isBlank()) {
            terms
        } else {
            val q = searchQuery.trim().lowercase()
            terms.filter {
                it.term.lowercase().contains(q) ||
                it.plainExplanation.lowercase().contains(q) ||
                it.whatItMeasures.lowercase().contains(q) ||
                it.limits.lowercase().contains(q)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LumoraColors.DarkBackground)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(LumoraColors.TopRightWarmGlow)
        )

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp, start = 20.dp, end = 20.dp, bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onBackClick,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0x22FFFFFF))
                                .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)), CircleShape)
                                .testTag("glossary_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        GoldEyebrow(text = "GLOSSARY")

                        Spacer(modifier = Modifier.size(40.dp))
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    SerifHeadline(
                        text = "Facial Terms & Anatomy",
                        italicWord = "Terms",
                        fontSize = 28.sp,
                        lineHeight = 34.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Scientific measurements, landmarks, and community conventions explained.",
                        fontFamily = PlusJakartaSans,
                        fontSize = 13.sp,
                        color = LumoraColors.DarkOnSurfaceMuted
                    )
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                // Search field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            text = "Search terms (e.g. canthal tilt, FWHR, PSL)...",
                            fontFamily = PlusJakartaSans,
                            fontSize = 14.sp,
                            color = Color(0x77FFFFFF)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = LumoraColors.ChampagneGold,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear",
                                    tint = Color.White.copy(alpha = 0.6f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0x18FFFFFF),
                        unfocusedContainerColor = Color(0x10FFFFFF),
                        focusedBorderColor = LumoraColors.ChampagneGold,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                        focusedTextColor = Color(0xFFF4F4F6),
                        unfocusedTextColor = Color(0xFFF4F4F6)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("glossary_search_input")
                )

                Spacer(modifier = Modifier.height(18.dp))

                filteredTerms.forEach { term ->
                    GlossaryTermCard(term = term)
                    Spacer(modifier = Modifier.height(14.dp))
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Educational information, not medical advice.",
                    fontFamily = PlusJakartaSans,
                    fontSize = 11.sp,
                    color = Color(0x66FFFFFF),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                )
            }
        }
    }
}

@Composable
fun GlossaryTermCard(term: GlossaryTerm, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (term.isCommunityOnly) Color(0x1F2A141A) else Color(0x14FFFFFF),
        border = BorderStroke(
            1.dp,
            if (term.isCommunityOnly) Color(0x55F87171) else Color.White.copy(alpha = 0.10f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag("glossary_term_${term.id}")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = term.term,
                    fontFamily = Fraunces,
                    fontWeight = FontWeight.Normal,
                    fontSize = 18.sp,
                    color = Color(0xFFF4F4F6),
                    modifier = Modifier.weight(1f)
                )

                if (term.isCommunityOnly) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0x33F87171),
                        border = BorderStroke(1.dp, Color(0x66F87171))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = LumoraColors.ErrorCoral,
                                modifier = Modifier.size(11.dp)
                            )
                            Text(
                                text = "No Scientific Validity",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 10.sp,
                                color = LumoraColors.ErrorCoral
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = term.plainExplanation,
                fontFamily = PlusJakartaSans,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                color = Color(0xFFF4F4F6)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // What it measures
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0x10FFFFFF),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "WHAT IT MEASURES",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 10.sp,
                        letterSpacing = 1.sp,
                        color = LumoraColors.ChampagneGold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = term.whatItMeasures,
                        fontFamily = PlusJakartaSans,
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        color = LumoraColors.DarkOnSurfaceMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Limits & context
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (term.isCommunityOnly) Color(0x22F87171) else Color(0x0CFFFFFF),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = if (term.isCommunityOnly) "APP POLICY & LIMITS" else "BIOLOGICAL LIMITS & ACCURACY",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 10.sp,
                        letterSpacing = 1.sp,
                        color = if (term.isCommunityOnly) LumoraColors.ErrorCoral else Color(0xBBFFFFFF)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = term.limits,
                        fontFamily = PlusJakartaSans,
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        color = if (term.isCommunityOnly) Color(0xFFFEE2E2) else Color(0x99FFFFFF)
                    )
                }
            }
        }
    }
}
