package com.example.zenfit

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class Account : AppCompatActivity() {
    private lateinit var usernameField: EditText
    private lateinit var firstNameField: EditText
    private lateinit var lastNameField: EditText
    private lateinit var emailField: EditText
    private lateinit var weightField: EditText
    private lateinit var heightField: EditText
    private lateinit var saveChangesBtn: Button
    private lateinit var deleteAccountBtn: TextView
    private lateinit var backBtn: ImageView
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)

        sessionManager = SessionManager(this)

        usernameField = findViewById(R.id.usernameField)
        firstNameField = findViewById(R.id.firstNameField)
        lastNameField = findViewById(R.id.lastNameField)
        emailField = findViewById(R.id.emailField)
        weightField = findViewById(R.id.weightField)
        heightField = findViewById(R.id.heightField)
        saveChangesBtn = findViewById(R.id.saveChangesBtn)
        deleteAccountBtn = findViewById(R.id.deleteAccountBtn)
        backBtn = findViewById(R.id.backBtn)

        backBtn.setOnClickListener { finish() }

        saveChangesBtn.setOnClickListener {
            saveChanges()
        }

        deleteAccountBtn.setOnClickListener {
            showDeleteConfirmationDialog()
        }

        loadUserData()
    }

    private fun loadUserData() {
        val userId = sessionManager.getUserId()

        if (userId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL(ApiConfig.GET_ACCOUNT_URL)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.doOutput = true

                val postData = "user_id=$userId"
                OutputStreamWriter(connection.outputStream).use { it.write(postData) }

                val responseCode = connection.responseCode
                val response = connection.inputStream.bufferedReader().readText()
                val jsonResponse = JSONObject(response)

                withContext(Dispatchers.Main) {
                    if (responseCode == 200 && jsonResponse.getString("status") == "success") {
                        val userData = jsonResponse.getJSONObject("data")
                        usernameField.setText(userData.optString("username", ""))
                        firstNameField.setText(userData.optString("first_name", ""))
                        lastNameField.setText(userData.optString("last_name", ""))
                        emailField.setText(userData.optString("email", ""))
                        weightField.setText(userData.optString("weight", ""))
                        heightField.setText(userData.optString("height", ""))
                    } else {
                        Toast.makeText(
                            this@Account,
                            jsonResponse.getString("message"),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@Account,
                        "Error loading data: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun saveChanges() {
        val userId = sessionManager.getUserId()

        if (userId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val username = usernameField.text.toString().trim()
        val firstName = firstNameField.text.toString().trim()
        val lastName = lastNameField.text.toString().trim()
        val email = emailField.text.toString().trim()
        val weight = weightField.text.toString().trim()
        val height = heightField.text.toString().trim()

        if (username.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Username and email are required", Toast.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL(ApiConfig.UPDATE_ACCOUNT_URL)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.doOutput = true

                val postData = "user_id=$userId&username=$username&email=$email&first_name=$firstName&last_name=$lastName&weight=$weight&height=$height"
                OutputStreamWriter(connection.outputStream).use { it.write(postData) }

                val responseCode = connection.responseCode
                val response = connection.inputStream.bufferedReader().readText()
                val jsonResponse = JSONObject(response)

                withContext(Dispatchers.Main) {
                    if (responseCode == 200 && jsonResponse.getString("status") == "success") {
                        Toast.makeText(
                            this@Account,
                            "Account updated successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            this@Account,
                            jsonResponse.getString("message"),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@Account,
                        "Error saving changes: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Delete Account")
            .setMessage("Are you sure you want to permanently delete your account? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                deleteAccount()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteAccount() {
        val userId = sessionManager.getUserId()

        if (userId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL(ApiConfig.DELETE_ACCOUNT_URL)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.doOutput = true

                val postData = "user_id=$userId"
                OutputStreamWriter(connection.outputStream).use { it.write(postData) }

                val responseCode = connection.responseCode
                val response = connection.inputStream.bufferedReader().readText()
                val jsonResponse = JSONObject(response)

                withContext(Dispatchers.Main) {
                    if (responseCode == 200 && jsonResponse.getString("status") == "success") {
                        sessionManager.logoutUser()
                        Toast.makeText(
                            this@Account,
                            "Account deleted successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        val intent = Intent(this@Account, Login::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(
                            this@Account,
                            jsonResponse.getString("message"),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@Account,
                        "Error deleting account: ${e.message}",
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
