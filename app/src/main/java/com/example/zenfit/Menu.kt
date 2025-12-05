package com.example.zenfit

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.RelativeLayout
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
            val intent= Intent(this, MealLogging::class.java)
            startActivity(intent)
        }

        // Meal Photo
        findViewById<ImageView>(R.id.btnMealPhoto).setOnClickListener {
            val intent= Intent(this, MealUpload::class.java)
            startActivity(intent)
        }

        // Food Library
        findViewById<ImageView>(R.id.btnFoodLibrary).setOnClickListener {
            val intent= Intent(this, MealIdeas::class.java)
            startActivity(intent)
        }

        // Meal History
        findViewById<ImageView>(R.id.btnMealHistory).setOnClickListener {
            val intent= Intent(this, MealHistory::class.java)
            startActivity(intent)
        }

        // Workout Library
        findViewById<ImageView>(R.id.btnWorkoutLibrary).setOnClickListener {
            val intent= Intent(this, WorkoutLibrary::class.java)
            startActivity(intent)
        }

        // Workout History
        findViewById<ImageView>(R.id.btnWorkoutHistory).setOnClickListener {
            val intent= Intent(this, WorkoutHistory::class.java)
            startActivity(intent)
        }

        // Create Workout
        findViewById<ImageView>(R.id.btnCreateWorkout).setOnClickListener {
            val intent= Intent(this, CreateWorkout::class.java)
            startActivity(intent)
        }

        // Notifications
        findViewById<ImageView>(R.id.btnNotifications).setOnClickListener {
            val intent= Intent(this, NotificationsActivity::class.java)
            startActivity(intent)
        }
    }

    private fun applyTheme() {
        val prefs = getSharedPreferences("ThemePrefs", Context.MODE_PRIVATE)
        val isDarkMode = prefs.getBoolean("isDarkMode", false)

        val rootLayout = findViewById<RelativeLayout>(R.id.main)
        if (isDarkMode) {
            rootLayout.setBackgroundResource(R.drawable.zenfit_background)
        } else {
            rootLayout.setBackgroundResource(R.drawable.zenfit_background_light)
        }
    }

    override fun onResume() {
        super.onResume()
        applyTheme()
    }
}
