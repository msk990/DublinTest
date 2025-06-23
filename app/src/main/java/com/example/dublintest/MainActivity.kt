package com.example.dublintest

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.dublintest.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

// 🧪 Firebase imports
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.auth.ktx.auth
import android.view.View


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate view
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        // Setup NavController
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        val navController = navHostFragment.navController
        navController.addOnDestinationChangedListener { _, destination, _ ->
            val showBottomNav = when (destination.id) {
                R.id.navigation_victory -> false
                else -> true
            }
            binding.navView.visibility = if (showBottomNav) View.VISIBLE else View.GONE
        }


        val navOptions = NavOptions.Builder()
            .setLaunchSingleTop(true)
            .setPopUpTo(navController.graph.startDestinationId, false)
            .build()

        // Define bottom nav destinations
        val bottomNavDestinations = setOf(
            R.id.navigation_profile,

            R.id.navigation_camera,
            R.id.navigation_quest,
//            R.id.navigation_story,
//            R.id.navigation_survey
        )

        val appBarConfiguration = AppBarConfiguration(bottomNavDestinations)
        navView.setupWithNavController(navController)

        // ✅ Firebase test logic
        testFirebaseConnection()
    }

    private fun testFirebaseConnection() {
        val db = Firebase.firestore

        val testData = hashMapOf(
            "timestamp" to System.currentTimeMillis(),
            "status" to "connected"
        )

        db.collection("debug_connection")
            .add(testData)
            .addOnSuccessListener {
                Log.d("FirebaseTest", "✅ Firestore write successful!")
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseTest", "❌ Firestore write failed", e)
            }

        // Optional: sign in anonymously
        Firebase.auth.signInAnonymously()
            .addOnSuccessListener {
                Log.d("FirebaseTest", "Signed in anonymously as ${it.user?.uid}")
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseTest", "Auth failed", e)
            }
    }
}
