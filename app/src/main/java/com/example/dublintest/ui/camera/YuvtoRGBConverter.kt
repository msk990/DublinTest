package com.example.dublintest.ui.camera

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.util.Log
import androidx.camera.core.ImageProxy
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

/**
 * CPU-based YUV_420_888 to RGB Bitmap converter using YuvImage (green-goo-proof).
 */
object YuvtoRGBConverter {

    fun convert(image: ImageProxy): Bitmap {
        val width = image.width
        val height = image.height

        val yPlane = image.planes[0]
        val uPlane = image.planes[1]
        val vPlane = image.planes[2]

        val ySize = width * height
        val uvSize = width * height / 4
        val nv21 = ByteArray(ySize + uvSize * 2)

        // Copy Y channel row by row (accounting for rowStride)
        val yBuffer = yPlane.buffer
        val yRowStride = yPlane.rowStride
        var pos = 0
        for (row in 0 until height) {
            yBuffer.position(row * yRowStride)
            yBuffer.get(nv21, pos, width)
            pos += width
        }

        // Copy UV interleaved (NV21 = VU) safely
        val uBuffer = uPlane.buffer
        val vBuffer = vPlane.buffer
        val uvRowStride = uPlane.rowStride
        val uvPixelStride = uPlane.pixelStride

        var offset = ySize
        for (row in 0 until height / 2) {
            for (col in 0 until width / 2) {
                val uvIndex = row * uvRowStride + col * uvPixelStride
                nv21[offset++] = vBuffer.get(uvIndex)
                nv21[offset++] = uBuffer.get(uvIndex)
            }
        }

        val yuvImage = YuvImage(nv21, ImageFormat.NV21, width, height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, width, height), 100, out)
        val jpegBytes = out.toByteArray()

        image.close()
        return BitmapFactory.decodeByteArray(jpegBytes, 0, jpegBytes.size)
    }
}
