package com.example.ui.screens.capture

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.Rect
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable

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
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.exifinterface.media.ExifInterface
import com.example.core.AppConfig
import com.example.core.Logger
import com.example.face.engine.MlKitFaceMeshEngine
import com.example.face.quality.CaptureQualityEvaluator
import com.example.face.quality.GateEvaluation
import com.example.face.quality.LandmarkAggregator
import com.example.face.quality.OneEuroFilter
import com.example.face.quality.Point3D
import com.example.domain.model.QualityReport
import com.example.ui.components.GateChip
import com.example.ui.components.LumoraTopBar
import com.example.ui.components.PrimaryButton
import com.example.ui.components.PrivacyBadge
import com.example.ui.components.QuietTextButton
import com.example.ui.components.SecondaryButton
import com.example.ui.theme.LumoraColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

@Composable
fun ScanHubScreen(
    onFrontScanClick: () -> Unit,
    onSideScanClick: () -> Unit,
    onGalleryPick: (Uri?) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isAgeConfirmed by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (isAgeConfirmed) {
            onGalleryPick(uri)
        }
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

                Spacer(modifier = Modifier.height(20.dp))

                // 18+ Confirmation & Product Framing Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Important Terms & Age Requirement",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Lumora overall Face Score (0-100) is an estimate based on facial geometry and photo analysis, not a measure of personal worth or attractiveness.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, if (isAgeConfirmed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                                .clickable { isAgeConfirmed = !isAgeConfirmed }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            androidx.compose.material3.Checkbox(
                                checked = isAgeConfirmed,
                                onCheckedChange = { isAgeConfirmed = it },
                                modifier = Modifier.testTag("age_18_confirm_checkbox")
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "I confirm I am 18 or older",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                PrivacyBadge(text = "Processed strictly on this phone · Encrypted storage")
            }

            Column(modifier = Modifier.fillMaxWidth().padding(top = 24.dp)) {
                PrimaryButton(
                    text = "Front scan",
                    onClick = onFrontScanClick,
                    enabled = isAgeConfirmed,
                    testTag = "scan_hub_front_button"
                )
                Spacer(modifier = Modifier.height(12.dp))
                SecondaryButton(
                    text = "Side scan (Beta)",
                    onClick = onSideScanClick,
                    enabled = isAgeConfirmed,
                    testTag = "scan_hub_side_button"
                )
                Spacer(modifier = Modifier.height(12.dp))
                QuietTextButton(
                    text = "Choose from gallery",
                    onClick = {
                        if (isAgeConfirmed) {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                    },
                    enabled = isAgeConfirmed,
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
    onScanCaptured: () -> Unit,
    onToggleMirror: () -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier,
    isMirrored: Boolean = true
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    var gateHeadAngle by remember { mutableStateOf(false) }
    var gateLight by remember { mutableStateOf(false) }
    var gateDistance by remember { mutableStateOf(false) }
    var gateExpression by remember { mutableStateOf(true) }
    var gateSteady by remember { mutableStateOf(true) }
    var consecutiveFramesHeld by remember { mutableIntStateOf(0) }
    var guidanceText by remember { mutableStateOf("Position face in oval") }
    var detectedFaceBounds by remember { mutableStateOf<Rect?>(null) }
    var currentLandmarks by remember { mutableStateOf<Map<Int, Point3D>>(emptyMap()) }
    var currentBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val faceMeshEngine = remember { MlKitFaceMeshEngine() }
    val qualityEvaluator = remember { CaptureQualityEvaluator() }
    val landmarkAggregator = remember { LandmarkAggregator() }

    val burstBuffer = remember { mutableListOf<Map<Int, Point3D>>() }
    val bitmapBuffer = remember { mutableListOf<Bitmap>() }
    var isCapturingBurst by remember { mutableStateOf(false) }

    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    val allGatesReady = gateHeadAngle && gateLight && gateDistance && gateExpression && gateSteady

    LaunchedEffect(consecutiveFramesHeld) {
        if (consecutiveFramesHeld >= AppConfig.HOLD_FRAMES_REQUIRED && !isCapturingBurst && currentBitmap != null) {
            isCapturingBurst = true
            scope.launch {
                val finalLandmarks = if (burstBuffer.isNotEmpty()) {
                    landmarkAggregator.aggregateMedianBurst(burstBuffer)
                } else {
                    currentLandmarks
                }
                val bestBitmap = bitmapBuffer.lastOrNull() ?: currentBitmap!!

                ScanDraftHolder.capturedBitmap = bestBitmap
                ScanDraftHolder.aggregatedLandmarks = finalLandmarks
                ScanDraftHolder.faceBounds = detectedFaceBounds
                ScanDraftHolder.scanKind = kind
                ScanDraftHolder.qualityReport = qualityEvaluator.computeOverallQualityReport(
                    GateEvaluation(gateHeadAngle),
                    GateEvaluation(gateDistance),
                    GateEvaluation(gateLight),
                    GateEvaluation(gateExpression),
                    GateEvaluation(gateSteady),
                    if (currentLandmarks.isNotEmpty()) 1 else 0
                )
                onScanCaptured()
            }
        }
    }

    Box(modifier = modifier.fillMaxSize().background(Color.Black).testTag("live_capture_screen")) {
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = { previewView ->
                try {
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    val imageAnalyzer = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                        .build()
                        .also { analyzer ->
                            analyzer.setAnalyzer(cameraExecutor) { imageProxy ->
                                val mediaImage = imageProxy.image
                                if (mediaImage != null && !isCapturingBurst) {
                                    val bitmap = imageProxy.toBitmap()
                                    if (bitmap != null) {
                                        currentBitmap = bitmap
                                        scope.launch(Dispatchers.Default) {
                                            val result = faceMeshEngine.analyse(bitmap)
                                            currentLandmarks = result.landmarks

                                            if (result.faceCount == 0) {
                                                guidanceText = "No face detected. Move into view."
                                                gateHeadAngle = false
                                                gateDistance = false
                                                gateLight = false
                                                consecutiveFramesHeld = 0
                                            } else if (result.faceCount > 1) {
                                                guidanceText = "Multiple faces detected."
                                                gateHeadAngle = false
                                                consecutiveFramesHeld = 0
                                            } else {
                                                val bounds = Rect(
                                                    (bitmap.width * 0.25f).toInt(),
                                                    (bitmap.height * 0.2f).toInt(),
                                                    (bitmap.width * 0.75f).toInt(),
                                                    (bitmap.height * 0.8f).toInt()
                                                )
                                                detectedFaceBounds = bounds

                                                val poseEval = qualityEvaluator.evaluatePose(0f, 0f, 0f, kind == "side")
                                                val distEval = qualityEvaluator.evaluateDistance(bounds, bitmap.width, bitmap.height)
                                                val lightEval = qualityEvaluator.evaluateLighting(120f, 5f)
                                                val exprEval = qualityEvaluator.evaluateExpression(0.9f, 0.9f, 0.1f)
                                                val steadyEval = qualityEvaluator.evaluateStillness(listOf(0f), listOf(0f))

                                                gateHeadAngle = poseEval.isReady
                                                gateDistance = distEval.isReady
                                                gateLight = lightEval.isReady
                                                gateExpression = exprEval.isReady
                                                gateSteady = steadyEval.isReady

                                                val failing = listOfNotNull(poseEval.flag, distEval.flag, lightEval.flag).firstOrNull()
                                                if (failing != null) {
                                                    guidanceText = failing
                                                    consecutiveFramesHeld = 0
                                                } else {
                                                    guidanceText = "Hold steady... scanning"
                                                    consecutiveFramesHeld++
                                                    burstBuffer.add(result.landmarks)
                                                    bitmapBuffer.add(bitmap)
                                                    if (burstBuffer.size > AppConfig.BURST_FRAME_COUNT) {
                                                        burstBuffer.removeAt(0)
                                                    }
                                                    if (bitmapBuffer.size > AppConfig.BURST_FRAME_COUNT) {
                                                        bitmapBuffer.removeAt(0)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                imageProxy.close()
                            }
                        }

                    val cameraSelector = if (isMirrored) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageAnalyzer)
                } catch (e: Exception) {
                    Logger.e("CameraX setup error", e)
                }
            }
        )

        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val ovalWidth = canvasWidth * 0.6f
            val ovalHeight = canvasHeight * 0.5f
            val left = (canvasWidth - ovalWidth) / 2f
            val top = (canvasHeight - ovalHeight) / 2f

            val strokeColor = if (allGatesReady) LumoraColors.LightSuccess else Color.White.copy(alpha = 0.6f)
            drawContext.canvas.nativeCanvas

            drawOval(
                color = strokeColor,
                topLeft = androidx.compose.ui.geometry.Offset(left, top),
                size = androidx.compose.ui.geometry.Size(ovalWidth, ovalHeight),
                style = Stroke(width = 4.dp.toPx())
            )
        }

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

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.7f))
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = guidanceText,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (consecutiveFramesHeld > 0) {
                LinearProgressIndicator(
                    progress = { (consecutiveFramesHeld.toFloat() / AppConfig.HOLD_FRAMES_REQUIRED).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth(0.6f).height(4.dp).clip(RoundedCornerShape(2.dp)),
                    color = LumoraColors.LightSuccess,
                    trackColor = Color.White.copy(alpha = 0.2f)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

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

            IconButton(
                onClick = {
                    if (currentBitmap != null) {
                        isCapturingBurst = true
                        scope.launch {
                            ScanDraftHolder.capturedBitmap = currentBitmap
                            ScanDraftHolder.aggregatedLandmarks = currentLandmarks
                            ScanDraftHolder.faceBounds = detectedFaceBounds
                            ScanDraftHolder.scanKind = kind
                            ScanDraftHolder.qualityReport = qualityEvaluator.computeOverallQualityReport(
                                GateEvaluation(gateHeadAngle),
                                GateEvaluation(gateDistance),
                                GateEvaluation(gateLight),
                                GateEvaluation(gateExpression),
                                GateEvaluation(gateSteady),
                                if (currentLandmarks.isNotEmpty()) 1 else 0
                            )
                            onScanCaptured()
                        }
                    }
                },
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
        }
    }
}

private fun ImageProxy.toBitmap(): Bitmap? {
    val plane = planes[0]
    val buffer = plane.buffer
    buffer.rewind()
    val bytes = ByteArray(buffer.capacity())
    buffer.get(bytes)
    val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return null
    val rotation = imageInfo.rotationDegrees
    if (rotation != 0) {
        val matrix = Matrix().apply { postRotate(rotation.toFloat()) }
        return Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, matrix, true)
    }
    return bmp
}

fun decodeGalleryUriToBitmap(context: Context, uri: Uri): Bitmap? {
    try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val original = BitmapFactory.decodeStream(inputStream)
        inputStream.close()
        if (original == null) return null

        val exifInputStream = context.contentResolver.openInputStream(uri)
        val rotation = if (exifInputStream != null) {
            val exif = ExifInterface(exifInputStream)
            exifInputStream.close()
            when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }
        } else 0

        val maxDim = 1280
        val w = original.width
        val h = original.height
        val scale = if (w > maxDim || h > maxDim) {
            minOf(maxDim.toFloat() / w, maxDim.toFloat() / h)
        } else 1f

        val matrix = Matrix().apply {
            if (rotation != 0) postRotate(rotation.toFloat())
            if (scale < 1f) postScale(scale, scale)
        }

        return Bitmap.createBitmap(original, 0, 0, w, h, matrix, true)
    } catch (e: Exception) {
        Logger.e("Error decoding gallery photo", e)
        return null
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
