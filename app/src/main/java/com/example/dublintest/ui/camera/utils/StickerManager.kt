package com.example.dublintest.ui.camera.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.core.graphics.scale

object StickerManager {
    private val labelToSticker = mutableMapOf<String, Bitmap>()
    private val stickerCache = mutableMapOf<Pair<String, Int>, Bitmap>()

    fun registerSticker(label: String, bitmap: Bitmap) {
        labelToSticker[label.lowercase()] = bitmap
    }

    fun loadFromAssets(context: Context, labelToPath: Map<String, String>) {
        labelToPath.forEach { (label, path) ->
            try {
                val inputStream = context.assets.open(path)
                inputStream.use {
                    val bitmap = BitmapFactory.decodeStream(it)
                    if (bitmap != null) {
                        labelToSticker[label.lowercase()] = bitmap
                        Log.d("StickerManager", "Loaded sticker for $label from $path")
                    } else {
                        Log.w("StickerManager", "Failed to decode image for $label from $path")
                    }
                }
            } catch (e: Exception) {
                Log.w("StickerManager", "Failed to load sticker for $label from $path: ${e.message}")
            }
        }
    }




    fun getScaledSticker(label: String, width: Int): Bitmap? {
        val base = labelToSticker[label.lowercase()] ?: return null
        val key = label.lowercase() to width
        return stickerCache.getOrPut(key) {
            val scale = width.toFloat() / base.width
            base.scale(width, (base.height * scale).toInt())
        }
    }
}
