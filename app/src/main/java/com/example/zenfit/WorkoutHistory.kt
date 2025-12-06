package com.example.zenfit

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class WorkoutHistory : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var workoutHistoryRecyclerView: RecyclerView
    private lateinit var emptyState: TextView
    private lateinit var searchBar: EditText
    private lateinit var btnDelete: ImageButton
    private lateinit var historyAdapter: WorkoutHistoryAdapter
    private val historyList = mutableListOf<WorkoutHistoryItem>()
    private lateinit var cacheManager: CacheManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workout_history)

        sessionManager = SessionManager(this)
        cacheManager = CacheManager(this)

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener { finish() }

        workoutHistoryRecyclerView = findViewById(R.id.workoutHistoryRecyclerView)
        emptyState = findViewById(R.id.emptyState)
        searchBar = findViewById(R.id.searchBar)
        btnDelete = findViewById(R.id.btnDelete)

        setupRecyclerView()
        setupSearchBar()
        setupDeleteButton()
        loadCachedHistory()

        // Then try to fetch fresh data if online
        if (isNetworkAvailable()) {
            fetchWorkoutHistory()
        } else {
            Toast.makeText(this, "Offline mode - showing cached data", Toast.LENGTH_SHORT).show()
        }

        applyTheme()
    }
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    // Add method to load cached history
    private fun loadCachedHistory() {
        val cachedHistory = cacheManager.getCachedWorkoutHistory()
        if (cachedHistory.isNotEmpty()) {
            historyList.clear()
            historyList.addAll(cachedHistory)
            historyAdapter.updateWorkouts(historyList)
            emptyState.visibility = View.GONE
            workoutHistoryRecyclerView.visibility = View.VISIBLE
        }
    }

    private fun setupRecyclerView() {
        historyAdapter = WorkoutHistoryAdapter(historyList) { selectedIds ->
            btnDelete.visibility = if (selectedIds.isNotEmpty()) View.VISIBLE else View.GONE
        }
        workoutHistoryRecyclerView.adapter = historyAdapter
        workoutHistoryRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun setupSearchBar() {
        searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                historyAdapter.filter(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupDeleteButton() {
        btnDelete.setOnClickListener {
            val selectedIds = historyAdapter.getSelectedIds()
            if (selectedIds.isEmpty()) {
                Toast.makeText(this, "No items selected", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            AlertDialog.Builder(this)
                .setTitle("Delete History")
                .setMessage("Are you sure you want to delete ${selectedIds.size} item(s)?")
                .setPositiveButton("Delete") { _, _ ->
                    deleteSelectedItems(selectedIds)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun deleteSelectedItems(ids: List<Int>) {
        val url = ApiConfig.DELETE_WORKOUT_HISTORY_URL
        val idsString = ids.joinToString(",")

        val request = object : StringRequest(
            Request.Method.POST, url,
            { response ->
                try {
                    val json = JSONObject(response)
                    if (json.getBoolean("success")) {
                        Toast.makeText(this, "Deleted successfully", Toast.LENGTH_SHORT).show()
                        historyAdapter.clearSelection()
                        btnDelete.visibility = View.GONE
                        fetchWorkoutHistory()
                    } else {
                        Toast.makeText(this, "Failed to delete", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                error.printStackTrace()
                Toast.makeText(this, "Network error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams() = hashMapOf("ids" to idsString)
        }

        Volley.newRequestQueue(this).add(request)
    }
    private fun calculateCalories(reps: Int, sets: Int, weight: Int, duration: Int): Int {
        val weightCalories = (reps * sets * weight * 0.05).toInt()
        val durationCalories = (duration * 0.1).toInt()
        return weightCalories + durationCalories
    }
    private fun fetchWorkoutHistory() {
        val userId = sessionManager.getUserId() ?: ""
        val url = ApiConfig.GET_WORKOUT_HISTORY_URL

        val request = object : StringRequest(
            Method.POST, url,
            { response ->
                Log.d("WorkoutHistory", "Response: $response")

                if (response.isNullOrEmpty()) {
                    Toast.makeText(this, "Empty response from server", Toast.LENGTH_SHORT).show()
                    emptyState.visibility = View.VISIBLE
                    workoutHistoryRecyclerView.visibility = View.GONE
                } else {
                    try {
                        val json = JSONObject(response)
                        if (json.getBoolean("success")) {
                            val workoutsArray = json.getJSONArray("history")
                            historyList.clear()

                            for (i in 0 until workoutsArray.length()) {
                                val obj = workoutsArray.getJSONObject(i)
                                historyList.add(
                                    WorkoutHistoryItem(
                                        id = obj.getInt("id"),
                                        exerciseName = obj.getString("exercise_name"),
                                        reps = obj.getInt("reps"),
                                        sets = obj.getInt("sets"),
                                        weight = obj.getInt("weight"),
                                        duration = obj.getInt("duration"),
                                        restTime = obj.getInt("rest_time"),
                                        caloriesBurned = obj.optInt("calories_burned", 0),
                                        completedDate = obj.getLong("completed_date")
                                    )
                                )
                            }

                            // Cache the history
                            cacheManager.cacheWorkoutHistory(historyList)
                            historyAdapter.updateWorkouts(historyList)

                            if (historyList.isEmpty()) {
                                emptyState.visibility = View.VISIBLE
                                workoutHistoryRecyclerView.visibility = View.GONE
                            } else {
                                emptyState.visibility = View.GONE
                                workoutHistoryRecyclerView.visibility = View.VISIBLE
                            }
                        } else {
                            Toast.makeText(this, json.optString("message", "Failed to load history"), Toast.LENGTH_SHORT).show()
                            emptyState.visibility = View.VISIBLE
                            workoutHistoryRecyclerView.visibility = View.GONE
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        emptyState.visibility = View.VISIBLE
                        workoutHistoryRecyclerView.visibility = View.GONE
                    }
                }
            },
            { error ->
                error.printStackTrace()
                Toast.makeText(this, "Using cached data", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams() = hashMapOf("user_id" to userId)
        }

        Volley.newRequestQueue(this).add(request)
    }


    private fun applyTheme() {
        val prefs = getSharedPreferences("ThemePrefs", MODE_PRIVATE)
        val isDarkMode = prefs.getBoolean("isDarkMode", false)

        findViewById<android.widget.RelativeLayout>(R.id.main).setBackgroundResource(
            if (isDarkMode) R.drawable.zenfit_background
            else R.drawable.zenfit_background_light
        )
    }

    override fun onResume() {
        super.onResume()
        applyTheme()
    }

    override fun onBackPressed() {
        if (historyAdapter.isSelectionMode) {
            historyAdapter.clearSelection()
            btnDelete.visibility = View.GONE
        } else {
            super.onBackPressed()
        }
    }
}
