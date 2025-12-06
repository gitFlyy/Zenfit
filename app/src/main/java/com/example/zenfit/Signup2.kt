package com.example.zenfit

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import java.util.Calendar
import kotlin.or
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.FirebaseApp
class Signup2 : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_signup2)
        FirebaseApp.initializeApp(this)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        sessionManager = SessionManager(this)

        val username = intent.getStringExtra("username") ?: ""
        val email = intent.getStringExtra("email") ?: ""
        val password = intent.getStringExtra("password") ?: ""
        val profileImage = intent.getStringExtra("profile_image")

        val firstNameInput = findViewById<EditText>(R.id.firstNameInput)
        val lastNameInput = findViewById<EditText>(R.id.lastNameInput)
        val dateInput = findViewById<EditText>(R.id.dateOfBirthInput)
        val locationInput = findViewById<EditText>(R.id.locationInput)
        val cityInput = findViewById<EditText>(R.id.cityInput)

        dateInput.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePicker = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = "$selectedYear-${String.format("%02d", selectedMonth + 1)}-${String.format("%02d", selectedDay)}"
                dateInput.setText(selectedDate)
            }, year, month, day)

            datePicker.show()
        }

        val createAccountButton = findViewById<Button>(R.id.createAccountButton)
        createAccountButton.setOnClickListener {
            val firstName = firstNameInput.text.toString().trim()
            val lastName = lastNameInput.text.toString().trim()
            val dateOfBirth = dateInput.text.toString().trim()
            val location = locationInput.text.toString().trim()
            val city = cityInput.text.toString().trim()

            if (firstName.isEmpty() || lastName.isEmpty()) {
                Toast.makeText(this, "Please enter your full name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (dateOfBirth.isEmpty()) {
                Toast.makeText(this, "Please select date of birth", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            performSignup(username, email, password, firstName, lastName, dateOfBirth, location, city, profileImage)
        }
    }

    private fun performSignup(
        username: String,
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        dateOfBirth: String,
        location: String,
        city: String,
        profileImage: String?
    ) {
        val requestQueue = Volley.newRequestQueue(this)
        val stringRequest = object : StringRequest(
            Request.Method.POST,
            ApiConfig.SIGNUP_URL,
            { response ->
                Log.d("Signup2", "Server Response: $response")
                try {
                    val jsonResponse = JSONObject(response)
                    val statusCode = jsonResponse.getInt("statuscode")

                    if (statusCode == 200) {
                        val userData = jsonResponse.getJSONObject("user")
                        val userId = userData.getString("id")

                        sessionManager.createLoginSession(
                            userId,
                            userData.getString("username"),
                            userData.getString("email"),
                            "$firstName $lastName"
                        )

                        // Update FCM token after successful signup
                        updateFcmTokenForNewUser(userId)

                        Toast.makeText(this, "Account created successfully!", Toast.LENGTH_LONG).show()
                        val intent = Intent(this, SetupScreen::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    } else {
                        val message = jsonResponse.getString("message")
                        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                        Log.e("Signup2", "Error: $message")
                    }
                } catch (e: Exception) {
                    Log.e("Signup2", "Parse Error: ${e.message}", e)
                    Toast.makeText(this, "Server response error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            },
            { error ->
                val errorMsg = error.message ?: "Unknown error"
                Log.e("Signup2", "Network Error: $errorMsg", error)
                Toast.makeText(this, "Connection error: $errorMsg", Toast.LENGTH_LONG).show()
            }
        ) {
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params["username"] = username
                params["email"] = email
                params["password"] = password
                params["first_name"] = firstName
                params["last_name"] = lastName
                params["date_of_birth"] = dateOfBirth
                params["location"] = location
                params["city"] = city
                if (!profileImage.isNullOrEmpty()) {
                    params["profile_image"] = profileImage
                    Log.d("Signup2", "Profile image size: ${profileImage.length} characters")
                }
                Log.d("Signup2", "Sending signup request with params: username=$username, email=$email")
                return params
            }
        }

        requestQueue.add(stringRequest)
    }

    private fun updateFcmTokenForNewUser(userId: String) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                Log.d("Signup2", "FCM Token retrieved: $token")

                val url = ApiConfig.UPDATE_FCM_TOKEN_URL
                val request = object : StringRequest(
                    Request.Method.POST, url,
                    { response ->
                        try {
                            val json = JSONObject(response)
                            if (json.getString("status") == "success") {
                                Log.d("Signup2", "FCM token saved successfully")
                            }
                        } catch (e: Exception) {
                            Log.e("Signup2", "FCM token save error: ${e.message}")
                        }
                    },
                    { error ->
                        Log.e("Signup2", "FCM token network error: ${error.message}")
                    }
                ) {
                    override fun getParams() = hashMapOf(
                        "userId" to userId,
                        "fcmToken" to token
                    )
                }

                Volley.newRequestQueue(this).add(request)
            } else {
                Log.e("Signup2", "Failed to retrieve FCM token: ${task.exception?.message}")
            }
        }
    }


    private fun applyTheme() {
        val prefs = getSharedPreferences("ThemePrefs", Context.MODE_PRIVATE)
        val isDarkMode = prefs.getBoolean("isDarkMode", false)

        val rootLayout = findViewById<RelativeLayout>(R.id.main)
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
