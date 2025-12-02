package com.example.zenfit

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class ForgetPassword : AppCompatActivity() {
    private lateinit var emailField: EditText
    private lateinit var sendBtn: Button
    private lateinit var loginText: TextView

    companion object {
        private const val TAG = "ForgetPassword"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forget_password)

        emailField = findViewById(R.id.emailField)
        sendBtn = findViewById(R.id.sendBtn)
        loginText = findViewById(R.id.loginText)

        sendBtn.setOnClickListener {
            val email = emailField.text.toString().trim()

            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            sendResetRequest(email)
        }

        loginText.setOnClickListener {
            finish()
        }
    }

    private fun sendResetRequest(email: String) {
        CoroutineScope(Dispatchers.IO).launch {
            var connection: HttpURLConnection? = null
            try {
                val urlString = ApiConfig.FORGOT_PASSWORD_URL
                Log.d(TAG, "Connecting to: $urlString")

                val url = URL(urlString)
                connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.doOutput = true
                connection.doInput = true
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                connection.connectTimeout = 10000
                connection.readTimeout = 10000

                val postData = "email=$email"
                Log.d(TAG, "Sending data: $postData")

                OutputStreamWriter(connection.outputStream).use {
                    it.write(postData)
                    it.flush()
                }

                val responseCode = connection.responseCode
                Log.d(TAG, "Response code: $responseCode")

                val response = if (responseCode == 200) {
                    BufferedReader(InputStreamReader(connection.inputStream)).use {
                        it.readText()
                    }
                } else {
                    BufferedReader(InputStreamReader(connection.errorStream)).use {
                        it.readText()
                    }
                }

                Log.d(TAG, "Response: $response")

                val jsonResponse = JSONObject(response)

                withContext(Dispatchers.Main) {
                    if (responseCode == 200 && jsonResponse.getString("status") == "success") {
                        val resetCode = jsonResponse.getString("reset_code")

                        Toast.makeText(
                            this@ForgetPassword,
                            "Reset code: $resetCode\n(Copy this code)",
                            Toast.LENGTH_LONG
                        ).show()

                        val intent = Intent(this@ForgetPassword, ResetPassword::class.java)
                        intent.putExtra("email", email)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(
                            this@ForgetPassword,
                            jsonResponse.optString("message", "Unknown error"),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error sending reset request", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@ForgetPassword,
                        "Error: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } finally {
                connection?.disconnect()
            }
        }
    }

    private fun applyTheme() {
        val prefs = getSharedPreferences("ThemePrefs", Context.MODE_PRIVATE)
        val isDarkMode = prefs.getBoolean("isDarkMode", false)

        val rootLayout = findViewById<LinearLayout>(R.id.main)
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
