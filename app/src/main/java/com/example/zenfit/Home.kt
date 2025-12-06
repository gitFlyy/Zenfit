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
import java.util.Calendar
import kotlin.printStackTrace
import kotlin.ranges.rangeTo
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.messaging.FirebaseMessaging
import kotlin.printStackTrace
import kotlin.text.compareTo

class Home : AppCompatActivity() {
    private lateinit var sessionManager: SessionManager
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission granted - get FCM token and update server
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    updateFCMToken(token)
                }
            }
            Toast.makeText(this, "Notifications enabled", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Notifications disabled. You won't receive workout completion alerts.", Toast.LENGTH_LONG).show()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        sessionManager = SessionManager(this)
        checkNotificationPermission()
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
        fetchTodayCalories()
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
            val intent = Intent(this, UploadPost::class.java)
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
    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Permission already granted - ensure FCM token is updated
                    FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val token = task.result
                            updateFCMToken(token)
                        }
                    }
                }
                else -> {
                    // Request permission
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            // For Android 12 and below, permission is granted automatically
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    updateFCMToken(token)
                }
            }
        }
    }

    private fun updateFCMToken(token: String) {
        val userId = sessionManager.getUserId() ?: return

        val request = object : StringRequest(
            Request.Method.POST,
            ApiConfig.UPDATE_FCM_TOKEN_URL,
            { response ->
                // Token updated successfully
            },
            { error ->
                error.printStackTrace()
            }
        ) {
            override fun getParams() = hashMapOf(
                "userId" to userId,
                "fcmToken" to token
            )
        }

        Volley.newRequestQueue(this).add(request)
    }
    private fun fetchTodayCalories() {
        val userId = sessionManager.getUserId() ?: ""
        val url = ApiConfig.GET_WORKOUT_HISTORY_URL

        val request = object : StringRequest(
            Request.Method.POST, url,
            { response ->
                try {
                    val json = JSONObject(response)
                    if (json.getBoolean("success")) {
                        val workoutsArray = json.getJSONArray("history")
                        var totalCalories = 0
                        var sessionCount = 0

                        val todayStart = getTodayStartTimestamp()
                        val todayEnd = getTodayEndTimestamp()

                        for (i in 0 until workoutsArray.length()) {
                            val obj = workoutsArray.getJSONObject(i)
                            val completedDate = obj.getLong("completed_date")

                            if (completedDate in todayStart..todayEnd) {
                                totalCalories += obj.optInt("calories_burned", 0)
                                sessionCount++
                            }
                        }

                        val caloriesValue = findViewById<TextView>(R.id.caloriesValue)
                        caloriesValue.text = "$totalCalories kCal"

                        val noOfSessions = findViewById<TextView>(R.id.noOfSessions)
                        noOfSessions.text = "Sessions:\n$sessionCount"
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
            { error ->
                error.printStackTrace()
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
        fetchTodayCalories()
    }
}