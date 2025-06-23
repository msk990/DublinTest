package com.example.dublintest.ml

import android.graphics.RectF

data class DetectionResult(
    val label: String,
    val score: Float,
    val boundingBox: RectF
)
