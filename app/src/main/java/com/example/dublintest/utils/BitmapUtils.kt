package com.example.dublintest.utils

import android.content.Context
import android.graphics.*
import android.util.Log
import java.io.File
import java.io.FileOutputStream

object BitmapUtils {

    fun rotateBitmap(bitmap: Bitmap, rotationDegrees: Int): Bitmap {
        val matrix = Matrix().apply {
            postRotate(rotationDegrees.toFloat())
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    fun resizeWithAspectRatioAndPadding(bitmap: Bitmap, targetSize: Int): Bitmap {
        val scale = targetSize.toFloat() / maxOf(bitmap.width, bitmap.height)
        val newWidth = (bitmap.width * scale).toInt()
        val newHeight = (bitmap.height * scale).toInt()
        val scaled = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)

        val output = Bitmap.createBitmap(targetSize, targetSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        val bgPaint = Paint().apply { color = Color.BLACK }
        canvas.drawRect(0f, 0f, targetSize.toFloat(), targetSize.toFloat(), bgPaint)

        val dx = ((targetSize - newWidth) / 2f)
        val dy = ((targetSize - newHeight) / 2f)

        val paint = Paint().apply {
            isFilterBitmap = true
            isAntiAlias = true
            alpha = 255
        }

        canvas.drawBitmap(scaled, dx, dy, paint)
        return output
    }




    fun saveBitmapToInternalStorage(
        context: Context,
        bitmap: Bitmap,
        filename: String = "model_input_${System.currentTimeMillis()}.png"
    ) {
        try {
            val file = File(context.filesDir, filename)
            FileOutputStream(file).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            }
            Log.d("BitmapUtils", "Saved image to: ${file.absolutePath}")
        } catch (e: Exception) {
            Log.e("BitmapUtils", "Failed to save image", e)
        }
    }
}
