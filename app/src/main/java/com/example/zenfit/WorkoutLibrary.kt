package com.example.zenfit

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import kotlin.text.clear

class WorkoutLibrary : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var searchBox: EditText
    private lateinit var workoutCardsContainer: LinearLayout
    private val allWorkouts = mutableListOf<Workout>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workout_library)

        sessionManager = SessionManager(this)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        searchBox = findViewById(R.id.searchBox)
        workoutCardsContainer = findViewById(R.id.workoutCardsContainer)

        setupSearchBox()
        fetchWorkouts()
        applyTheme()
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

    private fun fetchWorkouts() {
        val userId = sessionManager.getUserId() ?: ""
        val url = ApiConfig.GET_WORKOUTS_URL

        val request = object : StringRequest(
            Request.Method.POST, url,
            { response ->
                try {
                    val json = JSONObject(response)
                    if (json.getBoolean("success")) {
                        val workoutsArray = json.getJSONArray("workouts")
                        allWorkouts.clear()

                        for (i in 0 until workoutsArray.length()) {
                            val obj = workoutsArray.getJSONObject(i)
                            allWorkouts.add(
                                Workout(
                                    name = obj.getString("name"),
                                    duration = obj.getInt("duration"),
                                    reps = obj.getInt("reps"),
                                    sets = obj.getInt("sets"),
                                    weight = obj.optInt("weight", 150),
                                    restTime = obj.optInt("rest_time", 60),
                                    exerciseCount = 1
                                )
                            )
                        }

                        displayWorkouts(allWorkouts)
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Toast.makeText(this, "Failed to fetch workouts: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams() = hashMapOf("user_id" to userId)
        }

        Volley.newRequestQueue(this).add(request)
    }


    private fun displayWorkouts(workouts: List<Workout>) {
        workoutCardsContainer.removeAllViews()

        workouts.forEach { workout ->
            val cardView = LayoutInflater.from(this).inflate(R.layout.item_workout_card, workoutCardsContainer, false) as RelativeLayout

            // Convert seconds to display format
            val durationText = formatDuration(workout.duration)

            cardView.findViewById<TextView>(R.id.workoutName).text = workout.name
            cardView.findViewById<TextView>(R.id.durationText).text = "Duration:\n$durationText"
            cardView.findViewById<TextView>(R.id.repsText).text = "Reps:\n${workout.reps}"
            cardView.findViewById<TextView>(R.id.setsText).text = "Sets:\n${workout.sets}"

            cardView.setOnClickListener { openWorkoutLogging(workout) }
            workoutCardsContainer.addView(cardView)
        }
    }

    private fun formatDuration(totalSeconds: Int): String {
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return when {
            minutes == 0 -> "${seconds}s"
            seconds == 0 -> "${minutes}min"
            else -> "${minutes}min ${seconds}s"
        }
    }


    private fun filterWorkouts(query: String) {
        val filtered = if (query.isEmpty()) {
            allWorkouts
        } else {
            allWorkouts.filter { it.name.contains(query, ignoreCase = true) }
        }
        displayWorkouts(filtered)
    }

    private fun openWorkoutLogging(workout: Workout) {
        val intent = Intent(this, WorkoutLogging::class.java).apply {
            putExtra("workout_name", workout.name)
            putExtra("workout_duration", workout.duration)
            putExtra("workout_reps", workout.reps)
            putExtra("workout_sets", workout.sets)
            putExtra("workout_weight", workout.weight)
            putExtra("workout_rest_time", workout.restTime)
            putExtra("workout_exercise_count", workout.exerciseCount)
        }
        startActivity(intent)
    }


    private fun applyTheme() {
        val prefs = getSharedPreferences("ThemePrefs", Context.MODE_PRIVATE)
        val isDarkMode = prefs.getBoolean("isDarkMode", false)

        findViewById<ScrollView>(R.id.main).setBackgroundResource(
            if (isDarkMode) R.drawable.zenfit_background else R.drawable.zenfit_background_light
        )
    }

    override fun onResume() {
        super.onResume()
        applyTheme()
        fetchWorkouts()
    }
}
