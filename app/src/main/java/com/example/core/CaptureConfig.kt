package com.example.core

object CaptureConfig {
    const val MAX_FRONT_YAW_DEG = 8.0f
    const val MAX_FRONT_PITCH_DEG = 8.0f
    const val MAX_FRONT_ROLL_DEG = 6.0f

    const val MIN_SIDE_YAW_DEG = 70.0f
    const val MAX_SIDE_YAW_DEG = 90.0f

    const val MIN_FACE_WIDTH_RATIO = 0.40f
    const val MAX_FACE_WIDTH_RATIO = 0.75f

    const val MIN_MEAN_LUMA = 70f
    const val MAX_MEAN_LUMA = 200f
    const val MAX_LUMA_ASYMMETRY = 0.15f
    const val MAX_SPECULAR_CLIP_FRACTION = 0.05f

    const val DEFAULT_SHARPNESS_FLOOR = 75.0f

    const val MIN_EYE_OPEN_PROB = 0.60f
    const val MAX_SMILE_PROB = 0.30f

    const val MAX_STILLNESS_DRIFT_RATIO = 0.015f
    const val STILLNESS_WINDOW_MS = 600L

    const val REQUIRED_CONSECUTIVE_FRAMES = 8
    const val BURST_SAMPLE_COUNT = 8
}
