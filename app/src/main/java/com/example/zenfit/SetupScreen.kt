package com.example.zenfit

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class SetupScreen : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_setup_screen)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        sessionManager = SessionManager(this)

        val weightInput = findViewById<EditText>(R.id.weightInput)
        val heightInput = findViewById<EditText>(R.id.heightInput)
        val activityInput = findViewById<EditText>(R.id.activityInput)
        val finishBtn = findViewById<Button>(R.id.finishBtn)

        finishBtn.setOnClickListener {
            val weight = weightInput.text.toString().trim()
            val height = heightInput.text.toString().trim()
            val activity = activityInput.text.toString().trim()

            if (weight.isEmpty()) {
                weightInput.error = "Weight is required"
                weightInput.requestFocus()
                return@setOnClickListener
            }

            if (height.isEmpty()) {
                heightInput.error = "Height is required"
                heightInput.requestFocus()
                return@setOnClickListener
            }

            if (activity.isEmpty()) {
                activityInput.error = "Daily activity is required"
                activityInput.requestFocus()
                return@setOnClickListener
            }

            updateProfile(weight, height, activity)
        }
    }

    private fun updateProfile(weight: String, height: String, activity: String) {
        val userId = sessionManager.getUserId()

        if (userId == null) {
            Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_SHORT).show()
            navigateToLogin()
            return
        }

        val requestQueue = Volley.newRequestQueue(this)
        val stringRequest = object : StringRequest(
            Request.Method.POST,
            ApiConfig.UPDATE_PROFILE_URL,
            { response ->
                try {
                    val jsonResponse = JSONObject(response)
                    val statusCode = jsonResponse.getInt("statuscode")

                    if (statusCode == 200) {
                        Toast.makeText(this, "Setup completed successfully!", Toast.LENGTH_SHORT).show()
                        sessionManager.logoutUser()
                        navigateToLogin()
                    } else {
                        val message = jsonResponse.getString("message")
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Toast.makeText(this, "Connection error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params["user_id"] = userId
                params["weight"] = weight
                params["height"] = height
                params["daily_activity_minutes"] = activity
                return params
            }
        }

        requestQueue.add(stringRequest)
    }

    private fun navigateToLogin() {
        val intent = Intent(this, Login::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
