package com.example.ui.screens.results

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.PostureLogEntity
import com.example.data.repo.PostureExercise
import com.example.data.repo.PostureExerciseData
import com.example.face.posture.PostureAnalysisResult
import com.example.face.posture.PostureBand
import com.example.ui.components.LumoraTopBar
import com.example.ui.screens.capture.decodeGalleryUriToBitmap
import com.example.ui.theme.Fraunces
import com.example.ui.theme.GoldEyebrow
import com.example.ui.theme.LumoraColors
import com.example.ui.theme.SerifHeadline
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PostureAnalysisScreen(
    viewModel: PostureAnalysisViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val planAdded by viewModel.planAdded.collectAsState()
    val context = LocalContext.current

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val bmp = decodeGalleryUriToBitmap(context, it)
            bmp?.let { b -> viewModel.processSideBitmap(b) }
        }
    }

    Scaffold(
        topBar = {
            LumoraTopBar(
                title = "Posture Analysis",
                onBackClick = onBackClick,
                actions = {
                    IconButton(
                        onClick = { galleryLauncher.launch("image/*") },
                        modifier = Modifier.testTag("posture_upload_button")
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = "Upload side photo", tint = LumoraColors.ChampagneGold)
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
            when (val state = uiState) {
                is PostureUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = LumoraColors.ChampagneGold
                    )
                }
                is PostureUiState.Error -> {
                    Text(
                        text = state.message,
                        color = Color.Red,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp)
                    )
                }
                is PostureUiState.Success -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Top side photo with ear-shoulder-hip overlay
                        PosturePhotoOverlayCard(
                            bitmap = state.currentBitmap,
                            analysis = state.analysis,
                            onPickPhoto = { galleryLauncher.launch("image/*") }
                        )

                        // Result Band Chip & Confidence Note
                        PostureResultHeaderCard(
                            analysis = state.analysis
                        )

                        // Medical & Safety Notice
                        MedicalDisclaimerCard()

                        // Trend Chart across saved posture checks
                        if (state.history.isNotEmpty()) {
                            PostureTrendChartCard(history = state.history)
                        }

                        // 4-Week Gentle Plan Card & Add to Plan
                        PosturePlanCard(
                            exerciseData = state.exerciseData,
                            planAdded = planAdded,
                            onAddPlan = { viewModel.add4WeekPlanToHabits() }
                        )

                        // Exercises List
                        PostureExercisesListSection(
                            exercises = state.exerciseData.exercises
                        )

                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun PosturePhotoOverlayCard(
    bitmap: Bitmap?,
    analysis: PostureAnalysisResult,
    onPickPhoto: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = LumoraColors.DarkSurface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, LumoraColors.ChampagneGold.copy(alpha = 0.3f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("posture_photo_overlay_card")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                GoldEyebrow(text = "SIDE PROFILE ALIGNMENT")
                TextButton(onClick = onPickPhoto) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, tint = LumoraColors.ChampagneGold, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Analyze Side Photo", color = LumoraColors.ChampagneGold, fontSize = 12.sp)
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1A1A22)),
                contentAlignment = Alignment.Center
            ) {
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Side Profile Photo",
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.AccessibilityNew,
                            contentDescription = null,
                            tint = LumoraColors.ChampagneGold.copy(alpha = 0.6f),
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "Side-view posture line demonstration",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 12.sp
                        )
                    }
                }

                // Overlay Canvas drawing Ear -> Shoulder -> Hip line
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    val earP = Offset(w * 0.55f, h * 0.25f)
                    val shoulderP = Offset(w * 0.45f, h * 0.42f)
                    val hipP = Offset(w * 0.45f, h * 0.75f)

                    // Draw Ear-Shoulder line (Gold)
                    drawLine(
                        color = LumoraColors.ChampagneGold,
                        start = earP,
                        end = shoulderP,
                        strokeWidth = 6f
                    )

                    // Draw Shoulder-Hip vertical reference line (Cyan)
                    drawLine(
                        color = Color(0xFF38BDF8),
                        start = shoulderP,
                        end = hipP,
                        strokeWidth = 6f
                    )

                    // Horizontal reference through shoulder (Dashed)
                    drawLine(
                        color = Color.White.copy(alpha = 0.5f),
                        start = Offset(shoulderP.x - 80f, shoulderP.y),
                        end = Offset(shoulderP.x + 80f, shoulderP.y),
                        strokeWidth = 3f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                    )

                    // Draw landmark nodes
                    drawCircle(color = LumoraColors.ChampagneGold, radius = 10f, center = earP)
                    drawCircle(color = Color(0xFF38BDF8), radius = 10f, center = shoulderP)
                    drawCircle(color = Color(0xFF38BDF8), radius = 10f, center = hipP)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                LegendItem(color = LumoraColors.ChampagneGold, label = "Ear to Shoulder")
                LegendItem(color = Color(0xFF38BDF8), label = "Shoulder to Hip")
                LegendItem(color = Color.White.copy(alpha = 0.5f), label = "Horizontal Ref")
            }
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color))
        Text(text = label, color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
    }
}

