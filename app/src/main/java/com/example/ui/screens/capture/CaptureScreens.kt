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
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.Fraunces
import com.example.ui.theme.GoldEyebrow
import com.example.ui.theme.PlusJakartaSans
import com.example.ui.theme.SerifHeadline
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.collectAsState
import androidx.exifinterface.media.ExifInterface
import com.example.BuildConfig
import com.example.core.AppConfig
import com.example.core.Logger
import com.example.face.engine.MlKitFaceMeshEngine
import com.example.face.quality.CaptureQualityEvaluator
import com.example.face.quality.FaceGateAnalyzer
import com.example.face.quality.GateEvaluation
import com.example.face.quality.GateFrame
import com.example.face.quality.LandmarkAggregator
import com.example.face.quality.OneEuroFilter
import com.example.face.quality.Point3D
import com.example.domain.model.QualityReport
import com.example.ui.components.GateChip
import com.example.ui.components.GlassCard
import com.example.ui.components.GradientButton
import com.example.ui.components.LumoraTopBar
import com.example.ui.components.PrimaryButton
import com.example.ui.components.PrivacyBadge
import com.example.ui.components.QuietTextButton
import com.example.ui.components.SecondaryButton
import com.example.ui.theme.LumoraColors
import com.example.ui.util.Haptics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
                GlassCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Important Terms & Age Requirement",
                        style = MaterialTheme.typography.titleSmall,
                        color = LumoraColors.DarkOnSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Lumora overall Face Score (0-100) is an estimate based on facial geometry and photo analysis, not a measure of personal worth or attractiveness.",
                        style = MaterialTheme.typography.bodySmall,
                        color = LumoraColors.DarkOnSurfaceMuted
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, if (isAgeConfirmed) LumoraColors.SuccessMint else LumoraColors.DarkOutline, RoundedCornerShape(8.dp))
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
                            color = LumoraColors.DarkOnSurface
                        )
                    }
                }
 
                Spacer(modifier = Modifier.height(16.dp))
                PrivacyBadge(text = "Processed strictly on this phone · Encrypted storage")
            }
 
            Column(modifier = Modifier.fillMaxWidth().padding(top = 24.dp)) {
                GradientButton(
                    text = "Front scan",
                    onClick = onFrontScanClick,
                    enabled = isAgeConfirmed,
                    modifier = Modifier.fillMaxWidth()
                )
                // Re-enable/disable based on isAgeConfirmed within GradientButton? Or wrap GradientButton in something that handles enabled?
                // The current GradientButton doesn't handle 'enabled'. I need to fix that.
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

@Composable
fun LiveCaptureScreen(
    kind: String, // "front" or "side"
    onScanCaptured: () -> Unit,
    onToggleMirror: () -> Unit = {},
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier,
    isMirrored: Boolean = true
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    var hasCameraPermission by remember {
        mutableStateOf(
            android.content.pm.PackageManager.PERMISSION_GRANTED ==
                androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA)
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    // Re-check permission on ON_RESUME
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                hasCameraPermission = android.content.pm.PackageManager.PERMISSION_GRANTED ==
                    androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(hasCameraPermission) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(android.Manifest.permission.CAMERA)
        }
    }

    val gateFrameFlow = remember { MutableStateFlow(GateFrame()) }
    val gateFrame by gateFrameFlow.collectAsState()

    var isCapturing by remember { mutableStateOf(false) }
    var captureErrorMessage by remember { mutableStateOf<String?>(null) }
    var cameraError by remember { mutableStateOf<String?>(null) }
    var imageCaptureRef by remember { mutableStateOf<ImageCapture?>(null) }
    var previewViewRef by remember { mutableStateOf<PreviewView?>(null) }

    val faceMeshEngine = remember { MlKitFaceMeshEngine() }
    val qualityEvaluator = remember { CaptureQualityEvaluator() }

    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
            try {
                if (cameraProviderFuture.isDone) {
                    cameraProviderFuture.get().unbindAll()
                }
            } catch (e: Exception) {
                Logger.e("Camera unbind error", e)
            }
        }
    }

    // Update mirror preview scale on previewView
    LaunchedEffect(isMirrored, previewViewRef) {
        previewViewRef?.let { pv ->
            pv.scaleX = if (isMirrored) 1f else -1f
        }
    }

    // Bind Camera lifecycle
    LaunchedEffect(hasCameraPermission, lifecycleOwner, previewViewRef) {
        val previewView = previewViewRef ?: return@LaunchedEffect
        if (!hasCameraPermission) return@LaunchedEffect

        try {
            val cameraProvider = cameraProviderFuture.get()
            // Always front camera for both front and side scans
            val frontSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            if (!cameraProvider.hasCamera(frontSelector)) {
                cameraError = "Front camera is unavailable on this device."
                return@LaunchedEffect
            }

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()
            imageCaptureRef = imageCapture

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setTargetResolution(android.util.Size(640, 480))
                .build()

            val analyzer = FaceGateAnalyzer(kind = kind) { frame ->
                gateFrameFlow.value = frame
            }
            imageAnalysis.setAnalyzer(cameraExecutor, analyzer)

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(lifecycleOwner, frontSelector, preview, imageAnalysis, imageCapture)
            cameraError = null
        } catch (e: Exception) {
            Logger.e("CameraX setup error", e)
            cameraError = "Camera unavailable or hardware connection error."
        }
    }

    fun performCapture(forced: Boolean = false) {
        if (isCapturing) return
        val imageCapture = imageCaptureRef ?: return
        isCapturing = true

        imageCapture.takePicture(
            cameraExecutor,
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(imageProxy: ImageProxy) {
                    try {
                        val rawBitmap = imageProxy.toBitmap()
                        val rotation = imageProxy.imageInfo.rotationDegrees
                        val matrix = Matrix().apply {
                            if (rotation != 0) postRotate(rotation.toFloat())
                        }
                        val rotated = Bitmap.createBitmap(rawBitmap, 0, 0, rawBitmap.width, rawBitmap.height, matrix, true)

                        val maxDim = 1280
                        val w = rotated.width
                        val h = rotated.height
                        val finalBitmap = if (w > maxDim || h > maxDim) {
                            val scale = minOf(maxDim.toFloat() / w, maxDim.toFloat() / h)
                            Bitmap.createScaledBitmap(rotated, (w * scale).toInt().coerceAtLeast(1), (h * scale).toInt().coerceAtLeast(1), true)
                        } else {
                            rotated
                        }

                        Haptics.heavy(context)

                        scope.launch(Dispatchers.Default) {
                            val meshResult = faceMeshEngine.analyse(finalBitmap)
                            val currentGate = gateFrameFlow.value
                            val report = qualityEvaluator.computeOverallQualityReport(
                                GateEvaluation(currentGate.poseReady),
                                GateEvaluation(currentGate.distanceReady),
                                GateEvaluation(currentGate.lightReady),
                                GateEvaluation(currentGate.expressionReady),
                                GateEvaluation(currentGate.steadyReady),
                                meshResult.faceCount
                            )

                            if (meshResult.faceCount != 1 || meshResult.landmarks.size < 468 || report.score < 50) {
                                withContext(Dispatchers.Main) {
                                    isCapturing = false
                                    captureErrorMessage = if (meshResult.faceCount != 1) {
                                        "Please ensure only one face is visible."
                                    } else if (meshResult.landmarks.size < 468) {
                                        "Face mesh alignment incomplete (${meshResult.landmarks.size}/468 points). Hold steady and try again."
                                    } else {
                                        "Quality check score too low (${report.score}/100). Please check lighting and posture."
                                    }
                                }
                                return@launch
                            }

                            val computedBounds = if (meshResult.landmarks.isNotEmpty()) {
                                val minX = meshResult.landmarks.values.minOf { it.x }
                                val maxX = meshResult.landmarks.values.maxOf { it.x }
                                val minY = meshResult.landmarks.values.minOf { it.y }
                                val maxY = meshResult.landmarks.values.maxOf { it.y }
                                val w = (maxX - minX).coerceAtLeast(1f)
                                val h = (maxY - minY).coerceAtLeast(1f)
                                val padX = w * 0.05f
                                val padY = h * 0.05f
                                Rect(
                                    (minX - padX).toInt().coerceIn(0, finalBitmap.width),
                                    (minY - padY).toInt().coerceIn(0, finalBitmap.height),
                                    (maxX + padX).toInt().coerceIn(0, finalBitmap.width),
                                    (maxY + padY).toInt().coerceIn(0, finalBitmap.height)
                                )
                            } else {
                                Rect(0, 0, finalBitmap.width, finalBitmap.height)
                            }

                            withContext(Dispatchers.Main) {
                                ScanDraftHolder.capturedBitmap = finalBitmap
                                ScanDraftHolder.aggregatedLandmarks = meshResult.landmarks
                                ScanDraftHolder.faceBounds = computedBounds
                                ScanDraftHolder.scanKind = kind
                                ScanDraftHolder.qualityReport = if (forced) {
                                    report.copy(flags = report.flags + "Manual capture before all gates passed")
                                } else {
                                    report
                                }
                                onScanCaptured()
                            }
                        }
                    } catch (e: Exception) {
                        Logger.e("Capture processing failed", e)
                        scope.launch(Dispatchers.Main) {
                            isCapturing = false
                            captureErrorMessage = "Capture failed: ${e.message}"
                        }
                    } finally {
                        imageProxy.close()
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    Logger.e("ImageCapture error", exception)
                    scope.launch(Dispatchers.Main) {
                        isCapturing = false
                        captureErrorMessage = "Camera error: ${exception.message}"
                    }
                }
            }
        )
    }

    // Auto-capture after 8 consecutive allPass frames
    LaunchedEffect(gateFrame.consecutivePassCount) {
        if (gateFrame.consecutivePassCount >= 8 && !isCapturing) {
            performCapture(forced = false)
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val bmp = decodeGalleryUriToBitmap(context, uri)
                if (bmp != null) {
                    val result = faceMeshEngine.analyse(bmp)
                    ScanDraftHolder.capturedBitmap = bmp
                    ScanDraftHolder.aggregatedLandmarks = result.landmarks
                    ScanDraftHolder.faceBounds = Rect(0, 0, bmp.width, bmp.height)
                    ScanDraftHolder.scanKind = kind
                    ScanDraftHolder.qualityReport = QualityReport(
                        score = if (result.faceCount == 1 && result.landmarks.size >= 400) 90 else 0,
                        flags = if (result.faceCount == 0) listOf("No face detected in gallery photo") else emptyList(),
                        perGate = emptyMap()
                    )
                    onScanCaptured()
                }
            }
        }
    }

    // TalkBack live announcement on instruction change
    val accessibilityManager = remember {
        context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? android.view.accessibility.AccessibilityManager
    }
    var lastAnnouncedText by remember { mutableStateOf("") }
    LaunchedEffect(gateFrame.hint) {
        if (gateFrame.hint.isNotBlank() && gateFrame.hint != lastAnnouncedText) {
            lastAnnouncedText = gateFrame.hint
            if (accessibilityManager?.isEnabled == true) {
                val event = android.view.accessibility.AccessibilityEvent.obtain(
                    android.view.accessibility.AccessibilityEvent.TYPE_ANNOUNCEMENT
                ).apply {
                    text.add(gateFrame.hint)
                }
                accessibilityManager.sendAccessibilityEvent(event)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("live_capture_screen")
    ) {
        if (!hasCameraPermission) {
            CameraRationaleScreen(
                onAllowCamera = { permissionLauncher.launch(android.Manifest.permission.CAMERA) },
                onUseGallery = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                onOpenSettings = {
                    val intent = android.content.Intent(
                        android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        android.net.Uri.fromParts("package", context.packageName, null)
                    )
                    context.startActivity(intent)
                }
            )
        } else if (cameraError != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xEE0B0B0F))
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C26)),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = LumoraColors.WarningAmber,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Camera Unavailable",
                            fontFamily = Fraunces,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = cameraError ?: "Unable to connect to camera hardware.",
                            fontFamily = PlusJakartaSans,
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        PrimaryButton(
                            text = "Upload photo from gallery",
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            testTag = "camera_error_gallery_button"
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        QuietTextButton(
                            text = "Close",
                            onClick = onCloseClick,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        } else {
            // Full-screen camera preview
            AndroidView(
                factory = { ctx ->
                    PreviewView(ctx).apply {
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                        scaleX = if (isMirrored) 1f else -1f
                        previewViewRef = this
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // Dynamic Oval guide: changes color when allPass
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasWidth = size.width
                val canvasHeight = size.height

                val ovalWidth = canvasWidth * 0.68f
                val ovalHeight = canvasHeight * 0.44f
                val left = (canvasWidth - ovalWidth) / 2f
                val top = (canvasHeight - ovalHeight) / 2f
                val right = left + ovalWidth
                val bottom = top + ovalHeight
                val centerX = canvasWidth / 2f
                val centerY = canvasHeight / 2f

                val guideColor = if (gateFrame.allPass) LumoraColors.ChampagneGold else Color.White.copy(alpha = 0.70f)
                val strokeWidthPx = (if (gateFrame.allPass) 2.5.dp else 1.5.dp).toPx()
                val tickLenPx = 12.dp.toPx()

                // Oval outline
                drawOval(
                    color = guideColor,
                    topLeft = Offset(left, top),
                    size = Size(ovalWidth, ovalHeight),
                    style = Stroke(width = strokeWidthPx)
                )

                // Top tick
                drawLine(
                    color = guideColor,
                    start = Offset(centerX, top - tickLenPx / 2f),
                    end = Offset(centerX, top + tickLenPx / 2f),
                    strokeWidth = strokeWidthPx
                )
                // Bottom tick
                drawLine(
                    color = guideColor,
                    start = Offset(centerX, bottom - tickLenPx / 2f),
                    end = Offset(centerX, bottom + tickLenPx / 2f),
                    strokeWidth = strokeWidthPx
                )
                // Left tick
                drawLine(
                    color = guideColor,
                    start = Offset(left - tickLenPx / 2f, centerY),
                    end = Offset(left + tickLenPx / 2f, centerY),
                    strokeWidth = strokeWidthPx
                )
                // Right tick
                drawLine(
                    color = guideColor,
                    start = Offset(right - tickLenPx / 2f, centerY),
                    end = Offset(right + tickLenPx / 2f, centerY),
                    strokeWidth = strokeWidthPx
                )
            }

            // Top row: round X close button on left; on right "Upload photos" pill + mirror toggle + 3-dash step indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 44.dp, start = 20.dp, end = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onCloseClick,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0x66000000))
                        .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                        .testTag("live_capture_cancel")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    IconButton(
                        onClick = onToggleMirror,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0x66000000))
                            .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                            .testTag("mirror_toggle_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FlipCameraAndroid,
                            contentDescription = "Mirror Preview",
                            tint = if (isMirrored) LumoraColors.ChampagneGold else Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                            .clickable {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                            .testTag("upload_photos_button"),
                        color = Color(0x66000000)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoLibrary,
                                contentDescription = null,
                                tint = LumoraColors.ChampagneGold,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Upload photos",
                                fontFamily = PlusJakartaSans,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White
                            )
                        }
                    }

                    // 3-dash step indicator
                    val currentStep = if (kind == "side") 1 else 0
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (stepIndex in 0..2) {
                            Box(
                                modifier = Modifier
                                    .width(14.dp)
                                    .height(3.dp)
                                    .clip(RoundedCornerShape(1.5.dp))
                                    .background(
                                        if (stepIndex == currentStep) LumoraColors.ChampagneGold
                                        else Color.White.copy(alpha = 0.25f)
                                    )
                            )
                        }
                    }
                }
            }

            // Debug Overlay in BuildConfig.DEBUG
            if (BuildConfig.DEBUG) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(top = 100.dp, start = 16.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xCC000000)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = "Y: ${"%.1f".format(gateFrame.yaw)}° P: ${"%.1f".format(gateFrame.pitch)}° R: ${"%.1f".format(gateFrame.roll)}°",
                            fontSize = 10.sp,
                            color = Color.Green
                        )
                        Text(
                            text = "Luma: ${"%.0f".format(gateFrame.meanLuma)} Asym: ${"%.2f".format(gateFrame.lumaAsymmetry)}",
                            fontSize = 10.sp,
                            color = Color.Green
                        )
                        Text(
                            text = "Face: ${"%.0f".format(gateFrame.faceWidthPct)}% Drift: ${"%.3f".format(gateFrame.drift)}",
                            fontSize = 10.sp,
                            color = Color.Green
                        )
                        Text(
                            text = "Hold: ${gateFrame.consecutivePassCount} / 8 (${if (gateFrame.allPass) "ALL PASS" else "WAIT"})",
                            fontSize = 10.sp,
                            color = if (gateFrame.allPass) LumoraColors.ChampagneGold else Color.Yellow
                        )
                    }
                }
            }

            // Bottom controls & gate indicators
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color(0x990B0B0F),
                                Color(0xEE0B0B0F),
                                Color(0xFF0B0B0F)
                            )
                        )
                    )
                    .padding(horizontal = 20.dp)
                    .padding(top = 28.dp, bottom = 36.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                GoldEyebrow(
                    text = if (kind == "side") "STEP 2 OF 4 · SIDE" else "STEP 1 OF 4 · FRONT"
                )

                Spacer(modifier = Modifier.height(10.dp))

                SerifHeadline(
                    text = gateFrame.hint.ifBlank { "Centre your face in the oval" },
                    fontSize = 20.sp,
                    lineHeight = 26.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics {
                            liveRegion = LiveRegionMode.Polite
                        }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 5 small chips in one row: Frame, Angle, Light, Neutral, Still
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CaptureQualityChip(label = "Frame", isPassing = gateFrame.distanceReady, modifier = Modifier.weight(1f))
                    CaptureQualityChip(label = "Angle", isPassing = gateFrame.poseReady, modifier = Modifier.weight(1f))
                    CaptureQualityChip(label = "Light", isPassing = gateFrame.lightReady, modifier = Modifier.weight(1f))
                    CaptureQualityChip(label = "Neutral", isPassing = gateFrame.expressionReady, modifier = Modifier.weight(1f))
                    CaptureQualityChip(label = "Still", isPassing = gateFrame.steadyReady, modifier = Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(18.dp))

                if (gateFrame.consecutivePassCount > 0) {
                    val progress = (gateFrame.consecutivePassCount.toFloat() / 8f).coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .height(3.dp)
                            .clip(RoundedCornerShape(1.5.dp)),
                        color = LumoraColors.ChampagneGold,
                        trackColor = Color.White.copy(alpha = 0.15f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Shutter button (enabled when all gates ready or capturing)
                AnimatedVisibility(
                    visible = gateFrame.allPass || isCapturing,
                    enter = fadeIn(animationSpec = tween(300)),
                    exit = fadeOut(animationSpec = tween(200))
                ) {
                    Button(
                        onClick = { performCapture(forced = false) },
                        enabled = !isCapturing,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("shutter_button"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF5F5F7),
                            contentColor = Color(0xFF0B0B0F)
                        )
                    ) {
                        if (isCapturing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color(0xFF0B0B0F),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    tint = Color(0xFF0B0B0F),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Capture",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 16.sp,
                                    color = Color(0xFF0B0B0F)
                                )
                            }
                        }
                    }
                }

                // Manual capture anyway fallback
                if (!gateFrame.allPass && !isCapturing) {
                    TextButton(
                        onClick = { performCapture(forced = true) },
                        modifier = Modifier.testTag("capture_anyway_button")
                    ) {
                        Text(
                            text = "Capture anyway",
                            fontFamily = PlusJakartaSans,
                            fontSize = 13.sp,
                            color = LumoraColors.ChampagneGold.copy(alpha = 0.85f),
                            textDecoration = TextDecoration.Underline
                        )
                    }
                }
            }
        }

        // Rejection / error dialog
        if (captureErrorMessage != null) {
            AlertDialog(
                onDismissRequest = { captureErrorMessage = null },
                title = { Text("Quality Check", color = Color.White) },
                text = { Text(captureErrorMessage ?: "", color = Color.White.copy(alpha = 0.8f)) },
                confirmButton = {
                    Button(
                        onClick = { captureErrorMessage = null },
                        colors = ButtonDefaults.buttonColors(containerColor = LumoraColors.ChampagneGold)
                    ) {
                        Text("Try Again", color = Color.Black)
                    }
                },
                containerColor = Color(0xFF1C1C26)
            )
        }
    }
}

