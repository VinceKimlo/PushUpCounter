package com.example.pushupcounter.ui

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.view.PreviewView
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LifecycleOwner
import com.example.pushupcounter.PoseAnalyzer
import com.example.pushupcounter.PushUpCounter
import com.example.pushupcounter.PushUpViewModel
import java.util.concurrent.Executors

/**
 * PushUpCounterApp composable: holds camera preview, overlays, and status.
 *
 * - Starts CameraX Preview + ImageAnalysis when permission is granted
 * - Shows push-up count and simple form indicator overlayed on the preview
 */
@Composable
fun PushUpCounterApp(
    viewModel: PushUpViewModel,
    lifecycleOwner: LifecycleOwner,
    requestPermission: () -> Unit
) {
    val context = LocalContext.current

    val permissionGranted by remember { viewModel.permissionGranted }
    val count by remember { viewModel.count }
    val isDown by remember { viewModel.isDown }
    val goodForm by remember { viewModel.goodForm }

    // Create a single-thread executor for image analysis
    val analysisExecutor = remember { Executors.newSingleThreadExecutor() }

    // Keep the PushUpCounter instance for counting across frames
    val pushUpCounter = remember { PushUpCounter() }

    Box(modifier = Modifier.fillMaxSize()) {
        if (permissionGranted) {
            // Camera preview using AndroidView(PreviewView)
            AndroidView(factory = { ctx ->
                val previewView = PreviewView(ctx).apply {
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }

                startCamera(
                    context = ctx,
                    lifecycleOwner = lifecycleOwner,
                    previewView = previewView,
                    analysisExecutor = analysisExecutor,
                    // Analyzer will update viewModel state via pushUpCounter
                    onPose = { pose, width, height ->
                        val (newCount, down, formGood) = pushUpCounter.processPose(pose, width, height)
                        viewModel.updateFromAnalyzer(newCount, down, formGood)
                    }
                )
                previewView
            }, modifier = Modifier.fillMaxSize())
        } else {
            // Show permission request UI
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Camera permission required to detect push-ups")
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = requestPermission) {
                    Text("Grant Camera Permission")
                }
            }
        }

        // Overlay UI: count and status
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier
                    .wrapContentSize()
                    .align(Alignment.CenterHorizontally),
                colors = CardDefaults.cardColors(containerColor = Color(0xAA000000))
            ) {
                Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Push-ups", color = Color.White, style = MaterialTheme.typography.titleMedium)
                    Text("$count", color = Color.White, style = MaterialTheme.typography.displaySmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(if (goodForm) Color.Green else Color.Red, shape = CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (goodForm) "Good form" else "Adjust form",
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = if (isDown) "Down" else "Up", color = Color.White)
                }
            }
        }
    }
}

/**
 * startCamera: sets up CameraX preview and image analysis.
 *
 * - previewView: target preview surface
 * - analysisExecutor: Executor for ImageAnalysis
 * - onPose: callback invoked from PoseAnalyzer
 */
private fun startCamera(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    previewView: PreviewView,
    analysisExecutor: java.util.concurrent.Executor,
    onPose: (pose: com.google.mlkit.vision.pose.Pose, width: Int, height: Int) -> Unit
) {
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
    cameraProviderFuture.addListener({
        val cameraProvider = cameraProviderFuture.get()

        // Preview
        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }

        // ImageAnalysis: attach PoseAnalyzer
        val imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        imageAnalysis.setAnalyzer(
            analysisExecutor,
            PoseAnalyzer { pose, width, height ->
                onPose(pose, width, height)
            }
        )

        // Select camera (front for self-view; change to BACK if desired)
        val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageAnalysis)
        } catch (exc: Exception) {
            exc.printStackTrace()
        }
    }, ContextCompat.getMainExecutor(context))
}