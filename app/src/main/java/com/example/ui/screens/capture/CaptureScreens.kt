package com.example.ui.screens.capture

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ui.components.GateChip
import com.example.ui.components.LumoraTopBar
import com.example.ui.components.PrimaryButton
import com.example.ui.components.PrivacyBadge
import com.example.ui.components.QuietTextButton
import com.example.ui.components.SecondaryButton
import com.example.ui.theme.LumoraColors

@Composable
fun ScanHubScreen(
    onFrontScanClick: () -> Unit,
    onSideScanClick: () -> Unit,
    onGalleryPick: (android.net.Uri?) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        onGalleryPick(uri)
    }

    Scaffold(
        topBar = { LumoraTopBar(title = "New Scan", onBackClick = onBackClick) },
        modifier = modifier.testTag("scan_hub_screen")
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Capture conditions checklist",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Consistent photos give the most reliable progress comparisons over time.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(20.dp))

                val checklist = listOf(
                    "Face soft, natural daylight (e.g. facing a window)",
                    "Relax your expression (neutral, mouth closed)",
                    "Keep hair off forehead and eyebrows",
                    "Remove glasses and hats",
                    "Hold phone upright at eye level, roughly arm's length"
                )

                checklist.forEach { item ->
                    Row(
                        modifier = Modifier.padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = item,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                PrivacyBadge(text = "Processed strictly on this phone · Encrypted storage")
            }

            Column(modifier = Modifier.fillMaxWidth().padding(top = 24.dp)) {
                PrimaryButton(
                    text = "Front scan",
                    onClick = onFrontScanClick,
                    testTag = "scan_hub_front_button"
                )
                Spacer(modifier = Modifier.height(12.dp))
                SecondaryButton(
                    text = "Side scan (Beta)",
                    onClick = onSideScanClick,
                    testTag = "scan_hub_side_button"
                )
                Spacer(modifier = Modifier.height(12.dp))
                QuietTextButton(
                    text = "Choose from gallery",
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "scan_hub_gallery_button"
                )
            }
        }
    }
}