@Composable
fun CaptureQualityChip(
    label: String,
    isPassing: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = 1.dp,
                color = if (isPassing) Color.White.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.08f),
                shape = RoundedCornerShape(12.dp)
            ),
        color = if (isPassing) Color(0x33282834) else Color(0x2214141C)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(if (isPassing) LumoraColors.ChampagneGold else Color(0xFF6E6E78))
            )
            Spacer(modifier = Modifier.width(3.dp))
            if (isPassing) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(11.dp)
                )
                Spacer(modifier = Modifier.width(2.dp))
            }
            Text(
                text = label,
                fontFamily = PlusJakartaSans,
                fontSize = 11.sp,
                fontWeight = if (isPassing) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isPassing) Color(0xFFF7F7F8) else Color(0xFF7E7E88),
                maxLines = 1
            )
        }
    }
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
                    modifier = Modifier.size(72.dp),
                    color = Color.Transparent,
                    strokeWidth = 4.dp,
                    trackColor = Color.White.copy(alpha = 0.1f)
                )
                // Overlay a custom gradient indicator logic could go here, but using a themed standard one for now
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(72.dp),
                        color = LumoraColors.VioletPrimary,
                        strokeWidth = 4.dp
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))
                
                Text(
                    text = "Analysing Face",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(24.dp))

                val stages = listOf(
                    "Detecting landmarks" to "Mapping 468 3D points",
                    "Evaluating lighting" to "Checking luma and contrast",
                    "Calculating symmetry" to "Analysing lateral balance",
                    "Finalising report" to "Generating Lumora indices"
                )
                
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    stages.forEach { (title, subtitle) ->
                        val isActive = currentStageText.contains(title, ignoreCase = true) || 
                                     (currentStageText.contains("Finalising", ignoreCase = true) && title.contains("Finalising"))
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(if (isActive) LumoraColors.SecondaryCyan else Color.White.copy(alpha = 0.1f))
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = if (isActive) Color.White else Color.White.copy(alpha = 0.4f),
                                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
                                )
                                Text(
                                    text = subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isActive) Color.White.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.2f)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))
                QuietTextButton(text = "Cancel", onClick = onCancel, testTag = "processing_cancel_button")
            }
        }
    }
}
