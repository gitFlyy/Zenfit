package com.example.zenfit

import android.graphics.Color
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class MoodScreen : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private var selectedMood: Pair<LinearLayout, String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mood_overview)

        sessionManager = SessionManager(this)

        val moodVeryBad = findViewById<LinearLayout>(R.id.moodVeryBad)
        val moodBad = findViewById<LinearLayout>(R.id.moodBad)
        val moodFine = findViewById<LinearLayout>(R.id.moodFine)
        val moodGood = findViewById<LinearLayout>(R.id.moodGood)
        val moodVeryGood = findViewById<LinearLayout>(R.id.moodVeryGood)
        val sleepHoursInput = findViewById<EditText>(R.id.HoursSlept)
        val sleepQualitySpinner = findViewById<Spinner>(R.id.spinnerSleepQuality)
        val saveButton = findViewById<Button>(R.id.btnSaveChanges)

        val sleepQualities = arrayOf(
            "Very Poor",
            "Poor",
            "Average",
            "Good",
            "Excellent"
        )

        val spinnerAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            sleepQualities
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        sleepQualitySpinner.adapter = spinnerAdapter

        val moodGrid = mapOf(
            moodVeryBad to "Very Bad",
            moodBad to "Bad",
            moodFine to "Fine",
            moodGood to "Good",
            moodVeryGood to "Very Good"
        )

        moodGrid.forEach { (view, mood) ->
            view.setOnClickListener {
                highlightMood(view, mood)
            }
        }

        saveButton.setOnClickListener {
            val sleepHours = sleepHoursInput.text.toString().toIntOrNull()
            val selectedSleepQuality = sleepQualitySpinner.selectedItem.toString()

            selectedMood?.let { (_, mood) ->
                updateMood(mood, sleepHours, selectedSleepQuality)
            } ?: Toast.makeText(this, "Please select a mood", Toast.LENGTH_SHORT).show()
        }
    }

    private fun highlightMood(selected: LinearLayout, mood: String) {
        selectedMood?.first?.setBackgroundColor(Color.TRANSPARENT)
        selected.setBackgroundColor(Color.LTGRAY)
        selectedMood = Pair(selected, mood)

        // Update the image based on the selected mood
        updateMoodImage(mood)
    }

    private fun updateMoodImage(mood: String) {
        val imageRes = when (mood) {
            "Very Good" -> R.drawable.very_happy
            "Good" -> R.drawable.happy
            "Fine" -> R.drawable.neutral
            "Bad" -> R.drawable.sad
            "Very Bad" -> R.drawable.very_sad
            else -> R.drawable.neutral
        }
    }

    private fun updateMood(mood: String, sleepHours: Int?, sleepQuality: String) {
        val userId = sessionManager.getUserId()
        if (userId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val url = ApiConfig.UPDATE_MOOD_URL
        val requestQueue: RequestQueue = Volley.newRequestQueue(this)

        val stringRequest = object : StringRequest(
            Request.Method.POST,
            url,
            Response.Listener { response ->
                val jsonResponse = JSONObject(response)
                if (jsonResponse.optBoolean("success", false)) {
                    Toast.makeText(this, "Mood updated!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to update mood", Toast.LENGTH_SHORT).show()
                }
            },
            Response.ErrorListener {
                Toast.makeText(this, "API Error!", Toast.LENGTH_SHORT).show()
            }) {

            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["user_id"] = userId
                params["mood"] = mood
                params["sleep_hours"] = sleepHours?.toString() ?: "0"
                params["sleep_quality"] = sleepQuality
                return params
            }
        }

        requestQueue.add(stringRequest)
    }
}
