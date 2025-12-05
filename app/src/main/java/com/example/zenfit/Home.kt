package com.example.zenfit

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class Home : AppCompatActivity() {
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        sessionManager = SessionManager(this)

        // Top cards
        val settingsCard = findViewById<ImageView>(R.id.settingsCard)
        val moodCard = findViewById<ImageView>(R.id.moodCard)
        loadMoodCard(moodCard)

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
            val intent= Intent(this, MoodScreen::class.java)
            startActivity(intent)
        }

        // Workout button listeners
        btnStartWorkout.setOnClickListener {
            val intent = Intent(this, WorkoutLogging::class.java)
            startActivity(intent)
        }

        btnDetails.setOnClickListener {
            val intent = Intent(this, CaloriesActivity::class.java)
            startActivity(intent)
        }

        btnStartWorkoutMain.setOnClickListener {
            val intent = Intent(this, WorkoutLogging::class.java)
            startActivity(intent)
        }

        btnMenu.setOnClickListener {
            val intent = Intent(this, Menu::class.java)
            startActivity(intent)
        }

        navWorkout.setOnClickListener {
            val intent = Intent(this, WorkoutLogging::class.java)
            startActivity(intent)
        }

        navAdd.setOnClickListener {
            val intent = Intent(this, CreateWorkout::class.java)
            startActivity(intent)
        }

        navCalendar.setOnClickListener {
            val intent = Intent(this, CalendarActivity::class.java)
            startActivity(intent)
        }

        navProfile.setOnClickListener {
            val intent = Intent(this, Profile::class.java)
            startActivity(intent)
        }
    }

    private fun loadMoodCard(moodCard: ImageView) {

        val userId = sessionManager.getUserId()
        val url = ApiConfig.GET_MOOD_URL

        val request = object : StringRequest(
            Request.Method.POST, url,
            { response ->
                val json = JSONObject(response)

                if (json.getBoolean("success")) {
                    val mood = json.getString("mood")

                    val imageRes = when (mood) {
                        "Very Good" -> R.drawable.very_happy
                        "Good" -> R.drawable.happy
                        "Fine" -> R.drawable.neutral
                        "Bad" -> R.drawable.sad
                        "Very Bad" -> R.drawable.very_sad
                        else -> R.drawable.neutral
                    }

                    moodCard.setImageResource(imageRes)
                    moodCard.setColorFilter(
                        ContextCompat.getColor(this, android.R.color.white),
                        android.graphics.PorterDuff.Mode.SRC_IN
                    )
                }
            },
            {
                Toast.makeText(this, "Failed loading mood", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["user_id"] = userId!!
                return params
            }
        }

        Volley.newRequestQueue(this).add(request)
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
        val moodCard = findViewById<ImageView>(R.id.moodCard)
        loadMoodCard(moodCard)
        val welcomeText = findViewById<TextView>(R.id.welcomeText)
        val username = sessionManager.prefs.getString(SessionManager.KEY_USERNAME, "User")
        welcomeText.text = "Welcome back, $username!"
    }
}