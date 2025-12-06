package com.example.zenfit

import android.content.Context
import android.os.Bundle
import android.view.View
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
import java.util.*
import kotlin.collections.plusAssign
import kotlin.printStackTrace
import kotlin.ranges.rangeTo
import kotlin.text.clear
import kotlin.toString

class CaloriesActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var calorieValue: TextView
    private lateinit var editCalories: EditText
    private lateinit var btnAddCalories: ImageButton
    private lateinit var workoutHistoryRecyclerView: RecyclerView
    private lateinit var emptyState: TextView
    private lateinit var historyAdapter: WorkoutHistoryAdapter
    private val workoutHistoryList = mutableListOf<WorkoutHistoryItem>()
    private var totalCalories = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calories)

        sessionManager = SessionManager(this)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        calorieValue = findViewById(R.id.calorieValue)
        editCalories = findViewById(R.id.editCalories)
        btnAddCalories = findViewById(R.id.btnAddCalories)
        workoutHistoryRecyclerView = findViewById(R.id.workoutHistoryRecyclerView)
        emptyState = findViewById(R.id.emptyState)

        setupRecyclerView()

        btnAddCalories.setOnClickListener { addManualCalories() }

        fetchTodayWorkoutHistory()
        applyTheme()
    }

    private fun setupRecyclerView() {
        historyAdapter = WorkoutHistoryAdapter(workoutHistoryList) { }
        workoutHistoryRecyclerView.adapter = historyAdapter
        workoutHistoryRecyclerView.layoutManager = LinearLayoutManager(this)
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

    private fun fetchTodayWorkoutHistory() {
        val userId = sessionManager.getUserId() ?: ""
        val url = ApiConfig.GET_WORKOUT_HISTORY_URL

        val request = object : StringRequest(
            Request.Method.POST, url,
            { response ->
                try {
                    val json = JSONObject(response)
                    if (json.getBoolean("success")) {
                        val workoutsArray = json.getJSONArray("history")
                        workoutHistoryList.clear()
                        totalCalories = 0

                        val todayStart = getTodayStartTimestamp()
                        val todayEnd = getTodayEndTimestamp()

                        for (i in 0 until workoutsArray.length()) {
                            val obj = workoutsArray.getJSONObject(i)
                            val completedDate = obj.getLong("completed_date")

                            if (completedDate in todayStart..todayEnd) {
                                val caloriesBurned = obj.optInt("calories_burned", 0)
                                totalCalories += caloriesBurned

                                workoutHistoryList.add(
                                    WorkoutHistoryItem(
                                        id = obj.getInt("id"),
                                        exerciseName = obj.getString("exercise_name"),
                                        reps = obj.getInt("reps"),
                                        sets = obj.getInt("sets"),
                                        weight = obj.getInt("weight"),
                                        duration = obj.getInt("duration"),
                                        restTime = obj.getInt("rest_time"),
                                        caloriesBurned = caloriesBurned,
                                        completedDate = completedDate
                                    )
                                )
                            }
                        }

                        calorieValue.text = totalCalories.toString()
                        historyAdapter.updateWorkouts(workoutHistoryList)

                        if (workoutHistoryList.isEmpty()) {
                            emptyState.visibility = View.VISIBLE
                            workoutHistoryRecyclerView.visibility = View.GONE
                        } else {
                            emptyState.visibility = View.GONE
                            workoutHistoryRecyclerView.visibility = View.VISIBLE
                        }
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


    private fun getTodayStartTimestamp(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun getTodayEndTimestamp(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.timeInMillis
    }

    private fun applyTheme() {
        val prefs = getSharedPreferences("ThemePrefs", Context.MODE_PRIVATE)
        val isDarkMode = prefs.getBoolean("isDarkMode", false)

        findViewById<ScrollView>(R.id.main).setBackgroundResource(
            if (isDarkMode) R.drawable.zenfit_background
            else R.drawable.zenfit_background_light
        )
    }


    override fun onResume() {
        super.onResume()
        applyTheme()
        fetchTodayWorkoutHistory()
    }
}
