package com.example.zenfit

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class Menu : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        // Back arrow
        findViewById<ImageView>(R.id.backArrow).setOnClickListener {
            finish()
        }

        // Meal Logging
        findViewById<ImageView>(R.id.btnMealLogging).setOnClickListener {
            // TODO: Navigate to Meal Logging
        }

        // Meal Photo
        findViewById<ImageView>(R.id.btnMealPhoto).setOnClickListener {
            // TODO: Navigate to Meal Photo
        }

        // Food Library
        findViewById<ImageView>(R.id.btnFoodLibrary).setOnClickListener {
            // TODO: Navigate to Food Library
        }

        // Meal History
        findViewById<ImageView>(R.id.btnMealHistory).setOnClickListener {
            // TODO: Navigate to Meal History
        }

        // Workout Library
        findViewById<ImageView>(R.id.btnWorkoutLibrary).setOnClickListener {
            // TODO: Navigate to Workout Library
        }

        // Workout History
        findViewById<ImageView>(R.id.btnWorkoutHistory).setOnClickListener {
            // TODO: Navigate to Workout History
        }

        // Create Workout
        findViewById<ImageView>(R.id.btnCreateWorkout).setOnClickListener {
            // TODO: Navigate to Create Workout
        }

        // Notifications
        findViewById<ImageView>(R.id.btnNotifications).setOnClickListener {
            // TODO: Navigate to Notifications
        }
    }
}
