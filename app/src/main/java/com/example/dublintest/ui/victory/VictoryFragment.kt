package com.example.dublintest.ui.victory

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.dublintest.R
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import nl.dionsegijn.konfetti.core.models.Size
import nl.dionsegijn.konfetti.xml.KonfettiView
import java.util.concurrent.TimeUnit

class VictoryFragment : Fragment() {

    private var firestoreListener: ListenerRegistration? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_victory, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val konfettiView = view.findViewById<KonfettiView>(R.id.victoryKonfetti)
        val victoryText = view.findViewById<TextView>(R.id.victoryText)
        val victoryEmoji = view.findViewById<TextView>(R.id.victoryEmoji)
//        val returnButton = view.findViewById<Button>(R.id.returnButton)

        view.findViewById<View>(R.id.victory_root).setOnLongClickListener {
            resetTeamDocument()
            true
        }


        // Start confetti party
        konfettiView.start(
            Party(
                speed = 0f,
                maxSpeed = 30f,
                damping = 0.9f,
                spread = 360,
                size = listOf(Size.SMALL, Size.LARGE),
                emitter = Emitter(duration = 3, TimeUnit.SECONDS).perSecond(200),
                position = Position.Relative(0.5, 0.3)
            )
        )

        // Animate text
        victoryText.animate().alpha(1f).setDuration(1000).start()
        victoryEmoji.animate().alpha(1f).setDuration(1000).start()

        // Return to profile or home
//        returnButton.setOnClickListener {
//            findNavController().popBackStack(R.id.navigation_profile, false)
//        }
        val db = Firebase.firestore
        firestoreListener = db.collection("teams").document("current")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener
                if (!isAdded) return@addSnapshotListener

                val progress = snapshot.get("progress") as? Map<*, *> ?: return@addSnapshotListener

                val air = (progress["air"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                val earth = (progress["earth"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                val water = (progress["water"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()

                // if reset = go back to Quest
                if (air.isEmpty() && earth.isEmpty() && water.isEmpty()) {
                    findNavController().popBackStack(R.id.navigation_quest, false)
                }
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
}
