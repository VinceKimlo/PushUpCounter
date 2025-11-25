package com.example.pushupcounter

import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * PushUpCounter: simple state machine to detect push-up repetitions from Pose data.
 *
 * Approach:
 * - We use nose and average shoulder Y to determine "down" vs "up".
 * - State machine:
 *     - If not in 'down' and nose goes below shoulder by downThreshold -> entering down.
 *     - If in 'down' and nose rises above shoulder by upThreshold -> count++ and leave down.
 * - Good form heuristic:
 *     - Compute torso angle between shoulder midpoint and hip midpoint; if torso is fairly horizontal (angle low),
 *       we mark as good form. This is a simplistic metric; tune as needed.
 *
 * Inputs:
 * - pose: ML Kit Pose
 * - imageWidth, imageHeight: to normalize coordinates (we use normalized Y).
 *
 * Returns:
 * - Triple(count, isDown, goodForm)
 */
class PushUpCounter {

    private var count = 0
    private var down = false

    // Thresholds (normalized to image height). Tune as needed.
    private val downThreshold = 0.06f  // nose below shoulders by this fraction -> down
    private val upThreshold = 0.02f    // nose above shoulders by this fraction -> up

    fun processPose(pose: Pose, imageWidth: Int, imageHeight: Int): Triple<Int, Boolean, Boolean> {
        // Get landmarks we need
        val nose = pose.getPoseLandmark(PoseLandmark.NOSE)
        val leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)
        val rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)
        val leftHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP)
        val rightHip = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP)

        if (nose == null || leftShoulder == null || rightShoulder == null || leftHip == null || rightHip == null) {
            // Not enough data to evaluate
            return Triple(count, down, false)
        }

        // Normalize Y coordinates to [0,1] by image height
        val noseY = nose.position.y / imageHeight.toFloat()
        val shoulderY = (leftShoulder.position.y + rightShoulder.position.y) / 2f / imageHeight.toFloat()
        val hipY = (leftHip.position.y + rightHip.position.y) / 2f / imageHeight.toFloat()

        // Detect down/up transitions
        if (!down) {
            // If nose is sufficiently below shoulders (y increases downwards), mark as down
            if (noseY - shoulderY > downThreshold) {
                down = true
            }
        } else {
            // If we were down and now nose is above shoulders enough, count a rep
            if (shoulderY - noseY > upThreshold) {
                down = false
                count += 1
            }
        }

        // Simple "good form" heuristic: torso angle (shoulder midpoint -> hip midpoint)
        val shoulderMidX = (leftShoulder.position.x + rightShoulder.position.x) / 2f
        val shoulderMidY = (leftShoulder.position.y + rightShoulder.position.y) / 2f
        val hipMidX = (leftHip.position.x + rightHip.position.x) / 2f
        val hipMidY = (leftHip.position.y + rightHip.position.y) / 2f

        val dx = hipMidX - shoulderMidX
        val dy = hipMidY - shoulderMidY
        // Angle relative to horizontal in degrees (0 means perfectly horizontal)
        val angle = Math.toDegrees(abs(atan2(dy.toDouble(), dx.toDouble())))

        val goodForm = angle < 20.0 // torso fairly straight (horizontal-ish) -> good form

        return Triple(count, down, goodForm)
    }

    fun reset() {
        count = 0
        down = false
    }
}