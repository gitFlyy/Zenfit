package com.example.zenfit

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import kotlin.toString

class CreateWorkout : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private var repsCount = 7
    private var setsCount = 10
    private var weightCount = 150
    private var restCount = 80
    private var selectedDuration = 30

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_workout)

        sessionManager = SessionManager(this)

        setupViews()
        setupListeners()
        applyTheme()
    }

    private fun setupViews() {
        val durationSpinner = findViewById<Spinner>(R.id.spinnerDuration)

        // Generate durations from 30 seconds to 60 minutes with 30-second intervals
        val durations = mutableListOf<String>()
        for (i in 1..120) { // 120 * 30 seconds = 60 minutes
            val totalSeconds = i * 15
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60

            durations.add(when {
                minutes == 0 -> "${seconds}s"
                seconds == 0 -> "${minutes}min"
                else -> "${minutes}min ${seconds}s"
            })
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, durations)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        durationSpinner.adapter = adapter
        durationSpinner.setSelection(59) // Default to 30 minutes (position 59)

        durationSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // Calculate total seconds: (position + 1) * 30
                selectedDuration = (position + 1) * 30
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }


    private fun setupListeners() {
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        // Reps controls
        findViewById<ImageButton>(R.id.btnRepsUp).setOnClickListener {
            repsCount++
            updateRepsDisplay()
        }
        findViewById<ImageButton>(R.id.btnRepsDown).setOnClickListener {
            if (repsCount > 0) {
                repsCount--
                updateRepsDisplay()
            }
        }

        // Sets controls
        findViewById<ImageButton>(R.id.btnSetsUp).setOnClickListener {
            setsCount++
            updateSetsDisplay()
        }
        findViewById<ImageButton>(R.id.btnSetsDown).setOnClickListener {
            if (setsCount > 0) {
                setsCount--
                updateSetsDisplay()
            }
        }

        // Weight controls
        findViewById<ImageButton>(R.id.btnWeightUp).setOnClickListener {
            weightCount += 5
            updateWeightDisplay()
        }
        findViewById<ImageButton>(R.id.btnWeightDown).setOnClickListener {
            if (weightCount > 0) {
                weightCount -= 5
                updateWeightDisplay()
            }
        }

        // Rest controls
        findViewById<ImageButton>(R.id.btnRestUp).setOnClickListener {
            restCount += 10
            updateRestDisplay()
        }
        findViewById<ImageButton>(R.id.btnRestDown).setOnClickListener {
            if (restCount > 0) {
                restCount -= 10
                updateRestDisplay()
            }
        }

        findViewById<Button>(R.id.btnCreateWorkout).setOnClickListener {
            createWorkout()
        }
    }

    private fun updateRepsDisplay() {
        findViewById<TextView>(R.id.repsValue).text = "$repsCount reps"
    }

    private fun updateSetsDisplay() {
        findViewById<TextView>(R.id.setsValue).text = "$setsCount sets"
    }

    private fun updateWeightDisplay() {
        findViewById<TextView>(R.id.weightValue).text = "$weightCount lbs"
    }

    private fun updateRestDisplay() {
        findViewById<TextView>(R.id.restValue).text = "${restCount}s"
    }

    private fun createWorkout() {
        val name = findViewById<EditText>(R.id.editExerciseName).text.toString()
        val duration = selectedDuration
        val reps = repsCount
        val sets = setsCount
        val weight = weightCount
        val restTime = restCount

        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter a workout name", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = sessionManager.getUserId() ?: ""
        val url = ApiConfig.CREATE_WORKOUT_URL

        val request = object : StringRequest(
            Request.Method.POST, url,
            { response ->
                try {
                    val json = JSONObject(response)
                    if (json.getBoolean("success")) {
                        Toast.makeText(this, "Workout created successfully!", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this, json.getString("message"), Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Toast.makeText(this, "Failed to create workout: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["user_id"] = userId
                params["name"] = name
                params["duration"] = duration.toString()
                params["reps"] = reps.toString()
                params["sets"] = sets.toString()
                params["weight"] = weight.toString()
                params["rest_time"] = restTime.toString()
                return params
            }
        }

        Volley.newRequestQueue(this).add(request)
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
