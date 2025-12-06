package com.example.zenfit

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class WorkoutLibrary : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var searchBox: EditText
    private lateinit var workoutRecyclerView: RecyclerView
    private lateinit var btnDelete: ImageButton
    private lateinit var workoutAdapter: WorkoutLibraryAdapter
    private val allWorkouts = mutableListOf<Workout>()
    private lateinit var cacheManager: CacheManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workout_library)

        sessionManager = SessionManager(this)
        cacheManager = CacheManager(this)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        searchBox = findViewById(R.id.searchBox)
        btnDelete = findViewById(R.id.btnDelete)

        // Convert LinearLayout to RecyclerView in layout
        val workoutCardsContainer = findViewById<LinearLayout>(R.id.workoutCardsContainer)
        workoutCardsContainer.removeAllViews()

        workoutRecyclerView = RecyclerView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }
        workoutCardsContainer.addView(workoutRecyclerView)

        setupRecyclerView()
        setupSearchBox()
        setupDeleteButton()
        setupBackPress()

        loadCachedWorkouts()
        if (isNetworkAvailable()) {
            fetchWorkouts()
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

    // Add method to load cached workouts
    private fun loadCachedWorkouts() {
        val cachedWorkouts = cacheManager.getCachedWorkouts()
        if (cachedWorkouts.isNotEmpty()) {
            allWorkouts.clear()
            allWorkouts.addAll(cachedWorkouts)
            workoutAdapter.updateWorkouts(allWorkouts)
        }
    }
    private fun setupRecyclerView() {
        workoutAdapter = WorkoutLibraryAdapter(
            allWorkouts,
            onWorkoutClick = { workout -> openWorkoutLogging(workout) },
            onSelectionChanged = { selectedIds ->
                btnDelete.visibility = if (selectedIds.isNotEmpty()) View.VISIBLE else View.GONE
            }
        )
        workoutRecyclerView.adapter = workoutAdapter
        workoutRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun setupSearchBox() {
        searchBox.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                workoutAdapter.filter(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupDeleteButton() {
        btnDelete.setOnClickListener {
            val selectedIds = workoutAdapter.getSelectedIds()
            if (selectedIds.isEmpty()) {
                Toast.makeText(this, "No items selected", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            AlertDialog.Builder(this)
                .setTitle("Delete Workouts")
                .setMessage("Are you sure you want to delete ${selectedIds.size} workout(s)?")
                .setPositiveButton("Delete") { _, _ ->
                    deleteSelectedWorkouts(selectedIds)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun setupBackPress() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (workoutAdapter.isSelectionMode) {
                    workoutAdapter.clearSelection()
                    btnDelete.visibility = View.GONE
                } else {
                    finish()
                }
            }
        })
    }

    private fun deleteSelectedWorkouts(ids: List<Int>) {
        val url = ApiConfig.DELETE_WORKOUT_URL
        val idsString = ids.joinToString(",")

        val request = object : StringRequest(
            Request.Method.POST, url,
            { response ->
                try {
                    val json = JSONObject(response)
                    if (json.getBoolean("success")) {
                        Toast.makeText(this, "Deleted successfully", Toast.LENGTH_SHORT).show()
                        workoutAdapter.clearSelection()
                        btnDelete.visibility = View.GONE
                        fetchWorkouts()
                    } else {
                        Toast.makeText(this, "Failed to delete", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Toast.makeText(this, "Network error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams() = hashMapOf("ids" to idsString)
        }

        Volley.newRequestQueue(this).add(request)
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
                                    id = obj.getInt("id"),
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

                        // Cache the workouts
                        cacheManager.cacheWorkouts(allWorkouts)
                        workoutAdapter.updateWorkouts(allWorkouts)
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                // On network error, use cached data
                Toast.makeText(this, "Using cached data", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams() = hashMapOf("user_id" to userId)
        }

        Volley.newRequestQueue(this).add(request)
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
