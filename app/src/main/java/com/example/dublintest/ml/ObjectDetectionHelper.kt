package com.example.dublintest.ml

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.vision.detector.ObjectDetector
import org.tensorflow.lite.task.vision.detector.Detection

class ObjectDetectionHelper(context: Context) {

    private val detector: ObjectDetector = ObjectDetector.createFromFileAndOptions(
        context,
        "models/efficientdet.tflite",
        ObjectDetector.ObjectDetectorOptions.builder()
            .setMaxResults(5)
            .setScoreThreshold(0.5f)
            .build()
    )

    /**
     * Takes a 320x320 upright RGB [Bitmap] and returns a list of [Detection]s.
     */
    fun detect(bitmap: Bitmap): List<Detection> = try {
        detector.detect(TensorImage.fromBitmap(bitmap))
    } catch (e: Exception) {
        Log.e("ObjectDetectionHelper", "Detection failed", e)
        emptyList()
    }

    fun detectFormatted(bitmap: Bitmap): List<DetectionResult> {
        return detect(bitmap).flatMap { detection ->
            detection.categories.map { category ->
                DetectionResult(
                    label = category.label,
                    score = category.score,
                    boundingBox = detection.boundingBox
                )
            }
        }
    }
}
