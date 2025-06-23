package com.example.dublintest.stickers.model

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object StickerAssetMap {

    data class StickerInfo(
        val image: String,
        val description: String,
        val type: StickerType,
        val domains: List<String> = emptyList()
    )


    private val stickerMap = mutableMapOf<String, StickerInfo>()
    private val stickerBitmaps = mutableMapOf<String, Bitmap>()

    fun loadStickerData(context: Context): Map<String, StickerInfo> {
        return try {
            val json = context.assets.open("stickers/sticker_assets.json")
                .bufferedReader()
                .use { it.readText() }

            val type = object : TypeToken<Map<String, StickerInfo>>() {}.type
            val data: Map<String, StickerInfo> = Gson().fromJson(json, type)

            stickerMap.clear()
            stickerMap.putAll(data)
            preloadBitmaps(context, data)
            data
        } catch (e: Exception) {
            Log.w("StickerAssetMap", "Failed to load JSON: ${e.message}")
            Toast.makeText(context, "Could not load sticker data. Using default setup.", Toast.LENGTH_LONG).show()

            val fallback = mapOf(
                "laptop" to StickerInfo("stickers/hi.png", "This shiny laptop sticker is awarded when a laptop is detected!", StickerType.COLLECTIBLE),
                "bicycle" to StickerInfo("stickers/sing.png", "Ride into fun with this bicycle sticker!", StickerType.INTERACTIVE)
            )

            stickerMap.clear()
            stickerMap.putAll(fallback)
            preloadBitmaps(context, fallback)
            fallback
        }
    }

    private fun preloadBitmaps(context: Context, data: Map<String, StickerInfo>) {
        data.forEach { (label, info) ->
            try {
                context.assets.open(info.image).use {
                    stickerBitmaps[label] = BitmapFactory.decodeStream(it)
                }
            } catch (e: Exception) {
                Log.w("StickerAssetMap", "Failed to load bitmap for $label: ${e.message}")
            }
        }
    }

    fun getStickerInfo(label: String): StickerInfo? = stickerMap[label]

    fun getBitmap(label: String): Bitmap? = stickerBitmaps[label]
}
