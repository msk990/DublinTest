package com.example.dublintest.stickers.ui

import android.app.AlertDialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.RectF
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.example.dublintest.R

import com.example.dublintest.stickers.model.StickerAssetMap
import com.example.dublintest.stickers.model.StickerType
import com.example.dublintest.storage.StickerRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.models.Size
import nl.dionsegijn.konfetti.core.emitter.Emitter
import nl.dionsegijn.konfetti.xml.KonfettiView
import java.util.concurrent.TimeUnit

import com.bumptech.glide.Glide
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import androidx.navigation.findNavController




class StickerOverlayManager(
    private val container: FrameLayout,
    private val appContext: Context,
    private val konfettiView: KonfettiView,
    private val takePhotoButton: Button,
    private val coroutineScope: CoroutineScope
) {

    fun clear() {
        container.removeAllViews()
        takePhotoButton.visibility = View.GONE
        takePhotoButton.isEnabled = true
    }

    fun showSticker(label: String, box: RectF, sticker: Bitmap) {
        when (StickerAssetMap.getStickerInfo(label)?.type) {
            StickerType.COLLECTIBLE -> addCollectibleSticker(label, box, sticker)
            StickerType.INTERACTIVE -> addInteractiveSticker(label, box, sticker)
            else -> addInteractiveSticker(label, box, sticker)
        }
    }

    private fun addCollectibleSticker(label: String, box: RectF, sticker: Bitmap) {
//        val imageView = ImageView(appContext)
//        imageView.isClickable = true
//        imageView.isFocusable = true
//        imageView.scaleType = ImageView.ScaleType.FIT_CENTER
//        imageView.setPadding(16, 16, 16, 16)
//        imageView.setBackgroundColor(0x00000000)
        val imageView = ImageView(appContext).apply {
            isClickable = true
            isFocusable = true
            scaleType = ImageView.ScaleType.FIT_CENTER
            setPadding(0, 0, 0, 0) // 👈 removes any invisible dead zone
            setBackgroundColor(0x01000000) // 👈 1% alpha: transparent but catches taps
            contentDescription = label
        }


// 🔁 Load the sticker (GIF or PNG)
        loadStickerImage(label, imageView)

        val size = 1028 // adjust if you want larger/smaller stickers

        imageView.layoutParams = FrameLayout.LayoutParams(size, size).apply {
            leftMargin = (box.centerX() - size / 2).toInt()
            topMargin = (box.centerY() - size / 2).toInt()
        }

        imageView.contentDescription = label

        imageView.setOnClickListener {
            val description = StickerAssetMap.getStickerInfo(label)?.description
                ?: "You found a $label sticker!"

            showCollectibleDialog(label, description, null) {
               // collectToFirestore(label)

                konfettiView.start(
                    Party(
                        speed = 30f,
                        maxSpeed = 50f,
                        damping = 0.9f,
                        spread = 360,
                        size = listOf(Size(12), Size(24)),
                        emitter = Emitter(duration = 300, TimeUnit.MILLISECONDS).perSecond(200),
                        colors = listOf(0xFFE91E63.toInt(), 0xFFFFC107.toInt(), 0xFF00BCD4.toInt()),
                        position = Position.Relative(0.5, 0.5)
                    )
                )

                container.removeView(imageView)

                // 🧭 Navigate to QuestFragment
                switchBottomNavToQuest()

            }

        }

        container.addView(imageView)

//        container.addView(imageView)
    }

    private fun addInteractiveSticker(label: String, box: RectF, sticker: Bitmap) {
        val imageView = ImageView(appContext).apply {
            setImageBitmap(sticker)
            layoutParams = FrameLayout.LayoutParams(sticker.width, sticker.height).apply {
                leftMargin = (box.centerX() - sticker.width / 2).toInt()
                topMargin = (box.centerY() - sticker.height / 2).toInt()
            }
            contentDescription = label
        }

        takePhotoButton.setOnClickListener {
            takePhotoButton.isEnabled = false
            val loader = ProgressBar(appContext).apply {
                layoutParams = FrameLayout.LayoutParams(150, 150).apply {
                    gravity = Gravity.CENTER
                }
            }
            container.addView(loader)

            Handler(Looper.getMainLooper()).postDelayed({
                container.removeView(loader)
                showPhotoRewardDialog(label, sticker) {
                    konfettiView.start(
                        Party(
                            speed = 30f,
                            maxSpeed = 50f,
                            damping = 0.9f,
                            spread = 360,
                            size = listOf(Size(12), Size(24)),
                            emitter = Emitter(duration = 300, TimeUnit.MILLISECONDS).perSecond(200),
                            colors = listOf(0xFFE91E63.toInt(), 0xFFFFC107.toInt(), 0xFF00BCD4.toInt()),
                            position = Position.Relative(0.5, 0.5)
                        )
                    )
                    container.removeView(imageView)
                    takePhotoButton.visibility = View.GONE
                    takePhotoButton.isEnabled = true
                }
            }, 1000)
        }

        takePhotoButton.visibility = View.VISIBLE
        takePhotoButton.bringToFront()
        container.addView(imageView)
    }

    private fun showCollectibleDialog(
        label: String,
        description: String,
        onCollect1: Nothing?,
        onCollect: () -> Unit
    )
    {
        val dialogView = LayoutInflater.from(appContext).inflate(R.layout.dialog_collectible_sticker, null)

        val dialog = AlertDialog.Builder(appContext)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        val imageView = dialogView.findViewById<ImageView>(R.id.stickerImage)
        loadStickerImage(label, imageView)

        dialogView.findViewById<TextView>(R.id.stickerTitle).text = label
        dialogView.findViewById<TextView>(R.id.stickerDescription).text = description

        dialogView.findViewById<Button>(R.id.collectButton).setOnClickListener {
            coroutineScope.launch {
//                StickerRepository.collect(label)
                collectToFirestore(label)
            }

            onCollect()
            dialog.dismiss()
        }


        dialog.show()
    }

    private fun showPhotoRewardDialog(label: String, sticker: Bitmap, onClose: () -> Unit) {
        val dialogView = LayoutInflater.from(appContext).inflate(R.layout.dialog_photo_reward, null)

        val dialog = AlertDialog.Builder(appContext)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialogView.findViewById<ImageView>(R.id.rewardStickerImage).setImageBitmap(sticker)
        dialogView.findViewById<Button>(R.id.rewardCloseButton).setOnClickListener {
            coroutineScope.launch {
                Log.d("Overlay", "Adding 10 points...")
                StickerRepository.addPoints(10)

//                walletViewModel.incrementPoints(10)

            }
            onClose()
            dialog.dismiss()

        }

        dialog.show()
    }
    private fun loadStickerImage(label: String, imageView: ImageView) {
        val info = StickerAssetMap.getStickerInfo(label)
        val assetPath = info?.image ?: return

        val fullPath = "file:///android_asset/$assetPath"
        val isGif = assetPath.endsWith(".gif", ignoreCase = true)

        val glide = Glide.with(imageView)

        if (isGif) {
            glide.asGif()
                .load(fullPath)
                .into(imageView)
        } else {
            glide.asBitmap()
                .load(fullPath)
                .dontAnimate()
                .into(imageView)
        }
    }
    private fun collectToFirestore(label: String) {
        val info = StickerAssetMap.getStickerInfo(label) ?: return
        val db = Firebase.firestore

        info.domains.forEach { domain ->
            db.collection("teams")
                .document("current")
                .update("progress.$domain", FieldValue.arrayUnion(label))
                .addOnSuccessListener {
                    Log.d("CameraFragment", "✅ Added '$label' to $domain")
                }
                .addOnFailureListener { e ->
                    Log.e("CameraFragment", "❌ Failed to add '$label' to $domain", e)
                }
        }
    }
    private fun switchBottomNavToQuest() {
        val navView = (container.rootView.findViewById<View>(R.id.nav_view) as? com.google.android.material.bottomnavigation.BottomNavigationView)
        navView?.selectedItemId = R.id.navigation_quest
    }

}
