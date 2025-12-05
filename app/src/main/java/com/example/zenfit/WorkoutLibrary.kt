package com.example.zenfit

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.ScrollView
import androidx.appcompat.app.AppCompatActivity

class WorkoutLibrary : AppCompatActivity() {

    private lateinit var searchBox: EditText
    private lateinit var workoutCardsContainer: LinearLayout
    private val workoutCards = mutableListOf<Pair<RelativeLayout, String>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workout_library)

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish()
        }

        searchBox = findViewById(R.id.searchBox)
        workoutCardsContainer = findViewById(R.id.workoutCardsContainer)

        setupWorkoutCards()
        setupSearchBox()
        applyTheme()
    }

    private fun setupWorkoutCards() {
        // Store references to all workout cards with their names
        workoutCards.add(Pair(findViewById(R.id.workoutCard1), "Push ups"))
        workoutCards.add(Pair(findViewById(R.id.workoutCard2), "Squats"))
        workoutCards.add(Pair(findViewById(R.id.workoutCard3), "Pull ups"))
        workoutCards.add(Pair(findViewById(R.id.workoutCard4), "Weight Lifting"))
        workoutCards.add(Pair(findViewById(R.id.workoutCard5), "Sprints"))
        workoutCards.add(Pair(findViewById(R.id.workoutCard6), "Deadlifts"))
    }

    private fun setupSearchBox() {
        searchBox.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterWorkouts(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filterWorkouts(query: String) {
        for ((card, workoutName) in workoutCards) {
            if (query.isEmpty() || workoutName.contains(query, ignoreCase = true)) {
                card.visibility = View.VISIBLE
            } else {
                card.visibility = View.GONE
            }
        }
    }

    private fun applyTheme() {
        val prefs = getSharedPreferences("ThemePrefs", Context.MODE_PRIVATE)
        val isDarkMode = prefs.getBoolean("isDarkMode", false)

        val rootLayout = findViewById<ScrollView>(R.id.main)
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
