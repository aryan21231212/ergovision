package com.ergovision.app.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.ergovision.app.bridge.OfficeKitBridge
import com.ergovision.app.camera.CameraXManager
import com.ergovision.app.data.database.HazardDatabase
import com.ergovision.app.data.entity.HazardEvent
import com.ergovision.app.data.model.HazardState
import com.ergovision.app.data.model.HazardType
import com.ergovision.app.data.model.Point2D
import com.ergovision.app.data.model.PostureMetrics
import com.ergovision.app.data.repository.HazardRepository
import com.ergovision.app.debouncer.HazardDebouncer
import com.ergovision.app.llm.LiteRtLmCoach
import com.ergovision.app.math.RebaPoseAnalyzer
import com.ergovision.app.pose.MediaPipePoseEstimator
import com.ergovision.app.sensor.ImuPostureTracker
import com.ergovision.app.service.CameraForegroundService
import com.ergovision.app.tts.TtsAlertManager
import com.ergovision.app.ui.components.BottomControlDock
import com.ergovision.app.ui.components.ComplianceReportDialog
import com.ergovision.app.ui.components.HazardLogSheet
import com.ergovision.app.ui.components.PocketModeView
import com.ergovision.app.ui.components.PostureHud
import com.ergovision.app.ui.components.SkeletonOverlay
import com.ergovision.app.ui.theme.ErgoVisionTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var cameraManager: CameraXManager
    private lateinit var poseEstimator: MediaPipePoseEstimator
    private lateinit var poseAnalyzer: RebaPoseAnalyzer
    private lateinit var debouncer: HazardDebouncer
    private lateinit var ttsManager: TtsAlertManager
    private lateinit var repository: HazardRepository
    private lateinit var llmCoach: LiteRtLmCoach
    private lateinit var officeKitBridge: OfficeKitBridge
    private lateinit var imuTracker: ImuPostureTracker

    private val currentMetrics = mutableStateOf(PostureMetrics(0L, 0f, 0f, 0f, 0f, null))
    private val currentScreenLandmarks = mutableStateOf<List<Point2D>>(emptyList())
    private val currentState = mutableStateOf(HazardState.SAFE)
    private val isThermalThrottled = mutableStateOf(false)
    private val isPocketMode = mutableStateOf(false)
    private val showLogsSheet = mutableStateOf(false)
    private val isDimmedMode = mutableStateOf(false)
    private val isAudioMuted = mutableStateOf(false)
    private val isFrontCamera = mutableStateOf(false)
    private val showReportDialog = mutableStateOf(false)
    private val generatedReportText = mutableStateOf("")
    private val isGeneratingReport = mutableStateOf(false)

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startErgoVisionPipeline()
        } else {
            Toast.makeText(this, "Camera permission is required.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Core Engines
        val db = HazardDatabase.getInstance(this)
        repository = HazardRepository(db.hazardEventDao())
        ttsManager = TtsAlertManager(this)
        llmCoach = LiteRtLmCoach().apply { initialize("models/gemma3_1b.bin") }
        officeKitBridge = OfficeKitBridge(this)
        poseAnalyzer = RebaPoseAnalyzer()

        setupThermalMonitoring()

        // Time-based debouncer: 5-second sustained hazard trigger
        debouncer = HazardDebouncer(
            windowDurationMs = 5000L,
            cooldownDurationMs = 15000L,
            onHazardTriggered = { hazardType, peakAngle, durationSec ->
                onHazardConfirmed(hazardType, peakAngle, durationSec)
            }
        )

        // Stretch H1: Low-power IMU Posture Tracker for pocket/clip usage
        imuTracker = ImuPostureTracker(this) { metrics ->
            if (isPocketMode.value) {
                currentMetrics.value = metrics
                currentScreenLandmarks.value = emptyList()
                currentState.value = debouncer.processFrame(metrics)
            }
        }

        // Pose Estimator (MediaPipe Tasks Vision on GPU delegate)
        poseEstimator = MediaPipePoseEstimator(
            context = this,
            onPoseResult = { worldLandmarks, screenLandmarks, timestampMs ->
                if (!isPocketMode.value) {
                    val metrics = poseAnalyzer.analyze(worldLandmarks, timestampMs)
                    currentMetrics.value = metrics
                    currentScreenLandmarks.value = screenLandmarks
                    currentState.value = debouncer.processFrame(metrics)
                }
            },
            onError = { error ->
                runOnUiThread { Toast.makeText(this, error, Toast.LENGTH_SHORT).show() }
            }
        )

        setContent {
            ErgoVisionTheme {
                val eventsList by repository.allEventsFlow.collectAsState(initial = emptyList())

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Camera Preview Surface (Responsive full-fill without distortion)
                        AndroidView(
                            factory = { ctx ->
                                PreviewView(ctx).also { previewView ->
                                    previewView.scaleType = PreviewView.ScaleType.FILL_CENTER
                                    previewView.implementationMode = PreviewView.ImplementationMode.PERFORMANCE
                                    cameraManager = CameraXManager(
                                        context = this@MainActivity,
                                        lifecycleOwner = this@MainActivity,
                                        onFrameAvailable = { imageProxy ->
                                            if (!isPocketMode.value) {
                                                poseEstimator.processImageProxy(imageProxy)
                                            }
                                        }
                                    )
                                    cameraManager.startCamera(previewView.surfaceProvider)
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )

                        // Mode-specific display: AR Skeleton vs Pocket Sensor Dial
                        if (!isPocketMode.value) {
                            SkeletonOverlay(
                                landmarks = currentScreenLandmarks.value,
                                metrics = currentMetrics.value
                            )
                        } else {
                            PocketModeView(
                                metrics = currentMetrics.value,
                                state = currentState.value,
                                onCalibrateClick = { calibratePosture() },
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        // Responsive Top Industrial Posture HUD
                        PostureHud(
                            metrics = currentMetrics.value,
                            state = currentState.value,
                            isThermalThrottled = isThermalThrottled.value,
                            isPocketMode = isPocketMode.value,
                            isAudioMuted = isAudioMuted.value,
                            isFrontCamera = isFrontCamera.value,
                            onCalibrateClick = { calibratePosture() },
                            onDimScreenClick = { isDimmedMode.value = true },
                            onToggleModeClick = { togglePostureMode() },
                            onToggleAudioClick = { isAudioMuted.value = !isAudioMuted.value },
                            onToggleCameraClick = {
                                if (::cameraManager.isInitialized) {
                                    isFrontCamera.value = cameraManager.toggleCamera()
                                    Toast.makeText(this@MainActivity, if (isFrontCamera.value) "Front Camera (Self-Test)" else "Back Camera (Mount)", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .widthIn(max = 600.dp)
                        )

                        // Responsive Floating Glassmorphic Bottom Control Dock
                        BottomControlDock(
                            eventCount = eventsList.size,
                            onEhsAuditClick = { exportWeeklySummary() },
                            onLogsClick = { showLogsSheet.value = true },
                            onExportCsvClick = { exportCsvLogs() },
                            onSimulateHazardClick = {
                                onHazardConfirmed(HazardType.TRUNK_FLEXION_SEVERE, 65f, 5.0f)
                                Toast.makeText(this@MainActivity, "Simulated Hazard Alert", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .widthIn(max = 600.dp)
                        )

                        // OLED Low-Power / Privacy Dimmed Overlay
                        if (isDimmedMode.value) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black)
                                    .clickable { isDimmedMode.value = false },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .background(
                                                if (currentState.value == HazardState.TRIGGERED) com.ergovision.app.ui.theme.HazardRed
                                                else com.ergovision.app.ui.theme.HazardGreen,
                                                androidx.compose.foundation.shape.CircleShape
                                            )
                                    )
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Text(
                                        text = "OLED Low-Power Monitoring Active",
                                        color = Color(0xFF64748B),
                                        fontSize = 13.sp,
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                                    )
                                    Text(
                                        text = "Camera inference running • Tap anywhere to wake",
                                        color = Color(0xFF475569),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        // Hazard Log Inspector Sheet
                        if (showLogsSheet.value) {
                            HazardLogSheet(
                                events = eventsList,
                                onDismiss = { showLogsSheet.value = false },
                                onClearLogs = {
                                    lifecycleScope.launch {
                                        repository.clearLogs()
                                        Toast.makeText(this@MainActivity, "Logs cleared", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        }

                        // On-Device LiteRT-LM Compliance Audit Report Sheet
                        if (showReportDialog.value) {
                            ComplianceReportDialog(
                                reportText = generatedReportText.value,
                                eventCount = eventsList.size,
                                events = eventsList,
                                isLoading = isGeneratingReport.value,
                                onDismiss = { showReportDialog.value = false },
                                onCopyToClipboard = {
                                    officeKitBridge.copyToSharedClipboard("ErgoVision Weekly Report", generatedReportText.value)
                                    Toast.makeText(this@MainActivity, "Copied report to shared clipboard", Toast.LENGTH_SHORT).show()
                                },
                                onShareReport = {
                                    val shareIntent = officeKitBridge.createShareIntent(generatedReportText.value)
                                    startActivity(Intent.createChooser(shareIntent, "Share EHS Audit Report (Office Kit)"))
                                }
                            )
                        }
                    }
                }
            }
        }

        checkAndRequestPermissions()
    }

    private fun setupThermalMonitoring() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager
            powerManager?.addThermalStatusListener { status ->
                val throttled = status >= PowerManager.THERMAL_STATUS_MODERATE
                isThermalThrottled.value = throttled
                if (::cameraManager.isInitialized) {
                    cameraManager.setFrameInterval(if (throttled) 500L else 200L)
                }

                // Section H2 Thermal Synergy: Auto-switch to low-power IMU mode on severe heat
                if (status >= PowerManager.THERMAL_STATUS_SEVERE && !isPocketMode.value) {
                    togglePostureMode()
                    Toast.makeText(this, "Thermal Protection: Auto-switched to low-power IMU mode", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun togglePostureMode() {
        val newMode = !isPocketMode.value
        isPocketMode.value = newMode
        if (newMode) {
            imuTracker.start()
            imuTracker.calibrateBaseline()
            Toast.makeText(this, "Pocket/Clip IMU Mode Active (Camera suspended)", Toast.LENGTH_SHORT).show()
        } else {
            imuTracker.stop()
            Toast.makeText(this, "Camera Mount Mode Active", Toast.LENGTH_SHORT).show()
        }
    }

    private fun calibratePosture() {
        if (isPocketMode.value) {
            imuTracker.calibrateBaseline()
            Toast.makeText(this, "Neutral standing baseline calibrated for Pocket IMU", Toast.LENGTH_SHORT).show()
        } else {
            val currentTrunk = currentMetrics.value.trunkAngleDegrees
            poseAnalyzer.calibrateBaseline(currentTrunk)
            Toast.makeText(this, "Neutral posture calibrated (zeroed at ${currentTrunk.toInt()}°)", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startErgoVisionPipeline()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun startErgoVisionPipeline() {
        val serviceIntent = Intent(this, CameraForegroundService::class.java)
        ContextCompat.startForegroundService(this, serviceIntent)
    }

    private fun onHazardConfirmed(hazardType: HazardType, peakAngle: Float, durationSec: Float) {
        // Tier 1: Instant deterministic audio alert (<50ms) if not muted
        if (!isAudioMuted.value) {
            ttsManager.triggerTemplateAlert(hazardType)
        }

        // Tier 2: Persist event and asynchronously synthesize LLM coaching
        lifecycleScope.launch {
            val event = HazardEvent(
                timestampMs = System.currentTimeMillis(),
                durationSeconds = durationSec,
                hazardType = hazardType,
                peakAngleDegrees = peakAngle,
                peakScore = peakAngle / 30f
            )
            val eventId = repository.recordHazard(event)

            val advice = llmCoach.generateCoachingAdvice(event)
            repository.updateCoaching(eventId, advice)
        }
    }

    private fun exportWeeklySummary() {
        isGeneratingReport.value = true
        showReportDialog.value = true
        lifecycleScope.launch {
            val recentEvents = repository.getRecentEvents(7 * 24 * 3600 * 1000L)
            val summaryText = llmCoach.generateWeeklySummary(recentEvents)
            generatedReportText.value = summaryText
            isGeneratingReport.value = false
        }
    }

    private fun exportCsvLogs() {
        lifecycleScope.launch {
            val csv = repository.exportToCsvString()
            val file = officeKitBridge.saveCsvForFileTransfer(csv)
            val shareIntent = officeKitBridge.createShareIntent(csv)
            startActivity(Intent.createChooser(shareIntent, "Share Hazard CSV (Office Kit)"))
            Toast.makeText(this@MainActivity, "Saved CSV to: ${file.name}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::cameraManager.isInitialized) cameraManager.shutdown()
        if (::poseEstimator.isInitialized) poseEstimator.close()
        if (::ttsManager.isInitialized) ttsManager.shutdown()
        if (::imuTracker.isInitialized) imuTracker.stop()
        debouncer.reset()
    }
}
