package com.example.dublintest.ui.camera.utils


import android.graphics.*

fun drawLabel(
    canvas: Canvas,
    text: String,
    box: RectF,
    textPaint: Paint,
    backgroundPaint: Paint,
    padding: Float = 8f
) {
    val textWidth = textPaint.measureText(text)
    val textHeight = textPaint.textSize

    val bgLeft = box.left
    val bgRight = bgLeft + textWidth + 2 * padding
    val bgBottom = box.top
    val bgTop = bgBottom - textHeight - 2 * padding

    if (bgTop > 0) {
        canvas.drawRect(bgLeft, bgTop, bgRight, bgBottom, backgroundPaint)
        canvas.drawText(text, bgLeft + padding, bgBottom - padding, textPaint)
    } else {
        val fallbackTop = box.top + padding
        canvas.drawRect(bgLeft, fallbackTop, bgRight, fallbackTop + textHeight + 2 * padding, backgroundPaint)
        canvas.drawText(text, bgLeft + padding, fallbackTop + textHeight + padding / 2, textPaint)
    }
}
