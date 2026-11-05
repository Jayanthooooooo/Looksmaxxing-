package com.example.ui.journey

import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.SkillNodeEntity
import com.example.ui.components.AppBottomSheet
import com.example.ui.components.LumoraTopBar
import com.example.ui.theme.GoldEyebrow
import com.example.ui.theme.LumoraColors
import com.example.ui.theme.PlusJakartaSans
import com.example.ui.util.Haptics
import com.example.ui.util.SparkleBurstAnimation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JourneyScreen(viewModel: JourneyViewModel, onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val nodes by viewModel.skillNodes.collectAsState()
    val nodeProgress by viewModel.nodeProgress.collectAsState()
    val unlockedCount = nodes.count { it.status == "DONE" }

    val snackbarHostState = remember { SnackbarHostState() }
    var selectedPhotoNodeId by remember { mutableStateOf<String?>(null) }
    var sparkleNodeId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.snackbarEvent.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        val nodeId = selectedPhotoNodeId
        if (bitmap != null && nodeId != null) {
            viewModel.saveProofPhoto(nodeId, bitmap) {
                Haptics.light(context)
                sparkleNodeId = nodeId
            }
            selectedPhotoNodeId = null
        }
    }

    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        val nodeId = selectedPhotoNodeId
        if (uri != null && nodeId != null) {
            try {
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
                viewModel.saveProofPhoto(nodeId, bitmap) {
                    Haptics.light(context)
                    sparkleNodeId = nodeId
                }
            } catch (_: Exception) {}
            selectedPhotoNodeId = null
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            LumoraTopBar(
                title = "Skill Tree",
                onBackClick = onNavigateBack
            )
        },
        containerColor = LumoraColors.DarkBackground,
        modifier = Modifier.testTag("journey_screen")
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                GoldEyebrow("SKILL TREE & MILESTONES")
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Consistency Milestones",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = Color.White
                    )
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0x28D9C59B)
                    ) {
                        Text(
                            text = "$unlockedCount / ${nodes.size} unlocked",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            color = LumoraColors.ChampagneGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { if (nodes.isEmpty()) 0f else unlockedCount.toFloat() / nodes.size },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = LumoraColors.ChampagneGold,
                    trackColor = Color.White.copy(alpha = 0.1f)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            items(nodes, key = { it.id }) { node ->
                SkillNodeCard(
                    node = node,
                    progressText = nodeProgress[node.id],
                    photoVault = viewModel.photoVault,
                    showSparkle = sparkleNodeId == node.id,
                    onSparkleComplete = { sparkleNodeId = null },
                    onPhotoClick = { selectedPhotoNodeId = node.id },
                    onRemoveProof = { viewModel.removeProofPhoto(node.id) }
                )
            }
        }

        // Photo proof upload bottom sheet
        if (selectedPhotoNodeId != null) {
            AppBottomSheet(onDismiss = { selectedPhotoNodeId = null }) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Add proof photo",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Upload a photo proof to mark this milestone as complete.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            onClick = { cameraLauncher.launch(null) },
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0x28D9C59B),
                            border = BorderStroke(1.dp, LumoraColors.ChampagneGold),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("take_photo_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    tint = LumoraColors.ChampagneGold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Camera",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        Surface(
                            onClick = {
                                galleryLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0x18FFFFFF),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("choose_gallery_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhotoLibrary,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Gallery",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "Stored only on this device.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
fun SkillNodeCard(
    node: SkillNodeEntity,
    progressText: String? = null,
    photoVault: com.example.data.local.PhotoVault,
    showSparkle: Boolean,
    onSparkleComplete: () -> Unit,
    onPhotoClick: () -> Unit,
    onRemoveProof: () -> Unit
) {
    val isDone = node.status == "DONE"
    val isPhotoNode = node.requirementType == "PHOTO"

    val proofBitmap = remember(node.proofPhotoPath) {
        node.proofPhotoPath?.let { photoVault.loadDecryptedPhoto(it) }
    }

    Surface(
        onClick = {
            if (isPhotoNode && !isDone) {
                onPhotoClick()
            }
        },
        shape = RoundedCornerShape(16.dp),
        color = Color(0x14FFFFFF),
        border = BorderStroke(
            1.dp,
            if (isDone) LumoraColors.ChampagneGold else Color.White.copy(alpha = 0.08f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("skill_node_${node.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (isDone) Color(0x28D9C59B) else Color.White.copy(alpha = 0.05f))
                    .border(
                        1.5.dp,
                        if (isDone) LumoraColors.ChampagneGold else Color.White.copy(alpha = 0.2f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isDone) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Done",
                        tint = LumoraColors.ChampagneGold,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        text = when (node.requirementType) {
                            "PHOTO" -> "📷"
                            "SCAN" -> "🔍"
                            else -> "⚡"
                        },
                        fontSize = 18.sp
                    )
                }

                if (showSparkle) {
                    SparkleBurstAnimation(
                        trigger = true,
                        onComplete = onSparkleComplete
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = node.name,
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White.copy(alpha = 0.08f)
                    ) {
                        Text(
                            text = when (node.requirementType) {
                                "PHOTO" -> "PHOTO PROOF"
                                "SCAN" -> "SCAN (${node.requirementValue})"
                                else -> "HABIT (${node.requirementValue}d)"
                            },
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = LumoraColors.ChampagneGold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = node.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.65f),
                    modifier = Modifier.padding(top = 2.dp)
                )

                if (!isPhotoNode && !progressText.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = progressText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isDone) LumoraColors.ChampagneGold else Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.testTag("node_progress_${node.id}")
                    )
                }

                if (isPhotoNode && isDone && proofBitmap != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Image(
                            bitmap = proofBitmap.asImageBitmap(),
                            contentDescription = "Proof photo",
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .border(1.dp, LumoraColors.ChampagneGold, RoundedCornerShape(10.dp)),
                            contentScale = ContentScale.Crop
                        )

                        Surface(
                            onClick = onRemoveProof,
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0x14FFFFFF),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                            modifier = Modifier.testTag("remove_proof_${node.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Remove",
                                    tint = Color(0xFFF87171),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Remove proof",
                                    fontSize = 11.sp,
                                    color = Color(0xFFF87171),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