@Composable
fun CameraRationaleScreen(
    onAllowCamera: () -> Unit,
    onUseGallery: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
    isPermanentlyDenied: Boolean = false
) {
    Scaffold(
        topBar = { LumoraTopBar(title = "Camera Access") },
        modifier = modifier.testTag("camera_rationale_screen")
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Camera permission needed",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Lumora uses your front camera to provide live alignment guides (head angle, distance, lighting) and take your scan. Photos stay on this phone unless you enable cloud backup.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                if (isPermanentlyDenied) {
                    PrimaryButton(
                        text = "Open device settings",
                        onClick = onOpenSettings,
                        testTag = "open_settings_button"
                    )
                } else {
                    PrimaryButton(
                        text = "Allow camera",
                        onClick = onAllowCamera,
                        testTag = "allow_camera_button"
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                QuietTextButton(
                    text = "Use gallery photo instead",
                    onClick = onUseGallery,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "use_gallery_fallback_button"
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LiveCaptureScreen(
    kind: String, // "front" or "side"
    gateHeadAngle: Boolean,
    gateLight: Boolean,
    gateDistance: Boolean,
    gateExpression: Boolean,
    gateSteady: Boolean,
    consecutiveFramesHeld: Int,
    onManualShutterClick: () -> Unit,
    onToggleMirror: () -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier,
    isMirrored: Boolean = true,
    cameraPreviewContent: (@Composable () -> Unit)? = null
) {
    val allGatesReady = gateHeadAngle && gateLight && gateDistance && gateExpression && gateSteady

    Box(modifier = modifier.fillMaxSize().background(Color.Black).testTag("live_capture_screen")) {
        // Camera Preview Content
        if (cameraPreviewContent != null) {
            cameraPreviewContent()
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Camera Preview ($kind scan)",
                    color = Color.White.copy(alpha = 0.5f),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // Top controls: Close & Mirror toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp, start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            QuietTextButton(
                text = "Cancel",
                onClick = onCloseClick,
                testTag = "live_capture_cancel"
            )

            IconButton(
                onClick = onToggleMirror,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.5f))
                    .testTag("mirror_toggle_button")
            ) {
                Icon(
                    imageVector = Icons.Default.FlipCameraAndroid,
                    contentDescription = "Toggle mirror preview",
                    tint = Color.White
                )
            }
        }

        // Central Guide (Oval for front, profile for side)
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(width = 240.dp, height = 320.dp)
                .border(
                    width = 2.dp,
                    color = if (allGatesReady) LumoraColors.LightSuccess else Color.White.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(120.dp)
                )
        )

        // Bottom GateChips & Shutter
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.6f))
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Consecutive hold indicator
            if (consecutiveFramesHeld > 0) {
                LinearProgressIndicator(
                    progress = { (consecutiveFramesHeld / 8f).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth(0.6f).height(4.dp).clip(RoundedCornerShape(2.dp)),
                    color = LumoraColors.LightSuccess,
                    trackColor = Color.White.copy(alpha = 0.2f)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // 5 Quality Gate Chips
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                GateChip(label = if (kind == "front") "Head Angle" else "Profile Angle", isReady = gateHeadAngle)
                GateChip(label = "Lighting", isReady = gateLight)
                GateChip(label = "Distance", isReady = gateDistance)
                GateChip(label = "Expression", isReady = gateExpression)
                GateChip(label = "Steady", isReady = gateSteady)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Shutter Button
            IconButton(
                onClick = onManualShutterClick,
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(if (allGatesReady) LumoraColors.LightSuccess else Color.White)
                    .testTag("shutter_button")
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .border(3.dp, Color.Black, CircleShape)
                )
            }

            if (!allGatesReady) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Manual capture available (alignment recommended)",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun PhotoReviewScreen(
    bitmap: Bitmap?,
    onAccept: () -> Unit,
    onRetake: () -> Unit,
    modifier: Modifier = Modifier,
    qualityWarning: String? = null
) {
    Scaffold(
        topBar = { LumoraTopBar(title = "Review Photo") },
        modifier = modifier.testTag("photo_review_screen")
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Captured photo preview",
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text("No image preview", color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (qualityWarning != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = LumoraColors.LightCaution.copy(alpha = 0.15f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = LumoraColors.LightCaution)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = qualityWarning, style = MaterialTheme.typography.bodySmall, color = LumoraColors.LightCaution)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SecondaryButton(
                    text = "Retake",
                    onClick = onRetake,
                    modifier = Modifier.weight(1f),
                    testTag = "review_retake_button"
                )
                PrimaryButton(
                    text = "Accept & Analyse",
                    onClick = onAccept,
                    modifier = Modifier.weight(1f),
                    testTag = "review_accept_button"
                )
            }
        }
    }
}

@Composable
fun ProcessingScreen(
    currentStageText: String,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    errorMessage: String? = null,
    onRetry: (() -> Unit)? = null,
    onKeepOnPhoneOnly: (() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp)
            .testTag("processing_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (errorMessage != null) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(56.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Analysis Issue",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                if (onRetry != null) {
                    PrimaryButton(text = "Retry", onClick = onRetry)
                    Spacer(modifier = Modifier.height(12.dp))
                }
                if (onKeepOnPhoneOnly != null) {
                    SecondaryButton(text = "Keep photo on this phone", onClick = onKeepOnPhoneOnly)
                    Spacer(modifier = Modifier.height(12.dp))
                }
                QuietTextButton(text = "Cancel", onClick = onCancel)
            } else {
                CircularProgressIndicator(
                    modifier = Modifier.size(64.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 3.dp
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = currentStageText,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Measuring landmarks and evaluating lighting...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(32.dp))
                QuietTextButton(text = "Cancel", onClick = onCancel, testTag = "processing_cancel_button")
            }
        }
    }
}
