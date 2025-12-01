package com.example.zenfit

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
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

class ResetPassword : AppCompatActivity() {
    private lateinit var resetCodeField: EditText
    private lateinit var newPasswordField: EditText
    private lateinit var confirmPasswordField: EditText
    private lateinit var resetBtn: Button
    private lateinit var togglePassword: ImageView
    private lateinit var toggleConfirmPassword: ImageView

    private var isPasswordVisible = false
    private var isConfirmPasswordVisible = false
    private lateinit var email: String

    companion object {
        private const val TAG = "ResetPassword"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)

        email = intent.getStringExtra("email") ?: ""

        resetCodeField = findViewById(R.id.resetCodeField)
        newPasswordField = findViewById(R.id.newPasswordField)
        confirmPasswordField = findViewById(R.id.confirmPasswordField)
        resetBtn = findViewById(R.id.resetBtn)
        togglePassword = findViewById(R.id.togglePassword)
        toggleConfirmPassword = findViewById(R.id.toggleConfirmPassword)

        setupPasswordToggles()

        resetBtn.setOnClickListener {
            val resetCode = resetCodeField.text.toString().trim()
            val newPassword = newPasswordField.text.toString()
            val confirmPassword = confirmPasswordField.text.toString()

            if (validateInputs(resetCode, newPassword, confirmPassword)) {
                resetPassword(resetCode, newPassword)
            }
        }
    }

    private fun setupPasswordToggles() {
        togglePassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            togglePasswordVisibility(newPasswordField, togglePassword, isPasswordVisible)
        }

        toggleConfirmPassword.setOnClickListener {
            isConfirmPasswordVisible = !isConfirmPasswordVisible
            togglePasswordVisibility(confirmPasswordField, toggleConfirmPassword, isConfirmPasswordVisible)
        }
    }

    private fun togglePasswordVisibility(field: EditText, toggle: ImageView, isVisible: Boolean) {
        if (isVisible) {
            field.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            toggle.setImageResource(R.drawable.eye)
        } else {
            field.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            toggle.setImageResource(R.drawable.eye)
        }
        field.setSelection(field.text.length)
    }

    private fun validateInputs(code: String, password: String, confirm: String): Boolean {
        if (code.isEmpty()) {
            Toast.makeText(this, "Please enter reset code", Toast.LENGTH_SHORT).show()
            return false
        }

        if (code.length != 6) {
            Toast.makeText(this, "Reset code must be 6 digits", Toast.LENGTH_SHORT).show()
            return false
        }

        if (password.isEmpty()) {
            Toast.makeText(this, "Please enter new password", Toast.LENGTH_SHORT).show()
            return false
        }

        if (password.length < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            return false
        }

        if (password != confirm) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun resetPassword(resetCode: String, newPassword: String) {
        CoroutineScope(Dispatchers.IO).launch {
            var connection: HttpURLConnection? = null
            try {
                val urlString = ApiConfig.RESET_PASSWORD_URL
                Log.d(TAG, "Connecting to: $urlString")

                val url = URL(urlString)
                connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.doOutput = true
                connection.doInput = true
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                connection.connectTimeout = 10000
                connection.readTimeout = 10000

                val postData = "email=$email&reset_code=$resetCode&new_password=$newPassword"
                Log.d(TAG, "Sending data: email=$email&reset_code=***&new_password=***")

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
                        Toast.makeText(
                            this@ResetPassword,
                            "Password reset successfully!",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Go back to login
                        val intent = Intent(this@ResetPassword, Login::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(
                            this@ResetPassword,
                            jsonResponse.optString("message", "Unknown error"),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error resetting password", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@ResetPassword,
                        "Error: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } finally {
                connection?.disconnect()
            }
        }
    }
}
