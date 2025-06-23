package com.example.dublintest.ui.camera.utils



import android.graphics.RectF

fun scaleBox(box: RectF, scaleX: Float, scaleY: Float): RectF {
    return RectF(
        box.left * scaleX,
        box.top * scaleY,
        box.right * scaleX,
        box.bottom * scaleY
    )
}
