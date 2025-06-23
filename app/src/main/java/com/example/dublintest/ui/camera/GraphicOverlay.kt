package com.example.dublintest.ui.camera

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

import android.graphics.Typeface


import androidx.core.graphics.toColorInt
import com.example.dublintest.ml.DetectionResult
import com.example.dublintest.ui.camera.utils.StickerManager
import com.example.dublintest.ui.camera.utils.drawLabel
import com.example.dublintest.ui.camera.utils.scaleBox

class GraphicOverlay @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val boxPaint = Paint().apply {
        color = "#00BCD4".toColorInt()
        style = Paint.Style.STROKE
        strokeWidth = 6f
        isAntiAlias = true
    }

    private val textBackgroundPaint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.FILL
        alpha = 255 // ✅ this is correct
    }


    private val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = 42f
        isAntiAlias = true
        typeface = Typeface.DEFAULT_BOLD
    }

    private var results: List<DetectionResult> = emptyList()
    private var scaleXFactor: Float = 1f
    private var scaleYFactor: Float = 1f

    fun update(detections: List<DetectionResult>, imageWidth: Int, imageHeight: Int) {
        this.results = detections
        scaleXFactor = width.toFloat() / imageWidth.toFloat()
        scaleYFactor = height.toFloat() / imageHeight.toFloat()
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        for (result in results) {
            val box = scaleBox(result.boundingBox, scaleXFactor, scaleYFactor)
            val labelText = "${result.label} ${(result.score * 100).toInt()}%"

            // Draw bounding box and label
            canvas.drawRect(box, boxPaint)

            drawLabel(
                canvas = canvas,
                text = labelText,
                box = box,
                textPaint = textPaint,
                backgroundPaint = textBackgroundPaint
            )


            // Draw sticker if available
            val labelKey = result.label.lowercase()
            val sticker = StickerManager.getScaledSticker(labelKey, (box.width() * 0.8f).toInt())
            if (sticker != null) {
                val centerX = box.centerX()
                val centerY = box.centerY()
                val left = centerX - sticker.width / 2f
                val top = centerY - sticker.height / 2f
                canvas.drawBitmap(sticker, left, top, null)
            }
        }
    }



}
