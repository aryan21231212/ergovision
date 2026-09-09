package com.ergovision.app.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
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
import com.ergovision.app.service.CameraForegroundService
import com.ergovision.app.tts.TtsAlertManager
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

    private val currentMetrics = mutableStateOf(PostureMetrics(0L, 0f, 0f, 0f, 0f, null))
    private val currentScreenLandmarks = mutableStateOf<List<Point2D>>(emptyList())
    private val currentState = mutableStateOf(HazardState.SAFE)

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

        // Time-based debouncer: 5-second sustained hazard trigger
        debouncer = HazardDebouncer(
            windowDurationMs = 5000L,
            cooldownDurationMs = 15000L,
            onHazardTriggered = { hazardType, peakAngle, durationSec ->
                onHazardConfirmed(hazardType, peakAngle, durationSec)
            }
        )

        // Pose Estimator (MediaPipe Tasks Vision on GPU delegate)
        poseEstimator = MediaPipePoseEstimator(
            context = this,
            onPoseResult = { worldLandmarks, screenLandmarks, timestampMs ->
                val metrics = poseAnalyzer.analyze(worldLandmarks, timestampMs)
                currentMetrics.value = metrics
                currentScreenLandmarks.value = screenLandmarks
                currentState.value = debouncer.processFrame(metrics)
            },
            onError = { error ->
                runOnUiThread { Toast.makeText(this, error, Toast.LENGTH_SHORT).show() }
            }
        )

        setContent {
            ErgoVisionTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Camera Preview Surface
                        AndroidView(
                            factory = { ctx ->
                                PreviewView(ctx).also { previewView ->
                                    cameraManager = CameraXManager(
                                        context = this@MainActivity,
                                        lifecycleOwner = this@MainActivity,
                                        onFrameAvailable = { imageProxy ->
                                            poseEstimator.processImageProxy(imageProxy)
                                        }
                                    )
                                    cameraManager.startCamera(previewView.surfaceProvider)
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )

                        // Live Skeleton Pose Overlay
                        SkeletonOverlay(
                            landmarks = currentScreenLandmarks.value,
                            hazardActive = currentState.value == HazardState.TRIGGERED
                        )

                        // Top Ergonomic HUD
                        PostureHud(
                            metrics = currentMetrics.value,
                            state = currentState.value,
                            modifier = Modifier.align(Alignment.TopCenter)
                        )

                        // Bottom Office Kit Action Bar
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .background(Color(0xDD0F172A))
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Button(onClick = { exportWeeklySummary() }) {
                                Text("Sync Summary")
                            }
                            Button(onClick = { exportCsvLogs() }) {
                                Text("Export CSV")
                            }
                        }
                    }
                }
            }
        }

        checkAndRequestPermissions()
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
        // Tier 1: Instant deterministic audio alert (<50ms)
        ttsManager.triggerTemplateAlert(hazardType)

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
        lifecycleScope.launch {
            val recentEvents = repository.getRecentEvents(7 * 24 * 3600 * 1000L)
            val summaryText = llmCoach.generateWeeklySummary(recentEvents)
            officeKitBridge.copyToSharedClipboard("ErgoVision Weekly Report", summaryText)
            Toast.makeText(this@MainActivity, "Copied weekly summary to shared clipboard", Toast.LENGTH_SHORT).show()
        }
    }

    private fun exportCsvLogs() {
        lifecycleScope.launch {
            val csv = repository.exportToCsvString()
            val file = officeKitBridge.saveCsvForFileTransfer(csv)
            Toast.makeText(this@MainActivity, "Saved CSV log to: ${file.name}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::cameraManager.isInitialized) cameraManager.shutdown()
        if (::poseEstimator.isInitialized) poseEstimator.close()
        if (::ttsManager.isInitialized) ttsManager.shutdown()
        debouncer.reset()
    }
}
