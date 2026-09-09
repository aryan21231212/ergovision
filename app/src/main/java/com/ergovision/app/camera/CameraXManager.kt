package com.ergovision.app.camera

import android.content.Context
import android.os.SystemClock
import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * CameraX 1.5+ manager throttled to ~5 FPS to prevent thermal saturation.
 * Uses RGBA_8888 and STRATEGY_KEEP_ONLY_LATEST backpressure.
 */
class CameraXManager(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val onFrameAvailable: (ImageProxy) -> Unit
) {

    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private var lastProcessedFrameTimestampMs: Long = 0L
    private val targetFrameIntervalMs = 200L // 5 FPS throttle interval

    fun startCamera(previewSurfaceProvider: Preview.SurfaceProvider? = null) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            // 640x480 resolution to conserve mobile GPU / NPU memory bandwidth
            val targetResolution = Size(640, 480)

            val imageAnalysis = ImageAnalysis.Builder()
                .setTargetResolution(targetResolution)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                val currentTime = SystemClock.elapsedRealtime()

                // Hardware throttling fallback gate: guarantee ~5 FPS even if HAL ignores frame config
                if (currentTime - lastProcessedFrameTimestampMs >= targetFrameIntervalMs) {
                    lastProcessedFrameTimestampMs = currentTime
                    try {
                        onFrameAvailable(imageProxy)
                    } finally {
                        imageProxy.close()
                    }
                } else {
                    imageProxy.close()
                }
            }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()

                if (previewSurfaceProvider != null) {
                    val preview = Preview.Builder()
                        .setTargetResolution(targetResolution)
                        .build()
                    preview.setSurfaceProvider(previewSurfaceProvider)
                    cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageAnalysis)
                } else {
                    cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, imageAnalysis)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(context))
    }

    fun shutdown() {
        cameraExecutor.shutdown()
    }
}
