package com.ergovision.app.pose

import android.content.Context
import android.graphics.Bitmap
import android.os.SystemClock
import androidx.camera.core.ImageProxy
import com.ergovision.app.data.model.Point2D
import com.ergovision.app.data.model.Point3D
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.core.Delegate
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult

/**
 * MediaPipe Tasks Vision wrapper utilizing pose_landmarker_lite.task.
 * Defaults to Qualcomm GPU Delegate for sub-15ms inference.
 */
class MediaPipePoseEstimator(
    private val context: Context,
    private val onPoseResult: (worldLandmarks: List<Point3D>, screenLandmarks: List<Point2D>, timestampMs: Long) -> Unit,
    private val onError: (String) -> Unit
) {

    private var poseLandmarker: PoseLandmarker? = null

    init {
        initializePoseLandmarker()
    }

    private fun initializePoseLandmarker() {
        try {
            val baseOptions = BaseOptions.builder()
                .setModelAssetPath("pose_landmarker_lite.task")
                .setDelegate(Delegate.GPU) // Fast and stable on Adreno GPUs
                .build()

            val options = PoseLandmarker.PoseLandmarkerOptions.builder()
                .setBaseOptions(baseOptions)
                .setRunningMode(RunningMode.LIVE_STREAM)
                .setMinPoseDetectionConfidence(0.5f)
                .setMinTrackingConfidence(0.5f)
                .setResultListener { result: PoseLandmarkerResult, _: MPImage ->
                    val timestamp = SystemClock.elapsedRealtime()
                    // Extract 3D World Landmarks (metric coordinates, hip-origin) for REBA math
                    val worldLandmarks = result.worldLandmarks().firstOrNull()?.map { lm ->
                        Point3D(lm.x(), lm.y(), lm.z(), lm.visibility().orElse(1.0f))
                    } ?: emptyList()

                    // Extract 2D Normalized Screen Landmarks (0.0 to 1.0) for live HUD skeleton canvas
                    val screenLandmarks = result.landmarks().firstOrNull()?.map { lm ->
                        Point2D(lm.x(), lm.y())
                    } ?: emptyList()

                    onPoseResult(worldLandmarks, screenLandmarks, timestamp)
                }
                .setErrorListener { error ->
                    onError(error.message ?: "MediaPipe Pose estimation error")
                }
                .build()

            poseLandmarker = PoseLandmarker.createFromOptions(context, options)
        } catch (e: Exception) {
            tryFallbackToCpu()
        }
    }

    private fun tryFallbackToCpu() {
        try {
            val baseOptions = BaseOptions.builder()
                .setModelAssetPath("pose_landmarker_lite.task")
                .setDelegate(Delegate.CPU)
                .build()

            val options = PoseLandmarker.PoseLandmarkerOptions.builder()
                .setBaseOptions(baseOptions)
                .setRunningMode(RunningMode.LIVE_STREAM)
                .setResultListener { result: PoseLandmarkerResult, _: MPImage ->
                    val timestamp = SystemClock.elapsedRealtime()
                    val worldLandmarks = result.worldLandmarks().firstOrNull()?.map { lm ->
                        Point3D(lm.x(), lm.y(), lm.z(), lm.visibility().orElse(1.0f))
                    } ?: emptyList()
                    val screenLandmarks = result.landmarks().firstOrNull()?.map { lm ->
                        Point2D(lm.x(), lm.y())
                    } ?: emptyList()
                    onPoseResult(worldLandmarks, screenLandmarks, timestamp)
                }
                .build()

            poseLandmarker = PoseLandmarker.createFromOptions(context, options)
        } catch (e: Exception) {
            onError("Fatal: Unable to initialize MediaPipe PoseLandmarker: ${e.message}")
        }
    }

    fun processImageProxy(imageProxy: ImageProxy) {
        val landmarker = poseLandmarker ?: return
        val bitmap = imageProxy.toBitmap()
        val mpImage = BitmapImageBuilder(bitmap).build()
        val frameTime = SystemClock.uptimeMillis()
        landmarker.detectAsync(mpImage, frameTime)
    }

    fun close() {
        poseLandmarker?.close()
        poseLandmarker = null
    }
}
