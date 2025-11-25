package com.example.pushupcounter

import android.annotation.SuppressLint
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.AccuratePoseDetectorOptions
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseLandmark

/**
 * PoseAnalyzer: an ImageAnalysis.Analyzer that runs ML Kit Pose Detection on camera frames.
 *
 * It calls `onPoseDetected` with the Pose and image dimensions when a detection is available.
 *
 * Important: we close the imageProxy on completion to avoid blocking the camera pipeline.
 */
class PoseAnalyzer(
    private val onPoseDetected: (pose: Pose, imageWidth: Int, imageHeight: Int) -> Unit
) : ImageAnalysis.Analyzer {

    private val options = AccuratePoseDetectorOptions.Builder()
        // Use STREAM_MODE for real-time detection (lower latency)
        .setDetectorMode(AccuratePoseDetectorOptions.STREAM_MODE)
        .build()

    private val detector = PoseDetection.getClient(options)

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }
        val rotationDegrees = imageProxy.imageInfo.rotationDegrees
        val inputImage = InputImage.fromMediaImage(mediaImage, rotationDegrees)

        detector.process(inputImage)
            .addOnSuccessListener { pose ->
                // Call the callback with pose and original image dimensions
                onPoseDetected(pose, inputImage.width, inputImage.height)
            }
            .addOnFailureListener {
                // ignore detection failures for now (could log)
            }
            .addOnCompleteListener {
                // Very important: close the imageProxy to let CameraX continue
                imageProxy.close()
            }
    }
}