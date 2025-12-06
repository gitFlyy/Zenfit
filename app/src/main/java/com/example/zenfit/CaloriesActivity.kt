package com.example.zenfit

import android.content.Context
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class CaloriesActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var calorieValue: TextView
    private lateinit var editCalories: EditText
    private lateinit var btnAddCalories: ImageButton
    private val workoutHistoryList = mutableListOf<WorkoutHistoryItem>()
    private var totalCalories = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calories)

        sessionManager = SessionManager(this)

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish()
        }

        calorieValue = findViewById(R.id.calorieValue)
        editCalories = findViewById(R.id.editCalories)
        btnAddCalories = findViewById(R.id.btnAddCalories)

        btnAddCalories.setOnClickListener {
            addManualCalories()
        }

        fetchWorkoutHistory()
        applyTheme()
    }

    private fun addManualCalories() {
        val calories = editCalories.text.toString().toIntOrNull()
        if (calories != null && calories > 0) {
            totalCalories += calories
            calorieValue.text = totalCalories.toString()
            editCalories.text.clear()
            Toast.makeText(this, "Added $calories calories", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Enter valid calories", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchWorkoutHistory() {
        val userId = sessionManager.getUserId() ?: ""
        val url = ApiConfig.GET_WORKOUT_HISTORY_URL

        val request = object : StringRequest(
            Request.Method.POST, url,
            { response ->
                try {
                    val json = JSONObject(response)
                    if (json.getBoolean("success")) {
                        val workoutsArray = json.getJSONArray("workouts")
                        workoutHistoryList.clear()
                        totalCalories = 0

                        for (i in 0 until workoutsArray.length()) {
                            val obj = workoutsArray.getJSONObject(i)
                            val calories = calculateCalories(
                                obj.getInt("reps"),
                                obj.getInt("sets"),
                                obj.getInt("weight"),
                                obj.getInt("duration")
                            )
                            totalCalories += calories

                            workoutHistoryList.add(
                                WorkoutHistoryItem(
                                    id = obj.getInt("id"),
                                    exerciseName = obj.getString("exercise_name"),
                                    reps = obj.getInt("reps"),
                                    sets = obj.getInt("sets"),
                                    weight = obj.getInt("weight"),
                                    duration = obj.getInt("duration"),
                                    restTime = obj.getInt("rest_time"),
                                    calories = calories,
                                    completedDate = obj.getLong("completed_date")
                                )
                            )
                        }

                        calorieValue.text = totalCalories.toString()
                        updateWorkoutCards()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "Error loading workout history", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                error.printStackTrace()
                Toast.makeText(this, "Failed to fetch workout history", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams() = hashMapOf("user_id" to userId)
        }

        Volley.newRequestQueue(this).add(request)
    }

    private fun calculateCalories(reps: Int, sets: Int, weight: Int, duration: Int): Int {
        // Formula: (reps × sets × weight × 0.05) + (duration × 0.1)
        val weightCalories = (reps * sets * weight * 0.05).toInt()
        val durationCalories = (duration * 0.1).toInt()
        return weightCalories + durationCalories
    }

    private fun updateWorkoutCards() {
        // Group by day
        val today = Calendar.getInstance()
        val todayWorkouts = mutableListOf<WorkoutHistoryItem>()
        val yesterdayWorkouts = mutableListOf<WorkoutHistoryItem>()
        val olderWorkouts = mutableListOf<WorkoutHistoryItem>()

        for (workout in workoutHistoryList) {
            val workoutCal = Calendar.getInstance()
            workoutCal.timeInMillis = workout.completedDate

            val daysDiff = ((today.timeInMillis - workout.completedDate) / (1000 * 60 * 60 * 24)).toInt()

            when (daysDiff) {
                0 -> todayWorkouts.add(workout)
                1 -> yesterdayWorkouts.add(workout)
                else -> olderWorkouts.add(workout)
            }
        }

        // Update first card (Today)
        if (todayWorkouts.isNotEmpty()) {
            val todayTotal = todayWorkouts.sumOf { it.calories }
            val totalReps = todayWorkouts.sumOf { it.reps * it.sets }

            findViewById<TextView>(R.id.calorieValue).text = todayTotal.toString()
            findViewById<TextView>(R.id.exerciseName1).text = todayWorkouts.firstOrNull()?.exerciseName ?: "Push ups"
        }

        // Update second card (Yesterday/Tuesday)
        if (yesterdayWorkouts.isNotEmpty()) {
            val yesterdayTotal = yesterdayWorkouts.sumOf { it.calories }
            findViewById<TextView>(R.id.exerciseName2).text = yesterdayWorkouts.firstOrNull()?.exerciseName ?: "Squats"
        }

        // Update third card (Older)
        if (olderWorkouts.isNotEmpty()) {
            val olderTotal = olderWorkouts.sumOf { it.calories }
            findViewById<TextView>(R.id.exerciseName3).text = olderWorkouts.firstOrNull()?.exerciseName ?: "Deadlift"
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
        fetchWorkoutHistory()
    }
}


