package com.example.zenfit

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity

class Home : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Top cards
        val settingsCard = findViewById<ImageView>(R.id.settingsCard)
        val moodCard = findViewById<ImageView>(R.id.moodCard)

        // Workout buttons
        val btnStartWorkout = findViewById<Button>(R.id.btnStartWorkout)
        val btnDetails = findViewById<Button>(R.id.btnDetails)
        val btnStartWorkoutMain = findViewById<ImageButton>(R.id.btnStartWorkoutMain)
        val btnMenu = findViewById<ImageButton>(R.id.btnMenu)

        // Bottom navigation
        val navHome = findViewById<ImageButton>(R.id.navHome)
        val navWorkout = findViewById<ImageButton>(R.id.navWorkout)
        val navAdd = findViewById<ImageButton>(R.id.navAdd)
        val navCalendar = findViewById<ImageButton>(R.id.navCalendar)
        val navProfile = findViewById<ImageButton>(R.id.navProfile)

        settingsCard.setOnClickListener {
            val intent = Intent(this, Settings::class.java)
            startActivity(intent)
        }

        moodCard.setOnClickListener {
            // Navigate to Mood Overview
            // TODO: Create MoodOverview activity
        }

        // Workout button listeners
        btnStartWorkout.setOnClickListener {
            // Start the workout
            // TODO: Create workout activity
        }

        btnDetails.setOnClickListener {
            // Show calories details
            // TODO: Create details activity
        }

        btnStartWorkoutMain.setOnClickListener {
            // Start a new workout
            // TODO: Create workout selection activity
        }

        btnMenu.setOnClickListener {
            val intent = Intent(this, Menu::class.java)
            startActivity(intent)
        }

        // Bottom navigation listeners
        navHome.setOnClickListener {
            // Already on home
        }

        navWorkout.setOnClickListener {
            // Navigate to Workout
            // TODO: Create Workout activity
        }

        navAdd.setOnClickListener {
            // Add new workout
            // TODO: Create add workout activity
        }

        navCalendar.setOnClickListener {
            // Navigate to Calendar
            // TODO: Create Calendar activity
        }

        navProfile.setOnClickListener {
            // Navigate to Profile
            // TODO: Create Profile activity
        }
    }
    private fun applyTheme() {
        try {
            val prefs = getSharedPreferences("ThemePrefs", Context.MODE_PRIVATE)
            val isDarkMode = prefs.getBoolean("isDarkMode", false)

            val rootLayout = findViewById<RelativeLayout>(R.id.main)
            if (isDarkMode) {
                rootLayout.setBackgroundResource(R.drawable.zenfit_background)
            } else {
                rootLayout.setBackgroundResource(R.drawable.zenfit_background_light)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        applyTheme()
    }
}