@Composable
fun PostureResultHeaderCard(analysis: PostureAnalysisResult) {
    Card(
        colors = CardDefaults.cardColors(containerColor = LumoraColors.DarkSurface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, LumoraColors.ChampagneGold.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                GoldEyebrow(text = "POSTURE ASSESSMENT")
                Surface(
                    color = LumoraColors.ChampagneGold.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "${analysis.confidence} CONFIDENCE",
                        color = LumoraColors.ChampagneGold,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            if (analysis.errorMessage != null) {
                Text(
                    text = analysis.errorMessage,
                    fontFamily = Fraunces,
                    color = Color.Red,
                    fontSize = 20.sp
                )
                analysis.retakeTip?.let { tip ->
                    Text(text = tip, color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    val icon = when (analysis.band) {
                        PostureBand.NEUTRAL -> Icons.Default.CheckCircle
                        PostureBand.SLIGHTLY_FORWARD -> Icons.Default.Info
                        PostureBand.FORWARD -> Icons.Default.Warning
                        else -> Icons.Default.Info
                    }
                    val iconTint = when (analysis.band) {
                        PostureBand.NEUTRAL -> Color(0xFF4ADE80)
                        PostureBand.SLIGHTLY_FORWARD -> LumoraColors.ChampagneGold
                        PostureBand.FORWARD -> Color(0xFFF87171)
                        else -> LumoraColors.ChampagneGold
                    }

                    Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(28.dp))
                    Text(
                        text = analysis.band?.displayName ?: "Analyzed",
                        fontFamily = Fraunces,
                        color = Color.White,
                        fontSize = 24.sp
                    )
                }

                Text(
                    text = analysis.reasoning,
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 14.sp
                )

                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    MetricMiniStat("Craniovertebral Angle", "${analysis.craniovertebralAngle.toInt()}°", "Ref: 50°-65°")
                    MetricMiniStat("Forward Head Offset", "${(analysis.forwardOffsetRatio * 100).toInt()}%", "Ref: < 12%")
                }
            }
        }
    }
}

@Composable
fun MetricMiniStat(label: String, value: String, sub: String) {
    Column {
        Text(text = label, color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
        Text(text = value, color = LumoraColors.ChampagneGold, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(text = sub, color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp)
    }
}

@Composable
fun MedicalDisclaimerCard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0x22F87171)),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0x44F87171)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(Icons.Default.MedicalServices, contentDescription = null, tint = Color(0xFFF87171), modifier = Modifier.size(20.dp))
            Text(
                text = "For persistent pain, numbness, tingling or marked asymmetry, see a doctor or physiotherapist. Lumora never diagnoses scoliosis or any clinical spinal condition.",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
fun PostureTrendChartCard(history: List<PostureLogEntity>) {
    Card(
        colors = CardDefaults.cardColors(containerColor = LumoraColors.DarkSurface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GoldEyebrow(text = "CRANIOVERTEBRAL ANGLE TREND")
            Text(text = "Angle measurements across saved checks. Photos are never stored by default.", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    if (history.isEmpty()) return@Canvas

                    val reversed = history.reversed()
                    val points = reversed.mapIndexed { idx, item ->
                        val x = if (reversed.size > 1) (idx.toFloat() / (reversed.size - 1)) * w else w / 2f
                        val y = h - ((item.craniovertebralAngle.coerceIn(30f, 75f) - 30f) / 45f) * h
                        Offset(x, y)
                    }

                    for (i in 0 until points.size - 1) {
                        drawLine(
                            color = LumoraColors.ChampagneGold,
                            start = points[i],
                            end = points[i + 1],
                            strokeWidth = 4f
                        )
                    }

                    points.forEach { pt ->
                        drawCircle(color = LumoraColors.ChampagneGold, radius = 6f, center = pt)
                    }
                }
            }
        }
    }
}

@Composable
fun PosturePlanCard(
    exerciseData: PostureExerciseData,
    planAdded: Boolean,
    onAddPlan: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = LumoraColors.DarkSurface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, LumoraColors.ChampagneGold.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GoldEyebrow(text = "4-WEEK GENTLE POSTURE PLAN")
            SerifHeadline(text = "Awareness & Ergonomic Balance", fontSize = 20.sp)
            Text(
                text = "Posture routines may help improve upper back awareness and neck alignment in photos. They do not alter bone structure.",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 13.sp
            )

            exerciseData.fourWeekPlan.forEach { week ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Surface(
                        color = LumoraColors.ChampagneGold.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "WK ${week.week}",
                            color = LumoraColors.ChampagneGold,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Column {
                        Text(text = week.title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text(text = week.focus, color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick = onAddPlan,
                enabled = !planAdded,
                colors = ButtonDefaults.buttonColors(containerColor = LumoraColors.ChampagneGold),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("add_posture_plan_button")
            ) {
                Icon(
                    imageVector = if (planAdded) Icons.Default.Check else Icons.Default.AddTask,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (planAdded) "Plan Added to Daily Habits" else "Add 4-Week Plan to My Habits",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun PostureExercisesListSection(exercises: List<PostureExercise>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        GoldEyebrow(text = "8 BEGINNER POSTURE EXERCISES")

        exercises.forEach { ex ->
            var expanded by remember { mutableStateOf(false) }
            Card(
                colors = CardDefaults.cardColors(containerColor = LumoraColors.DarkSurface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = ex.name, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text(text = "${ex.category} · ${ex.sets} · ${ex.reps}", color = LumoraColors.ChampagneGold, fontSize = 12.sp)
                        }
                        Icon(
                            imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Toggle exercise details",
                            tint = Color.White.copy(alpha = 0.5f)
                        )
                    }

                    if (expanded) {
                        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                        Text(text = "STEPS", color = LumoraColors.ChampagneGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        ex.steps.forEachIndexed { i, step ->
                            Text(text = "${i + 1}. $step", color = Color.White.copy(alpha = 0.9f), fontSize = 13.sp)
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "MISTAKES TO AVOID", color = Color.Red.copy(alpha = 0.8f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        ex.mistakesToAvoid.forEach { mistake ->
                            Text(text = "• $mistake", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFFBBF24), modifier = Modifier.size(14.dp))
                            Text(text = ex.safetyWarning, color = Color(0xFFFBBF24), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}
