package com.example.tripi.stickers.ui

import android.graphics.RectF
import android.util.Log
import android.widget.FrameLayout
import com.example.dublintest.ml.DetectionResult
import com.example.dublintest.stickers.model.StickerAssetMap
import com.example.dublintest.stickers.ui.StickerOverlayManager
import com.example.dublintest.ui.camera.utils.scaleBox
import com.example.dublintest.ui.profile.ProfileType

class StickerPlacementManager(
    private val overlay: FrameLayout,
    private val stickerOverlayManager: StickerOverlayManager
) {

//    fun showStickers(results: List<DetectionResult>, imageWidth: Int, imageHeight: Int) {
//        val scaleX = overlay.width.toFloat() / imageWidth.toFloat()
//        val scaleY = overlay.height.toFloat() / imageHeight.toFloat()
//
//        overlay.removeAllViews()
//
//        for (result in results) {
//            val label = result.label.lowercase()
//            val box: RectF = scaleBox(result.boundingBox, scaleX, scaleY)
//           // val sticker = StickerManager.getScaledSticker(label, (box.width() * 0.8f).toInt())
//            val sticker = StickerAssetMap.getBitmap(label)
//            if (sticker != null) {
//                stickerOverlayManager.showSticker(label, box, sticker)
//            } else {
//                Log.d("StickerPlacementManager", "No sticker for label: $label")
//            }
//        }
//    }

    fun showStickers(
        results: List<DetectionResult>,
        imageWidth: Int,
        imageHeight: Int,
        currentProfile: ProfileType // 👈 pass this in from the fragment
    ) {
        val scaleX = overlay.width.toFloat() / imageWidth.toFloat()
        val scaleY = overlay.height.toFloat() / imageHeight.toFloat()

        overlay.removeAllViews()

        val allowedDomains = when (currentProfile) {
            ProfileType.WOLF -> listOf("earth", "air")
            ProfileType.FROG -> listOf("earth", "water")
            ProfileType.PELICAN -> listOf("air", "water")
        }

        for (result in results) {
            val label = result.label.lowercase()
            val box: RectF = scaleBox(result.boundingBox, scaleX, scaleY)

            val info = StickerAssetMap.getStickerInfo(label)
            if (info != null && info.domains.any { it in allowedDomains }) {
                val sticker = StickerAssetMap.getBitmap(label)
                if (sticker != null) {
                    stickerOverlayManager.showSticker(label, box, sticker)
                } else {
                    Log.d("StickerPlacementManager", "No sticker bitmap for label: $label")
                }
            } else {
                Log.d("StickerPlacementManager", "Sticker $label skipped — not allowed for $currentProfile")
            }
        }
    }

}
