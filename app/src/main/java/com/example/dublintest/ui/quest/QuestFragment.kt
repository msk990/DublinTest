package com.example.dublintest.ui.quest

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.dublintest.R
import com.example.dublintest.stickers.model.StickerAssetMap
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class QuestFragment : Fragment() {

    private lateinit var rowEarth: LinearLayout
    private lateinit var rowAir: LinearLayout
    private lateinit var rowWater: LinearLayout
    private var questCompleted = false
    private var firestoreListener: ListenerRegistration? = null



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_quest, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rowEarth = view.findViewById(R.id.row_earth)
        rowAir = view.findViewById(R.id.row_air)
        rowWater = view.findViewById(R.id.row_water)

        listenToTeamProgress()

        // 👀 Game master hidden reset gesture
        view.findViewById<View>(R.id.quest_root).setOnLongClickListener {
            resetTeamDocument()
            true
        }
//        view.findViewById<Button>(R.id.testUpdateButton).setOnClickListener {
//            Firebase.firestore.collection("teams").document("current")
//                .update("progress.earth", FieldValue.arrayUnion("fork"))
//                .addOnSuccessListener {
//                    Log.d("QuestFragment", "✅ Test sticker added to EARTH!")
//                }
//                .addOnFailureListener { e ->
//                    Log.e("QuestFragment", "❌ Failed to add test sticker", e)
//                }
//        }

    }

    private fun listenToTeamProgress() {
        val db = Firebase.firestore
        firestoreListener = db.collection("teams").document("current")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener
                if (!isAdded || context == null) return@addSnapshotListener // 🛡️ Guard after fragment is gone

                val progress = snapshot.get("progress") as? Map<*, *> ?: return@addSnapshotListener

                val air = (progress["air"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                val earth = (progress["earth"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                val water = (progress["water"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()

                Log.d("QuestFragment", "Updating row: earth with ${earth.size} collected")

                updateRow(rowEarth, earth)
                updateRow(rowAir, air)
                updateRow(rowWater, water)

                if (!questCompleted && earth.size >= 2 && air.size >= 2 && water.size >= 2) {
                    questCompleted = true
                    showVictoryScreen()
                }
            }
    }

    private fun updateRow(row: LinearLayout, labels: List<String>) {
        row.removeAllViews()
        val max = 2

        for (i in 0 until max) {
            val imageView = ImageView(requireContext())
            val size = resources.getDimensionPixelSize(R.dimen.sticker_size)
            val lp = LinearLayout.LayoutParams(size, size).apply {
                marginEnd = 16
            }
            imageView.layoutParams = lp

            if (i < labels.size) {
                val label = labels[i]
                val bitmap = StickerAssetMap.getBitmap(label)
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap)
                } else {
                    imageView.setImageResource(R.drawable.placeholder_sticker)
                }
            } else {
                imageView.setImageResource(R.drawable.placeholder_sticker)
            }

            row.addView(imageView)
        }
    }

    private fun resetTeamDocument() {
        val db = Firebase.firestore
        val initialData = mapOf(
            "players" to listOf("wolf", "frog", "pelican"),
            "progress" to mapOf(
                "air" to emptyList<String>(),
                "earth" to emptyList<String>(),
                "water" to emptyList<String>()
            ),
            "startedAt" to FieldValue.serverTimestamp()
        )

        db.collection("teams").document("current")
            .set(initialData)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Team progress reset!", Toast.LENGTH_SHORT).show()
                Log.d("QuestFragment", "✅ Team document reset")
            }
            .addOnFailureListener { e ->
                Log.e("QuestFragment", "❌ Failed to reset team", e)
            }
    }

    private fun showVictoryScreen() {

        if (findNavController().currentDestination?.id == R.id.navigation_quest) {
            Log.d("QuestFragment", "🎉 Quest complete! Navigating to VictoryFragment")
            findNavController().navigate(R.id.navigation_victory)
        }
    }

}

