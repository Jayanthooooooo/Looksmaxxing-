package com.example.ui.screens.results

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.face.shape.ShapeStyleGuide
import com.example.ui.components.LumoraTopBar
import com.example.ui.theme.Fraunces
import com.example.ui.theme.GoldEyebrow
import com.example.ui.theme.LumoraColors
import com.example.ui.theme.SerifHeadline

@Composable
fun FaceShapeStyleScreen(
    viewModel: FaceShapeStyleViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    val isTeenMode by viewModel.isTeenMode.collectAsState()
    val favorites by viewModel.favorites.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var showBarberDialog by remember { mutableStateOf(false) }

    if (showBarberDialog && state is FaceShapeStyleState.Success) {
        val guide = (state as FaceShapeStyleState.Success).activeShapeGuide
        if (guide != null) {
            AlertDialog(
                onDismissRequest = { showBarberDialog = false },
                containerColor = LumoraColors.DarkSurface,
                title = {
                    SerifHeadline(
                        text = "Show to My Barber",
                        italicWord = "Barber",
                        fontSize = 20.sp
                    )
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        GoldEyebrow(text = "FACE SHAPE: ${guide.name.uppercase()}")
                        Text(
                            text = guide.barber_summary,
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 14.sp
                        )
                        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                        BarberRow("Top", guide.haircuts.top)
                        BarberRow("Sides", guide.haircuts.sides)
                        BarberRow("Fringe", guide.haircuts.fringe)
                        BarberRow("Parting", guide.haircuts.parting)
                        if (!isTeenMode && guide.beard_options.isNotEmpty()) {
                            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                            GoldEyebrow(text = "RECOMMENDED BEARD STACKING")
                            Text(text = guide.beard_options.firstOrNull()?.style ?: "", color = Color.White, fontSize = 13.sp)
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showBarberDialog = false }) {
                        Text("Close", color = LumoraColors.ChampagneGold)
                    }
                }
            )
        }
    }

    Scaffold(
        topBar = {
            LumoraTopBar(
                title = "Face Shape & Style",
                onBackClick = onBackClick,
                actions = {
                    IconButton(
                        onClick = { showBarberDialog = true },
                        modifier = Modifier.testTag("barber_card_button")
                    ) {
                        Icon(Icons.Default.ContentCut, contentDescription = "Show to Barber", tint = LumoraColors.ChampagneGold)
                    }
                }
            )
        },
        containerColor = LumoraColors.DarkBackground,
        modifier = modifier
    ) { paddingVals ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingVals)
        ) {
            when (val s = state) {
                is FaceShapeStyleState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = LumoraColors.ChampagneGold
                    )
                }
                is FaceShapeStyleState.Error -> {
                    Text(
                        text = s.message,
                        color = Color.Red,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp)
                    )
                }
                is FaceShapeStyleState.Success -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        s.activeShapeGuide?.let { activeGuide ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = LumoraColors.DarkSurface),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, LumoraColors.ChampagneGold.copy(alpha = 0.3f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("active_face_shape_card")
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        GoldEyebrow(text = "DETECTED FACE SHAPE")
                                        val topMatch = s.analysis.topMatches.firstOrNull()
                                        if (topMatch != null) {
                                            Surface(
                                                color = LumoraColors.ChampagneGold.copy(alpha = 0.15f),
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Text(
                                                    text = "${topMatch.confidence} CONFIDENCE",
                                                    color = LumoraColors.ChampagneGold,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                        }
                                    }

                                    SerifHeadline(
                                        text = activeGuide.name,
                                        fontSize = 28.sp
                                    )

                                    Text(
                                        text = activeGuide.description,
                                        color = Color.White.copy(alpha = 0.8f),
                                        fontSize = 14.sp
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        s.allShapes.forEach { shape ->
                                            val isSelected = shape.id.equals(activeGuide.id, ignoreCase = true)
                                            FilterChip(
                                                selected = isSelected,
                                                onClick = { viewModel.selectShape(shape.id) },
                                                label = { Text(shape.name) },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = LumoraColors.ChampagneGold,
                                                    selectedLabelColor = Color.Black,
                                                    containerColor = LumoraColors.DarkSurfaceVariant,
                                                    labelColor = Color.White
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        s.activeShapeGuide?.let { guide ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = LumoraColors.DarkSurface),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showBarberDialog = true }
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.ContentCut,
                                        contentDescription = null,
                                        tint = LumoraColors.ChampagneGold,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        GoldEyebrow(text = "BARBER-READY SUMMARY")
                                        Text(text = guide.barber_summary, color = Color.White.copy(alpha = 0.9f), fontSize = 13.sp)
                                    }
                                    Icon(Icons.Default.ArrowForwardIos, contentDescription = null, tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(16.dp))
                                }
                            }
                        }

                        val tabs = if (isTeenMode) listOf("Hair", "Brows", "Glasses") else listOf("Hair", "Beard", "Brows", "Glasses")
                        ScrollableTabRow(
                            selectedTabIndex = selectedTab,
                            containerColor = Color.Transparent,
                            contentColor = LumoraColors.ChampagneGold,
                            edgePadding = 0.dp
                        ) {
                            tabs.forEachIndexed { index, title ->
                                Tab(
                                    selected = selectedTab == index,
                                    onClick = { selectedTab = index },
                                    text = { Text(text = title, fontWeight = FontWeight.Bold) },
                                    selectedContentColor = LumoraColors.ChampagneGold,
                                    unselectedContentColor = Color.White.copy(alpha = 0.6f)
                                )
                            }
                        }

                        s.activeShapeGuide?.let { guide ->
                            val actualTab = if (isTeenMode && selectedTab >= 1) selectedTab + 1 else selectedTab
                            when (actualTab) {
                                0 -> HaircutsSection(guide, favorites, viewModel::toggleFavorite)
                                1 -> if (!isTeenMode) BeardSection(guide, favorites, viewModel::toggleFavorite)
                                2 -> BrowsSection(guide)
                                3 -> GlassesSection(guide, favorites, viewModel::toggleFavorite)
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun HaircutsSection(guide: ShapeStyleGuide, favorites: Set<String>, onToggleFavorite: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        GoldEyebrow(text = "HAIRCUT RECOMMENDATIONS")
        Text(text = "These styles tend to balance your proportions harmoniously. Personal taste always overrides general rules.", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)

        HaircutCard("Top Volume", guide.haircuts.top, "hair_top", favorites, onToggleFavorite)
        HaircutCard("Sides & Fade", guide.haircuts.sides, "hair_sides", favorites, onToggleFavorite)
        HaircutCard("Fringe Styling", guide.haircuts.fringe, "hair_fringe", favorites, onToggleFavorite)
        HaircutCard("Parting", guide.haircuts.parting, "hair_parting", favorites, onToggleFavorite)

        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "What to Avoid", color = Color.Red.copy(alpha = 0.9f), fontSize = 13.sp, fontWeight = FontWeight.Bold)
        guide.what_to_avoid.forEach { avoid ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
                Icon(Icons.Default.Close, contentDescription = null, tint = Color.Red, modifier = Modifier.size(16.dp))
                Text(text = avoid, color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun HaircutCard(title: String, desc: String, id: String, favorites: Set<String>, onToggleFavorite: (String) -> Unit) {
    val isFav = favorites.contains(id)
    Card(
        colors = CardDefaults.cardColors(containerColor = LumoraColors.DarkSurface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                GoldEyebrow(text = title)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = desc, color = Color.White, fontSize = 14.sp)
            }
            IconButton(onClick = { onToggleFavorite(id) }) {
                Icon(
                    imageVector = if (isFav) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                    contentDescription = "Favorite",
                    tint = if (isFav) LumoraColors.ChampagneGold else Color.White.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
fun BeardSection(guide: ShapeStyleGuide, favorites: Set<String>, onToggleFavorite: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        GoldEyebrow(text = "BEARD & FACIAL HAIR STYLING")
        Text(text = "Facial hair can be used strategically to change how jaw and lower face proportions read.", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)

        guide.beard_options.forEachIndexed { index, option ->
            val id = "beard_$index"
            val isFav = favorites.contains(id)
            Card(
                colors = CardDefaults.cardColors(containerColor = LumoraColors.DarkSurface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = option.style, color = LumoraColors.ChampagneGold, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = option.effect, color = Color.White.copy(alpha = 0.9f), fontSize = 13.sp)
                    }
                    IconButton(onClick = { onToggleFavorite(id) }) {
                        Icon(
                            imageVector = if (isFav) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Favorite",
                            tint = if (isFav) LumoraColors.ChampagneGold else Color.White.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BrowsSection(guide: ShapeStyleGuide) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        GoldEyebrow(text = "EYEBROW GROOMING TIPS")
        Card(
            colors = CardDefaults.cardColors(containerColor = LumoraColors.DarkSurface),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                GoldEyebrow(text = "RECOMMENDED SHAPE & ARCH")
                Text(text = guide.eyebrow_tips, color = Color.White, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun GlassesSection(guide: ShapeStyleGuide, favorites: Set<String>, onToggleFavorite: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        GoldEyebrow(text = "RECOMMENDED GLASSES FRAMES")
        Text(text = "Frames that contrast or balance your facial geometry.", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)

        guide.glasses_frames.forEachIndexed { index, frame ->
            val id = "glasses_$index"
            val isFav = favorites.contains(id)
            Card(
                colors = CardDefaults.cardColors(containerColor = LumoraColors.DarkSurface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = frame, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                    IconButton(onClick = { onToggleFavorite(id) }) {
                        Icon(
                            imageVector = if (isFav) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Favorite",
                            tint = if (isFav) LumoraColors.ChampagneGold else Color.White.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BarberRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        GoldEyebrow(text = label)
        Text(text = value, color = Color.White, fontSize = 13.sp)
    }
}
