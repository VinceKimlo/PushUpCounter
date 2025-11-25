package com.example.pushupcounter

import android.Manifest
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LifecycleOwner

/**
 * MainActivity: requests camera permission and starts the PushUpCounter UI.
 * The real camera setup is done by startCamera function invoked from composable when permission granted.
 */
class MainActivity : ComponentActivity() {

    private val viewModel: PushUpViewModel by viewModels()

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted) {
                Toast.makeText(this, "Camera permission is required", Toast.LENGTH_LONG).show()
            }
            viewModel.onPermissionResult(isGranted)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request permission immediately (alternatively can wait for UI button)
        requestPermissionLauncher.launch(Manifest.permission.CAMERA)

        setContent {
            MaterialTheme {
                val lifecycleOwner = LocalLifecycleOwner.current
                val context = LocalContext.current

                // Provide the UI and let it start camera when permission is granted
                PushUpCounterApp(
                    viewModel = viewModel,
                    lifecycleOwner = lifecycleOwner,
                    requestPermission = {
                        // fallback if user wants to re-request
                        requestPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                )
            }
        }
    }
}