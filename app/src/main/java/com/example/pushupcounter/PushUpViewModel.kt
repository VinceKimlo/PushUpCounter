package com.example.pushupcounter

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

/**
 * ViewModel holds state for the compose UI.
 * - count: total push-ups counted
 * - isDown: whether user is currently in down position
 * - goodForm: whether form is acceptable
 * - permissionGranted: camera permission state
 */
class PushUpViewModel : ViewModel() {
    var count = mutableStateOf(0)
        private set

    var isDown = mutableStateOf(false)
        private set

    var goodForm = mutableStateOf(false)
        private set

    var permissionGranted = mutableStateOf(false)
        private set

    fun onPermissionResult(granted: Boolean) {
        permissionGranted.value = granted
    }

    // Called by analyzer / PushUpCounter logic
    fun updateFromAnalyzer(newCount: Int, down: Boolean, formGood: Boolean) {
        viewModelScope.launch {
            if (newCount != count.value) {
                count.value = newCount
            }
            isDown.value = down
            goodForm.value = formGood
        }
    }
}