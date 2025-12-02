package com.example.zenfit

import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class ChangePassword : AppCompatActivity() {
    private lateinit var oldPasswordField: EditText
    private lateinit var newPasswordField: EditText
    private lateinit var confirmPasswordField: EditText
    private lateinit var updatePasswordBtn: Button
    private lateinit var backBtn: ImageView
    private lateinit var toggleOldPassword: ImageView
    private lateinit var toggleNewPassword: ImageView
    private lateinit var toggleConfirmPassword: ImageView
    private lateinit var sessionManager: SessionManager

    private var isOldPasswordVisible = false
    private var isNewPasswordVisible = false
    private var isConfirmPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)

        sessionManager = SessionManager(this)

        oldPasswordField = findViewById(R.id.oldPasswordField)
        newPasswordField = findViewById(R.id.newPasswordField)
        confirmPasswordField = findViewById(R.id.confirmPasswordField)
        updatePasswordBtn = findViewById(R.id.updatePasswordBtn)
        backBtn = findViewById(R.id.backBtn)
        toggleOldPassword = findViewById(R.id.toggleOldPassword)
        toggleNewPassword = findViewById(R.id.toggleNewPassword)
        toggleConfirmPassword = findViewById(R.id.toggleConfirmNewPassword)

        backBtn.setOnClickListener { finish() }

        setupPasswordToggles()

        updatePasswordBtn.setOnClickListener {
            val oldPassword = oldPasswordField.text.toString()
            val newPassword = newPasswordField.text.toString()
            val confirmPassword = confirmPasswordField.text.toString()

            if (validatePasswords(oldPassword, newPassword, confirmPassword)) {
                changePassword(oldPassword, newPassword)
            }
        }
    }

    private fun setupPasswordToggles() {
        toggleOldPassword.setOnClickListener {
            isOldPasswordVisible = !isOldPasswordVisible
            togglePasswordVisibility(oldPasswordField, toggleOldPassword, isOldPasswordVisible)
        }

        toggleNewPassword.setOnClickListener {
            isNewPasswordVisible = !isNewPasswordVisible
            togglePasswordVisibility(newPasswordField, toggleNewPassword, isNewPasswordVisible)
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

    private fun validatePasswords(old: String, new: String, confirm: String): Boolean {
        if (old.isEmpty() || new.isEmpty() || confirm.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
            return false
        }

        if (new.length < 6) {
            Toast.makeText(this, "New password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            return false
        }

        if (new != confirm) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun changePassword(oldPassword: String, newPassword: String) {
        val userId = sessionManager.getUserId()

        if (userId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL(ApiConfig.CHANGE_PASSWORD_URL)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.doOutput = true

                val postData = "user_id=$userId&old_password=$oldPassword&new_password=$newPassword"
                OutputStreamWriter(connection.outputStream).use { it.write(postData) }

                val responseCode = connection.responseCode
                val response = connection.inputStream.bufferedReader().readText()
                val jsonResponse = JSONObject(response)

                withContext(Dispatchers.Main) {
                    if (responseCode == 200 && jsonResponse.getString("status") == "success") {
                        Toast.makeText(
                            this@ChangePassword,
                            "Password updated successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    } else {
                        Toast.makeText(
                            this@ChangePassword,
                            jsonResponse.getString("message"),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@ChangePassword,
                        "Error: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun applyTheme() {
        val prefs = getSharedPreferences("ThemePrefs", Context.MODE_PRIVATE)
        val isDarkMode = prefs.getBoolean("isDarkMode", false)

        val rootLayout = findViewById<LinearLayout>(R.id.rootLayout)
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
